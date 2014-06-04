""" Label sentences given a map from actual relation to BLOG object and sentence labels 

	$:~ python3 add_observed_labels.py <.eblog file> <dictionary_file> <sentence_labels_file> <output_file>

"""

import sys

def main():

	args = sys.argv[1:]

	eblog_file = open(args[0], 'r')
	map_file = open(args[1], 'r')
	sentence_labels_file = open(args[2], 'r')
	output_file = open(args[3], 'w')

	# Create the actual relation to BLOG object map
	label_dict = {}
	for line in map_file:
		line = line.strip()
		if line == '':
			continue
		line = line.split(': ')
		label_dict[line[1]] = line[0]

	# Create the sentence to BLOG relation map
	sent_to_rel = {}
	for line in sentence_labels_file:
		line = line.strip()
		if line == '':
			continue
		line = line.split(': ')
		sent_to_rel[line[0]] = label_dict[line[-1]]

	# Add these labels to .eblog file
	already_written = set()
	for line in eblog_file:
		if 'obs' not in line:
			output_file.write(line)
			continue

		sent_num = line.split('(')[1].split(')')[0]

		# Should be first of three lines, but just in case
		observations = [line]
		new_line = eblog_file.readline()
		while 'obs' in new_line:
			observations.append(new_line)
			new_line = eblog_file.readline()

		for obs in observations:
			output_file.write(obs)
		
		if sent_num in sent_to_rel and sent_to_rel[sent_num] not in already_written:
			output_file.write('obs Rel(SourceFact({0})) = '.format(sent_num) + sent_to_rel[sent_num] + ';\n')
			already_written.add(sent_to_rel[sent_num])
		
		output_file.write('\n')




if __name__ == '__main__':
	main()