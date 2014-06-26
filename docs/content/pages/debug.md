title: Debugging BLOG models
status: hidden
date: 2014-06-25 17:09
sortorder: 15


# Status

The BLOG interactive shell and debugger are alpha-grade features. They might
contain bugs, and they might change drastically from version to version.


# Introduction

Start the BLOG interactive shell using the `iblog` command. In the examples
below, lines prefixed with `scala>` are what you type into the shell. Lines
without that prefix are output printed by the shell.


# Example: Debugging Likelihood Weighting

Create a LW sampler for the burglary model:

```text
scala> val d = LWDebugger.make("example/burglary.blog")
Using fixed random seed for repeatability.
d: blog.debug.LWDebugger = blog.debug.LWDebugger@767dfcb6
```

Investigate the model, evidence, and queries:

```text
scala> d.model.print(System.out)
random Boolean Burglary
    if true then ~ blog.distrib.BooleanDistrib()
random Boolean Earthquake
    if true then ~ blog.distrib.BooleanDistrib()
random Boolean Alarm
    if ((Burglary = true) & (Earthquake = true)) then ~ blog.distrib.BooleanDistrib()
    elseif (Burglary = true) then ~ blog.distrib.BooleanDistrib()
    elseif (Earthquake = true) then ~ blog.distrib.BooleanDistrib()
    elseif true then ~ blog.distrib.BooleanDistrib()
random Boolean JohnCalls
    if (Alarm = true) then ~ blog.distrib.BooleanDistrib()
    elseif true then ~ blog.distrib.BooleanDistrib()
random Boolean MaryCalls
    if (Alarm = true) then ~ blog.distrib.BooleanDistrib()
    elseif true then ~ blog.distrib.BooleanDistrib()

scala> d.evidence
res0: blog.model.Evidence = [JohnCalls = true, MaryCalls = true]

scala> d.queries
res1: blog.model.Queries = [Burglary]
```

Import trick to avoid having to type `d.<method>` every time:

```text
scala> import d._
import d._
```

Get some samples:

```text
scala> n
LWSample(logWeight: -7.600902459542082, world: {Basic: {Earthquake=false, Alarm=false, MaryCalls=true, JohnCalls=true, Burglary=false}, Derived: {}})

scala> n
LWSample(logWeight: -7.600902459542082, world: {Basic: {Earthquake=false, Alarm=false, MaryCalls=true, JohnCalls=true, Burglary=false}, Derived: {}})

scala> n
LWSample(logWeight: -7.600902459542082, world: {Basic: {Earthquake=false, Alarm=false, MaryCalls=true, JohnCalls=true, Burglary=false}, Derived: {}})
```

All samples are stored in `samples`, and the latest samples is stored in `s`.

```text
scala> samples.size
res6: Int = 3

scala> s
res8: blog.debug.LWSample = LWSample(logWeight: -7.600902459542082, world: {Basic: {Earthquake=false, Alarm=false, MaryCalls=true, JohnCalls=true, Burglary=false}, Derived: {}})

scala> s.logWeight
res18: Double = -7.600902459542082
```

We can evaluate arbitrary expressions in the possible world of a sample:

```text
scala> s.eval("Burglary")
res10: Object = false

scala> s.eval("JohnCalls")
res11: Object = true

scala> s.eval("JohnCalls & MaryCalls")
res12: Object = true

scala> s.eval("1 + 2 + 3")
res13: Object = 6
```

Run many samples, and then print out the query results:

```text
scala> sampleMany(10000)
[... output elided ...]

scala> hist
Distribution of values for Burglary
    0.7235478031923686  false
    0.27645219680763833 true
```


# Example: Debugging Metropolis-Hastings

The MH sampler is very similar, but its samples provide more information:

```text
scala> val d = MHDebugger.make("example/aircraft-static.blog")
Using fixed random seed for repeatability.
Constructing M-H proposer of class blog.sample.GenericProposer
d: blog.debug.MHDebugger = blog.debug.MHDebugger@370a44f7

scala> 

scala> import d._
import d._

scala> n
MHSample(accepted: true, world: {Basic: {b1=(Blip, 3), #Blip=3, #Blip(Source = (Aircraft, 4))=0, #Blip(Source = (Aircraft, 1))=0, #Blip(Source = (Aircraft, 2))=1, #Aircraft=4, #Blip(Source = (Aircraft, 3))=0}, Derived: {}})

scala> s.dump
MHSample:
world: {Basic: {b1=(Blip, 3), #Blip=3, #Blip(Source = (Aircraft, 4))=0, #Blip(Source = (Aircraft, 1))=0, #Blip(Source = (Aircraft, 2))=1, #Aircraft=4, #Blip(Source = (Aircraft, 3))=0}, Derived: {}}
accepted: true
chosenVar: b1
chosenVarOldValue: (Blip, 2)
chosenVarNewValue: (Blip, 3)
logProposalBackward: -3.332204510175204
logProposalForward: -3.332204510175204
logProbRatio: 0.0
logAcceptRatio: 0.0
```

Each sample contains the following fields:

- `world`: the PartialWorld in the sample (a new world if the proposal was
  accepted, the old world otherwise)
- `accepted`: whether the proposal was accepted
- `chosenVar`: the variable that the GenericProposer chose to modify
- `chosenVarOldValue`: the chosen variable's value in the old world
- `chosenVarNewValue`: the chosen variable's value in the new world
- `logProposalBackward`: `log q(x | x')`
- `logProposalForward`: `log q(x' | x)`
- `logProbRatio`: `log( p(x') / p(x) )`
- `logAcceptRatio`: `log( (p(x') * q(x | x')) / (p(x) * q(x' | x)) )`

Where `p` is the true probability given the evidence, and `q` is the proposal
probability.


# Example: Debugging the Particle Filter

You can run the particle filter one step at a time using the `advance` method,
and inspect the particles on the way. Note that if there is atemporal evidence
provided, it will be processed before the first timestep. (That's why you see
"processed timestep -1" in the output below.) If there are atemporal queries,
they will be processed after the last timestep. (That's why you see "procesed
timestep 11" twice in the output below.)

```text
scala> val d = ParticleFilter.make("example/pf-test.blog", 10)
Using fixed random seed for repeatability.
d: blog.debug.ParticleFilter = blog.debug.ParticleFilter@13973a69

scala> import d._
import d._

scala> advance
advance: processed timestep -1

scala> advance
advance: processed timestep 1

scala> d.currentEvidence
res2: blog.model.Evidence = [x(@1) = 0.1]

scala> d.currentQueries
res3: blog.model.Queries = null

scala> advanceUntilFinished
advance: processed timestep 3
advance: processed timestep 5
advance: processed timestep 10
advance: processed timestep 11
advance: processed timestep 11

scala> d.currentEvidence
res13: blog.model.Evidence = null

scala> d.currentQueries
res12: blog.model.Queries = [/*DerivedVar*/ mu]

scala> particles.size
res5: Int = 10

scala> val p = particles(2)
p: blog.debug.Particle = Particle(logWeight: -0.9387766035733844, world: {Basic: {something=3.0, x(@11)=1.2672851367814115}, Derived: {}})

scala> p.eval("something")
res6: Object = 3.0

scala> particles.foreach(particle => println(particle.eval("x(@11)")))
-0.16279602410074512
1.2672851367814115
1.2672851367814115
-1.0869420759611352
1.2672851367814115
0.21966510934467814
-2.4999577147848684
-0.2898923550912115
-0.2898923550912115
1.0845004791444346
```
