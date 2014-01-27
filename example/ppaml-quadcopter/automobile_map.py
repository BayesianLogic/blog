#!/usr/bin/env python

"""
Draw map of laser readings and intensity data, using ground-truth GPS location.
"""

from automobile_data import INTENSITY_COLS
from automobile_data import INTENSITY_MAX
from automobile_data import LASER_COLS
from automobile_data import LASER_MAX
from automobile_data import LATITUDE_MIN
from automobile_data import LATITUDE_MAX
from automobile_data import LONGITUDE_MIN
from automobile_data import LONGITUDE_MAX
from automobile_data import read_data
from automobile_data import read_metadata

import numpy as np
import matplotlib.pyplot as plt


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
            agent_xs.append(agent_x)
            agent_ys.append(agent_y)
            for i in xrange(LASER_COLS):
                distance = reading.laser[i]
                if np.abs(distance - LASER_MAX) < 1e-6:
                    continue  # no obstacles within laser range
                intensity = reading.intensity[i]
                radians = (-0.5 * i + 90) * np.pi / 180
                obst_x = agent_x + distance * np.cos(radians)
                obst_y = agent_y + distance * np.sin(radians)
                obst_c = intensity / INTENSITY_MAX
                obst_xs.append(obst_x)
                obst_ys.append(obst_y)
                obst_cs.append(obst_c)

    print "Have {} agent locations and {} obstacle locations.".format(
        len(agent_xs), len(obst_xs))
    ax.scatter(agent_xs, agent_ys, c='red', linewidths=0)
    ax.scatter(obst_xs, obst_ys, c=obst_cs, linewidths=0)
    true_obst_xs, true_obst_ys = zip(*true_obstacles)
    ax.scatter(true_obst_xs, true_obst_ys, c='yellow')
    ax.set_xlim(LONGITUDE_MIN - 1, LONGITUDE_MAX + 1)
    ax.set_ylim(LATITUDE_MIN - 1, LATITUDE_MAX + 1)
    plt.draw()


if __name__ == "__main__":
    data_dir = "./data/automobile/1_straight/data/ground/"
    readings = read_data(data_dir)
    properties, obstacles = read_metadata(data_dir)
    fig = plt.figure()
    ax = fig.add_subplot(111)
    draw_map(ax, readings, properties, obstacles)
    plt.show()
