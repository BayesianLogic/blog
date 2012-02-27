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
 * Option that takes Boolean values.  A Boolean option does not expect
 * any value on the command line, but it can have two long forms, one
 * indicating that the value is true and one indicating that it's
 * false.  If a Boolean option occurs more than once on a command
 * line, this class prints a warning and ignores all but the last
 * occurrence.
 */
public class BooleanOption extends AbstractOption {
    /**
     * Creates a Boolean option and registers it with the Parser class.  
     *
     * @param shortForm single-character flag indicating that the value is 
     *                  true, or null for an option with no short form
     * 
     * @param longForm long form indicating that the value is true, or null 
     *                 for an option with no long form.  If a long form 
     *                 <i>foo</i> is specified, then a second long form 
     *                 no<i>foo</i> is also defined to indicate that the 
     *                 option value is false.  
     *
     * @param def      default value to return if the option does not occur
     *
     * @param docStr   short (preferably less than 40 characters) 
     *                 string specifying what happens when this option 
     *                 is true
     */
    public BooleanOption(String shortForm, String longForm, boolean def, 
			 String docStr) {
	super(shortForm, longForm);

	if (longForm != null) {
	    longForms.add("no" + longForm);
	}

	value = def;
	this.docStr = docStr;

	Parser.addOption(this);
    }

    public boolean expectsValue() {
	return false;
    }

    public void recordOccurrence(String form, String valueStr) {
	super.recordOccurrence(form, valueStr);

	if (form.startsWith("no")) {
	    value = false;
	} else {
	    value = true;
	}
    }

    public String getUsageString() {
	StringBuffer buf = new StringBuffer();
	if (!shortForms.isEmpty()) {
	    buf.append("-" + shortForms.get(0));
	    if (!longForms.isEmpty()) {
		buf.append(", ");
	    }
	}
	if (!longForms.isEmpty()) {
	    buf.append("--[no]" + longForms.get(0));
	}
	
	while (buf.length() < DOC_OFFSET) {
	    buf.append(" ");
	}
	buf.append(docStr);

	return buf.toString();
    }

    /**
     * Returns the value specified on the command line for this
     * option, or the default value if the option did not occur.
     */
    public boolean getValue() {
	return value;
    }

    private String docStr;

    private boolean value;
}
