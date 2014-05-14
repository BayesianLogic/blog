#!/usr/bin/env python

"""
Plot the car's trajectory as output by BLOG.

Usage:
    ./plot_blog_traj.py out.json
"""


import json
import matplotlib.pyplot as plt
import numpy as np
import sys


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print >>sys.stderr, "Usage: {} input.json".format(sys.argv[0])
        sys.exit(1)
    data = json.load(open(sys.argv[1]))
    states = []
    for query_str, entries in data:
        # query_str is like "state(@123)"
        timestep = int(query_str[7:-1])
        # Pick particle with largest weight.
        best_entry = (-np.inf, None)
        for log_prob, value in entries:
            if log_prob > best_entry[0]:
                best_entry = (log_prob, value)
        # state is like "[0.0; 0.0; 0.0]"
        state = json.loads(best_entry[1].replace(';', ','))
        states.append(state)
    states = np.array(states)

    # State is [x, y, theta].
    # Plot x, y trajectory.
    plt.figure(figsize=(8, 8))
    xs = states[:, 0]
    ys = states[:, 1]
    plt.plot(xs, ys)
    plt.plot([-7, -7, 7, 7, -7], [-7, 7, 7, -7, -7], 'k')
    plt.xlim(-15, 15)
    plt.ylim(-15, 15)
    plt.show()
