""" Grab confident probabilities

	$:~ python3 confident_probs.py <scored_file> <lower_threshold> <optional_upper_threshold>
"""

import sys

def main():

	args = sys.argv[1:]

	results = open(args[0], 'r')

	lower_threshold = float(args[1])

	try:
		upper_threshold = float(args[2])
	except:
		upper_threshold = 1.0

	for line in results:

		if 'Score' not in line:
			continue

		prob = float(line.strip().split()[-1])

		if upper_threshold >= prob >= lower_threshold:
			print(line.strip())
			x = results.readline().strip().split('|')
			print(x[0].strip())
			print(x[1].strip())
			print()

		# End for loop

	# End function

if __name__ == '__main__':
	main()