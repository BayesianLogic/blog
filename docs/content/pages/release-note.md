title: Release Note
status: hidden
date: 2014-06-11 15:11
sortorder: 50

BLOG 0.8
-------------------
* make target for compiling with debug support (make debug)
* make SamplingEngine work with log weights
    - modify Histogram to use log weights consistently
    - remove unused class LogHistogram
    - LWSampler supports the calcuation of weight in log domain
    - particle filter operate in log domain 
* adding syntax highlight support for sublime 
* Add Multinomial distribution, which accept both fixed and random arguments
* Categorical now accept three forms of arguments:
    1. a Map from distinct or fixed symbol to real constants
    2. a Map from distinct or fixed symbol to real random variable
    3. a weight array
* MultivarGaussian now natively calculate getLogProb()
* Added builtin type `RealMatrix`
* Adding builtin functions
    - linear algebra operators: +, -, *, inv, det, transpose, 
    - sum a matrix column-wise
    - vstack to create a matrix from rows
    - __SCALAR_STACK to create a column matrix from scalar variables
    - array/matrix indexing (subscript index can be random as well)
    - sin, cos, tan, atan2 functions 
    - builtin constant pi
    - eye, zeros, ones builtins
* use factory pattern instead of importing JamaMatrixLib everywhere


BLOG 0.7 (27 December 2013)
-------------------
* Added a special distribution Size, to calculate the size of a Set.
* Poisson now supports a random variable as its argument.
* Renaming distribution: 
    - Bernoulli => BooleanDistrib
    - BinaryBernoulliDistrib => Bernoulli
* update support of blog.vim and lexer.py to support syntax updates
* new model: 
    - Birthday collision
    - Tug-of-war
* more robust error handling in syntax
* Syntax highlight style file for emacs (created by Wei Wang)
* Abstract syntax tree is now printed in a nicer format (pprinter)
* maintenance fix for webui
    - web server can now automatically start as a deamon
    - added an option (--test) for start-server.sh for testrun of the server
    - removed duplicated web/run.sh and use the one in parent directory instead
* automatic regression test
* Bug-fix
    - add testing equality of matrix by checking elements 
      (previous caused problem on copied possible world)
    - fix the key removal in linkedHashMap in InstantiationEvalContext 
      (previously remove the value rather than key)
    - fix the incorrect parsing of sub_array expression within LBRACE and RBRACE 
      (due to a reduce-reduce conflict with array type)
    - web server python script can now successfully parse results in scientific 
      format (e.g. 1E-5)

BLOG 0.6 (5 April 2013)
-------------------
* Changes to built-in functions
    - Syntax changes: Concat replaced with +
    - New operations: ^ (power), => (implies), <= (leq), >= (geq), min of set, max of set, round
* Support for queries on tuple sets, 
    - queries of form {f(x) for x: cond(x)} (last supported in v0.3)
* New models:
    - background subtraction: for known, unknown numbers of components
    - computer vision: and-or trees, projection, scene viewer
    - Gaussian mixture model
    - stochastic volatility
* Miscellaneous bug fixes and improvements, e.g.:
    - `<` now may be used in implicit sets, as may `>` in conditions of form x > a && x < b (where a < b) 
    - UniformVector distribution now consistent with current matrix implementation
    - Dirichlet distribution now accepts arguments of form {dims, unnormalized weight}

BLOG 0.5 (23 December 2012)
-------------------
* Support of array (instead of using RkVector)
* Support arithmetic operators 
* Matrix operations supported, +, -, *, and reference to array element 
* Reference to distinct objects the same as reference to array element 
* Correct MHSampler (old one in blog 0.3 produces incorrect answers with indirect evidence) 
* Fixed Particle filtering 
* Added sveral new models, including black-jack-one, kalman filter, LDA, AR2 
* Several new distributions, Dirichlet, MultivarGaussian

BLOG 0.4 (3 September 2012)
-------------------
* Support of Map data structure;
* Categorical Distribution and TabularCPD using Maps;
* Uniform argument representation in CPD;
* Nested if-then-else in dependency statement;
* Support of Array, Matrix (Real Array)
* Support of arithmetic operators, and linear algebra operators.
* Without causing confusion, 
    - "nonrandom" is changed to "fixed", 
    - "guaranteed" is changed to "distinct";
* Complete redesign of parser and semantic translator, 
  towards parsing efficiency and modularity.


BLOG 0.3 (14 August 2008)
-------------------
* Added a module for exact inference using variable elimination (VE).  
  This inference engine can be invoked by running the runblog script 
  with the option -e ve.VarElimEngine.  It works only on BLOG models 
  without unknown objects, that is, without number statements.
* Added an implementation of first-order variable elimination with 
  counting formulas (C-FOVE) as presented by B. Milch, L. S. Zettlemoyer, 
  M. Haimes, and L. P. Kaelbling at AAAI 2008.  This is an exact 
  inference algorithm that exploits symmetries between objects to run 
  faster than the standard VE algorithm.  Like VE, it is currently 
  applicable only to BLOG models without unknown objects.  It can be 
  invoked with the flag -e fove.LiftedVarElim.  
* Added the ability to specify undirected dependencies in BLOG model
  files using parfactors [D. Poole (2003) "Lifted Probabilistic
  Inference", Proc. 18th IJCAI].  The syntax for parfactors is
  explained in the brief tutorial on the BLOG web site.  The VE and
  C-FOVE algorithms operate on both these explicitly specified
  parfactors, and parfactors constructed automatically from BLOG
  dependency statements.  The sampling-based inference algorithms
  (likelihood weighting, etc.) currently ignore any explicitly
  specified parfactors.
* Added a built-in type Timestep for use in dynamic BLOG (DBLOG) models.  
  This type has guaranteed objects in one-to-one correspondence with the 
  natural numbers.  Timestep literals are represented in BLOG files as 
  natural numbers prefixed with an @-sign, such as @0, @1, @2, etc.  By 
  convention, functions that vary over time take a Timestep as their 
  last argument; an example is State(Aircraft, Timestep).  The built-in 
  function Prev maps each timestep to the previous timestep (@0 is 
  mapped to Model.NULL).  Internally, objects of type Timestep are 
  represented as instances of class blog.Timestep.  
* This release includes a prototype particle filtering engine for DBLOG 
  models (developed by Rodrigo de Salvo Braz based on initial code by 
  Keith Henderson).  It can be invoked with -e blog.engine.ParticleFilter.  If 
  the useDecayedMCMC property is set to true with -PuseDecayedMCMC=true, 
  the particle filter will do an iteration of MCMC on each particle at 
  each time step (the number of MCMC iterations can be changed by setting 
  the numMoves property).  This allows the filter to recover from poor 
  initial guesses about atemporal variables.  Specifically, the particle 
  filter uses a decayed MCMC proposer [B. Marthi, H. Pasula, S. Russell, 
  and Y. Peres (2002) "Decayed MCMC Filtering", Proc. 18th UAI].  
* Several bug fixes and internal improvements.


BLOG 0.2 (14 December 2007)
-------------------
* The functions formerly called "generating functions" are now called
  "origin functions".  The keyword "origin" is now preferred for
  declaring these functions (although "generating" is still supported).
* BLOG now includes statements that are both random function declarations 
  and dependency statements.  That is, you can write something like:
  random Color ObsColor(Draw d) 
     ~ TabularCPD[[0.8, 0.2], [0.2, 0.8]](TrueColor(BallDrawn(d)));
  This statement simultaneously declares ObsColor as a random function 
  that takes a Draw and returns a Color, and specifies the dependency 
  model for this function.  
* Functions can now be overloaded.  That is, you can declare several
  functions with the same name, as long as they have different
  argument types.  The mapping from identifiers to Function objects
  is now resolved in FuncAppTerm.checkTypesAndScope, after the types
  of any logical variables in the function application term have been
  determined.
* New CPD classes in blog.distrib: BoundedGeometric, CharDistrib, 
  ChooseFromArgs, Iota, MixtureDistrib, StringEditModel, 
  StringEditModelWithJumps, UniformVectorDistrib.
* The code for evaluating set expressions can now compactly represent
  sets that consist of all the objects of a given type except for a
  few excluded objects.  For instance, suppose we are sampling
  authors for a paper p without replacement:
  NthAuthor(p, n) 
    ~ UniformChoice({Researcher r : !exists NaturalNum m
                          ((m < n) & (NthAuthor(p, m) = r))});
  Previously, the set passed to UniformChoice would have been
  represented by explicitly listing all the Researcher objects that
  did not appear earlier in the author list.  Thus, the time to
  compute acceptance probabilities for MCMC moves affecting NthAuthor
  grew linearly with the number of hypothesized researchers.  The new
  representation does not depend on the number of hypothesized
  researchers: it grows only with the length of the author list.
  This capability is implemented by the POPAppBasedSet class and
  CompiledSetSpec.getExplicitVersion.
* Lots of other changes "under the hood".  Most notably, the
  MutablePartialWorld class no longer exists.  Instead, the
  PartialWorld interface includes mutator methods, and there is a
  class PartialWorldDiff that can represent a set of changes relative
  to any underlying PartialWorld object.  Also, ObjectIdentifiers are
  now handled in a different way.  When you are iterating over the
  extension of a type that is represented using ObjectIdentifiers,
  and you get to an object for which no ObjectIdentifier has been
  created yet, the PartialWorld now creates a new ObjectIdentifier
  automatically (rather than returning an IdentifierHook object as it
  did before).  The PartialWorld maintains a distinction between
  ObjectIdentifiers that have been asserted by the client to exist,
  and ObjectIdentifiers that have only been used as return values and
  thus can be removed if necessary.
* Lots of bug fixes.

BLOG 0.1.6 (16 March 2007)
---------------------
* Fixed a bug in common/BipartiteMatcher.java that sometimes caused
  it to return incomplete or non-optimal matches.  Thanks go to the
  users who reported this bug.


BLOG 0.1.5 (13 January 2006)
---------------------
* Changed the parser so it no longer checks for type and scope
  errors, but leaves this to a separate phase of checking in the main
  body of the code.  This allows proper handling of tuple set
  specifications, which cannot be scope-checked in the parser because
  variables are not declared until after they are used.  It also
  ensures that statements and formulas that are added
  programmatically are still checked thoroughly.
* Fixed bug in common/AbstractTupleIterator.java that prevented tuple
  set specifications from working properly.

BLOG 0.1.4 (21 December 2005)
---------------------
* Restored parser's ability to read number statements in the old syntax, 
  as well as the new syntax introduced in version 0.1.3.  
* Added built-in non-random function Concat that concatenates two strings.
* Corrected bugs in blog.distrib.Geometric.
* Changed name of blog.distrib.BernoulliDistrib to blog.distrib.Bernoulli, 
  and added ability to specify the success probability as a CPD argument.
* Corrected bug that caused TabularCPD to throw an exception when a list 
  of explicitly specified probabilities did not sum to one.
* Corrected bug that caused the Boolean values "true" and "false" to be 
  swapped in the displayed query results.
* Corrected error in blog/SymbolEvidenceStatement.java that prevented it 
  from compiling under Java 1.4.

Changes in BLOG 0.1.3 (6 November 2005)
---------------------
* Changed the syntax for number statements in an effort to make them
  clearer.  For example, the number statement for radar blips
  generated by a given aircraft at a given time step is now:
   #Blip(Source = a, Time = t) ~ DetectionDistrib(State(a, t));
  We believe this is much clearer than the old syntax, which was:
   #Blip: (Source, Time) -> (a, t) ~ DetectionDistrib(State(a, t));
  Note that the syntax for number statements with no generating objects 
  (such as #Ball ~ Poisson[6]) is unchanged.
* Non-random constant and function definitions can now contain other
  non-random constants and functions.  The inference engine checks
  that these definitions are acyclic.
* Matrices that are written out in brackets can now contain arbitrary 
  number-valued terms, not just numbers.
* The observed value of a term in an observation statement can now be any 
  term, not just a constant symbol.  
* Added several new CPDs to the blog.distrib package: Binomial,
  NegativeBinomial, UniformReal, Exponential, Gamma, and Beta.
* Replaced blog.distrib.MultinomialDistrib with a new
  blog.distrib.Categorical CPD, which allows the parameter vector to
  be given as an argument.
* Added a new file, examples/aircraft-wandering.mblog, containing a
  simplified version of the aircraft tracking model from [Milch et
  al., IJCAI 2005].
* The command line options that direct query results to files work now.
* Recursive number statements no longer cause stack overflow errors.

BLOG 0.1.2 (27 September 2005)
---------------------
* Fixed numerical inaccuracy in the getProb method in
  blog/distrib/Poisson.java.  It now avoids overflow by calling
  getLogProb and exponentiating the result.  This problem was hidden
  in BLOG 0.1, but in version 0.1.1 it caused Metropolis-Hastings
  sampling with the UrnBallsSplitMerge proposal distribution to yield
  extremely bad results.

BLOG 0.1.1  (21 September 2005)
---------------------
* Added three new examples: 
  - a university example often used for explaining probabilistic 
    relational models (examples/grades.{mblog,eblog,qblog}); 
  - a hidden Markov model (examples/hmm.{mblog,eblog,qblog}); 
  - a Gaussian mixture model (examples/mixture.{mblog,eblog,qblog}).
* It is now possible to declare a nonrandom constant or function
  without specifying its interpretation, and then give its
  interpretation later.  This allows a relational skeleton to be
  specified separately from the model.  See examples/grades.mblog and
  examples/grades.eblog.
* Added two implementations of the FunctionInterp inferface:
  ListInterp and TabularInterp.  These allow you to specify the
  interpretation of a non-random function (on a finite set of
  argument tuples) explicitly in a BLOG file.  This is useful for
  specifying relational skeletons.
* You no longer need to use parentheses after a CPD name when it
  takes no arguments.
* Added != operator, and infix operators <, >, <=, >= for the
  corresponding built-in functions
* Added built-in Succ and Pred functions on natural numbers.
* TabularCPD now allows integers and natural numbers as the child
  type, defining a distribution over a prefix of the natural numbers.
* Added character literals (such as 'a'), improved handling of escape
  sequences in string and character literals (now same as Java),
  implemented concatenation of consecutive string literals (as in C).
* Fixed bug whereby compiled versions of set specifications might be
  incorrect if additional number statements occurred after the set
  specification in an input file.
* Fixed bug whereby RejectionSampler wouldn't instantiate basic RVs
  corresponding to random constants introduced in evidence
  statements.
* Fixed bug in sorting of histogram in query outputs.
* Built-in functions now handle null arguments properly.
* Makefiles no longer require GNU make.

BLOG 0.1 (10 September 2005)
--------
Initial release
