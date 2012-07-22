package test.example;

import java.util.HashMap;

/**
 * Regression test for burglary.blog
 * (correct as of July 21, 2012)
 *
 * @author akanata
 * @date July 19, 2012
 *
 */
public class TestBurglary extends ExampleTest {
        protected static final String DEF_ENGINE = "blog.engine.SamplingEngine";
        protected static final int NUM_SAMPLES = 50000;
        protected static final int NUM_BURNIN = 0;
        protected static final String DEF_SAMPLER = "blog.sample.LWSampler";
        protected static final String DEF_PROPOSER = "blog.GenericProposer";
        protected static final int STAT_INTERVAL = 2500;

        private static final double ERR_BOUND = 1.5e-2;

        public static void main(String[] args) throws IncorrectProbException {
                TestBurglary test = new TestBurglary();
                test.runTest();
        }

        public TestBurglary() {
                super(ERR_BOUND);
        }

        @Override
        public void setInputModel() {
                pathsSource.add("example/burglary.blog");
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
                HashMap<Object, Double> damage = refProbs.get(0);
                damage.put(true, 0.284);
                damage.put(false, 0.716);
        }
}
