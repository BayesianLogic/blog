package blog.engine;

import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import blog.bn.BasicVar;
import blog.common.Util;
import blog.model.Function;
import blog.model.Model;
import blog.world.PartialWorld;


public class LiuWestFilter extends ParticleFilter {

  public LiuWestFilter(Model model, Properties properties) {
    super(model, properties);

    // Param that determines amount of perturbation.
    // If rho not provided, assume 1 (performs no perturbation).
    String rhoStr = properties.getProperty("rho", "1");
    try {
      rho = Double.parseDouble(rhoStr);
      System.out.println("perturbation amount: " + rho);
    } catch (NumberFormatException e) {
      Util.fatalErrorWithoutStack("Invalid rho parameter: " + rhoStr);
    }

    // Precompute list of static variables.
    funcNamesToPerturb = new ArrayList<String>();
    for (Function func : model.getFunctions()) {
      if (!func.isTimeIndexed()) {
        funcNamesToPerturb.add(func.getName());
      }
    }
    System.out.println("funcNamesToPerturb: " + funcNamesToPerturb);
  }

  @Override
  protected void resample() {
    // Resample like a regular particle filter.
    super.resample();

    // Perturb static variables.
    // FIXME: Currently assumes they're continuous scalars.
    Random rng = new Random();
    for (String funcName : funcNamesToPerturb) {
      Particle particle = (Particle) particles.get(0);
      PartialWorld world = particle.getLatestWorld();
      if (world.getBasicVarByName(funcName) == null) {
        // This variable is not present in this round.
        System.out.println("Skipping " + funcName + " because not present");
        continue;
      }

      double[] values = new double[particles.size()];
      for (int i = 0; i < particles.size(); i++) {
        particle = (Particle) particles.get(i);
        world = particle.getLatestWorld();
        Double value = (Double) world.getValue(world.getBasicVarByName(funcName));
        values[i] = value;
      }

      double mean = 0;
      for (int i = 0; i < particles.size(); i++) {
        mean += values[i];
      }
      mean /= particles.size();

      double stdev = 0;
      for (int i = 0; i < particles.size(); i++) {
        stdev += (values[i] - mean) * (values[i] - mean);
      }
      stdev = Math.sqrt(stdev / particles.size());

      System.out.println(funcName + " has mean " + mean + " and stdev " + stdev);

      for (int i = 0; i < particles.size(); i++) {
        particle = (Particle) particles.get(i);
        world = particle.getLatestWorld();
        Double oldValue = (Double) world.getValue(world.getBasicVarByName(funcName));
        double newValue = (
          rho * oldValue +
          (1 - rho) * mean +
          Math.sqrt(1 - rho * rho) * stdev * rng.nextGaussian());
        world.setValue(world.getBasicVarByName(funcName), newValue);
      }
    }
  }

  // FIXME: explain rho is 'a' in paper.
  // FIXME: debug why it's getting slower with increasing timestep.

  // Amount of perturbation; between 0 and 1.
  // Recommended value is between 0.95 and 0.99.
  double rho;

  private ArrayList<String> funcNamesToPerturb;
}
