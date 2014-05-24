#!/usr/bin/env python

"""
Convert BLOG JSON output to challenge-problem output format.

Supports the three tasks:
- reconstruction
- prediction
- estimation

Example usage:
    python -m convert_output out.json reconstruction
"""


import argparse
import csv
import itertools
import json
import numpy as np
import re


class BirdsData(object):
    """
    Container for the query results output by BLOG.
    """
    def __init__(self):
        # Timesteps are 0-indexed.
        # Cells are 1-indexed.
        self.num_timesteps = None
        self.num_cells = None
        self.train_timesteps = None
        self.test_timesteps = None
        self.all_cells = None
        self.avg_birds = None
        self.avg_betas = None

    def train_timestep_to_year_day(self, timestep):
        assert timestep in self.train_timesteps
        day = timestep % 20
        year = timestep / 20
        return (year + 1, day + 1)

    def test_timestep_to_year_day(self, timestep):
        assert timestep in self.test_timesteps
        day = timestep % 20
        year = (timestep - 60) / 20
        return (year + 1, day + 1)

    def avg_number_of_birds(self, timestep, src_cell, dest_cell):
        assert self.has_timestep(timestep)
        assert self.has_cell(src_cell)
        assert self.has_cell(dest_cell)
        return self.avg_birds[timestep][src_cell - 1][dest_cell - 1]

    def has_timestep(self, timestep):
        return 0 <= timestep < self.num_timesteps

    def has_cell(self, cell):
        return 0 < cell <= self.num_cells


def clean_particles(entries):
    """
    Take list of (log_prob, arr_str) entries and parse the latter.

    Return array with (log_prob, *arr) rows.
    """
    log_probs = []
    vectors = []
    for log_prob, str_vector in entries:
        str_vector = str_vector[1:-1]
        if ';' in str_vector:
            str_vector = str_vector.split('; ')
        else:
            str_vector = str_vector.split(' ')
        log_probs.append(log_prob)
        vectors.append(map(float, str_vector))
    log_probs = np.array(log_probs)
    log_probs.shape = (log_probs.shape[0], 1)
    vectors = np.array(vectors)
    return np.hstack((log_probs, vectors))


def avg_particles(samples):
    """
    Take list of (log_prob, arr) samples and return weighted average.
    """
    # np.average() normalizes the weights (so they don't have to sum to one).
    # But if all weights are tiny, then np.exp(weights) will be all zeros.
    # To fix this, we scale all weights s.t. the max weight is 1.
    log_weights = samples[:, 0]
    log_weights -= np.max(log_weights)
    return np.average(samples[:, 1:], axis=0, weights=np.exp(log_weights))


def read_blog_json_data(path):
    """
    Load BLOG JSON data into a BirdsData object.
    """
    # The queries are in this order:
    # "outflow_vector(l[0], @0)"
    # "outflow_vector(l[1], @0)"
    # ...
    # "outflow_vector(l[15], @0)"
    # "outflow_vector(l[0], @1)"
    # ...
    # "outflow_vector(l[15], @599)"
    # beta(@599)

    # First pass:
    # outflow_vector[t, s] = list of (log_prob, outflow_vector) tuples.
    # beta = list of (log_prob, beta_vector) tuples.
    data = json.load(open(path))
    outflow_re = re.compile("outflow_vector\(l\[(\d+)\], @(\d+)\)")
    beta_re = re.compile("beta\(@(\d+)\)")
    outflow_vectors = {}
    betas = []
    for query_str, entries in data:
        match = outflow_re.match(query_str)
        if match:
            src, timestep = map(int, match.groups())
            outflow_vectors[timestep, src] = clean_particles(entries)
            continue

        match = beta_re.match(query_str)
        if match:
            assert not betas
            betas = clean_particles(entries)
            continue

        assert False, "unknown query"

    # Second pass: populate BirdsData.
    blog_data = BirdsData()
    max_timestep = max(t for t, s in outflow_vectors.iterkeys())
    max_cell = max(s for t, s in outflow_vectors.iterkeys())
    blog_data.num_timesteps = max_timestep + 1
    first_test_timestep = (max_timestep + 1) / 2
    blog_data.train_timesteps = range(0, first_test_timestep)
    blog_data.test_timesteps = range(first_test_timestep, max_timestep + 1)
    blog_data.num_cells = max_cell + 1
    blog_data.all_cells = range(1, max_cell + 2)
    # avg_birds[t][s] = weighted average of outflow
    avg_birds = []
    for timestep in xrange(max_timestep + 1):
        avg_birds_ts = []
        for src_cell in xrange(max_cell + 1):
            avg_birds_ts.append(
                avg_particles(outflow_vectors[timestep, src_cell]))
        avg_birds.append(avg_birds_ts)
    blog_data.avg_birds = np.array(avg_birds)
    blog_data.avg_betas = avg_particles(betas)

    return blog_data


def output_reconstruction(blog_data):
    """
    Output train-reconstruction.csv in required format.

    Columns: year, day, from.cell, to.cell, number.of.birds
    """
    with open('train-reconstruction.csv', 'w') as csv_file:
        writer = csv.writer(csv_file)
        writer.writerow(
            ['year', 'day', 'from.cell', 'to.cell', 'number.of.birds'])
        for timestep, src_cell, dest_cell in itertools.product(
                blog_data.train_timesteps,
                blog_data.all_cells, blog_data.all_cells):
            year, day = blog_data.train_timestep_to_year_day(timestep)
            birds = blog_data.avg_number_of_birds(
                timestep, src_cell, dest_cell)
            writer.writerow([year, day, src_cell, dest_cell, birds])


def output_prediction(blog_data):
    """
    Output test-prediction.csv in required format.

    Columns: year, day, from.cell, to.cell, number.birds, number.birds2
    """
    with open('test-prediction.csv', 'w') as csv_file:
        writer = csv.writer(csv_file)
        writer.writerow(
            ['Year', 'Day', 'FromCell', 'ToCell',
             'number.birds', 'number.birds2'])
        for timestep, src_cell, dest_cell in itertools.product(
                blog_data.test_timesteps,
                blog_data.all_cells, blog_data.all_cells):
            year, day = blog_data.test_timestep_to_year_day(timestep)
            birds1 = blog_data.avg_number_of_birds(
                timestep, src_cell, dest_cell)
            birds2 = None
            if blog_data.has_timestep(timestep + 1):
                birds2 = blog_data.avg_number_of_birds(
                    timestep + 1, src_cell, dest_cell)
            writer.writerow([year, day, src_cell, dest_cell, birds1, birds2])


def output_estimation(blog_data):
    """
    Output estimation.txt in the required format.

    Four lines: beta1, beta2, beta3, beta4.
    """
    with open('../cps/dataset1/estimated-parameters.csv', 'w') as csv_file:
        writer = csv.writer(csv_file)
        writer.writerow(['b1','b2','b3','b4'])
        writer.writerow(blog_data.avg_betas)
        # print >>txt_file, blog_data.avg_betas[0]
        # print >>txt_file, blog_data.avg_betas[1]
        # print >>txt_file, blog_data.avg_betas[2]
        # print >>txt_file, blog_data.avg_betas[3]


if __name__ == "__main__":
    # Parse command-line args.
    parser = argparse.ArgumentParser(
        description='Convert BLOG JSON output to challenge output format.')
    parser.add_argument('json_file')
    parser.add_argument('task', choices=[
        'reconstruction', 'prediction', 'estimation'])
    args = parser.parse_args()

    # Load BLOG JSON output.
    blog_data = read_blog_json_data(args.json_file)

    if args.task == 'reconstruction':
        output_reconstruction(blog_data)
    elif args.task == 'prediction':
        output_prediction(blog_data)
    elif args.task == 'estimation':
        output_estimation(blog_data)
    else:
        assert False
