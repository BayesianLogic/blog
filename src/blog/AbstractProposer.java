package blog;

import java.util.*;

import common.Util;

/**
 * A class providing basic Proposer functionality, including the creation of an
 * initial world by a {@link LWSampler}. The user must implement
 * {@link #proposeNextState(PartialWorldDiff)}.
 */
public abstract class AbstractProposer implements Proposer {

	public AbstractProposer(Model model, Properties properties) {
		this.model = model;
		this.properties = properties;
	}

	public PartialWorldDiff initialize(Evidence evidence, List queries) {
		this.evidence = new Evidence();
		evidenceVars = new HashSet();
		numBasicEvidenceVars = 0;

		this.queries = new LinkedList();
		queryVars = new LinkedHashSet();

		add(evidence);
		addQueries(queries);

		return constructInitialState();
	}

	/**
	 * Adds extra evidence to the evidence the proposer must consider.
	 */
	public void add(Evidence evidence) {
		this.evidence.addAll(evidence);
		evidenceVars.add(evidence.getEvidenceVars());
		for (Iterator iter = evidenceVars.iterator(); iter.hasNext();) {
			if (iter.next() instanceof BasicVar) {
				numBasicEvidenceVars++;
			}
		}
	}

	/**
	 * Adds extra queries to the ones the proposer must consider.
	 */
	public void addQueries(Collection queries) {
		this.queries.addAll(queries);
		for (Iterator iter = queries.iterator(); iter.hasNext();) {
			queryVars.addAll(((Query) iter.next()).getVariables());
		}
	}

	/**
	 * Actually builds initial state, after whatever initializations are
	 * necessary. Separated from {@link #initialize(Evidence,List)} so extensions
	 * can change it alone.
	 */
	protected PartialWorldDiff constructInitialState() {
		Sampler initialStateSampler = new LWSampler(model, properties);
		++numTrials;
		numInitialStateTriesThisTrial = 0;
		initialStateSampler.initialize(evidence, queries);

		while (true) {
			initialStateSampler.nextSample();
			PartialWorld initWorld = initialStateSampler.getLatestWorld();
			++totalNumInitialStateTries;
			++numInitialStateTriesThisTrial;

			if (initialStateSampler.getLatestWeight() > 0) {
				if (Util.verbose()) {
					System.out.println("Probability of " + numInitialStateTriesThisTrial
							+ "th initial state = " + initialStateSampler.getLatestWeight());
					initWorld.print(System.out);
				}

				PartialWorld underlying = new DefaultPartialWorld(
						initWorld.getIdTypes());
				return new PartialWorldDiff(underlying, initWorld);
			} else { // world is inconsistent with evidence, try again
				if (Util.verbose()) {
					System.out.println(numInitialStateTriesThisTrial
							+ "th initial world rejected.");
				}
			}
		}
	}

	public void printStats() {
		System.out.println("===== " + getClass().getName() + " Stats ====");
		System.out.println("Initial world attempts: "
				+ numInitialStateTriesThisTrial);
		if (numTrials > 0) {
			System.out.println("\tRunning average (for trials so far): "
					+ (totalNumInitialStateTries / (double) numTrials));
		}
	}

	public void updateStats(boolean accepted) {
	}

	public abstract double proposeNextState(PartialWorldDiff proposedWorld);

	protected Model model;

	protected Properties properties;

	// TODO: it seems wasteful to keep evidence and queries
	// since a proposer is typically used by a sampler that also keeps them.

	protected Evidence evidence = null;
	protected List queries; // of Query

	protected Set evidenceVars;
	protected int numBasicEvidenceVars;
	protected Set queryVars;

	protected double logProbForward;
	protected double logProbBackward;

	protected int numTrials = 0;
	protected int totalNumInitialStateTries = 0;
	protected int numInitialStateTriesThisTrial = 0;
}
