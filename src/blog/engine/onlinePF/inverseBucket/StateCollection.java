package blog.engine.onlinePF.inverseBucket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import blog.engine.Particle;
import blog.engine.onlinePF.ObservabilitySignature;
import blog.engine.onlinePF.evidenceGenerator.EvidenceQueryDecisionGeneratorOnline;
import blog.model.Evidence;
import blog.model.Query;
import blog.sample.Sampler;

/**
 * represents a collection of particles belonging to all states
 * essentially a state bucket
 * @author cheng
 *
 */
public class StateCollection {
	/**
	 * obtains the action to apply to each observability signature
	 */
	Map<Integer, Evidence> OStoAction = new HashMap<Integer, Evidence> ();
	/**
	 * obtains the query result for each observability signature
	 */
	//Map<ObservabilitySignature, List<Query>> OStoQuery = new HashMap<ObservabilitySignature, List<Query>> ();
	/**
	 * Obtains the number of times each observability signature occurs in this state
	 */
	//Map<ObservabilitySignature, Double> OStoCount = new HashMap<ObservabilitySignature, Double> ();
	/**
	 * A map from State to itself
	 */
	Map<InverseParticle, HiddenState> IPtoState = new HashMap<InverseParticle, HiddenState> ();
	/**
	 * Constructor, initializes with the OS of a default particle in each map, 
	 * and number of particles in OStoCount is provided
	 * OStoQuery and OStoAction have empty evidence and query respectively.
	 * The other arguments are for creating particles.
	 */
	public StateCollection (int numParticles, InverseParticle p){
		HiddenState originalState = new HiddenState(p, this);
		ObservabilitySignature os = new ObservabilitySignature();
		os.update(p);
		Integer osIndex = os.getIndex();
		HashMap<Integer, Double> tmpOSCountMap = new HashMap <Integer, Double> ();
		tmpOSCountMap.put(osIndex, new Double(numParticles));
		originalState.addOSCounts(tmpOSCountMap);
		IPtoState.put(p, originalState);
		
		OStoAction.put(osIndex, new Evidence());
		nextTimestepEvidence = new Evidence();
		nextTimestepQueries = new ArrayList<Query>();
		
		//OStoQuery.put(os, new ArrayList<Query>());
		//OStoCount.put(os, new Double(numParticles));
		totalCount = new Double(numParticles);
	}
	
	/**
	 * updated the actions for one observability signature
	 */
	public void setActionForOS(Evidence actionEvidence, Integer osIndex){
		/*
		if (OStoAction.containsKey(os)){
			System.err.println("StateCollection.updateActionForOS : given os has already been seen");
			System.exit(1);
		}
		*/
		OStoAction.put(osIndex, actionEvidence);
		//System.err.print("updateActionForObservabilitySignature is not implemented");
		//System.exit(1);
	}
	/**
	 * called after updateActionForOS is done for all os
	 * essentially advancephase1() for the next timestep
	 */
	public void setNextEvidence(Evidence ev){
		nextTimestepEvidence = ev;
	}
	public void setNextQuery(List<Query> queries){
		nextTimestepQueries = queries;
	}
	
	/**
	 * answer the given queries for given observation signature.
	 */
	public void getQueryResult_old (List<Query> queries, ObservabilitySignature os){
		for (InverseParticle p : IPtoState.keySet()){
			HiddenState s = IPtoState.get(p);
			if (s.OStoCount.containsKey(os))
				p.updateQueriesStats(queries, s.OStoCount.get(os));
		}
	}
	/**
	 * answer the given queries for given observation signature.
	 */
	public List<Query> getQueryResult (Integer osIndex){
		return this.os_to_query.get(osIndex);
	}
	
	public void updateOSQueries (){
		for (InverseParticle p : IPtoState.keySet()){
			HiddenState s = IPtoState.get(p);
			for (Integer osIndex: s.OStoCount.keySet()){
				p.updateQueriesStats(os_to_query.get(osIndex), s.OStoCount.get(osIndex));
			}
		}
	}
	
	/**
	 * answer the given queries in general
	 */
	public void getQueryResult (List<Query> queries){
		for (InverseParticle p : IPtoState.keySet()){
			p.updateQueriesStats(queries, IPtoState.get(p).totalCount);
		}
	}
	
	/**
	 * Constructor that does not do anything, completely empty maps
	 */
	public StateCollection (){
	}
	
	/**
	 * nextStateQueries must be initialized - done separately before
	 * OStoAction must be filled up - done separately before
	 * OStoAction must be filled up with actual evidence (dummy Evidence has already been put in place) - see above
	 * nextStateCollection must be initialized - check
	 */
	public void doActionAndAnswerQueriesForAllStates(){
        ObservabilitySignature.resetBucketCount();
		nextStateCollection = new StateCollection();
		for (InverseParticle p : IPtoState.keySet()){
			if (UBT.particleCoupling)
				IPtoState.get(p).doActionsAndAnswerQueries();
			else
				IPtoState.get(p).doActionsAndAnswerQueries2();
		}
        if (UBT.debug)
        	ObservabilitySignature.updateBucketCount();
		
	}
	
	/**
	 * method that takes a inverseParticle, and its associated
	 * distribution of observabilitySignatures, and adds it to the internal maps
	 */
	public void addParticle(InverseParticle p, Map<Integer, Double> oscounts){
		if (IPtoState.containsKey(p)){
			IPtoState.get(p).addOSCounts(oscounts);
		}
		else {
			HiddenState newState = new HiddenState(p, this);
			newState.addOSCounts(oscounts);
			IPtoState.put(p, newState);
		}
		for (Integer osIndex : oscounts.keySet()){
			if (!OStoAction.containsKey(osIndex)){
				OStoAction.put(osIndex, new Evidence());
			}
			totalCount += oscounts.get(osIndex);
		}
	}
	
	
	
	
	
	public Map<Integer, List<Query>> os_to_query = new HashMap<Integer, List<Query>>();
	public Double totalCount = 0.0;
	public StateCollection nextStateCollection;
	public List<Query> nextTimestepQueries;
	public Evidence nextTimestepEvidence;
}
