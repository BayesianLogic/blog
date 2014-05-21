#!/usr/bin/env python

"""
Generator for model with lasers.

Generates `car-laser.blog` in the current directory.
"""

from ppaml_car.blog_gen import gps_reading_to_state
from ppaml_car.data import path_for_dataset
from ppaml_car.data import read_data
from ppaml_car.data import read_metadata
import blog_jinja
import ppaml_car

from jinja2 import Environment
from jinja2 import FileSystemLoader
import numpy as np
import sys


def generate_model(readings, car_params, obstacles):
    """
    Take readings and return the generated model as a string.
    """
    # Initial state comes from the first GPS observation.
    first_gps_reading = next(
        reading for reading in readings if reading.gps_latitude)
    initial_state = gps_reading_to_state(first_gps_reading)

    # Model parameters.
    model_vars = {}
    model_vars['car_params'] = [
        car_params.a, car_params.b, car_params.h, car_params.L]
    model_vars['initial_state'] = initial_state
    model_vars['obstacles'] = obstacles

    # We create one timestep in our model for:
    # - each controls reading,
    # - each GPS reading,
    # - each laser reading.
    # The time between these timesteps is not fixed. For each timestep, we
    # output the controls that were active up until that timestep.
    active_controls = np.array([0.0, 0.0])
    time = []
    controls = []
    for reading in readings:
        if reading.velocity:
            time.append(reading.time)
            controls.append(active_controls.copy())
            active_controls[0] = reading.velocity
            active_controls[1] = reading.steering
        elif reading.gps_latitude:
            time.append(reading.time)
            controls.append(active_controls.copy())
        elif reading.laser:
            time.append(reading.time)
            controls.append(active_controls.copy())
        else:
            assert False

    # GPS observations are (timestep, location) tuples.
    # Laser observations are (timestep, lasers) tuples.
    gps_observations = []
    laser_observations = []
    query_timesteps = []
    current_timestep = 0
    for reading in readings:
        if reading.velocity:
            current_timestep += 1
        elif reading.gps_latitude:
            gps_observations.append((
                current_timestep,
                gps_reading_to_state(reading)[:2]))
            query_timesteps.append(current_timestep)
            current_timestep += 1
        elif reading.laser:
            laser_observations.append((
                current_timestep,
                reading.laser))
            current_timestep += 1
        else:
            assert False
    assert current_timestep == len(time)

    # Generate the model.
    env = Environment(loader=FileSystemLoader(ppaml_car.__path__))
    env.filters['blog_column_vector'] = blog_jinja.blog_column_vector_filter
    env.filters['blog_matrix'] = blog_jinja.blog_matrix_filter
    template = env.get_template('car-laser.blog.template')
    return template.render(
        model=model_vars,
        gps_observations=gps_observations,
        laser_observations=laser_observations,
        time=time,
        controls=controls,
        query_timesteps=query_timesteps)


if __name__ == "__main__":
    if len(sys.argv) != 3:
        raise RuntimeError(
            "Usage example: {} 1_straight ground".format(sys.argv[0]))
    data_dir = path_for_dataset(sys.argv[1], sys.argv[2])
    readings = read_data(data_dir)
    car_params, obstacles = read_metadata(data_dir)
    # Hardcoded obstacle radius, since not provided in dataset.
    obstacles = np.array([(x, y, 0.4) for x, y in obstacles])
    # XXX Output only a few, to keep model size small.
    readings = readings[:1000]
    model = generate_model(readings, car_params, obstacles)
    with open('car-laser.blog', 'w') as f:
        f.write(model)
