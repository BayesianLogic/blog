#!/usr/bin/env python

"""
Read the automobile data.
"""

import csv
import os

LASER_COLS = 361
INTENSITY_COLS = 361


class Reading(object):
    """
    One sensor reading. Values are left None when unobserved.
    """
    def __init__(self):
        self.time = None
        self.gps_latitude = None
        self.gps_longitude = None
        self.gps_orientation = None
        self.velocity = None
        self.steering = None
        self.laser = None
        self.intensity = None


def read_data(data_dir):
    """
    Read data from given path and return a list of Readings sorted by time.
    """
    readings = []

    # GPS data:
    with open(os.path.join(data_dir, 'slam_gps.csv')) as csv_file:
        reader = csv.reader(csv_file)
        header = reader.next()
        assert header[0] == 'TimeGPS'
        for row in reader:
            assert len(row) == 4
            reading = Reading()
            reading.time = float(row[0])
            reading.gps_latitude = float(row[1])
            reading.gps_longitude = float(row[2])
            reading.gps_orientation = float(row[3])
            readings.append(reading)

    # Control data:
    with open(os.path.join(data_dir, 'slam_control.csv')) as csv_file:
        reader = csv.reader(csv_file)
        header = reader.next()
        assert header[0] == 'Time_VS'
        for row in reader:
            assert len(row) == 3
            reading = Reading()
            reading.time = float(row[0])
            reading.velocity = float(row[1])
            reading.steering = float(row[2])
            readings.append(reading)

    # Laser data:
    with open(os.path.join(data_dir, 'slam_laser.csv')) as csv_file:
        reader = csv.reader(csv_file)
        header = reader.next()
        assert header[0] == 'TimeLaser'
        for row in reader:
            assert len(row) == 1 + LASER_COLS + INTENSITY_COLS
            reading = Reading()
            reading.time = float(row[0])
            reading.laser = map(float, row[1:1 + LASER_COLS])
            reading.intensity = map(float, row[1 + LASER_COLS:])
            readings.append(reading)

    readings.sort(key=lambda reading: reading.time)

    # Sanity check against declared sensors:
    declared_sensors = []
    code_to_sensor = {'1': 'gps', '2': 'control', '3': 'laser'}
    with open(os.path.join(data_dir, 'slam_sensor.csv')) as csv_file:
        reader = csv.reader(csv_file)
        header = reader.next()
        assert header[0] == 'Time'
        for row in reader:
            assert len(row) == 2
            declared_sensors.append((float(row[0]), code_to_sensor[row[1]]))
    assert len(declared_sensors) == len(readings)
    for reading, decl in zip(readings, declared_sensors):
        assert abs(reading.time - decl[0]) < 1e-10
        if decl[1] == 'gps':
            assert reading.gps_latitude is not None
        elif decl[1] == 'control':
            assert reading.velocity is not None
        elif decl[1] == 'laser':
            assert reading.laser is not None
        else:
            assert False

    return readings


if __name__ == "__main__":
    data_dir = "./data/automobile/1_straight/data/ground/"
    data = read_data(data_dir)
