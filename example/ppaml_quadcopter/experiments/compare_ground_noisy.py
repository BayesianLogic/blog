import matplotlib.pyplot as plt
import numpy as np
import sys

from ppaml_car.data import LATITUDE_MIN
from ppaml_car.data import LATITUDE_MAX
from ppaml_car.data import LONGITUDE_MIN
from ppaml_car.data import LONGITUDE_MAX
from ppaml_car.data import path_for_dataset
from ppaml_car.data import read_data


def traj_from_dataset(dataset_name, dataset_kind):
    """
    Return traj as an array with (time, lat, lon) rows.
    """
    data_dir = path_for_dataset(dataset_name, dataset_kind)
    readings = read_data(data_dir)
    traj = []
    for reading in readings:
        if reading.gps_latitude:
            traj.append(
                (reading.time, reading.gps_latitude, reading.gps_longitude))
    return np.array(traj)


def plot_traj(ax, label, traj):
    """
    Plot trajectory as a line in 2D.
    """
    # Traj has rows (time, lat, lon). Note x=lon, y=lat.
    ax.plot(traj[:, 2], traj[:, 1], label=label)


def plot_components(fig, label, traj):
    """
    Plot lat, lon individually w.r.t. time.
    """
    # Traj has rows (time, lat, lon). Note x=lon, y=lat.

    x_ax = fig.add_subplot(211)
    x_ax.plot(traj[:, 0], traj[:, 2], label=label)
    x_ax.set_ylabel('x = longitude')
    x_ax.set_ylim(LONGITUDE_MIN - 1, LONGITUDE_MAX + 1)
    x_ax.legend()

    y_ax = fig.add_subplot(212)
    y_ax.plot(traj[:, 0], traj[:, 1], label=label)
    y_ax.set_ylabel('y = latitude')
    y_ax.set_ylim(LONGITUDE_MIN - 1, LONGITUDE_MAX + 1)
    y_ax.legend()


def compute_error(ground_traj, my_traj):
    """
    Compute error between trajectories, in the same way as the evaluator does.
    """
    # Times should match.
    if ground_traj.shape != my_traj.shape:
        raise ValueError("ground_traj and my_traj must have the same shape")
    if np.sum(np.abs(ground_traj[:, 0] - my_traj[:, 0])) > 1e-10:
        raise ValueError("ground_traj and my_traj must have the same times")

    d = ground_traj[:, 1:] - my_traj[:, 1:]
    norm2 = np.sqrt(np.sum(d * d, axis=1))
    return np.sum(norm2)


if __name__ == "__main__":
    if len(sys.argv) != 2:
        raise RuntimeError("Usage example: {} 1_straight".format(sys.argv[0]))

    dataset_name = sys.argv[1]
    ground_traj = traj_from_dataset(dataset_name, 'ground')
    noisy_traj = traj_from_dataset(dataset_name, 'noisy')

    # Trajectory plots:
    fig1 = plt.figure()
    ax1 = fig1.add_subplot(111)
    plot_traj(ax1, 'ground', ground_traj)
    plot_traj(ax1, 'noisy', noisy_traj)
    ax1.set_xlim(LONGITUDE_MIN - 1, LONGITUDE_MAX + 1)
    ax1.set_ylim(LATITUDE_MIN - 1, LATITUDE_MAX + 1)
    ax1.legend()

    # Plots of the components of the trajectory:
    fig2 = plt.figure()
    plot_components(fig2, 'ground', ground_traj)
    plot_components(fig2, 'noisy', noisy_traj)

    err = compute_error(ground_traj, noisy_traj)
    print "Error between trajectories:", err

    plt.show()
