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
 * Abstract implementation of the Option interface that takes care of some
 * bookkeeping and error checking.
 */
public abstract class AbstractOption implements Option {
	/**
	 * Creates an option.
	 * 
	 * @param shortForm
	 *          single-character form of the option, or null if the option has no
	 *          single-character form
	 * 
	 * @param longForm
	 *          long form of the option, or null if the option has no long form
	 * 
	 * @throws IllegalArgumentException
	 *           if <code>shortForm</code> is a string of length other than 1
	 * 
	 * @throws IllegalArgumentException
	 *           if <code>longForm</code> is an empty string
	 * 
	 * @throws IllegalArgumentException
	 *           if both <code>shortForm</code> and <code>longForm</code> are null
	 */
	public AbstractOption(String shortForm, String longForm) {
		if (shortForm != null) {
			if (shortForm.length() != 1) {
				throw new IllegalArgumentException("Invalid short form \"" + shortForm
						+ "\".  " + "Must be single character.");
			}
			shortForms.add(new Character(shortForm.charAt(0)));
		}

		if (longForm != null) {
			if (longForm.length() == 0) {
				throw new IllegalArgumentException(
						"Long form of option cannot be empty string.");
			}
			longForms.add(longForm);
		}

		if (shortForms.isEmpty() && longForms.isEmpty()) {
			throw new IllegalArgumentException(
					"Can't create option with no long or short forms.");
		}
	}

	public List getShortForms() {
		return shortForms;
	}

	public List getLongForms() {
		return longForms;
	}

	/**
	 * This default implementation prints a warning if this option has already
	 * been occurred. It also sets the <code>occurred</code> member variable to
	 * true. However, it does not parse or record the value.
	 */
	public void recordOccurrence(String form, String valueStr) {
		if (occurred) {
			System.err.println("Warning: repeated occurrence of " + this
					+ " option.  Ignoring earlier occurrences.");
		}
		occurred = true;
	}

	/**
	 * Returns true if any form of this option occurred on the command line.
	 */
	public boolean wasPresent() {
		return occurred;
	}

	public String toString() {
		if (longForms.isEmpty()) {
			return ("-" + shortForms.get(0).toString());
		}
		return ("--" + (String) longForms.get(0));
	}

	/**
	 * Offset for documentation strings in usage strings. Implementations of
	 * <code>getUsageString</code> should pad their return values with spaces so
	 * that the documentation string begins at this offset.
	 */
	protected static final int DOC_OFFSET = 30;

	/**
	 * List of Character objects that are short forms of this option.
	 */
	protected List shortForms = new ArrayList();

	/**
	 * List of String objects that are long forms of this option.
	 */
	protected List longForms = new ArrayList();

	/**
	 * True if recordOccurrence has already been called on this option.
	 */
	protected boolean occurred = false;
}
