package blog.engine;

import java.util.Collection;
import java.util.Iterator;

import blog.BLOGUtil;
import blog.DBLOGUtil;
import blog.TemporalEvidenceGenerator;
import blog.model.ArgSpecQuery;
import blog.model.Model;
import blog.model.Query;

/**
 * 
 * @author Cheng
 *	
 * Defines/Redefines some methods in TemporalEvidenceGenerator to make it work for online particle filtering 
 */

public class OPFevidenceGenerator extends TemporalEvidenceGenerator {

	public OPFevidenceGenerator(Model model, Collection linkStrings,
			Collection queryStrings) {
		super(model, linkStrings, queryStrings);
	}
	
	
	/**
	 * This method increments the lastTimeStep,  
	 * 
	 * @return a collection of queries for the next timestep.
	 */
	private Collection getQueries (int timestep) {
		Collection a = queryInstantiator.getQueries(timestep); //Cheng: changed visibility of queryInstantiator to protected

		//now link query to the model
		boolean correct = true;
		int errors = 0;
		for (Iterator iter = a.iterator(); iter.hasNext();) {
			Query q = (Query) iter.next();
			if (!q.checkTypesAndScope(model)) { //linked to model
				correct = false;
			}
		}

		for (Iterator iter = a.iterator(); iter.hasNext();) {
			errors += ((Query) iter.next()).compile(); //compile query
		}
		if (errors > 0) {
			System.err.println("Encountered " + errors
					+ " errors in compilation of queries in OPFevidenceGenerator.getQuery().");
			System.exit(1);
		}
		
		return a;
	}
	

	/**
	 * Augments and returns the current world based on the instantiation of the
	 * query templates for the time step next to the last one used, or 0 if this
	 * is the first generation.
	 */
	public void moveOn() {
		moveOn(++lastTimeStep);
	}

	/**
	 * Augments and returns the current world based on the instantiation of the
	 * query templates for the given value of t.
	 */
	public void moveOn(int t) {
		moveOn(getQueries(lastTimeStep = t));
	}



}
