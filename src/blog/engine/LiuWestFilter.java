package blog.engine;

import java.util.ArrayList;
import java.util.Properties;

import blog.common.Util;
import blog.model.BuiltInTypes;
import blog.model.Function;
import blog.model.Model;
import blog.model.NonRandomFunction;
import blog.world.PartialWorld;

/**
 * Liu-West filter.
 * 
 * Performs artificial evolution for the atemporal parameters of the model. In
 * a traditional particle filter, some values are sampled for these parameters,
 * and these values are never revisited. The particles with less likelihood
 * die, so we end up with a bunch of particles that all have the same values
 * for the atemporal parameters. Liu-West is supposed to prevent this
 * degeneracy, by perturbing the atemporal parameters when resampling
 * particles.
 * 
 * The central equation is (3.6) in Liu and West, "Combined parameter and state
 * estimation in simulation-based filtering", in Sequential Monte Carlo methods
 * in practice, 2001. Note that our parameter "rho" is "a" in their paper. Rho
 * is between 0 (maximum perturbation) and 1 (no perturbation). Liu and West
 * recommend a value of 0.97 - 0.99.
 * 
 * This filter currently perturbs ONLY static variables of type Real. In
 * particular, it does NOT perturb variables of other types, or random functions
 * that take arguments. (Such functionality might be added later.)
 * 
 * @author cberzan
 * @since Jun 11, 2014
 */
public class LiuWestFilter extends ParticleFilter {

  public LiuWestFilter(Model model, Properties properties) {
    super(model, properties);

    // Param that determines amount of perturbation.
    // If rho not provided, we use a default value of 0.97.
    String rhoStr = properties.getProperty("rho", "0.97");
    try {
      rho = Double.parseDouble(rhoStr);
      System.out.println("perturbation amount: " + rho);
    } catch (NumberFormatException e) {
      Util.fatalErrorWithoutStack("Invalid rho parameter: " + rhoStr);
    }

    // Precompute list of variables to perturb.
    // (This will make Liu-West only work for perturbing Real-valued
    // parameters. We can extend it to perturb vectors etc. later.)
    funcNamesToPerturb = new ArrayList<String>();
    for (Function func : model.getFunctions()) {
      if (func instanceof NonRandomFunction) {
        // We only perturb functions that are random.
        continue;
      }
      if (func.isTimeIndexed()) {
        // We only perturb functions that are static.
        continue;
      }
      if (func.getRetType() != BuiltInTypes.REAL) {
        // We only perturb functions that return a Real.
        continue;
      }
      if (func.getArgTypes().length > 0) {
        // We only perturb functions that take no arguments.
        continue;
      }
      // Function satisfies all the requirements to be perturbed.
      funcNamesToPerturb.add(func.getName());
    }
    System.out.println("funcNamesToPerturb: " + funcNamesToPerturb);
  }

  @Override
  protected void resample() {
    // Resample like a regular particle filter.
    super.resample();

    // Perturb static variables.
    // FIXME: Currently assumes they're continuous scalars.
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
        Double value = (Double) world.getValue(world
            .getBasicVarByName(funcName));
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

      System.out
          .println(funcName + " has mean " + mean + " and stdev " + stdev);

      for (int i = 0; i < particles.size(); i++) {
        particle = (Particle) particles.get(i);
        world = particle.getLatestWorld();
        Double oldValue = (Double) world.getValue(world
            .getBasicVarByName(funcName));
        double newValue = (rho * oldValue + (1 - rho) * mean + Math.sqrt(1
            - rho * rho)
            * stdev * Util.randGaussian());
        world.setValue(world.getBasicVarByName(funcName), newValue);
      }
    }
  }

  // Amount of perturbation; see class docs.
  double rho;

  private ArrayList<String> funcNamesToPerturb;
}
