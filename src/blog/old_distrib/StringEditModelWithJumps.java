/*
 * Copyright (c) 2005, 2006, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.  
 *
 * * Neither the name of the University of California, Berkeley nor
 *   the names of its contributors may be used to endorse or promote
 *   products derived from this software without specific prior 
 *   written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package blog.old_distrib;

import java.util.*;

/**
 * Subclass of StringEditModel that allows "jumps", that is, deletions of whole
 * substrings of the original string (rather than just single characters). This
 * is an abstract class. Subclasses must override the initJumps method to
 * specify what jumps are allowed in a given input string, and what their
 * probabilities are.
 */
public abstract class StringEditModelWithJumps extends StringEditModel {

	public StringEditModelWithJumps() {
	}

	public StringEditModelWithJumps(List params) {
		super(params);
	}

	protected double getProbInternal(String input, String output) {
		arrivingJumps = new List[input.length() + 1];
		nonJumpProb = new double[input.length() + 1];
		Arrays.fill(nonJumpProb, 1.0);
		initJumps(input);

		// p[i][n] = P(at some time, scribe has written i characters and his
		// finger is before character n of the input)
		double[][] p = new double[output.length() + 1][input.length() + 1];

		p[0][0] = 1; // other entries automatically initialized to 0
		for (int n = 0; n <= input.length(); n++) {
			for (int i = 0; i <= output.length(); i++) {
				// Ways of generating i characters of output (indexed 0
				// through i-1) using input characters before position n

				// First way: generate i-1 characters using input before
				// position n-1, then generate output[i-1] using input[n-1]
				if ((n > 0) && (i > 0)) {
					p[i][n] += (p[i - 1][n - 1] * nonJumpProb[n - 1] * probSubst(input,
							n - 1, output, i - 1, output.charAt(i - 1)));
				}

				// Second way: generate i characters using input before
				// position n-1, then do a deletion, skipping input[n-1]
				if (n > 0) {
					p[i][n] += (p[i][n - 1] * nonJumpProb[n - 1] * probDelete(input,
							n - 1, output, i));
				}

				// Third way: generate i-1 characters using input before
				// position n, then generate output[i-1] by insertion
				if (i > 0) {
					p[i][n] += (p[i - 1][n] * nonJumpProb[n] * probInsert(input, n,
							output, i - 1, output.charAt(i - 1)));
				}

				// Fourth way: generate i characters using input before
				// position m, then do a jump from m to n
				if (arrivingJumps[n] != null) {
					for (Iterator iter = arrivingJumps[n].iterator(); iter.hasNext();) {
						Jump jump = (Jump) iter.next();
						p[i][n] += (p[i][jump.origin] * jump.prob);
					}
				}
			}
		}

		// To generate the given output string, scribe must write it down and
		// have his finger after the last input character, *and* decide to
		// stop.
		return (p[output.length()][input.length()] * probStop(input,
				input.length(), output, output.length()));
	}

	/**
	 * Figures out what jumps are allowed for the given input string, and calls
	 * addJump for each one.
	 */
	protected abstract void initJumps(String input);

	/**
	 * Allows a jump from before character <code>origin</code> to before character
	 * <code>dest</code>, with probability <code>prob</code>. The destination must
	 * be greater than the origin. For any origin, the jump probabilities should
	 * sum to something <= 1.0; the remaining probability is distributed over
	 * non-jump moves. If you add several jumps from an origin to the same
	 * destination, the probabilities are added together.
	 */
	protected void addJump(int origin, int dest, double prob) {
		if (origin >= dest) {
			throw new IllegalArgumentException("Origin " + origin
					+ " greater than or equal to " + "destination " + dest + ".");
		}

		nonJumpProb[origin] -= prob;
		if (nonJumpProb[origin] < 0) {
			throw new IllegalArgumentException("Sum of jump probabilities "
					+ "from before character " + origin + " is > 1.0.");
		}

		if (arrivingJumps[dest] == null) {
			arrivingJumps[dest] = new ArrayList();
		}
		arrivingJumps[dest].add(new Jump(origin, prob));
	}

	private static class Jump {
		public int origin;
		public double prob;

		public Jump(int origin, double prob) {
			this.origin = origin;
			this.prob = prob;
		}
	}

	private List[] arrivingJumps; // each List contains Jump objects
	private double[] nonJumpProb;
}
