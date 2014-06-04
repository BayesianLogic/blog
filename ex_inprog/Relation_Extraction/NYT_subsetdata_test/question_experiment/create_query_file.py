""" Given an Object, create the query file for asking the question:

	Is the fact of (R[0], Entity e, Object) true? This question is asked for every possible Entity e.

	For a sentence that does not have the correct object, the probability of this query will be 0 due to the
	relevant fact initialization and data driven MCMC. We only consider Entities such that there exists a sentence
	where the Object is present.

	Call it like this:

	$:~ python3 create_query_file.py <Object> <.eblog file> <output_qblog_file>
"""

import sys

def main():

	args = sys.argv[1:]

	Object = args[0]
	eblog_file = args[1]
	qblog_file = args[2]

	eblog_file = open(eblog_file, 'r')
	qblog_file = open(qblog_file, 'w')

	qblog_file.write("random Fact F(Entity e) ~ Iota({{ Fact f : Rel(f) == R[0] & Arg1(f) == e & Arg2(f) == {0} }});\n\n".format(Object))

	used_entities = set()

	for line in eblog_file:
		if 'Subject' not in line:
			continue
		
		arg1 = line.strip().split(' = ')[-1][:-1]
		next_line = eblog_file.readline()
		arg2 = next_line.strip().split(' = ')[-1][:-1]
		next_line = eblog_file.readline()
		trigger = next_line.strip().split(' = ')[-1][:-1]

		if arg2 == Object and arg1 not in used_entities:
			qblog_file.write('query Holds(F({0}));\t// {1} {2} {3}\n'.format(arg1, arg1, get_clean_trig(trigger), arg2))
			used_entities.add(arg1)

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