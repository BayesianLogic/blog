#!/usr/bin/env python

"""
Plot the car's trajectory as output by BLOG.

Usage:
    ./plot_blog_traj.py map/avg out.json
"""


import json
import matplotlib.pyplot as plt
import numpy as np
import sys


def most_likely_particle(entries):
    """
    "MAP" aggregator: return the particle with the largest weight.
    """
    best_log_prob = -np.inf
    best_value = None
    for log_prob, value in entries:
        if log_prob > best_log_prob:
            best_log_prob = log_prob
            best_value = value
    return best_value


def weighted_average_of_particles(entries):
    """
    "Expected-value" aggregator: return weighted average of particles.
    """
    average_value = 0.0
    sum_weights = 0.0
    for log_prob, value in entries:
        weight = np.exp(log_prob)
        sum_weights += weight
        average_value += weight * np.array(value)
    average_value /= sum_weights
    return average_value


if __name__ == "__main__":
    if len(sys.argv) != 3:
        print >>sys.stderr, "Usage: {} map/avg input.json".format(sys.argv[0])
        sys.exit(1)
    if sys.argv[1] == 'map':
        aggregator_func = most_likely_particle
    elif sys.argv[1] == 'avg':
        aggregator_func = weighted_average_of_particles
    else:
        raise RuntimeError("unknown method '{}'".format(sys.argv[1]))
    data = json.load(open(sys.argv[2]))

    states = []
    for query_str, entries in data:
        for i in xrange(len(entries)):
            # entry is like (log_prob, "[0.0; 0.0; 0.0]")
            entries[i][1] = json.loads(entries[i][1].replace(';', ','))
        state = aggregator_func(entries)
        states.append(state)
    states = np.array(states)

    # State is [x, y, theta].
    # Plot x, y trajectory.
    plt.figure(figsize=(8, 8))
    xs = states[:, 0]
    ys = states[:, 1]
    plt.plot(xs, ys)
    plt.plot([-7, -7, 7, 7, -7], [-7, 7, 7, -7, -7], 'k')
    plt.xlim(-8, 8)
    plt.ylim(-8, 8)
    plt.show()
