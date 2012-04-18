package blog;

import java.util.*;

import blog.bn.BasicVar;
import blog.bn.RandFuncAppVar;
import blog.common.HashMultiMap;
import blog.common.Util;
import blog.model.ArgSpec;
import blog.model.Evidence;
import blog.model.NonRandomFunction;
import blog.model.RandomFunction;
import blog.model.SkolemConstant;
import blog.model.Term;
import blog.model.Type;


/**
 * Samples the likelihood of evidence in a partial world, with varying weight,
 * using importance sampling of associations between symbol evidence identifiers
 * and objects, if applicable. Otherwise, falls back on regular likelihood
 * calculation.
 * 
 * As an example of how this happens, consider the following bit of BLOG code:
 * <code>
 * type Aircraft;<br>
 * guaranteed Aircraft a, a2;<br>
 * random Real Position(Aircraft);<br>
 * Position(ac) ~ Gaussian(3.0, 1.0);<br>
 * 
 * type Blip;<br>
 * origin Aircraft Source(Blip);<br>
 * #Blip(Source = ac) = 1;<br>
 * 
 * random Real ApparentPos(Blip);<br>
 * ApparentPos(blip) ~ Gaussian[1](Position(Source(blip)));<br>
 * obs Position(a)  = 3;<br>
 * obs Position(a2) = 7;<br>
 * obs {Blip x} = {Blip1, Blip2};<br>
 * obs ApparentPos(Blip1) = 1;<br>
 * obs ApparentPos(Blip2) = 6;
 * </code>
 * 
 * Running regular likelihood weighting for such evidence will not work, because
 * <code>ApparentPos(Blip1)</code> is a derived variable with parents
 * <code>Blip1</code> and <code>ApparentPos(b)</code>, for <code>b</code> the
 * value of <code>Blip1</code>. Both <code>Blip1</code> and
 * <code>ApparentPos(b)</code> are sampled without any regard for the evidence,
 * and since this is a continuous variable, the evidence will be matched with
 * probability zero.
 * 
 * The situation is better with discrete variables but not ideal, since the
 * identifier would still be sampled without regard for the evidence. Since
 * there are exponentially many different total bindings for the symbol evidence
 * identifiers, the chances of sampling a good association is small.
 * 
 * We can circumvent the problem by sampling identifiers according to a proposal
 * distribution proportional to the evidence likelihood obtained by that
 * association. However, since there are exponentially many such associations,
 * this solution is expensive. We can instead greedily sample one identifier at
 * a time according to the likelihood of the portion of evidence relative to it
 * (that is, the portion of evidence that, given this and previous identifiers's
 * instantiations, becomes a ground random variable).
 * 
 * In the example above, <code>Blip1</code> is much more likely to be assigned
 * the blip coming from aircraft <code>a</code>, since its position is more
 * likely to come from that aircraft. Once that aircraft is taken,
 * <code>Blip2</code> has only <code>a2</code> left to be assigned to. This
 * association is indeed the most likely one.
 * 
 * Therefore, the proposal distribution is defined as the product of
 * probabilities of the independent choices made. This is the weight returned by
 * the sampling method.
 * 
 * When there is no symbol evidence, regular evidence likelihood is used instead
 * with weight 1.
 */
public class SymbolEvidenceLikelihoodWeighter extends
		AbstractEvidenceLikelihoodWeighter {
	public SymbolEvidenceLikelihoodWeighter() {
		super(new DefaultEvidenceLikelihoodWeighter());
	}

	public SymbolEvidenceLikelihoodWeighter(
			EvidenceLikelihoodWeighter evidenceLikelihoodWeighter) {
		super(evidenceLikelihoodWeighter);
	}

	public Evidence keepRelevantEvidenceAndReturnTheRest(Evidence evidence) {
		this.evidence = evidence;

		symbolStatementByIdentifier = getSymbolStatementByIdentifier(evidence);
		identifiers = symbolStatementByIdentifier.keySet();
		valueEvidenceByIdentifier = getValueEvidenceByLastIdentifierToBeSampled(
				identifiers, evidence);
		Evidence valueEvidenceWithNoIdentifiers = (Evidence) valueEvidenceByIdentifier
				.get(null);

		return valueEvidenceWithNoIdentifiers;
	}

	public LikelihoodAndWeight specificLikelihoodSample(PartialWorld world) {
		this.curWorld = world;

		// Preserves original world in case proposal does not work.
		PartialWorld originalWorld = curWorld;
		PartialWorldDiff changed = new PartialWorldDiff(originalWorld);
		curWorld = changed;

		/*
		 * If there is symbol evidence, check whether world satisfies number of
		 * objects, sampling if necessary.
		 */
		if (!worldSatisfiesNumberOfSymbols())
			return new LikelihoodAndWeight(0, 1);

		LikelihoodAndWeight result = new LikelihoodAndWeight(1.0, 1.0);
		valuesAlreadyTakenBySymbolEvidenceStatement = new HashMultiMap();
		boolean firstOne = true;
		for (Iterator it = identifiers.iterator(); it.hasNext();) {
			FuncAppTerm identifier = (FuncAppTerm) it.next();

			if (!firstOne) {
				// If identifier has become instantiated somehow, skip it.
				BasicVar identifierVariable = getIdentifierVariable(identifier);
				if (curWorld.getValue(identifierVariable) != null)
					continue;
			} else
				firstOne = false;

			Choice choice = makeChoice(identifier);

			if (choice == null) // proposal selected space with likelihood zero. TODO:
													// this could be made cleaner by having makeChoice
													// return a Choice with likelihood zero.
				return new LikelihoodAndWeight(0.0, 1);

			result.multiply(choice);

			// System.out.println("identifier: " + identifier);
			// System.out.println("Chosen value: " + choice.chosenValue);
			// System.out.println("Likelihood: " + choice.likelihood);
			// System.out.println("p: " + choice.p);
			// System.out.println("q: " + choice.q);
			// System.out.println("Weight (p/q): " + choice.weight);
			// System.out.println("Joint weight: " + result.weight);
			// System.out.println("Joint likelihood: " + result.likelihood);
			// System.out.println("(joint) likelihood * weight: " +
			// result.weightedLikelihood());
		}

		// Proposal worked, save changes.
		changed.save();

		return result;
	}

	/**
	 * Returns the collection of symbol terms in symbol evidence statements in
	 * given evidence.
	 */
	private boolean worldSatisfiesNumberOfSymbols() {
		for (Iterator it = evidence.getSymbolEvidence().iterator(); it.hasNext();) {
			SymbolEvidenceStatement statement = (SymbolEvidenceStatement) it.next();
			statement.getSetSpec().evaluate(
					new ClassicInstantiatingEvalContext(curWorld));
			Set satisfiers = statement.getSetSpec().getSatisfyingSet(curWorld);
			if (satisfiers.size() != statement.getSkolemConstants().size())
				return false;
		}
		return true;
	}

	/**
	 * Returns a LinkedHashMap from identifiers to their respective symbol
	 * evidence statements in a given evidence object, preserving the order of
	 * symbols in their respective symbol evidence statements.
	 */
	public LinkedHashMap getSymbolStatementByIdentifier(Evidence evidence) {
		LinkedHashMap result = new LinkedHashMap();
		for (Iterator it = evidence.getSymbolEvidence().iterator(); it.hasNext();) {
			SymbolEvidenceStatement statement = (SymbolEvidenceStatement) it.next();
			for (Iterator it2 = statement.getSkolemConstants().iterator(); it2
					.hasNext();) {
				SkolemConstant skC = (SkolemConstant) it2.next();
				Term identifier = new FuncAppTerm(skC);
				result.put(identifier, statement);
			}
		}
		return result;
	}

	/**
	 * Returns a map from each identifier to an evidence object containing all
	 * value evidence relative to the identifier, coming from a given evidence
	 * object, or null if any statement contains more than one of the given
	 * identifiers. The map includes an evidence object indexed by null,
	 * containing all value evidence statements not containing any of the
	 * identifiers. Does not include symbol evidence statements.
	 */
	public static Map getValueEvidenceByLastIdentifierToBeSampled(
			Collection identifiersInSamplingOrder, Evidence evidence) {
		Map result = new HashMap();
		for (Iterator it = identifiersInSamplingOrder.iterator(); it.hasNext();) {
			Object identifier = (Object) it.next();
			result.put(identifier, new Evidence());
		}
		Evidence valueEvidenceOnNoIdentifiers = new Evidence();
		result.put(null, valueEvidenceOnNoIdentifiers);
		for (Iterator it = evidence.getValueEvidence().iterator(); it.hasNext();) {
			ValueEvidenceStatement statement = (ValueEvidenceStatement) it.next();
			LinkedHashSet identifiersInStatement = getIdentifiersInValueStatement(
					identifiersInSamplingOrder, statement);
			if (identifiersInStatement.size() == 0) {
				valueEvidenceOnNoIdentifiers.addValueEvidence(statement);
				continue;
			}
			Object identifier = Util.getLast(identifiersInStatement);
			Evidence identifierEvidence = (Evidence) result.get(identifier);
			identifierEvidence.addValueEvidence(statement);
			result.put(identifier, identifierEvidence);
		}

		if (valueEvidenceOnNoIdentifiers.getValueEvidence().size() != 0)
			result.put(null, valueEvidenceOnNoIdentifiers);

		for (Iterator it = result.values().iterator(); it.hasNext();)
			((Evidence) it.next()).compile();

		return result;
	}

	/**
	 * Returns a LinkedHashSet of identifiers, among the ones provided, in a value
	 * evidence statement, preserving the same relative order as the one in the
	 * identifiers collection -- this is useful because, in symbol evidence
	 * sampling, we must use value evidence to measure the proposal distribution
	 * for its last identifier to be sampled only (since we need a total
	 * instantiation of identifiers in order to calculate it).
	 */
	public static LinkedHashSet getIdentifiersInValueStatement(
			Collection identifiers, ValueEvidenceStatement statement) {
		LinkedHashSet result = new LinkedHashSet();
		for (Iterator it = identifiers.iterator(); it.hasNext();) {
			Term identifier = (Term) it.next();
			if (statement.getLeftSide().find(identifier) != null
					|| statement.getOutput().find(identifier) != null)
				result.add(identifier);
		}
		return result;
	}

	/**
	 * Returns the next choice, or <code>null</code> if this is not possible (from
	 * likelihoods being all zero).
	 */
	private Choice makeChoice(FuncAppTerm identifier) {
		BasicVar identifierVariable = getIdentifierVariable(identifier);
		SymbolEvidenceStatement identifierSymbolStatement = (SymbolEvidenceStatement) symbolStatementByIdentifier
				.get(identifier);
		Evidence identifierValueEvidence = (Evidence) valueEvidenceByIdentifier
				.get(identifier);
		Collection valuesAlreadyTaken = (Collection) valuesAlreadyTakenBySymbolEvidenceStatement
				.get(identifierSymbolStatement);

		Type type = identifierSymbolStatement.getSetSpec().getType();
		Set satisfiers = new HashSet(getSatisfiers(identifier,
				identifierSymbolStatement, curWorld));
		satisfiers.removeAll(valuesAlreadyTaken);

		Object[] possibleValues = Util.asArray(satisfiers);
		double[] likelihoods = computeLikelihoods(type, identifier,
				identifierVariable, possibleValues, identifierValueEvidence);
		if (likelihoods == null)
			return null;

		Choice choice = new Choice(type, identifier, identifierVariable,
				possibleValues, likelihoods);

		valuesAlreadyTaken.add(choice.chosenValue);
		curWorld.setValue(identifierVariable, choice.chosenValue);

		return choice;
	}

	private Set getSatisfiers(FuncAppTerm identifier,
			SymbolEvidenceStatement identifierSymbolStatement, PartialWorld world) {
		return identifierSymbolStatement.getSetSpec().getSatisfyingSet(world);
	}

	private void setIdentifierVariableToValue(FuncAppTerm identifier,
			Choice choice, PartialWorld world) {
		BasicVar identifierVariable = getIdentifierVariable(identifier);
		world.setValue(identifierVariable, choice.chosenValue);
	}

	private BasicVar getIdentifierVariable(FuncAppTerm identifier) {
		RandomFunction function = (RandomFunction) identifier.getFunction();
		BasicVar identifierVariable = new RandFuncAppVar(function,
				Collections.EMPTY_LIST);
		return identifierVariable;
	}

	/**
	 * Compute set of likelihoods of a value evidence statement for each possible
	 * value of an identifier of a given type, in the current world, or returns
	 * <code>null</code> if all likelihoods are zero.
	 */
	private double[] computeLikelihoods(Type type, Term identifier,
			BasicVar identifierVariable, Object[] possibleValues,
			Evidence identifierValueEvidence) {
		double[] likelihoods = new double[possibleValues.length];
		double sum = 0;
		for (int i = 0; i != possibleValues.length; i++) {
			likelihoods[i] = getLikelihood(type, identifier, identifierVariable,
					possibleValues[i], identifierValueEvidence);
			sum += likelihoods[i];
		}
		if (sum == 0)
			return null;
		return likelihoods;
	}

	private double getLikelihood(Type type, Term identifier,
			BasicVar identifierVariable, Object value,
			Evidence identifierValueEvidence) {
		PartialWorldDiff scratchWorld = new PartialWorldDiff(curWorld);
		scratchWorld.setValue(identifierVariable, value);
		ArgSpec valueTerm = constantTerm(type, value);
		identifierValueEvidence = identifierValueEvidence.replace(identifier,
				valueTerm);
		double likelihood = identifierValueEvidence
				.setEvidenceEnsureSupportedAndReturnLikelihood(scratchWorld);

		// System.out.println("identifier: " + identifier);
		// System.out.println("value: " + valueTerm);
		// System.out.println("Value evidence after replacement: " +
		// identifierValueEvidence);
		// System.out.println("likelihood: " + likelihood + "\n");

		return likelihood;
	}

	private ArgSpec constantTerm(Type type, Object value) {
		return new FuncAppTerm(NonRandomFunction.createConstant(value.toString(),
				type, value));
	}

	public class Choice extends LikelihoodAndWeight {
		/**
		 * Constructs a choice for an identifier out of a set of possible values and
		 * respective likelihoods. Assumes likelihoods' sum is greater than zero.
		 */
		public Choice(Type type, Term identifier, BasicVar identifierVariable,
				Object[] possibleValues, double[] likelihoods) {
			double[] probabilities = Util.normalize(likelihoods);
			int choice = Util.sampleWithProbs(probabilities);

			chosenValue = possibleValues[choice];
			likelihood = likelihoods[choice];
			p = (1.0 / possibleValues.length); // p(x) = uniform prior for
																					// associations
			q = probabilities[choice];
			weight = p / q;
		}

		public Object chosenValue;
		public double p;
		public double q;
	}

	public Evidence evidence;
	public PartialWorld curWorld;

	public Collection identifiers;
	public Map symbolStatementByIdentifier;
	public Map valueEvidenceByIdentifier;
	public HashMultiMap valuesAlreadyTakenBySymbolEvidenceStatement; // each key
																																		// is a
																																		// symbol
																																		// evidence
																																		// statement
																																		// and maps
																																		// to the
																																		// set of
																																		// objects
																																		// already
																																		// taken by
																																		// its
																																		// identifiers
																																		// already
																																		// instantiated.
}
