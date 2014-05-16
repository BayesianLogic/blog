import matplotlib.pyplot as plt
import sys

from ppaml_car.data import path_for_dataset
from ppaml_car.data import read_data
from ppaml_car.evaluate import gps_from_readings
from ppaml_car.evaluate import plot_traj
from ppaml_car.evaluate import compute_error


def plot_components(fig, label, traj):
    """
    Plot lat, lon individually w.r.t. time.
    """
    # Traj has rows (time, lat, lon). Note x=lon, y=lat.

    x_ax = fig.add_subplot(211)
    x_ax.plot(traj[:, 0], traj[:, 2], label=label)
    x_ax.set_ylabel('x = longitude')
    x_ax.set_ylim(-8, 8)
    x_ax.legend()

    y_ax = fig.add_subplot(212)
    y_ax.plot(traj[:, 0], traj[:, 1], label=label)
    y_ax.set_ylabel('y = latitude')
    y_ax.set_ylim(-8, 8)
    y_ax.legend()


if __name__ == "__main__":
    if len(sys.argv) != 2:
        raise RuntimeError("Usage example: {} 1_straight".format(sys.argv[0]))

    dataset_name = sys.argv[1]
    ground_readings = read_data(path_for_dataset(dataset_name, 'ground'))
    noisy_readings = read_data(path_for_dataset(dataset_name, 'noisy'))
    ground_traj = gps_from_readings(ground_readings)
    noisy_traj = gps_from_readings(noisy_readings)

    # Trajectory plots:
    fig1 = plt.figure()
    ax1 = fig1.add_subplot(111)
    plot_traj(ax1, 'ground', ground_traj)
    plot_traj(ax1, 'noisy', noisy_traj)
    ax1.legend()

    # Plots of the components of the trajectory:
    fig2 = plt.figure()
    plot_components(fig2, 'ground', ground_traj)
    plot_components(fig2, 'noisy', noisy_traj)

    err = compute_error(ground_traj, noisy_traj)
    print "Error between trajectories:", err

    plt.show()
