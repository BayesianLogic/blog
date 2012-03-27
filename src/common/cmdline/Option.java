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
 * Interface for command line options. This interface is solely for interaction
 * with the command line parser. The application will also need to create Option
 * objects, and query them to find out whether they occurred and (if so) with
 * what values. However, each Option implementation defines its own methods for
 * those purposes.
 * 
 * <p>
 * All Option implementations should called <code>Parser.add(this)</code> in
 * their constructors, once they have fully set up their lists of long and short
 * forms.
 */
public interface Option {
	/**
	 * Returns the short (single-character) forms of this option.
	 * 
	 * @return List of Character objects
	 */
	List getShortForms();

	/**
	 * Returns the long forms of this option.
	 * 
	 * @return List of String objects
	 */
	List getLongForms();

	/**
	 * Returns true if this option expects a value on the command line.
	 */
	boolean expectsValue();

	/**
	 * Records an occurrence of this option with the given value. The
	 * <code>form</code> argument specifies the particular long or short form of
	 * the option that was used. If the value is invalid, or if the option has
	 * already occurred and a second occurrence is not allowed, this method may
	 * print an error message and exit the program.
	 * 
	 * @param form
	 *          specifies the particular long or short form of the option that was
	 *          used
	 * 
	 * @param valueStr
	 *          the given value, or null if no value was given. The parser will
	 *          only pass null for this parameter if <code>expectsValue</code>
	 *          returns false; otherwise, if the value is missing, the parser will
	 *          not call this method.
	 */
	void recordOccurrence(String form, String valueStr);

	/**
	 * Returns a string that documents the option. This string should fit on one
	 * 80-column line.
	 */
	String getUsageString();
}
