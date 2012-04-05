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

package blog.common.cmdline;

import java.util.*;

/**
 * Option whose values are key-value pairs, represented as strings of the form
 * <i>key</i>=<i>val</i>. If this option occurs with a value that does not
 * contain an equals sign, then this class prints an error message and exits the
 * program. This option is allowed to occur multiple times on a command line.
 * The key-value pairs are accumulated in a Properties object.
 */
public class PropertiesOption extends AbstractOption {
	/**
	 * Creates a Properties option and registers it with the Parser class. By
	 * convention, a Properties option has a short form that is a capital letter,
	 * and no long form (but these conventions are not enforced).
	 * 
	 * @param shortForm
	 *          single-character form of the option, or null for an option with no
	 *          short form
	 * 
	 * @param longForm
	 *          long form of the option, or null for an option with no long form
	 * 
	 * @param defaults
	 *          Properties object containing defaults for the Properties object
	 *          that will be returned. If null, the object returned has no
	 *          defaults (i.e., it contains no keys by default).
	 * 
	 * @param docStr
	 *          short (preferably less than 40 characters) string specifying what
	 *          this option's key-value pairs influence
	 */
	public PropertiesOption(String shortForm, String longForm,
			Properties defaults, String docStr) {
		super(shortForm, longForm);

		if (defaults == null) {
			value = new Properties();
		} else {
			value = new Properties(defaults);
		}

		this.docStr = docStr;

		Parser.addOption(this);
	}

	public boolean expectsValue() {
		return true;
	}

	public void recordOccurrence(String form, String valueStr) {
		occurred = true;

		int equalsIndex = valueStr.indexOf("=");
		if (equalsIndex == -1) {
			System.err.println("Invalid value for \"" + form + "\" option: "
					+ valueStr);
			System.err.println("(should be of the form <key>=<value>).");
			System.exit(1);
		}

		String key = valueStr.substring(0, equalsIndex);
		String val = valueStr.substring(equalsIndex + 1);
		value.setProperty(key, val);
	}

	public String getUsageString() {
		StringBuffer buf = new StringBuffer();
		if (!shortForms.isEmpty()) {
			buf.append("-" + shortForms.get(0) + "<key>=<value>");
			if (!longForms.isEmpty()) {
				buf.append(", ");
			}
		}
		if (!longForms.isEmpty()) {
			buf.append("--" + longForms.get(0) + " <key>=<value>");
		}

		while (buf.length() < DOC_OFFSET) {
			buf.append(" ");
		}
		buf.append(docStr);

		return buf.toString();
	}

	/**
	 * Returns a Properties object containing the key-value pairs that were
	 * specified for this option on the command line. The default entries for this
	 * Properties object are given by the default Properties object passed to the
	 * constructor (if any).
	 */
	public Properties getValue() {
		return value;
	}

	private String docStr;

	private Properties value;
}
