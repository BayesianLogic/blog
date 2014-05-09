from compare_ground_noisy import traj_from_dataset
from compare_ground_noisy import compute_error


if __name__ == "__main__":
    dataset_names = [
        "1_straight",
        "2_bend",
        "3_curvy",
        "4_circle",
        "5_eight",
        "6_loop",
        "7_random",
    ]
    for dataset_name in dataset_names:
        ground_traj = traj_from_dataset(dataset_name, 'ground')
        noisy_traj = traj_from_dataset(dataset_name, 'noisy')
        err = compute_error(ground_traj, noisy_traj)
        print "{}: {}".format(dataset_name, err)
