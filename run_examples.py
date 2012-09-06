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

blog = "./run.sh"
example = "example"
working_examples = []
broken_examples = []

def find_examples(examples_dir):
    example_paths = []
    for dirname, subdirnames, filenames in os.walk(examples_dir):
        for filename in filenames:
            example_path = os.path.join(dirname, filename)
            example_paths.append(example_path)
    return example_paths

def run_examples(example_paths):
    global blog
    working_examples = []
    broken_examples = []
    for example_path in example_paths:
        command = [blog, example_path]
        try:
            start_time = time.time()
            output = subprocess.check_output(command)
            end_time = time.time()
            working_examples.append((example_path, end_time - start_time))
        except subprocess.CalledProcessError:
            print "Failure!"
            broken_examples.append(example_path)
        print output
    return (working_examples, broken_examples)

def print_results(num_examples, working_examples, broken_examples):
    print "Ran " + str(num_examples) + " examples in total"
    print str(len(working_examples)) + " passed:"
    max_working_len = max(map(len,
                            [os.path.basename(x[0]) for x in working_examples]))
    for working_example_path, time in working_examples:
        working_example = os.path.basename(working_example_path)
        diff_len = max_working_len - len(working_example)
        out = "\t" + str(working_example) + ": " + " " * diff_len
        out += str(round(time,2)) + " seconds"
        print out
    print str(len(broken_examples)) + " failed."
    for broken_example_path in broken_examples:
        broken_example = os.path.basename(broken_example_path)
        out = "\t" + str(broken_example)
        print out

if __name__ == "__main__":
    examples_dir = os.path.join(os.getcwd(), example)
    example_paths = find_examples(examples_dir)
    (working_example_data, broken_example_data) = run_examples(example_paths)
    print_results(len(example_paths), working_example_data, broken_example_data)
