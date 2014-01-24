#!/usr/bin/env python

"""
Use this script to generate a model with a given number of samples:

./multivar_gen.py 20 >multivar-20.blog
./multivar_gen.py 1000 >multivar-1000.blog
"""

import filters

from collections import namedtuple
from jinja2 import Environment
from jinja2 import FileSystemLoader
import numpy as np
import sys


Observation = namedtuple('Observation', ['timestep', 'value'])


def generate_model(mean, covar, num_samples):
    """
    Return the generated model as a string.
    """
    # Observations:
    observations = []
    for timestep in xrange(num_samples):
        value = np.random.multivariate_normal(mean, covar)
        observations.append(Observation(timestep, value))

    # Generate the model.
    env = Environment(loader=FileSystemLoader('.'))
    env.filters['blog_column_vector'] = filters.blog_column_vector_filter
    env.filters['blog_matrix'] = filters.blog_matrix_filter
    template = env.get_template('multivar.blog.template')
    return template.render(observations=observations)


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print >>sys.stderr, \
            "Usage: {} num_observations >model.blog".format(sys.argv[0])
        sys.exit(1)
    num_samples = int(sys.argv[1])
    mean = np.array([1, 0, -1])
    covar = np.array([[1, 0, 0], [0, 1, 0], [0, 0, 1]])
    model = generate_model(mean, covar, num_samples)
    print model
