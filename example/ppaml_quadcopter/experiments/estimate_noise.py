from ppaml_car.data import path_for_dataset
from ppaml_car.data import read_data
from ppaml_car.evaluate import controls_from_readings
from ppaml_car.evaluate import gps_from_readings
from ppaml_car.evaluate import lasers_from_readings

from mpl_toolkits.mplot3d import Axes3D
import matplotlib.pyplot as plt
import numpy as np
import scipy.stats
import sys


def estimate_controls_noise(ground_readings, noisy_readings):
    # Controls are (time, velocity, steering) tuples.
    # Plot the difference in controls:
    ground_controls = controls_from_readings(ground_readings)
    noisy_controls = controls_from_readings(noisy_readings)
    diffs = noisy_controls - ground_controls
    plt.figure()
    plt.scatter(diffs[:, 1], diffs[:, 2], s=10)

    # Estimate noise variance, individually for velocity and steering.
    for dim, name in ((1, 'velocity'), (2, 'steering')):
        data = diffs[:, dim]
        std = np.std(data)
        var = std ** 2
        print "{} var = {}".format(name, var)

        # Plot estimated noise distributions.
        xs = np.arange(-0.1, 0.1, 0.001)
        ys = scipy.stats.norm.pdf(xs, loc=0, scale=std)
        plt.figure()
        plt.plot(xs, ys, color='red')
        plt.hist(data, bins=100, normed=True)


def estimate_gps_noise(ground_readings, noisy_readings):
    # GPS readings are (time, lat, lon, theta) tuples.
    # Plot the difference in GPS readings:
    ground_gps = gps_from_readings(ground_readings)
    noisy_gps = gps_from_readings(noisy_readings)
    diffs = noisy_gps - ground_gps
    fig = plt.figure()
    ax = Axes3D(fig)
    ax.scatter(diffs[:, 1], diffs[:, 2], diffs[:, 3], s=10)

    # Estimate noise variance, individually for each dimension.
    for dim, name in ((1, 'x'), (2, 'y'), (3, 'theta')):
        data = diffs[:, dim]
        std = np.std(data)
        var = std ** 2
        print "{} var = {}".format(name, var)

        # Plot estimated noise distributions.
        xs = np.arange(-0.2, 0.2, 0.001)
        ys = scipy.stats.norm.pdf(xs, loc=0, scale=std)
        plt.figure()
        plt.plot(xs, ys, color='red')
        plt.hist(data, bins=100, normed=True)


def estimate_lasers_noise(ground_readings, noisy_readings):
    # Laser readings are (time, laser0, ..., laser360) tuples.
    ground_lasers = lasers_from_readings(ground_readings)
    noisy_lasers = lasers_from_readings(noisy_readings)
    diffs = noisy_lasers[:, 1:] - ground_lasers[:, 1:]

    # Estimate noise variance, flattened across all angles.
    diffs = diffs.flatten()
    std = np.std(diffs)
    var = std ** 2
    print "lasers var = {}".format(var)

    # Plot estimated noise distributions.
    xs = np.arange(-1, 1, 0.001)
    ys = scipy.stats.norm.pdf(xs, loc=0, scale=std)
    plt.figure()
    plt.plot(xs, ys, color='red')
    plt.hist(diffs, bins=100, normed=True)


if __name__ == "__main__":
    if len(sys.argv) != 2:
        raise RuntimeError("Usage example: {} 1_straight".format(sys.argv[0]))

    dataset_name = sys.argv[1]
    ground_readings = read_data(path_for_dataset(dataset_name, 'ground'))
    noisy_readings = read_data(path_for_dataset(dataset_name, 'noisy'))

    # estimate_controls_noise(ground_readings, noisy_readings)
    # estimate_gps_noise(ground_readings, noisy_readings)
    estimate_lasers_noise(ground_readings, noisy_readings)

    plt.show()
