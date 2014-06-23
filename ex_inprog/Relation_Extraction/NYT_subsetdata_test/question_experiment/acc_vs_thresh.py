""" Calculates accuracy vs. threshold curve for given input results file and true labels file

	$:~ python acc_vs_thresh.py <true_label_file> <1st_results_file> <2nd_results_file> ... <nth_results_file>

	Plots accurcay vs. threshold values.
"""

import matplotlib.pyplot as plt
import sys

def main():

	args = sys.argv[1:]
	true_label_file = open(args[0], 'r').readlines()
	results_files = [open(args[i], 'r') for i in range(1, len(args))]

	# Only two experiments, so I can quantify this here
	if args[1].startswith('36'):
		num_queries = 36
	else:
		num_queries = 14

	# Preprocess values
	true_values = [eval(line.strip().split('| ')[-1]) for line in true_label_file]

	results_values = [[0 for x in range(num_queries)] for i in range(len(results_files))]
	for i in range(len(results_files)):
		j = 0
		line = results_files[i].readline()
		while line != '': # Get the probability of true for each query

			if "Distribution" in line:
				next_line = results_files[i].readline().strip().split()

				if next_line[-1] == 'true': # Add to results_values and move on
					results_values[i][j] = float(next_line[0])
				else: # false is first
					prob_for_false = float(next_line[0])
					results_values[i][j] = 1 - prob_for_false
				j += 1

			line = results_files[i].readline()


	thresholds = [x/100.0 for x in range(101)]
	
	accs = [[0 for x in range(len(thresholds))] for i in range(len(results_values))]

	for index, threshold in enumerate(thresholds):
		for x in range(len(results_values)):
			for y in range(len(results_values[0])):
				if (results_values[x][y] >= threshold) == true_values[y]:
					accs[x][index] += 1
			accs[x][index] /= float(len(results_values[x]))

	# Title
	title = args[1].split('.')[0].split('_')
	title = title[3] + ' ' + title[4]

	# Plot each graph
	colors = ['r', 'g', 'b', 'c', 'm', 'y', 'k'] # I hope there are less than this many graphs..
	for i in range(1, len(args)):
		label_stuff = args[i].split('.')[0].split('_')
		ith_label = label_stuff[5] + ' ' + label_stuff[6]
		plt.plot(thresholds, accs[i-1], colors[i-1], label=ith_label)

	plt.legend(bbox_to_anchor=(0, 0, 1, 1), bbox_transform=plt.gcf().transFigure)
	plt.xlabel('Threshold Value')
	plt.ylabel('Accuracy')
	plt.title(title)
	plt.show()






if __name__ == '__main__':
	main()