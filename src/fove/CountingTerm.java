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

import blog.*;
import blog.common.HashMapDiff;
import blog.common.TupleIterator;
import blog.common.Util;

/**
 * A term whose value is a histogram, counting the number of bindings to a set
 * of logical variables that yield each possible tuple of values for a tuple of
 * sub-terms. For instance, the value of the counting term
 * <code>#(X, Y)[f(X), g(X, Y), h(Y)]</code> in a given world is a histogram
 * with a bucket for each tuple of possible values for f(X), g(X, X) and h(X).
 * The count in a given bucket is the number of bindings to (X, Y) that yield
 * that tuple of term values in this world.
 * 
 * TODO: Standardize apart logical vars when CountingTerm is created. Allow
 * unification.
 */
public class CountingTerm extends Term {
	/**
	 * Creates a new counting term with the given bound variables and the given
	 * sub-terms, with no constraint.
	 */
	public CountingTerm(Collection<? extends LogicalVar> vars,
			List<? extends Term> subTerms) {
		this(vars, new Constraint(vars), subTerms);
	}

	/**
	 * Creates a new counting term with the given bound variables, constraint, and
	 * sub-terms.
	 */
	public CountingTerm(Collection<? extends LogicalVar> vars,
			Constraint constraint, List<? extends Term> subTerms) {
		this.vars = new ArrayList<LogicalVar>(vars);
		this.constraint = constraint;
		this.subTerms = new ArrayList<Term>(subTerms);
	}

	/**
	 * Creates a new counting term with the given bound variables, a constraint
	 * defined by the given formula, and the given sub-terms.
	 * 
	 * <p>
	 * This constructor is used by the parser. If you use it elsewhere, be sure to
	 * call compile() afterwards to compile the constraint formula into a
	 * Constraint object.
	 */
	public CountingTerm(Collection<? extends LogicalVar> vars,
			Formula constraint, List<? extends Term> subTerms) {
		this.vars = new ArrayList<LogicalVar>(vars);
		this.fConstraint = constraint;
		this.subTerms = new ArrayList<Term>(subTerms);
	}

	public CountingTerm addConstraint(Term t) {
		checkSingletons();
		LogicalVar v = vars.get(0);
		return new CountingTerm(vars, constraint.addConstraint(v, t), subTerms);
	}

	public CountingTerm renameCountingVar(LogicalVar newVar) {
		checkSingletons();
		LogicalVar v = vars.get(0);
		Substitution theta = new Substitution();
		theta.add(v, newVar);
		ArgSpec subt = subTerms.get(0);
		CountingTerm newC = new CountingTerm(vars,
				constraint.getSubstResult(theta), subTerms);
		newC.subTerms.set(0, replaceCountVarSubTerm(newVar));
		newC.vars.set(0, newVar);
		return newC;
	}

	public Term replaceCountVarSubTerm(Term t) {
		checkSingletons();
		Term subt = subTerms.get(0);
		LogicalVar v = vars.get(0);
		Substitution theta = new Substitution();
		theta.add(v, t);
		return (Term) subt.getSubstResult(theta);
	}

	/**
	 * Returns the logical variables being counted over.
	 */
	public List<? extends LogicalVar> logicalVars() {
		return Collections.unmodifiableList(vars);
	}

	/**
	 * Returns an unmodifiable list of the terms in this counting term.
	 */
	public List<? extends Term> subTerms() {
		return Collections.unmodifiableList(subTerms);
	}

	/**
	 * Returns this counting term's constraint.
	 */
	public Constraint constraint() {
		return constraint;
	}

	public boolean checkTypesAndScope(Model model, Map scope) {
		Map extendedScope = new HashMapDiff(scope);
		for (LogicalVar v : vars) {
			extendedScope.put(v.getName(), v);
		}

		if ((fConstraint != null)
				&& !fConstraint.checkTypesAndScope(model, extendedScope)) {
			return false;
		}

		for (ListIterator<Term> it = subTerms.listIterator(); it.hasNext();) {
			Term termInScope = it.next().getTermInScope(model, extendedScope);
			if (termInScope == null) {
				return false;
			}
			it.set(termInScope);
		}

		return true;
	}

	public int compile(LinkedHashSet callStack) {
		int numErrs = 0;

		if (constraint == null) {
			numErrs = fConstraint.compile(callStack);
			if (numErrs == 0) {
				try {
					constraint = new Constraint(fConstraint, vars);
				} catch (IllegalArgumentException e) {
					System.err.println(e.getMessage());
					++numErrs;
				}
			}
		}

		for (ListIterator<Term> it = subTerms.listIterator(); it.hasNext();) {
			Term term = it.next();
			numErrs += term.compile(callStack);
			it.set(term.getCanonicalVersion());
		}

		return numErrs;
	}

	/**
	 * Returns the number of joint instantiations of all the ground random
	 * variables covered by this term that yield the given histogram.
	 * 
	 * <p>
	 * The simple case is where:
	 * <ul>
	 * <li>For each binding of the logical variables, the atoms being counted
	 * correspond to distinct random variables (or disjoint sets of random
	 * variables if they're counting terms themselves). This does not hold for a
	 * term like #(X, Y)[r(X, Y), r(Y, X)], because for a binding where X = Y, the
	 * two atoms correspond to the same random variable.
	 * 
	 * <li>Distinct bindings of the logical variables cover disjoint sets of
	 * random variables. This does not hold for #(X, Y)[p(X), r(X, Y)], because
	 * distinct bindings to (X, Y) cover the same p(X) variables.
	 * </ul>
	 * If these conditions hold, the number of joint instantiations that yield
	 * <code>h</code> is the multinomial coefficient: <blockquote> n! / (h[0]! *
	 * ... * h[k]!) </blockquote> where n is the total count and k is the number
	 * of buckets in the histogram.
	 * 
	 * <p>
	 * Note that the conditions trivially hold when we are counting a single
	 * FuncAppTerm, and all the logical variables we're counting over occur free
	 * in that term. This is the only case we're dealing with for now.
	 */
	public double getNumJointInstsYieldingHist(Histogram h) {
		return h.multinomialCoefficient(getHistType().getTotal());
	}

	public Object evaluate(EvalContext context) {
		List<List<?>> buckets = getHistType().getBuckets();
		int[] counts = new int[buckets.size()];
		Arrays.fill(counts, 0);

		List<Collection<Object>> varDomains = new ArrayList<Collection<Object>>(
				vars.size());
		for (LogicalVar v : vars) {
			varDomains.add(v.getType().getGuaranteedObjects());
		}

		TupleIterator iter = new TupleIterator(varDomains);
		while (iter.hasNext()) {
			List objs = (List) iter.next();
			for (int i = 0; i < vars.size(); ++i) {
				context.assign(vars.get(i), objs.get(i));
			}
			if (constraint.isSatisfied(context)) {
				List bucket = new ArrayList<Object>(subTerms.size());
				for (ArgSpec subTerm : subTerms) {
					bucket.add(subTerm.evaluate(context));
				}
				++counts[buckets.indexOf(bucket)];
			}
		}

		return new Histogram(counts);
	}

	public Collection getSubExprs() {
		Set subExprs = new HashSet(subTerms);
		if (fConstraint != null) {
			subExprs.add(fConstraint);
		}
		return subExprs;
	}

	public boolean containsRandomSymbol() {
		if ((fConstraint != null) && fConstraint.containsRandomSymbol()) {
			return true;
		}

		for (ArgSpec t : subTerms) {
			if (t.containsRandomSymbol()) {
				return true;
			}
		}

		return false;
	}

	public LogicalVar getCountVar() {
		checkSingletons();
		return vars.get(0);
	}

	public Set getFreeVars() {
		Set freeVars = new HashSet();

		if (constraint == null) {
			freeVars.addAll(fConstraint.getFreeVars());
		} else {
			freeVars.addAll(constraint.getFreeVars());
		}

		for (ArgSpec t : subTerms) {
			freeVars.addAll(t.getFreeVars());
		}

		freeVars.removeAll(vars);
		return Collections.unmodifiableSet(freeVars);
	}

	public ArgSpec getSubstResult(Substitution subst, Set<LogicalVar> boundVars) {
		boundVars = new HashSet<LogicalVar>(boundVars);
		boundVars.addAll(vars);

		List<Term> newSubTerms = new ArrayList<Term>(subTerms.size());
		for (ArgSpec t : subTerms) {
			newSubTerms.add((Term) t.getSubstResult(subst, boundVars));
		}

		Constraint newConstraint = constraint.getSubstResult(subst, boundVars);
		return new CountingTerm(vars, newConstraint, newSubTerms);
	}

	public Type getType() {
		ensureRetTypeInited();
		return retType;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("#(");
		for (int i = 0; i < vars.size(); ++i) {
			buf.append(vars.get(i));
			if (i + 1 < vars.size()) {
				buf.append(", ");
			}
		}
		buf.append(": ");
		if (constraint == null) {
			buf.append(fConstraint);
		} else {
			buf.append(constraint);
		}
		buf.append(")[");
		for (int i = 0; i < subTerms.size(); ++i) {
			buf.append(subTerms.get(i));
			if (i + 1 < subTerms.size()) {
				buf.append(", ");
			}
		}
		buf.append("]");
		return buf.toString();
	}

	/**
	 * Returns true if <code>o</code> is a CountingTerm that is alpha-equivalent
	 * to this one. That is, the two counting terms must be the same up to
	 * renaming of the count variable.
	 */
	public boolean equals(Object o) {
		checkSingletons();
		if (o instanceof CountingTerm) {
			CountingTerm other = (CountingTerm) o;
			other.checkSingletons();

			LogicalVar myV = vars.get(0);
			LogicalVar otherV = other.vars.get(0);

			// check that the excluded sets match
			if (constraint != null) {
				if (!constraint.excluded(myV).equals(other.constraint.excluded(otherV))) {
					return false;
				}
			} else { // constraint is null, check fConstraint
				Substitution renaming = new Substitution();
				renaming.makeEqual(myV, otherV);

				if (!fConstraint.getSubstResult(renaming).equals(
						other.fConstraint.getSubstResult(renaming))) {
					return false;
				}
			}

			// check that the atoms are the same
			FuncAppTerm myA = (FuncAppTerm) subTerms.get(0);
			FuncAppTerm otherA = (FuncAppTerm) other.subTerms.get(0);
			if (!myA.getFunction().equals(otherA.getFunction())) {
				return false;
			}

			Term[] myArgs = myA.getArgs();
			Term[] otherArgs = otherA.getArgs();
			for (int i = 0; i < myArgs.length; i++) {
				// the both need to be the counting vars or equal to each other
				if (!((myArgs[i] == myV && otherArgs[i] == otherV) || (myArgs[i]
						.equals(otherArgs[i])))) {
					return false;
				}
			}

			return true;

			/*
			 * return (vars.equals(other.vars) && (((constraint != null) &&
			 * constraint.equals(other.constraint)) || ((fConstraint != null) &&
			 * fConstraint.equals(other.fConstraint))) &&
			 * subTerms.equals(other.subTerms));
			 */
		}
		return false;
	}

	/**
	 * CountingTerm only counts a single FuncAppTerm for now; other classes
	 * relying on this assumption can call this method to get that term. If this
	 * assumption ever changes, this method can be removed, so the compile errors
	 * expose uses of the assumption elsewhere in the code base.
	 */
	public FuncAppTerm singleSubTerm() {
		if (subTerms.size() != 1) {
			throw new IllegalStateException(
					"Counting term must have exactly one sub-term: " + this);
		}
		return (FuncAppTerm) subTerms.get(0);
	}

	public int hashCode() {
		// Don't include constraint or fConstraint here so compiling
		// doesn't change the hash code.

		checkSingletons();
		LogicalVar myV = vars.get(0);
		FuncAppTerm myA = (FuncAppTerm) subTerms.get(0);
		Term[] myArgs = myA.getArgs();

		int hashCode = myA.getFunction().hashCode();
		for (int i = 0; i < myArgs.length; i++) {
			if (myArgs[i] != myV) {
				hashCode *= myArgs[i].hashCode();
			}
		}
		return hashCode;
	}

	protected HistogramType getHistType() {
		ensureRetTypeInited();
		return retType;
	}

	private void ensureRetTypeInited() {
		checkSingletons();
		if (retType == null) {
			LogicalVar v = vars.get(0);
			int numBindings = constraint.numConstrainedGroundings(v);
			List<Type> termTypes = new ArrayList<Type>();
			termTypes.add(subTerms.get(0).getType());
			retType = new HistogramType(termTypes, numBindings);
		}

		/*
		 * if (retType == null) { int numBindings = 1; for (LogicalVar v : vars) {
		 * numBindings *= v.getType().getGuaranteedObjects().size(); } // TODO: take
		 * constraint into account here
		 * 
		 * List<Type> termTypes = new ArrayList<Type>(); for (Term t : subTerms) {
		 * termTypes.add(t.getType()); }
		 * 
		 * retType = new HistogramType(termTypes, numBindings); }
		 */
	}

	public void checkSingletons() {
		if (vars.size() != 1) {
			System.err.println("ERROR: " + this
					+ " is counting more than one variable.");
			System.exit(-1);
		}
		if (subTerms.size() != 1) {
			System.err.println("ERROR: " + this + " has more than one sub term.");
			System.exit(-1);
		}
	}

	public boolean makeOverlapSubst(Term t, Substitution theta) {
		if (t instanceof CountingTerm) {
			CountingTerm other = (CountingTerm) t;
			Term f0 = (Term) subTerms.get(0);
			Term f1 = (Term) other.subTerms.get(0);
			if (f0.makeOverlapSubst(f1, theta)) {
				if (constraint.consistent(theta) && other.constraint.consistent(theta)) {
					return true;
				}
			}
		} else {
			Term f = (Term) subTerms.get(0);
			if (f.makeOverlapSubst(t, theta)) {
				// System.out.println("theta: "+theta);
				if (constraint.consistent(theta)) {
					return true;
				}
			}
		}
		return false;
	}

	public ArgSpec replace(Term t, ArgSpec another) {
		Util.fatalError("CountingTerm.replace(Term,Term) called but not implemented yet!");
		// TODO: implement.
		return null;
	}

	public Term getCanonicalVersion() {
		// Note: the Constraint class ensures that terms in
		// constraints are always as canonicalized as possible.

		List<Term> newSubTerms = new ArrayList<Term>();
		for (Term term : subTerms) {
			newSubTerms.add(term.getCanonicalVersion());
		}

		CountingTerm canonical = new CountingTerm(vars, constraint, newSubTerms);
		canonical.setLocation(location);
		return canonical;
	}

	protected List<LogicalVar> vars;
	protected Constraint constraint;
	protected List<Term> subTerms;

	protected Formula fConstraint;

	private HistogramType retType;
}
