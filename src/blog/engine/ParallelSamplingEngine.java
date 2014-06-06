/**
 * 
 */
package blog.engine;

import java.util.Iterator;
import java.util.Properties;

import blog.BLOGUtil;
import blog.common.Timer;
import blog.model.Model;
import blog.model.Query;
import blog.sample.Sampler;

/**
 * @author ChrisXie
 * @since Jun 5, 2014
 * 
 */
public class ParallelSamplingEngine extends SamplingEngine implements Runnable {

  public ParallelSamplingEngine(Model model, Properties properties) {
    super(model, properties);
  }

  /*
   * This method is basically the answerQueries() method in SamplingEngine.
   * The difference is that each ParallelSamplingEngine has it's own sampler,
   * and needs it's own set of queries. At the end of it's sampling, it must
   * update the original query.
   */
  @Override
  public void run() {
    // boolean printed = false;
    sampler.initialize(evidence, queries);

    /*
     * // Only let Thread-0 print this out
     * System.out.println(Thread.currentThread().getName());
     * if (Thread.currentThread().getName().equals("Thread-0")) {
     * System.out.println("Evidence: " + evidence);
     * System.out.println("Query: " + queries);
     * System.out.println("Running for " + numSamples + " samples...");
     * System.out.println("Query Reporting interval is " + queryReportInterval);
     * if (numBurnIn != 0) {
     * System.out.println("(Burn-in samples: " + numBurnIn + ")");
     * }
     * }
     */

    Timer timer = new Timer();
    timer.start();

    for (int i = 0; i < numSamples; ++i) {
      // Util.debug("\nIteration ", i, ":");
      sampler.nextSample();
      double logWeight = sampler.getLatestLogWeight();

      /*
       * if (Util.verbose()) {
       * printGeneratedWorld(sampler, logWeight);
       * }
       * 
       * 
       * if (i != 0 && (i) % queryReportInterval == 0) {
       * // Print query results
       * System.out.println("======== Query Results for "
       * + Thread.currentThread().getName() + " ========");
       * System.out.println("Iteration " + i + ":");
       * for (Iterator iter = queries.iterator(); iter.hasNext();) {
       * Query q = (Query) iter.next();
       * q.printResults(System.out);
       * }
       * System.out.println("======== Done ========");
       * }
       */

      if (i >= numBurnIn) {
        if (logWeight > Sampler.NEGLIGIBLE_LOG_WEIGHT) {
          // Update statistics to reflect this sample.
          for (Iterator iter = queries.iterator(); iter.hasNext();) {
            Query query = ((Query) iter.next());

            synchronized (query) {
              // Make sure the new world supports the query variables
              BLOGUtil.ensureDetAndSupported(query.getVariables(),
              // this is not part of the sampler's
              // sampling, but sampling done on
              // top of it. Since this sampling is
              // done according to the model's
              // distribution, it still converges
              // to it.
                  sampler.getLatestWorld());
              query.updateStats(sampler.getLatestWorld(), logWeight);
            }
          }
        }

        /*
         * if ((Main.outputPath() != null)
         * && ((i + 1) % Main.outputInterval() == 0)) {
         * for (Iterator iter = queries.iterator(); iter.hasNext();) {
         * ((Query) iter.next()).logResults(i + 1);
         * }
         * }
         */
      }

      if (reportInterval != -1 && (i + 1) % reportInterval == 0) {
        System.out.println("Samples done for "
            + Thread.currentThread().getName() + ": " + (i + 1)
            + ".  \tTime elapsed: " + timer.elapsedTime() + " s.");
      }

      /*
       * if (Util.print() && logWeight > Sampler.NEGLIGIBLE_LOG_WEIGHT &&
       * !printed) {
       * printGeneratedWorld(sampler, logWeight);
       * printed = true;
       * }
       */
    }

    // Print sampler stats for this sampler
    synchronized (System.out) {
      System.out.println("Sampler stats for "
          + Thread.currentThread().getName());
      sampler.printStats();
    }
  }

}
