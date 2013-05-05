package blog.engine.onlinePF.inverseBucket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import blog.engine.Particle;
import blog.engine.onlinePF.OPFevidenceGenerator;
import blog.engine.onlinePF.ObservabilitySignature;
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
	Map<ObservabilitySignature, Evidence> OStoAction = new HashMap<ObservabilitySignature, Evidence> ();
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
	Map<InverseParticle, State> IPtoState = new HashMap<InverseParticle, State> ();
	/**
	 * Constructor, initializes with the OS of a default particle in each map, 
	 * and number of particles in OStoCount is provided
	 * OStoQuery and OStoAction have empty evidence and query respectively.
	 * The other arguments are for creating particles.
	 */
	public StateCollection (int numParticles, InverseParticle p){
		State originalState = new State(p, this);
		ObservabilitySignature os = new ObservabilitySignature();
		os.update(p);
		HashMap<ObservabilitySignature, Double> tmpOSCountMap = new HashMap <ObservabilitySignature, Double> ();
		tmpOSCountMap.put(os, new Double(numParticles));
		originalState.addOSCounts(tmpOSCountMap);
		IPtoState.put(p, originalState);
		
		OStoAction.put(os, new Evidence());
		nextTimestepEvidence = new Evidence();
		nextTimestepQueries = new ArrayList<Query>();
		
		//OStoQuery.put(os, new ArrayList<Query>());
		//OStoCount.put(os, new Double(numParticles));
		totalCount = new Double(numParticles);
	}
	
	/**
	 * updated the actions for one observability signature
	 */
	public void setActionForOS(Evidence actionEvidence, ObservabilitySignature os){
		/*
		if (OStoAction.containsKey(os)){
			System.err.println("StateCollection.updateActionForOS : given os has already been seen");
			System.exit(1);
		}
		*/
		OStoAction.put(os, actionEvidence);
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
			State s = IPtoState.get(p);
			if (s.OStoCount.containsKey(os))
				p.updateQueriesStats(queries, s.OStoCount.get(os));
		}
	}
	/**
	 * answer the given queries for given observation signature.
	 */
	public List<Query> getQueryResult (ObservabilitySignature os){
		return this.os_to_query.get(os);
	}
	
	public void updateOSQueries (){
		for (InverseParticle p : IPtoState.keySet()){
			State s = IPtoState.get(p);
			for (ObservabilitySignature os: s.OStoCount.keySet()){
				p.updateQueriesStats(os_to_query.get(os), s.OStoCount.get(os));
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
		nextStateCollection = new StateCollection();
		for (InverseParticle p : IPtoState.keySet()){
			if (UBT.particleCoupling)
				IPtoState.get(p).doActionsAndAnswerQueries();
			else
				IPtoState.get(p).doActionsAndAnswerQueries2();
		}
		
	}
	
	/**
	 * method that takes a inverseParticle, and its associated
	 * distribution of observabilitySignatures, and adds it to the internal maps
	 */
	public void addParticle(InverseParticle p, Map<ObservabilitySignature, Double> oscounts){
		if (IPtoState.containsKey(p)){
			IPtoState.get(p).addOSCounts(oscounts);
		}
		else {
			State newState = new State(p, this);
			newState.addOSCounts(oscounts);
			IPtoState.put(p, newState);
		}
		for (ObservabilitySignature os : oscounts.keySet()){
			if (!OStoAction.containsKey(os)){
				OStoAction.put(os, new Evidence());
			}
			totalCount += oscounts.get(os);
		}
	}
	
	
	
	
	
	public Map<ObservabilitySignature, List<Query>> os_to_query = new HashMap<ObservabilitySignature, List<Query>>();
	public Double totalCount = 0.0;
	public StateCollection nextStateCollection;
	public List<Query> nextTimestepQueries;
	public Evidence nextTimestepEvidence;
}
