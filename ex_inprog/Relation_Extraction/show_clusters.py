""" Takes a results file that was spit out from the Relation Extraction experiment
	and shows the clustered relations. I am taking the highest probability relation
	for each sentence.

	$:~ python3 show_clusters.py <results_file> <original_sentences_file> <cluster_file_name>

	Clusters are written like this:

	Relation: R[0]
	Triggers:
		Trig[1]		: 5
		Trig[14]	: 2

	Trig[1]:


"""

import sys, json, string

def main():

	args = sys.argv[1:]
	results = open(args[0], 'r')

	sentences = json.load(open(args[1], 'r'))
	if type(sentences) == dict: # This is for full data set. Most data files I have created on my own, and manually configured.
		sentences = sentences['sentences']
	sentences = clean_sentences(sentences)

	clusters = {}

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

		# Add to cluster
		if prediction in clusters:
			clusters[prediction].append(sent_num)
		else:
			clusters[prediction] = [sent_num]

		line = results.readline()

	# Print out clusters
	outfile = open(args[2], 'w')
	for cluster in sorted(clusters.keys()):
		outfile.write("Relation: " + cluster + '\n')

		# Find all triggers
		triggers = {}
		for sent_num in clusters[cluster]:
			if sentences[sent_num]['depPath'] in triggers:
				triggers[sentences[sent_num]['depPath']].append(sent_num)
			else:
				triggers[sentences[sent_num]['depPath']] = [sent_num]

		# Print the triggers
		outfile.write("Triggers:\n")
		for trigger in triggers:
			outfile.write('\t' + trigger + '\t: ' + str(len(triggers[trigger])) + '\n')
		outfile.write("\n")

		# Print the sentences for each trigger
		for trigger in triggers:
			outfile.write('\t' + trigger + '\t: ' + str(len(triggers[trigger])) + '\n')
			for sent_num in triggers[trigger]:
				sentence = sentences[sent_num]
				outfile.write('\t\tSentence: [' + sentence["source"] + ', ' + sentence["dest"] + ']' + '\n')

		outfile.write('\n\n')


def clean_sentences(sentences):
	""" Replace all whitespace and crap with underscores 
		For damaged sentences, have this workaround: Create a result list. Don't add damaged sentences.
	"""

	result_list = []
	for sentence in sentences:
		sentence["source"] = clean_string(sentence["source"])
		sentence["dest"] = clean_string(sentence["dest"])
		sentence["depPath"] = clean_string(sentence["depPath"])

		if not (sentence["source"] == "" or sentence["dest"] == "" or sentence["depPath"] == ""):
			result_list.append(sentence)

	return result_list

def clean_string(str):
	result = str
	for char in str:
		if char not in string.ascii_letters and char not in string.digits:
			result = result.replace(char, '_')

	# Clean sentences further
	while result.startswith('_') or result[:1].isdigit():
		result = result[1:]

	return result

if __name__ == '__main__':
	main()