package test.blog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;
import blog.BLOGUtil;
import blog.Main;
import blog.bn.BayesNetVar;
import blog.common.Histogram;
import blog.common.Util;
import blog.engine.ParticleFilter;
import blog.engine.ParticleFilterRunnerOnline;
import blog.model.ArgSpecQuery;
import blog.model.Evidence;
import blog.model.Model;
import blog.model.ModelEvidenceQueries;
import blog.model.Query;
import blog.model.ValueEvidenceStatement;


import java.io.*;

import org.junit.Test;
/**
 * Unit testing for the {@link ParticleFilter}. Because sampling can potentially
 * fail no matter the error margin, tests sometimes fail. This should be rare,
 * however. If so, the user can check the indicated error to see if things look
 * ok, or run the test again.
 */
public class ChoiceTest extends TestCase {

	// Configuration:
	private double delta = 0.000001; // the allowed difference between
																// expected and computed values
	public static void main (String[] args){
	}
	/** Sets particle filter properties to default values before every test. */
	public void setUp() {
		Util.initRandom(false);
		setDefaultParticleFilterProperties();
	}

	private void setDefaultParticleFilterProperties() {
		properties = new Properties();
		properties.setProperty("numParticles", "5000");
		properties.setProperty("samplerClass", "blog.LWSampler");
		properties.setProperty("useDecayedMCMC", "false");
		properties.setProperty("numMoves", "1");
	}	
	
	private static final String logisticsModelString = 
			  "type Box;  type Truck;  type City;"
			+	"distinct Box b3, b2, b1;"
			+	"distinct Truck t1, t2;"
			+	"distinct City c1, c2, c3;"

			+	"choice Boolean chosen_Load(Box b, Truck tr, Timestep t) {"
			+	"  if (true) then = true"
			+	"};"

			+	"choice Boolean chosen_Unload(Box b, Truck tr, Timestep t) {"
			+	"  if (true) then = true"
			+	"};"

			+	"choice Boolean chosen_Drive(City c, Truck tr, Timestep t) {"
			+	"  if (true) then = true"
			+ 	"};"
			
			+	"random Boolean applied_Load(Box b, Truck tr, Timestep t) {"
			+	"  if (true) then = (chosen_Load(b, tr, t) & succeed_action(t))"
			+	"};"

			+	"random Boolean applied_Unload(Box b, Truck tr, Timestep t) {"
			+	"  if (true) then = (chosen_Unload(b, tr, t) & succeed_action(t))"
			+	"};"

			+	"random Boolean applied_Drive(City c, Truck tr, Timestep t) {"
			+	"  if (true) then = (chosen_Drive(c, tr, t) & succeed_action(t))"
			+ 	"};"
			
			+	"random Boolean succeed_action (Timestep t){"
			+		"~ Categorical({true -> 0.9, false -> 0.1})"
			+	"};"
			+	"random Boolean pass(Timestep t){"
			+	"  if (t==@0) then"
			+	"    = false"
			+	"  else if (BoxIn(b3, c3, t) & BoxIn(b1, c3, t) & BoxIn(b2, c3, t)) then"
			+	"    = true"
			+	"  else"
			+	"    = pass(Prev(t))"
			+	"};"

			+	"random String actionName(Timestep t){"
			+	"  if (true & exists Box b exists Truck tr (applied_Load(b,tr,t) & true)) then"
			+	"    =\"load\""
			+	"  else if (true & exists Box b exists Truck tr (true & applied_Unload(b,tr,t))) then"
			+	"    =\"unload\""
			+	"  else if (true & exists Truck tr exists City c (true & applied_Drive(c,tr,t))) then"
			+	"    =\"drive\""
			+	"  else"
			+	"    =\"na\""
			+	"};"

			+	"random Real reward(Timestep t){"
			+	"  if(exists Box b exists Truck tr (applied_Unload(b,tr,t) & (TruckIn(c3,tr,t) & BoxOn(b,tr,t)) )) then "
			+	"    = 10.0"
			+	"  else"
			+	"    = 0.0"
			+	"};"

			+	"random Real discount(Timestep t){"
			+	"  if(t==@0) then "
			+	"    = 1.0"
			+	"  else"
			+	"    = (discount(Prev(t)) * 0.9) /*gamma = 0.9*/"
			+	"};"

			+	"random Real value(Timestep t){"
			+	"  if(t == @0) then"
			+	"    =reward(@0)"
			+	"  else"
			+	"    = (value(Prev(t))) + ( reward(t) * discount(t))"
			+	"};"

			+	"random Boolean BoxIn(Box b, City c, Timestep t) {"
			+	"  if (t == @0) then"
			+	"    if (c == c1) then "
			+	"      = true"
			+	"    else"
			+	"      = false"
			+	"  else"
			+	"    = (exists Truck tr (applied_Unload(b, tr, (Prev(t))) & TruckIn (c, tr, (Prev(t)))))"
			+	"      | (BoxIn(b, c, (Prev(t))) & !(exists Truck tr (true & applied_Load(b, tr, (Prev(t))))))"
			+	"};"

			+	"random Boolean TruckIn(City c, Truck tr, Timestep t) {"
			+	"  if (t == @0) then"
			+	"    if (c == c1) then "
			+	"      = true"
			+	"    else"
			+	"      = false"
			+	"  else"
			+	"    = applied_Drive(c, tr, (Prev(t)))"
			+	"      | (TruckIn(c, tr, (Prev(t))) & !(exists City c2 (true & applied_Drive(c2, tr, (Prev(t))) & c2 != c)))"
			+	"};"

			+	"random Boolean BoxOn(Box b, Truck tr, Timestep t) {"
			+	"  if (t == @0) then"
			+	"    = false"
			+	"  else"
			+	"    = (exists City c (applied_Load(b, tr, (Prev(t))) & BoxIn(b, c, (Prev(t))) & TruckIn(c, tr, (Prev(t)))))"
			+	"      | (BoxOn(b, tr, (Prev(t))) & !(true & applied_Unload(b, tr, (Prev(t)))))"
			+	"};"



			;
	private static final String mazeModelString = 
			"type Action;"	
		+	"distinct Action up, down, left, right;"
					
		+	"choice Boolean applied_action (Action a, Timestep t){if true then = true};"
		
		+	"random Boolean succeed_action (Timestep t){"
		+	"  ~ Categorical({true -> 0.9, false -> 0.1})"
		+	"};"
			
		+	"random Integer pos (Timestep t) {"
		+	"  if (t == @0) then"
		+	"    = 1"
		+	"  else if (pos (Prev(t)) == 1 & applied_action (up, Prev(t)) & succeed_action(t)) then"
		+	"    = 2"
		+	"  else if (pos (Prev(t)) == 2 & applied_action (right, Prev(t)) & succeed_action(t)) then"
		+	"    = 3"
		+	"  else if (pos (Prev(t)) == 2 & applied_action (down, Prev(t)) & succeed_action(t)) then"
		+	"    = 1"
		+	"  else if (pos (Prev(t)) == 3 & applied_action (left, Prev(t)) & succeed_action(t)) then"
		+	"    = 2"
		+	"  else if (pos (Prev(t)) == 3 & applied_action (down, Prev(t)) & succeed_action(t)) then"
		+	"    = 4"
		+	"  else if (pos (Prev(t)) == 4 & applied_action (up, Prev(t)) & succeed_action(t)) then"
		+	"    = 3"
		+	"  else if (pos (Prev(t)) == 4 & applied_action (right, Prev(t)) & succeed_action(t)) then"
		+	"    = 5"
		+	"  else if (pos (Prev(t)) == 5 & applied_action (left, Prev(t)) & succeed_action(t)) then"
		+	"    = 4"
		+	"  else if (pos (Prev(t)) == 5 & applied_action (up, Prev(t)) & succeed_action(t)) then"
		+	"    = 6"
		+	"  else if (pos (Prev(t)) == 6 & applied_action (down, Prev(t)) & succeed_action(t)) then"
		+	"    = 5"
		+	"  else"
		+	"    = pos (Prev(t))"
		+	"};"
			
		+	"random Boolean pass(Timestep t){"
		+	"  if (t==@0) then"
		+	"    = false"
		+	"  else if (pos(t)==6) then"
		+	"    = true"
		+	"  else"
		+	"    = pass(Prev(t))"
		+	"};"
			
		+	"random Real reward(Timestep t){"
		+	"  if ( (pass(t) == true) & (pass(Prev(t)) == false) ) then" 
		+	"    = 10.0"
		+	"  else"
		+	"    = 0.0"
		+	"};"
			
		+	"random Real discount(Timestep t){"
		+	"  if(t == @0) then" 
		+	"    = 1.0"
		+	"  else"
		+	"    = (discount(Prev(t)) * 0.9) /*gamma = 0.9*/"
		+	"};"
			
		+	"random Real value(Timestep t){"
		+	"  if(t == @0) then"
		+	"    =reward(@0)"
		+	"  else"
		+	"    = (value(Prev(t))) + ( reward(t) * discount(t))"
		+	"};"
		
		;
	
	
	private void setModel(String newModelString) throws Exception {
		model = new Model();
		Evidence evidence = new Evidence();
		LinkedList queries = new LinkedList();
		Main.stringSetup(model, evidence, queries, newModelString);
	}
	/*
	@Test
	public void test_maze() throws Exception {
		Properties properties = new Properties();
		properties.setProperty("numParticles", "1000");
		properties.setProperty("useDecayedMCMC", "false");
		properties.setProperty("numMoves", "1");
		boolean randomize = true;
		Collection linkStrings = Util.list();
		Collection queryStrings = Util.list("pos(t)", "applied_action(up,t)");

		Util.initRandom(false);
		Util.setVerbose(true);

	    PipedInputStream pin = new PipedInputStream();
	    PipedOutputStream pout = new PipedOutputStream(pin);
	    
	    PrintStream out = new PrintStream(pout);
	    BufferedReader in = new BufferedReader(new InputStreamReader(pin));
	    setModel(mazeModelString);
	    ParticleFilterRunnerOnline runner = new ParticleFilterRunnerOnline(model, linkStrings, queryStrings, properties);
	    runner.eviInputStream = pin;
	    runner.in = new BufferedReader(new InputStreamReader(pin));
	    
	    out.println("obs applied_action(up, @0) = true;");
	    runner.moveOn();
	    out.println("obs applied_action(right, @1) = true;");
	    runner.moveOn();
	    out.println("obs applied_action(down, @2) = true;");
	    runner.moveOn();
	    out.println("obs applied_action(right, @3) = true;");
	    runner.moveOn();
	    out.println("obs applied_action(up, @4) = true;");
	    runner.moveOn();
	}
*/
	@Test
	public void test_logistics() throws Exception {
		Properties properties = new Properties();
		properties.setProperty("numParticles", "1000");
		properties.setProperty("useDecayedMCMC", "false");
		properties.setProperty("numMoves", "1");
		boolean randomize = true;
		Collection linkStrings = Util.list();
		Collection queryStrings = Util.list("value(t)");

		Util.initRandom(false);
		Util.setVerbose(true);

	    setModel(logisticsModelString);
	    ParticleFilterRunnerOnline runner = new ParticleFilterRunnerOnline(model, linkStrings, queryStrings, properties);
	    PrintStream out = (PrintStream) runner.eviOutputStream;
	    
	    out.println("obs chosen_Load(b1, t1, @0) = true;");
	    runner.moveOn();
	    out.println("obs chosen_Drive(c3, t1, @1) = true;");
	    runner.moveOn();
	    out.println("obs chosen_Unload(b1, t1, @2) = true;");
	    runner.moveOn();
	    out.println("obs chosen_Drive(c1, t1, @3) = true;");
	    runner.moveOn();
	    out.println("obs chosen_Load(b2, t1, @4) = true;");
	    runner.moveOn();
	    out.println("obs chosen_Drive(c3, t1, @5) = true;");
	    runner.moveOn();
	    out.println("obs chosen_Unload(b2, t1, @6) = true;");
	    runner.moveOn();
	}


	/**
	 * Helper function that gets a collection assumed to contain a single query
	 * and returns that query.
	 */
	private ArgSpecQuery getQuery(Collection singleton) {
		return (ArgSpecQuery) Util.getFirst(singleton);
	}

	private static Properties properties;
	private static ParticleFilter particleFilter;
	private static Model model;
}
