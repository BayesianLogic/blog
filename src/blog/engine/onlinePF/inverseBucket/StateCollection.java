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
	public StateCollection (int numParticles, Set idTypes, int numTimeSlicesInMemory, Sampler sampler){
		InverseParticle p = makeParticle(idTypes, numTimeSlicesInMemory, sampler);
		IPtoState.put(p, new State(p, this));
		ObservabilitySignature os = new ObservabilitySignature(p);
		OStoAction.put(os, new Evidence());
		//OStoQuery.put(os, new ArrayList<Query>());
		//OStoCount.put(os, new Double(numParticles));
		totalCount = 0.0;
	}
	
	/**
	 * updated the actions for one observability signature
	 */
	public void updateActionForOS(Evidence ev, ObservabilitySignature os){
		OStoAction.put(os, ev);
		//System.err.print("updateActionForObservabilitySignature is not implemented");
		//System.exit(1);
	}
	/**
	 * called after updateActionForOS is done for all os
	 * essentially advancephase1() for the next timestep
	 */
	public void setNextQuery(List<Query> queries){
		nextTimestepQueries = queries;
	}
	
	/**
	 * answer the given queries for given observation signature.
	 */
	public void answerQuery (List<Query> queries, ObservabilitySignature os){
		for (InverseParticle p : IPtoState.keySet()){
			p.updateQueriesStats(queries, IPtoState.get(p).OStoCount.get(os));
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
	 * @return the StateCollection that is the result of performing certain actions
	 */
	public StateCollection doActionAndAnswerQueriesForAllStates(){
		nextStateCollection = new StateCollection();
		for (InverseParticle p : IPtoState.keySet()){
			IPtoState.get(p).doActionsAndAnswerQueries();
		}
		
		return nextStateCollection;
	}
	
	public void answer (OPFevidenceGenerator eviGen){
		
	}
	/**
	 * this method should be moved into the particle filter later
	 */
	protected InverseParticle makeParticle(Set idTypes, int numTimeSlicesInMemory, Sampler sampler) {
		return new InverseParticle(idTypes, numTimeSlicesInMemory, sampler);
	}
	
	/**
	 * method that takes a inverseParticle, and its associated
	 * distribution of observabilitySignatures, and adds it to the internal maps
	 */
	public void addParticle(InverseParticle p, Map<ObservabilitySignature, Double> oscounts, Double scalingFactor){
		if (IPtoState.containsKey(p)){
			IPtoState.get(p).addOSCounts(oscounts);
		}
		else {
			State newState = new State(p, this);
			newState.addOSCounts(oscounts);
			IPtoState.put(p, newState);
		}
		for (ObservabilitySignature os : oscounts.keySet()){
			if (OStoAction.containsKey(os)){
				//if (!(OStoCount.containsKey(os)) || !OStoQuery.containsKey(os)){
				//	System.err.println("StateCollection.addParticle(): discrepency in keyset for action maps");
				//	System.exit(1);
				//}
				//OStoCount.put(os, OStoCount.get(os) + oscounts.get(os));
			}
			else{
				//OStoCount.put(os, oscounts.get(os));
				OStoAction.put(os, new Evidence());
				//OStoQuery.put(os, new ArrayList<Query>());
			}
			totalCount += oscounts.get(os);
		}
		
		//remember to add to statetostate - check
		//remember to clone the OS and update them - check
		//remember to update all maps, as well as totalcount - check
	}
	
	
	
	
	
	
	public Double totalCount;
	public StateCollection nextStateCollection;
	public List<Query> nextTimestepQueries;
}
