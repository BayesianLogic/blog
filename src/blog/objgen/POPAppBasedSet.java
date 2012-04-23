/*
 * Copyright (c) 2006, Regents of the University of California
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

package blog.objgen;

import java.util.*;

import blog.bn.NumberVar;
import blog.common.HashMultiMap;
import blog.common.IndexedHashSet;
import blog.common.IndexedSet;
import blog.common.MultiMap;


/**
 * ObjectSet implementation that is composed of individual objects, plus all the
 * objects that satisfy certain potential object pattern (POP) applications
 * (number variables), with certain exceptional objects excluded.
 */
public class POPAppBasedSet extends AbstractObjectSet {

	/**
	 * Adds the given individual to this set, asserting that the individual
	 * satisfies the POP application governed by the given number variable (or no
	 * POP application if the number variable is null). This does nothing if the
	 * given POP application is already in this set and the given individual is
	 * not an exception. If the given individual is currently in the exception set
	 * for its POP application, it is removed from that exception set.
	 */
	public void addIndividual(Object indiv, NumberVar popApp) {
		if ((popApp == null) || (!popAppSatisfiers.containsKey(popApp))) {
			individuals.add(indiv);
			popAppIndivs.add(popApp, indiv);
			++size;
		} else {
			Set exceptions = (Set) popAppExceptions.get(popApp);
			if (exceptions.remove(indiv)) {
				++size;
			}
		}
	}

	/**
	 * Adds the given POP application satisfier set as a subset of this set, with
	 * the given exceptions. The exceptions set must be a subset of the given
	 * satisfier set. If the given POP application was already a subset of this
	 * set, then the new exceptions set is the intersection of the given
	 * exceptions set with the old one. Otherwise, the exceptions set is the given
	 * one minus any individual elements of this set that satisfy the given POP
	 * application.
	 */
	public void addSatisfiers(NumberVar popApp, ObjectSet satisfiers,
			Set newExceptions) {
		if (popAppSatisfiers.containsKey(popApp)) {
			// already in set; assume satisfiers the same
			Set curExceptions = (Set) popAppExceptions.get(popApp);
			int oldNumExceptions = curExceptions.size();
			curExceptions.retainAll(newExceptions);
			size += (oldNumExceptions - curExceptions.size());
		} else {
			popAppSatisfiers.put(popApp, satisfiers);
			Set oldIndivs = (Set) popAppIndivs.remove(popApp);
			for (Iterator iter = oldIndivs.iterator(); iter.hasNext();) {
				individuals.remove(iter.next());
			}

			Set curExceptions = new HashSet(newExceptions);
			curExceptions.removeAll(oldIndivs);
			popAppExceptions.put(popApp, curExceptions);

			size += (satisfiers.size() - oldIndivs.size() // because they were already
																										// here
			- curExceptions.size()); // because they weren't added
		}
	}

	protected Integer sizeInternal() {
		return new Integer(size);
	}

	protected Boolean containsInternal(Object o) {
		if (individuals.contains(o)) {
			return Boolean.TRUE;
		}

		for (Iterator iter = popAppSatisfiers.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			NumberVar popApp = (NumberVar) entry.getKey();
			ObjectSet satisfiers = (ObjectSet) entry.getValue();
			if (satisfiers.contains(o)) {
				Set exceptions = (Set) popAppExceptions.get(popApp);
				return Boolean.valueOf(!exceptions.contains(o));
			}
		}

		return Boolean.FALSE;
	}

	public ObjectIterator iterator(Set externallyDistinguished) {
		return new POPAppBasedSetIterator();
	}

	public ObjectSet getExplicitVersion() {
		return this;
	}

	public Object sample(int n) {
		if ((n < 0) || (n >= size)) {
			throw new IllegalArgumentException("Can't get element " + n
					+ " from set " + this + " of size " + size);
		}

		if (n < individuals.size()) {
			return individuals.get(n);
		}

		int indexSoFar = individuals.size();
		for (Iterator iter = popAppSatisfiers.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			NumberVar popApp = (NumberVar) entry.getKey();
			ObjectSet satisfiers = (ObjectSet) entry.getValue();
			Set exceptions = (Set) popAppExceptions.get(popApp);

			int numInSet = satisfiers.size() - exceptions.size();
			if (n < indexSoFar + numInSet) {
				return sampleFromPOPApp(satisfiers, exceptions, n - indexSoFar);
			}
			indexSoFar += numInSet;
		}

		return null; // shouldn't get here
	}

	public int indexOf(Object o) {
		int indivIndex = individuals.indexOf(o);
		if (indivIndex != -1) {
			return indivIndex;
		}

		int indexSoFar = individuals.size();
		for (Iterator iter = popAppSatisfiers.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			NumberVar popApp = (NumberVar) entry.getKey();
			ObjectSet satisfiers = (ObjectSet) entry.getValue();
			Set exceptions = (Set) popAppExceptions.get(popApp);

			if (satisfiers.contains(o) && !exceptions.contains(o)) {
				return (indexSoFar + getIndexInPOPApp(satisfiers, exceptions, o));
			}

			int numInSet = satisfiers.size() - exceptions.size();
			indexSoFar += numInSet;
		}

		return -1;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("POPAppBasedSet(");
		buf.append(individuals);

		for (Iterator iter = popAppSatisfiers.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			NumberVar popApp = (NumberVar) entry.getKey();
			ObjectSet satisfiers = (ObjectSet) entry.getValue();
			Set exceptions = (Set) popAppExceptions.get(popApp);

			buf.append(", ");
			buf.append(popApp);
			buf.append(" \\ ");
			buf.append(exceptions);
		}

		buf.append(")");
		return buf.toString();
	}

	private Object sampleFromPOPApp(ObjectSet satisfiers, Set exceptions, int n) {
		SortedSet exceptionIndices = new TreeSet();
		for (Iterator iter = exceptions.iterator(); iter.hasNext();) {
			Object exception = iter.next();
			exceptionIndices.add(new Integer(satisfiers.indexOf(exception)));
		}

		for (Iterator iter = exceptionIndices.iterator(); iter.hasNext();) {
			Integer exceptionIndex = (Integer) iter.next();
			if (exceptionIndex.intValue() <= n) {
				++n; // skip over this removed index
			} else {
				break;
			}
		}

		return satisfiers.sample(n);
	}

	// assumes o is in satisfiers \ exceptions
	private int getIndexInPOPApp(ObjectSet satisfiers, Set exceptions, Object o) {
		SortedSet exceptionIndices = new TreeSet();
		for (Iterator iter = exceptions.iterator(); iter.hasNext();) {
			Object exception = iter.next();
			exceptionIndices.add(new Integer(satisfiers.indexOf(exception)));
		}

		int origIndex = satisfiers.indexOf(o);
		int numExceptionsBefore = exceptionIndices.headSet(new Integer(origIndex))
				.size();
		return (origIndex - numExceptionsBefore);
	}

	private class POPAppBasedSetIterator extends AbstractObjectIterator {
		POPAppBasedSetIterator() {
			indivIter = individuals.iterator();
			mapIter = popAppSatisfiers.entrySet().iterator();
		}

		protected Object findNext() {
			if (indivIter.hasNext()) {
				return indivIter.next();
			}

			// While we have a current satisfier set or we can get one...
			while ((curSatisfiersIter != null) || mapIter.hasNext()) {
				while ((curSatisfiersIter != null) && curSatisfiersIter.hasNext()) {
					// continue through the satisfiers of the current POP app
					Object obj = curSatisfiersIter.next();
					if (!curExceptions.contains(obj)) {
						return obj;
					}
				}

				// If we get here, then we've run out of individual elements
				// and satisfiers of the current POP app
				curSatisfiersIter = null;
				if (mapIter.hasNext()) {
					Map.Entry entry = (Map.Entry) mapIter.next();
					NumberVar popApp = (NumberVar) entry.getKey();
					curSatisfiersIter = ((Set) entry.getValue()).iterator();
					curExceptions = (Set) popAppExceptions.get(popApp);
				}
			}

			return null;
		}

		private Iterator indivIter;
		private Iterator mapIter;
		private Iterator curSatisfiersIter = null;
		private Set curExceptions = null;
	}

	private IndexedSet individuals = new IndexedHashSet(); // of Object
	private MultiMap popAppIndivs = new HashMultiMap(); // from NumberVar to
																											// Objects
	private Map popAppSatisfiers = new LinkedHashMap(); // from NumberVar to
																											// ObjectSet
	private Map popAppExceptions = new HashMap(); // from NumberVar to Set
	private int size = 0;
}
