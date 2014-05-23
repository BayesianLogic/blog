#!/usr/bin/env python

"""
Convert BLOG JSON output to challenge-problem output format.

Example usage:
    python -m convert_output out.json
"""


import argparse
import csv

from ppaml_car.evaluate import avg_aggregator
from ppaml_car.evaluate import read_blog_json_data
from ppaml_car.evaluate import traj_from_blog


def write_traj(traj_path, traj):
    """
    Write trajectory output file.

    `traj` is a list of (time, lat, lon, theta) tuples, sorted by time.
    Passing an iterable that behaves the same way (e.g. np.array) is OK too.
    """
    with open(traj_path, 'w') as csv_file:
        writer = csv.writer(csv_file)
        writer.writerow(['SLAMGPSTime', 'SLAMLat', 'SLAMLon'])
        for time, lat, lon, theta in traj:
            writer.writerow([time, lat, lon])


def write_obstacles(obst_path, obstacles):
    """
    Write obstacles output file.

    `obstacles` is a list of (x, y) obstacles.
    """
    with open(obst_path, 'w') as csv_file:
        writer = csv.writer(csv_file)
        writer.writerow(['SLAMBeaconX', 'SLAMBeaconY'])
        for x, y in obstacles:
            writer.writerow([x, y])


if __name__ == "__main__":
    # Parse command-line args.
    parser = argparse.ArgumentParser(
        description='Convert BLOG JSON output to challenge output format.')
    parser.add_argument('json_file')
    args = parser.parse_args()

    # Load trajectory from BLOG JSON output.
    timesteps, timestamps, samples = read_blog_json_data(args.json_file)
    avg_traj = traj_from_blog(timestamps, samples, avg_aggregator)

    # Output trajectory as CSV.
    write_traj("slam_out_path.csv", avg_traj)

    # Output obstacles as CSV (no obstacles for now).
    write_obstacles("slam_out_landmarks.csv", [])
