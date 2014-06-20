import java.util.Properties

import blog.BLOGUtil
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

class WorldWrapper(world: blog.world.PartialWorld) {
    def eval(exprStr: String): Object = {
        var result: Object = null

        // Try basic lookup first.
        if (result == null) {
            val variable = world.getBasicVarByName(exprStr)
            if (variable != null) {
                result = world.getValue(variable)
            }
        }

        // Then try evaluating an arbitrary expression.
        if (result == null) {
            val expression = BLOGUtil.parseArgSpec(exprStr, model)
            if (expression != null) {
                result = expression.evaluate(world)
            }
        }

        result
    }
}

// Debugging magic:
var sample: blog.sample.LWSample = null;

// Compute next sample, print it out, and save it for inspection.
// (Uses global vars: sample, sampler.)
def n = {
    sample = sampler.sampleOne()
    println(s"logWeight: ${sample.logWeight}  world: ${sample.world}")
}

// Testing:
n
var w = new WorldWrapper(sample.world)
w.eval("Burglary")
w.eval("JohnCalls & MaryCalls")
