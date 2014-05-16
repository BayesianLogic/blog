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

from ppaml_car.data import path_for_dataset
from ppaml_car.data import read_data

from compare_ground_noisy import compute_error
from compare_ground_noisy import gps_from_readings
from compare_ground_noisy import plot_traj


def read_blog_json_data(path):
    """
    Load BLOG JSON data into a convenient format.

    Return (timesteps, samples) where for all i:
    - timesteps[i] is the i-th integer timestep
    - samples[i] is an array of (log_prob, x, y, theta) rows at that timestep
    """
    data = json.load(open(path))
    all_timesteps = []
    all_samples = []
    for query_str, entries in data:
        # query_str is like "state(@123)"
        timestep = int(query_str[7:-1])
        samples = []
        for entry in entries:
            # entry is like (log_prob, "[0.0; 0.0; 0.0]")
            sample = [entry[0]] + map(float, entry[1][1:-1].split('; '))
            samples.append(sample)
        all_timesteps.append(timestep)
        all_samples.append(samples)
    all_timesteps = np.array(all_timesteps)
    all_samples = np.array(all_samples)
    return all_timesteps, all_samples


def traj_from_blog(timestamps, samples, aggregator_func):
    """
    Return computed trajectory as an array of (time, lat, lon, theta) rows.
    """
    assert len(timestamps) == len(samples)
    traj = np.empty((len(timestamps), 4))
    traj[:, 0] = timestamps
    for i in xrange(len(timestamps)):
        x, y, theta = aggregator_func(samples[i])
        traj[i, 1] = y
        traj[i, 2] = x
        traj[i, 3] = theta
    return traj


def map_aggregator(samples):
    """
    "MAP" aggregator: return the particle with the largest weight.
    """
    best_index = np.argmax(samples[:, 0])
    return samples[best_index][1:]


def avg_aggregator(samples):
    """
    "Expected-value" aggregator: return weighted average of particles.
    """
    return np.average(samples[:, 1:], axis=0, weights=np.exp(samples[:, 0]))


if __name__ == "__main__":
    # Parse command-line args.
    parser = argparse.ArgumentParser(
        description='Evaluate trajectory output by BLOG.')
    parser.add_argument('dataset')
    parser.add_argument('json_file')
    parser.add_argument('--plot', action='store_true')
    args = parser.parse_args()

    # Load GPS readings from dataset.
    ground_readings = read_data(path_for_dataset(args.dataset, 'ground'))
    noisy_readings = read_data(path_for_dataset(args.dataset, 'noisy'))
    ground_traj = gps_from_readings(ground_readings)
    noisy_traj = gps_from_readings(noisy_readings)

    # Load trajectory from BLOG JSON output.
    timesteps, samples = read_blog_json_data(args.json_file)
    timestamps = ground_traj[:, 0]
    map_traj = traj_from_blog(timestamps, samples, map_aggregator)
    avg_traj = traj_from_blog(timestamps, samples, avg_aggregator)

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
