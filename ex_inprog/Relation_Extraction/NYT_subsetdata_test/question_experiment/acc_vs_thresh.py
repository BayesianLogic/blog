""" Calculates accuracy vs. threshold curve for given input results file and true labels file

	$:~ python acc_vs_thresh.py <1st_results_file> <2nd_results_file> <true_label_file>

	Plots accurcay vs. threshold values.
"""

import matplotlib.pyplot as plt
import sys

def main():

	args = sys.argv[1:]
	results_file_1 = open(args[0], 'r').readlines()
	results_file_2 = open(args[1], 'r').readlines()
	true_label_file = open(args[2], 'r').readlines()

	# Preprocess values
	results_values_1 = [float(line.strip().split(' is ')[-1]) for line in results_file_1]
	results_values_2 = [float(line.strip().split(' is ')[-1]) for line in results_file_2]
	true_values = [eval(line.strip().split('| ')[-1]) for line in true_label_file]

	thresholds = [x/100.0 for x in range(101)] # this gives [0.1, 0.2, ..., 1.0]
	print(thresholds)

	accs_1 = [0 for x in range(len(thresholds))]
	accs_2 = [0 for x in range(len(thresholds))]

	for index, threshold in enumerate(thresholds):
		for j in range(len(results_values_1)):
			if (results_values_1[j] >= threshold) == true_values[j]:
				accs_1[index] += 1
			if (results_values_2[j] >= threshold) == true_values[j]:
				accs_2[index] += 1
		accs_1[index] /= float(len(results_values_1))
		accs_2[index] /= float(len(results_values_2))

	# Title
	title = args[0].split('.')[0].split('_')
	title = title[3] + ' ' + title[4]

	plt.plot(thresholds, accs_1, 'r', label='1 sample')
	plt.plot(thresholds, accs_2, 'g', label='10M samples')
	plt.legend(bbox_to_anchor=(0, 0, 1, 1), bbox_transform=plt.gcf().transFigure)
	plt.xlabel('Threshold Value')
	plt.ylabel('Accuracy')
	plt.title(title)
	plt.show()






if __name__ == '__main__':
	main()