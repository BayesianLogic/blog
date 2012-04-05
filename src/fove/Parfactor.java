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

package fove;

import java.util.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.io.PrintStream;

import blog.*;
import blog.common.CartesianProduct;
import blog.common.TupleIterator;
import blog.common.Util;
import blog.model.ArgSpec;
import blog.model.LogicalVar;
import blog.model.Model;
import blog.model.NonRandomFunction;
import blog.model.Term;
import blog.model.Type;
import ve.*;

/**
 * A parameterized factor. TODO: make sure the terms passed into the
 * constructors are not VariableTerms.
 */
public class Parfactor {
	/**
	 * Creates a new parfactor with the given logical variables, terms, and
	 * potential. The constraint is set so it is always true.
	 */
	public Parfactor(Collection<? extends LogicalVar> logicalVars,
			List<? extends Term> terms, Potential potential) {
		this(logicalVars, new Constraint(logicalVars), terms, potential);
	}

	/**
	 * Creates a new parfactor with the given logical variables, constraint,
	 * terms, and potential.
	 */
	public Parfactor(Collection<? extends LogicalVar> logicalVars,
			Constraint constraint, List<? extends Term> terms, Potential potential) {
		this.logicalVars = new ArrayList<LogicalVar>(logicalVars);
		this.constraint = constraint;
		this.dimTerms = new ArrayList<Term>(terms);
		this.potential = potential;

		for (ArgSpec term : dimTerms) {
			if (term instanceof CountingTerm) {
				// Check for normal-form violations
				String errMsg = ((CountingTerm) term).constraint().findNormalFormError(
						constraint);
				if (errMsg != null) {
					throw new IllegalArgumentException("Counting term " + term
							+ " has constraint that is "
							+ "not in normal form with respect to the constraint "
							+ "on its parfactor: " + errMsg + ".");
				}
			}
		}
	}

	/**
	 * Creates a parfactor with the given logical variables, constraint, and
	 * terms. Also stores the given potential class and parameters, which will be
	 * used by the <code>compile</code> method to create the potential.
	 */
	public Parfactor(Collection<? extends LogicalVar> logicalVars,
			Formula constraint, List<? extends Term> terms, Class potentialClass,
			List<? extends ArgSpec> potentialParams) {
		this.logicalVars = new ArrayList<LogicalVar>(logicalVars);
		this.fConstraint = constraint;
		this.dimTerms = new ArrayList<Term>(terms);
		this.potentialClass = potentialClass;
		this.potentialParams = potentialParams;
	}

	/**
	 * Creates a new parfactor with no terms, and a potential containing a single
	 * real number.
	 */
	public Parfactor(double val) {
		this.logicalVars = new ArrayList<LogicalVar>();
		this.constraint = Constraint.EMPTY;
		this.dimTerms = new ArrayList<Term>();
		this.potential = new MultiArrayPotential(Collections.EMPTY_LIST, val);
	}

	/**
	 * Returns the logical variables over which this parfactor quantifies.
	 * 
	 * @return unmodifiable list
	 */
	public List<? extends LogicalVar> logicalVars() {
		return Collections.unmodifiableList(logicalVars);
	}

	/**
	 * Returns the terms on which this parfactor is defined. (These are called
	 * formulas in the AAAI 2008 paper on C-FOVE, but they're treated as terms in
	 * the code, because BLOG assumes that formulas are Boolean-valued.)
	 */
	public List<? extends Term> dimTerms() {
		return Collections.unmodifiableList(dimTerms);
	}

	/**
	 * Returns the number of terms on which this parfactor is defined. (Terms are
	 * called formulas in the AAAI 2008 paper on C-FOVE, but they're treated as
	 * terms in the code, because BLOG assumes that formulas are Boolean-valued.)
	 */
	public int numDimTerms() {
		return dimTerms.size();
	}

	/**
	 * Returns this parfactor's constraint.
	 * 
	 * @throws IllegalStateException
	 *           if this parfactor was loaded from a file and has not been
	 *           compiled
	 */
	public Constraint constraint() {
		if (constraint == null) {
			throw new IllegalStateException("Parfactor has not been compiled.");
		}
		return constraint;
	}

	/**
	 * Returns this parfactor's potential.
	 * 
	 * @throws IllegalStateException
	 *           if this parfactor was loaded from a file and has not been
	 *           compiled
	 */
	public Potential potential() {
		if (potential == null) {
			throw new IllegalStateException("Parfactor has not been compiled.");
		}
		return potential;
	}

	/**
	 * Returns an object pointing to the specified index in this parfactor's term
	 * list.
	 */
	public Parfactor.TermPtr termPtr(int index) {
		return new TermPtr(index);
	}

	/**
	 * Returns true if types and scope are correct in this parfactor; otherwise
	 * returns false.
	 */
	public boolean checkTypesAndScope(Model model) {
		Map<String, LogicalVar> scope = new HashMap<String, LogicalVar>();
		for (LogicalVar v : logicalVars) {
			scope.put(v.getName(), v);
		}

		boolean correct = true;
		if (!fConstraint.checkTypesAndScope(model, scope)) {
			correct = false;
		}

		for (ListIterator<Term> it = dimTerms.listIterator(); it.hasNext();) {
			Term termInScope = it.next().getTermInScope(model, scope);
			if (termInScope == null) {
				return false;
			}
			it.set(termInScope);
		}

		if (potentialParams != null) {
			// Potential parameters should not include logical variables
			Map<String, LogicalVar> emptyScope = Collections.emptyMap();
			for (ArgSpec p : potentialParams) {
				if (!p.checkTypesAndScope(model, emptyScope)) {
					correct = false;
				}
			}
		}

		return correct;
	}

	/**
	 * Compiles all expressions in this parfactor, and creates the potential
	 * object from its class and parameters (if it has not been created already).
	 * Returns the number of errors that occurred.
	 */
	public int compile(LinkedHashSet callStack) {
		callStack.add(this);
		int errors = 0;

		if (constraint == null) {
			errors += fConstraint.compile(callStack);
			if (errors == 0) {
				try {
					constraint = new Constraint(fConstraint, logicalVars);
				} catch (IllegalArgumentException e) {
					System.err.println(e.getMessage());
					++errors;
				}
			}
		}

		for (ListIterator<Term> it = dimTerms.listIterator(); it.hasNext();) {
			Term t = it.next();

			int termCompErrors = t.compile(callStack);
			errors += termCompErrors;
			if (termCompErrors != 0) {
				continue;
			}

			t = t.getCanonicalVersion();
			it.set(t);

			if (t instanceof CountingTerm) {
				// Check for normal-form violations
				String errMsg = ((CountingTerm) t).constraint().findNormalFormError(
						constraint);
				if (errMsg != null) {
					System.err.println(t.getLocation() + ": " + "Constraint on counting "
							+ "term " + t + " is not in normal form with respect "
							+ "to constraint on its parfactor: " + errMsg + ".");
					++errors;
				}
			}
		}

		if (potential == null) {
			errors += initPotential(callStack);
		}

		callStack.remove(this);
		return errors;
	}

	public Parfactor copyWithConstraint(LogicalVar l, Term t) {
		Parfactor intermediate = new Parfactor(logicalVars, new Constraint(
				constraint, l, t), dimTerms, potential);
		return intermediate.getRenamedCopy();
	}

	/**
	 * Returns a new Parfactor with some free variables bound by theta, and the
	 * constraint properly projected.
	 */
	public Parfactor applySubstitution(Substitution theta) {

		List<Term> newArgTerms = new ArrayList<Term>(dimTerms.size());
		for (ArgSpec t : dimTerms) {
			newArgTerms.add(((Term) t.getSubstResult(theta)).getCanonicalVersion());
		}

		// remove/rename variables
		List<LogicalVar> newLVs = new ArrayList<LogicalVar>(logicalVars);
		for (int i = 0; i < newLVs.size(); i++) {
			LogicalVar x = newLVs.get(i);
			ArgSpec t = theta.getReplacement(x);
			if (x != t) {
				if (t instanceof LogicalVar && !newLVs.contains((LogicalVar) t)) {
					newLVs.set(i, (LogicalVar) t);
				} else {
					newLVs.remove(i);
					i--;
				}
			}
		}

		return new Parfactor(newLVs, constraint.getSubstResult(theta), newArgTerms,
				potential);
	}

	public Parfactor getRenamedCopy() {
		Substitution renaming = new Substitution();
		for (LogicalVar var : logicalVars) {
			renaming.add(var, var.makeNew());
		}
		Parfactor res = applySubstitution(renaming);
		for (int i = 0; i < res.dimTerms.size(); i++) {
			if (res.dimTerms.get(i) instanceof CountingTerm) {
				CountingTerm orig = (CountingTerm) res.dimTerms.get(i);
				LogicalVar x = orig.getCountVar();
				CountingTerm newC = orig.renameCountingVar(x.makeNew());
				res.dimTerms.set(i, newC);
			}
		}
		return res;
	}

	/**
	 * Returns the number of groundings of this parfactor: that is, the number of
	 * propositional factors it defines.
	 */
	public int numGroundings() {
		return constraint.numConstrainedGroundings(logicalVars);
	}

	/**
	 * Returns the collection of propositional factors defined by this parfactor.
	 */
	public Collection<Factor> getFactors() {
		List<Collection<Object>> varDomains = new ArrayList<Collection<Object>>(
				logicalVars.size());
		for (LogicalVar v : logicalVars) {
			varDomains.add(v.getType().getGuaranteedObjects());
		}

		Collection<Factor> factors = new ArrayList<Factor>();
		TupleIterator iter = new TupleIterator(varDomains);
		while (iter.hasNext()) {
			List objs = (List) iter.next();

			PartialWorld w = new DefaultPartialWorld();
			ParentRecEvalContext context = new SimpleInstEvalContext(w);
			for (int i = 0; i < logicalVars.size(); ++i) {
				context.assign(logicalVars.get(i), objs.get(i));
			}

			if (constraint.isSatisfied(context)) {
				factors.add(getInstanceForContext(w, context));
			}
		}

		return factors;
	}

	/**
	 * Returns the weight specified by this parfactor for the given partial world.
	 * 
	 * @throws IllegalArgumentException
	 *           if the given partial world does not assign values to all the
	 *           random variables on which this parfactor is defined.
	 */
	public double getWeight(PartialWorld world) {
		double weight = 1;
		for (Factor f : getFactors()) {
			weight *= f.getWeight(world);
		}
		return weight;
	}

	/**
	 * Prints a description of this parfactor to the given stream.
	 */
	public void print(PrintStream out) {
		out.print("logical vars: ");
		out.println(logicalVars);

		out.print("constraint: ");
		if (constraint == null) {
			out.println(fConstraint);
		} else {
			constraint.print(out);
		}

		out.print("dimTerms: ");
		out.println(dimTerms);

		if (potential == null) {
			out.print(potentialClass.getName());
			out.println(potentialParams);
		} else {
			potential.print(out);
		}
	}

	private Factor getInstanceForContext(PartialWorld w,
			ParentRecEvalContext context) {
		// Figure out what random variables are needed
		for (ArgSpec term : dimTerms) {
			term.evaluate(context);
		}
		List<BasicVar> factorRVs = new ArrayList<BasicVar>(
				(Set<BasicVar>) context.getParents());

		// Make potential for factor
		List<Type> types = new ArrayList<Type>(factorRVs.size());
		List<List> ranges = new ArrayList<List>(factorRVs.size());
		for (BasicVar rv : factorRVs) {
			types.add(rv.getType());
			ranges.add(rv.getType().range());
		}
		Potential factorPot = new MultiArrayPotential(types);

		// Set the values in the factor's potential
		for (Iterator iter = new TupleIterator(ranges); iter.hasNext();) {
			List varValues = (List) iter.next();
			for (int i = 0; i < factorRVs.size(); ++i) {
				w.setValue(factorRVs.get(i), varValues.get(i));
			}

			List termValues = new ArrayList(dimTerms.size());
			for (ArgSpec term : dimTerms) {
				termValues.add(term.evaluate(context));
			}

			factorPot.setValue(varValues, potential.getValue(termValues));
		}

		return new Factor(factorRVs, factorPot);
	}

	/**
	 * Modifies the given map from types to partitions, such that when the method
	 * returns, two objects are in the same partition block just if they were in
	 * the same partition block previously and they are treated identically by
	 * this parfactor.
	 */
	public void refinePartitions(Map<Type, List<Set<Object>>> typePartitions) {
		constraint.refinePartitions(typePartitions);

		// Any object that is denoted by a constant symbol in this
		// parfactor is treated specially.
		for (ArgSpec term : dimTerms) {
			if (term instanceof CountingTerm) {
				// extract first FuncAppTerm
				term = ((CountingTerm) term).singleSubTerm();
			}

			Term[] args = ((FuncAppTerm) term).getArgs();
			for (int i = 0; i < args.length; ++i) {
				Term arg = args[i];
				if (!(arg instanceof LogicalVar)) {
					List<Set<Object>> partition = typePartitions.get(arg.getType());
					if (partition == null) {
						continue; // not looking at this type
					}

					// If object is not already a singleton, remove it
					// from its existing block and add a new block for it.
					Object obj = args[i].getValueIfNonRandom();
					for (Set<Object> block : partition) {
						if (block.contains(obj)) {
							if (block.size() > 1) {
								block.remove(obj);
								Set<Object> newBlock = new LinkedHashSet<Object>();
								newBlock.add(obj);
								partition.add(newBlock);
							}
							break;
						}
					}
				}
			}
		}
	}

	public void normalize() {
		potential.normalize();
	}

	/**
	 * Sets the location of this parfactor, for instance, the file name and line
	 * number where it appears. The location can be any object whose toString
	 * method returns an identifying string that can be used in error messages.
	 */
	public void setLocation(Object loc) {
		location = loc;
	}

	/**
	 * Returns the object specified by the last call to setLocation. If
	 * setLocation has not been called, returns the string "(no location)".
	 */
	public Object getLocation() {
		return location;
	}

	private int initPotential(LinkedHashSet callStack) {
		int errors = 0;

		// Create array of argument types
		List<Type> argTypes = new ArrayList<Type>();
		for (Term arg : dimTerms) {
			argTypes.add(arg.getType());
		}

		// Create array of parameter values
		List paramValues = new ArrayList();
		for (ArgSpec param : potentialParams) {
			int thisParamErrors = param.compile(callStack);
			errors += thisParamErrors;
			if (thisParamErrors == 0) {
				Object val = param.getValueIfNonRandom();
				if (val == null) {
					System.err.println("Error initializing potential at " + getLocation()
							+ ": parameter " + param + " is random.  Random "
							+ "parameters should be passed as arguments.");
					++errors;
				} else {
					paramValues.add(param.getValueIfNonRandom());
				}
			}
		}

		if (errors > 0) {
			return errors; // can't compute parameters, can't create potential
		}

		try {
			Class[] constrArgTypes = { List.class, List.class };
			Constructor ct = potentialClass.getConstructor(constrArgTypes);
			Object[] constrArgs = { argTypes, paramValues };
			potential = (Potential) ct.newInstance(constrArgs);
		} catch (InvocationTargetException e) {
			System.err.println("Error initializing potential at " + getLocation()
					+ ": " + e.getCause().getClass().getName() + " ("
					+ e.getCause().getMessage() + ")");
			++errors;
		} catch (NoSuchMethodException e) {
			System.err.println("Error initializing potential at " + getLocation()
					+ ": " + potentialClass + " does not have a constructor with "
					+ "argument types (List<Type>, List).");
			++errors;
		} catch (ClassCastException e) {
			System.err.println("Error initializing potential at " + getLocation()
					+ ": " + potentialClass + " does not implement the "
					+ "Potential interface.");
			++errors;
		} catch (Exception e) {
			System.err.println("Error initializing potential at " + getLocation()
					+ ": couldn't instantiate " + "class " + potentialClass);
			++errors;
		}

		return errors;
	}

	/**
	 * Returns true iff RV sets overlap but are not equal, and shatters if so,
	 * with <code>residuals</code> destructively modified to contain new
	 * parfactors created by shattering.
	 */
	public boolean shatter(Parfactor other, int myAtomIndex, int otherAtomIndex,
			Collection<Parfactor> residuals) {
		return shatter(other, myAtomIndex, otherAtomIndex, residuals, residuals);
	}

	/**
	 * Returns true iff some shattering was done, with <code>myResiduals</code>
	 * destructively modified to contain new parfactors created from
	 * <code>this</code>, and <code>otherResiduals</code> destructively modified
	 * to contain new parfactors created from <code>other</code>.
	 */
	public boolean shatter(Parfactor other, int myTermIndex, int otherTermIndex,
			Collection<Parfactor> myResiduals, Collection<Parfactor> otherResiduals) {

		Term t1 = dimTerms.get(myTermIndex);
		Constraint c1 = constraint;

		Term t2 = other.dimTerms.get(otherTermIndex);
		Constraint c2 = other.constraint;

		Constraint.Overlap overlap = Constraint.getOverlap(t1, c1, t2, c2);
		// System.out.println("Overlap between " + t1 + " : " + c1
		// + " and " + t2 + " : " + c2 + " is " + overlap);
		if ((overlap == null) || overlap.isFull()) {
			return false;
		}

		LogicalVar cvar1 = null, cvar2 = null;
		if (t1 instanceof CountingTerm) {
			cvar1 = ((CountingTerm) t1).getCountVar();
		}
		if (t2 instanceof CountingTerm) {
			cvar2 = ((CountingTerm) t2).getCountVar();
		}

		separate(overlap.theta(), overlap.c2theta(), cvar1, myTermIndex,
				myResiduals);
		other.separate(overlap.theta(), overlap.c1theta(), cvar2, otherTermIndex,
				otherResiduals);
		return true;

	}

	public boolean shatter(int myTermIndex, Term groundTerm,
			Collection<Parfactor> residuals) {

		Term t1 = dimTerms.get(myTermIndex);
		Constraint c1 = constraint;

		Constraint.Overlap overlap = Constraint.getOverlap(t1, c1, groundTerm,
				Constraint.EMPTY);
		if ((overlap == null) || overlap.isFull()) {
			return false;
		}

		LogicalVar cvar1 = null;
		if (t1 instanceof CountingTerm) {
			cvar1 = ((CountingTerm) t1).getCountVar();
		}
		separate(overlap.theta(), overlap.c2theta(), cvar1, myTermIndex, residuals);

		return true;

	}

	// if cVar!=null, we're separating a counting term with count variable cVar
	public void separate(Substitution theta, Constraint c, LogicalVar cVar,
			int cIndex, Collection<Parfactor> residuals) {
		Parfactor p = this;

		// Split on each part of the substitution that applies to a
		// free variable in this parfactor (as opposed to a counting
		// variable or a variable from another parfactor).
		for (LogicalVar l : theta.getVars()) {
			if (logicalVars.contains(l)) {
				// System.out.println("**"+l+" : "+theta.getReplacement(l));
				Term t = theta.getReplacement(l);
				p = p.splitOn(l, t, residuals);
			}
		}

		// Split on each constraint that applies to a free variable in p.
		for (LogicalVar l : c.logicalVars()) {
			if (p.logicalVars().contains(l)) {
				p = p.splitOnConstraint(l, c.excluded(l), residuals);
			}
		}

		// Now expand cVar if necessary
		if (cVar != null) {
			Term t = theta.getReplacement(cVar);
			p = p.expandCount(cVar, t, cIndex, residuals);

			if ((t instanceof LogicalVar) && !p.logicalVars().contains(t)
					&& c.logicalVars().contains(t)) {
				// Note that since t is not one of p's quantified variables,
				// the expandCount call above just renamed the count variable.
				// So the index of the counting term is unchanged.
				cVar = (LogicalVar) t;
				p = p
						.expandCountOnConstraint(cVar, c.excluded(cVar), cIndex, residuals);
			}
		}

		residuals.add(p.getRenamedCopy());
	}

	public Parfactor splitOn(LogicalVar l, Term t, Collection<Parfactor> residuals) {
		// System.out.println("----Parfactor " + this);
		// System.out.println("----Splitting on: "+l+":"+t);

		Substitution theta = new Substitution();
		theta.add(l, t);
		if ((t instanceof LogicalVar) && !logicalVars.contains((LogicalVar) t)) {
			return applySubstitution(theta);
		}

		if (Util.verbose()) {
			System.out.println("Splitting " + this + " on " + l + "=" + t);
		}
		Parfactor p2 = copyWithConstraint(l, t);
		if (!p2.constraint().hasContradiction()) {
			// System.out.println("----Residual: " + p2);
			residuals.add(p2);
		}
		Parfactor p1 = applySubstitution(theta);
		// System.out.println("----Result: " + p1);
		// System.out.println("------------------");

		return p1;
	}

	public Set<Parfactor> splitOn(LogicalVar l, Term t) {
		Set<Parfactor> result = new HashSet<Parfactor>();
		if (constraint.excluded(l).contains(t)) {
			result.add(this);
			return result;
		}
		Substitution theta = new Substitution();
		theta.add(l, t);
		Parfactor p1 = applySubstitution(theta);
		result.add(p1);
		Parfactor p2 = copyWithConstraint(l, t);
		result.add(p2);
		return result;
	}

	public Parfactor splitOnConstraint(LogicalVar l,
			Set<? extends Term> excludedInOther, Collection<Parfactor> residuals) {
		// System.out.println("----Parfactor " + this);
		// System.out.println("----Splitting on: "+l+"!="+excludedInOther);
		if (Util.verbose()) {
			System.out.println("Splitting " + this + " on constraint " + l + " != "
					+ excludedInOther);
		}

		Set<? extends Term> excludedInThis = constraint.excluded(l);

		// The overlap of the two constraints excludes all terms that
		// are excluded by either of the two.
		Set<Term> excludedInOverlap = new LinkedHashSet<Term>(excludedInThis);
		excludedInOverlap.addAll(excludedInOther);
		Constraint overlap = constraint.replaceExcluded(l, excludedInOverlap);
		if (overlap.hasContradiction()) {
			throw new IllegalArgumentException(
					"Trying to split on constraint with no overlap.");
		}

		Parfactor p1 = new Parfactor(logicalVars, overlap, dimTerms, potential);
		// System.out.println("----Result: " + p1);

		// For each logical variable y that is in excludedInOther but
		// not excludedInThis, create a residual with l replaced by y.
		Set<Term> excludedInMainResidual = new LinkedHashSet<Term>(excludedInThis);
		for (Term term : excludedInOther) {
			if ((term instanceof LogicalVar) && !excludedInThis.contains(term)) {
				Substitution subst = new Substitution();
				subst.add(l, term);
				Parfactor residual = applySubstitution(subst);
				residuals.add(residual);
				// System.out.println("----Residual: " + residual);
				excludedInMainResidual.add(term);
			}
		}

		// If necessary, create the "main residual", which covers
		// constant symbols allowed by this constraint but excluded by
		// the other constraint. To avoid overlap with p1, the main
		// residual excludes constant symbols allowed by the other constraint.
		if (excludedInOverlap.size() > excludedInMainResidual.size()) {
			for (Object obj : l.getType().getGuaranteedObjects()) {
				Term term = l.getType().getCanonicalTerm(obj);
				if (!excludedInOther.contains(term)) {
					excludedInMainResidual.add(term);
				}
			}
			Constraint cResidual = constraint.replaceExcluded(l,
					excludedInMainResidual);
			Parfactor mainResidual = new Parfactor(logicalVars, cResidual, dimTerms,
					potential);
			// System.out.println("----Residual: " + mainResidual);
			residuals.add(mainResidual);
		}

		// System.out.println("------------------");
		return p1;
	}

	/**
	 * Creates a new parfactor where the counting term at countTermIndex #x .
	 * p(..,x,..) has been expanded so that x!=t. The dimension of the table in
	 * the resulting Parfactor is one larger than the dimension of the table in
	 * this Parfactor.
	 * 
	 * <p>
	 * If adding the constraint x!=t would leave the counting term with an
	 * unsatisfiable constraint, then the counting term is removed from the
	 * resulting parfactor, and the resulting parfactor's table is the same size
	 * as the original one.
	 */
	public Parfactor expandCount(LogicalVar cVar, Term t, int countTermIndex,
			Collection<Parfactor> residuals) {
		CountingTerm countTerm = (CountingTerm) dimTerms.get(countTermIndex);
		assert cVar == countTerm.getCountVar();

		// System.out.println("----Parfactor " + this);
		// System.out.println("----Expanding on: "+cVar+"="+t);

		if ((t instanceof LogicalVar) && !logicalVars.contains(t)) {
			// just need to rename the counting variable
			CountingTerm renamed = countTerm.renameCountingVar((LogicalVar) t);
			List<Term> newDimTerms = new ArrayList<Term>(dimTerms);
			newDimTerms.set(countTermIndex, renamed);
			Parfactor result = new Parfactor(logicalVars, constraint, newDimTerms,
					potential);
			// System.out.println("----Result: " + result);
			// System.out.println("------------------");
			return result;
		}

		// We actually need to do some expansion. See if we need to
		// split another variable first
		Set<? extends Term> cExcluded = countTerm.constraint().excluded(cVar);
		if (t instanceof LogicalVar) {
			LogicalVar x = (LogicalVar) t;
			if (constraint.allowsSomeAllowedBy(x, cExcluded)
					&& !constraint.allowsOnlyAllowedBy(x, cExcluded)) {
				// A counting term excluding x would not be in normal form.
				// System.out.println("----Need to split " + x + " first");
				return splitOnConstraint(x, cExcluded, residuals);
			}
		} else {
			for (ArgSpec exTerm : countTerm.constraint().excluded(cVar)) {
				if (exTerm instanceof LogicalVar) {
					LogicalVar x = (LogicalVar) exTerm;
					if (logicalVars.contains(x) && !constraint.excluded(x).contains(t)) {
						// If we expanded on cVar = t now, one of the resulting
						// counting terms would exclude both x and t, and
						// wouldn't be in normal form.
						// System.out.println("----Need to split " + x + " first");
						return splitOn(x, t, residuals);
					}
				}
			}
		}

		if (Util.verbose()) {
			System.out.println("In parfactor " + this + ", expanding " + countTerm
					+ " on " + cVar + "=" + t);
		}

		// make the new FuncAppTerm and new CountingTerm

		FuncAppTerm newFuncAppTerm = (FuncAppTerm) countTerm
				.replaceCountVarSubTerm(t);
		List<FuncAppTerm> newFuncApps = Collections.singletonList(newFuncAppTerm);

		List<CountingTerm> newCountingTerms = new ArrayList<CountingTerm>();
		Constraint newCountConstraint = countTerm.constraint().addConstraint(cVar,
				t);
		if (!newCountConstraint.hasContradiction()) {
			newCountConstraint = newCountConstraint.getSimplified(constraint);
			CountingTerm newCountTerm = new CountingTerm(countTerm.logicalVars(),
					newCountConstraint, countTerm.subTerms());
			newCountingTerms.add(newCountTerm);
		}

		Parfactor result = expandCountInto(countTermIndex, newCountingTerms,
				newFuncApps);
		// System.out.println("----Result: " + result);
		// System.out.println("------------------");

		return result;
	}

	/**
	 * Returns a parfactor where the counting term at index <code>cIndex</code>
	 * has been replaced with two counting terms, one covering only terms allowed
	 * by <code>excludedInOther</code>, and one covering only terms not allowed by
	 * <code>excludedInOther</code>. The second new counting term is omitted if
	 * all the terms not allowed by <code>excludedInOther</code> are already
	 * disallowed by the existing constraint. The method assumes that the first
	 * counting term is non-contradictory: that is, some terms allowed by
	 * <code>excludedInOther</code> are also allowed by the existing constraint.
	 */
	public Parfactor expandCountOnConstraint(LogicalVar l,
			Set<? extends Term> excludedInOther, int cIndex,
			Collection<Parfactor> residuals) {
		// System.out.println("----Parfactor: " + this);
		// System.out.println("----Expanding on " + l + "!=" + excludedInOther);

		CountingTerm origCountTerm = (CountingTerm) dimTerms.get(cIndex);
		Set<? extends Term> excludedInOrig = origCountTerm.constraint().excluded(l);

		// See if we need to split on another variable first
		for (ArgSpec exTerm : excludedInOrig) {
			if (exTerm instanceof LogicalVar) {
				LogicalVar x = (LogicalVar) exTerm;
				if (logicalVars.contains(x)
						&& constraint.allowsSomeAllowedBy(x, excludedInOther)
						&& !constraint.allowsOnlyAllowedBy(x, excludedInOther)) {
					// Expanding now might create a non-normal-form
					// counting constraint.
					// System.out.println("----Need to split " + x + " first");
					return splitOnConstraint(x, excludedInOther, residuals);
				}
			}
		}

		if (Util.verbose()) {
			System.out.println("In parfactor " + this + ", expanding "
					+ origCountTerm + " on constraint " + l + " != " + excludedInOther);
		}

		List<CountingTerm> newCountingTerms = new ArrayList<CountingTerm>();
		List<FuncAppTerm> newFuncApps = new ArrayList<FuncAppTerm>();

		Set<Term> excludedInOverlap = new LinkedHashSet<Term>(excludedInOrig);
		excludedInOverlap.addAll(excludedInOther);
		Constraint overlapConstraint = origCountTerm.constraint().replaceExcluded(
				l, excludedInOverlap);
		overlapConstraint = overlapConstraint.getSimplified(constraint);
		CountingTerm ct1 = new CountingTerm(origCountTerm.logicalVars(),
				overlapConstraint, origCountTerm.subTerms());
		newCountingTerms.add(ct1);

		// For each logical variable y that is in excludedInOther but not
		// excludedInOrig, create a FuncAppTerm with l replaced by y.
		Set<Term> excludedInMainResidual = new LinkedHashSet<Term>(excludedInOrig);
		for (Term term : excludedInOther) {
			if ((term instanceof LogicalVar) && !excludedInOrig.contains(term)) {
				Substitution subst = new Substitution();
				subst.add(l, term);
				newFuncApps.add((FuncAppTerm) origCountTerm.singleSubTerm()
						.getSubstResult(subst));
				excludedInMainResidual.add(term);
			}
		}

		// If necessary, create a "main residual" that covers constant
		// symbols allowed by excludedInOrig but not by excludedInOther.
		// To avoid overlap with ct1, the main residual excludes constant
		// symbols that are allowed by excludedInOther.
		if (excludedInOverlap.size() > excludedInMainResidual.size()) {
			for (Object obj : l.getType().getGuaranteedObjects()) {
				Term term = l.getType().getCanonicalTerm(obj);
				if (!excludedInOther.contains(term)) {
					// allowed by other, so we can safely exclude here
					excludedInMainResidual.add(term);
				}
			}

			Constraint residualConstraint = origCountTerm.constraint()
					.replaceExcluded(l, excludedInMainResidual);
			residualConstraint = residualConstraint.getSimplified(constraint);
			CountingTerm mainResidual = new CountingTerm(origCountTerm.logicalVars(),
					residualConstraint, origCountTerm.subTerms());
			mainResidual = mainResidual.renameCountingVar(LogicalVar.createVar(l
					.getType()));
			newCountingTerms.add(mainResidual);
		}

		Parfactor result = expandCountInto(cIndex, newCountingTerms, newFuncApps);
		// System.out.println("----Result: " + result);
		// System.out.println("------------------");
		return result;
	}

	/**
	 * Returns a version of this parfactor where the counting term at index
	 * <code>cIndex</code> has been replaced with the given counting terms and
	 * function applications. This method assumes that the random variable sets of
	 * the given terms form a partition of the random variables covered by the
	 * original term.
	 */
	public Parfactor expandCountInto(int cIndex,
			List<? extends CountingTerm> newCountingTerms,
			List<? extends FuncAppTerm> newFuncApps) {
		// Assemble the new list of dimTerms and record some indices
		List<Term> newArgTerms = new ArrayList<Term>();
		for (int i = 0; i < cIndex; ++i) {
			newArgTerms.add(dimTerms.get(i));
		}
		newArgTerms.addAll(newCountingTerms);
		int afterNewCountingTerms = newArgTerms.size();
		newArgTerms.addAll(newFuncApps);
		int afterNewFuncApps = newArgTerms.size();
		for (int i = cIndex + 1; i < dimTerms.size(); ++i) {
			newArgTerms.add(dimTerms.get(i));
		}

		// Get types and ranges of new dimTerms
		List<Type> newTermTypes = new ArrayList<Type>();
		List<List<Object>> newTermRanges = new ArrayList<List<Object>>();
		for (Term term : newArgTerms) {
			Type type = term.getType();
			newTermTypes.add(type);
			newTermRanges.add(type.range());
		}

		// Create array for rebuilding original histograms
		Type countedType = ((CountingTerm) dimTerms.get(cIndex)).singleSubTerm()
				.getType();
		List<Object> countedTypeRange = countedType.range();
		int[] counts = new int[countedTypeRange.size()];

		// Make the new potential by iterating through all of the new
		// indices and looking up the value in the old potential.
		Potential newPotential = new MultiArrayPotential(newTermTypes);
		for (List newInd : new CartesianProduct(newTermRanges)) {
			List<Object> oldInd = new ArrayList<Object>();
			ListIterator<Object> newArgIter = newInd.listIterator();

			while (newArgIter.nextIndex() < cIndex) {
				oldInd.add(newArgIter.next());
			}

			// Assemble the original histogram and put it in old arg tuple
			Arrays.fill(counts, 0);
			while (newArgIter.nextIndex() < afterNewCountingTerms) {
				Histogram h = (Histogram) newArgIter.next();
				for (int i = 0; i < counts.length; ++i) {
					counts[i] += h.getCount(i);
				}
			}
			while (newArgIter.nextIndex() < afterNewFuncApps) {
				int index = countedTypeRange.indexOf(newArgIter.next());
				++(counts[index]);
			}
			oldInd.add(new Histogram(counts));

			while (newArgIter.hasNext()) {
				oldInd.add(newArgIter.next());
			}

			newPotential.setValue(newInd, potential.getValue(oldInd));
		}

		// return the new Parfactor
		Parfactor result = new Parfactor(logicalVars, constraint, newArgTerms,
				newPotential);
		return result;
	}

	public Set<Parfactor> makeConstraintsNormalForm() {
		Set<Parfactor> result = new HashSet<Parfactor>();
		Set<Parfactor> toSplit = new HashSet<Parfactor>();
		Term[] violation = constraint.getNormalFormViolation();
		Parfactor current = null;
		if (violation != null) {
			current = this;
		}

		while (violation != null) {
			// do this split for the current one
			// System.out.println(toSplit+" : "+current+" : "+violation);
			toSplit.addAll(current.splitOn((LogicalVar) violation[0], violation[1]));

			// find another one that need to be split
			Iterator<Parfactor> i = toSplit.iterator();
			violation = null;
			while (violation == null && i.hasNext()) {
				Parfactor p = i.next();
				violation = p.constraint().getNormalFormViolation();
				if (violation == null) {
					result.add(p);
				} else {
					current = p;
				}
				i.remove();
			}
		}

		if (result.size() == 0) {
			result.add(this);
			return result;
		} else {
			return result;
		}

	}

	// this is definitely something we could precompute and cache...
	public Set<LogicalVar> getUnusedVars() {
		Set<LogicalVar> vs = new HashSet<LogicalVar>(logicalVars);
		for (ArgSpec t : dimTerms) {
			vs.removeAll(t.getFreeVars());
		}
		return vs;
	}

	/**
	 * Creates a new Parfactor where x has been removed and all of the entries in
	 * the table are raised to the power of the number of groundings of x. Assumes
	 * x doesn't appear in any of the terms for this Parfactor. Also, drops x from
	 * the Constraint.
	 */

	public Parfactor exponentiate(LogicalVar x) {
		List<LogicalVar> vars = new ArrayList<LogicalVar>(logicalVars);
		vars.remove(x);
		Potential newP = potential.copy();
		newP.pow(constraint.numConstrainedGroundings(x));

		return new Parfactor(vars, constraint.dropVar(x), dimTerms, newP);
	}

	/**
	 * Creates a new Parfactor where the Term (FuncAppTerm or CountingTerm) at
	 * termIndex has been summed out.
	 */
	public Parfactor sumOut(int termIndex) {
		ArgSpec sumTerm = dimTerms.get(termIndex);

		// update the dimTerms/counting terms
		List<Term> newAs = new ArrayList<Term>(dimTerms);
		newAs.remove(termIndex);

		// make the new potential
		Potential newP = null;
		if (sumTerm instanceof FuncAppTerm) {
			// sum out a single index
			List<Integer> indices = new ArrayList<Integer>(1);
			indices.add(new Integer(termIndex));

			// do the sum out
			newP = potential.sumOut(indices);

		} else if (sumTerm instanceof CountingTerm) { // must be a CountingTerm
			CountingTerm countTerm = (CountingTerm) sumTerm;

			List<Type> newTypes = new ArrayList<Type>(dimTerms.size());
			for (Term a : dimTerms) {
				newTypes.add(a.getType());
			}
			newTypes.remove(termIndex);

			// System.out.println("countIndex: "+countIndex);

			// make the new potential
			newP = new MultiArrayPotential(newTypes);

			// fill the entries
			List<Collection<Object>> atomRanges = new ArrayList<Collection<Object>>();
			for (Term f : dimTerms) {
				atomRanges.add(f.getType().range());
			}
			TupleIterator outerIter = new TupleIterator(atomRanges);
			// System.out.println("atomRanges: "+atomRanges);

			while (outerIter.hasNext()) {
				List fullInd = (List) outerIter.next();
				List newInd = new LinkedList(fullInd);
				newInd.remove(termIndex);
				// System.out.println("fullInd: "+fullInd);
				// System.out.println("h: "+vals.get(countIndex));
				Histogram countVal = (Histogram) fullInd.get(termIndex);
				double mult = countTerm.getNumJointInstsYieldingHist(countVal);
				// System.out.println(countVal+" : "+mult);
				newP.setValue(newInd,
						newP.getValue(newInd) + potential.getValue(fullInd) * mult);
			}
		} else {
			System.err.println("In Parfactor.sumOut:");
			System.err.println(sumTerm + " is not a FuncAppTerm or a CountingTerm");
			System.exit(-1);
		}

		// return the new parfactor
		return new Parfactor(logicalVars, constraint, newAs, newP);
	}

	/**
	 * Multiplies this Parfactor with other. Assumes that they have the same set
	 * of instances of LogicalVar objects and we can use direct equality on the
	 * Terms to see where they line up. Assumes that the set of constraints are
	 * the same for both Parfactors. Also assumes that the same Term is never used
	 * twice to index into a single Parfactor.
	 */
	public Parfactor multiply(Parfactor other) {
		// Terms in product will be terms in this parfactor, followed by
		// those terms in the other parfactor that don't occur in this one.
		List<Term> newAs = new ArrayList<Term>(dimTerms);
		int[] otherIndicesInProduct = new int[other.dimTerms.size()];
		int otherIndex = 0;
		for (Term term : other.dimTerms) {
			int indexInThis = dimTerms.indexOf(term);
			if (indexInThis == -1) {
				otherIndicesInProduct[otherIndex++] = newAs.size();
				newAs.add(term);
			} else {
				otherIndicesInProduct[otherIndex++] = indexInThis;
			}
		}
		DimMapping otherProductMap = new DimMapping(otherIndicesInProduct);

		// make the new potential
		Potential newP = potential.multiply(other.potential, otherProductMap);

		// make the new Parfactor
		return new Parfactor(logicalVars, constraint, newAs, newP);

	}

	/**
	 * Multiplies together the Parfactors in parFactors. Assumes that they all
	 * have the same set of instances of LogicalVar objects and we can use direct
	 * equality on the Terms to see where they line up.
	 */
	static public Parfactor multiply(List<Parfactor> inputs) {
		List<? extends LogicalVar> logicalVars = Collections.emptyList();
		Constraint constraint = Constraint.EMPTY;
		if (!inputs.isEmpty()) {
			Parfactor pf = inputs.get(0);
			logicalVars = pf.logicalVars();
			constraint = pf.constraint();
		}

		List<Term> productTerms = new ArrayList<Term>();
		Map<Term, Integer> productTermIndices = new HashMap<Term, Integer>();
		List<Type> productTermTypes = new ArrayList<Type>();

		List<Potential> potentials = new ArrayList<Potential>(inputs.size());
		DimMapping[] dimMappings = new DimMapping[inputs.size()];
		for (int i = 0; i < inputs.size(); ++i) {
			Parfactor pf = inputs.get(i);
			potentials.add(pf.potential());

			List<? extends Term> inputTerms = pf.dimTerms();
			int[] indicesInProduct = new int[inputTerms.size()];
			for (int j = 0; j < inputTerms.size(); ++j) {
				Term term = inputTerms.get(j);
				Integer indexInProduct = productTermIndices.get(term);
				if (indexInProduct == null) {
					indexInProduct = new Integer(productTerms.size());
					productTerms.add(term);
					productTermIndices.put(term, indexInProduct);
					productTermTypes.add(term.getType());
				}
				indicesInProduct[j] = indexInProduct.intValue();
			}
			dimMappings[i] = new DimMapping(indicesInProduct);
		}

		Potential productPot = MultiArrayPotential.multiply(potentials,
				productTermTypes, dimMappings);

		return new Parfactor(logicalVars, constraint, productTerms, productPot);
	}

	/**
	 * Determines if this parfactor and the given one can be merged to form one
	 * parfactor that defines the same weighting function. If so, returns the
	 * merged parfactor; otherwise returns null.
	 * 
	 * <p>
	 * This implementation begins by checking that the potentials of the two
	 * parfactors have the same dimensions and differ by at most Util.TOLERANCE in
	 * each entry. Then it checks that both parfactors can be viewed as
	 * constrained versions of a single original parfactor, where one logical
	 * variable is constrained to have disjoint sets of allowed values in the two
	 * parfactors. In one or both of the given parfactors, the original logical
	 * variable may be replaced with a constant symbol, which is treated the same
	 * way as a constraint with just one allowed value.
	 * 
	 * <p>
	 * TODO: Detect the possibility of merging in cases where the original logical
	 * variable is constrained to be not-equal to another logical variable in one
	 * of the given parfactors.
	 */
	public Parfactor getMerged(Parfactor other) {
		if (!potential.withinTol(other.potential())) {
			return null;
		}

		List<? extends Term> otherArgTerms = other.dimTerms();
		if (dimTerms.size() != otherArgTerms.size()) {
			return null;
		}

		List<Term> newArgTerms = new ArrayList<Term>();
		Set<LogicalVar> newLogVars = new LinkedHashSet<LogicalVar>();
		Substitution thisSubst = new Substitution();
		Substitution otherSubst = new Substitution();
		List<LogicalVar> splitVars = new ArrayList<LogicalVar>();
		for (int i = 0; i < dimTerms.size(); ++i) {
			Term newAtom = mergeTopLevelTerms(dimTerms.get(i), otherArgTerms.get(i),
					thisSubst, otherSubst, splitVars);
			if (newAtom == null) {
				return null;
			}
			newArgTerms.add(newAtom);
			newLogVars.addAll(newAtom.getFreeVars());
		}

		Map<LogicalVar, Set<Term>> newExcluded = new HashMap<LogicalVar, Set<Term>>();
		for (LogicalVar x : newLogVars) {
			Set<Term> excluded = mergeExcludedSets(x, constraint, other.constraint(),
					thisSubst, otherSubst, splitVars);
			if (excluded == null) {
				return null;
			}
			newExcluded.put(x, excluded);
		}

		if (splitVars.isEmpty()) {
			// Never split on a variable, so parfactors are equivalent.
			// Could just square the potential; for now we'll return null.
			return null;
		}

		return new Parfactor(new ArrayList<LogicalVar>(newLogVars), new Constraint(
				newExcluded), newArgTerms, potential);
	}

	/**
	 * Returns true if the given parfactor's arguments have the same basic form as
	 * this one's. Specifically, this method returns true if the given parfactor
	 * has the same number of arguments as this one, and for each position in the
	 * argument list, either:
	 * <ul>
	 * <li>both parfactors have function application terms with the same function
	 * symbol; or
	 * <li>both parfactors have counting terms whose sub-terms have the same
	 * function symbols.
	 * </ul>
	 */
	public boolean sameArgSkeleton(Parfactor other) {
		List<? extends Term> otherTerms = other.dimTerms();
		if (dimTerms.size() != otherTerms.size()) {
			return false;
		}

		for (int i = 0; i < dimTerms.size(); ++i) {
			ArgSpec t1 = dimTerms.get(i);
			ArgSpec t2 = otherTerms.get(i);
			if ((t1 instanceof FuncAppTerm) && (t2 instanceof FuncAppTerm)) {
				if (((FuncAppTerm) t1).getFunction() != ((FuncAppTerm) t2)
						.getFunction()) {
					return false;
				}
			} else if ((t1 instanceof CountingTerm) && (t2 instanceof CountingTerm)) {
				List<? extends Term> args1 = ((CountingTerm) t1).subTerms();
				List<? extends Term> args2 = ((CountingTerm) t2).subTerms();
				if (args1.size() != args2.size()) {
					return false;
				}
				for (int j = 0; j < args1.size(); ++j) {
					if (((FuncAppTerm) args1.get(j)).getFunction() != ((FuncAppTerm) args2
							.get(j)).getFunction()) {
						return false;
					}
				}
			} else {
				// two different Term subclasses
				return false;
			}
		}

		return true;
	}

	/**
	 * Returns the excluded set to use for <code>forVar</code> in a parfactor
	 * obtained by merging two parfactors with the constraints <code>c1</code> and
	 * <code>c2</code>. This function looks at the excluded set for
	 * <code>subst1(forVar)</code> in <code>c1</code> and the excluded set for
	 * <code>subst2(forVar)</code> in <code>c2</code>. If
	 * <code>subst1(forVar)</code> or <code>subst2(forVar)</code> is a constant
	 * symbol, its excluded set is treated as consisting of all the other
	 * canonical constant symbols for the relevant type. The inverse of
	 * <code>subst1</code> is applied to the first excluded set, and the inverse
	 * of <code>subst2</code> is applied to the second excluded set.
	 * 
	 * <p>
	 * If the resulting excluded sets are the same, this function returns that
	 * set. If the excluded sets consist just of constant symbols and have
	 * disjoint complements, and <code>splitVars</code> is empty, then the
	 * function adds <code>forVar</code> to <code>splitVars</code> and returns the
	 * intersection of the two excluded sets. Otherwise, the function returns
	 * null.
	 * 
	 * <p>
	 * Taking the intersection of excluded sets that contain logical variables
	 * would be problematic because the resulting constraint might not be in
	 * normal form. We can't merge perfactors while maintain normal form in
	 * general, because we can't split while maintaining normal form. For
	 * instance, if we have R(x, y), x != y and we split on x=c, we end up with
	 * R(c, y), y != c and R(x, y), x != {c, y}. To obtain normal form, this
	 * second parfactor is split into R(x, c), x != c and R(x, y), x != {c, y}, y
	 * != {c, x}. Merging these last two parfactors would result in a
	 * non-normal-form constraint.
	 */
	private static Set<Term> mergeExcludedSets(LogicalVar forVar, Constraint c1,
			Constraint c2, Substitution subst1, Substitution subst2,
			List<LogicalVar> splitVars) {
		Term t1 = subst1.getReplacement(forVar);
		Term t2 = subst2.getReplacement(forVar);

		if (t1 instanceof FuncAppTerm) {
			if (t2 instanceof FuncAppTerm) {
				// two constants -- exclude everything but them
				Set<Term> excluded = new LinkedHashSet<Term>();
				Type type = forVar.getType();
				for (Object obj : type.getGuaranteedObjects()) {
					Term t = type.getCanonicalTerm(obj);
					if ((!t.equals(t1)) && (!t.equals(t2))) {
						excluded.add(t);
					}
				}
				return excluded;
			}

			// t2 is logical variable
			return reduceExcludedSet(((LogicalVar) t2), c2, t1);
		}
		if (t2 instanceof FuncAppTerm) {
			// t1 is logical variable
			return reduceExcludedSet((LogicalVar) t1, c1, t2);
		}

		// t1 and t2 are both logical variables
		LogicalVar x1 = (LogicalVar) t1;
		LogicalVar x2 = (LogicalVar) t2;

		Set<Term> excluded1 = excludedForMerge(c1.excluded(x1), subst1);
		Set<Term> excluded2 = excludedForMerge(c2.excluded(x2), subst2);
		if (excluded1.equals(excluded2)) {
			return excluded1; // same excluded set
		}

		// x1 and x2 have different excluded sets. See if we can
		// make forVar the split variable. This is possible if neither
		// x1 nor x2 excludes any logical variables, and the sets of
		// constants they allow are disjoint. To check disjointness,
		// we check that x1 excludes all the constants that x1 includes:
		// in other words, the union of their excluded sets is the whole
		// set of constants.
		if (splitVars.isEmpty()
				&& (excluded1.size() == c1.excludedConstants(x1).size())
				&& (excluded2.size() == c2.excludedConstants(x2).size())) {
			Set<Term> union = new HashSet<Term>(excluded1);
			union.addAll(excluded2);
			if (union.size() == forVar.getType().getGuaranteedObjects().size()) {
				// allowed sets are disjoint

				splitVars.add(forVar);
				Set<Term> excluded = new LinkedHashSet<Term>(excluded1);
				excluded.retainAll(excluded2);
				return excluded;
			}
		}

		return null;
	}

	private static Set<Term> reduceExcludedSet(LogicalVar x, Constraint c,
			Term toInclude) {
		Set<Term> excluded = new LinkedHashSet<Term>(c.excluded(x));
		if (excluded.size() != c.excludedConstants(x).size()) {
			// Can't merge when split variable has inequalities with
			// other variables.
			return null;
		}
		if (!excluded.remove(toInclude)) {
			return null; // c already allowed toInclude, so parfactors overlap
		}
		return excluded;
	}

	private static Set<Term> excludedForMerge(Set<? extends Term> excluded,
			Substitution subst) {
		Set<Term> newExcluded = new LinkedHashSet<Term>();
		for (Term t : excluded) {
			if (t instanceof LogicalVar) {
				Set<LogicalVar> preimage = subst.getPreimage(t, false);
				if (preimage.size() != 1) {
					throw new IllegalStateException(
							"In merge, preimage of logical variable " + t + " has size "
									+ preimage.size() + " (should be 1).");
				}
				t = preimage.iterator().next(); // unique element of preimage
			}
			newExcluded.add(t);
		}
		return newExcluded;
	}

	/**
	 * Tries to construct a term <code>tNew</code> and extend the substitutions
	 * <code>subst1</code> and <code>subst2</code> so that
	 * <code>subst1(tNew) = t1</code> and <code>subst2(tNew) = t2</code>. Each
	 * substitution must be one-to-one. There is allowed to be at most one logical
	 * variable in <code>tNew</code> that is "split": that is, it maps to a
	 * constant in one of the substitutions (and either a a constant or a variable
	 * in the other substitution). This split variable is stored in
	 * <code>splitVars</code>. The other variables must map to variables in both
	 * substitutions.
	 * 
	 * <p>
	 * Note: the restriction that substitutions must be one-to-one rules out some
	 * cases with inequalities. For instance, we'd like to be able to merge R(x,
	 * x) and R(x, y), x!=y to get the unconstrained term R(z, w). To do this,
	 * we'd need subst1 to be [z/x, w/x], which is not one-to-one.
	 * 
	 * @return <code>tNew</code> if it exists; otherwise null
	 */
	private static Term mergeTopLevelTerms(ArgSpec t1, ArgSpec t2,
			Substitution subst1, Substitution subst2, List<LogicalVar> splitVars) {
		if ((t1 instanceof CountingTerm) && (t2 instanceof CountingTerm)) {
			CountingTerm ct1 = (CountingTerm) t1;
			CountingTerm ct2 = (CountingTerm) t2;

			List<LogicalVar> newLogVars = new ArrayList<LogicalVar>();
			List<? extends LogicalVar> logVars1 = ct1.logicalVars();
			List<? extends LogicalVar> logVars2 = ct2.logicalVars();
			if (logVars1.size() != logVars2.size()) {
				return null;
			}
			for (int i = 0; i < logVars1.size(); ++i) {
				LogicalVar x1 = logVars1.get(i);
				LogicalVar x2 = logVars2.get(i);
				if (x1.getType() != x2.getType()) {
					return null;
				}
				LogicalVar xNew = LogicalVar.createVar(x1.getType());
				subst1.add(xNew, x1);
				subst2.add(xNew, x2);
			}

			List<Term> newSubTerms = new ArrayList<Term>();
			List<? extends Term> subTerms1 = ct1.subTerms();
			List<? extends Term> subTerms2 = ct2.subTerms();
			if (subTerms1.size() != subTerms2.size()) {
				return null;
			}
			for (int i = 0; i < subTerms1.size(); ++i) {
				Term merged = mergeTopLevelTerms(subTerms1.get(i), subTerms2.get(i),
						subst1, subst2, splitVars);
				if (merged == null) {
					return null;
				}
				newSubTerms.add(merged);
			}

			for (LogicalVar x : newLogVars) {
				subst1.remove(x);
				subst2.remove(x);
			}

			return new CountingTerm(newLogVars, newSubTerms);
		}

		if ((t1 instanceof FuncAppTerm) && (t2 instanceof FuncAppTerm)) {
			FuncAppTerm ft1 = (FuncAppTerm) t1;
			FuncAppTerm ft2 = (FuncAppTerm) t2;

			if (ft1.getFunction() != ft2.getFunction()) {
				return null;
			}

			List<Term> newArgs = new ArrayList<Term>();
			Type[] argTypes = ft1.getFunction().getArgTypes();
			Term[] args1 = ft1.getArgs();
			Term[] args2 = ft2.getArgs();
			for (int i = 0; i < argTypes.length; ++i) {
				Term merged = mergeTypedTerms(argTypes[i], args1[i], args2[i], subst1,
						subst2, splitVars);
				if (merged == null) {
					return null;
				}
				newArgs.add(merged);
			}

			return new FuncAppTerm(ft1.getFunction(), newArgs);
		}

		return null; // different kinds of terms
	}

	private static Term mergeTypedTerms(Type type, Term t1, Term t2,
			Substitution subst1, Substitution subst2, List<LogicalVar> splitVars) {
		if (t1 instanceof FuncAppTerm) {
			if (((FuncAppTerm) t1).getArgs().length > 0) {
				return mergeTopLevelTerms(t1, t2, subst1, subst2, splitVars);
			}
			if (t1.equals(t2)) {
				// same constant; don't need to do anything
				return t1;
			}
		}

		// t1 and t2 are constants or logical variables

		Set<LogicalVar> preimage1 = subst1.getPreimage(t1, false);
		Set<LogicalVar> preimage2 = subst2.getPreimage(t2, false);
		if (!preimage1.equals(preimage2)) {
			// One of the preimages must be non-empty, so can't add
			// variable that maps to t1 and t2 without violating
			// one-to-oneness condition.
			return null;
		}

		if (preimage1.isEmpty()) {
			// Can introduce new variable that maps to t1 and t2
			LogicalVar xNew = LogicalVar.createVar(type);

			if ((t1 instanceof FuncAppTerm) || (t2 instanceof FuncAppTerm)) {
				// xNew is being split
				if (!splitVars.isEmpty()) {
					return null; // can't split more than one variable
				}
				splitVars.add(xNew);
			}

			subst1.add(xNew, t1);
			subst2.add(xNew, t2);
			return xNew;
		}

		LogicalVar x = preimage1.iterator().next();
		return x;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append(dimTerms);
		buf.append(" : ");
		if (constraint == null) {
			buf.append(fConstraint);
		} else {
			buf.append(constraint);
		}
		return buf.toString();
	}

	/**
	 * Inner class representing an index in this parfactor's term list.
	 */
	public class TermPtr {
		private TermPtr(int index) {
			this.index = index;
		}

		public Parfactor parfactor() {
			return Parfactor.this;
		}

		public int index() {
			return index;
		}

		public Term term() {
			return dimTerms.get(index);
		}

		public boolean equals(Object o) {
			if (o instanceof Parfactor.TermPtr) {
				Parfactor.TermPtr other = (Parfactor.TermPtr) o;
				return ((Parfactor.this == other.parfactor()) && (index == other
						.index()));
			}
			return false;
		}

		public int hashCode() {
			return (Parfactor.this.hashCode() + index);
		}

		public String toString() {
			return (Parfactor.this.toString() + ":" + index);
		}

		int index;
	}

	/**
	 * Returns a parfactor that assigns weight one to the given tuple of values
	 * for the arguments, and zero to all other tuples.
	 */
	public static Parfactor delta(List<LogicalVar> logicalVars,
			Constraint constraint, List<? extends Term> dimTerms, List<?> argValues) {
		List<Type> dims = new ArrayList<Type>();
		for (Term term : dimTerms) {
			dims.add(term.getType());
		}
		Potential pot = new MultiArrayPotential(dims, 0.0);
		pot.setValue(argValues, 1.0);
		return new Parfactor(logicalVars, constraint, dimTerms, pot);
	}

	/**
	 * Returns a set of parfactors that are equivalent to the dependency statement
	 * for the given random function. If the function has no dependency statement,
	 * returns an empty set.
	 */
	public static Set<Parfactor> createForCPD(RandomFunction rf) {
		Map<LogicalVar, Object> a = Collections.emptyMap();
		return createForCPD(rf, a, false);
	}

	/**
	 * Returns a set of parfactors that are equivalent to the dependency statement
	 * for the given random function. If the function has no dependency statement,
	 * returns an empty set.
	 * 
	 * @param a
	 *          assignment of values to a subset of the logical variables that
	 *          stand for <code>rf</code>'s arguments. The constructed parfactors
	 *          cover only those random variables consistent with this assignment.
	 */
	public static Set<Parfactor> createForCPD(RandomFunction rf,
			Map<LogicalVar, Object> a) {
		return createForCPD(rf, a, false);
	}

	/**
	 * Returns a set of parfactors that are equivalent to the dependency statement
	 * for the given random function. If the function has no dependency statement,
	 * returns an empty set.
	 * 
	 * @param a
	 *          assignment of values to a subset of the logical variables that
	 *          stand for <code>rf</code>'s arguments. The constructed parfactors
	 *          cover only those random variables consistent with this assignment.
	 * 
	 * @param useFuncValues
	 *          If true, assumes that <code>rf</code> is a real-valued function
	 *          with a deterministic dependency model. Returns a set of parfactors
	 *          that are equivalent to this function when treated additively.
	 */
	protected static Set<Parfactor> createForCPD(RandomFunction rf,
			Map<LogicalVar, Object> a, boolean useFuncValues) {
		// System.out.println("Creating parfactors for " + rf + "...");

		DependencyModel depModel = rf.getDepModel();
		if (depModel == null) {
			// no dependency statement for this function
			return Collections.emptySet();
		}

		// See which variables we're supposed to generalize over.
		LogicalVar[] argVars = rf.getArgVars();
		Set<LogicalVar> varsToLift = new LinkedHashSet<LogicalVar>();
		for (int i = 0; i < argVars.length; ++i) {
			LogicalVar var = argVars[i];
			if (a.containsKey(var)) {
				continue;
			}

			Type type = var.getType();
			if (!type.getPOPs().isEmpty()) {
				Util.fatalErrorWithoutStack("Can't create factors for function " + rf
						+ " generalizing over type " + type
						+ ", which has unknown objects.");
			}
			if (!type.hasFiniteGuaranteed()) {
				Util.fatalErrorWithoutStack("Can't create factors for function " + rf
						+ " generalizing over type " + type
						+ ", which has infinitely many objects.");
			}
			varsToLift.add(var);
		}

		// For which variables will we have to iterate over all possible
		// objects?
		removeNonLiftableVars(depModel, varsToLift);
		List<LogicalVar> iterationVars = new ArrayList<LogicalVar>();
		List<List<?>> iterationDomains = new ArrayList<List<?>>();
		for (int i = 0; i < argVars.length; ++i) {
			LogicalVar var = argVars[i];
			if (!a.containsKey(var) && !varsToLift.contains(var)) {
				// System.out.println("\tWill iterate over values for " + var);
				iterationVars.add(var);
				iterationDomains.add(var.getType().getGuaranteedObjects());
			}
		}

		// Create parfactors.
		List<LogicalVar> pfVars = new ArrayList<LogicalVar>(varsToLift);
		Set<Parfactor> pfs = new LinkedHashSet<Parfactor>();
		a = new HashMap<LogicalVar, Object>(a);
		for (List<?> binding : new CartesianProduct(iterationDomains)) {
			for (int i = 0; i < iterationVars.size(); ++i) {
				a.put(iterationVars.get(i), binding.get(i));
			}
			pfs.add(createOneParfactorForCPD(rf, a, pfVars, useFuncValues));
		}

		return pfs;
	}

	/**
	 * Returns a collection of parfactors that (when treated additively) represent
	 * the given deterministic, real-valued function. If the function has no
	 * dependency statement, returns an empty set.
	 */
	public static Collection<Parfactor> createForFuncValues(RandomFunction rf) {
		Map<LogicalVar, Object> a = Collections.emptyMap();
		return createForCPD(rf, a, true);
	}

	/**
	 * Returns a collection of parfactors that (when treated additively) represent
	 * the given deterministic, real-valued function. If the function has no
	 * dependency statement, returns an empty set.
	 * 
	 * @param a
	 *          assignment of values to a subset of the logical variables that
	 *          stand for <code>rf</code>'s arguments. The constructed parfactors
	 *          cover only those random variables consistent with this assignment.
	 */
	public static Collection<Parfactor> createForFuncValues(RandomFunction rf,
			Map<LogicalVar, Object> a) {
		return createForCPD(rf, a, true);
	}

	private static Parfactor createOneParfactorForCPD(RandomFunction rf,
			Map<LogicalVar, Object> a, List<LogicalVar> pfVars, boolean useFuncValues) {
		LogicalVar[] argVars = rf.getArgVars();

		// If we're not creating a parfactor for function values, create
		// a child atom.
		FuncAppTerm childAtom = null;
		if (!useFuncValues) {
			List<Term> args = new ArrayList<Term>();
			for (int i = 0; i < argVars.length; ++i) {
				LogicalVar var = argVars[i];
				Object val = a.get(var);
				if (val == null) {
					args.add(var);
				} else {
					Term argTerm = var.getType().getCanonicalTerm(val);
					if (argTerm == null) {
						throw new IllegalArgumentException("No canonical term for object "
								+ val + " of type " + var.getType());
					}
					args.add(argTerm);
				}
			}
			childAtom = new FuncAppTerm(rf, args);
		}

		// Create decision tree
		LiftedDecisionTree tree = LiftedDecisionTree
				.createForFunc(rf, a, childAtom);

		// Create table corresponding to decision tree
		List<Term> pfTerms = tree.getSplitLabels();
		if (childAtom != null) {
			// move childAtom to end
			pfTerms.remove(childAtom);
			pfTerms.add(childAtom);
		}
		Potential pot = tree.getMultiArray(pfTerms);

		// Create parfactor
		return new Parfactor(pfVars, pfTerms, pot);
	}

	/**
	 * Removes from varsToCheck those logical variables that are not "liftable". A
	 * variable is not liftable if it occurs as an argument to a nonrandom
	 * function, or somewhere where it is not an argument to any function (for
	 * instance, as an argument in an equality formula or an element in a set
	 * expression).
	 * 
	 * @return true if varsToCheck has been made empty; otherwise false.
	 */
	private static boolean removeNonLiftableVars(DependencyModel depModel,
			Set<LogicalVar> varsToCheck) {
		for (Iterator iter = depModel.getClauseList().iterator(); iter.hasNext();) {
			Clause clause = (Clause) iter.next();
			if (removeNonLiftableVars(clause, varsToCheck)) {
				return true;
			}
		}
		return false;
	}

	private static boolean removeNonLiftableVars(Clause clause,
			Set<LogicalVar> varsToCheck) {
		if (removeNonLiftableVars(clause.getCond(), varsToCheck)) {
			return true;
		}

		for (Iterator iter = clause.getArgs().iterator(); iter.hasNext();) {
			ArgSpec cpdArg = (ArgSpec) iter.next();
			if (removeNonLiftableVars(clause.getCond(), varsToCheck)) {
				return true;
			}
		}

		return false;
	}

	private static boolean removeNonLiftableVars(ArgSpec argSpec,
			Set<LogicalVar> varsToCheck) {
		if (argSpec instanceof LogicalVar) {
			// this variable is not liftable
			varsToCheck.remove(argSpec);
			return varsToCheck.isEmpty();
		}

		if (argSpec instanceof FuncAppTerm) {
			FuncAppTerm funcApp = (FuncAppTerm) argSpec;
			if (!(funcApp.getFunction() instanceof NonRandomFunction)) {
				// Variables that are arguments to this func app are still
				// liftable. Just recurse on arguments that are not
				// logical variables.
				Term[] args = funcApp.getArgs();
				for (int i = 0; i < args.length; ++i) {
					Term arg = args[i];
					if (!(arg instanceof LogicalVar)) {
						if (removeNonLiftableVars(arg, varsToCheck)) {
							return true;
						}
					}
				}
				return false;
			}
		}

		// Default case: recurse on all sub-expressions
		for (Iterator iter = argSpec.getSubExprs().iterator(); iter.hasNext();) {
			ArgSpec subExpr = (ArgSpec) iter.next();
			if (removeNonLiftableVars(subExpr, varsToCheck)) {
				return true;
			}
		}
		return false;
	}

	protected List<LogicalVar> logicalVars;
	protected Constraint constraint;
	protected List<Term> dimTerms;
	protected Potential potential; // indexed by types of dimTerms

	protected Formula fConstraint; // used until compile() is called
	protected Class potentialClass;
	protected List<? extends ArgSpec> potentialParams;

	private static String DEFAULT_LOCATION = "(no location)";

	private Object location = DEFAULT_LOCATION;
}
