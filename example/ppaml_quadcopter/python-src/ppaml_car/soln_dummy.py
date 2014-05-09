#!/usr/bin/env python

"""
Dummy solution that just prints out the GPS data provided as input.
"""


import csv
import os
import sys

from ppaml_car.data import read_data


def write_traj(traj_path, traj):
    """
    Write trajectory output file.

    `traj` is a list of (time, lat, lon) tuples, sorted by time.
    Passing an iterable that behaves the same way (e.g. np.array) is OK too.
    """
    with open(traj_path, 'w') as csv_file:
        writer = csv.writer(csv_file)
        writer.writerow(['SLAMGPSTime', 'SLAMLat', 'SLAMLon'])
        for time, lat, lon in traj:
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
    if len(sys.argv) != 3:
        print >>sys.stderr, "Usage: {} input_dir output_dir".format(
            sys.argv[0])
        sys.exit(1)
    in_dir, out_dir = sys.argv[1:]

    # Read input:
    readings = read_data(in_dir)

    # The trajectory is just the GPS coords from input:
    traj = []
    for reading in readings:
        if reading.gps_latitude:
            traj.append(
                (reading.time, reading.gps_latitude, reading.gps_longitude))

    # Write output:
    traj_path = os.path.join(out_dir, "slam_out_path.csv")
    write_traj(traj_path, traj)
    obstacles = []
    obst_path = os.path.join(out_dir, "slam_out_landmarks.csv")
    write_obstacles(obst_path, obstacles)
