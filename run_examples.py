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

examples_dir = os.path.join(os.getcwd(), "example")
blog = "./run.sh"
working_examples = []
broken_examples = []
num_examples = 0

for dirname, subdirnames, filenames in os.walk(examples_dir):
    for filename in filenames:
        num_examples += 1
        example_path = os.path.join(dirname, filename)
        command = [blog, example_path]
        print "Command: " + " ".join(command)
        try:
            start_time = time.time()
            output = subprocess.check_output(command)
            end_time = time.time()
            working_examples.append((example_path, end_time - start_time))
        except subprocess.CalledProcessError:
            print "Failure!"
            broken_examples.append(example_path)
        print output

print "Ran " + str(num_examples) + " examples in total"
print str(len(working_examples)) + " passed:"
for working_example_path, time in working_examples:
    working_example = os.path.basename(working_example_path)
    print "\t" + str(working_example) + ": " + str(round(time,2)) + " seconds"
print str(len(broken_examples)) + " failed."
for broken_example_path in broken_examples:
    broken_example = os.path.basename(broken_example_path)
    print "\t" + str(broken_example)
