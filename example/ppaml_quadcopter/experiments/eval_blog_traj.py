import matplotlib.pyplot as plt
import numpy as np
import sys

from ppaml_car.data import path_for_dataset
from ppaml_car.data import read_data

from compare_ground_noisy import compute_error
from compare_ground_noisy import gps_from_readings
from compare_ground_noisy import plot_traj


if __name__ == "__main__":
    if len(sys.argv) != 3:
        raise RuntimeError(
            "Usage example: {} 1_straight traj.txt".format(sys.argv[0]))

    dataset_name = sys.argv[1]
    ground_readings = read_data(path_for_dataset(dataset_name, 'ground'))
    noisy_readings = read_data(path_for_dataset(dataset_name, 'noisy'))
    ground_traj = gps_from_readings(ground_readings)
    noisy_traj = gps_from_readings(noisy_readings)

    # Load blog traj. Convert from (x, y, theta) to (time, lat, lon, theta).
    blog_traj_xyt = np.loadtxt(sys.argv[2])
    blog_traj = np.zeros_like(ground_traj)
    blog_traj[:, 0] = ground_traj[:, 0]
    blog_traj[:, 1] = blog_traj_xyt[:, 1]
    blog_traj[:, 2] = blog_traj_xyt[:, 0]
    blog_traj[:, 3] = blog_traj_xyt[:, 2]

    # Trajectory plots:
    fig1 = plt.figure()
    ax1 = fig1.add_subplot(111)
    plot_traj(ax1, 'ground', ground_traj)
    plot_traj(ax1, 'noisy', noisy_traj)
    plot_traj(ax1, 'blog', blog_traj)
    ax1.legend()

    print "Noisy error:", compute_error(ground_traj, noisy_traj)
    print "Blog  error:", compute_error(ground_traj, blog_traj)

    plt.show()
