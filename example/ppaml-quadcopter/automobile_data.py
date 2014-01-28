#!/usr/bin/env python

"""
Read the automobile data.
"""

import csv
import os
import sys

LASER_COLS = 361
INTENSITY_COLS = 361
LASER_MIN = 0.0
LASER_MAX = 10.0 + 1e-10
INTENSITY_MIN = 0.0
INTENSITY_MAX = 32768.0 + 1e-10
LATITUDE_MIN = -7
LATITUDE_MAX = 7
LONGITUDE_MIN = -7
LONGITUDE_MAX = 7


class Reading(object):
    """
    One sensor reading. Values are left None when unobserved.

    Note: In the data, GPS latitude and longitude are for the car's rear axle
    center, from which the laser is displaced by (properties.a, properties.b).
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
            assert LATITUDE_MIN <= reading.gps_latitude <= LATITUDE_MAX
            reading.gps_longitude = float(row[2])
            assert LONGITUDE_MIN <= reading.gps_longitude <= LONGITUDE_MAX
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
    shown_out_of_range_laser_warning = False
    shown_out_of_range_intensity_warning = False
    with open(os.path.join(data_dir, 'slam_laser.csv')) as csv_file:
        reader = csv.reader(csv_file)
        header = reader.next()
        assert header[0] == 'TimeLaser'
        for row in reader:
            assert len(row) == 1 + LASER_COLS + INTENSITY_COLS
            reading = Reading()
            reading.time = float(row[0])
            reading.laser = map(float, row[1:1 + LASER_COLS])
            if (not shown_out_of_range_laser_warning and
                    (min(reading.laser) < LASER_MIN or
                     max(reading.laser) > LASER_MAX)):
                print "Warning: found out-of-range laser readings"
                shown_out_of_range_laser_warning = True
            reading.intensity = map(float, row[1 + LASER_COLS:])
            if (not shown_out_of_range_intensity_warning and
                    (min(reading.intensity) < INTENSITY_MIN or
                     max(reading.intensity) > INTENSITY_MAX)):
                print "Warning: found out-of-range intensity readings"
                shown_out_of_range_intensity_warning = True
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


class Properties(object):
    """
    Object holding arbitrary properties as attributes.
    """


def read_metadata(data_dir):
    """
    Read metadata in given dir and return Properties and list of Obstacles.
    """
    # Read properties file.
    properties = Properties()
    with open(os.path.join(data_dir, 'properties.csv')) as csv_file:
        reader = csv.reader(csv_file)
        header = reader.next()
        values = reader.next()
        for name, value in zip(header, values):
            setattr(properties, name, float(value))
        for row in reader:
            raise ValueError("did not expect more rows")

    # Read obstacles file.
    obstacles = []
    with open(os.path.join(data_dir, '..', '..', 'obstacles.csv')) as csv_file:
        reader = csv.reader(csv_file)
        header = reader.next()
        assert header[0] == 'GPSLon'
        for row in reader:
            assert len(row) == 2
            obstacles.append((float(row[0]), float(row[1])))

    return properties, obstacles


def path_for_dataset(name, kind):
    """
    Return path to dataset of given name and kind.

    >>> path_for_dataset('1_straight', 'ground')
    './data/automobile/1_straight/data/ground/'
    """
    return os.path.join('./data/automobile', name, 'data', kind)


def demo(dataset_name, dataset_kind):
    """
    Read data and return readings, properties, obstacles.
    """
    data_dir = path_for_dataset(dataset_name, dataset_kind)
    readings = read_data(data_dir)
    properties, obstacles = read_metadata(data_dir)
    return readings, properties, obstacles


if __name__ == "__main__":
    if len(sys.argv) != 3:
        raise RuntimeError(
            "Usage example: {} 1_straight ground".format(sys.argv[0]))
    readings, properties, obstacles = demo(sys.argv[1], sys.argv[2])
