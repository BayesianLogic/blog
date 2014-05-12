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

    Readings are computed for the angles provided in `laser_angles`.
    """
    readings = np.empty(len(laser_angles))
    for i, angle in enumerate(laser_angles):
        a = 1.0
        b = (2.0 * (laser_x - obstacle_x) * np.cos(laser_theta + angle) +
             2.0 * (laser_y - obstacle_y) * np.sin(laser_theta + angle))
        c = ((laser_x - obstacle_x) ** 2 +
             (laser_y - obstacle_y) ** 2 -
             obstacle_r ** 2)
        k1, k2 = solve_quadratic_equation(a, b, c)
        if k1 is None or k2 < 0 or k1 > laser_max_range:
            # Does not intersect ray, or intersects but too far.
            readings[i] = laser_max_range
        elif k1 < 0:
            readings[i] = k2
        else:
            readings[i] = k1
        assert 0 <= readings[i] <= laser_max_range
    return readings


def readings_for_obstacles(
        laser_x, laser_y, laser_theta,
        laser_angles, laser_max_range,
        obstacles):
    """
    Like readings_for_obstacle, but accepts multiple obstacles.

    `obstacles` is a list of (x, y, r) tuples.
    """
    readings = np.ones_like(laser_angles) * laser_max_range
    for x, y, r in obstacles:
        single_readings = readings_for_obstacle_vectorized(
            laser_x, laser_y, laser_theta,
            laser_angles, laser_max_range, x, y, r)
        readings = np.minimum(readings, single_readings)
    return readings


def readings_for_obstacle_vectorized(
        laser_x, laser_y, laser_theta, laser_angles, laser_max_range,
        obstacle_x, obstacle_y, obstacle_r):
    """
    Like readings_for_obstacle() but using only matrix operations.
    """
    eqn_a = 1.0
    eqn_bs = (2 * (laser_x - obstacle_x) * np.cos(laser_theta + laser_angles) +
              2 * (laser_y - obstacle_y) * np.sin(laser_theta + laser_angles))
    eqn_c = ((laser_x - obstacle_x) ** 2 +
             (laser_y - obstacle_y) ** 2 -
             obstacle_r ** 2)
    eqn_deltas = eqn_bs ** 2 - 4.0 * eqn_a * eqn_c
    no_soln_indicators = (eqn_deltas < 0).astype(np.int)
    eqn_deltas_safe = np.maximum(eqn_deltas, 0)  # to avoid nans
    eqn_k1s = (-eqn_bs - np.sqrt(eqn_deltas_safe)) / (2.0 * eqn_a)
    eqn_k2s = (-eqn_bs + np.sqrt(eqn_deltas_safe)) / (2.0 * eqn_a)
    k2_behind_indicators = (eqn_k2s < 0).astype(np.int)
    k1_behind_indicators = (eqn_k1s < 0).astype(np.int)
    k1_beyond_indicators = (eqn_k1s > laser_max_range).astype(np.int)
    max_range_indicators = (
        (no_soln_indicators + k2_behind_indicators + k1_beyond_indicators)
        > 0).astype(np.int)
    soln_k2_indicators = (
        (-max_range_indicators + k1_behind_indicators) > 0).astype(np.int)
    soln_k1_indicators = ((
        np.ones_like(laser_angles) -
        max_range_indicators -
        soln_k2_indicators) > 0).astype(np.int)
    assert np.sum(
        max_range_indicators +
        soln_k2_indicators +
        soln_k1_indicators) == len(laser_angles)
    solns = (
        laser_max_range * max_range_indicators +
        eqn_k1s * soln_k1_indicators +
        eqn_k2s * soln_k2_indicators)
    # NOTE: I verified that max_range_indicators and soln_k1_indicators does
    # the right thing, but in my example soln_k2_indicators is all zeros.
    assert np.all(0 <= solns) and np.all(solns <= laser_max_range)
    return solns


def plot_lasers(
        laser_x, laser_y, laser_theta,
        laser_angles, laser_max_range,
        obstacles, readings, ax):
    """
    Plot the given lasers scene.

    `obstacles` is a list of (x, y, r) tuples.
    """
    ax.plot([laser_x], [laser_y], 'go')
    for x, y, r in obstacles:
        obst = plt.Circle((x, y), radius=r, color='r')
        ax.add_patch(obst)
    for i, angle in enumerate(laser_angles):
        ax.add_line(plt.Line2D(
            [laser_x,
             laser_x + readings[i] * np.cos(laser_theta + angle)],
            [laser_y,
             laser_y + readings[i] * np.sin(laser_theta + angle)]))
    ax.set_xlim(-10, 20)
    ax.set_ylim(-10, 20)

    # TODO: use transparency s.t. the rays don't cover the obstacles...


def default_laser_angles():
    """
    Return `laser_angles` from -90 deg to +90 deg, in increments of 0.5 deg.
    """
    return np.arange(-90, 90.5, 0.5) * np.pi / 180


def default_laser_max_range():
    return 10.0


def demo(readings_for_obstacle):
    laser_x = 2.0
    laser_y = 3.0
    laser_theta = 0.3
    laser_angles = default_laser_angles()
    laser_max_range = default_laser_max_range()
    obstacle_x = 7.0
    obstacle_y = 9.0
    obstacle_r = 2.0

    readings = readings_for_obstacle(
        laser_x, laser_y, laser_theta, laser_angles, laser_max_range,
        obstacle_x, obstacle_y, obstacle_r)

    fig = plt.figure()
    ax = fig.add_subplot(111)
    plot_lasers(
        laser_x, laser_y, laser_theta,
        laser_angles, laser_max_range,
        [(obstacle_x, obstacle_y, obstacle_r)],
        readings, ax)
    plt.show()


if __name__ == "__main__":
    demo(readings_for_obstacle)
    demo(readings_for_obstacle_vectorized)
