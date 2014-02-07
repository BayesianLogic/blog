#!/usr/bin/env python

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


def dynamics(car_params, old_state, encoder_velocity, steering_angle, delta_t):
    """
    Apply dynamics model and return new state.

    The model is taken from guivant_2006, equations 5 and 6.
    The model is non-linear and noise-free.

    state is assumed to be a column vector of [x, y, theta, xdot, ydot,
    thetadot, obst_x, obst_y].

    steering_angle is in radians.
    """
    a, b, h, L = car_params.a, car_params.b, car_params.h, car_params.L
    [x, y, theta, xdot, ydot, thetadot, obst_x, obst_y] = old_state

    # Translate velocity from encoder to center of back axle:
    velocity = encoder_velocity / (1 - np.tan(steering_angle) * h / L)

    # Compute new xdot, ydot, thetadot:
    new_xdot = (
        velocity * np.cos(theta) -
        (velocity / L) *
        (a * np.sin(theta) + b * np.cos(theta)) *
        np.tan(steering_angle))
    new_ydot = (
        velocity * np.sin(theta) +
        (velocity / L) *
        (a * np.cos(theta) - b * np.sin(theta)) *
        np.tan(steering_angle))
    new_thetadot = (velocity / L) * np.tan(steering_angle)

    # Compute new x, y, theta:
    new_x = x + delta_t * xdot
    new_y = y + delta_t * ydot
    new_theta = thetadot  # XXX mistake in their paper??

    # The obstacles stay the same:
    new_obst_x = obst_x
    new_obst_y = obst_y

    return [
        new_x, new_y, new_theta,
        new_xdot, new_ydot, new_thetadot,
        new_obst_x, new_obst_y,
    ]


def plot_traj(ax, label, ts, xs, ys, thetas):
    """
    Plot trajectory.
    """
    ax.plot(xs, ys, label=label)

    # TODO take into account ts and thetas


def demo(dataset_name, dataset_kind):
    """
    Read data and show true trajectory and trajectory given by dynamics model.
    """
    data_dir = path_for_dataset(dataset_name, dataset_kind)
    readings = read_data(data_dir)
    car_params, obstacles = read_metadata(data_dir)
    fig = plt.figure()
    ax = fig.add_subplot(111)

    # Ground-truth trajectory from GPS readings:
    gps_readings = [reading for reading in readings if reading.gps_latitude]
    gps_ts = [reading.time for reading in gps_readings]
    gps_xs = [reading.gps_longitude for reading in gps_readings]
    gps_ys = [reading.gps_latitude for reading in gps_readings]
    gps_thetas = [reading.gps_orientation for reading in gps_readings]
    plot_traj(ax, 'ground', gps_ts, gps_xs, gps_ys, gps_thetas)

    # Trajectory as given by our dynamics model:
    control_readings = [reading for reading in readings if reading.velocity]
    my_ts = [0]
    my_xs = [gps_xs[0]]
    my_ys = [gps_ys[0]]
    my_thetas = [gps_thetas[0]]
    prev_state = [
        gps_xs[0], gps_ys[0], gps_thetas[0],
        0, 0, 0,
        123, 45]
    for reading in control_readings:
        delta_t = reading.time - my_ts[-1]
        new_state = dynamics(
            car_params, prev_state, reading.velocity,
            reading.steering, delta_t)
        my_ts.append(reading.time)
        my_xs.append(new_state[0])
        my_ys.append(new_state[1])
        my_thetas.append(new_state[2])

        # LEFT TODO: debug dynamics function
        print new_state

    plot_traj(ax, 'mine', my_ts, my_xs, my_ys, my_thetas)

    ax.set_xlim(LONGITUDE_MIN - 1, LONGITUDE_MAX + 1)
    ax.set_ylim(LATITUDE_MIN - 1, LATITUDE_MAX + 1)
    plt.legend()
    plt.show()


if __name__ == "__main__":
    if len(sys.argv) != 3:
        raise RuntimeError(
            "Usage example: {} 1_straight ground".format(sys.argv[0]))
    demo(sys.argv[1], sys.argv[2])
