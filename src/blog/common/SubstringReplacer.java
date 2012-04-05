/*
 * Copyright (c) 2005, Regents of the University of California
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

package blog.common;

import java.util.*;
import java.io.*;

/**
 * A SubstringReplacer is an engine for replacing substrings of a given string.
 * The user specifies an original string and a set of (beginIndex, endIndex,
 * replacement) triples; then the <code>getResult</code> method returns a
 * version of the original string where the specified substrings are replaced
 * with the specified replacements.
 */
public class SubstringReplacer {
	/**
	 * Creates a new SubstringReplacer. You must specify an original string using
	 * the <code>reset</code> method; if you call <code>addReplacement</code> or
	 * <code>getResult</code> before <code>reset</code>, an IllegalStateException
	 * will be thrown.
	 */
	public SubstringReplacer() {
		replOps = new ArrayList();
	}

	/**
	 * Creates a new SubstringReplacer with the given original string.
	 */
	public SubstringReplacer(String orig) {
		this();
		reset(orig);
	}

	/**
	 * Sets the original string to the <code>orig</code>, and clears the set of
	 * replacement operations.
	 */
	public void reset(String orig) {
		if (orig == null) {
			throw new IllegalArgumentException();
		}
		this.orig = orig;
		replOps.clear();
	}

	/**
	 * Adds an operation to replace the substring [begin, end) of the original
	 * string with the given replacement string. Throws an exception if the
	 * substring is empty, is not a valid substring of the original string, or
	 * overlaps with another substring that is to be replaced.
	 */
	public void addReplacement(int begin, int end, String repl) {
		if (orig == null) {
			throw new IllegalStateException("No original string specified");
		}
		if ((begin < 0) || (end > orig.length()) || (begin >= end)) {
			throw new IllegalArgumentException("Empty or invalid substring: " + "["
					+ begin + ", " + end + ")");
		}

		// Figure out where to insert the new ReplOp object. Start at the
		// end of replOps, which is an optimization for the case where
		// the replacement ops are being added in sorted order.
		int predIndex = replOps.size() - 1;
		while (predIndex >= 0) {
			ReplOp pred = (ReplOp) replOps.get(predIndex);
			if (pred.begin >= end) {
				predIndex--;
			} else if (pred.end > begin) {
				throw new IllegalArgumentException("Substring [" + begin + ", " + end
						+ ") overlaps " + "existing substring [" + pred.begin + ", "
						+ pred.end + ")");
			} else {
				break;
			}
		}
		replOps.add(predIndex + 1, new ReplOp(begin, end, repl));
	}

	/**
	 * Returns the string obtained by taking the original string specified in the
	 * last call to <code>reset</code>, and applying all the substring replacement
	 * operations specified using <code>addReplacement</code> since the last call
	 * to <code>reset</code>.
	 */
	public String getResult() {
		if (orig == null) {
			throw new IllegalStateException("No original string specified");
		}

		StringBuffer buf = new StringBuffer();
		int nextToWrite = 0;
		for (Iterator iter = replOps.iterator(); iter.hasNext();) {
			ReplOp op = (ReplOp) iter.next();
			buf.append(orig.substring(nextToWrite, op.begin));
			buf.append(op.repl);
			nextToWrite = op.end;
		}

		buf.append(orig.substring(nextToWrite));
		return buf.toString();
	}

	/**
	 * Data structure representing a replacement operation.
	 */
	class ReplOp {
		int begin; // index of first character to replace in original string
		int end; // 1 + index of last char to replace in original string
		String repl; // replacement string

		public ReplOp(int begin, int end, String repl) {
			this.begin = begin;
			this.end = end;
			this.repl = repl;
		}
	}

	String orig;
	List replOps; // of ReplOp objects, sorted by begin

	public static void main(String[] args) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		SubstringReplacer replacer = new SubstringReplacer();

		try {
			System.out.print("Orig string> ");
			String orig = reader.readLine();
			replacer.reset(orig);

			while (true) {
				System.out.print("Replacement op (blank if done)> ");
				String opStr = reader.readLine();
				if (opStr.length() == 0) {
					break;
				}

				StringTokenizer tokenizer = new StringTokenizer(opStr);
				int begin = Integer.parseInt(tokenizer.nextToken());
				int end = Integer.parseInt(tokenizer.nextToken());
				String repl = tokenizer.nextToken();
				replacer.addReplacement(begin, end, repl);
			}
		} catch (IOException e) {
			Util.fatalError(e);
		}

		String result = replacer.getResult();
		System.out.println(result);
	}
}
