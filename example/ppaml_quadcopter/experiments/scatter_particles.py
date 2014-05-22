import argparse
import matplotlib.pyplot as plt
import numpy as np

from ppaml_car.data import path_for_dataset
from ppaml_car.data import read_data
from ppaml_car.evaluate import gps_from_readings
from ppaml_car.evaluate import read_blog_json_data
from ppaml_car.evaluate import plot_traj


if __name__ == "__main__":
    # Parse command-line args.
    parser = argparse.ArgumentParser()
    parser.add_argument('dataset')
    parser.add_argument('json_file')
    args = parser.parse_args()

    # Load GPS readings from dataset.
    ground_readings = read_data(path_for_dataset(args.dataset, 'ground'))
    noisy_readings = read_data(path_for_dataset(args.dataset, 'noisy'))
    ground_traj = gps_from_readings(ground_readings)
    noisy_traj = gps_from_readings(noisy_readings)

    # Scatter all points visited.
    timesteps, samples = read_blog_json_data(args.json_file)
    timestamps = ground_traj[:, 0]
    all_particles = []
    for timestep in xrange(len(samples)):
        for log_prob, x, y, theta in samples[timestep]:
            all_particles.append((x, y))
    all_particles = np.array(all_particles)

    plt.figure(figsize=(8, 8))
    for traj, name in (
            (ground_traj, 'ground'),
            (noisy_traj, 'noisy')):
        plot_traj(plt.gca(), name, traj)
    plt.plot([-7, -7, 7, 7, -7], [-7, 7, 7, -7, -7], 'k')
    plt.scatter(
        all_particles[:, 0],
        all_particles[:, 1],
        s=1,
        label='all particles')
    plt.xlim(-8, 8)
    plt.ylim(-8, 8)
    plt.legend()
    plt.show()
