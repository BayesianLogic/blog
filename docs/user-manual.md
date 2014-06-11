BLOG can run on all operating systems that support Java. The minimal requirement is Java 1.6 or plus. 

This manual assumes you have already downloaded the latest version of BLOG and correctly unzipped or installed. If you havenot, please refer to [this](index.md).

On Windows, the BLOG command is `blog.bat`. On Linux/Mac, it is `blog`. `dblog.bat` (on Windows) and `dblog` is for running dynamic BLOG models (i.e. with `Timestep`). The following manual is based on Linux, however, you may run all these command by replacing `blog` with `blog.bat`.

# Basic Usage
The BLOG package contains a library of examples. 
One example is about burglary-earthquake network, 
first described in "Artificial Intelligence: A Modern Approach", 2nd ed., p. 494.
The model file is `example/burglary.blog`, the same as below. 

```blog
random Boolean Burglary ~ BooleanDistrib(0.001);

random Boolean Earthquake ~ BooleanDistrib(0.002);

random Boolean Alarm {
	if Burglary then
		if Earthquake then
		  ~ BooleanDistrib(0.95)
		else
		  ~ BooleanDistrib(0.94)
	else
		if Earthquake then
		  ~ BooleanDistrib(0.29)
		else
		  ~ BooleanDistrib(0.001)  
};

random Boolean JohnCalls 
	if Alarm then 
	  ~ BooleanDistrib(0.9)
	else 
	  ~ BooleanDistrib(0.05);

random Boolean MaryCalls 
  if Alarm then 
    ~ BooleanDistrib(0.7)
  else 
    ~ BooleanDistrib(0.01);

/* Evidence for the burglary model saying that both 
 * John and Mary called.  Given this evidence, the posterior probability 
 * of Burglary is 0.284 (see p. 505 of "AI: A Modern Approach", 2nd ed.).
 */
obs JohnCalls = true;
obs MaryCalls = true;

/* Query for the burglary model asking whether Burglary 
 * is true.
 */
query Burglary;
```

There is one query described in the model. It is asking whether there is burglary. 
By running the model, we expect to obtain the probability of burglary event. 

Use the following command to run the model.
```
./blog example/burglary.blog
```

If you already installed, you may run it with 
```
blog example/burglary.blog
```

By default, BLOG uses Likelihood-weighting algorithm to infer the posterior probability.  
It will draw 50,000 samples and output a probability. 
One can request 1 million samples by issuing the following command. 
```
./blog -n 1000000 example/burglary.blog
```

One can request to use the Metropolis-Hasting algorithm (as described in Milch et al 2006). 
```
./blog -s blog.sample.MHSampler example/burglary.blog
```

# Commandline options
The general form of blog command is 
```
./blog [options] <blog file1> [<blog file2> <blog file3> ...]
```
The `[options]` are optional. The orders of these options do not matter. If no option is provided, it will use LWSampler (parental likelihood-weighting algorithm), with 50,000 samples. 

The following options are provided. For every option, there is a short form and a long form. Either is acceptable. 

- Setting random seed. By default, BLOG engine always use the same random seed for easy debugging.
  `-r` or `--randomize`
  For example
```
./blog -r example/burglary.blog
```
- Use Inference engine. 
  `-e [string]` or `--engine [string]`
  By default, BLOG uses sampling engine. For dynamic models, two additional engines are provided:
  - Bootstrap Particle filter (applicable to general dynamic models)
    `-e blog.engine.ParticleFilter`
  - Liu-west Filter (Liu & West 2001), only applicable to Real static parameters.
    `-e blog.engine.LiuWestFilter`
  For example, the following command uses particle filter to run a Hidden Markov Model. 
```
./blog -e blog.engine.ParticleFilter example/hmm.dblog
```
- Controlling the accuracy by specify number of samples. By default, it is 50,000.
  '-n [integer]' or `--num_samples [integer]` 
  For example, to run the burglary model with 1 million samples.
```
./blog -n 1000000 example/burglary.blog
```
- Use a different sampling algorithm.
  `-s [string]` or `--sampler [string]`
  BLOG provides three sampling algorithms
  - rejection sampling, `-s blog.sample.RejectionSampler`
  - likelihood-weighting (default), '-s blog.sample.LWSampler'
  - Metropolis-Hasting algorithm (Milch et al 2006), `-s blog.sample.MHSampler`
- 



===========LEI LI stops here================
-q <n>, --query_report_interval <n>Report Query values after <n> samples
-i <n>, --interval <n>        Write results after every <n> samples
-b <n>, --burn_in <n>         Treat first <n> samples as burn-in
         Use sampler class <s>
-p <s>, --proposer <s>        Use Metropolis-Hastings proposer class <s>
--[no]generate                Sample worlds from prior and print them
--max_timestep <n>            If model is dynamic, generate up to <n> timesteps
-k <s>, --package <s>         Parser looks for classes in package <s>
-v, --[no]verbose             Print info about every world sampled
-d, --[no]displaycbn          Print the CBN of the sampled world
-g, --[no]debug               Print model, evidence, and queries
-w <s>, --write <s>           Write sampling results to file <s>
-h <s>, --histogram_output <s>Write histogram output to file <s>
-P<key>=<value>               Set inference configuration properties
-x <s>, --extend <s>          Extend setup with object of class <s>
-m <n>, --num_moves <n>       Use <m> moves per rejuvenation step (PF only)


It can accept an additional variable CLASSPATH to setup classpath of 
user provided distribution and library functions.
e.g. blog CLASSPATH=userdir -k User.blog example.blog
=============LEI LI stops here============================

# Running dynamic models 
For dynamic models (models with `Timestep`), one can use bootstrap particle filter. 
Bootstrap particle filter is an approximate algorithm for making inference about dynamic probabilistic model with general distributions. The following command runs a particle filter for a hidden Markov model.
```
./blog -e blog.engine.ParticleFilter example/hmm.dblog
```

The hidden Markov model describes the generative process of genetic sequences. 
```blog
type State;
distinct State A, C, G, T;

type Output;
distinct Output ResultA, ResultC, ResultG, ResultT;

random State S(Timestep t) {
    if t == @0 then ~ Categorical({A -> 0.3,
                 C -> 0.2, 
                 G -> 0.1, 
                 T -> 0.4})
    else ~ TabularCPD(
      {A -> ~ Categorical({A -> 0.1, C -> 0.3, G -> 0.3, T -> 0.3}),
       C -> ~ Categorical({A -> 0.3, C -> 0.1, G -> 0.3, T -> 0.3}),
       G -> ~ Categorical({A -> 0.3, C -> 0.3, G -> 0.1, T -> 0.3}),
       T -> ~ Categorical({A -> 0.3, C -> 0.3, G -> 0.3, T -> 0.1})},
      S(Prev(t)))
};

random Output O(Timestep t)
   ~ TabularCPD(
     {A -> ~ Categorical({ResultA -> 0.85, ResultC -> 0.05, ResultG -> 0.05, ResultT -> 0.05}),
      C -> ~ Categorical({ResultA -> 0.05, ResultC -> 0.85, ResultG -> 0.05, ResultT -> 0.05}),
      G -> ~ Categorical({ResultA -> 0.05, ResultC -> 0.05, ResultG -> 0.85, ResultT -> 0.05}),
      T -> ~ Categorical({ResultA -> 0.05, ResultC -> 0.05, ResultG -> 0.05, ResultT -> 0.85})},
     S(t));

/* Evidence for the Hidden Markov Model.
 */

obs O(@0) = ResultC;
obs O(@1) = ResultA;
obs O(@2) = ResultA;
obs O(@3) = ResultA;
obs O(@4) = ResultG;

/* Queries for the Hiddem Markov Model, given the evidence.  
 * Note that we can query S(5) even though our observations only 
 * went up to time 4.
 */

query S(@0);
query S(@1);
query S(@2);
query S(@3);
query S(@4);
query S(@5);
```

Note when using particle filtering or Liu-West filter, BLOG is answering the query at the query time. 
For example, `query S(@2)` will be answered after all evidence at `Timestep` 2. It is expected to give probability of the state at 2nd `Timestep` given all evidence at `Timestep` 0, 1, and 2.

To specify the number of particles, use `-n`. By default, BLOG uses 50,000 particles. The following command runs a particle filter with 100,000 particles. 
```
./blog -e blog.engine.ParticleFilter -n 100000 example/hmm.dblog
```
