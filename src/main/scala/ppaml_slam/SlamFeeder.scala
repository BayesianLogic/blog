package ppaml_slam

import java.io.File
import scala.collection.JavaConversions._

import blog.debug.FilterFeeder
import blog.model.Evidence
import blog.model.Queries
import blog.model.Model
import com.github.tototoshi.csv._

class SlamFeeder(model: Model, inputDirPath: String, maxTimesteps: Int) extends FilterFeeder {

  // Read properties file.
  val propertiesReader = CSVReader.open(new File(
    inputDirPath + "/input_properties.csv")).iterator
  val propertiesHeader = propertiesReader.next
  val propertiesValues = propertiesReader.next
  val paramL = propertiesValues(0).toDouble
  val paramH = propertiesValues(1).toDouble
  val paramA = propertiesValues(2).toDouble
  val paramB = propertiesValues(3).toDouble
  val initTheta = propertiesValues(4).toDouble
  val initY = propertiesValues(5).toDouble
  val initX = propertiesValues(6).toDouble

  // Set up readers for the sensor files.
  // These will be read lazily (in an online fashion).
  val sensorReader = CSVReader.open(new File(
    inputDirPath + "/input_sensor.csv")).iterator
  val code2sensor = Map("1" -> 'gps, "2" -> 'control, "3" -> 'laser)
  val sensorHeader = sensorReader.next
  val controlReader = CSVReader.open(new File(
    inputDirPath + "/input_control.csv")).iterator
  val controlHeader = controlReader.next
  val laserReader = CSVReader.open(new File(
    inputDirPath + "/input_laser.csv")).iterator
  val laserHeader = laserReader.next

  var timestep = -1
  var prevVelocity = 0.0
  var prevSteering = 0.0
  var prevTimestepWasGPS = false

  def hasNext: Boolean = {
    if (timestep >= maxTimesteps) {
      false
    } else {
      sensorReader.hasNext
    }
  }

  def next: (Int, Evidence, Queries) = {
    prevTimestepWasGPS = false
    val evidence = new Evidence(model)
    val queries = new Queries(model)
    val result = if (timestep == -1) {
      // Return atemporal evidence.
      evidence.addFromString(s"obs car_a = $paramA;");
      evidence.addFromString(s"obs car_b = $paramB;");
      evidence.addFromString(s"obs car_h = $paramH;");
      evidence.addFromString(s"obs car_L = $paramL;");
      evidence.addFromString(s"obs init_x = $initX;");
      evidence.addFromString(s"obs init_y = $initY;");
      evidence.addFromString(s"obs init_theta = $initTheta;");
    } else {
      // Return next timestep from the dataset.
      // Note that the controls are provided at every timestep.
      // (BLOG itself cannot get them from older timestep, because of forgetting the past.)
      // Also, the state is queried at every timestep.
      // (Otherwise we get incorrect results because of forgetting the past; see
      // https://github.com/BayesianLogic/blog/issues/330)
      val sensorLine = sensorReader.next
      val time = sensorLine(0).toDouble
      val sensor = code2sensor(sensorLine(1))
      evidence.addFromString(s"obs time(@$timestep) = $time;")
      evidence.addFromString(s"obs velocity(@$timestep) = $prevVelocity;")
      evidence.addFromString(s"obs steering(@$timestep) = $prevSteering;")
      queries.addFromString(s"query time(@$timestep);")
      queries.addFromString(s"query car_pose(@$timestep);")
      // TODO: query landmarks
      if (sensor == 'gps) {
        prevTimestepWasGPS = true
      } else if (sensor == 'control) {
        val controlLine = controlReader.next
        val controlTime = controlLine(0).toDouble
        prevVelocity = controlLine(1).toDouble
        prevSteering = controlLine(2).toDouble
        assert(Math.abs(controlTime - time) < 1e-2)
      } else if (sensor == 'laser) {
        val laserLine = laserReader.next
        val laserTime = laserLine(0).toDouble
        val laserVals = laserLineToLaserVals(laserLine)
        assert(Math.abs(laserTime - time) < 1e-2)
        // TODO: extract landmarks and provide Blip observations.
      }
    }
    evidence.compile // FIXME: clumsy interface
    queries.compile // FIXME: clumsy interface
    timestep += 1
    println("-------------")
    println(timestep - 1)
    println(evidence)
    println(queries)
    println("-------------")
    (timestep - 1, evidence, queries)
  }

  def seqToBlogColVec(seq: Seq[Double]): String = {
    "[ " + seq.mkString("; ") + "]"
  }

  def seqToBlogRowVec(seq: Seq[Double]): String = {
    "[ " + seq.mkString(", ") + "]"
  }

  def seqSeqToBlogMatrix(seqSeq: Seq[Seq[Double]]): String = {
    "[\n    " + seqSeq.map(seqToBlogRowVec).mkString(",\n    ") + "\n]"
  }

  def laserLineToLaserVals(laserCSVLine: Seq[String]): Seq[Double] = {
    // In the data, the laser readings are clockwise.
    // Make them counter-clockwise (trigonometric order).
    laserCSVLine.drop(1).take(361).reverse.map((s) => s.toDouble)
  }
}

object SlamFeeder {
  def carLocToLaserLoc(carX: Double, carY: Double, carTheta: Double, paramA: Double, paramB: Double): (Double, Double, Double) = {
    val laserX = carX + paramA * Math.cos(carTheta) + paramB * Math.cos(carTheta + Math.PI / 2)
    val laserY = carY + paramA * Math.sin(carTheta) + paramB * Math.sin(carTheta + Math.PI / 2)
    val laserTheta = carTheta
    (laserX, laserY, laserTheta)
  }

}
