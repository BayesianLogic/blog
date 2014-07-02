package test.example;

import java.util.HashMap;

/**
 * Regression test for hurricane.blog
 * (correct as of July 19, 2012)
 *
 * @author awong
 * @date July 19, 2012
 *
 */
public class TestHurricane extends ExampleTest {
        protected static final String DEF_ENGINE = "blog.engine.SamplingEngine";
        protected static final int NUM_SAMPLES = 50000;
        protected static final int NUM_BURNIN = 0;
        protected static final String DEF_SAMPLER = "blog.sample.LWSampler";
        protected static final String DEF_PROPOSER = "blog.GenericProposer";
        protected static final int STAT_INTERVAL = 2500;

        private static final double ERR_BOUND = 5e-3;

        public static void main(String[] args) throws IncorrectProbException {
                TestHurricane test = new TestHurricane();
                test.runTest();
        }

        public TestHurricane() {
                super(ERR_BOUND);
        }

        @Override
        public void setInputModel() {
                pathsSource.add("example/hurricane.blog");
        }

        @Override
        public void setInferenceProps() {
                inferenceProps.setProperty("engineClass", DEF_ENGINE);
                inferenceProps.setProperty("numSamples", "" + NUM_SAMPLES);
                inferenceProps.setProperty("burnIn", "" + NUM_BURNIN);
                inferenceProps.setProperty("samplerClass", DEF_SAMPLER);
                inferenceProps.setProperty("proposerClass", DEF_PROPOSER);
                inferenceProps.setProperty("reportInterval", "" + STAT_INTERVAL);
        }

        @Override
        public void referenceProbs() {
        		HashMap<Object, Double> first = refProbs.get(0);
        		first.put(model.getConstantValue("A"), 0.5);
        		first.put(model.getConstantValue("B"), 0.5);
        		
        		HashMap<Object, Double> damageA = refProbs.get(1);
        		damageA.put(model.getConstantValue("Severe"), 0.63);
        		damageA.put(model.getConstantValue("Mild"), 0.37);
        		
        		HashMap<Object, Double> damageB = refProbs.get(2);
        		damageB.put(model.getConstantValue("Severe"), 0.63);
        		damageB.put(model.getConstantValue("Mild"), 0.37);
        	
                HashMap<Object, Double> damage = refProbs.get(3);
                damage.put(model.getConstantValue("Mild"), 0.74);
                damage.put(model.getConstantValue("Severe"), 0.26);
        }
}
