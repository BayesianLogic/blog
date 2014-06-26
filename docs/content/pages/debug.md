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
