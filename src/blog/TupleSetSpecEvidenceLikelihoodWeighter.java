package blog;

import java.util.*;

import blog.bn.BayesNetVar;
import blog.common.HashMultiset;
import blog.common.Multiset;
import blog.common.Util;
import blog.model.Evidence;
import blog.model.SymbolEvidenceStatement;
import blog.model.TupleSetSpec;
import blog.model.ValueEvidenceStatement;
import blog.world.PartialWorld;


/**
 * Computes likelihood of evidence stating that a set of variables is equal to a
 * set of values, using importance sampling with an idea similar to the one used
 * in {@link SymbolEvidenceLikelihoodWeighter}. The idea is that when the
 * evidence is on an implicit set of random variables (represented by a
 * TupleSetSpec), to instantiate those variables with values that guarantee the
 * union of their values will be the observed value. Moreover, there are many
 * way of obtaining this, so variables are associated to observed set values
 * according to likelihood. These choices form a proposal distribution for
 * importance sampling.
 * 
 * In fact, this is more complex than {@link SymbolEvidenceLikelihoodWeighter}
 * because more than one variable can generate the same observation, so the
 * number of observed values constitute only a lower bound for the size of the
 * set of random variables.
 * 
 * While there are more unassigned variables than values, they are assigned
 * their most likely value in the whole observed values set. Once the number of
 * unassigned variables and values becomes the same, variables are always forced
 * to choose among the yet unassigned values.
 */
public class TupleSetSpecEvidenceLikelihoodWeighter extends
		AbstractEvidenceLikelihoodWeighter {

	public TupleSetSpecEvidenceLikelihoodWeighter() {
		super(new DefaultEvidenceLikelihoodWeighter()); // TODO: take care of
																										// fallback
	}

	public TupleSetSpecEvidenceLikelihoodWeighter(
			EvidenceLikelihoodWeighter fallback) {
		super(fallback);
	}

	public Evidence keepRelevantEvidenceAndReturnTheRest(Evidence evidence) {
		valueEvidenceStatements = new LinkedList();
		Evidence rest = new Evidence();

		for (Iterator it = evidence.getValueEvidence().iterator(); it.hasNext();) {
			ValueEvidenceStatement statement = (ValueEvidenceStatement) it.next();
			if (statement.getLeftSide() instanceof TupleSetSpec)
				valueEvidenceStatements.add(statement);
			else
				rest.addValueEvidence(statement);
		}

		for (Iterator it = evidence.getSymbolEvidence().iterator(); it.hasNext();) {
			SymbolEvidenceStatement statement = (SymbolEvidenceStatement) it.next();
			rest.addSymbolEvidence(statement);
		}
		rest.compile();
		return rest;
	}

	public LikelihoodAndWeight specificLikelihoodSample(PartialWorld world) {
		this.world = world;
		LikelihoodAndWeight result = new LikelihoodAndWeight(1.0, 1.0);
		for (Iterator it = valueEvidenceStatements.iterator(); it.hasNext();) {
			ValueEvidenceStatement statement = (ValueEvidenceStatement) it.next();
			LikelihoodAndWeight lw = likelihoodAndWeight(statement);
			result.multiply(lw);
		}
		return result;
	}

	private LikelihoodAndWeight likelihoodAndWeight(
			ValueEvidenceStatement valueEvidenceStatement) {
		LikelihoodAndWeight result = new LikelihoodAndWeight(1.0, 1.0);

		List randomVariables = getRandomVariablesFromSetValueStatement(valueEvidenceStatement);
		Collection values = getValuesFromSetValueStatement(valueEvidenceStatement);

		if (values.size() < randomVariables.size()) {
			// Probability of two continuous variables hitting the same value is zero.
			// TODO: This condition came later; we were allowing two variables with
			// same value before.
			// Allowing that makes the code more complicated and general.
			// It is currently left in the more general form in case we decide to
			// revert this.
			// Should we stick with this condition, the code might be simplified.
			// See and reflect changes in (*) in {@link
			// TupleSetSpecEvidenceLikelihoodWeighterTest}.
			return new LikelihoodAndWeight(0.0, 1.0);
		}

		if (someVariableAlreadyInstantiatedToValueNotInSet(randomVariables, values)) {
			// System.out.println("Likelihood is zero.");
			return new LikelihoodAndWeight(0.0, 1.0);
		}

		if (values.size() == 0) {
			if (randomVariables.size() == 0)
				return new LikelihoodAndWeight(1.0, 1.0);
			else
				return new LikelihoodAndWeight(0.0, 1.0);
		}

		int numOfStillAvailableRandomVariables = randomVariables.size();
		Collection notYetChosenValues = new HashSet(values);

		if (notYetChosenValues.size() > numOfStillAvailableRandomVariables) { // no
																																					// way
																																					// of
																																					// generating
																																					// more
																																					// values
																																					// than
																																					// variables.
			// System.out.println("TupleLikelihoodWeighter: More values to choose (" +
			// Util.join(notYetChosenValues) +
			// ") than variables to generate them, so likelihood zero.");
			return new LikelihoodAndWeight(0.0, 1.0);
		}

		// System.out.println("World before sort: " + world);
		Collections.sort(randomVariables, instantiatedFirstComparator); // we want
																																		// instantiated
																																		// variables
																																		// to choose
																																		// first,
																																		// since
																																		// they are
																																		// the most
																																		// restricted.
		// System.out.println("TupleLikelihoodWeighter: Sorted: " +
		// randomVariables);
		for (BayesNetVar var : (Collection<BayesNetVar>) randomVariables) {

			if (notYetChosenValues.size() > numOfStillAvailableRandomVariables) { // no
																																						// way
																																						// of
																																						// generating
																																						// more
																																						// values
																																						// than
																																						// variables.
				// System.out.println("TupleLikelihoodWeighter: More values to choose ("
				// + Util.join(notYetChosenValues) +
				// ") than variables to generate them, so likelihood zero.");
				return new LikelihoodAndWeight(0.0, 1.0);
			}

			// If we don't have more variables than values, we need to always choose
			// from unchosen values from now on to make sure all values are explained.
			boolean onlyChooseFromYetUnchosenOnes = numOfStillAvailableRandomVariables == notYetChosenValues
					.size();
			Collection valuesToChooseFrom = onlyChooseFromYetUnchosenOnes ? notYetChosenValues
					: values;

			Choice choice = makeChoice(var, valuesToChooseFrom);
			// System.out.println("TupleLikelihoodWeighter: " + var + "'s choice: " +
			// choice);

			if (Double.isNaN(choice.weight)) {
				Util.fatalError("TupleLikelihoodWeighter: weight is not a number: "
						+ choice);
			}

			result.multiply(choice);
			if (result.likelihood == 0.0)
				return result;
			notYetChosenValues.remove(choice.chosenValue);
			numOfStillAvailableRandomVariables--;
		}
		return result;
	}

	private boolean someVariableAlreadyInstantiatedToValueNotInSet(
			Collection randomVariables, Collection values) {
		for (BayesNetVar var : (Collection<BayesNetVar>) randomVariables) {
			Object value = world.getValue(var);
			if (value != null && !values.contains(value)) {
				// System.out.println(var + " instantiated with " + value +
				// ", not present in " + Util.join(values));
				return true;
			}
		}
		return false;
	}

	private Choice makeChoice(BayesNetVar var, Collection valuesToChooseFrom) {
		Choice choiceIfInstantiated = makeChoiceIfInstantiated(var,
				valuesToChooseFrom);
		if (choiceIfInstantiated != null)
			return choiceIfInstantiated;

		HashMap likelihoodByValue = new HashMap();
		for (Object value : valuesToChooseFrom) {
			likelihoodByValue.put(value,
					BLOGUtil.setAndGetProbability(var, value, world));
		}
		Util.SampledMapEntry sampledEntry = Util
				.sampleBasedOnValue(likelihoodByValue);

		if (sampledEntry == null) { // impossible to choose
			return new Choice(null, 1.0, 0.0);
		}

		Object value = sampledEntry.getKey();
		double pFactor = sampledEntry.doubleValue();
		double qFactor = sampledEntry.getProbability();

		// if (Double.isNaN(pFactor/qFactor)) {
		// System.out.println("pFactor: " + pFactor);
		// System.out.println("qFactor: " + qFactor);
		// System.out.println("Weight is not a number!");
		// System.out.println("Map of options:");
		// System.out.println(Util.join("\n", likelihoodByValue));
		// System.exit(-1);
		// }

		BLOGUtil.setIfBasicVar(var, value, world); // commit to chosen value
		return new Choice(value, 1.0, pFactor / qFactor); // likelihood is 1 because
																											// value is always in set.
	}

	private Choice makeChoiceIfInstantiated(BayesNetVar var,
			Collection valuesToChooseFrom) {
		Object value = world.getValue(var);
		if (value != null) {
			double pFactor = valuesToChooseFrom.contains(value) ? 1 : 0;
			return new Choice(value, 1.0, pFactor / 1.0); // likelihood is 1 because
																										// value is always in set.
																										// proposal q = 1 because is
																										// deterministic (determined
																										// by the instantiated
																										// value).
		}
		return null;
	}

	private LinkedList getRandomVariablesFromSetValueStatement(
			ValueEvidenceStatement statement) {
		TupleSetSpec setSpec = (TupleSetSpec) statement.getLeftSide();
		if (setSpec.getGenericTuple().length != 1)
			Util.fatalError("Value evidence with tuple set " + setSpec
					+ " on left side should have tuples with exactly one element.");
		Multiset instantiatedVars = setSpec
				.getRandomVariables(new ClassicInstantiatingEvalContext(world));
		LinkedList randomVariables = new LinkedList();
		for (Iterator it = instantiatedVars.iterator(); it.hasNext();) {
			List row = (List) it.next();
			BayesNetVar var = (BayesNetVar) Util.getFirst(row);
			randomVariables.add(var);
		}
		return randomVariables;
	}

	private Collection getValuesFromSetValueStatement(
			ValueEvidenceStatement statement) {
		return (Collection) statement.getOutput().evaluate(
				new ClassicInstantiatingEvalContext(world));
	}

	private static class Choice extends LikelihoodAndWeight {
		public Choice(Object chosenValue, double likelihood, double weight) {
			super(likelihood, weight);
			this.chosenValue = chosenValue;
		}

		public String toString() {
			return chosenValue + ", lw: " + super.toString();
		}

		Object chosenValue;
	}

	private Comparator instantiatedFirstComparator = new Comparator<BayesNetVar>() {
		public int compare(BayesNetVar var1, BayesNetVar var2) {
			if (world.getValue(var1) != null)
				if (world.getValue(var2) != null) {
					return 0;
				} else {
					return -1;
				}
			else if (world.getValue(var2) != null) {
				return 1;
			} else {
				return 0;
			}
		}
	};

	private ValueEvidenceStatement valueEvidenceStatement;
	private Collection valueEvidenceStatements;
	private PartialWorld world;
}
