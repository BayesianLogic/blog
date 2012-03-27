/*
 * Copyright (c) 2007 Massachusetts Institute of Technology
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
 * * Neither the name of the Massachusetts Institute of Technology nor
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

import java.util.*;

/**
 * A mapping from logical variables to terms.
 */
public class Substitution {
	/**
	 * Creates a new "empty" substitution that does not change anything.
	 */
	public Substitution() {
	}

	/**
	 * Creates a new substitution that maps the given list of logical variables
	 * respectively to the given list of terms.
	 * 
	 * @throws IllegalArgumentException
	 *           if the two given lists have different lengths
	 */
	public Substitution(List<LogicalVar> vars, List<? extends Term> terms) {
		if (vars.size() != terms.size()) {
			throw new IllegalArgumentException("Can't map " + vars.size()
					+ " variables to " + terms.size() + " terms.");
		}

		for (int i = 0; i < vars.size(); ++i) {
			add(vars.get(i), terms.get(i));
		}
	}

	/**
	 * Adds the given mapping to this substitution, replacing any existing mapping
	 * for <code>var</code>.
	 */
	public void add(LogicalVar var, Term replacement) {
		map.put(var, replacement);
	}

	/**
	 * Removes the mapping (if any) for the given logical variable.
	 */
	public void remove(LogicalVar var) {
		map.remove(var);
	}

	/**
	 * Adds the given mapping to this substitution, and keeps the substitution in
	 * normal form (so that the rewrites can be applied in any order). Returns
	 * false if the substitution can't be converted to normal form. This function
	 * is doing a simple case of unification.
	 */
	public boolean makeEqual(Term t1, Term t2) {
		if (t1 == t2)
			return true;
		if (t1 instanceof LogicalVar) { // first term is a variable
			LogicalVar lt = (LogicalVar) t1;
			Term tr = getReplacement(lt);
			if (tr != lt) { // first term already has a substitution
				// this variable already appears, make it equal to t2
				if (t2 instanceof LogicalVar) {
					// both variables, first maps to something
					return makeEqual(t2, tr);
				} else {
					// second term a constant
					return makeEqual(tr, t2);
				}
			} else if (t2 instanceof LogicalVar) {
				// both terms are variables, first doesn't map to anything
				LogicalVar lt2 = (LogicalVar) t2;
				tr = getReplacement(lt2);
				if (tr != lt2) {
					// second maps to tr
					return makeEqual(t1, tr);
				} else {
					// both are variables, neither maps to anything
					if (map.values().contains(lt)) {
						// map lt2 to lt
						if (!replaceInRange(lt2, lt)) {
							add(lt2, lt);
						}
					} else if (map.values().contains(lt2)) {
						// map lt to lt2
						add(lt, lt2);
					} else {
						// make a new variable
						LogicalVar lnew = lt.makeNew();
						add(lt, lnew);
						add(lt2, lnew);
					}
					return true;
				}
			} else { // first term variable, second term constant
				// if variable is already in range somewhere, replace with
				// constant, else add new mapping...
				if (!replaceInRange(lt, t2)) {
					add(lt, t2);
				}
				return true;
			}
		} else if (t2 instanceof LogicalVar) {
			// constant, variable --> reverse them
			return makeEqual(t2, t1);
		} else
			return t1.equals(t2); // two constants
	}

	private boolean replaceInRange(LogicalVar l, Term t) {
		boolean replaced = false;
		Iterator<Map.Entry<LogicalVar, Term>> i = map.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry<LogicalVar, Term> me = i.next();
			if (me.getValue().equals(l)) {
				me.setValue(t);
				replaced = true;
			}
		}
		return replaced;
	}

	/**
	 * Returns the replacement for the given logical variable, or the logical
	 * variable itself if no replacement has been specified.
	 */
	public Term getReplacement(LogicalVar var) {
		Term replacement = map.get(var);
		if (replacement == null) {
			return var;
		}
		return replacement;
	}

	/**
	 * Returns the set of logical variables that map to the given term.
	 * 
	 * @param allowSelf
	 *          if true, the set returned may include <code>t</code> itself;
	 *          otherwise <code>t</code> is excluded
	 */
	public Set<LogicalVar> getPreimage(Term t, boolean allowSelf) {
		Set<LogicalVar> preimage = new HashSet<LogicalVar>();
		for (Map.Entry<LogicalVar, Term> entry : map.entrySet()) {
			if (entry.getValue().equals(t) && (allowSelf || (entry.getKey() != t))) {
				preimage.add(entry.getKey());
			}
		}
		if (allowSelf && (t instanceof LogicalVar) && !map.containsKey(t)) {
			preimage.add((LogicalVar) t);
		}
		return preimage;
	}

	/**
	 * Returns true if this substitution, restricted to the given variables, is
	 * one-to-one.
	 */
	public boolean isOneToOneOn(Collection<LogicalVar> vars) {
		Set<Term> range = new HashSet<Term>();
		for (LogicalVar var : vars) {
			range.add(getReplacement(var));
		}
		return (range.size() == vars.size());
	}

	public boolean equals(Object o) {
		if (o instanceof Substitution) {
			Substitution other = (Substitution) o;
			return map.equals(other.map);
		}
		return false;
	}

	public int hashCode() {
		return map.hashCode();
	}

	public int size() {
		return map.size();
	}

	public boolean hasConstant() {
		Iterator<Term> i = map.values().iterator();
		while (i.hasNext()) {
			if (!(i.next() instanceof LogicalVar)) {
				return true;
			}
		}
		return false;
	}

	public Set<LogicalVar> getVars() {
		return map.keySet();
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append('[');
		for (Iterator<Map.Entry<LogicalVar, Term>> iter = map.entrySet().iterator(); iter
				.hasNext();) {
			Map.Entry<LogicalVar, Term> entry = iter.next();
			buf.append(entry.getKey());
			buf.append('/');
			buf.append(entry.getValue());

			if (iter.hasNext()) {
				buf.append(", ");
			}
		}
		buf.append(']');
		return buf.toString();
	}

	protected Map<LogicalVar, Term> map = new HashMap<LogicalVar, Term>();
}
