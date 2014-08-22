import re
import math
import sys

class Distribution(object):
    def __init__(self):
        self.probs = {}

    def add(self, key, value):
        self.probs[key] = value

    def get(self, key):
        return self.probs.get(key, 0.0)

    def __str__(self):
        s = ""
        for key in self.probs:
            s += "\t" + key + " : " + str(self.probs[key]) + "\n"
        return s

"""
    Returns the list of distribution
    given a list of all the lines in a file
"""
def getListDistributions(lines):
    distributions = {}
    for index, line in enumerate(lines):
        if len(line) >= 28 and line[:12] == "Distribution":
            name = line[27:-1]
            dsn = getDistribution(lines, index)
            
            dsn_list = distributions.get(name, 0)
            if dsn_list == 0:
                distributions[name] = [dsn]
            else:
                dsn_list.append(dsn)

    return distributions

"""
    startIndex = Line Number (0-indexed) where the Distribution starts

    Returns the distribution (assuming it is discrete), 
    which is represented as a dictionary where
    keys    = element of the support
    values  = corresponding probabilities of the distribution taking
              on that element of the support
"""
def getDistribution(lines, startIndex):
    distrib = Distribution()
    count = startIndex + 1
    while (lines[count] != "======== Done ========\n" 
            and lines[count][:12] != "Distribution"):
        line = lines[count]
        p = re.compile(r'\s+(\w+)\s+([0-9.]+)')
        probs = p.match(line)
        if probs:
            distrib.add(probs.group(1), float(probs.group(2)))
        else:
            raise Exception("expecting a regex match for line: '" + str(line[:-1]) + "'")
        count += 1
    return distrib

"""
    Given a list of distributions that all have the same support and 
    all are sampled from the same origin distribution, 
    returns a distribution that is the average over all these distributions.
"""
def getEmpiricalDistribution(distributions):
    dictionary = {}
    for distribution in distributions:
        for key in distribution.probs:
            dictionary[key] = dictionary.get(key, 0.0) + distribution.probs[key]
    N = len(distributions)
    for key in dictionary:
        dictionary[key] = dictionary[key] / N
    dist = Distribution()
    dist.probs = dictionary
    return dist

"""
    Returns the symmetric KL Divergence between dsnA and dsnB
"""
def getSymmetricKL(dsnA, dsnB):
    return (getKL(dsnA, dsnB) + getKL(dsnB, dsnA)) / 2

def getKL(dsnA, dsnB):
    val = 0.0
    for key in dsnA.probs:
        p = dsnA.get(key)
        q = dsnB.get(key)
        if q == 0.0:
            return float("inf")
        val += (math.log(p/q) * p)
    return val

def getAverageKL(distributions, actual_distribution):
    KL = 0.0
    for distribution in distributions:
        KL += getSymmetricKL(distribution, actual_distribution)
    return KL / len(distributions)

# Read in argument
# The first argument is used for parallel processes
# to distinguish the different temporary files open at one time
if len(sys.argv) != 2:
    print "must provide exactly 2 arguments"

TMP_LW="tools/testing/output/tmpLW" + sys.argv[1]
TMP_MH="tools/testing/output/tmpMH" + sys.argv[1]

# Read in the LW Sampling
f = open(TMP_LW, "r")
dsnsLW = getListDistributions(f.readlines())
distribution_estimates = {}
for name in dsnsLW:
    distribution_list = dsnsLW[name]
    distribution_estimates[name] = getEmpiricalDistribution(distribution_list)

# Read in the MH Sampling
f = open(TMP_MH, "r")
dsnsMH = getListDistributions(f.readlines())

# Print out the KLs of each distribution for MH vs. LW
print("%-25s %-10s %-10s %-10s %-10s" % ("Random Variable", "LW", "MH", "Log-LW", "Log-MH"))
for name in distribution_estimates:
    KL_LW = getAverageKL(dsnsLW[name], distribution_estimates[name])
    KL_MH = getAverageKL(dsnsMH[name], distribution_estimates[name])
    KL_LW_DER = -1.0 / math.log(KL_LW)
    KL_MH_DER = -1.0 / math.log(KL_MH)
    print("%-25s %-10.6f %-10.6f %-10.3f %-10.3f" % (name, KL_LW, KL_MH, KL_LW_DER, KL_MH_DER))
