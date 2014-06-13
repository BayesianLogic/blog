#!/usr/bin/env python

"""
Draw map of laser readings and intensity data, using ground-truth GPS location.
"""

from ppaml_car.data import INTENSITY_COLS
from ppaml_car.data import INTENSITY_MAX
from ppaml_car.data import LASER_COLS
from ppaml_car.data import LASER_MAX
from ppaml_car.data import LATITUDE_MIN
from ppaml_car.data import LATITUDE_MAX
from ppaml_car.data import LONGITUDE_MIN
from ppaml_car.data import LONGITUDE_MAX
from ppaml_car.data import path_for_dataset
from ppaml_car.data import read_data
from ppaml_car.data import read_metadata
from ppaml_car.lasers import car_loc_to_laser_loc

import numpy as np
import matplotlib.pyplot as plt
import sys


def draw_map(ax, readings, properties, true_obstacles):
    """
    Draw map based on laser, intensity, and GPS data.
    """
    assert LASER_COLS == INTENSITY_COLS == 361

    obst_xs = []
    obst_ys = []
    obst_cs = []
    agent_xs = []
    agent_ys = []
    prev_gps_reading = None
    for reading in readings:
        if reading.gps_latitude:
            prev_gps_reading = reading
        if reading.laser and reading.intensity:
            agent_x = prev_gps_reading.gps_longitude
            agent_y = prev_gps_reading.gps_latitude
            agent_phi = prev_gps_reading.gps_orientation
            laser_x, laser_y, _ = car_loc_to_laser_loc(
                agent_x, agent_y, agent_phi, properties.a, properties.b)
            agent_xs.append(agent_x)
            agent_ys.append(agent_y)
            for i in xrange(LASER_COLS):
                distance = reading.laser[i]
                if np.abs(distance - LASER_MAX) < 1e-6:
                    continue  # no obstacles within laser range
                intensity = reading.intensity[i]
                radians = agent_phi + (-90 + 0.5 * i) * np.pi / 180
                obst_x = laser_x + distance * np.cos(radians)
                obst_y = laser_y + distance * np.sin(radians)
                obst_c = intensity / INTENSITY_MAX
                obst_xs.append(obst_x)
                obst_ys.append(obst_y)
                obst_cs.append(obst_c)

    print "Have {} agent locations and {} obstacle locations.".format(
        len(agent_xs), len(obst_xs))
    ax.scatter(agent_xs, agent_ys, c='red', linewidths=0)
    # ax.scatter(obst_xs, obst_ys, c=obst_cs, linewidths=0, alpha=0.01)
    ax.scatter(obst_xs, obst_ys, c='blue', linewidths=0, alpha=0.01)
    true_obst_xs = []
    true_obst_ys = []
    if true_obstacles:
        true_obst_xs, true_obst_ys = zip(*true_obstacles)
    ax.scatter(true_obst_xs, true_obst_ys, c='yellow')
    ax.set_xlim(LONGITUDE_MIN - 1, LONGITUDE_MAX + 1)
    ax.set_ylim(LATITUDE_MIN - 1, LATITUDE_MAX + 1)
    plt.draw()


def demo(dataset_name, dataset_kind):
    """
    Read data and show map of laser data for the entire run.
    """
    data_dir = path_for_dataset(dataset_name, dataset_kind)
    readings = read_data(data_dir)
    properties, obstacles = read_metadata(data_dir)
    fig = plt.figure()
    ax = fig.add_subplot(111)
    draw_map(ax, readings, properties, obstacles)
    plt.show()


if __name__ == "__main__":
    if len(sys.argv) != 3:
        raise RuntimeError(
            "Usage example: {} 1_straight ground".format(sys.argv[0]))
    demo(sys.argv[1], sys.argv[2])
