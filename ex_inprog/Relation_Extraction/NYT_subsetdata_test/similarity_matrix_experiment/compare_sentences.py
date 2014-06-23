""" Given results of samples from BLOG of the qblog file that queries a similarity matrix, 
	output a file where each line has the sentences numbers and score, and the line after
	it has the sentences separated by some separator

	Call it like this:

	$:~ python3 clean_triggers.py <results_probs_file> <original_sentence_file> <output_file_name>

	Output looks like this:

	Sent[51], Sent[54]	Score: 0.029118571428571428
	Stephen_Gillers professor at New_York_University | Merle_Black professor at Emory_University
	...

"""

import sys, json, string

def compare_sentences():

	args = sys.argv[1:]
	
	sentences_file = open(args[1], 'r', encoding='latin-1')

	sentences = json.load(sentences_file)
	sentences_file.close()
	if type(sentences) == dict: # This is for full data set. Most data files I have created on my own, and manually configured.
		sentences = sentences['sentences']
	sentences = clean_sentences(sentences)

	results_file = open(args[0], 'r')
	output_file = open(args[2], 'w')

	for line in results_file:

		if line.strip() == '' or 'Distribution' not in line:
			continue

		s1 = int(line.strip().split('[')[1].split(']')[0])
		s2 = int(line.strip().split('[')[2].split(']')[0])

		next_line = results_file.readline().strip().split()
		if next_line[-1] == 'true':
			score = float(next_line[0])
		else:
			prob_for_false = float(next_line[0])
			score = 1 - prob_for_false

		output_file.write('Sent[{0}], Sent[{1}]\tScore: '.format(s1,s2) + str(score) + '\n\t' + construct_clean_sent(sentences[s1]) + ' | ' + construct_clean_sent(sentences[s2]) + '\n')

		# End for loop

	output_file.close()
	results_file.close()

def construct_clean_sent(sent_dict):

	return sent_dict['source'] + ' ' + get_clean_trig(sent_dict) + '' + sent_dict['dest']

def get_clean_trig(sent_dict):

	#return sent_dict['depPath']

	list_of_stuff = sent_dict["depPath"].replace('_', ' ').split()[2:-2]
	trig = ''
	n = 0
	while n < len(list_of_stuff):
		trig += list_of_stuff[n] + ' '
		n += 2
	return trig

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
	compare_sentences()
