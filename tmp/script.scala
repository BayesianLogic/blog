import java.util.Properties

import blog.common.Util
import blog.engine.SamplingEngine
import blog.model.Evidence
import blog.model.Model
import blog.model.Queries
import blog.sample.LWSampler

var model = Model.fromFile("tmp/burglary.blog")
var evidence = new Evidence(model)
evidence.addFromFile("tmp/burglary.evi")
var queries = new Queries(model)
queries.addFromFile("tmp/burglary.q")

model.compile()
evidence.compile()
queries.compile()
Util.initRandom(false)

var sampler = new LWSampler(model, new Properties())
sampler.initialize(evidence, queries)
/*
for (i <- 1 to 1000) {
    var oneSample = sampler.sampleOne()
    println(oneSample.world)
    println(oneSample.logWeight)
}
*/
// var samples = sampler.sample(1000)


// Debugging magic:
var sample: blog.sample.LWSample = null;

// Compute next sample, print it out, and save it for inspection.
// (Uses global vars: sample, sampler.)
def n = {
    sample = sampler.sampleOne()
    println(s"logWeight: ${sample.logWeight}  world: ${sample.world}")
}
