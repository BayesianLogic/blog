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
    # print "old_state:", old_state
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
    new_theta = theta + delta_t * thetadot

    # The obstacles stay the same:
    new_obst_x = obst_x
    new_obst_y = obst_y

    new_state = [
        new_x, new_y, new_theta,
        new_xdot, new_ydot, new_thetadot,
        new_obst_x, new_obst_y,
    ]
    # print "new_state:", new_state
    # raw_input()
    return new_state


def plot_traj(ax, label, ts, xs, ys, thetas):
    """
    Plot trajectory.
    """
    ax.plot(xs, ys, label=label)

    # TODO take into account ts and thetas


def plot_components(fig, label, gps_poses, my_poses, controls):
    """
    Plot x, y, theta, velocity, steering individually.
    """
    gps_ts, gps_xs, gps_ys, gps_thetas = gps_poses
    my_ts, my_xs, my_ys, my_thetas = my_poses
    control_ts, velocities, steerings = controls

    x_ax = fig.add_subplot(511)
    x_ax.plot(gps_ts, gps_xs, label='ground')
    x_ax.plot(my_ts, my_xs, label='mine')
    x_ax.set_ylabel('x')
    x_ax.set_ylim(LONGITUDE_MIN - 1, LONGITUDE_MAX + 1)
    x_ax.legend()

    y_ax = fig.add_subplot(512)
    y_ax.plot(gps_ts, gps_ys, label='ground')
    y_ax.plot(my_ts, my_ys, label='mine')
    y_ax.set_ylabel('y')
    y_ax.set_ylim(LONGITUDE_MIN - 1, LONGITUDE_MAX + 1)
    y_ax.legend()

    theta_ax = fig.add_subplot(513)
    theta_ax.plot(gps_ts, gps_thetas, label='ground')
    theta_ax.plot(my_ts, my_thetas, label='mine')
    theta_ax.set_ylabel('theta')
    theta_ax.set_ylim(-np.pi, np.pi)
    theta_ax.legend()

    velocity_ax = fig.add_subplot(514)
    velocity_ax.plot(control_ts, velocities)
    velocity_ax.set_ylabel('velocity')

    steering_ax = fig.add_subplot(515)
    steering_ax.plot(control_ts, steerings)
    steering_ax.set_ylabel('steering')


def get_controls(readings):
    """
    Return control_ts, velocities, steerings from control readings.
    """
    control_readings = [reading for reading in readings if reading.velocity]
    ts = [reading.time for reading in control_readings]
    velocities = [reading.velocity for reading in control_readings]
    steerings = [reading.steering for reading in control_readings]
    return ts, velocities, steerings


def get_ground_truth_poses(readings):
    """
    Return ts, xs, ys, thetas from GPS readings.
    """
    gps_readings = [reading for reading in readings if reading.gps_latitude]
    gps_ts = [reading.time for reading in gps_readings]
    gps_xs = [reading.gps_longitude for reading in gps_readings]
    gps_ys = [reading.gps_latitude for reading in gps_readings]
    gps_thetas = [reading.gps_orientation for reading in gps_readings]
    return gps_ts, gps_xs, gps_ys, gps_thetas


def get_my_poses(readings, car_params, dynamics):
    """
    Return ts, xs, ys, thetas given by dynamics model.
    """
    initial_t, initial_x, initial_y, initial_theta = None, None, None, None
    first_gps_reading = next(rdng for rdng in readings if rdng.gps_latitude)
    initial_t = first_gps_reading.time
    initial_x = first_gps_reading.gps_longitude
    initial_y = first_gps_reading.gps_latitude
    initial_theta = first_gps_reading.gps_orientation
    control_readings = [reading for reading in readings if reading.velocity]
    assert control_readings[0].time > initial_t
    my_ts = [initial_t]
    my_xs = [initial_x]
    my_ys = [initial_y]
    my_thetas = [initial_theta]
    prev_state = [
        initial_x, initial_y, initial_theta,
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
        prev_state = new_state
    return my_ts, my_xs, my_ys, my_thetas


def demo(dataset_name, dataset_kind):
    """
    Read data and show true trajectory and trajectory given by dynamics model.
    """
    data_dir = path_for_dataset(dataset_name, dataset_kind)
    readings = read_data(data_dir)
    car_params, obstacles = read_metadata(data_dir)

    # Ground-truth trajectory vs trajectory from dynamics model:
    fig1 = plt.figure()
    ax1 = fig1.add_subplot(111)
    gps_poses = get_ground_truth_poses(readings)
    plot_traj(ax1, 'ground', *gps_poses)
    my_poses = get_my_poses(readings, car_params, dynamics)
    plot_traj(ax1, 'mine', *my_poses)
    ax1.set_xlim(LONGITUDE_MIN - 1, LONGITUDE_MAX + 1)
    ax1.set_ylim(LATITUDE_MIN - 1, LATITUDE_MAX + 1)
    ax1.legend()

    # Components of ground-truth trajectory vs my trajectory:
    controls = get_controls(readings)
    fig2 = plt.figure()
    plot_components(fig2, 'ground', gps_poses, my_poses, controls)

    plt.show()

    return gps_poses, my_poses


if __name__ == "__main__":
    if len(sys.argv) != 3:
        raise RuntimeError(
            "Usage example: {} 1_straight ground".format(sys.argv[0]))
    gps_poses, my_poses = demo(sys.argv[1], sys.argv[2])
    gps_ts, gps_xs, gps_ys, gps_thetas = gps_poses
    my_ts, my_xs, my_ys, my_thetas = my_poses
