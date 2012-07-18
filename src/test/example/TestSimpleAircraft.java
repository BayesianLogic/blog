package test.example;

import java.util.HashMap;
import java.util.Properties;

/**
 * Regression test for simple-aircraft.blog
 * (correct as of at least June 8, 2012)
 *
 * @author akanata
 * @date July 13, 2012
 *
 */
public class TestSimpleAircraft extends ExampleTest{
        protected static final String DEF_ENGINE = "blog.engine.SamplingEngine";
        protected static final int NUM_SAMPLES = 50000;
        protected static final int NUM_BURNIN = 0;
        protected static final String DEF_SAMPLER = "blog.sample.LWSampler";
        protected static final String DEF_PROPOSER = "blog.GenericProposer";
        protected static final int STAT_INTERVAL = 2500;

        private static final double errBound = 2e-2;

        private static final double MEAN_PLANES = 5;
        private static final double MEAN_BLIPS = 4;
        private static final double NUM_TERMS = 10;

        public static void main(String[] args) throws IncorrectProbException {
                TestSimpleAircraft test = new TestSimpleAircraft();
                test.runTest();
        }

        public TestSimpleAircraft() {
                super(errBound);
        }

        @Override
        public void setInferenceProps() {
                inferenceProps = new Properties();

                inferenceProps.setProperty("engineClass", DEF_ENGINE);
                inferenceProps.setProperty("numSamples", "" + NUM_SAMPLES);
                inferenceProps.setProperty("burnIn", "" + NUM_BURNIN);
                inferenceProps.setProperty("samplerClass", DEF_SAMPLER);
                inferenceProps.setProperty("proposerClass", DEF_PROPOSER);
                inferenceProps.setProperty("reportInterval", "" + STAT_INTERVAL);
        }

        @Override
        public void setInputModel() {
                pathsSource.add("example/simple-aircraft.blog");
        }

        @Override
        public void referenceProbs() {
                HashMap<Object, Double> numPlanes = refProbs.get(0);
                double totalProb = 0;

                double expPlanes = Math.exp(-1 * MEAN_PLANES);
                double expBlips = Math.exp(-1 * MEAN_BLIPS);

                // Special case for one plane (must produce 3 blips)
                double prob1 = expPlanes * MEAN_PLANES *
                                                        expBlips * Math.pow(MEAN_BLIPS, 3) / 6;
                numPlanes.put(1, prob1);
                totalProb += prob1;

                // Special case for two planes (either 3/0 or 2/1 blips)
                double prob2 = expPlanes * Math.pow(MEAN_PLANES, 2) *
                                                        ((expBlips * Math.pow(MEAN_BLIPS, 3) / 6 * expBlips) +
                                                         (expBlips * Math.pow(MEAN_BLIPS, 2) / 2 * expBlips * MEAN_BLIPS));
                numPlanes.put(2, prob2);
                totalProb += prob2;


                // General case (3+ planes, blips distributed in any combination)
                double c1 = 3, c2 = 6, c3 = 1;
                double factorial = 2;
                for (int i = 3; i <= NUM_TERMS; i++) {
                        double probI = 0;
                        factorial *= i;

                        double prob3Blips = expBlips * Math.pow(MEAN_BLIPS,  3) / 6;
                        for (int noDetect = 1; noDetect < i; noDetect++) {
                                prob3Blips *= expBlips;
                        }

                        double prob21Blips = expBlips * Math.pow(MEAN_BLIPS, 2) / 2 * expBlips * MEAN_BLIPS;
                        for (int noDetect = 2; noDetect < i; noDetect++) {
                                prob21Blips *= expBlips;
                        }

                        double prob111Blips = Math.pow(expBlips * MEAN_BLIPS, 3);
                        for (int noDetect = 3; noDetect < i; noDetect++) {
                                prob111Blips *= expBlips;
                        }

                        probI = expPlanes * Math.pow(MEAN_PLANES, i) / factorial *
                                                                        (c1 * prob3Blips + c2 * prob21Blips + c3 * prob111Blips);
                        numPlanes.put(i, probI);
                        totalProb += probI;

                        // Update the coefficients for the next iteration
                        c1 += 1;
                        c2 *= (i + 1) / (i - 1);
                        c3 *= (i + 1) / (i - 2);
                }

                // Probability table output for debugging
                for (int i = 1; i <= NUM_TERMS; i++) {
                        numPlanes.put(i, numPlanes.get(i) / totalProb);
                        System.out.println("p(" + i + ") = " + numPlanes.get(i));
                }
        }
}
