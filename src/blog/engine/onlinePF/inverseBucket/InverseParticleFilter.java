package blog.engine.onlinePF.inverseBucket;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import blog.DBLOGUtil;
import blog.common.Util;
import blog.engine.InferenceEngine;
import blog.engine.onlinePF.ObservabilitySignature;
import blog.model.Evidence;
import blog.model.Model;
import blog.model.Query;
import blog.sample.AfterSamplingListener;
import blog.sample.Sampler;

public class InverseParticleFilter extends InferenceEngine {

	/**
	 * Creates a new particle filter for the given BLOG model, with configuration
	 * parameters specified by the given properties table.
	 */
	public InverseParticleFilter(Model model, Properties properties) {
		super(model);

		String numParticlesStr = properties.getProperty("numParticles");
		String numSamplesStr = properties.getProperty("numSamples");
		if (numParticlesStr != null && numSamplesStr != null
				&& !numParticlesStr.equals(numSamplesStr))
			Util.fatalError("ParticleFilter received both numParticles and numSamples properties with distinct values.");
		if (numParticlesStr == null)
			numParticlesStr = numSamplesStr;
		if (numParticlesStr == null)
			numParticlesStr = "1000";
		try {
			numParticles = Integer.parseInt(numParticlesStr);
		} catch (NumberFormatException e) {
			Util.fatalErrorWithoutStack("Invalid number of particles: "
					+ numParticlesStr); // do not dump stack.
		}

		String numMovesStr = properties.getProperty("numMoves", "1");
		try {
			Integer.parseInt(numMovesStr);
		} catch (NumberFormatException e) {
			Util.fatalErrorWithoutStack("Invalid number of moves: " + numMovesStr);
		}

		String idTypesString = properties.getProperty("idTypes", "none");
		idTypes = model.getListedTypes(idTypesString);
		if (idTypes == null) {
			Util.fatalErrorWithoutStack("Fatal error: invalid idTypes list.");
		}

		String samplerClassName = properties.getProperty("samplerClass",
				"blog.sample.LWSampler");
		System.out.println("Constructing sampler of class " + samplerClassName);
		particleSampler = Sampler.make(samplerClassName, model, properties);
		sc = new StateCollection(numParticles, new InverseParticle(idTypes, 1, particleSampler));
	}

	/** Answers the queries provided at construction time. */
	
	public void answerQueries() {
		System.err.println("this should not be called");
		System.exit(1);
		System.out.println("Evidence: " + evidence);
		System.out.println("Query: " + queries);
		resetAndTakeInitialEvidence();
		getQueryResult(queries);
	}

	/**
	 * Prepares particle filter for a new sequence of evidence and queries by
	 * generating a new set of particles from scratch, which are consistent with
	 * evidence set by {@link #setEvidence(Evidence)} (if it has not been invoked,
	 * empty evidence is assumed), ensuring behavior consistent with other
	 * {@link InferenceEngine}s.
	 */
	public void resetAndTakeInitialEvidence() {
		reset();
		//takeInitialEvidence();
	}

	private void reset() {
		System.out.println("Using " + numParticles + " particles...");
		int numTimeSlicesInMemory =  1;
		if (evidence == null)
			evidence = new Evidence();
		if (queries == null)
			queries = new LinkedList();
		sc = new StateCollection(numParticles, new InverseParticle(idTypes, numTimeSlicesInMemory, particleSampler));
	}

//	private List evidenceInOrderOfMaxTimestep;
/*
	private void takeInitialEvidence() {
		if (evidenceInOrderOfMaxTimestep == null)
			evidenceInOrderOfMaxTimestep = DBLOGUtil
					.splitEvidenceByMaxTimestep(evidence);

		for (Iterator it = evidenceInOrderOfMaxTimestep.iterator(); it.hasNext();) {
			Evidence evidenceSlice = (Evidence) it.next();
			sc.setNextEvidence(evidenceSlice);
			advanceState();	
		}
	}*/
	
	public void answerQueriesWithEvidence(){
		sc.doActionAndAnswerQueriesForAllStates();
		sc = sc.nextStateCollection;
	}

	public void getQueryResult(Collection queries) {
		sc.getQueryResult((List<Query>) queries);
	}
	
	/**
	 * Irreversibly zeroes out the collection of queries and update the histogram using only 
	 * particles corresponding to the given observability signature.
	 * @param queries
	 * @param os
	 */
	public List<Query> getQueryResultFromPartition(ObservabilitySignature os){
		return sc.getQueryResult(os);
	}


	public void takeActionWithPartition(Evidence evidence, ObservabilitySignature os) {
		sc.setActionForOS(evidence, os);
	}
	
	public void setNextEvidence(Evidence evidence){
		sc.setNextEvidence(evidence);
	}
	
	public void setNextQuery(List<Query> queries){
		sc.setNextQuery(queries);
	}
	
	public Set<ObservabilitySignature> getPartitionSet(){
		return sc.OStoAction.keySet();
	}

	private Set idTypes; // of Type

	int numParticles;
	private Sampler particleSampler;
	private AfterSamplingListener afterSamplingListener;
	public List cachedParticlesBeforeTakingEvidence;
	public StateCollection sc;
}
