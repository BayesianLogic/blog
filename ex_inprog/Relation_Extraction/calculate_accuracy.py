""" Calculates accuracy of classifying by trigger given dictionary of triggers and relation

	$:~ python3 calculate_accuracy.py <results_file> <sentences_file> <trigger_ground_truth_file>
"""

import sys, json

def main():

	args = sys.argv[1:]
	results = open(args[0], 'r')

	sentences = json.load(open(args[1], 'r'))
	clean_sentences(sentences)

	truth_dictionary = json.load(open(args[2], 'r')) # a dictionary from triggers to relation

	correct = 0
	total = 0

	line = results.readline()
	while line != '':
		if 'Sent[' not in line:
			line = results.readline()
			continue
		sent_num = line.strip().split('[')[1].split(']')[0] # Grab sentence number in ad hoc way whatever...
		sent_num = int(sent_num)

		# Find predicted relation (label)	
		line = results.readline()
		prediction = line.strip().split()[-1]

		# Compare prediction with truth
		if truth_dictionary[sentences[sent_num]["depPath"]] == prediction:
			correct += 1
		total += 1

		line = results.readline()


	print("Total number of data points: " + str(total))
	print("Number of Correct Labels: " + str(correct))
	print("Accuracy Rate: " + str(correct/total))


def clean_sentences(sentences):
	""" Replace all whitespace and crap with underscores """

	for sentence in sentences:
		sentence["source"] = sentence["source"].replace(' ', '_').replace('.', '').replace('&', '')
		sentence["dest"] = sentence["dest"].replace(' ', '_').replace('.', '').replace('&', '')
		sentence["depPath"] = sentence["depPath"].replace('->', '_').replace('<-', '_').replace('|', '_')

if __name__ == '__main__':
	main()