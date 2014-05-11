from ppaml_car.data import ALL_DATASETS
from compare_ground_noisy import traj_from_dataset
from compare_ground_noisy import compute_error


if __name__ == "__main__":
    for dataset_name in ALL_DATASETS:
        ground_traj = traj_from_dataset(dataset_name, 'ground')
        noisy_traj = traj_from_dataset(dataset_name, 'noisy')
        err = compute_error(ground_traj, noisy_traj)
        print "{}: {}".format(dataset_name, err)
