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

import java.io.*;
import java.util.*;
import blog.*;
import blog.bn.BasicVar;
import blog.bn.BayesNetVar;
import blog.bn.RandFuncAppVar;
import blog.common.Util;
import blog.engine.InferenceEngine;
import blog.model.ArgSpec;
import blog.model.ArgSpecQuery;
import blog.model.Evidence;
import blog.model.FuncAppTerm;
import blog.model.Function;
import blog.model.LogicalVar;
import blog.model.Model;
import blog.model.NonRandomFunction;
import blog.model.Query;
import blog.model.Term;
import blog.model.Type;
import ve.*;

/**
 * Performs lifted inference with counting formulas. Usable either by
 * instantiation or through static inference method <code>liftedElim</code>.
 */
public class LiftedVarElim extends InferenceEngine {

	/**
	 * Performs lifted elimination on the given set of parfactors, according to
	 * the specified query. Modifies the set of parfactors in-place. Uses a greedy
	 * operator oracle.
	 */
	public static void liftedElim(Set<Parfactor> factors, ElimTester query) {
		liftedElim(factors, query, new LinkedList<Term>(), GREEDY_OPERATOR_ORACLE,
				false);
	}

	/**
	 * Performs lifted elimination on the given set of parfactors, according to
	 * the specified query. Modifies the set of parfactors in-place. Uses the
	 * given operator ordering oracle instead of the usual greedy default.
	 */
	public static void liftedElim(Set<Parfactor> factors, ElimTester query,
			LiftedInfOperatorOracle oracle) {
		liftedElim(factors, query, new LinkedList<Term>(), oracle, false);
	}

	/**
	 * Replaces the given set of Parfactors with an equivalent set that is
	 * shattered. No two terms ground to overlapping RV sets.
	 */
	public static void shatter(Set<Parfactor> parfactors,
			Collection<? extends Term> queryTerms) {
		ShatteredParfactorBag bag = new ShatteredParfactorBag(parfactors);
		bag.splitOnQueryTerms(queryTerms);
		// System.out.println("New parfactors: " + bag.parfactors());

		// now, convert everything to normal form
		// System.out.println("Converting to normal form...");
		parfactors.clear();
		for (Parfactor p : bag.parfactors())
			parfactors.addAll(p.makeConstraintsNormalForm());
	}

	// convenience
	private static void liftedElim(Set<Parfactor> factors, ElimTester query,
			Collection<? extends Term> queryTerms, boolean print) {
		liftedElim(factors, query, queryTerms, GREEDY_OPERATOR_ORACLE, print);
	}

	/**
	 * Where the magic happens.
	 * 
	 * @param factors
	 *          the parfactors to do lifted elimination on
	 * @param query
	 *          how to decide when to stop
	 * @param queryTerms
	 *          terms to split on during shattering
	 * @param print
	 *          verbose printlining
	 */
	public static void liftedElim(Set<Parfactor> factors, ElimTester query,
			Collection<? extends Term> queryTerms, LiftedInfOperatorOracle oracle,
			boolean print) {
		String errMsg = checkParfactors(factors);
		if (errMsg != null) {
			Util.fatalErrorWithoutStack(errMsg);
		}

		shatter(factors, queryTerms);

		if (print) {
			System.out.println("\n After shattering: \n");
			System.out.println("----------------------");
			for (Parfactor pf : factors) {
				pf.print(System.out);
				System.out.println("*");
			}
			System.out.println("----------------------");
		}

		while (hasAnyToElim(factors, query)) {
			LiftedInfOperator opToApply = oracle.nextOperator(factors, query);

			// apply the operator
			if (print) {
				System.out.println();
				System.out.println("Applying op: " + opToApply);
			}
			// note: the set of parfactors is in the operator
			opToApply.operate();
		}

		if (print) {
			System.out.println();
			System.out.println("Final set of Parfactors:\n");
			for (Parfactor p : factors) {
				p.print(System.out);
				System.out.println();
			}
		}
	}

	private static String checkParfactors(Collection<Parfactor> pfs) {
		for (Parfactor pf : pfs) {
			for (LogicalVar x : pf.logicalVars()) {
				Type type = x.getType();
				if (!type.getPOPs().isEmpty()) {
					return ("Can't handle parfactor quantifying over type " + type + ", which has unknown objects.");
				}
				if (!type.hasFiniteGuaranteed()) {
					return ("Can't handle parfactor quantifying over type " + type + ", which has infinitely many objects.");
				}
			}

			for (Term term : pf.dimTerms()) {
				String errMsg = checkParfactorTerm(term);
				if (errMsg != null) {
					return ("Can't handle parfactor with a dimension defined " + "by "
							+ term + " " + errMsg + ".");
				}
			}
		}

		return null;
	}

	private static String checkParfactorTerm(Term term) {
		FuncAppTerm funcApp = null;
		if (term instanceof FuncAppTerm) {
			funcApp = (FuncAppTerm) term;
		} else if (term instanceof CountingTerm) {
			funcApp = ((CountingTerm) term).singleSubTerm();
		} else {
			return ("of class " + term.getClass().getName());
		}

		Function f = funcApp.getFunction();
		if (f instanceof NonRandomFunction) {
			return ("which is application of nonrandom function " + f);
		}

		Type[] argTypes = f.getArgTypes();
		Term[] args = (Term[]) funcApp.getArgs();
		for (int i = 0; i < args.length; ++i) {
			String errMsg = checkParfactorTermArg(args[i], argTypes[i]);
			if (errMsg != null) {
				return ("whose argument " + args[i] + " " + errMsg);
			}
		}

		return null;
	}

	private static String checkParfactorTermArg(Term arg, Type argType) {
		if (arg instanceof LogicalVar) {
			return null;
		}

		if (arg instanceof FuncAppTerm) {
			if (!arg.getFreeVars().isEmpty()) {
				return "contains a nested logical variable";
			}
			Object argValue = arg.getValueIfNonRandom();
			if (argValue == null) {
				return "is random";
			}
			if (!arg.equals(argType.getCanonicalTerm(argValue))) {
				return "is not the canonical term for the object it denotes";
			}
			return null;
		}

		return "is not a logical variable or function application";
	}

	/**
	 * Returns true if any of the parfactors in the given collection contain a
	 * formula that should be summed out, according to the given query.
	 */
	private static boolean hasAnyToElim(Collection<? extends Parfactor> pfs,
			ElimTester query) {
		for (Parfactor pf : pfs) {
			Constraint constraint = pf.constraint();
			for (Term term : pf.dimTerms()) {
				if (query.shouldEliminate(term, constraint)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Creates a new LiftedVarElim engine for the given model. The properties
	 * argument is currently ignored.
	 */
	public LiftedVarElim(Model model, Properties properties) {
		super(model);
		pmn = new ParMarkovNet(model);

		if (Util.verbose()) {
			pmn.print(System.out);
		}
	}

	public void setEvidence(Evidence evidence) {
		super.setEvidence(evidence);

		if (!evidence.getSkolemConstants().isEmpty()) {
			throw new IllegalArgumentException(
					"LiftedVarElim doesn't handle symbol evidence.");
		}

		// Accumulate evidence factors in a list separate from the Markov
		// net, because setEvidence could be called again with different
		// evidence.
		evidenceParfactors = new ArrayList<Parfactor>();
		for (Iterator iter = evidence.getEvidenceVars().iterator(); iter.hasNext();) {
			BayesNetVar rv = (BayesNetVar) iter.next();
			if (rv instanceof RandFuncAppVar) {
				Object val = evidence.getObservedValue(rv);
				List<LogicalVar> logicalVars = Collections.emptyList();
				List<? extends Term> terms = Collections
						.singletonList(((RandFuncAppVar) rv).getCanonicalTerm());
				List<Object> valTuple = Collections.singletonList(val);
				evidenceParfactors.add(Parfactor.delta(logicalVars, Constraint.EMPTY,
						terms, valTuple));
			} else {
				throw new IllegalArgumentException("Can't handle evidence variable: "
						+ rv);
			}
		}
	}

	public void setQueries(List queries) {
		super.setQueries(queries);

		queryTerms = new LinkedHashMap<Query, FuncAppTerm>();
		for (Iterator iter = queries.iterator(); iter.hasNext();) {
			Query query = (Query) iter.next();
			FuncAppTerm queryTerm = getQueryTerm(query);
			String errMsg = checkParfactorTerm(queryTerm);
			if (errMsg != null) {
				Util.fatalErrorWithoutStack("LiftedVarElim engine can't handle query on "
						+ "term " + queryTerm + " " + errMsg + ".");
			}
			queryTerms.put(query, queryTerm);
		}
	}

	public void answerQueries() {

		long start = System.nanoTime();

		// System.out.println("before shattering!!");

		Set<Parfactor> factors = new LinkedHashSet<Parfactor>(pmn.getParfactors());
		factors.addAll(evidenceParfactors);

		ElimTester query = new GroundQuery(queryTerms.values());

		// do the work: lifted elimination on factors, for query, splitting
		// on queryTerms during shattering, with printlines on
		liftedElim(factors, query, queryTerms.values(), Util.verbose());

		long end = System.nanoTime();

		// Make sure all remaining parfactors are ground, and
		// contain only query atoms
		for (Parfactor p : factors) {
			if (!p.logicalVars().isEmpty()) {
				Util.fatalError("Parfactor " + p + " still contains logical variables.");
			}
			if (!queryTerms.values().containsAll(p.dimTerms())) {
				Util.fatalError("Parfactor " + p + " still contains a non-query term.");
			}
		}

		// Record answers to queries
		for (Map.Entry<Query, FuncAppTerm> entry : queryTerms.entrySet()) {
			recordAnswer(entry.getKey(), entry.getValue(), factors);
		}

		// If no queries, then the remaining factors must contain no
		// terms. Multiply them together and print out the resulting weight.
		if (queryTerms.isEmpty()) {
			Parfactor result = Parfactor.multiply(new ArrayList<Parfactor>(factors));
			System.out.println("Resulting zero-ary potential:");
			result.potential().print(System.out);
		}

		System.out.println("\n**TIME**" + (end - start));
	}

	private FuncAppTerm getQueryTerm(Query query) {
		if (!(query instanceof ArgSpecQuery)) {
			Util.fatalErrorWithoutStack("LiftedVarElim engine can't handle query of class "
					+ query.getClass().getName());
		}

		ArgSpec argSpec = ((ArgSpecQuery) query).argSpec();
		if (!(argSpec instanceof FuncAppTerm)) {
			Util.fatalErrorWithoutStack("With LiftedVarElim engine, queries must be "
					+ "function applications, not " + query);
		}

		return (FuncAppTerm) argSpec;
	}

	private void recordAnswer(Query query, FuncAppTerm queryTerm,
			Set<Parfactor> factors) {
		// Find factors containing query term
		List<Parfactor> queryFactors = new ArrayList<Parfactor>();
		for (Parfactor pf : factors) {
			if (pf.dimTerms().contains(queryTerm)) {
				if (pf.dimTerms().size() != 1) {
					Util.fatalErrorWithoutStack("Can't handle dependencies between query terms: "
							+ pf);
				}
				queryFactors.add(pf);
			}
		}

		// Multiply these factors together
		// (we know they're all ground factors)
		Parfactor product = Parfactor.multiply(queryFactors);

		// Record the answer to the query
		Potential pot = product.potential().copy();
		pot.normalize();
		List<BasicVar> queryVars = Collections.singletonList((BasicVar) queryTerm
				.getVariable());
		query.setPosterior(new Factor(queryVars, pot));
	}

	protected ParMarkovNet pmn;
	protected List<Parfactor> evidenceParfactors = Collections.emptyList();
	protected Map<Query, FuncAppTerm> queryTerms = Collections.emptyMap();

	// implements the existing greedy ordering. this should probably get
	// moved to another file when more of these get written.
	private static final LiftedInfOperatorOracle GREEDY_OPERATOR_ORACLE = new LiftedInfOperatorOracle() {

		public LiftedInfOperator nextOperator(Set<Parfactor> factors,
				ElimTester query) {
			Collection<LiftedInfOperator> ops = LiftedInfOperator.validOps(factors,
					query);
			if (ops.isEmpty()) {
				throw new IllegalStateException(
						"Not done with elimination, and no lifted inference "
								+ "operators can be applied.");
			}

			// find the lowest cost operator
			// TODO: get debugging information in here somehow
			// if (print)
			// System.out.println("Applicable operations:");
			LiftedInfOperator bestOp = null;
			double bestLogCost = 0;
			for (LiftedInfOperator o : ops) {
				double logCost = o.logCost();
				// if (print)
				// System.out.println("\t" + Math.round(Math.exp(logCost))
				// + " " + o);
				if ((bestOp == null) || (logCost < bestLogCost)) {
					bestOp = o;
					bestLogCost = logCost;
				}
			}
			return bestOp;
		}

	};

}
