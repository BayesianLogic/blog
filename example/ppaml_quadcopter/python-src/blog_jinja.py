"""
Handy Jinja2 filters for BLOG model templates.
"""

from StringIO import StringIO


def blog_column_vector_filter(val):
    """
    Jinja2 filter for outputting a column vector in BLOG format.
    """
    try:
        val[0]
    except TypeError:
        raise TypeError("value passed to blog_column_vector must be a vector")

    return '[{}]'.format('; '.join(map(unicode, val)))


def blog_matrix_filter(val):
    """
    Jinja2 filter for outputting a matrix in BLOG format.
    """
    try:
        val[0][0]
    except TypeError:
        raise TypeError("value passed to blog_matrix must be a matrix")

    stream = StringIO()
    print >>stream, '['
    for i, row in enumerate(val):
        print >>stream, '    [{}]{}'.format(
            ', '.join(map(unicode, row)),
            '' if i == len(val) - 1 else ',')
    print >>stream, ']',
    return stream.getvalue()
