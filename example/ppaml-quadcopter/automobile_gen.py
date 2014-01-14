#!/usr/bin/env python

"""
Generate the BLOG model for the automobile problem.
"""

from automobile_data import INTENSITY_COLS
from automobile_data import LASER_COLS
from automobile_data import read_data

from collections import namedtuple
from copy import copy
from jinja2 import Environment
from jinja2 import FileSystemLoader
from StringIO import StringIO
import numpy as np


# Aggregate data types passed into the template.
Observation = namedtuple('Observation', ['timestep', 'sensors'])
Control = namedtuple('Control', ['timestep', 'controls'])

TIME_CHUNK = 0.005


def discretize_time(readings):
    """
    Discretize time into chunks of TIME_CHUNK.

    `readings` must be sorted by time.

    Returns a list of Readings, where each reading has an extra `timestep`
    field that is an integer starting from zero.

    Each original reading is assigned to the nearest discrete timestep that
    occurs after the reading (i.e., each reading is delayed by at most
    TIME_CHUNK.)
    """
    disc_readings = []
    timestep = 0
    elapsed = 0
    for reading in readings:
        while elapsed < reading.time:
            timestep += 1
            elapsed += TIME_CHUNK
        disc_reading = copy(reading)
        disc_reading.timestep = timestep
        disc_readings.append(disc_reading)
    return disc_readings


def blog_column_vector_filter(val):
    """
    Jinja2 filter for outputting a column vector in BLOG format.
    """
    return '[{}]'.format('; '.join(map(unicode, val)))


def blog_matrix_filter(val):
    """
    Jinja2 filter for outputting a matrix in BLOG format.
    """
    stream = StringIO()
    print >>stream, '['
    for i, row in enumerate(val):
        print >>stream, '    [{}]{}'.format(
            ', '.join(map(unicode, row)),
            '' if i == len(val) - 1 else ',')
    print >>stream, ']',
    return stream.getvalue()


def generate_model(disc_readings):
    """
    Take discretized readings and return the generated model as a string.
    """
    state_size = 3
    controls_size = 2
    sensors_size = INTENSITY_COLS + LASER_COLS

    # Model parameters (values completely made up):
    model_vars = {}
    model_vars['A'] = np.eye(state_size)
    model_vars['B'] = np.eye(state_size, controls_size)
    model_vars['C'] = np.eye(sensors_size, state_size)
    model_vars['V'] = np.eye(state_size)
    model_vars['W'] = np.eye(sensors_size)
    model_vars['mu_0'] = np.zeros(state_size)
    model_vars['sigma_0'] = np.eye(state_size)

    # Observations and controls:
    last_timestep = disc_readings[-1].timestep
    observations = []
    controls = []
    for reading in disc_readings:
        if reading.laser:
            observations.append(Observation(
                timestep=reading.timestep,
                sensors=reading.laser + reading.intensity))
        elif reading.velocity:
            controls.append(Control(
                timestep=reading.timestep,
                controls=(reading.velocity, reading.steering)))
        else:
            pass  # skip GPS readings

    # Generate the model.
    env = Environment(loader=FileSystemLoader('.'))
    env.filters['blog_column_vector'] = blog_column_vector_filter
    env.filters['blog_matrix'] = blog_matrix_filter
    template = env.get_template('automobile.blog.template')
    return template.render(
        model=model_vars,
        observations=observations,
        controls=controls,
        last_timestep=last_timestep)


if __name__ == "__main__":
    data_dir = "./data/automobile/1_straight/data/ground/"
    readings = read_data(data_dir)
    disc_readings = discretize_time(readings)
    # XXX Output only a few, to keep model size small.
    disc_readings = disc_readings[:100]
    model = generate_model(disc_readings)
    with open('automobile.blog', 'w') as f:
        f.write(model)
