#!/usr/bin/env python

"""
Generate the BLOG model for the automobile problem.
"""

from automobile_data import read_data
from automobile_data import read_metadata
from automobile_data import Reading
import filters

from collections import namedtuple
from copy import copy
from jinja2 import Environment
from jinja2 import FileSystemLoader
import numpy as np


# Aggregate data types passed into the template.
Observation = namedtuple('Observation', ['timestep', 'laser', 'intensity'])
Control = namedtuple('Control', ['timestep', 'velocity', 'steering'])

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


def generate_model(disc_readings, car_params):
    """
    Take discretized readings and return the generated model as a string.
    """
    # Model parameters (values completely made up):
    model_vars = {}
    model_vars['delta_t'] = TIME_CHUNK
    model_vars['car_params'] = [
        car_params.a, car_params.b, car_params.h, car_params.L]
    model_vars['initial_state'] = np.zeros(6)
    model_vars['obstacle_x'] = 3.0
    model_vars['obstacle_y'] = 2.0
    model_vars['obstacle_r'] = 1.0

    # Observations and controls:
    # (We fill in controls even at time steps where they are not observed.)
    last_timestep = disc_readings[-1].timestep
    observations = []
    controls = []
    next_control_timestep = 0
    prev_control_reading = Reading()
    prev_control_reading.velocity = 0.0
    prev_control_reading.steering = 0.0
    for reading in disc_readings:
        if reading.laser:
            observations.append(Observation(
                timestep=reading.timestep,
                laser=reading.laser,
                intensity=reading.intensity))
        elif reading.velocity:
            # Fill in unobserved timesteps with the previous control.
            while next_control_timestep < reading.timestep:
                controls.append([
                    prev_control_reading.velocity,
                    prev_control_reading.steering])
                next_control_timestep += 1
            # Add the new control:
            controls.append([reading.velocity, reading.steering])
            next_control_timestep += 1
            prev_control_reading = reading
        else:
            pass  # skip GPS readings
    controls = np.array(controls)

    # Generate the model.
    env = Environment(loader=FileSystemLoader('.'))
    env.filters['blog_column_vector'] = filters.blog_column_vector_filter
    env.filters['blog_matrix'] = filters.blog_matrix_filter
    template = env.get_template('automobile.blog.template')
    return template.render(
        model=model_vars,
        observations=observations,
        controls=controls,
        last_timestep=last_timestep)


if __name__ == "__main__":
    data_dir = "./data/automobile/1_straight/data/ground/"
    readings = read_data(data_dir)
    car_params, obstacles = read_metadata(data_dir)
    disc_readings = discretize_time(readings)
    # XXX Output only a few, to keep model size small.
    # XXX Note in updated dataset, there are no control readings until 0.246
    # sec, which is in disc_readings[11]... So need to output more than 11.
    disc_readings = disc_readings[:20]
    model = generate_model(disc_readings, car_params)
    with open('automobile.blog', 'w') as f:
        f.write(model)
