import nose

from numutil import norm_log_pdf
from numutil import norm_pdf


def test_norm_pdf():
    # TODO


def test_norm_log_pdf():
    # TODO
    # TODO: doesn't blow up if determinant is too big / small for double
    # precision


# normpdf in matlab stats toolbox, which I don't have...
#
# should be PDF[MultinormalDistribution[{0, 0}, {{2, 1/2}, {1/2, 1}}], {1, 1}]
# in Mathematica, but wolfram alpha is useless for this kind of query
