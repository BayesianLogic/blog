import numpy as np


def norm_pdf(x, mu, sigma):
    """
    PDF of multivariate normal distribution.

    (scipy.stats.multivariate_normal only exists in scipy 0.14.0+)
    """
    k = len(x)
    norm_const = 1.0 / np.sqrt(((2 * np.pi) ** k) * np.linalg.det(sigma))
    sigma_inv = np.linalg.inv(sigma)
    return norm_const * np.exp(-0.5 * (x - mu).dot(sigma_inv).dot(x - mu))


def norm_log_pdf_fixed_sigma(sigma):
    """
    Return a function f(x, mu) that evaluates the norm_log_pdf for this sigma.

    Use this when you need to evaluate the pdf many times for some fixed sigma.
    """
    k = sigma.shape[0]
    _, logdet = np.linalg.slogdet(sigma)
    log_norm_const = -0.5 * (k * np.log(2 * np.pi) + logdet)
    sigma_inv = np.linalg.inv(sigma)

    def func(x, mu):
        return log_norm_const - 0.5 * (x - mu).dot(sigma_inv).dot(x - mu)

    return func


def norm_log_pdf(x, mu, sigma):
    """
    Log PDF of multivariate normal distribution.
    """
    return norm_log_pdf_fixed_sigma(sigma)(x, mu)
