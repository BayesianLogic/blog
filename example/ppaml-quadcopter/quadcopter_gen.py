#!/usr/bin/env python

"""
Generate the BLOG model for the quadcopter problem.
"""

import filters

from jinja2 import Environment
from jinja2 import FileSystemLoader
import numpy as np


def generate_model():
    """
    Take discretized readings and return the generated model as a string.
    """
    state_size = 12
    controls_size = 4
    sensors_size = 9

    # Model parameters (values completely made up):
    model_vars = {}
    model_vars['A'] = np.eye(state_size)
    model_vars['B'] = np.eye(state_size, controls_size)
    model_vars['C'] = np.eye(sensors_size, state_size)
    model_vars['V'] = np.eye(state_size)
    model_vars['W'] = np.eye(sensors_size)
    model_vars['mu_0'] = np.zeros(state_size)
    model_vars['sigma_0'] = np.eye(state_size)

    # Generate the model.
    env = Environment(loader=FileSystemLoader('.'))
    env.filters['blog_column_vector'] = filters.blog_column_vector_filter
    env.filters['blog_matrix'] = filters.blog_matrix_filter
    template = env.get_template('quadcopter.blog.template')
    return template.render(model=model_vars)


if __name__ == "__main__":
    model = generate_model()
    with open('quadcopter.blog', 'w') as f:
        f.write(model)
