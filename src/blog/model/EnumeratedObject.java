/*
 * Copyright (c) 2005, 2006, 2012 Regents of the University of California
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

package blog.model;


/**
 * An EnumeratedObject represents a guaranteed object that is explicitly defined
 * and given a name in the model file, using a statement like: <blockquote>
 * distincted <i>typename</i> <i>name1</i>, <i>name2</i>, ..., <i>nameN</i>
 * </blockquote> A "name" is really a nonrandom constant symbol that refers to
 * this object. Other constant symbols may also refer to this object, but in
 * presenting query results to the user, we may want to use the name that the
 * modeler used to introduce this object in the model file. Each
 * EnumeratedObject of a given type also has an index, based on the order in
 * which the objects were introduced in the model file.
 * 
 * @author leili
 * @date 2012/12/17
 */
public class EnumeratedObject implements Comparable {
	/**
	 * Creates a new EnumeratedObject of the given type that was introduced with
	 * the given name.
	 */
	public EnumeratedObject(Type type, int index, NonRandomFunction constant) {
		this.type = type;
		this.index = index;
		this.constant = constant;
	}

	/**
	 * Returns the non-random constant that canonically stands for this object.
	 */
	public NonRandomFunction getConstant() {
		return constant;
	}

	/**
	 * Returns the name used to introduce this object.
	 */
	public String getName() {
		return constant.getName();
	}

	/**
	 * Returns the index of this object, that is, the number i such that i
	 * enumerated objects of this type were introduced before this object in the
	 * model file.
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Returns this object's type.
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Returns this object's name.
	 */
	public String toString() {
		return constant.getName();
	}

	public int hashCode() {
		return type.hashCode() ^ (constant.hashCode() ^ index);
	}

	/**
	 * Compares this enumerated object to another one of the same type, based on
	 * their indices. Enumerated objects of different types are incomparable.
	 */
	public int compareTo(Object o) {
		EnumeratedObject other = (EnumeratedObject) o;
		if (type != other.getType()) {
			return 0;
		}
		return (index - other.getIndex());
	}

	private Type type;
	private int index;
	private NonRandomFunction constant;
}
