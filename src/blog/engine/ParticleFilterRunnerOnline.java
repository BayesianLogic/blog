package blog.engine;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.*;

import blog.DBLOGUtil;
import blog.Main;
import blog.common.UnaryProcedure;
import blog.common.Util;
import blog.model.ArgSpecQuery;
import blog.model.Evidence;
import blog.model.Model;
import blog.model.Query;
import blog.world.PartialWorld;


/**
 * ParticleFilterRunnerOnGenerator extends {@link #ParticleFilterRunner} in
 * order to obtain evidence from an external stream input
 * @author Cheng
 * @since Jan 03 2013
 * 
 */
public class ParticleFilterRunnerOnline extends ParticleFilterRunner {
	private BufferedReader eviReader; //evidence is read from here
	private InputStream eviInputStream;
	private PrintStream eviOutputStream; //evidence should be printed to here
	private PrintStream queryOutputStream; 
	private BufferedReader queryReader; //query is read from here
	

	
	public ParticleFilterRunnerOnline(Model model, Collection linkStrings,
			Collection queryStrings, Properties particleFilterProperties) {
		super(model, particleFilterProperties);
		this.particleFilterProperties = particleFilterProperties;
		this.queryStrings = queryStrings;

		//evidenceGenerator.afterMove = afterMoveForward; // this should always be so.
		//afterMove = monitorGeneratorWorld; // this is just a default and the user can change it

		setUpStreams();
		
		Util.setVerbose(false);
		
		evidenceGenerator = new OPFevidenceGenerator(model, queryStrings, eviReader);
	}
	
	public void setUpStreams(){
		
		PipedInputStream pin = new PipedInputStream();
		eviInputStream = pin;
		PipedOutputStream pout = null;
		try {
			 pout = new PipedOutputStream(pin);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		eviOutputStream = new PrintStream(pout);
		eviReader = new BufferedReader(new InputStreamReader(eviInputStream));


		
		
		pin = new PipedInputStream();
		try {
			pout = new PipedOutputStream(pin);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			queryOutputStream = new PrintStream("test.txt");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} //System.out;
		queryReader = new BufferedReader(new InputStreamReader(pin));

	}
	
public void setUpStreams(InputStream pin, PrintStream pout){
		eviInputStream = pin;
		eviReader = new BufferedReader(new InputStreamReader(eviInputStream));

		evidenceGenerator = new OPFevidenceGenerator(model, queryStrings, eviReader);

	}
	
	public PrintStream getEviOutput (){
		return eviOutputStream;
	}
	public BufferedReader getQueryOutput (){
		return queryReader;
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
	public boolean advancePhase1() {
		queriesCacheInvalid = true;
		
		Evidence evidence;
		Collection queries;
		beforeEvidenceAndQueries();
		evidenceGenerator.updateObservationQuery();
		if ((evidence = evidenceGenerator.getLatestObservation()) != null && (queries = evidenceGenerator.getLatestQueries()) != null) {
			particleFilter.take(evidence);
			particleFilter.answer(queries);
			afterEvidenceAndQueries();
			return true;
		}
		return false;
	}
	
	//decide applied_Load(argload(@0), t1, @0)=true;
	//
	
	
	public boolean advancePhase2() {
		Evidence evidence;
		evidenceGenerator.updateDecision();
		if ((evidence = evidenceGenerator.getLatestDecision()) != null) {	
			particleFilter.take(evidence);
			return true;
		}
		return false;
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

			//System.out.println("PF estimate of " + query + ":");
			query.printResults(queryOutputStream);
			query.printResults(System.out);//strange bug here needs fixing
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
		
		String modelFile = "ex_inprog/logistics/logistics_choice.mblog";
		Collection linkStrings = Util.list();
		Collection queryStrings = Util.list("value(t)","#{Box b: BoxIn(b, c3, t)==true}","#{Box b: BoxIn(b, c1, t)==true}", "#{Box b: applied_Load(b, t1, t)==true}");

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
		a.eviReader=new BufferedReader(new InputStreamReader(System.in));
		FileInputStream evidenceIn = new FileInputStream("ex_inprog/logistics/logistics_choice.evidence");
		a.setUpStreams(evidenceIn, System.out);
		a.run();
		
	}

	

	/** Runs until there are no evidence or queries anymore. */
	public void run() {
		int i=0;
		while (advancePhase1()&&advancePhase2()){
			i++;
			//if (i>15)
			//	break;
		}
	}

	@Override
	public Evidence getEvidence() {
		System.err.println("particlefilterrunneronline.getEvidence should not have been called");
		return evidenceGenerator.getEvidence();
	}
	


	
	
}
