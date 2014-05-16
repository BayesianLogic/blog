#!/usr/bin/env python

"""
Evaluate the trajectory output by BLOG.

Computes the trajectory in two ways:
- map trajectory
- expected trajectory

Compares the error on the above two with the error of the noisy GPS inputs.

Example usage:
    python eval_blog_traj.py 2_bend out.json --plot
"""


import argparse
import json
import matplotlib.pyplot as plt
import numpy as np
import sys

from ppaml_car.data import path_for_dataset
from ppaml_car.data import read_data

from compare_ground_noisy import compute_error
from compare_ground_noisy import gps_from_readings
from compare_ground_noisy import plot_traj


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


def traj_from_blog(timestamps, data, aggregator_func):
    states = []
    for query_str, entries in data:
        state = aggregator_func(entries)
        states.append(state)
    states = np.array(states)

    # Convert from (x, y, theta) to (time, lat, lon, theta).
    traj = np.empty((len(states), 4))
    traj[:, 0] = timestamps
    traj[:, 1] = states[:, 1]
    traj[:, 2] = states[:, 0]
    traj[:, 3] = states[:, 2]
    return traj


if __name__ == "__main__":
    # Parse command-line args.
    parser = argparse.ArgumentParser(
        description='Evaluate trajectory output by BLOG.')
    parser.add_argument('dataset')
    parser.add_argument('json_file')
    parser.add_argument('--plot', action='store_true')
    args = parser.parse_args()

    # parser.add_argument('aggregator', choices=['map', 'avg'])
    # aggregator_func = {
    #     'map': most_likely_particle,
    #     'avg': weighted_average_of_particles,
    # }[args.aggregator]

    # Load GPS readings from dataset.
    ground_readings = read_data(path_for_dataset(args.dataset, 'ground'))
    noisy_readings = read_data(path_for_dataset(args.dataset, 'noisy'))
    ground_traj = gps_from_readings(ground_readings)
    noisy_traj = gps_from_readings(noisy_readings)

    # Load trajectory from BLOG JSON output.
    data = json.load(open(sys.argv[2]))
    for query_str, entries in data:
        for i in xrange(len(entries)):
            # entry is like (log_prob, "[0.0; 0.0; 0.0]")
            entries[i][1] = json.loads(entries[i][1].replace(';', ','))
    timestamps = ground_traj[:, 0]
    map_traj = traj_from_blog(timestamps, data, most_likely_particle)
    avg_traj = traj_from_blog(timestamps, data, weighted_average_of_particles)

    # Evaluate trajectories.
    # All trajectories are lists of (time, lat, lon, theta).
    for traj, name in (
            (noisy_traj, 'noisy'),
            (map_traj, 'map'),
            (avg_traj, 'avg')):
        print "{:5}: {:10.5f}".format(name, compute_error(ground_traj, traj))

    # Optionally plot trajectories.
    if args.plot:
        plt.figure(figsize=(8, 8))
        for traj, name in (
                (ground_traj, 'ground'),
                (noisy_traj, 'noisy'),
                (map_traj, 'map'),
                (avg_traj, 'avg')):
            plot_traj(plt.gca(), name, traj)
        plt.plot([-7, -7, 7, 7, -7], [-7, 7, 7, -7, -7], 'k')
        plt.xlim(-8, 8)
        plt.ylim(-8, 8)
        plt.legend()
        plt.show()
