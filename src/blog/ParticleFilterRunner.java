package blog;

import java.util.*;
import common.EZIterator;

/**
 * Wrapper of a particle filter with knowledge of which evidence
 * and queries to provide at each step. 
 * A method {@link #moveOn()} obtains evidence and queries from
 * methods
 * {@link #getEvidence(Model)} and {@link #getQueries(Model)}
 * respectively.
 * If either of these methods returns <code>null</code>,
 * execution stops and {@link #moveOn()} returns <code>false</code>.
 * Otherwise it returns <code>true</code> and waits for the next call.
 * The class is meant as a base class to be extended by classes
 * providing specialized evidence and queries appropriate to particular types of models.
 *
 * @author Rodrigo
 */
public abstract class ParticleFilterRunner {

    public ParticleFilterRunner(Model model, Properties particleFilterProperties) {
	this.model = model;
	this.particleFilter = makeParticleFilter(model, particleFilterProperties);
    }

    /**
     * Makes particle filter to be used (default {@link ParticleFilter}.
     * Useful for extensions using specialized particle filters.
     */
    protected ParticleFilter makeParticleFilter(Model model,
	    Properties particleFilterProperties) {
	return new ParticleFilter(model, particleFilterProperties);
    }
    
    public boolean moveOn() {
	Evidence evidence;
	Collection queries;
	beforeEvidenceAndQueries();
	if ((evidence = getEvidence()) != null &&
		(queries = getQueries()) != null) {
	    particleFilter.take(evidence);
	    particleFilter.answer(queries);
	    afterEvidenceAndQueries();
	    return true;
	}
	return false;	
    }
    
    /** Runs until there are no evidence or queries anymore. */
    public void run() {
	while (moveOn());
    }

    /**
     * Method called before evidence and queries are collected; does nothing by default.
     */
    protected void beforeEvidenceAndQueries() {
    }
    
    /**
     * Method called after evidence and queries are collected; does nothing by default.
     */
    protected void afterEvidenceAndQueries() {
    }
    
    /**
     * Provides evidence for each step of {@link #moveOn()};
     * it ends the process if it returns <code>null</code>. 
     */
    public abstract Evidence getEvidence();
    
    /**
     * Provides queries for each step of {@link #moveOn()};
     * it ends the process if it returns <code>null</code>. 
     */
    public abstract Collection getQueries();
    
    /** The associated model. */
    public Model model;
    
    /** The associated particle filter. */
    public ParticleFilter particleFilter;
}
