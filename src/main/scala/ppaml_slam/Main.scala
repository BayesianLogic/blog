package ppaml_slam

import scala.collection.JavaConversions._
import blog.common.numerical.MatrixLib
import blog.common.Histogram
import blog.common.Util
import blog.debug.ParticleFilter
import blog.model.Evidence
import blog.model.Queries
import blog.model.Model
import java.io.PrintWriter
import java.io.File
import scala.io.Source

object Main {
  def main(args: Array[String]) = {
    if (args.length != 3) {
      throw new RuntimeException("Usage: Main param_file input_dir output_dir")
    }

    val rpc = new RPC

    // Parameter file has two lines: numParticles and maxTimesteps.
    val paramReader = Source.fromFile(args(0)).getLines
    val numParticles = paramReader.next.toInt
    val maxTimesteps = paramReader.next.toInt

    val inputDirPath = args(1)
    val outputDirPath = args(2)

    val outputPathWriter = new PrintWriter(new File(outputDirPath + "/slam_out_path.csv"))
    outputPathWriter.println("TimeGPS,GPSLat,GPSLon")

    Util.initRandom(false)
    val modelPath = getClass.getResource("slam.blog").getPath
    val model = new Model()
    val dummyEvidence = new Evidence(model)
    val dummyQueries = new Queries(model)
    blog.Main.simpleSetupFromFiles(model, dummyEvidence, dummyQueries, modelPath :: Nil)
    // Any evidence and queries from the model are ignored.
    // All the evidence and queries come from the feeder.
    val feeder = new SlamFeeder(model, inputDirPath, maxTimesteps)
    val pf = new ParticleFilter(model, numParticles, feeder)
    while (feeder.hasNext) {
      val queries = pf.advance
      if (feeder.prevTimestepWasGPS) {
        // Extract time. The histogram for time(@t) has a single value.
        // FIXME: horrible interface.
        assert(queries(0).toString().startsWith("time"))
        val time = queries(0).getHistogram().entrySet().iterator().next().asInstanceOf[Histogram.Entry].getElement

        // Extract pose from the most likely particle.
        // FIXME: horrible interface.
        assert(queries(1).toString().startsWith("car_pose"))
        var bestPose: MatrixLib = null
        var bestLogLik = Double.NegativeInfinity
        queries(1).getHistogram().entrySet().foreach(rawEntry => {
          val entry = rawEntry.asInstanceOf[Histogram.Entry]
          if (entry.getLogWeight > bestLogLik) {
            bestLogLik = entry.getLogWeight
            bestPose = entry.getElement.asInstanceOf[MatrixLib]
          }
        })
        assert(bestPose != null)

        // Output results.
        val latitude = bestPose.elementAt(1, 0)
        val longitude = bestPose.elementAt(0, 0)
        outputPathWriter.println(s"$time,$latitude,$longitude")
        outputPathWriter.flush
      }
    }
    outputPathWriter.close

    val outputMapWriter = new PrintWriter(new File(outputDirPath + "/slam_out_landmarks.csv"));
    outputMapWriter.println("GPSLon,GPSLat")
    // TODO: write obstacles
    outputMapWriter.close
  }
}
