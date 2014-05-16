#!/usr/bin/env python

"""
Like blog_gen, but takes path to dataset instead of name of dataset.
"""

from ppaml_car.blog_gen import generate_model
from ppaml_car.data import read_data
from ppaml_car.data import read_metadata

import sys


if __name__ == "__main__":
    if len(sys.argv) != 2:
        raise RuntimeError("Usage example: {} dataset_dir".format(sys.argv[0]))
    data_dir = sys.argv[1]
    readings = read_data(data_dir)
    car_params, obstacles = read_metadata(data_dir)
    model = generate_model(readings, car_params)
    with open('car.blog', 'w') as f:
        f.write(model)
