""" Runs all currently available BLOG and DBLOG examples
    using each available sampling engine and
    displays the time each example takes to complete.

    The script also graphs the variation distance from the true posterior
    as a function of the number of samples n for each sampler s on each example.

    The script will soon be upgraded to graph MSE and other forms of error.
    Once DBLOG particle filtering is supported, this script will graph particle
    statistics as well.

    @author rbharath
    @date September 14th, 2012
"""
import os
import sys
import signal
import time
import re
import math
import subprocess
import threading
import matplotlib.pyplot as plot
from optparse import OptionParser

# Global Constants
blog = "./run.sh"
example = "example"
solutions = "example/solutions"
figures = "example/figures"
figure_type = ".png"
# Used to Gather Data on working and broken examples
working_examples = []
broken_examples = []
# Define Samplers
LWSampler = "blog.sample.LWSampler"
MHSampler = "blog.sample.MHSampler"
ModularLWSampler = "blog.sample.modular.ModularLWSampler"
samplers = [LWSampler, MHSampler, ModularLWSampler]
# Set your favorite color for your sampler
colors = {}
colors[LWSampler] = "r"
colors[MHSampler] = "b"
colors[ModularLWSampler] = "g"
# Global variables used to pass information between threads
result = 0
process = None
output = ""
# all_data is a global hash table mapping the example_path of a particular example
# and a particular sampler to the data associated with that example
#   all_data[example_path][sampler] = data for example run with sampler
all_data = {}

class ArgParser(object):
    """ Arguments accepted by this script.
    """
    def __init__(self):
        global samplers
        self.parser = OptionParser()
        self.parser.add_option("-s", "--sampler", dest="samplers",
                                type="string",
                                action="append", metavar="SAMPLER",
                                help="Use this sampler when evaluating")
        self.parser.add_option("--timeout", dest="timeout",
                                action="store", type="int",
                                default=100, metavar="TIMEOUT",
                                help="Timeout for any one example")
        self.parser.add_option("-e", "--example", dest="examples",
                                action="append", metavar="EXAMPLE",
                                type="string",
                                help="Run this example")
        self.parser.add_option("-n", "--num_samples", dest="n",
                                action="store", type="int",
                                metavar="N", default=50000,
                                help="Run inference engine for this many samples")
        self.parser.add_option("-q", "--query_interval", dest="q",
                                action="store", type="int",
                                default=5000,
                                metavar="QUERY_INTERVAL",
                                help="report query results per this many samples")
    def parse_args(self):
        global samplers
        (options, _) = self.parser.parse_args()
        if options.samplers is None:
            options.samplers = samplers
        return options

def sampler_base(s):
    """Given the full name of sampler, e.g. blog.sample.MHSampler, returns
       its base, i.e. MHSampler
   """
    return s.split(".")[-1:][0]

def find_examples(examples_dir, options):
    """ List the paths to all examples currently residing in the
        examples directory.
    """
    example_paths = []
    if options.examples is None:
        for dirname, subdirnames, filenames in os.walk(examples_dir):
            if os.path.basename(dirname) == "figures":
                continue
            for filename in filenames:
                example_path = os.path.join(dirname, filename)
                example_paths.append(example_path)
    else:
        for example in options.examples:
            example_paths.append(example)
    return example_paths

def parse_distribution(index, lines, data, samplesSoFar):
    """ Parse the distribution reported in lines starting at line number index and
        save the results to data. Returns the updated index
        Precondition: Must be called with index pointing to a Distribution
    """
    query_words = lines[index].split()
    if 'for' not in query_words:
        return index
    for_index = query_words.index('for')
    remaining = query_words[for_index + 1:]
    query_var = ' '.join(remaining)
    if query_var not in data:
        data[query_var] = {}
    # We should not have called this function for the same value of
    # samplesSoFar Previously
    data[query_var][samplesSoFar] = {}
    index += 1
    while (index < len(lines) and lines[index] is not "" and "Distribution"
            not in lines[index] and "Done" not in lines[index]):
        words = lines[index].split()
        if len(words) < 2:
            index += 1
            continue
        try:
            prob = float(words[0])
        except ValueError:
            prob = 0
        val = words[1]
        data[query_var][samplesSoFar][val] = prob
        index += 1
    return index

def parse_query_result(index, lines, data):
    """ Parse the results of reported queries.
        Precondition: Must be called on line where Query Results is stated.
    """
    # Skip the line ======== Query Results =========
    index += 1
    if "Iteration" not in lines[index]:
        samplesSoFar = 0
    else:
        iteration_words = lines[index].split()
        index += 1
        try:
            samplesSoFar = int(iteration_words[1].strip(":"))
        except ValueError:
            samplesSoFar = 0
    return parse_distribution(index, lines, data, samplesSoFar)


def parse_blog_output(output):
    """ Parses the blog output and stores it in hash table data. data is a
    multilevel dictionary. The keys of data are the query variables.  For each
    query variable qVar, data[qVar] is another dictionary that maps samplesSoFar
    to distributions.  That is, data[qVar][samplesSoFar] is a distribution. A
    distribution itself is another dictionary, mapping values to probabilities.
    The total structure looks like:

        data[qVar][N][val] = P(qVar = val) according to N samples drawn so far
    """
    data = {}
    index = 0
    lines = output.split("\n")
    lines = [line.strip() for line in lines]

    while True:
        while index < len(lines) and "Query Results" not in lines[index]:
            index += 1
        if index >= len(lines):
            break
        index = parse_query_result(index, lines, data)
    return data

def run_with_timeout(command, timeout):
    """ Run given command for timeout seconds. If command does not exit
        before timeout is reached, forcibly kill spawned process.
    """
    global result
    global output
    global process
    def thread_fun():
        global process
        global output
        global result
        print "Blog Thread Running " + command + " started"
        process = subprocess.Popen(command, shell=True, stdout=subprocess.PIPE,
                stderr=subprocess.PIPE, preexec_fn=os.setsid)
        output = process.communicate()[0]
        result = process.returncode
    thread = threading.Thread(target=thread_fun)
    thread.start()
    thread.join(timeout)

    if thread.isAlive():
        print "Killing Example"
        try:
            output = ""
            result = -1
            #process.kill()
            os.killpg(process.pid, signal.SIGTERM)
        except OSError:
            pass
        print "Going To Sleep"
        time.sleep(10)
        print "Woken Up"
        thread.join()
    return

def variation_distance(data1, data2):
    """ Returns the total variation distance between the distributions
        represented by data1 and data2
    """
    union_keys = list(set(data1.keys()) | set(data2.keys()))
    distance = 0
    for key in union_keys:
        if key in data1:
            val1 = data1[key]
        else:
            val1 = 0
        if key in data2:
            val2 = data2[key]
        else:
            val2 = 0
        distance += math.fabs(val1 - val2)
    return .5 * distance

def generate_graphs(examples_dir):
    """ Generate Graphs from data gathered on examples. These graphs currently
        show the changes in variation distance over number samples.
    """
    global all_data
    global solutions
    global colors

    for example_path in all_data:
        sampler_data = all_data[example_path]
        solution_path = os.path.join(solutions,
                                     os.path.basename(example_path))
        # Set the solution data
        if os.path.isfile(solution_path):
            solution = open(solution_path)
            output = solution.read()
            solution_data = parse_blog_output(output)
        else:
            # Construct solution from LWSampler results
            LWSampler = "blog.sample.LWSampler"
            if LWSampler in sampler_data:
                lw_data = sampler_data[LWSampler]
            else:
                continue
            if len(lw_data.keys()) == 0:
                print ("No information from LWSampler for " +
                        os.path.basename(example_path))
                continue
            solution_data = {}
            for qVar in lw_data:
                lw_qvar_data = lw_data[qVar]
                N = max(lw_qvar_data.keys())
                # Hack to make solution_data look like the out of
                # parse_blog_output
                solution_data[qVar] = {}
                solution_data[qVar][0] = lw_qvar_data[N]
            #all_data[example_path]["solution"] = solution_data
        # Use the solution data to generate the graphs for example_path
        for qVar in solution_data.keys():
            plot.clf()
            for sampler in sampler_data.keys():
                if (sampler not in sampler_data or
                        qVar not in sampler_data[sampler]):
                    continue
                data = sampler_data[sampler][qVar]
                xs = []
                ys = []
                for n in sorted(data.keys()):
                    dist = variation_distance(data[n],
                                              solution_data[qVar][0])
                    xs.append(n)
                    ys.append(dist)
                plot.scatter(xs, ys, c=colors[sampler],
                             label=sampler_base(sampler))
            base = os.path.relpath(example_path, examples_dir)
            graph_name = get_graph_name(base, qVar)
            print "graph_name: " + str(graph_name)
            plot.ylabel("Variation Distance")
            plot.xlabel("Num Samples")
            plot.title(str(os.path.basename(example_path) + ": " + str(qVar)))
            plot.legend()
            plot.savefig(graph_name)

def get_graph_name(example_path, qVar):
    """ Get the name of the graph for a given example_path, sampler,
        and query variable.
    """
    global figures
    global figure_type
    example = str(example_path)
    example += "_" + str(qVar)
    example = example.replace("/","_").replace(".","_")
    example += figure_type
    return str(os.path.join(figures, example))

def run_examples(example_paths, options):
    """ Run all examples in the examples folder, gather timing statistics.
    """
    global blog
    global result
    global output
    working_examples = []
    broken_examples = []
    for example_path in example_paths:
        timings = {}
        for sampler in options.samplers:
            command = [blog, "--sampler", sampler, example_path]
            command += ["-n", options.n, "-q", options.q]
            result = 0
            start_time = time.time()
            # This function spawns a thread to execute command. That thread
            # changes the value of global variables result and output
            run_with_timeout(" ".join([str(c) for c in command]), options.timeout)
            end_time = time.time()
            out = end_time - start_time
            if result != 0:
                out = "Fail!"
                broken_examples.append((example_path, sampler))
            else:
                data = parse_blog_output(output)
                if example_path not in all_data:
                    all_data[example_path] = {}
                all_data[example_path][sampler] = data
            timings[sampler] = out
        working_examples.append((example_path, timings))
    return (working_examples, broken_examples)

def print_results(num_examples, working_examples, broken_examples, options):
    print "Ran " + str(num_examples) + " examples in total"
    max_working_len = max(map(len,
                            [os.path.basename(x[0]) for x in working_examples]))
    max_sampler_len = max(map(len, [sampler_base(s) for s in samplers]))
    # Magic numbers below are for correct alignment
    out =  "\t" + " " * (max_working_len + 3)
    for sampler in samplers:
        base = sampler_base(sampler)
        diff_len = max_sampler_len - len(base)
        out += base + " " * (diff_len + 1)
    print out
    for working_example_path, timings in working_examples:
        working_example = os.path.basename(working_example_path)
        diff_len = max_working_len - len(working_example)
        out = "\t" + str(working_example) + ": " + " " * diff_len
        for sampler in sorted(timings.keys()):
            try:
                time = round(timings[sampler], 2)
            except TypeError:
                time = timings[sampler]
            diff_len = max_sampler_len - len(str(time))
            out += " " * (diff_len + 1) + str(time)
        print out
    out = str(len(working_examples) * len(samplers) - len(broken_examples))
    out += " passed."
    print out
    print str(len(broken_examples)) + " failed:"

if __name__ == "__main__":
    argParser = ArgParser()
    options = argParser.parse_args()
    examples_dir = os.path.join(os.getcwd(), example)
    example_paths = find_examples(examples_dir, options)
    (working_examples, broken_examples) = run_examples(example_paths, options)
    generate_graphs(examples_dir)
    print_results(len(example_paths), working_examples, broken_examples, options)
