package blog.engine;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;

import blog.DBLOGUtil;
import blog.Main;
import blog.TemporalEvidenceGenerator;
import blog.bn.BayesNetVar;
import blog.common.UnaryProcedure;
import blog.common.Util;
import blog.model.ArgSpecQuery;
import blog.model.Evidence;
import blog.model.Model;
import blog.model.Query;
import blog.msg.ErrorMsg;
import blog.parse.Parse;
import blog.semant.Semant;
import blog.world.PartialWorld;


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
		evidenceGenerator = new OPFevidenceGenerator(model, linkStrings,
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
		//evidenceGenerator.moveOn(); // move generator so that evidence can be
																// obtained.
		evidenceGenerator.moveOn();
	}

	//Cheng: overrode the moveOn in particleFilterRunner, mainly to gain access to evidenceGenerator
	public boolean moveOn() {
		Evidence evidence;
		Collection queries;
		beforeEvidenceAndQueries();
		if ((evidence = getEvidence()) != null && (queries = evidenceGenerator.getLatestQueries()) != null) {
			particleFilter.take(evidence);
			particleFilter.answer(queries);
			afterEvidenceAndQueries();
			return true;
		}
		return false;
	}
	
	/**
	 * Implements method used by {@link ParticleFilterRunner} to obtain evidence
	 * for current time step.
	 */
	public Evidence getEvidence() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		
		Evidence evidence = new Evidence();
		String evistr = "";
		if (evidenceGenerator.lastTimeStep==0){
			evistr = "obs O(@0) = ResultC;";
		}
		else if (evidenceGenerator.lastTimeStep==1){
			evistr = "obs O(@1) = ResultA;";
		}
		else if (evidenceGenerator.lastTimeStep==2){
			evistr = "obs O(@2) = ResultA;";
		}
		else if (evidenceGenerator.lastTimeStep==3){
			evistr = "obs O(@3) = ResultA;";
		}
		else if (evidenceGenerator.lastTimeStep==4){
			evistr = "obs O(@4) = ResultG;";
		}
		else
			evistr = "";

		parseAndTranslateEvidence(evidence, new StringReader((String) evistr));

		evidence.checkTypesAndScope(model);
		evidence.compile();
		//evidenceGenerator.getEvidence();
		return evidence; //hackyEvidence;
		
		//Evidence evidence=null;
		//try {
			//evidence = BLOGUtil.parseEvidence_NE(br.readLine(), model);
		//} catch (IOException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}
		//evidence.checkTypesAndScope(model);
		//evidence.compile();
		//return hackyEvidence;
	}

	/**
	 * Provides the query instantiations according to current time step, for use
	 * by {@link ParticleFilterRunner}.
	 */
	public Collection getQueries() {
		return getQueriesForLatestTimestep();
	}

	//public boolean moveOn() {
	//	queriesCacheInvalid = true;
	//	return super.moveOn();
	//}

	protected void afterEvidenceAndQueries() {
		Collection queries = evidenceGenerator.getLatestQueries();
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
	protected OPFevidenceGenerator evidenceGenerator;

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

	public static void main(String[] args) throws FileNotFoundException {
		Properties properties = new Properties();
		properties.setProperty("numParticles", "10000");
		properties.setProperty("useDecayedMCMC", "false");
		properties.setProperty("numMoves", "1");
		boolean randomize = true;

		// // DBN
		// String modelFile = "examples/aircraft-wandering-DBN.mblog";
		// Collection linkStrings = Util.list("#{Blip r: Time(r) = t}");
		// Collection queryStrings =
		// Util.list("#{Blip r: Time(r) = t & Source(r) = MyAircraft}");

		// Basic case
		String modelFile = "example/hmm.dblog";
		Collection linkStrings = Util.list();
		Collection queryStrings = Util.list("S(t)");

		Util.initRandom(randomize);
		Model model = new Model();
		Evidence evidence = new Evidence();
		ArrayList<Query> queries = new ArrayList<Query>();
		ArrayList<Object> rao = new ArrayList<Object>();
		rao.add(new Object[] {new FileReader(modelFile), "blank"});



		
		Main.setup(model, evidence, queries, rao, new ArrayList(), false, false);
		Util.initRandom(true);
		new ParticleFilterRunnerOnGenerator(model,
				linkStrings, queryStrings, properties).run();
		
	}
	private static boolean parseAndTranslateEvidence(Evidence e, Reader reader) {
		Parse parse = new Parse(reader, null);
		Semant sem = new Semant(null, e, null, new ErrorMsg("no msg"));
		sem.transProg(parse.getParseResult());
		return true;
	}
	

	/** Runs until there are no evidence or queries anymore. */
	public void run() {
		int i=0;
		while (moveOn()){
			i++;
			if (i>5)
				break;
		}
	}
}
