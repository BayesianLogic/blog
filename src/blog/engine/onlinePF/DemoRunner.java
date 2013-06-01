/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package blog.engine.onlinePF;

import blog.Main;
import blog.common.Util;
import blog.engine.ParticleFilter;
import blog.engine.ParticleFilterRunnerOnline;
import blog.engine.onlinePF.inverseBucket.UBT;
import blog.model.Evidence;
import blog.model.Model;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

/**
 *
 * @author xiang
 */
public class DemoRunner {
    
        public static String monopolyPolicyFile =
            "if (\"getState(t)==sl\" >= 0.95)"
            + "    {\"chosen_Move(openRight,t)\"}"
            + "else"
            + "    {\"chosen_Move(Listen,t)\"};";

    
    public static void main(String[] args) throws Exception {
        Collection linkStrings = Util.list();
        Collection queryStrings = Util.list("numberRentsObtained(0,@500)");
        setDefaultParticleFilterProperties();
        setModel("ex_inprog//logistics//monopoly_color.mblog");
        PolicyModel pm = PolicyModel.emptyPolicy();//PolicyModel.policyFromString(monopolyPolicyFile);
        ParticleFilterRunnerOnlinePartitioned.autoGenerateObs = false;
        //OPFevidenceGenerator.noInput= true;

     
            ParticleFilterRunnerOnline runner = new ParticleFilterRunnerOnline(model, linkStrings, queryStrings, properties);
            properties.setProperty("numParticles", "" + 1);

            UBT.runTimeTimer.startTimer();

            UBT.dataOutput.printInput("#Particles set to 1");
            //UBT.runTimeTimer.startTimer();
            runner.run();

    }

    private static void setDefaultParticleFilterProperties() {
        properties = new Properties();
        properties.setProperty("numParticles", "1");
        properties.setProperty("useDecayedMCMC", "false");
        properties.setProperty("numMoves", "1");
        Util.initRandom(true);
        Util.setVerbose(false);
    }
    private static Properties properties;
    private static ParticleFilter particleFilter;
    private static Model model;

    private static void setModel(String newModelString) {
        model = new Model();
        Evidence evidence = new Evidence();
        LinkedList queries = new LinkedList();
        Main.setupFromFiles(model, evidence, queries, Util.list(newModelString), Util.list(), true, true);
        //Main.stringSetup(model, evidence, queries, newModelString);
    }
}