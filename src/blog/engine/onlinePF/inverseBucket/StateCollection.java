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
	Map<ObservabilitySignature, List<Query>> OStoQuery = new HashMap<ObservabilitySignature, List<Query>> ();
	/**
	 * Obtains the number of times each observability signature occurs in this state
	 */
	Map<ObservabilitySignature, Double> OStoCount = new HashMap<ObservabilitySignature, Double> ();
	
	/**
	 * Constructor, initializes with the OS of a default particle in each map, 
	 * and number of particles in OStoCount is provided
	 * OStoQuery and OStoAction have empty evidence and query respectively.
	 * The other arguments are for creating particles.
	 */
	public StateCollection (int numParticles, Set idTypes, int numTimeSlicesInMemory, Sampler sampler){
		InverseParticle p = makeParticle(idTypes, numTimeSlicesInMemory, sampler);
		canonical = p;
		ObservabilitySignature os = new ObservabilitySignature(p);
		OStoAction.put(os, new Evidence());
		OStoQuery.put(os, new ArrayList<Query>());
		OStoCount.put(os, new Double(numParticles));
	}
	
	/**
	 * Constructor that does not do anything, completely empty maps
	 */
	public StateCollection (){
	}
	
	/**
	 * @return the StateCollection that is the result of performing certain actions
	 */
	public StateCollection doAction(){
		StateCollection newStateCollection = new StateCollection();
		newStateCollection.canonical = this.canonical;
		return newStateCollection;
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
	public void addParticle(InverseParticle p, Map<ObservabilitySignature, Double> os, Double scalingFactor){
		//remember to clone the OS and update them
		//remember to update all maps, as well as totalcount
	}
	
	private InverseParticle canonical;
	public Double totalCount;
	public StateCollection nextStateCollection;
	public List<Query> nextStateQueries;
}
