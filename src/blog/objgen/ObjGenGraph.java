/*
 * Copyright (c) 2005, 2006, Regents of the University of California
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

import blog.common.AbstractDGraph;
import blog.common.DGraph;
import blog.common.Util;
import blog.model.ArgSpec;
import blog.model.AtomicFormula;
import blog.model.BuiltInFunctions;
import blog.model.BuiltInTypes;
import blog.model.EqualityFormula;
import blog.model.Formula;
import blog.model.FuncAppTerm;
import blog.model.Function;
import blog.model.LogicalVar;
import blog.model.Model;
import blog.model.NegFormula;
import blog.model.OriginFunction;
import blog.model.POP;
import blog.model.Term;
import blog.model.Type;
import blog.sample.EvalContext;

/**
 * An object generation graph contains nodes that represent sets of objects with
 * certain generation histories. A node in an object generation graph is like a
 * formula with a free variable: given a particular world and a particular
 * assignment of values to other variables, we can say whether any given object
 * <em>satisfies</em> the node. An object <i>o</i> satisfies a node <i>v</i>
 * given a world <i>w</i> and a variable assignment <i>a</i> if:
 * <ul>
 * <li><i>v</i> is a <code>POPNode</code> for a POP <i>(tau, g1, ..., gk)</i>
 * and <i>o</i> satisfies a POP application <i>(tau, (g1, o1), ..., (gk,
 * ok))</i> such that <i>o1, ..., ok</i> satisfy the parents of <i>v</i> given
 * <i>(w, a)</i>;
 * 
 * <li><i>v</i> is an <code>OrNode</code> and <i>o</i> satisfies one of the
 * parents of <i>v</i> given <i>(w, a)</i> (we ensure that the parents of an
 * <code>OrNode</code> are satisfied by disjoint sets);
 * 
 * <li><i>v</i> is a <code>TermNode</code> for a term <i>t</i> and <i>o</i> is
 * the denotation of <i>t</i> given <i>(w, a)</i>;
 * 
 * <li><i>v</i> is an <code>IntegerNode</code> with lower bounds <i>s1, ...,
 * sj</i> and upper bounds <i>t1, ..., tk</i>, and <i>o</i> is an integer (or
 * timestep) that is &gt;= the denotations of <i>s1, ..., sj</i> and &lt;= the
 * denotations of <i>t1, ..., tk</i> given <i>(w, a)</i>;
 * 
 * <li><i>v</i> is a <code>GuaranteedNode</code> for type <i>tau</i> and
 * <i>o</i> is a guaranteed object of type <i>tau</i>.
 * </ul>
 * 
 * <p>
 * The arguments to the ObjGenGraph constructor describe a certain set of
 * objects. The ObjGenGraph contains a distinguished <em>target node</em> that
 * is satisfied by all the described objects (and possibly some other objects
 * that exist). The <code>iterator</code> method returns an iterator over the
 * objects that satisfy the target node. This set may be infinite, but we can
 * still iterate over it.
 * 
 * TODO: Fix case where term in TermNode evaluates to Model.NULL.
 */

public class ObjGenGraph extends AbstractDGraph {
	/**
	 * Creates an object generation graph where the target node is satisfied by
	 * all objects of the given type.
	 */
	public ObjGenGraph(Type type) {
		this.var = null;
		targetNode = getTypeNode(type, null, Collections.EMPTY_LIST);
		recordAncestorGraph(targetNode);
	}

	/**
	 * Creates an object generation graph where the target node is satisfied by
	 * all objects of the given type that satisfy all the given constraints when
	 * bound to <code>subjectVar</code>.
	 * 
	 * @param constraints
	 *          List of Formula objects. Only literals are processed.
	 * 
	 * @param freeVars
	 *          Set of LogicalVar objects representing variables that should not
	 *          be used in the graph because they will not be assigned values when
	 *          the graph is used.
	 */
	public ObjGenGraph(Type type, LogicalVar subjectVar, List constraints,
			Set freeVars) {
		this.var = subjectVar;
		this.freeVars = freeVars;

		// System.out.println("Creating ObjGenGraph for {" + type + " "
		// + subjectVar + " : " + constraints + "}");

		targetNode = getTypeNode(type, subjectVar, constraints);
		recordAncestorGraph(targetNode);

		// print(System.out);
	}

	/**
	 * Creates an object generation graph where the set of free variables consists
	 * of just the subject variable.
	 */
	public ObjGenGraph(Type type, LogicalVar subjectVar, List constraints) {
		this(type, subjectVar, constraints, Collections.singleton(subjectVar));
	}

	/**
	 * Returns true if this ObjGenGraph exactly represents the set specified in
	 * its constructor: that is, all the objects returned by
	 * <code>elementSet</code> satisfy the constraints passed to the constructor.
	 */
	public boolean isExact() {
		return false;
	}

	/**
	 * Returns an iterator over the set of objects that satisfy the target node in
	 * the given context.
	 */
	public ObjectIterator iterator(EvalContext context) {
		return iterator(context, Collections.EMPTY_SET, false);
	}

	/**
	 * Returns an iterator over the set of objects that satisfy the target node in
	 * the given context. If <code>returnPOPApps</code> is true, returns
	 * NumberVars to represent entire sets of POP application satisfiers, rather
	 * than returning the satifiers themselves.
	 */
	public ObjectIterator iterator(EvalContext context,
			Set externallyDistinguished, boolean returnPOPApps) {
		return new SatisfierIterator(this, context, externallyDistinguished,
				returnPOPApps);
	}

	/**
	 * Returns true if the iteration order for objects that satisfy the target
	 * node is affected by the iteration order for object identifiers in the given
	 * context. This is true if any ancestor of the target node is a POPNode for a
	 * type that is represented with object identifiers in the given context.
	 */
	public boolean dependsOnIdOrder(EvalContext context) {
		return targetNode.dependsOnIdOrder(context);
	}

	public Set nodes() {
		return Collections.unmodifiableSet(nodes);
	}

	public Set getParents(Object v) {
		if (nodes.contains(v)) {
			return ((Node) v).getParents();
		}
		throw new IllegalArgumentException("Tried to get parents of non-node " + v
				+ ".");
	}

	public Set getChildren(Object v) {
		if (nodes.contains(v)) {
			return ((Node) v).getChildren();
		}
		throw new IllegalArgumentException("Tried to get children of non-node " + v
				+ ".");
	}

	private Node getTypeNode(Type type, Term subject, List constraints) {
		List relevantCons = new ArrayList();

		// First do processing that applies regardless of type
		for (Iterator iter = constraints.iterator(); iter.hasNext();) {
			Formula constraint = (Formula) iter.next();

			if (constraint instanceof EqualityFormula) {
				EqualityFormula equality = (EqualityFormula) constraint;

				// If constraint says subject is null, return null
				if (equality.assertsNull(subject)) {
					return null;
				}

				// If constraint says subject equals a term that does not
				// contain any free vars, then return TermNode for that term
				Term equalTerm = equality.getEqualTerm(subject);
				if ((equalTerm != null) && !containsFreeVar(equalTerm)) {
					return new TermNode(equalTerm);
				}
			}

			// If constraint mentions subject, add it to relevant
			// constraints list
			if (constraint.containsTerm(subject)) {
				relevantCons.add(constraint);
			}
		}

		// Now do type-specific processing

		if (type.isSubtypeOf(BuiltInTypes.INTEGER)
				|| (type == BuiltInTypes.TIMESTEP)) {
			return getIntegerNode(subject, constraints, type);
		} else if (type.isBuiltIn()) {
			// Built-in type for which we don't recognize any constraints.
			// None of the unprocessed constraints could be relevant.
			Node typeNode = new GuaranteedNode(type);
			internNode(typeNode, type);
			return typeNode;
		}

		return getUserDefTypeNode(type, subject, constraints);
	}

	private Node getIntegerNode(Term subject, List constraints, Type integerType) {
		IntegerNode node = (integerType == BuiltInTypes.TIMESTEP ? new TimestepNode()
				: new IntegerNode());

		for (Iterator iter = constraints.iterator(); iter.hasNext();) {
			addBound((Formula) iter.next(), subject, node);
		}

		if (!node.isConstrained()) {
			Node existing = (Node) unconstrainedNodes.get(integerType);
			if (existing != null) {
				return existing;
			}
			internNode(node, integerType);
		}

		if (integerType == BuiltInTypes.NATURAL_NUM) {
			node.addLowerBound(new FuncAppTerm(BuiltInFunctions.ZERO), false);
		} else if (integerType == BuiltInTypes.TIMESTEP) {
			node.addLowerBound(new FuncAppTerm(BuiltInFunctions.EPOCH), false);
		}

		return node;
	}

	private Node getUserDefTypeNode(Type type, Term subject, List constraints) {
		// See what constraints are asserted about the values of generating
		// functions on subject. Here we find generating functions that
		// are constrained to be non-null; generating functions that are
		// constrained to be null will be discovered by getPOPNode
		// (when it calls getTypeNode with the relevant gen func application
		// as the subject).
		Set nonNullGenFuncs = new HashSet();
		List originFuncConstraints = new ArrayList();
		for (Iterator iter = constraints.iterator(); iter.hasNext();) {
			Formula constraint = (Formula) iter.next();
			Set originFuncsApplied = constraint.getGenFuncsApplied(subject);
			if (!originFuncsApplied.isEmpty()) {
				originFuncConstraints.add(constraint);
			}

			for (Iterator originFuncIter = originFuncsApplied.iterator(); originFuncIter
					.hasNext();) {
				OriginFunction g = (OriginFunction) originFuncIter.next();
				FuncAppTerm t = new FuncAppTerm(g, Collections.singletonList(subject));
				if (impliesNonNull(constraint, t)) {
					nonNullGenFuncs.add(g);
				}
			}
		}

		// Before creating node, check to see if we're creating an
		// unconstrained type node, and if so, whether one already exists
		// for this type. Even if the object generation graph is cyclic,
		// the type nodes that we need will eventually be unconstrained,
		// since constraints can only be finitely deep.
		Type unconstrainedType = null;
		if (originFuncConstraints.isEmpty()) {
			Node existing = (Node) unconstrainedNodes.get(type);
			if (existing != null) {
				return existing;
			}
			unconstrainedType = type; // first visit to unconstrained type node
		}

		// Create node
		OrNode typeNode = new OrNode();
		internNode(typeNode, unconstrainedType);

		// Create and add parents
		if (nonNullGenFuncs.isEmpty()) {
			// Guaranteed objects of this type might satisfy constraints
			typeNode.addParent(new GuaranteedNode(type));
		}

		for (Iterator iter = type.getPOPs().iterator(); iter.hasNext();) {
			POP pop = (POP) iter.next();
			OriginFunction[] popGenFuncs = pop.originFuncs();
			if (Arrays.asList(popGenFuncs).containsAll(nonNullGenFuncs)) {
				// this POP uses all the non-null gen funcs
				Node popNode = createPOPNode(pop, subject, originFuncConstraints);

				// popNode could be null, e.g., if there is a
				// constraint saying one of its gen funcs is null
				if (popNode != null) {
					typeNode.addParent(popNode);
				}
			}
		}

		return typeNode;
	}

	private Node createPOPNode(POP pop, Term subject, List constraints) {
		List parents = new ArrayList();
		for (int i = 0; i < pop.originFuncs().length; ++i) {
			OriginFunction g = pop.originFuncs()[i];
			Term newSubject = new FuncAppTerm(g, Collections.singletonList(subject));
			Node typeNode = getTypeNode(g.getRetType(), newSubject, constraints);
			if (typeNode == null) {
				// constraints say newSubject couldn't be any object
				return null;
			}
			parents.add(typeNode);
		}
		return new POPNode(pop, parents);
	}

	/**
	 * Adds a lower or upper bound on <code>subject</code> to the given
	 * IntegerNode.
	 */
	private void addBound(Formula phi, Term subject, IntegerNode node) {
		// See if phi is an atomic formula or the negation thereof
		AtomicFormula psi = null;
		boolean positive = true;
		if (phi instanceof AtomicFormula) {
			psi = (AtomicFormula) phi;
		} else if ((phi instanceof NegFormula)
				&& (((NegFormula) phi).getNeg() instanceof AtomicFormula)) {
			psi = (AtomicFormula) ((NegFormula) phi).getNeg();
			positive = false;
		}
		if (psi == null) {
			return; // not an atomic formula or negation thereof
		}

		// See if it uses one of the functions we recognize, and if one
		// of the arguments is <code>subject</code> and the other does not
		// contain <code>var</code>.
		ArgSpec psiTerm = psi.getTerm();
		Function f = (psiTerm instanceof FuncAppTerm) ? ((FuncAppTerm) psiTerm)
				.getFunction() : null;
		boolean isFirstArg = true;
		Term bound = null;
		if ((f == BuiltInFunctions.LT) || (f == BuiltInFunctions.LEQ)
				|| (f == BuiltInFunctions.GT) || (f == BuiltInFunctions.GEQ)) {
			Term[] args = (Term[]) ((FuncAppTerm) psiTerm).getArgs();
			if (subject.equals(args[0])) {
				bound = args[1];
			} else if (subject.equals(args[1])) {
				isFirstArg = false;
				bound = args[0];
			}
		}
		if ((bound == null) || containsFreeVar(bound)) {
			return; // not a formula we can interpret
		}

		// Go through the logic of what's being asserted
		boolean strict = ((f == BuiltInFunctions.LT) || (f == BuiltInFunctions.GT));
		boolean upper = ((f == BuiltInFunctions.LT) || (f == BuiltInFunctions.LEQ));
		if (!positive) {
			upper = !upper;
		}
		if (!isFirstArg) {
			upper = !upper;
		}

		if (upper) {
			node.addUpperBound(bound, strict);
		} else {
			node.addLowerBound(bound, strict);
		}
	}

	private boolean containsFreeVar(Term term) {
		return !Util.intersection(term.getFreeVars(), freeVars).isEmpty();
	}

	private void internNode(Node node, Type unconstrainedType) {
		if (unconstrainedType != null) {
			unconstrainedNodes.put(unconstrainedType, node);
		}
	}

	private void recordAncestorGraph(Node v) {
		if (!nodes.contains(v)) {
			nodes.add(v);

			for (Iterator iter = v.getParents().iterator(); iter.hasNext();) {
				Node parent = (Node) iter.next();
				parent.addChild(v);
				recordAncestorGraph(parent);
			}
		}
	}

	/**
	 * Returns true if phi being true implies that t is not null. Assumes phi is a
	 * literal.
	 */
	private static boolean impliesNonNull(Formula phi, Term t) {
		if (!phi.containsTerm(t)) {
			return false;
		}

		if (phi instanceof AtomicFormula) {
			// If phi is true then its top-level function returns true,
			// which means all its arguments are non-null
			return true;
		}

		if (phi instanceof EqualityFormula) {
			// Return true if one of the terms in the equality contains t,
			// and the other term is always non-null. This assumes that
			// any function returns null when applied to a null argument --
			// there's no special case that says Boolean functions return
			// false.
			EqualityFormula equality = (EqualityFormula) phi;
			return ((equality.getTerm1().containsTerm(t) && isNonNullTerm(equality
					.getTerm2())) || (equality.getTerm2().containsTerm(t) && isNonNullTerm(equality
					.getTerm1())));
		}

		if (phi instanceof NegFormula) {
			// Return true if phi is the negation of an equality formula
			// that asserts t is null
			NegFormula negation = (NegFormula) phi;
			return ((negation.getNeg() instanceof EqualityFormula) && ((EqualityFormula) negation
					.getNeg()).assertsNull(t));
		}

		return false;
	}

	/**
	 * Returns true if t is a term that cannot evaluate to null, namely a
	 * LogicalVar or a non-random ground term whose value is not null.
	 */
	private static boolean isNonNullTerm(Term t) {
		if (t instanceof LogicalVar) {
			return true;
		}
		if (t.getFreeVars().isEmpty()) {
			Object value = t.getValueIfNonRandom();
			return ((value != null) && (value != Model.NULL));
		}
		return false;
	}

	Set nodes = new HashSet(); // of Node
	private Map unconstrainedNodes = new HashMap(); // from Type to Node

	Node targetNode;

	private LogicalVar var;
	private Set freeVars;
}
