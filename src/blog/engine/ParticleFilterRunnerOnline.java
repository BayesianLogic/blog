package blog.engine;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;

import blog.BLOGUtil;
import blog.DBLOGUtil;
import blog.Main;
import blog.TemporalEvidenceGenerator;
import blog.bn.BayesNetVar;
import blog.common.UnaryProcedure;
import blog.common.Util;
import blog.model.ArgSpecQuery;
import blog.model.ChoiceEvidenceStatement;
import blog.model.Evidence;
import blog.model.EvidenceWithChoice;
import blog.model.Model;
import blog.model.Query;
import blog.model.ValueEvidenceStatement;
import blog.msg.ErrorMsg;
import blog.parse.Parse;
import blog.semant.Semant;
import blog.world.PartialWorld;


/**
 * ParticleFilterRunnerOnGenerator extends {@link #ParticleFilterRunner} in
 * order to obtain evidence from an external stream input
 * @author Cheng
 * @since Jan 03 2013
 * 
 */
public class ParticleFilterRunnerOnline extends ParticleFilterRunner {
	public BufferedReader in;
	public InputStream eviInputStream;
	public OutputStream eviOutputStream;
	public PrintStream queryOutputStream;
	

	
	public ParticleFilterRunnerOnline(Model model, Collection linkStrings,
			Collection queryStrings, Properties particleFilterProperties) {
		super(model, particleFilterProperties);
		this.particleFilterProperties = particleFilterProperties;
		this.queryStrings = queryStrings;
		evidenceGenerator = new OPFevidenceGenerator(model, linkStrings,
				queryStrings);
		//evidenceGenerator.afterMove = afterMoveForward; // this should always be so.
		//afterMove = monitorGeneratorWorld; // this is just a default and the user can change it

		
		PipedInputStream pin = new PipedInputStream();
		PipedOutputStream pout = null;
		try {
			 pout = new PipedOutputStream(pin);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		eviInputStream = pin;
		eviOutputStream = new PrintStream(pout);
		queryOutputStream = System.out;

		in = new BufferedReader(new InputStreamReader(eviInputStream));
		Util.setVerbose(false);
	}
	
	private UnaryProcedure afterMoveForward = new UnaryProcedure() {
		public void evaluate(Object queriesObj) {
			afterMove.evaluate(queriesObj);
		}
	};

	/** Default afterMove event. */
	private UnaryProcedure monitorGeneratorWorld = new UnaryProcedure() {
		public void evaluate(Object queriesObj) {
			
		}
	};

	
	protected void beforeEvidenceAndQueries() {
		//evidenceGenerator.moveOn(); // move generator so that evidence can be
																// obtained.
		evidenceGenerator.moveOn();
	}

	//Cheng: overrode the moveOn in particleFilterRunner, mainly to gain access to evidenceGenerator
	public boolean moveOn() {
		queriesCacheInvalid = true;
		
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

		EvidenceWithChoice evidence = new EvidenceWithChoice();
		String evistr = "";
		System.out.println("Enter evi for: "+evidenceGenerator.lastTimeStep);
		try {
			evistr = in.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		parseAndTranslateEvidence(evidence, new StringReader((String) evistr));
		
		
		
		evidence.checkTypesAndScope(model);
		evidence.compile();
		
		checkEvidenceMatchesTimestep(evidence);
		
		return evidence; 

	}

	/**
	 * Provides the query instantiations according to current time step, for use
	 * by {@link ParticleFilterRunner}.
	 * NOTE: this crashes if called before the first call to moveOn()
	 */
	public Collection getQueries() {
		return getQueriesForLatestTimestep();
	}

	protected void afterEvidenceAndQueries() {
		Collection queries = evidenceGenerator.getLatestQueries();
		for (Iterator it = queries.iterator(); it.hasNext();) {
			ArgSpecQuery query = (ArgSpecQuery) it.next();

			System.out.println("PF estimate of " + query + ":");
			query.printResults(queryOutputStream);
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
	public OPFevidenceGenerator evidenceGenerator;

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
		properties.setProperty("numParticles", "1000");
		properties.setProperty("useDecayedMCMC", "false");
		properties.setProperty("numMoves", "1");
		boolean verbose = false;
		boolean randomize = false;
		
		String modelFile = "ex_inprog/logistics/simplemaze_choice.mblog";
		Collection linkStrings = Util.list();
		Collection queryStrings = Util.list("value(t)");

		Util.initRandom(randomize);
		Util.setVerbose(verbose);
		Model model = new Model();
		Evidence evidence = new Evidence();
		ArrayList<Query> queries = new ArrayList<Query>();
		ArrayList<Object> readersAndOrigins = new ArrayList<Object>();
		readersAndOrigins.add(new Object[] {new FileReader(modelFile), "blank"});



		
		Main.setup(model, evidence, queries, readersAndOrigins, new ArrayList(), verbose, false);
		ParticleFilterRunnerOnline a = new ParticleFilterRunnerOnline(model,
				linkStrings, queryStrings, properties);
		a.in=new BufferedReader(new InputStreamReader(System.in));
		a.run();
		
	}
	//need to fix the error message for empty evidence string inputs
	private boolean parseAndTranslateEvidence(Evidence e, Reader reader) {
		Parse parse = new Parse(reader, null);
		Semant sem = new Semant(model, e, new ArrayList<Query>(), new ErrorMsg("ParticleFilterRunnerOnGenerator.parseAndTranslateEvidence()")); //ignore this error message for now
		sem.transProg(parse.getParseResult());
		return true;
	}
	

	/** Runs until there are no evidence or queries anymore. */
	public void run() {
		int i=0;
		while (moveOn()){
			i++;
			//if (i>15)
			//	break;
		}
	}
	
	/**
	 * Check that the evidence provided has the correct timestep associated
	 * @param evidence
	 */
	private void checkEvidenceMatchesTimestep(Evidence evidence){
		/*do nothing*/	
	}
	

	
	
}
