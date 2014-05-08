""" Calculate precision and recall on results

	$:~ python3 prec_recall.py <results_file> <true_clusters_file> <threshold_1> <threshold_2> ... <threshold_n>

<results_file> should be in the format:
	
	Sent[x], Sent[y]  Score: 0.547383
		Jon went to the grocery store | Alex drove home
	Sent[c], Sent[d]  SCore: 0.0113531
		Bud hopped on one leg | Tony is the leader of the Bio Club
	...

<true_clusters_file> should be in the format:

	Sent[0]: Jon went to the grocery store					Relation: R[0]
	Sent[1]: Alex drove home								Relation: R[2]
	...

"""

import sys

def main():

	args = sys.argv[1:]

	threshold_list = list(map(float, args[2:]))

	true_clusters = preprocess_true_clusters(args[1])

	for threshold in threshold_list:
		calculate_precision_recall(args[0], threshold, true_clusters)

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

def calculate_precision_recall(file_name, threshold, true_clusters):
	""" Calculate and print precision and recall rates for the file using the threshold """
	results_file = open(file_name, 'r')

	# For Precision
	num_classified_as_same_rel = 0

	# For Recall
	num_total_same_rel = 0

	# For Precision and Recall
	num_classified_as_same_rel_and_correct = 0

	for line in results_file:

		if 'Score' not in line:
			continue

		prob = float(line.strip().split()[-1])
		s1 = int(line.strip().split('[')[1].split(']')[0])
		s2 = int(line.strip().split('[')[2].split(']')[0])

		if prob >= threshold:
			num_classified_as_same_rel += 1
		if true_clusters[s1] == true_clusters[s2]:
			num_total_same_rel += 1
		if prob >= threshold and true_clusters[s1] == true_clusters[s2]:
			num_classified_as_same_rel_and_correct += 1

		# End for loop

	precision = num_classified_as_same_rel_and_correct/num_classified_as_same_rel
	recall = num_classified_as_same_rel_and_correct/num_total_same_rel

	print("Threshold: " + str(threshold) + '\tPrecision: ' + str(precision) + '\tRecall: ' + str(recall))


if __name__ == '__main__':
	main()