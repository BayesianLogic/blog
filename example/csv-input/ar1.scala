import scala.collection.JavaConversions._
import scala.io.Source

import blog.Main
import blog.common.Util

val model = new Model()
val evidence = new Evidence(model)
val queries = new Queries(model)
Main.simpleSetupFromFiles(model, evidence, queries, "example/csv-input/ar1.model" :: Nil)

// Read CSV line by line, skipping the first line.
// Convert to obs statements, using the line number as a timestep.
Source.fromFile("example/csv-input/ar1.csv").getLines.drop(1).zipWithIndex.foreach {
  case (line, lineno) =>
    evidence.addFromString(s"obs NoisyObs(@${lineno}) = ${line};")
}

Util.initRandom(false)
val d = new LWDebugger(model, evidence, queries)
import d._
