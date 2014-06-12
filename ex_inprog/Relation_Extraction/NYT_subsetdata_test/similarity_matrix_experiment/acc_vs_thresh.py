""" Calculates accuracy vs. threshold curve for given input results file and true labels file

	$:~ python acc_vs_thresh.py <results_file> <true_label_file>

	Plots accurcay vs. threshold values.
"""

import matplotlib.pyplot as plt
import sys

def main():

	args = sys.argv[1:]
	true_clusters = preprocess_true_clusters(args[1])

	thresholds = [x/10.0 for x in range(1,11)] # this gives [0.1, 0.2, ..., 1.0]
	accs = [0 for x in range(len(thresholds))]

	for index, threshold in enumerate(thresholds):
		results_file = open(args[0], 'r')
		for line in results_file:
			if 'Score' not in line:
				continue
			prob = float(line.strip().split()[-1])
			s1 = int(line.strip().split('[')[1].split(']')[0])
			s2 = int(line.strip().split('[')[2].split(']')[0])
			if prob >= threshold and true_clusters[s1] == true_clusters[s2]:
				accs[index] += 1
		accs[index] /= float(n_choose_2(len(true_clusters)))

	# Title
	title = 'Similarity Experiment'

	plt.plot(thresholds, accs)
	plt.xlabel('Threshold Value')
	plt.ylabel('Accuracy')
	plt.title(title)
	plt.show()

def preprocess_true_clusters(file_name):
	""" Return a dictionary of sentence number to relation """

	f = open(file_name, 'r')
	true_clusters = {}

	for line in f:
		sent_num = int(line.strip().split('[')[1].split(']')[0])
		rel = line.strip().split()[-1]

		true_clusters[sent_num] = rel

		# End for loop
	print len(true_clusters)

	return true_clusters

def n_choose_2(n):
	return n*(n-1)/2

if __name__ == '__main__':
	main()