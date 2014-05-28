package benchmark.blog;

import java.util.HashMap;
import java.util.Map;

import blog.common.Util;
import blog.distrib.BooleanDistrib;

/**
 * Native implementation of CSI inference algorithm.
 * Used for a performance comparison with BLOG.
 * 
 * @author awong
 */
public class CSI {

  // Parameters for model
  private static final double PROB_U = 0.3;
  private static final double PROB_V = 0.9;
  private static final double PROB_W = 0.1;
  private static final double PROB_X_IF_ARG = 0.8;
  private static final double PROB_X_NOT_ARG = 0.2;

  // Variables for model
  private boolean u;
  private boolean v;
  private boolean w;
  private boolean x;

  // Distributions for sampling in model
  private BooleanDistrib drawU;
  private BooleanDistrib drawV;
  private BooleanDistrib drawW;
  private BooleanDistrib drawXIfArg;
  private BooleanDistrib drawXNotArg;

  // Parameters for inference algorithm
  private static final int NUM_SAMPLES = 5000000;

  // Queries
  private Map<Boolean, Integer> distribX;
  private int samplesDrawn;

  public CSI() {
    Util.initRandom(false);
    distribX = new HashMap<Boolean, Integer>();

    drawU = new BooleanDistrib(PROB_U);
    drawV = new BooleanDistrib(PROB_V);
    drawW = new BooleanDistrib(PROB_W);
    drawXIfArg = new BooleanDistrib(PROB_X_IF_ARG);
    drawXNotArg = new BooleanDistrib(PROB_X_NOT_ARG);
  }

  public void runInference(int numSamples) {
    samplesDrawn = numSamples;

    for (int i = 0; i < numSamples; i++) {
      u = drawU.sampleVal_();
      v = drawV.sampleVal_();
      w = drawW.sampleVal_();
      if (u) {
        x = w ? drawXIfArg.sampleVal_() : drawXNotArg.sampleVal_();
      } else {
        x = v ? drawXIfArg.sampleVal_() : drawXNotArg.sampleVal_();
      }
      distribX.put(x, distribX.containsKey(x) ? distribX.get(x) + 1 : 1);
    }
  }

  public void printStats() {
    System.out.println("Total samples: " + samplesDrawn);
    System.out.println("\nValue of X:");
    for (Boolean val : distribX.keySet()) {
      System.out.println(val + ":\t" + (double) distribX.get(val)
          / samplesDrawn);
    }
  }

  public static void main(String[] args) {
    CSI model = new CSI();
    model.runInference(NUM_SAMPLES);
    model.printStats();
  }
}
