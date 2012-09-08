""" Runs all currently available BLOG and DBLOG examples
    using each available sampling engine and
    displays the time each example takes to complete.

    This script will soon graph the time taken per sample
    and the variance as a function of the number n of samples
    thus far. Once DBLOG particle filtering is supported,
    this script will graph the time and variance as a function
    of time t as well.

    @author rbharath
    @date September 4th, 2012
"""
import os
import time
import subprocess
import threading
from optparse import OptionParser

blog = "./run.sh"
example = "example"
working_examples = []
broken_examples = []
samplers = ["blog.sample.LWSampler", "blog.sample.MHSampler",
            "blog.sample.modular.ModularLWSampler"]
result = 0
process = None

class ArgParser(object):
    def __init__(self):
        global samplers
        self.parser = OptionParser()
        self.parser.add_option("-s", "--sampler", dest="samplers",
                                action="append", default=samplers,
                                metavar="SAMPLER",
                                help="Use this sampler when evaluating")
        self.parser.add_option("--timeout", dest="timeout",
                                action="store", type="int",
                                default=100, metavar="TIMEOUT",
                                help="Timeout for any one example")
    def parse_args(self):
        (options, _) = self.parser.parse_args()
        return options

def sampler_base(s):
    return s.split(".")[-1:][0]

def find_examples(examples_dir, options):
    example_paths = []
    for dirname, subdirnames, filenames in os.walk(examples_dir):
        for filename in filenames:
            example_path = os.path.join(dirname, filename)
            example_paths.append(example_path)
    return example_paths

def run_with_timeout(command, timeout):
    global result
    global process
    def thread_fun():
        global process
        print "Blog Thread Running " + command + " started"
        devnull = open('/dev/null', 'w')
        process = subprocess.Popen(command, shell=True, stdout=devnull,
                stderr=devnull)
        process.communicate()
    thread = threading.Thread(target=thread_fun)
    thread.start()
    thread.join(timeout)

    if thread.isAlive():
        print "Killing Example"
        try:
            process.kill()
        except OSError:
            pass
        print "Going To Sleep"
        time.sleep(10)
        print "Woken Up"
        thread.join()
    result = process.returncode

def run_examples(example_paths, options):
    global blog
    global result
    working_examples = []
    broken_examples = []
    for example_path in example_paths:
        timings = {}
        for sampler in samplers:
            command = [blog, "--sampler", sampler, example_path]
            start_time = time.time()
            run_with_timeout(" ".join(command), options.timeout)
            end_time = time.time()
            out = end_time - start_time
            if result != 0:
                out = "Fail!"
                broken_examples.append((example_path, sampler))
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
    for (broken_example_path, sampler) in broken_examples:
        broken_example = os.path.basename(broken_example_path)
        out = "\t" + str(broken_example) + " on " + str(sampler)
        print out

if __name__ == "__main__":
    argParser = ArgParser()
    options = argParser.parse_args()
    examples_dir = os.path.join(os.getcwd(), example)
    example_paths = find_examples(examples_dir, options)
    (working_examples, broken_examples) = run_examples(example_paths, options)
    print_results(len(example_paths), working_examples, broken_examples, options)
