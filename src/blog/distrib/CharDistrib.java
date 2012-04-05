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

package blog.distrib;

import java.util.*;
import Jama.Matrix;
import blog.*;
import blog.common.Util;

/**
 * A mixture between a multinomial distribution over a fixed set of characters
 * (called the enumerated characters), and a uniform distribution over all
 * characters (i.e., all unsigned 16-bit numbers).
 */
public class CharDistrib extends AbstractCondProbDistrib {
	/**
	 * Creates a new CharDistrib with the given enumerated characters and the
	 * given weight on the uniform distribution. The distribution over the
	 * enumerated characters is uniform.
	 */
	public CharDistrib(char[] enumChars, double uniformWeight) {
		this.enumChars = enumChars;
		initCharIndices();
		enumDistrib = new Categorical(enumChars.length);

		if ((uniformWeight < 0) || (uniformWeight > 1)) {
			throw new IllegalArgumentException("uniformWeight must be between "
					+ "0 and 1.");
		}
		this.uniformWeight = uniformWeight;
	}

	/**
	 * Creates a new CharDistrib with the given enumerated characters, the given
	 * distribution over the enumerated characters, and the given weight on the
	 * uniform distribution.
	 */
	public CharDistrib(char[] enumChars, double[] pi, double uniformWeight) {
		this.enumChars = enumChars;
		initCharIndices();
		enumDistrib = new Categorical(pi);

		if ((uniformWeight < 0) || (uniformWeight > 1)) {
			throw new IllegalArgumentException("uniformWeight must be between "
					+ "0 and 1.");
		}
		this.uniformWeight = uniformWeight;
	}

	/**
	 * Creates a new CharDistrib with the given parameters. A CharDistrib expects
	 * three parameters:
	 * <ol>
	 * <li>A string consisting of the enumerated characters;
	 * <li>A 1xN or Nx1 matrix specifying the probability of each enumerated
	 * character (where N is the number of enumerated characters);
	 * <li>The mixture weight on the uniform distribution (as a Number).
	 * </ol>
	 */
	public CharDistrib(List params) {
		if (params.size() != 3) {
			throw new IllegalArgumentException(
					"CharDistrib expects three parameters: string of enumerated "
							+ "characters, probability vector, and weight on uniform "
							+ "distribution.");
		}

		if (params.get(0) instanceof String) {
			enumChars = ((String) params.get(0)).toCharArray();
			initCharIndices();
		} else {
			throw new IllegalArgumentException(
					"First parameter to CharDistrib must be a String.");
		}

		if (params.get(1) instanceof Matrix) {
			Matrix pi = (Matrix) params.get(1);
			if ((pi.getRowDimension() == 1)
					&& (pi.getColumnDimension() == enumChars.length)) {
				enumDistrib = new Categorical(pi.getRowPackedCopy());
			} else if ((pi.getRowDimension() == enumChars.length)
					&& (pi.getColumnDimension() == 1)) {
				enumDistrib = new Categorical(pi.getColumnPackedCopy());
			} else {
				throw new IllegalArgumentException(
						"Probability vector must be either 1xN or Nx1, where "
								+ "N is length of enumerated character string.");
			}
		} else {
			throw new IllegalArgumentException(
					"Second parameter to CharDistrib must be a vector "
							+ "(represented as a Matrix object).");
		}

		if (params.get(2) instanceof Number) {
			uniformWeight = ((Number) params.get(2)).doubleValue();
			if ((uniformWeight < 0) || (uniformWeight > 1)) {
				throw new IllegalArgumentException(
						"Weight on uniform distrib must be between 0 and 1.");
			}
		} else {
			throw new IllegalArgumentException(
					"Third parameter to CharDistrib must be a Number.");
		}
	}

	/**
	 * Returns the probability of the given character.
	 */
	public double getProb(char c) {
		double prob = uniformWeight / Character.MAX_VALUE;
		int index = getCharIndex(c);
		if (index != -1) {
			prob += ((1 - uniformWeight) * enumDistrib.getProb(index));
		}
		return prob;
	}

	/**
	 * Returns the probability of the given value, which should be of class
	 * Character. Expects no arguments.
	 */
	public double getProb(List args, Object childValue) {
		if (!args.isEmpty()) {
			throw new IllegalArgumentException("CharDistrib expects no arguments.");
		}

		if (childValue instanceof Character) {
			return getProb(((Character) childValue).charValue());
		} else {
			throw new IllegalArgumentException(
					"CharDistrib defines distribution over objects of class "
							+ "Character, not " + childValue.getClass());
		}
	}

	/**
	 * Returns the log probability of the given character.
	 */
	public double getLogProb(char c) {
		return Math.log(getProb(c));
	}

	/**
	 * Returns a character selected randomly according to this distribution.
	 */
	public char sampleVal() {
		if (Util.random() < uniformWeight) {
			return (char) Util.randInt(Character.MAX_VALUE);
		}
		int index = enumDistrib.sampleVal();
		return enumChars[index];
	}

	/**
	 * Returns a Character object sampled randomly according to this distribution.
	 * Expects no arguments.
	 */
	public Object sampleVal(List args, Type childType) {
		if (!args.isEmpty()) {
			throw new IllegalArgumentException("CharDistrib expects no arguments.");
		}

		return new Character(sampleVal());
	}

	void initCharIndices() {
		int maxChar = 0;
		for (int i = 0; i < enumChars.length; i++) {
			if (enumChars[i] > maxChar) {
				maxChar = enumChars[i];
			}
		}

		charIndices = new int[maxChar + 1];
		for (int i = 0; i < charIndices.length; i++) {
			charIndices[i] = -1;
		}

		for (int i = 0; i < enumChars.length; i++) {
			charIndices[enumChars[i]] = i;
		}
	}

	int getCharIndex(char c) {
		if (c >= charIndices.length) {
			return -1;
		}
		return charIndices[c];
	}

	char[] enumChars;
	int[] charIndices;
	Categorical enumDistrib;
	double uniformWeight;
}
