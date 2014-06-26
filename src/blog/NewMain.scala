package blog

import java.io.File
import scala.collection.JavaConversions._
import blog.common.Util
import blog.model.Model
import blog.model.Evidence
import blog.model.Queries
import blog.sample.LWSampler
import blog.engine.SamplerRunner
import blog.io.TableWriter
import blog.io.JsonWriter

/**
 * @author cberzan
 * @since Jun 26, 2014
 */
object NewMain {
  case class Config(
    engine: String = null,
    sampler: String = null,
    numSamples: Int = 0,
    inputPaths: Seq[String] = Seq(), // TODO: change String -> File
    outputPath: String = null)

  private def makeOptionsParser: scopt.OptionParser[Config] = {
    new scopt.OptionParser[Config]("blog") {
      head("blog", "0.9")

      arg[String]("<engine>") required () action { (x, c) =>
        c.copy(engine = x)
      } text ("inference engine to run")

      arg[String]("<sampler>") required () action { (x, c) =>
        c.copy(sampler = x)
      } text ("sampler to use")

      arg[Int]("<numSamples>") required () action { (x, c) =>
        c.copy(numSamples = x)
      } text ("number of samples")

      arg[String]("<model>...") required () action { (x, c) =>
        c.copy(inputPaths = c.inputPaths :+ x)
      } text ("one or more BLOG model files")

      opt[String]('o', "out") valueName ("<file>") action { (x, c) =>
        c.copy(outputPath = x)
      } text ("path for writing JSON output")

      help("help") text ("print this usage text")

      note("\nAvailable inference engines:\n") // TODO

      note("Available samplers:\n") // TODO
    }
  }

  def main(args: Array[String]): Unit = {
    makeOptionsParser.parse(args, Config()) map { config =>
      Util.initRandom(false)

      val model = new Model()
      val evidence = new Evidence(model)
      val queries = new Queries(model)
      Main.simpleSetupFromFiles(model, evidence, queries, config.inputPaths)

      if (config.engine == "sampler" && config.sampler == "lw") {
        // Run inference.
        val sampler = new LWSampler(model, evidence, queries)
        val runner = new SamplerRunner(model, evidence, queries, sampler)
        runner.sampleTimes(config.numSamples)
        // TODO progress report

        // Print query results
        val tableWriter = new TableWriter(queries)
        tableWriter.setHeader("======== Query Results =========\n"
          + "Number of samples: " + config.numSamples)
        tableWriter.writeResults(System.out)
        println("======== Done ========")

        // Write query results to file, in JSON format.
        if (config.outputPath != null) {
          println("Writing query results to " + config.outputPath + "...")
          val jsonWriter = new JsonWriter(queries)
          jsonWriter.writeResults(config.outputPath)
          println("Done.")
        }

      } else {
        throw new Exception("Unsupported engine / sampler.")
      }

    } getOrElse {
      // Bad arguments; error message printed automatically by scopt.
      System.exit(255)
    }
  }
}
