package blog;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import common.Util;

import junit.framework.TestCase;
import static blog.BLOGUtil.*;

public class MiscTest extends TestCase {

    public static void main(String[] args) throws Exception {
	junit.textui.TestRunner.run(MiscTest.class);
    }

    public void testDBLOGUtilGetTimestepTermsIn() {
	Model model = Model.readFromString("random Boolean Weather(Timestep t) = true;");
	
	ArgSpec a;
	ArgSpec at10 = BLOGUtil.parseTerm_NE("@10", model);
	ArgSpec at13 = BLOGUtil.parseTerm_NE("@13", model);
	Set timesteps;
	
	a = BLOGUtil.parseArgSpec_NE("{Weather(@10), @13}", model);
	timesteps = Util.set(at10, at13);
	assertEquals(timesteps, DBLOGUtil.getTimestepTermsIn(a, Util.set()));
    }
    
    public void testSplitEvidenceByMaxTimestep() {
	Model model = Model.readFromString(
		"random Boolean Weather(Timestep);" +
		"Weather(t) = true;" +
		"random Boolean Dummy;" +
		"Dummy = true;");

	Evidence evidence;
	
	String evidenceDescription =
	    "obs Weather(@15) = true;" +
	    "obs Weather(@2) = true;" +
	    "obs Dummy = true;" +
	    "obs (Weather(@15)=true & Weather(@1)=false)=true;" +
	    "obs (Weather(@1)=true & Weather(@2)=false)=true;";
	
	evidence = parseEvidence_NE(evidenceDescription, model);
	
	List sortedEvidence = DBLOGUtil.splitEvidenceByMaxTimestep(evidence);
	
	System.out.println(Util.join(sortedEvidence, "\n"));
    }
    
    public void testParsingTupleSetSpec() { // to be removed
	Util.initRandom(true);
	Model model = Model.readFromString(
		"random Boolean Weather(Timestep);" +
		"Weather(t) ~ Bernoulli[0.8]();");
	ArgSpecQuery query = parseQuery_NE("query {Weather(t) for Timestep t : t = @0 | t = @1 | t = @2};", model);
	InferenceEngine engine = new SamplingEngine(model);
	engine.solve(query);
	query.printResults(System.out);
	assertEquals(0.512, query.getProb(Util.multiset(Util.list(true), Util.list(true), Util.list(true))), 0.1);
    }
}
