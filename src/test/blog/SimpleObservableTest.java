package test.blog;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;
import blog.Main;
import blog.common.Histogram;
import blog.common.Util;
import blog.engine.Particle;
import blog.engine.ParticleFilterRunnerOnline;
import blog.engine.ParticleFilter;
import blog.engine.onlinePF.ParticleFilterRunnerOnlinePartitioned;
import blog.engine.onlinePF.PartitionedParticleFilter;
import blog.model.ArgSpecQuery;
import blog.model.Evidence;
import blog.model.Model;
import blog.msg.ErrorMsg;
import blog.parse.Parse;
import blog.semant.Semant;


import java.io.*;

import org.junit.Test;
/**
 * Unit testing for the {@link ParticleFilter}. Because sampling can potentially
 * fail no matter the error margin, tests sometimes fail. This should be rare,
 * however. If so, the user can check the indicated error to see if things look
 * ok, or run the test again.
 */
public class SimpleObservableTest extends TestCase {

	// Configuration:
	private double delta = 0.000001; // the allowed difference between
							/*									// expected and computed values
	public static void main (String[] args) throws Exception{
		System.out.println(logisticsModelString);
	}*/
	/** Sets particle filter properties to default values before every test. */
	public void setUp() {
		Util.initRandom(false);
		setDefaultParticleFilterProperties();
	}

	private static void setDefaultParticleFilterProperties() {
		properties = new Properties();
		properties.setProperty("numParticles", "1000");
		properties.setProperty("useDecayedMCMC", "false");
		properties.setProperty("numMoves", "1");
		Util.initRandom(true);
		Util.setVerbose(true);
	}	

	private static final String logisticsModelStringRandomBoxes = 
			"type Move;"  
			+	"distinct Move rock, paper, scissors;"
			+	"decision Boolean chosen_Move(Move m, Timestep t);"
			+	"random Boolean applied_Move(Move m, Timestep t){if (true) then = chosen_Move(m, t)};"
			+	"random Move my_Move(Timestep t) {~ UniformChoice({Move m: (applied_Move (m, t) == true)})};"
			+	"random Boolean observable(opponent_Move(Timestep t)){~ Categorical({true -> 1.0})};"
			//+	"random Integer irrelevant (Move m, Timestep t){if (true) then = 1};"
			//+	"random Boolean observable(irrelevant(Move m, Timestep t)){if (true) then = true};"
			+	"random Move opponent_Move(Timestep t){~ UniformChoice({Move m})};"
			+	"random Integer reward (Timestep t) {"
			+	"	  if (opponent_Move(t)==rock) then"
			+	"	    if (my_Move(t)==rock)"
			+	"	      then = 0"
			+	"	    else if (my_Move(t)==paper)"
			+	"	      then = 1"
			+	"	    else "
			+	"	      = -1"
			+	"	  else if (opponent_Move(t)==paper) then"
			+	"	    if (my_Move(t)==rock)"
			+	"	      then = -1"
			+	"	    else if (my_Move(t)==paper)"
			+	"	      then = 0"
			+	"	    else "
			+	"	      = 1"
			+	"	  else if (opponent_Move(t) == scissors) then"
			+	"	    if (my_Move(t)==rock)"
			+	"	      then = 1"
			+	"	    else if (my_Move(t)==paper)"
			+	"	      then = -1"
			+	"	    else "
			+	"	      = 0"
			+	"	};"

			+	"random Real discount(Timestep t){"
			+	"  if(t==@0) then "
			+	"    = 1.0"
			+	"  else"
			+	"    = (discount(Prev(t)) * 1) /*gamma = 1*/"
			+	"};"

			+	"random Real value(Timestep t){"
			+	"  if(t == @0) then"
			+	"    = 0"
			+	"  else"
			+	"    = (value(Prev(t))) + ( reward(Prev(t)) * discount(t))"
			+	"};"



			;
	
	
	private static void setModel(String newModelString) {
		model = new Model();
		Evidence evidence = new Evidence();
		LinkedList queries = new LinkedList();
		Main.stringSetup(model, evidence, queries, newModelString);
	}
	

	@Test
	public static void main(String[] args){
	//public void test_random_logistics() throws Exception {
		Collection linkStrings = Util.list();
		Collection queryStrings = Util.list("value(t)");
		setDefaultParticleFilterProperties();
	    setModel(logisticsModelStringRandomBoxes);
	    
	    ParticleFilterRunnerOnlinePartitioned runner = new ParticleFilterRunnerOnlinePartitioned(model, linkStrings, queryStrings, properties);
	    //PrintStream out = runner.getEviCommunicator().p;
	    
	    runner.run();
	    /*
	    Thread t = new Thread(new PFRunnerThreadWrapper(runner));
	    BufferedReader br = new BufferedReader (new InputStreamReader (System.in));
	    t.start();
	    */
	    
	    //while(t.isAlive()){
	    	//String s = "";
	    	/*
	    	try {
				s = br.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			/*
	    	out.println("");
	    	runner.advancePhase1();
	    	out.println("decide chosen_Move(rock, @0) = true;");
	    	out.println("");
	    	
	    	runner.advancePhase2();
	    	*/
	    	//out.println("query opponent_Move(@0);");
	    	//out.println("");
	    	//runner.advancePhase1();
	    	/*
	    	while (true){
	    		String s = null;
		    	try {
					s = br.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    	out.println(s);
	    	}
	    	*/
	    	/*
	    	out.println("decide chosen_Move(scissors, @0) = true;");
	    	out.println("");
	    	out.println("decide chosen_Move(rock, @0) = true;");
	    	out.println("");
	    	out.println("decide chosen_Move(paper, @0) = true;");
	    	out.println("");
	    	runner.advancePhase2();
	    	//out.println("decide chosen_Move(paper, @1) = true;");
	    	//out.println("");
	    	out.println("query value(@0);");
	    	out.println("");
	    	runner.advancePhase1();
	    	out.close();
	    	*/
	    	//out.println("\n");
	    	//out.println("\n");
	    	//out.println("\n");
	    //}

	    

	}

	/**
	 * Helper function that gets a collection assumed to contain a single query
	 * and returns that query.
	 */
	private ArgSpecQuery getQuery(Collection singleton) {
		return (ArgSpecQuery) Util.getFirst(singleton);
	}
	private ArgSpecQuery getQuery(Collection singleton, int j) {
		int i =0;
		Iterator x = singleton.iterator();
		while (i!=j){
			x.next();
			i++;
		}
		return (ArgSpecQuery) x.next();
	}


	private static Properties properties;
	private static ParticleFilter particleFilter;
	private static Model model;
	
	public static class PFRunnerThreadWrapper implements Runnable{

		private ParticleFilterRunnerOnlinePartitioned pfr;
		public PFRunnerThreadWrapper(ParticleFilterRunnerOnlinePartitioned pfr){
			this.pfr = pfr;
		}
		@Override
		public void run() {
			pfr.run();
		}
		
	}
}
