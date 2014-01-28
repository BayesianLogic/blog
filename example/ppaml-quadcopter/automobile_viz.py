#!/usr/bin/env python

"""
Visualize laser and intensity data and allow user to scroll backwards and
forwards through time.
"""

from automobile_data import INTENSITY_COLS
from automobile_data import INTENSITY_MAX
from automobile_data import LASER_COLS
from automobile_data import LASER_MAX
from automobile_data import LATITUDE_MIN
from automobile_data import LATITUDE_MAX
from automobile_data import LONGITUDE_MIN
from automobile_data import LONGITUDE_MAX
from automobile_data import path_for_dataset
from automobile_data import read_data
from automobile_data import read_metadata

import numpy as np
import matplotlib.pyplot as plt
import sys


def draw_readings(ax, readings, index, properties, true_obstacles):
    """
    Draw the laser and intensity data at the given index.

    If no data is available for that index, look at previous indices.
    """
    ax.clear()
    ax.set_xlim(LONGITUDE_MIN - 1, LONGITUDE_MAX + 1)
    ax.set_ylim(LATITUDE_MIN - 1, LATITUDE_MAX + 1)
    true_obst_xs = []
    true_obst_ys = []
    if true_obstacles:
        true_obst_xs, true_obst_ys = zip(*true_obstacles)
    ax.scatter(true_obst_xs, true_obst_ys, c='yellow')

    # Find nearest (present or past) indices where data is available:
    assert 0 <= index < len(readings)
    gps_index = index
    while gps_index >= 0 and not readings[gps_index].gps_latitude:
        gps_index -= 1
    laser_index = index
    while laser_index >= 0 and not readings[laser_index].laser:
        laser_index -= 1
    intensity_index = index
    while intensity_index >= 0 and not readings[intensity_index].intensity:
        intensity_index -= 1
    if gps_index == -1 or laser_index == -1 or intensity_index == -1:
        ax.set_title("no data")
        plt.draw()
        return

    # Draw laser and intensity data based on latest GPS location.
    ax.set_title(
        "index {} ({}), gps {} ({}), laser {} ({}), intensity {} ({})"
        .format(
            index, readings[index].time,
            gps_index, readings[gps_index].time,
            laser_index, readings[laser_index].time,
            intensity_index, readings[intensity_index].time))
    agent_x = readings[gps_index].gps_longitude
    agent_y = readings[gps_index].gps_latitude
    agent_phi = readings[gps_index].gps_orientation
    laser_x = (agent_x + properties.a * np.cos(agent_phi) +
        properties.b * np.cos(agent_phi + np.pi / 2))
    laser_y = (agent_y + properties.a * np.sin(agent_phi) +
        properties.b * np.sin(agent_phi + np.pi / 2))
    ax.scatter([agent_x], [agent_y], c='red')
    obst_xs = []
    obst_ys = []
    obst_cs = []
    assert LASER_COLS == INTENSITY_COLS == 361
    for i in xrange(LASER_COLS):
        distance = readings[laser_index].laser[i]
        if np.abs(distance - LASER_MAX) < 1e-6:
            continue  # no obstacles within laser range
        intensity = readings[intensity_index].intensity[i]
        radians = agent_phi + (-0.5 * i + 90) * np.pi / 180
        obst_x = laser_x + distance * np.cos(radians)
        obst_y = laser_y + distance * np.sin(radians)
        obst_c = intensity / INTENSITY_MAX
        obst_xs.append(obst_x)
        obst_ys.append(obst_y)
        obst_cs.append(obst_c)

    ax.scatter(obst_xs, obst_ys, c=obst_cs)
    plt.draw()


class Zipper(object):
    """
    Keeps an index and allows changing it with on_key_press().

    Calls self.callback(index) when the index is changed.
    """

    def __init__(self, min_val, max_val, callback):
        self.min_val = min_val
        self.max_val = max_val
        self.callback = callback
        self.index = min_val

    def on_key_press(self, event):
        prev_index = self.index
        if event.key == 'left':
            self.index -= 1
        elif event.key == 'right':
            self.index += 1
        elif event.key == 'up':
            self.index += 10
        elif event.key == 'down':
            self.index -= 10
        if self.index < self.min_val:
            self.index = self.min_val
        if self.index > self.max_val:
            self.index = self.max_val
        if self.index != prev_index:
            self.callback(self.index)


def demo(dataset_name, dataset_kind):
    """
    Read data and show plot that allows scrolling time using the arrow keys.
    """
    data_dir = path_for_dataset(dataset_name, dataset_kind)
    readings = read_data(data_dir)
    properties, obstacles = read_metadata(data_dir)
    fig = plt.figure()
    ax = fig.add_subplot(111)

    def redraw(index):
        draw_readings(ax, readings, index, properties, obstacles)

    zipper = Zipper(0, len(readings) - 1, redraw)
    fig.canvas.mpl_connect('key_press_event', zipper.on_key_press)
    redraw(0)
    plt.show()
    return zipper


if __name__ == "__main__":
    if len(sys.argv) != 3:
        raise RuntimeError(
            "Usage example: {} 1_straight ground".format(sys.argv[0]))
    # We need to keep a reference to the zipper, because otherwise it gets
    # garbage-collected and won't respond to key_press_event.
    zipper = demo(sys.argv[1], sys.argv[2])
