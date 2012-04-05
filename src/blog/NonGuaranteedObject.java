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

import java.lang.ref.*;
import java.util.*;

import blog.model.Model;
import blog.model.OriginFunction;
import blog.model.Type;

/*
 * Implementation of a BLOG non-guaranteed object. A BLOG
 * non-guaranteed object is uniquely specified by a POP and associated
 * generating objects that generated this object, and a number
 * that distinguishes it from the other NonGuaranteedObjects generated 
 * by the same POP application.
 *
 * <p>NonGuaranteedObjects cannot be constructed directly outside this 
 * class.  Instead, clients can use the static <code>get</code> methods to 
 * get NonGuaranteedObjects.   These methods ensure that there is only one 
 * Java object for each non-guaranteed object, so NonGuaranteedObjects 
 * can be compared with ==.  
 */
public class NonGuaranteedObject {
	/*
	 * @param pop The POP that generated this object.
	 * 
	 * @param genObjs The generating objects used with this POP.
	 * 
	 * @param num This object is the i'th generated from the above mentiond
	 * POP/generating objects in this world (using 1-based indexing).
	 */
	public static NonGuaranteedObject get(POP pop, Object[] genObjs, int num) {
		// Clean any cleared references out of the map
		while (true) {
			NGORef clearedRef = (NGORef) clearedRefs.poll();
			if (clearedRef == null) {
				break;
			}
			ensureNotDangling(clearedRef.key);
		}

		// Get the requested object
		NGOKey key = new NGOKey(pop, genObjs, num);
		Reference ref = (Reference) canonicalObjs.get(key);
		if ((ref == null) || (ref.get() == null)) {
			key.genObjs = (Object[]) genObjs.clone(); // just to be safe
			NonGuaranteedObject obj = new NonGuaranteedObject(key);
			canonicalObjs.put(key, new NGORef(key, obj));
			return obj;
		}
		return (NonGuaranteedObject) ref.get();
	}

	/*
	 * @param generatedFrom The NumberVar object that uniquely specifies the POP
	 * and generating objects that generated this object.
	 * 
	 * @param num This object is the i'th generated from the above mentioned
	 * POP/generating objects in this world (using 1-based indexing).
	 */
	public static NonGuaranteedObject get(NumberVar generatedFrom, int num) {
		return get(generatedFrom.pop(), generatedFrom.args(), num);
	}

	private static void ensureNotDangling(NGOKey key) {
		NGORef ref = (NGORef) canonicalObjs.get(key);
		if ((ref != null) && (ref.get() == null)) {
			canonicalObjs.remove(key);
		}
	}

	private NonGuaranteedObject(NGOKey key) {
		this.key = key;
	}

	public final int getNumber() {
		return key.num;
	}

	public final POP getPOP() {
		return key.pop;
	}

	public final Object[] getGenObjs() {
		return key.genObjs;
	}

	/**
	 * Returns the value of the given origin function on this object.
	 */
	public Object getOriginFuncValue(OriginFunction originFunc) {
		int index = key.pop.getOriginFuncIndex(originFunc);
		if (index != -1) {
			return key.genObjs[index];
		}

		// originFunc is not used in POP that generated this obj
		return Model.NULL;
	}

	/**
	 * Returns the BLOG type of this non-guaranteed object.
	 */
	public Type getType() {
		return key.pop.type();
	}

	/**
	 * Returns the number variable whose value determines whether this
	 * non-guaranteed object exists or not.
	 */
	public NumberVar getNumberVar() {
		// genObjs array is stable because it was cloned by the get method
		// before this NonGuaranteedObject was constructed
		return new NumberVar(key.pop, key.genObjs, true);
	}

	/**
	 * Returns the depth of nesting in this tuple. A guaranteed object or an
	 * object generated by the empty tuple has depth zero. The depth of any other
	 * object is one more than the maximum depth of its generating objects.
	 */
	public int getDepth() {
		if (key.genObjs.length == 0) {
			return 0;
		}

		int maxGenObjDepth = 0;
		for (int i = 0; i < key.genObjs.length; ++i) {
			if (key.genObjs[i] instanceof NonGuaranteedObject) {
				int d = ((NonGuaranteedObject) key.genObjs[i]).getDepth();
				maxGenObjDepth = Math.max(d, maxGenObjDepth);
			} // else genObj is a guaranteed object, depth 0
		}

		return maxGenObjDepth + 1;
	}

	/**
	 * Returns the tuple representation of this object.
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("(" + getType() + ", ");

		for (int i = 0; i < key.genObjs.length; ++i) {
			buf.append("(" + key.pop.originFuncs()[i] + ", " + key.genObjs[i] + "), ");
		}

		buf.append(key.num + ")");
		return buf.toString();
	}

	/**
	 * Compares this non-guaranteed object to another one. The objects are first
	 * compared by depth: shallower objects come first. Objects of the same depth
	 * are compared lexicographically according to their lists of generating
	 * objects. Objects with the same generating objects but different POPs are
	 * compared according to the order in which their POPs were defined. Finally,
	 * objects from the same POP application are compared by their indices.
	 */
	public int compareTo(Object o) {
		NonGuaranteedObject other = (NonGuaranteedObject) o;
		int depthDiff = getDepth() - other.getDepth();
		if (depthDiff != 0) {
			return depthDiff;
		}

		int genObjListDiff = Model
				.compareArgTuples(key.genObjs, other.getGenObjs());
		if (genObjListDiff != 0) {
			return genObjListDiff;
		}

		int popDiff = key.pop.getDepModel().getCreationIndex()
				- other.getPOP().getDepModel().getCreationIndex();
		if (popDiff != 0) {
			return popDiff;
		}

		return (key.num - other.getNumber());
	}

	private static class NGOKey {
		NGOKey(POP pop, Object[] genObjs, int num) {
			this.pop = pop;
			this.genObjs = genObjs;
			this.num = num;
		}

		public boolean equals(Object obj) {
			if (obj != null && obj instanceof NGOKey) {
				NGOKey other = (NGOKey) obj;

				return ((num == other.num) && (pop == other.pop) && (Arrays.equals(
						genObjs, other.genObjs)));
			}
			return false;
		}

		public int hashCode() {
			int code = num ^ pop.hashCode();
			for (int i = 0; i < genObjs.length; ++i) {
				code ^= genObjs[i].hashCode();
			}
			return code;
		}

		POP pop;
		Object[] genObjs;
		int num;
	}

	private static class NGORef extends WeakReference {
		NGORef(NGOKey key, NonGuaranteedObject obj) {
			super(obj, NonGuaranteedObject.clearedRefs);
			this.key = key;
		}

		NGOKey key;
	}

	/**
	 * Map from NGOKey to WeakReference to NonGuaranteedObject
	 */
	private static Map canonicalObjs = new HashMap();

	private static ReferenceQueue clearedRefs = new ReferenceQueue();

	private NGOKey key;
}
