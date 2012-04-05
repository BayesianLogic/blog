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

package fove;

import java.util.*;
import java.io.PrintStream;

import blog.*;
import blog.common.Util;

public class Constraint {

	public static final Constraint EMPTY = new Constraint(
			(Collection<LogicalVar>) Collections.EMPTY_SET);

	/**
	 * Makes a new constraint that allows all substitutions to the given logical
	 * variables.
	 */
	public Constraint(Collection<? extends LogicalVar> lvs) {
		excluded = new HashMap<LogicalVar, Set<Term>>();
		for (LogicalVar l : lvs) {
			excluded.put(l, new HashSet<Term>());
		}
		initCacheVars();
	}

	public Constraint(Formula form, Collection<? extends LogicalVar> lvs) {
		excluded = new HashMap<LogicalVar, Set<Term>>();
		for (LogicalVar l : lvs) {
			excluded.put(l, new LinkedHashSet<Term>());
		}

		if (form instanceof ConjFormula) {
			addConstraintsFromFormula((ConjFormula) form);
		} else if (form instanceof NegFormula) {
			addConstraintFromNegation((NegFormula) form);
		} else if (!(form instanceof TrueFormula)) {
			throw new IllegalArgumentException(form.getLocation() + ": " + form
					+ " is not a well-formed constraint.");
		}
		initCacheVars();
	}

	public Constraint(Constraint other) {
		this.excluded = new HashMap<LogicalVar, Set<Term>>();
		Iterator<Map.Entry<LogicalVar, Set<Term>>> i = other.excluded.entrySet()
				.iterator();
		while (i.hasNext()) {
			Map.Entry<LogicalVar, Set<Term>> me = i.next();
			this.excluded.put(me.getKey(), new LinkedHashSet<Term>(me.getValue()));
		}
		initCacheVars();
	}

	public Constraint(Constraint one, Constraint two) {
		excluded = new HashMap<LogicalVar, Set<Term>>();
		Iterator<Map.Entry<LogicalVar, Set<Term>>> i = one.excluded.entrySet()
				.iterator();
		while (i.hasNext()) {
			Map.Entry<LogicalVar, Set<Term>> me = i.next();
			excluded.put(me.getKey(), new LinkedHashSet<Term>(me.getValue()));
		}

		i = two.excluded.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry<LogicalVar, Set<Term>> me = i.next();
			if (excluded.containsKey(me.getKey())) {
				excluded.get(me.getKey()).addAll(me.getValue());
			} else {
				excluded.put(me.getKey(), new LinkedHashSet<Term>(me.getValue()));
			}
		}
		initCacheVars();
	}

	public Constraint(Map<LogicalVar, Set<Term>> excluded) {
		this.excluded = excluded;
		initCacheVars();
	}

	/**
	 * Makes a new Constraint that is equal to other with the added l!=t
	 * inequality;
	 */
	public Constraint(Constraint other, LogicalVar l, Term t) {
		this.excluded = new HashMap<LogicalVar, Set<Term>>();
		Iterator<Map.Entry<LogicalVar, Set<Term>>> i = other.excluded.entrySet()
				.iterator();
		while (i.hasNext()) {
			Map.Entry<LogicalVar, Set<Term>> me = i.next();
			this.excluded.put(me.getKey(), new LinkedHashSet<Term>(me.getValue()));
		}
		excluded.get(l).add(t);
		if (t instanceof LogicalVar) {
			Set<Term> tExcluded = excluded.get((LogicalVar) t);
			if (tExcluded != null) {
				tExcluded.add(l);
			}
		}
		initCacheVars();
	}

	// cahces some computations on the Constraint so that they don't have to
	// be repeated. always call at the bottom of every constructor. these
	// values should never change because Constraints are immutable
	private void initCacheVars() {
		excludedConstants = new HashMap<LogicalVar, Set<Term>>();

		Iterator<Map.Entry<LogicalVar, Set<Term>>> i = excluded.entrySet()
				.iterator();
		while (i.hasNext()) {
			Map.Entry<LogicalVar, Set<Term>> entry = i.next();
			Iterator<Term> j = entry.getValue().iterator();
			Set<Term> constants = new LinkedHashSet<Term>();
			while (j.hasNext()) {
				Term t = j.next();
				if (t instanceof LogicalVar) {
					hasVarInEq = true;
				} else {
					constants.add(t);
				}
			}
			excludedConstants.put(entry.getKey(), constants);
		}

		firstNormalFormViolation = findNormalFormViolation();
	}

	public Constraint getSubstResult(Substitution theta) {
		Set<LogicalVar> boundVars = Collections.emptySet();
		return getSubstResult(theta, boundVars);
	}

	public Constraint getSubstResult(Substitution theta, Set<LogicalVar> boundVars) {
		// make a copy of the excluded sets
		Map<LogicalVar, Set<Term>> newExcluded = new HashMap<LogicalVar, Set<Term>>();
		Iterator<LogicalVar> i = excluded.keySet().iterator();
		while (i.hasNext()) {
			LogicalVar x = i.next();
			newExcluded.put(x, new LinkedHashSet<Term>(excluded.get(x)));
		}

		// first, handle variables that are being removed
		// we will apply the substition to the remaining excluded sets later
		for (LogicalVar x : excluded.keySet()) {
			Term tnew = theta.getReplacement(x);
			if ((!boundVars.contains(x)) && (tnew != x)) {
				if (tnew instanceof LogicalVar) {
					// we have a variable that is begin replaced
					if (excluded.get(tnew) == null) {
						// handle the substitution X/Y where Y is a new variable
						newExcluded.put((LogicalVar) tnew, newExcluded.get(x));
						newExcluded.remove(x);
					} else {
						// handle the substitution X/Y where Y is already present
						newExcluded.remove(x);
						Set<Term> vals = excluded.get(x);
						Set<Term> newVals = newExcluded.get(tnew);
						if (newVals != null) {
							newVals.addAll(vals);
							newVals.remove(x);
						}
					}
				} else {
					// x is a variable mapping to a constant, drop the inequalities
					newExcluded.remove(x);
				}
			}

		}

		// now, apply subsitution to excluded sets that are left
		for (Map.Entry<LogicalVar, Set<Term>> me : newExcluded.entrySet()) {
			LogicalVar x = me.getKey();
			// make a new set of Terms to hold the result of the subsitution
			Set<Term> newVals = new LinkedHashSet(me.getValue());
			for (ArgSpec t : me.getValue()) {
				if (t instanceof LogicalVar && !boundVars.contains((LogicalVar) t)) {
					// we have a variable
					Term t1 = theta.getReplacement((LogicalVar) t);
					if (t1 != t) {
						// the variable has a new value
						newVals.remove(t);
						newVals.add(t1.getCanonicalVersion());
					}
				}
			}
			// update the new set
			me.setValue(newVals);
		}

		return new Constraint(newExcluded);
	}

	/**
	 * Returns a version of this constraint where any redundant logical variable
	 * inequalities have been removed. An inequality x != y is redundant if the
	 * sets of allowed constants for x and y are disjoint. Removing such
	 * inequalities is sometimes necessary to get a constraint that is in normal
	 * form with respect to the constraint on its free variables.
	 */
	public Constraint getSimplified(Constraint freeVarsConstraint) {
		Map<LogicalVar, Set<Term>> newExcluded = new HashMap<LogicalVar, Set<Term>>();
		for (Map.Entry<LogicalVar, Set<Term>> entry : excluded.entrySet()) {
			newExcluded
					.put(entry.getKey(), new LinkedHashSet<Term>(entry.getValue()));
		}

		for (Map.Entry<LogicalVar, Set<Term>> entry : excluded.entrySet()) {
			LogicalVar var = entry.getKey();
			Set<? extends Term> varExcluded = entry.getValue();
			Set<? extends Term> newVarExcluded = newExcluded.get(var);
			for (ArgSpec term : varExcluded) {
				if ((term instanceof LogicalVar) && newVarExcluded.contains(term)) {
					LogicalVar otherVar = (LogicalVar) term;
					Set<? extends Term> otherExcluded = excluded.get(otherVar);
					if (otherExcluded == null) {
						otherExcluded = freeVarsConstraint.excluded(otherVar);
					}

					if (!allowsSomeAllowedBy(var, otherExcluded)) {
						// inequality is redundant
						newVarExcluded.remove(otherVar);
						Set<Term> newOtherExcluded = newExcluded.get(otherVar);
						if (newOtherExcluded != null) {
							newOtherExcluded.remove(var);
						}
					}
				}
			}
		}

		return new Constraint(newExcluded);
	}

	/**
	 * Returns a constraint on <code>subVars</code> allowing just those groundings
	 * of <code>subVars</code> that can be obtained by taking a grounding of this
	 * constraint and restricting it to <code>subVars</code>.
	 * 
	 * TODO: make this work right with free variables.
	 */
	public Constraint getProjection(Collection<? extends LogicalVar> subVars) {
		Map<LogicalVar, Set<Term>> newExcluded = new HashMap<LogicalVar, Set<Term>>();
		for (LogicalVar x : subVars) {
			Set<Term> oldEx = excluded.get(x);
			Set<Term> newEx = ((oldEx == null) ? new LinkedHashSet<Term>()
					: new LinkedHashSet<Term>(oldEx));
			for (Iterator<Term> iter = newEx.iterator(); iter.hasNext();) {
				Term term = iter.next();
				if ((term instanceof LogicalVar) && !subVars.contains(term)) {
					iter.remove();
				}
			}
			newExcluded.put(x, newEx);
		}

		return new Constraint(newExcluded);
	}

	public boolean consistent(Substitution theta) {
		for (Map.Entry<LogicalVar, Set<Term>> me : excluded.entrySet()) {
			LogicalVar x = me.getKey();
			Term xr = theta.getReplacement(x);
			for (ArgSpec t : me.getValue()) {
				if (t.equals(xr))
					return false;
				if (t instanceof LogicalVar
						&& theta.getReplacement((LogicalVar) t).equals(xr))
					return false;
			}
		}
		return true;
	}

	public Parfactor split(Parfactor p, Collection<Parfactor> residuals) {
		for (Map.Entry<LogicalVar, Set<Term>> me : excluded.entrySet()) {
			LogicalVar x = me.getKey();
			for (Term t : me.getValue()) {
				// System.out.println("r**"+x+" : "+t);
				p = p.splitOn(x, t, residuals);
			}
		}
		return p;
	}

	/**
	 * Returns the number of allowed groundings of logical variable x for any
	 * given grounding of the other logical variables mentioned in this
	 * constraint. NOTE: Assumes that this constraint is jointly in normal form
	 * with the constraints on all the variables it mentions.
	 */
	public int numConstrainedGroundings(LogicalVar x) {
		return x.getType().getGuaranteedObjects().size() - excluded.get(x).size();
	}

	/**
	 * Returns the number of allowed joint groundings of the given collection of
	 * logical variables, given any grounding of the remaining logical variables
	 * mentioned in this constraint. Assumes that the constraint is already in
	 * normal form.
	 */
	public int numConstrainedGroundings(Collection<? extends LogicalVar> vars) {
		int numGroundings = 1;
		Set<LogicalVar> varsToBind = new HashSet<LogicalVar>(vars);

		for (LogicalVar x : vars) {
			// Figure out the number of possible values for x, given
			// any allowed grounding of the earlier variables.

			if (!varsToBind.contains(x)) {
				continue; // repeated variable
			}

			int numValues = x.getType().getGuaranteedObjects().size();
			Set<? extends Term> excl = excluded.get(x);
			if (excl != null) {
				// Count logical variables in excl that still remain to
				// be bound. They don't actually rule out values for x.
				int numExVarsStillToBind = 0;
				for (ArgSpec t : excl) {
					if ((t instanceof LogicalVar) && varsToBind.contains(t)) {
						++numExVarsStillToBind;
					}
				}
				numValues -= (excl.size() - numExVarsStillToBind);
			}

			numGroundings *= numValues;
			varsToBind.remove(x);
		}

		return numGroundings;
	}

	/**
	 * Returns true if a contradiction is found in this constraint. One kind of
	 * contradiction is when the constraint asserts x != x for some logical
	 * variable x. Another is when the number of "obviously excluded" values for
	 * some logical variable is greater than or equal to the number of guaranteed
	 * objects of that variable's type. The number of obviously excluded values
	 * for a logical variable is the number of excluded constants, plus, if this
	 * constraint is in normal form, the number of excluded non-free logical
	 * variables.
	 * 
	 * <p>
	 * Note that if the constraint is not in normal form, we cannot include
	 * logical variables in the number of obviously excluded values because the
	 * logical variables could take on the same values as each other, or as the
	 * excluded constants.
	 */
	public boolean hasContradiction() {
		boolean normal = isNormalForm();

		for (Map.Entry<LogicalVar, Set<Term>> entry : excluded.entrySet()) {
			LogicalVar x = entry.getKey();
			Set<Term> xExcluded = entry.getValue();

			if (xExcluded.contains(x)) {
				return true;
			}

			int numObviouslyEx = 0;
			for (ArgSpec term : xExcluded) {
				if (term instanceof LogicalVar) {
					if (normal && excluded.containsKey(term)) {
						++numObviouslyEx;
					}
				} else {
					++numObviouslyEx;
				}
			}

			if (numObviouslyEx >= x.getType().getGuaranteedObjects().size()) {
				return true;
			}
		}

		return false;
	}

	public boolean isNormalForm() {
		return firstNormalFormViolation == null;
	}

	/**
	 * If this constraint is not in normal form, returns an array of two terms
	 * specifying a split that needs to be done to obtain normal-form constraints.
	 * Otherwise returns null.
	 * 
	 * <p>
	 * The first element of the returned array is a LogicalVar y, and the second
	 * element is a term t. The fact that this array is returned indicates that
	 * there is some other logical variable x such that this constraint asserts x
	 * != y and x != t, but does not assert y != t.
	 */
	public Term[] getNormalFormViolation() {
		return firstNormalFormViolation;
	}

	private Term[] findNormalFormViolation() {
		for (Map.Entry<LogicalVar, Set<Term>> entry : excluded.entrySet()) {
			LogicalVar x = entry.getKey();
			Set<Term> xExcluded = entry.getValue();

			for (ArgSpec term : xExcluded) {
				if (!(term instanceof LogicalVar)) {
					continue;
				}

				LogicalVar y = (LogicalVar) term;
				Set<Term> yExcluded = excluded.get(y);
				if (yExcluded == null) {
					continue;
				}

				for (Term otherTerm : xExcluded) {
					if ((otherTerm != y) && !yExcluded.contains(otherTerm)) {
						Term[] violation = new Term[2];
						violation[0] = y;
						violation[1] = otherTerm;
						return violation;
					}
				}
			}
		}

		return null;
	}

	public String findNormalFormError(Constraint freeVarsConstraint) {
		for (Map.Entry<LogicalVar, Set<Term>> entry : excluded.entrySet()) {
			LogicalVar x = entry.getKey();
			Set<? extends Term> xExcluded = entry.getValue();
			for (ArgSpec term : xExcluded) {
				if (term instanceof LogicalVar) {
					LogicalVar y = (LogicalVar) term;
					Set<? extends Term> yExcluded = excluded.get(y);
					if (yExcluded == null) {
						yExcluded = freeVarsConstraint.excluded(y);
					}

					for (ArgSpec toExclude : xExcluded) {
						if ((toExclude != y) && !yExcluded.contains(toExclude)) {
							return ("variable " + x + " excludes variable " + y
									+ ", which may take on the same "
									+ "value as another term in " + x + "'s excluded set");
						}
					}
				}
			}
		}

		return null;
	}

	public boolean selfExclusiveType(Type T) {
		for (LogicalVar X : excluded.keySet())
			if (X.getType().equals(T))
				for (ArgSpec v : excluded.get(X))
					if (v instanceof LogicalVar)
						return true;
		return false;
	}

	public Set<? extends Term> excluded(LogicalVar x) {
		return Collections.unmodifiableSet(excluded.get(x));
	}

	public Set<? extends Term> excludedConstants(LogicalVar x) {
		return Collections.unmodifiableSet(excludedConstants.get(x));
	}

	public Set<? extends Term> excludedInProj(LogicalVar x,
			Set<? extends Term> projVars) {
		Set<Term> result = new LinkedHashSet<Term>();
		Set<Term> exc = excluded.get(x);
		if (exc == null)
			return result;
		for (Term term : exc) {
			if ((!(term instanceof LogicalVar)) || projVars.contains(term)) {
				result.add(term);
			}
		}
		return result;
	}

	/**
	 * Returns the number of canonical constant symbols that could be substituted
	 * for <code>x</code> without making this constraint inconsistent.
	 */
	public int numAllowedConstants(LogicalVar x) {
		return (x.getType().getGuaranteedObjects().size() - excludedConstants
				.get(x).size());
	}

	/**
	 * Returns the set of canonical constant symbols that could be substituted for
	 * <code>x</code> without making this constraint inconsistent.
	 */
	public Set<Term> allowedConstants(LogicalVar x) {
		Set<Object> excludedVals = new HashSet<Object>();
		for (ArgSpec c : excludedConstants.get(x)) {
			excludedVals.add(c.getValueIfNonRandom());
		}

		Set<Term> allowed = new LinkedHashSet<Term>();
		for (Object obj : x.getType().getGuaranteedObjects()) {
			if (!excludedVals.contains(obj)) {
				allowed.add(x.getType().getCanonicalTerm(obj));
			}
		}
		return allowed;
	}

	/**
	 * Returns true if there is a non-empty overlap between the values that this
	 * constraint allows for <code>x</code> and the values allowed by the given
	 * excluded set. In other words, returns true if there is some object whose
	 * canonical term is neither excluded by this constraint nor excluded by the
	 * given set.
	 */
	public boolean allowsSomeAllowedBy(LogicalVar x,
			Set<? extends Term> otherExcluded) {
		Set<Term> xExcluded = excluded.get(x);
		Type type = x.getType();
		for (Object obj : type.getGuaranteedObjects()) {
			Term objTerm = type.getCanonicalTerm(obj);
			if (!xExcluded.contains(objTerm) && !otherExcluded.contains(objTerm)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if the values that this constraint allows for <code>x</code>
	 * are a subset of those allowed by the given excluded set. In other words,
	 * returns true if this constraint excludes all terms that are excluded by
	 * <code>otherExcluded</code> (ignoring logical variables that are not covered
	 * by this constraint).
	 */
	public boolean allowsOnlyAllowedBy(LogicalVar x,
			Set<? extends Term> otherExcluded) {
		Set<Term> xExcluded = excluded.get(x);
		for (ArgSpec term : otherExcluded) {
			if (((!(term instanceof LogicalVar)) || excluded.containsKey(term))
					&& !xExcluded.contains(term)) {
				return false;
			}
		}
		return true;
	}

	public boolean hasVarInEq() {
		return hasVarInEq;
	}

	public Collection<LogicalVar> logicalVars() {
		return Collections.unmodifiableSet(excluded.keySet());
	}

	public int numLogicalVars() {
		return excluded.size();
	}

	public boolean isSatisfied(EvalContext context) {
		for (Map.Entry<LogicalVar, Set<Term>> entry : excluded.entrySet()) {
			LogicalVar var = entry.getKey();
			Object val = context.getLogicalVarValue(var);
			if (val == null) {
				throw new IllegalArgumentException(
						"Context does not assign value to logical variable " + var);
			}

			for (ArgSpec t : entry.getValue()) {
				if (val.equals(t.evaluate(context))) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * The free variables are all the logical variables that occur in this
	 * constraint, whether as a key or in an excluded set.
	 */
	public Set getFreeVars() {
		Set freeVars = new LinkedHashSet();
		for (Map.Entry<LogicalVar, Set<Term>> entry : excluded.entrySet()) {
			freeVars.add(entry.getKey());
			for (ArgSpec t : entry.getValue()) {
				if (t instanceof LogicalVar) {
					freeVars.add(t);
				}
			}
		}
		return freeVars;
	}

	/**
	 * Returns a new constraint where the given logical variable is not included.
	 */
	public Constraint dropVar(LogicalVar x) {
		Map<LogicalVar, Set<Term>> newExcluded = new HashMap<LogicalVar, Set<Term>>();
		for (Map.Entry<LogicalVar, Set<Term>> entry : excluded.entrySet()) {
			if (entry.getKey() != x) {
				Set<Term> newVals = new LinkedHashSet<Term>(entry.getValue());
				newVals.remove(x);
				newExcluded.put(entry.getKey(), newVals);
			}
		}
		return new Constraint(newExcluded);
	}

	/**
	 * Returns a new constraint where the given logical variable is the only one
	 * that is included.
	 */
	public Constraint keepVar(LogicalVar x) {
		Map<LogicalVar, Set<Term>> newExcluded = new HashMap<LogicalVar, Set<Term>>();
		for (Map.Entry<LogicalVar, Set<Term>> entry : excluded.entrySet()) {
			if (entry.getKey() == x) {
				Set<Term> newVals = new LinkedHashSet<Term>(entry.getValue());
				newExcluded.put(entry.getKey(), entry.getValue());
			}
		}
		return new Constraint(newExcluded);
	}

	/**
	 * Returns a new constraint that is the same as this one, with the additional
	 * assertion that <code>x != t</code>.
	 */
	public Constraint addConstraint(LogicalVar x, Term t) {
		Map<LogicalVar, Set<Term>> newExcluded = new HashMap<LogicalVar, Set<Term>>();
		for (Map.Entry<LogicalVar, Set<Term>> entry : excluded.entrySet()) {
			newExcluded
					.put(entry.getKey(), new LinkedHashSet<Term>(entry.getValue()));
		}
		newExcluded.get(x).add(t);

		if (t instanceof LogicalVar) {
			Set<Term> tExcluded = newExcluded.get((LogicalVar) t);
			if (tExcluded != null) {
				tExcluded.add(x);
			}
		}

		return new Constraint(newExcluded);
	}

	/**
	 * Returns a new constraint that is the same as this one, with the excluded
	 * set for <code>x</code> replaced with the given set.
	 */
	public Constraint replaceExcluded(LogicalVar x, Set<? extends Term> newEx) {
		Map<LogicalVar, Set<Term>> newExcluded = new HashMap<LogicalVar, Set<Term>>(
				excluded);
		newExcluded.put(x, new LinkedHashSet<Term>(newEx));
		return new Constraint(newExcluded);
	}

	/*
	 * Modifies the given map from types to partitions, such that when the method
	 * returns, two objects are in the same partition block just if they were in
	 * the same partition block previously and they are treated identically by
	 * this constraint.
	 */
	public void refinePartitions(Map<Type, List<Set<Object>>> typePartitions) {
		for (Map.Entry<LogicalVar, Set<Term>> entry : excluded.entrySet()) {
			LogicalVar x = entry.getKey();
			List<Set<Object>> oldPartition = typePartitions.get(x.getType());
			if (oldPartition == null) {
				continue; // not maintaining partition for this type
			}

			Set<Term> excludedTerms = entry.getValue();
			Set<Object> excludedObjs = new HashSet<Object>();
			for (ArgSpec term : excludedTerms) {
				if (!(term instanceof LogicalVar)) {
					excludedObjs.add(term.getValueIfNonRandom());
				}
			}

			List newPartition = new ArrayList<Set<Object>>();
			for (Set<Object> oldBlock : oldPartition) {
				Iterator<Object> iter = oldBlock.iterator();
				Object firstObj = iter.next();
				boolean firstObjStatus = excludedObjs.contains(firstObj);
				Set<Object> diffFromFirst = new LinkedHashSet<Object>();
				while (iter.hasNext()) {
					Object obj = iter.next();
					if (excludedObjs.contains(obj) != firstObjStatus) {
						diffFromFirst.add(obj);
						iter.remove();
					}
				}

				newPartition.add(oldBlock); // now with diffFromFirst removed
				if (!diffFromFirst.isEmpty()) {
					newPartition.add(diffFromFirst);
				}
			}
			typePartitions.put(x.getType(), newPartition);
		}
	}

	public void print(PrintStream out) {
		out.println(this);
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		Iterator<Map.Entry<LogicalVar, Set<Term>>> i = excluded.entrySet()
				.iterator();
		while (i.hasNext()) {
			Map.Entry<LogicalVar, Set<Term>> me = i.next();
			LogicalVar l = me.getKey();
			buf.append(l);
			buf.append("!={");
			Iterator<Term> j = me.getValue().iterator();
			while (j.hasNext()) {
				buf.append(j.next());
				if (j.hasNext())
					buf.append(", ");
			}
			buf.append("}");
			if (i.hasNext())
				buf.append(", ");
		}
		return buf.toString();
	}

	public boolean equals(Object o) {
		if (o instanceof Constraint) {
			Constraint other = (Constraint) o;
			return excluded.equals(other.excluded);
		}
		return false;
	}

	public int hashCode() {
		return excluded.hashCode();
	}

	public static class Overlap {
		private Overlap(boolean isFull, Substitution theta, Constraint c1theta,
				Constraint c2theta) {
			this.isFull = isFull;
			this.theta = theta;
			this.c1theta = c1theta;
			this.c2theta = c2theta;
		}

		public boolean isFull() {
			return isFull;
		}

		public Substitution theta() {
			return theta;
		}

		public Constraint c1theta() {
			return c1theta;
		}

		public Constraint c2theta() {
			return c2theta;
		}

		public String toString() {
			return (isFull ? "FULL" : "PARTIAL");
		}

		private boolean isFull;
		private Substitution theta;
		private Constraint c1theta;
		private Constraint c2theta;
	}

	/**
	 * Returns a constraint that allows a grounding only if it is allowed both
	 * both of the given constraints. The returned constraint is defined on the
	 * union of the logical variable sets on which the two given constraints are
	 * defined.
	 */
	public static Constraint intersection(Constraint c1, Constraint c2) {
		Map<LogicalVar, Set<Term>> newExcludedMap = new HashMap<LogicalVar, Set<Term>>();
		for (LogicalVar x : c1.logicalVars()) {
			newExcludedMap.put(x, new LinkedHashSet<Term>(c1.excluded(x)));
		}

		// A term is excluded in the intersection if it is excluded in
		// either of the given constraints.
		for (LogicalVar x : c2.logicalVars()) {
			Set<Term> excluded = newExcludedMap.get(x);
			if (excluded == null) {
				newExcludedMap.put(x, new LinkedHashSet<Term>(c2.excluded(x)));
			}
			excluded.addAll(c2.excluded(x));
		}

		return new Constraint(newExcludedMap);
	}

	/**
	 * Returns a constraint on the given variable, allowing the given set of
	 * objects.
	 */
	public static Constraint createAllowing(LogicalVar x, Set<?> allowed) {
		Type type = x.getType();
		Set<Term> excludedForX = new LinkedHashSet<Term>();
		for (Object obj : x.getType().getGuaranteedObjects()) {
			if (!allowed.contains(obj)) {
				excludedForX.add(type.getCanonicalTerm(obj));
			}
		}

		Map<LogicalVar, Set<Term>> newExcluded = new HashMap<LogicalVar, Set<Term>>();
		newExcluded.put(x, excludedForX);
		return new Constraint(newExcluded);
	}

	public static Overlap getOverlap(Term a1, Constraint c1, Term a2,
			Constraint c2) {

		Substitution theta = new Substitution();
		if (!a1.makeOverlapSubst(a2, theta)) {
			return null;
		}
		// System.out.println("\t\ttheta = " + theta);

		// Extend constraints to include constraint on count variable, if any.
		// Also change a1 and a2 to be terms inside counting terms.
		if (a1 instanceof CountingTerm) {
			c1 = new Constraint(c1, ((CountingTerm) a1).constraint());
			a1 = ((CountingTerm) a1).singleSubTerm();
		}
		if (a2 instanceof CountingTerm) {
			c2 = new Constraint(c2, ((CountingTerm) a2).constraint());
			a2 = ((CountingTerm) a2).singleSubTerm();
		}

		// Check consistency

		if (!c1.consistent(theta) || !c2.consistent(theta)) {
			// System.out.println("\t\tconsistency check failed");
			return null; // can't unify with constraints
		}

		Term unified = (Term) a1.getSubstResult(theta);
		Set<LogicalVar> unifiedVars = (Set<LogicalVar>) unified.getFreeVars();

		Constraint c1theta = c1.getSubstResult(theta).getProjection(unifiedVars);
		Constraint c2theta = c2.getSubstResult(theta).getProjection(unifiedVars);
		Constraint intersection = Constraint.intersection(c1theta, c2theta);
		if (intersection.hasContradiction()) {
			return null;
		}

		// See if overlap is full or partial

		if (theta.hasConstant()) {
			// need to split on constant
			return new Overlap(false, theta, c1theta, c2theta);
		}

		// if the substitution changes the number of free vars in a1 or a2,
		// the overlap is partial.
		if (!theta.isOneToOneOn(a1.getFreeVars())
				|| !theta.isOneToOneOn(a2.getFreeVars())) {
			// partial overlap
			return new Overlap(false, theta, c1theta, c2theta);
		}

		// Substitution is one-to-one variable renaming, so overlap is full
		// iff constraints are the same after substitution and projection.
		return new Overlap(c1theta.equals(c2theta), theta, c1theta, c2theta);
	}

	private void addConstraintsFromFormula(ConjFormula c) {
		List inEqs = c.getConjuncts();
		Iterator i = inEqs.iterator();
		while (i.hasNext()) {
			Formula ie = (Formula) i.next();

			// This is here to handle formulas produced by getPropCNF()
			while ((ie instanceof DisjFormula)
					&& (((DisjFormula) ie).getDisjuncts().size() == 1)) {
				ie = (Formula) ((DisjFormula) ie).getDisjuncts().get(0);
			}

			if (ie instanceof NegFormula) {
				addConstraintFromNegation((NegFormula) ie);
			} else {
				if (ie instanceof ConjFormula) {
					addConstraintsFromFormula((ConjFormula) ie);
				} else {
					throw new IllegalArgumentException(ie.getLocation() + ": " + ie
							+ " is not a well-formed constraint.");
				}
			}
		}
	}

	private void addConstraintFromNegation(NegFormula nf) {
		Formula f = nf.getNeg();
		if (!(f instanceof EqualityFormula)) {
			throw new IllegalArgumentException(nf.getLocation() + ": " + nf
					+ " is not an inequality formula.");
		}
		EqualityFormula eq = (EqualityFormula) f;
		Term t1 = eq.getTerm1();
		Term t2 = eq.getTerm2();

		boolean gotVar = false;
		if (t1 instanceof LogicalVar) {
			Set<Term> s = excluded.get((LogicalVar) t1);
			if (s != null) {
				s.add(t2.getCanonicalVersion());
				gotVar = true;
			}
		}
		if (t2 instanceof LogicalVar) {
			Set<Term> s = excluded.get((LogicalVar) t2);
			if (s != null) {
				s.add(t1.getCanonicalVersion());
				gotVar = true;
			}
		}

		if (!gotVar) {
			throw new IllegalArgumentException(f.getLocation() + ": Inequality " + f
					+ " does not mention any of the logical variables for which "
					+ "this constraint is being specified.");
		}
	}

	Map<LogicalVar, Set<Term>> excluded;

	// these variables cache quantities that we will want to compute often
	// they are initialized in the constructors and should never change
	// since constraints are immutable
	Map<LogicalVar, Set<Term>> excludedConstants; // cache these for efficency
	boolean hasVarInEq = false; // are any variables included in the excluded sets
	Term[] firstNormalFormViolation; // null if in normal form
}
