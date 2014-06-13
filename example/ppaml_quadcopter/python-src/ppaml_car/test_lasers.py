import nose
import numpy as np

from ppaml_car import lasers


def test_readings_for_obstacle_vectorized():
    lx, ly, ltheta = -7.0, -7.0, 1.7
    laser_angles = lasers.default_laser_angles()
    laser_max_range = lasers.default_laser_max_range()
    ox, oy, orad = 7.0, 9.0, 1.0
    readings_1 = lasers.readings_for_obstacle(
        lx, ly, ltheta, laser_angles, laser_max_range, ox, oy, orad)
    readings_2 = lasers.readings_for_obstacle_vectorized(
        lx, ly, ltheta, laser_angles, laser_max_range, ox, oy, orad)
    nose.tools.assert_true(np.sum(np.abs(readings_1 - readings_2)) < 1e-6)
