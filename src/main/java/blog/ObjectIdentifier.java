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

package blog;

import blog.model.Type;

/**
 * An identifier for a non-guaranteed object in a PartialWorld. An
 * ObjectIdentifier specifies the type of the object it refers to; it must refer
 * to a non-guaranteed object, and distinct ObjectIdentifiers refer to distinct
 * BLOG objects. Other than that, an ObjectIdentifier provides no information.
 * 
 * <p>
 * Internally, ObjectIdentifiers are numbered in order of creation, and an
 * ObjectIdentifier's number is included in its string representation. This
 * makes debugging output reproducible across runs.
 */
public class ObjectIdentifier implements Comparable {
	/**
	 * Creates a new ObjectIdentifier for an object of the given type.
	 */
	public ObjectIdentifier(Type type) {
		this.type = type;
		num = new Integer(ObjectIdentifier.numCreated++);
	}

	public Type getType() {
		return type;
	}

	public String toString() {
		return (type + "#" + num);
	}

	public int compareTo(Object o) {
		return num.intValue() - ((ObjectIdentifier) o).getNum().intValue();
	}

	Integer getNum() {
		return num;
	}

	private Type type;
	private Integer num;

	private static int numCreated = 0;
}
