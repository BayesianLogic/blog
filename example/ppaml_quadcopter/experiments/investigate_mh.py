import matplotlib.pyplot as plt
import numpy as np

from ppaml_car import data
from ppaml_car import lasers
from numutil import norm_log_pdf_fixed_sigma


# Defaults as globals for now:
laser_angles = lasers.default_laser_angles()
laser_max_range = lasers.default_laser_max_range()
obstacles = np.array([
    [-4.475, 1.45, 0.4],
    [-1.3, 1.025, 0.4],
    [-3.0, -1.55, 0.4],
    [0.65, -1.95, 0.4],
    [-1.95, -3.8, 0.4],
    [0.15, -5.625, 0.4]
])
readings = np.array([
    9.99112, 10.1518, 9.96472, 10.0942, 10.0306, 10.0467, 10.0576, 10.0147,
    10.0248, 10.1041, 9.92387, 9.9082, 9.89024, 10.0892, 9.96818, 10.0904,
    10.0177, 9.9872, 10.0542, 10.0842, 10.0892, 10.2483, 10.0916, 10.12,
    10.0609, 10.0439, 10.1086, 9.96434, 10.0278, 10.0384, 10.2351, 9.9389,
    10.0557, 9.8775, 9.87645, 10.089, 9.91507, 10.0773, 9.92106, 10.1403,
    9.76055, 10.0453, 10.0433, 10.0836, 10.1515, 10.0739, 10.0093, 10.0195,
    10.0079, 10.0704, 10.0284, 9.99141, 10.0342, 10.0513, 10.0596, 9.99998,
    9.97684, 10.0724, 10.1458, 10.1059, 9.93182, 9.95667, 9.91674, 9.87357,
    10.0217, 10.0352, 9.9022, 9.8477, 9.79345, 9.86417, 10.115, 10.0853,
    10.1079, 9.91121, 9.9993, 10.0671, 10.067, 10.0597, 10.0264, 9.98759,
    10.0048, 10.0515, 5.22403, 5.24101, 5.12683, 5.08631, 5.15997, 5.0158,
    5.12577, 5.07404, 5.06636, 5.01376, 5.02826, 5.18655, 4.93637, 5.10793,
    5.25499, 5.37785, 9.90489, 9.8883, 10.0262, 9.99327, 9.9627, 10.0871,
    10.0318, 10.0479, 10.0319, 9.99507, 3.16966, 3.37584, 2.9762, 3.00049,
    3.03738, 3.091, 2.76963, 2.92976, 2.79277, 2.7931, 2.69354, 3.09668,
    2.86839, 2.78102, 2.66023, 2.66168, 2.85464, 2.85977, 3.03943, 2.86236,
    2.85527, 2.79503, 3.14345, 2.99083, 2.92165, 3.08306, 3.22666, 9.87075,
    10.0483, 10.0792, 10.0711, 9.89687, 6.59919, 6.58177, 6.53691, 6.5357,
    6.56482, 6.57846, 6.34427, 6.18941, 6.56314, 6.26824, 6.45965, 6.57841,
    6.98979, 10.0567, 9.8893, 9.9502, 9.74533, 10.1335, 10.1655, 9.98709,
    9.94385, 10.0421, 10.0377, 10.0875, 9.9222, 9.89828, 10.0212, 9.87701,
    9.8076, 10.1046, 10.0156, 10.0103, 9.97956, 10.0663, 10.0862, 9.81722,
    10.1458, 10.0705, 9.9629, 9.93532, 9.94897, 9.91343, 10.1665, 9.92292,
    10.1453, 10.0974, 9.90657, 9.91919, 10.1305, 9.85292, 9.8227, 10.004,
    10.0079, 10.0492, 10.0654, 9.94849, 10.0473, 4.40004, 4.53018, 4.25113,
    4.28116, 4.37335, 4.22752, 4.20528, 4.32295, 4.41131, 4.33851, 4.31093,
    4.26113, 4.36962, 4.10706, 4.119, 4.24227, 4.29144, 4.42043, 4.58056,
    10.1121, 10.126, 10.115, 10.1073, 10.0027, 10.0266, 10.0124, 10.063,
    9.98801, 10.0029, 9.97396, 10.1809, 9.95998, 9.97003, 9.93945, 9.92735,
    10.058, 9.93308, 10.0525, 10.0501, 9.98912, 10.0982, 10.0766, 9.98516,
    9.98273, 10.0959, 10.0152, 9.96086, 10.0043, 10.0369, 9.98806, 10.0551,
    10.1127, 9.99077, 9.99855, 10.1169, 10.0209, 1.8489, 1.77903, 1.85174,
    1.57384, 1.88631, 1.79471, 1.52312, 1.66396, 1.61406, 1.54994, 1.74775,
    1.64259, 1.64876, 1.59086, 1.50159, 1.82249, 1.6199, 1.66409, 1.52228,
    1.74092, 1.79667, 1.53008, 1.50714, 1.52771, 1.64773, 1.623, 1.63559,
    1.51077, 1.6481, 1.7745, 1.71953, 1.40376, 1.64063, 1.49152, 1.62863,
    1.75321, 1.75372, 1.61545, 1.69677, 1.60412, 1.76384, 1.71561, 1.98379,
    1.80654, 1.98831, 9.98531, 10.2139, 9.97562, 9.83308, 9.88611, 9.95925,
    9.86667, 10.1035, 9.96003, 10.112, 10.1505, 10.051, 10.1987, 9.85437,
    10.0424, 9.9402, 9.95207, 10.0513, 9.97947, 9.82434, 10.0792, 9.92583,
    9.68906, 9.92674, 10.1022, 9.81909, 9.9495, 10.0785, 10.0497, 10.0447,
    10.0375, 10.1026, 9.96022, 9.89837, 9.92919, 10.0884, 10.0211, 9.96716,
    9.95919, 9.9447, 10.0652, 9.89257, 10.0347, 10.0075, 10.2174, 10.1708,
    9.97744, 9.9607, 10.016, 10.1277, 9.96755, 9.95952, 9.94743, 9.94982,
    10.0254, 9.98185, 10.0474, 9.95918, 9.76251, 9.93913, 10.0674, 9.95075,
    10.2174
])


_pdf_for_noise_const = {}


def get_pdf_for_noise_const(noise_const):
    """
    Caches result of norm_log_pdf_fixed_sigma.
    """
    if noise_const not in _pdf_for_noise_const:
        sigma = noise_const * np.eye(len(readings))
        _pdf_for_noise_const[noise_const] = norm_log_pdf_fixed_sigma(sigma)
    return _pdf_for_noise_const[noise_const]


def log_likelihood(readings, noise_const, x, y, theta):
    """
    Return log likelihood of seeing given readings from given location.
    """
    mu = lasers.readings_for_obstacles(
        x, y, theta, laser_angles, laser_max_range, obstacles)
    pdf = get_pdf_for_noise_const(noise_const)
    return pdf(readings, mu)


def plot_liks(fixed_x, fixed_y, fixed_theta, true_x, true_y, true_theta):
    true_kwargs = {
        's': 200,
        'color': 'green',
        'marker': 'o',
        'label': 'true pose',
    }
    fixed_kwargs = {
        's': 200,
        'color': 'red',
        'marker': 'x',
        'label': 'MH pose',
    }

    # Plot likelihood for all x, given fixed y and theta:
    xs = np.arange(min_x, max_x + 0.01, 0.1)
    func = lambda x: log_likelihood(readings, 0.1, x, fixed_y, fixed_theta)
    func = np.vectorize(func)
    zs = func(xs)
    thresh = -50000
    zs[zs < thresh] = thresh
    plt.figure()
    plt.plot(xs, zs)
    plt.scatter([true_x], [func(true_x)], **true_kwargs)
    plt.scatter([fixed_x], [func(fixed_x)], **fixed_kwargs)
    plt.xlabel('x')
    plt.ylabel('log_lik')
    plt.legend(loc='upper left', scatterpoints=1)

    # Plot likelihood for all y, given fixed x and theta:
    ys = np.arange(min_y, max_y + 0.01, 0.1)
    func = lambda y: log_likelihood(readings, 0.1, fixed_x, y, fixed_theta)
    func = np.vectorize(func)
    zs = func(ys)
    thresh = -50000
    zs[zs < thresh] = thresh
    plt.figure()
    plt.plot(ys, zs)
    plt.scatter([true_y], [func(true_y)], **true_kwargs)
    plt.scatter([fixed_y], [func(fixed_y)], **fixed_kwargs)
    plt.xlabel('y')
    plt.ylabel('log_lik')
    plt.legend(loc='upper left', scatterpoints=1)

    # Plot likelihood for all theta, given fixed x and y:
    thetas = np.arange(min_theta, max_theta + 0.01, 0.02)
    func = lambda theta: log_likelihood(readings, 0.1, fixed_x, fixed_y, theta)
    func = np.vectorize(func)
    zs = func(thetas)
    thresh = -50000
    zs[zs < thresh] = thresh
    plt.figure()
    plt.plot(thetas, zs)
    plt.scatter([true_theta], [func(true_theta)], **true_kwargs)
    plt.scatter([fixed_theta], [func(fixed_theta)], **fixed_kwargs)
    plt.xlabel('theta')
    plt.ylabel('log_lik')
    plt.legend(loc='upper left', scatterpoints=1)


if __name__ == "__main__":
    min_x = data.LONGITUDE_MIN
    max_x = data.LONGITUDE_MAX
    min_y = data.LATITUDE_MIN
    max_y = data.LATITUDE_MAX
    min_theta = -np.pi
    max_theta = np.pi
    a = 0.299541
    b = 0.0500507

    # Location from ground GPS:
    true_x = -6.1039
    true_y = -0.0499926
    true_theta = -0.000112593
    true_x, true_y, true_theta = lasers.car_loc_to_laser_loc(
        true_x, true_y, true_theta, a, b)

    # Location from noisy GPS:
    fixed_x = -6.09833
    fixed_y = -0.0897621
    fixed_theta = -0.000112593
    fixed_x, fixed_y, fixed_theta = lasers.car_loc_to_laser_loc(
        fixed_x, fixed_y, fixed_theta, a, b)

    # Location from LW:
    fixed_x = -6.111071374818211
    fixed_y = -0.10284180236291353
    fixed_theta = 0.013397688774164038
    fixed_x, fixed_y, fixed_theta = lasers.car_loc_to_laser_loc(
        fixed_x, fixed_y, fixed_theta, a, b)

    # Noise constant for the lasers.
    noise_const = 0.01

    print "true  has lik {}".format(log_likelihood(
        readings, noise_const, true_x, true_y, true_theta))
    print "fixed has lik {}".format(log_likelihood(
        readings, noise_const, fixed_x, fixed_y, fixed_theta))
    # plot_liks(fixed_x, fixed_y, fixed_theta, true_x, true_y, true_theta)
    # plt.show()

    # 3D data across a grid for x, y, and theta:

    # # All x and y; fixed theta:
    # xs, ys, thetas = np.mgrid[
    #     min_x : max_x : 0.1,
    #     min_y : max_y : 0.1,
    #     true_theta : true_theta + 1 : 1]

    # All x, y, theta in neighborhood of true values:
    xs, ys, thetas = np.mgrid[
        true_x - 0.2 : true_x + 0.2 : 0.01,
        true_y - 0.2 : true_y + 0.2 : 0.01,
        true_theta - 0.05 : true_theta + 0.051 : 0.005]

    func = lambda x, y, theta: log_likelihood(
        readings, noise_const, x, y, theta)
    func = np.vectorize(func)
    vals = func(xs, ys, thetas)
    thresh = -50000
    vals[vals < thresh] = thresh

    # # Mayavi 3D plot:
    # from mayavi import mlab
    # surf = mlab.contour3d(xs, ys, thetas, vals, opacity=0.3, contours=10)
    # mlab.axes()
    # mlab.colorbar()
    # mlab.gcf().scene.background = (0.5, 0.5, 0.5)
    # mlab.show()

    # plot likelihood for all x, y, given best theta for that x, y.
    xs = xs[:, :, 0]
    ys = ys[:, :, 0]
    zs = np.max(vals, axis=2)
    plt.pcolormesh(xs, ys, zs)
    plt.colorbar()
    plt.scatter(
        [true_x], [true_y], s=200, marker='x', color='green', label='ground')
    plt.scatter(
        [fixed_x], [fixed_y], s=200, marker='x', color='black', label='fixed')
    plt.xlim(-7, 7)
    plt.ylim(-7, 7)
    plt.xlabel('x')
    plt.ylabel('y')
    plt.legend()
    plt.show()
