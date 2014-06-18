import java.util.Properties

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

var sampler = new LWSampler(model, new Properties())
sampler.initialize(evidence, queries)
var engine = new SamplingEngine(sampler)
