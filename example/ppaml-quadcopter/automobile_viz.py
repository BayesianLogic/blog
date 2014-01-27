#!/usr/bin/env python

"""
Visualize laser and intensity data and allow user to scroll backwards and
forwards through time.
"""

from automobile_data import INTENSITY_COLS
from automobile_data import INTENSITY_MAX
from automobile_data import LASER_COLS
from automobile_data import LASER_MAX
from automobile_data import read_data

import numpy as np
import matplotlib.pyplot as plt


def draw_readings(ax, readings, index):
    """
    Draw the laser and intensity data at the given index.

    If no data is available for that index, look at previous indices.
    """
    ax.clear()

    # Find nearest (present or past) indices where data is available:
    assert 0 <= index < len(readings)
    laser_index = index
    while laser_index >= 0 and not readings[laser_index].laser:
        laser_index -= 1
    intensity_index = index
    while intensity_index >= 0 and not readings[intensity_index].intensity:
        intensity_index -= 1
    if laser_index == -1 or intensity_index == -1:
        ax.set_title("no data")
        plt.draw()
        return

    # Draw laser and intensity data.
    ax.set_title("index {} ({}), laser {} ({}), intensity {} ({})".format(
        index, readings[index].time,
        laser_index, readings[laser_index].time,
        intensity_index, readings[intensity_index].time))
    xs = []
    ys = []
    cs = []
    assert LASER_COLS == INTENSITY_COLS == 361
    for i in xrange(LASER_COLS):
        distance = readings[laser_index].laser[i]
        if np.abs(distance - LASER_MAX) < 1e-6:
            continue  # no obstacles within laser range
        intensity = readings[intensity_index].intensity[i]
        radians = 0.5 * i * np.pi / 180
        xs.append(distance * np.cos(radians))
        ys.append(distance * np.sin(radians))
        cs.append(intensity / INTENSITY_MAX)

    ax.scatter(xs, ys, c=cs)
    ax.set_xlim(-5, 5)
    ax.set_ylim(-1, 10)
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


if __name__ == "__main__":
    data_dir = "./data/automobile/1_straight/data/ground/"
    readings = read_data(data_dir)
    fig = plt.figure()
    ax = fig.add_subplot(111)

    def redraw(index):
        draw_readings(ax, readings, index)

    zipper = Zipper(0, len(readings) - 1, redraw)
    fig.canvas.mpl_connect('key_press_event', zipper.on_key_press)
    redraw(0)
    plt.show()
