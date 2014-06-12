""" Given an Object, create the query file for asking the question:

	Is the fact of (R[0], Entity e, Object) true? This question is asked for every possible Entity e.

	For a sentence that does not have the correct object, the probability of this query will be 0 due to the
	relevant fact initialization and data driven MCMC. We only consider Entities such that there exists a sentence
	where the Object is present.

	Call it like this:

	$:~ python3 create_query_file.py <.eblog file> <output_qblog_file> <Object 1> <Object 2> ... <Object N>
"""

import sys

def main():

	args = sys.argv[1:]

	eblog_file = args[0]
	qblog_file = args[1]

	objects = args[2:]

	eblog_file = open(eblog_file, 'r')
	qblog_file = open(qblog_file, 'w')

	qblog_file.write("random Fact F(Entity e1, Entity e2) ~ Iota({{ Fact f : Rel(f) == R[0] & Arg1(f) == e1 & Arg2(f) == e2 }});\n\n")

	used_entities = set()

	objects = {x:0 for x in objects} # keep track of how many times each object will be queried, so not all queries are NYU

	for line in eblog_file:
		if 'Subject' not in line:
			continue
		
		arg1 = line.strip().split(' = ')[-1][:-1]
		next_line = eblog_file.readline()
		arg2 = next_line.strip().split(' = ')[-1][:-1]
		next_line = eblog_file.readline()
		trigger = next_line.strip().split(' = ')[-1][:-1]
		next_line = eblog_file.readline()
		if 'Rel' in next_line: # Don't query a tagged sentence
			continue

		if arg2 in objects and arg1 not in used_entities and objects[arg2] < 6:
			qblog_file.write('query Holds(F({0}, {1}));\t// {2} {3} {4}\n'.format(arg1, arg2, arg1, get_clean_trig(trigger), arg2))
			used_entities.add(arg1)
			objects[arg2] += 1

	# End for loop

	eblog_file.close()
	qblog_file.close()


def get_clean_trig(trigger):

	#return sent_dict['depPath']

	list_of_stuff = trigger.replace('_', ' ').split()[2:-2]
	trig = ''
	n = 0
	while n < len(list_of_stuff):
		trig += list_of_stuff[n] + ' '
		n += 2
	return trig


if __name__ == '__main__':
	main()