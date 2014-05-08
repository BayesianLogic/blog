""" Take completed results file, and output a file of just sentences so we can hand label
	the relations

	$:~ python3 write_sentences.py <input_file>

"""


import sys

def main():
	
	args = sys.argv[1:]
	input_file = open(args[0], 'r')

	sentences = {}

	for line in input_file:

		if "Score" not in line:
			print(line)
			continue

		sent_num = line.strip().split(',')[0]
		sent_num = int(sent_num.split('[')[1].split(']')[0])

		if sent_num not in sentences:
			sentences[sent_num] = input_file.readline().strip().split(' | ')[0]
		else:
			input_file.readline()

		# End for loop

	for num, sent in sorted(sentences.items()):
		print("Sent[{0}]: ".format(num) + sent)

	print(len(sentences))


if __name__ == '__main__':
	main()
