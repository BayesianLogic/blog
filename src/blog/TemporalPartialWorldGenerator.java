package blog;

import java.util.Collection;
import java.util.Iterator;

import blog.common.UnaryProcedure;
import blog.common.Util;
import blog.io.TableWriter;
import blog.model.ArgSpecQuery;
import blog.model.Model;
import blog.type.Timestep;
import blog.world.DefaultPartialWorld;
import blog.world.PartialWorld;

/**
 * Generates and increasingly augments a partial world by sampling
 * self-supporting instantiations to support a set of queries at each time step.
 * The set of queries is determined by a given collection of query templates
 * that gets instantiated at each time step. Each template is a string where an
 * identifier "t" stands for the current time step. This is replaced by the
 * actual time step in order to generate the queries that will be supported
 * (thus driving the augmentation of the partial world).
 * 
 * @author Rodrigo
 */
public class TemporalPartialWorldGenerator {

  public TemporalPartialWorldGenerator(Model model, Collection queryTemplates) {
    this.model = model;
    currentPartialWorld = new DefaultPartialWorld();
    queryInstantiator = new TemporalQueriesInstantiator(model, queryTemplates);
  }

  /**
   * Augments and returns the current world based on the instantiation of the
   * query templates for the time step next to the last one used, or 0 if this
   * is the first generation.
   */
  public void moveOn() {
    moveOn(++lastTimeStep);
  }

  /**
   * Augments and returns the current world based on the instantiation of the
   * query templates for the given value of t.
   */
  public void moveOn(int t) {
    moveOn(queryInstantiator.getQueries(lastTimeStep = t));
  }

  /**
   * Augments the current world based on the given queries.
   */
  public void moveOn(Collection queries) {
    for (Iterator it = queries.iterator(); it.hasNext();) {
      ArgSpecQuery query = (ArgSpecQuery) it.next();
      BLOGUtil.ensureDetAndSupported(query.getVariable(), currentPartialWorld);
    }
    latestQueries = queries;
    if (afterMove != null)
      afterMove.evaluate(latestQueries);
    uninstantiatePreviousTimeslices(currentPartialWorld);
    BLOGUtil.removeAllDerivedVars(currentPartialWorld);
  }

  /** Provides the latest instantiated queries. */
  public Collection getLatestQueries() {
    return latestQueries;
  }

  /** Returns the template that generated a given query in the last advance. */
  public String getTemplateOf(ArgSpecQuery query) {
    return (String) queryInstantiator.getTemplateOf(query);
  }

  protected Model model;

  public Model getModel() {
    return model;
  }

  public static int findLargestTimestepIndex(PartialWorld world) {
    int largest = -1;
    Iterator timestepIndexIt = DBLOGUtil.getTimestepIndicesIterator(world);
    while (timestepIndexIt.hasNext()) {
      Integer timestepIndex = (Integer) timestepIndexIt.next();
      if (timestepIndex.intValue() > largest)
        largest = timestepIndex.intValue();
    }
    return largest;
  }

  /**
   * Identifies the largest time step in a world and uninstantiates all temporal
   * random variables with a different time step.
   */
  public static void uninstantiatePreviousTimeslices(PartialWorld world) {
    int largestTimestepIndex = findLargestTimestepIndex(world);
    if (largestTimestepIndex != -1)
      DBLOGUtil.removeVarsAtDiffTimestep(Timestep.at(largestTimestepIndex),
          world);
  }

  /**
   * The current world.
   */
  public PartialWorld currentPartialWorld;
  public int lastTimeStep = -1;
  private Collection latestQueries;
  protected TemporalQueriesInstantiator queryInstantiator;
  // Cheng: changed visibility of queryInstantiator to protected.

  /**
   * If present, this function is evaluated on the query map (indexed by
   * templates) right after it is supported in the current world.
   */
  public UnaryProcedure afterMove = null;

  public static void main(String[] args) {
    Util.initRandom(true);
    Model model = Model
        .fromFile("examples/ParticleFilteringVsLikelihoodWeightingMay2008Experiment.mblog");
    final TemporalPartialWorldGenerator gen = new TemporalPartialWorldGenerator(
        model, Util.list("query #{Blip r: Time(r) = t};"));

    final ArgSpecQuery aircraft = (ArgSpecQuery) BLOGUtil.parseQuery_NE(
        "query #{Aircraft a};", model);

    gen.afterMove = new UnaryProcedure() {
      public void evaluate(Object queriesObj) {
        Collection queries = (Collection) queriesObj;
        ArgSpecQuery query = (ArgSpecQuery) Util.getFirst(queries);
        query.updateStats(gen.currentPartialWorld);
        TableWriter tableWriter = new TableWriter(Util.list(query));
        tableWriter.writeResults(System.out);

        aircraft.updateStats(gen.currentPartialWorld);
        tableWriter = new TableWriter(Util.list(aircraft));
        tableWriter.writeResults(System.out);
      }
    };

    for (int t = 0; t < 10000; t++) {
      gen.moveOn(t);
    }
  }
}
