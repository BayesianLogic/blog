package blog;

import java.util.*;

import blog.bn.BayesNetVar;
import blog.common.UnaryProcedure;
import blog.common.Util;
import blog.model.Evidence;
import blog.model.Model;


/**
 * ParticleFilterRunnerOnGenerator extends {@link #ParticleFilterRunner} in
 * order to solve the problem of how to generate evidence for it. This is done
 * by obtaining evidence from a {@link TemporalEvidenceGenerator} . The user
 * must provide a set of link variables that are read from the generator as a
 * query and provided to the particle filter as evidence. The user must also
 * provide a (possibly empty) set of queries to be monitored (distribution
 * printed to standard output) in both generator and particle filter.
 * 
 * @author Rodrigo
 */
public class ParticleFilterRunnerOnGenerator extends ParticleFilterRunner {

	public ParticleFilterRunnerOnGenerator(Model model, Collection linkStrings,
			Collection queryStrings, Properties particleFilterProperties) {
		super(model, particleFilterProperties);
		this.particleFilterProperties = particleFilterProperties;
		this.queryStrings = queryStrings;
		evidenceGenerator = new TemporalEvidenceGenerator(model, linkStrings,
				queryStrings);
		evidenceGenerator.afterMove = afterMoveForward; // this should always be so.
		afterMove = monitorGeneratorWorld; // this is just a default and the user
																				// can change it.
	}

	private UnaryProcedure afterMoveForward = new UnaryProcedure() {
		public void evaluate(Object queriesObj) {
			afterMove.evaluate(queriesObj);
		}
	};

	/** Default afterMove event. */
	private UnaryProcedure monitorGeneratorWorld = new UnaryProcedure() {
		public void evaluate(Object queriesObj) {
			Collection queries = (Collection) queriesObj;

			System.out.println("Generator model: " + getCurrentPartialWorld());

			for (Iterator it = queries.iterator(); it.hasNext();) {
				ArgSpecQuery generatorQuery = (ArgSpecQuery) it.next();
				BayesNetVar queryVar = generatorQuery.getVariable();

				System.out.println("Value of "
						+ generatorQuery.toString().replaceAll("DerivedVar ", "")
						+ " in generator world: "
						+ getCurrentPartialWorld().getValue(queryVar));
			}
		}
	};

	protected void beforeEvidenceAndQueries() {
		evidenceGenerator.moveOn(); // move generator so that evidence can be
																// obtained.
	}

	/**
	 * Implements method used by {@link ParticleFilterRunner} to obtain evidence
	 * for current time step.
	 */
	public Evidence getEvidence() {
		return evidenceGenerator.getEvidence();
	}

	/**
	 * Provides the query instantiations according to current time step, for use
	 * by {@link ParticleFilterRunner}.
	 */
	public Collection getQueries() {
		return getQueriesForLatestTimestep();
	}

	public boolean moveOn() {
		queriesCacheInvalid = true;
		return super.moveOn();
	}

	protected void afterEvidenceAndQueries() {
		Collection queries = getQueries();
		for (Iterator it = queries.iterator(); it.hasNext();) {
			ArgSpecQuery query = (ArgSpecQuery) it.next();

			System.out.println("PF estimate of " + query + ":");
			query.printResults(System.out);
		}
	}

	/**
	 * Returns the collection of queries instantiated for current time step.
	 */
	public Collection getQueriesForLatestTimestep() {
		if (queriesCacheInvalid) {
			queries = new LinkedList();
			for (Iterator it = queryStrings.iterator(); it.hasNext();) {
				String queryString = (String) it.next();
				queries.add(getQueryForLatestTimestep(queryString));
			}
			queriesCacheInvalid = false;
		}
		return queries;
	}

	/**
	 * Returns the query obtained by instantiating a query string with the latest
	 * time step.
	 */
	private ArgSpecQuery getQueryForLatestTimestep(String queryString) {
		return DBLOGUtil.getQueryForTimestep(queryString, model,
				evidenceGenerator.lastTimeStep);
	}

	public PartialWorld getCurrentPartialWorld() {
		return evidenceGenerator.currentPartialWorld;
	}

	/** The evidence generator . */
	protected TemporalEvidenceGenerator evidenceGenerator;

	/** Properties for construction of particle filter. */
	protected Properties particleFilterProperties;

	protected Collection queryStrings;
	private Collection queries;
	private boolean queriesCacheInvalid = true;

	/**
	 * An event handler called after every move, with the collection of
	 * instantiated queries as argument, with a default that prints the value of
	 * all queries on the generator current partial world, including the ones used
	 * to obtain evidence.
	 */
	public UnaryProcedure afterMove;

	public static void main(String[] args) {
		Properties properties = new Properties();
		properties.setProperty("numParticles", "3000");
		properties.setProperty("useDecayedMCMC", "false");
		properties.setProperty("numMoves", "1");
		boolean randomize = true;

		// // DBN
		// String modelFile = "examples/aircraft-wandering-DBN.mblog";
		// Collection linkStrings = Util.list("#{Blip r: Time(r) = t}");
		// Collection queryStrings =
		// Util.list("#{Blip r: Time(r) = t & Source(r) = MyAircraft}");

		// Basic case
		String modelFile = "examples/aircraft-wandering-simplest.mblog";
		Collection linkStrings = Util.list("#{Blip r: Time(r) = t}");
		Collection queryStrings = Util.list("#{Aircraft a}");

		Util.initRandom(randomize);
		new ParticleFilterRunnerOnGenerator(Model.readFromFile(modelFile),
				linkStrings, queryStrings, properties).run();
	}
}
