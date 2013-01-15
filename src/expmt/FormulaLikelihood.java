package expmt;

import java.util.*;

import blog.common.HashMultiMap;
import blog.common.MultiMap;
import blog.common.Util;
import blog.model.ArgSpec;
import blog.model.AtomicFormula;
import blog.model.ConjFormula;
import blog.model.ConstantInterp;
import blog.model.DisjFormula;
import blog.model.EqualityFormula;
import blog.model.Formula;
import blog.model.FuncAppTerm;
import blog.model.Function;
import blog.model.NegFormula;
import blog.model.NonRandomFunction;
import blog.model.Term;


/**
 * This is a collection of methods aimed at computing the likelihood of formulas
 * without sampling their components (atomic and equality formulas), making it
 * exact given the parents of these components. This is not always possible,
 * however. It is possible when the formula is a <i>mx-DNF</i>, that is, a DNF
 * in which all disjuncts are mutually exclusive, in which all random variables
 * present are independent from each other given their parents (in other words,
 * when there is no parent-child relationship between any of them). Since
 * deciding this independence for all possible instantiations of parents can be
 * complicated, we for now consider particular supporting instantiations of
 * parents, making the notion of mx-DNF relative to an actual partial world.
 * 
 * All these methods assume the absence of unbound logical variables.
 * 
 * @author Rodrigo
 * 
 */
public class FormulaLikelihood {

	/**
	 * Tests whether this is a DNF with mutually exclusive disjuncts.
	 */
	public static boolean isDNFWithMutuallyExclusiveDisjuncts(
			DisjFormula disjunction) {
		List disjuncts = disjunction.getDisjuncts();
		for (int i = 0; i != disjuncts.size(); i++) {
			ConjFormula disjunctI = (ConjFormula) disjuncts.get(i);
			for (int j = i + 1; j != disjuncts.size(); j++) {
				ConjFormula disjunctJ = (ConjFormula) disjuncts.get(j);
				if (!areMutuallyExclusive(disjunctI, disjunctJ))
					return false;
			}
		}

		return true;
	}

	/**
	 * Indicates whether two conjunctions are mutually exclusive.
	 */
	public static boolean areMutuallyExclusive(ConjFormula conjunction,
			ConjFormula another) {
		// two conjunctions will be mutually exclusive if at least one conjunct in
		// one of them
		// implies the negation of a conjunct of the other.
		// Since that relation is symmetric, there is no need to test it both ways.
		List cartesianProduct = new blog.common.CartesianProduct(Util.list(
				conjunction.getConjuncts(), another.getConjuncts()));
		for (Iterator it = cartesianProduct.iterator(); it.hasNext();) {
			List pair = (List) it.next();
			if (areMutuallyExclusiveLiterals((Formula) pair.get(0),
					(Formula) pair.get(1)))
				return true;
		}
		return false;
	}

	/**
	 * Indicates whether two literals are mutually exclusive
	 */
	public static boolean areMutuallyExclusiveLiterals(Formula a, Formula b) {
		return literalImpliesLiteral(a, getNegation(b));
	}

	public static Formula getNegation(Formula formula) {
		if (formula instanceof NegFormula)
			return ((NegFormula) formula).getNeg();
		return new NegFormula(formula);
	}

	public static boolean implyTheNegationOfEachOther(EqualityFormula e1,
			EqualityFormula e2) {
		Term a = (Term) e1.getTerm1();
		Term b = (Term) e1.getTerm2();
		Term c = (Term) e2.getTerm1();
		Term d = (Term) e2.getTerm2();

		return oneCase(a, c, b, d) || oneCase(a, d, b, c) || oneCase(b, c, a, d)
				|| oneCase(b, d, a, c);
	}

	private static boolean oneCase(Term a, Term b, Term c, Term d) {
		return isVariable(a) && isVariable(b) && a.equals(b) && !isVariable(c)
				&& !isVariable(d) && !c.equals(d);
	}

	private static boolean isVariable(ArgSpec t) {
		FuncAppTerm fa = (FuncAppTerm) t;
		Function f = fa.getFunction();
		return !(f instanceof NonRandomFunction)
				|| !((NonRandomFunction) f).getInterpClass().equals(
						ConstantInterp.class);
	}

	/**
	 * Indicates whether a literal is implied by all disjuncts of a DNF.
	 */
	public static boolean implies(DisjFormula dnf, Formula literal) {
		for (Iterator it = dnf.getDisjuncts().iterator(); it.hasNext();) {
			if (!implies((ConjFormula) it.next(), literal))
				return false;
		}
		return true;
	}

	public static boolean implies(ConjFormula conjunction, Formula literal) {
		for (Iterator it = conjunction.getConjuncts().iterator(); it.hasNext();) {
			if (!literalImpliesLiteral((Formula) it.next(), literal))
				return false;
		}
		return true;
	}

	public static boolean literalImpliesLiteral(Formula literal1, Formula literal2) {
		if (literal1 instanceof AtomicFormula) {
			if (literal2.equals(literal1))
				return true;
			return false;
		}

		if (literal1 instanceof NegFormula) {
			if (literal2 instanceof NegFormula) {
				Formula negLiteral1 = ((NegFormula) literal1).getNeg();
				Formula negLiteral2 = ((NegFormula) literal2).getNeg();
				return literalImpliesLiteral(negLiteral2, negLiteral1);
			}
			return false;
		}

		if (literal1 instanceof EqualityFormula) {
			if (literal2 instanceof EqualityFormula)
				return implies((EqualityFormula) literal1, (EqualityFormula) literal2);
			if (literal2 instanceof NegFormula) {
				NegFormula negFormula2 = (NegFormula) literal2;
				if (negFormula2.getNeg() instanceof EqualityFormula)
					return implyTheNegationOfEachOther((EqualityFormula) literal1,
							(EqualityFormula) negFormula2.getNeg());
			}
			return false;
		}

		Util.fatalError("Formula passed to FormulaLikelihood.literalImpliesLiteral not a literal: "
				+ literal1);
		return false;
	}

	public static boolean implies(EqualityFormula equality1,
			EqualityFormula equality2) {
		return (equality1.getTerm1().equals(equality2.getTerm1()) && equality1
				.getTerm2().equals(equality2.getTerm2()))
				|| (equality1.getTerm1().equals(equality2.getTerm2()) && equality1
						.getTerm2().equals(equality2.getTerm1()));
	}
}
