#!/usr/bin/env python

"""
Generator for dead-reckoning model.

Generates `car-dr.blog` in the current directory.
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


def generate_model(readings, car_params):
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

    # We create one timestep in our model for each controls reading and each
    # GPS reading. The time between these timesteps is not fixed. For each
    # timestep, we output the controls that were active up until that timestep.
    active_controls = np.array([0.0, 0.0])
    time = []
    controls = []
    query_timesteps = []
    for reading in readings:
        if reading.velocity:
            time.append(reading.time)
            controls.append(active_controls.copy())
            active_controls[0] = reading.velocity
            active_controls[1] = reading.steering
        elif reading.gps_latitude:
            query_timesteps.append(len(time))
            time.append(reading.time)
            controls.append(active_controls.copy())
        else:
            pass  # skip laser readings

    # Generate the model.
    env = Environment(loader=FileSystemLoader(ppaml_car.__path__))
    env.filters['blog_column_vector'] = blog_jinja.blog_column_vector_filter
    env.filters['blog_matrix'] = blog_jinja.blog_matrix_filter
    template = env.get_template('car-dr.blog.template')
    return template.render(
        model=model_vars,
        time=time,
        controls=controls,
        query_timesteps=query_timesteps)


if __name__ == "__main__":
    data_dir = path_for_dataset('1_straight', 'ground')
    readings = read_data(data_dir)
    car_params, obstacles = read_metadata(data_dir)
    # XXX Output only a few, to keep model size small.
    # readings = readings[:100]
    model = generate_model(readings, car_params)
    with open('car-dr.blog', 'w') as f:
        f.write(model)
