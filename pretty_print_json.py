#!/usr/bin/env python

"""
Pretty-prints JSON output from BLOG.

Also serves as an example of how to iterate through the JSON output.

Usage:
    ./pretty_print_json.py out.json
"""


import json
import sys


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print >>sys.stderr, "Usage: {} input.json".format(sys.argv[0])
        sys.exit(1)
    data = json.load(open(sys.argv[1]))
    print "["
    for i, (query_str, entries) in enumerate(data):
        print "    [{}, [".format(json.dumps(query_str))
        for j, (log_prob, value) in enumerate(entries):
            print "        [{}, {}]{}".format(
                json.dumps(log_prob),
                json.dumps(value),
                "" if j == len(entries) - 1 else ",")
        print "    ]]{}".format("" if i == len(data) - 1 else ",")
    print "]"
