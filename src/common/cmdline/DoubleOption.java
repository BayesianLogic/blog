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

package common.cmdline;

import java.util.*;

/**
 * Option that takes real values. If this option occurs with a value that is not
 * a valid real number, this class prints an error message and exits the
 * program. If the option occurs more than once on a command line, this class
 * prints a warning and ignores all but the last occurrence.
 */
public class DoubleOption extends AbstractOption {
	/**
	 * Creates a real-valued option and registers it with the Parser class.
	 * 
	 * @param shortForm
	 *          single-character form of this option, or null for an option with
	 *          no short form
	 * 
	 * @param longForm
	 *          long form of this option, or null for an option with no long form.
	 * 
	 * @param def
	 *          default value to return if the option does not occur
	 * 
	 * @param docStr
	 *          short (preferably less than 40 characters) string specifying what
	 *          happens when this option's value is "&lt;x&gt;"
	 */
	public DoubleOption(String shortForm, String longForm, double def,
			String docStr) {
		super(shortForm, longForm);

		value = def;
		this.docStr = docStr;

		Parser.addOption(this);
	}

	public boolean expectsValue() {
		return true;
	}

	public void recordOccurrence(String form, String valueStr) {
		super.recordOccurrence(form, valueStr);

		try {
			value = Double.parseDouble(valueStr);
		} catch (NumberFormatException e) {
			System.err.println("Invalid value for \"" + form + "\" option: "
					+ valueStr);
			System.err.println("(should be a real number).");
			System.exit(1);
		}
	}

	public String getUsageString() {
		StringBuffer buf = new StringBuffer();
		if (!shortForms.isEmpty()) {
			buf.append("-" + shortForms.get(0) + " <x>");
			if (!longForms.isEmpty()) {
				buf.append(", ");
			}
		}
		if (!longForms.isEmpty()) {
			buf.append("--" + longForms.get(0) + " <x>");
		}

		while (buf.length() < DOC_OFFSET) {
			buf.append(" ");
		}
		buf.append(docStr);

		return buf.toString();
	}

	/**
	 * Returns the value specified on the command line for this option, or the
	 * default value if the option did not occur.
	 */
	public double getValue() {
		return value;
	}

	private String docStr;

	private double value;
}
