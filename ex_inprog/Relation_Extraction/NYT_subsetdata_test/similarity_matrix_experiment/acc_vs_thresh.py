""" Calculates accuracy vs. threshold curve for given input results file and true labels file

	$:~ python acc_vs_thresh.py <true_label_file> <1st_results_file> <2nd_results_file> ... <nth_results_file>

	Plots accurcay vs. threshold values.

	NOTE: THIS FILE TAKES SCORED FILES. CREATE SCORED FILES FROM BLOG OUTPUT BY USING compare_sentences.py 
	(probably a bad name for what the file actually does..)
"""

import matplotlib.pyplot as plt
import sys

def main():

	args = sys.argv[1:]
	true_clusters = preprocess_true_clusters(args[0])
	results_files = [args[i] for i in range(1, len(args))]

	thresholds = [x/100.0 for x in range(101)]
	accs = [[0 for x in range(len(thresholds))] for i in range(len(results_files))]

	for i in range(len(results_files)):
		for index, threshold in enumerate(thresholds):
			results_file = open(results_files[i], 'r')
			for line in results_file:
				if 'Score' not in line:
					continue
				prob = float(line.strip().split()[-1])
				s1 = int(line.strip().split('[')[1].split(']')[0])
				s2 = int(line.strip().split('[')[2].split(']')[0])
				if prob >= threshold and true_clusters[s1] == true_clusters[s2]:
					accs[i][index] += 1
			accs[i][index] /= float(n_choose_2(len(true_clusters)))

	# Title
	title = 'Similarity Experiment'

	colors = ['r', 'g', 'b', 'c', 'm', 'y', 'k'] # I hope there are less than this many graphs..
	for i in range(1, len(args)):
		label_stuff = args[i].split('.')[0].split('_')
		ith_label = label_stuff[3] + ' ' + label_stuff[4]
		plt.plot(thresholds, accs[i-1], colors[i-1], label=ith_label)

	print accs[1]

	plt.legend(bbox_to_anchor=(0, 0, 1, 1), bbox_transform=plt.gcf().transFigure)
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

	return true_clusters

def n_choose_2(n):
	return n*(n-1)/2

if __name__ == '__main__':
	main()