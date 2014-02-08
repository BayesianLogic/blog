#!/usr/bin/env python

import matplotlib.pyplot as plt
import numpy as np


def solve_quadratic_equation(a, b, c):
    """
    Solve A * x^2 + B * x + C = 0 and return (x1, x2) s.t. x1 <= x2.

    If there are no solutions, returns (None, None).
    """
    delta = b ** 2 - 4.0 * a * c
    if delta < 0:
        return None, None
    x1 = (-b - np.sqrt(delta)) / (2.0 * a)
    x2 = (-b + np.sqrt(delta)) / (2.0 * a)
    return x1, x2


def readings_for_obstacle(
        laser_x, laser_y, laser_theta, laser_angles, laser_max_range,
        obstacle_x, obstacle_y, obstacle_r):
    """
    Return ground-truth laser readings for given pose and obstacle.

    Obstacle is a cylinder.

    Readings span from -90 deg to +90 deg, in increments of 0.5 deg.
    """
    readings = np.empty(len(laser_angles))
    for i, angle in enumerate(laser_angles):
        a = 1
        b = (2 * (laser_x - obstacle_x) * np.cos(laser_theta + angle) +
             2 * (laser_y - obstacle_y) * np.sin(laser_theta + angle))
        c = ((laser_x - obstacle_x) ** 2 +
             (laser_y - obstacle_y) ** 2 -
             obstacle_r ** 2)
        k1, k2 = solve_quadratic_equation(a, b, c)
        if k1 is None or k2 < 0:
            # Does not intersect ray.
            readings[i] = laser_max_range
        elif k1 < 0:
            readings[i] = k2
        else:
            readings[i] = k1
    return readings


def demo():
    laser_x = 2.0
    laser_y = 3.0
    laser_theta = 0.3
    laser_angles = np.arange(-90, 90.5, 0.5) * np.pi / 180
    laser_max_range = 10
    obstacle_x = 7.0
    obstacle_y = 9.0
    obstacle_r = 2.0

    readings = readings_for_obstacle(
        laser_x, laser_y, laser_theta, laser_angles, laser_max_range,
        obstacle_x, obstacle_y, obstacle_r)

    fig = plt.figure()
    ax = fig.add_subplot(111)
    ax.plot([laser_x], [laser_y], 'go')
    obst = plt.Circle((obstacle_x, obstacle_y), radius=obstacle_r, color='r')
    ax.add_patch(obst)
    for i, angle in enumerate(laser_angles):
        ax.add_line(plt.Line2D(
            [laser_x,
             laser_x + readings[i] * np.cos(laser_theta + angle)],
            [laser_y,
             laser_y + readings[i] * np.sin(laser_theta + angle)]))
    ax.set_xlim(-10, 20)
    ax.set_ylim(-10, 20)
    plt.show()


if __name__ == "__main__":
    demo()
