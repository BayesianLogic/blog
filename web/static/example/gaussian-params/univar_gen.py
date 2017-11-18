#!/usr/bin/env python

"""
Use this script to generate a model with a given number of samples:

./univar_gen.py 20 >univar-20.blog
./univar_gen.py 1000 >univar-1000.blog
"""


from collections import namedtuple
from jinja2 import Environment
from jinja2 import FileSystemLoader
import numpy as np
import sys


Observation = namedtuple('Observation', ['timestep', 'value'])


def generate_model(mean, variance, num_samples):
    """
    Return the generated model as a string.
    """
    # Observations:
    observations = []
    for timestep in xrange(num_samples):
        value = np.sqrt(variance) * np.random.randn() + mean
        observations.append(Observation(timestep, value))

    # Generate the model.
    env = Environment(loader=FileSystemLoader('.'))
    template = env.get_template('univar.blog.template')
    return template.render(observations=observations)


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print >>sys.stderr, \
            "Usage: {} num_observations >model.blog".format(sys.argv[0])
        sys.exit(1)
    num_samples = int(sys.argv[1])
    mean = 0
    variance = 5
    model = generate_model(mean, variance, num_samples)
    print model
