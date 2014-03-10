""" Converts a JSON list of triples (represented as dictionaries) to a file of BLOG observations
	
	Call it like this:

	$:~ python3 convert_triples_to_BLOG_obs.py <data_file> <model_file> <output_file_name>

	Will create a .eblog and .qblog file

	Things to print:

	- observed value of subject, object, verb
	- distinct Relation R[..];
	- distinct Sentence Sent[..];
	- distinct Entity X0, X1, ...;
	- distinct Trigger T0, T1, ...;
	- fixed Trigger idToTrigger(Integer i)
		= TabularInterp(...)

	Write a query file on your own

"""

import sys, json, string

def main():

	args = sys.argv[1:]
	
	data_file = open(args[0], 'r', encoding='latin-1')

	sentences = json.load(data_file)
	if type(sentences) == dict: # This is for full data set. Most data files I have created on my own, and manually configured.
		sentences = sentences['sentences']
	sentences = clean_sentences(sentences)

	evidence_output_file = open(args[2] + '.eblog', 'w')
	model_file = open(args[1], 'a')

	# Need a set of entities and triggers
	entity_set = set()
	trigger_set = set()

	##### WRITE TO OBSERVATION FILE #####

	uniq_arg_pairs = set()

	evidence_output_file.write("/* Observation file */\n\n")
	for index, sentence in enumerate(sentences):

		# Write observations of subject, object, verb
		evidence_output_file.write("obs Subject(Sent[{0}]) = {1};\n".format(index, sentence["source"]))
		evidence_output_file.write("obs Object(Sent[{0}]) = {1};\n".format(index, sentence["dest"]))
		evidence_output_file.write("obs Verb(Sent[{0}]) = {1};\n".format(index, sentence["depPath"]))
		evidence_output_file.write("\n")

		# Add entities and triggers to respective sets
		entity_set.add(sentence["source"])
		entity_set.add(sentence["dest"])
		trigger_set.add(sentence["depPath"])

		uniq_arg_pairs.add(sentence["source"]+sentence["dest"])
		uniq_arg_pairs.add(sentence["dest"]+sentence["source"])

		# End for loop

	data_file.close()
	evidence_output_file.close()

	##### WRITE TO MODEL FILE #####

	# Let's call number of relations to be some value. Write it to model file
	num_relations = 100
	model_file.write("\n\ndistinct Relation R[{0}];\n".format(num_relations))

	# Write out number of sentences
	model_file.write("distinct Sentence Sent[{0}];\n\n".format(len(sentences)))

	# Write out entityes and triggers
	model_file.write("distinct Entity " + ', '.join(list(entity_set)))
	model_file.write(";\n\n")
	model_file.write("distinct Trigger " + ', '.join(list(trigger_set)))
	model_file.write(";\n\n")

	# Need to create the idToTrigger thing
	model_file.write("fixed Trigger idToTrigger(Integer i)\n")
	model_file.write("  = TabularInterp(1,\n")
	for index, trigger in enumerate(trigger_set):
		if index != len(trigger_set) - 1:
			model_file.write("    {0}, {1},\n".format(index, trigger))
		else:
			model_file.write("    {0}, {1}\n".format(index, trigger))
	model_file.write("  );\n\n\n")

	model_file.close()

	##### WRITE TO QUERY FILE #####

	# Write queries here
	query_output_file = open(args[2] + '.qblog', 'w')
	query_output_file.write("/* Queries go here */\n\n")
	for i in range(len(sentences)):
		query_output_file.write("query Rel(SourceFact(Sent[{0}]));\n".format(i))

	query_output_file.close()

	print("Number of unique sentence arg pairs:" + str(len(uniq_arg_pairs)/2))

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
