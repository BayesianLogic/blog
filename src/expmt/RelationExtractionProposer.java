/*
 * Copyright (c) 2014, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.  
 *
 * * Neither the name of the University of California, Berkeley nor
 *   the names of its contributors may be used to endorse or promote
 *   products derived from this software without specific prior 
 *   written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package expmt;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Random;

import blog.BLOGUtil;
import blog.bn.BayesNetVar;
import blog.bn.RandFuncAppVar;
import blog.bn.NumberVar;
import blog.bn.VarWithDistrib;
import blog.common.Util;
import blog.distrib.Beta;
import blog.distrib.Dirichlet;
import blog.model.Evidence;
import blog.model.FunctionSignature;
import blog.model.Model;
import blog.model.NonGuaranteedObject;
import blog.model.OriginFunction;
import blog.model.RandomFunction;
import blog.model.Type;
import blog.model.POP;
import blog.objgen.ObjectSet;
import blog.sample.ClassicInstantiatingEvalContext;
import blog.sample.Proposer;
import blog.world.DefaultPartialWorld;
import blog.world.PartialWorld;
import blog.world.PartialWorldDiff;

/**
 * Proposal distribution for the relation extraction model.
 */
public class RelationExtractionProposer implements Proposer {

  // Instance variables
  private Model model;
  private Evidence evidence;
  private List queries; // of Query

  private Type relType; // Relation
  private Type entType; // Entity
  private Type factType; // Fact
  private Type trigType; // Trigger
  private Type sentType; // Sentence

  private POP relPOP;
  private POP entPOP;
  private POP factPOP;
  private POP trigPOP;
  private POP sentPOP;
  
  // parameters
  private Integer alpha;
  private Integer beta;
  private Double dir_alpha;

  // random variable functions as described by the model
  private RandomFunction sparsityFunc;
  private RandomFunction holdsFunc;
  private RandomFunction thetaFunc;
  private RandomFunction sourceFactFunc;
  private RandomFunction subjectFunc;
  private RandomFunction objectFunc;
  private RandomFunction triggerIDFunc;
  private RandomFunction verbFunc;
  
  // Origin functions
  private OriginFunction relFunc;
  private OriginFunction arg1Func;
  private OriginFunction arg2Func;
  
  // HashMap for facts
  private HashMap<String, Object> factMap;
  
  // Random Number Generate
  private Random rng;

  /**
   * Creates a new RelationExtractionProposer object for the given model.
   * 
   * Set the model, types, and random functions
   */
  public RelationExtractionProposer(Model model, Properties properties) {

	  
    // Set model
    this.model = model;

    // Set types
    relType = Type.getType("Relation");
    entType = Type.getType("Entity");
    factType = Type.getType("Fact");
    trigType = Type.getType("Trigger");
    sentType = Type.getType("Sentence");

    // Set Random Functions
    sparsityFunc = (RandomFunction) model.getFunction(new FunctionSignature(
        "Sparsity", relType));
    holdsFunc = (RandomFunction) model.getFunction(new FunctionSignature("Holds",
        factType));
    thetaFunc = (RandomFunction) model.getFunction(new FunctionSignature("Theta",
        relType));
    sourceFactFunc = (RandomFunction) model.getFunction(new FunctionSignature(
        "SourceFact", sentType));
    subjectFunc = (RandomFunction) model.getFunction(new FunctionSignature(
        "Subject", sentType));
    objectFunc = (RandomFunction) model.getFunction(new FunctionSignature("Object",
        sentType));
    triggerIDFunc = (RandomFunction) model.getFunction(new FunctionSignature(
        "TriggerID", sentType));
    verbFunc = (RandomFunction) model.getFunction(new FunctionSignature("Verb",
        sentType));
    
    // Set Origin Functions
    relFunc = (OriginFunction) model.getFunction(new FunctionSignature("Rel",
    	factType));
    arg1Func = (OriginFunction) model.getFunction(new FunctionSignature("Arg1",
    	factType));
    arg2Func = (OriginFunction) model.getFunction(new FunctionSignature("Arg2", 
    	factType));
    
    // Set POPs
    factPOP = (POP) factType.getPOPs().iterator().next();
    
    // Set parameter constants
    alpha = (Integer) model.getConstantValue("alpha");
    beta = (Integer) model.getConstantValue("beta");
    dir_alpha = (Double) model.getConstantValue("dir_alpha");

  }

  /**
   * Initialization:
   * 1) Set all observed variables (Subject, Object, Verb, TriggerID)
   * 2) Choose SourceFact for all observed sentences uniformly from all facts
   *     such that the argument pair matches
   * 3) Set the Holds(f) variable for this fact to be True
   * 4) Sample everything else
   *    a) #Fact variables which are set to 1
   * 	b) Sparsity
   * 	c) Theta
   * 	d) Holds
   */
  public PartialWorldDiff initialize(Evidence evidence, List queries) {

    // Set evidence and queries
    this.evidence = evidence;
    this.queries = queries;

    // Instantiate the world
    PartialWorld underlying = new DefaultPartialWorld(getTypes());
    PartialWorldDiff world = new PartialWorldDiff(underlying);
    
    // 1) Set observed variables
    for (Iterator iter = evidence.getEvidenceVars().iterator(); iter.hasNext();) {
    	RandFuncAppVar var = (RandFuncAppVar) iter.next();
    	RandomFunction varFunc = var.func();
    	
    	if (varFunc == verbFunc) {
    		Object sentence = var.args()[0]; // Get the sentence s
    		int triggerIndex = trigType.getGuaranteedObjIndex(evidence.getObservedValue(var)); // Assumes triggers are Distinct
    		RandFuncAppVar trigFuncVar = new RandFuncAppVar(triggerIDFunc, Collections.singletonList(sentence));
    		world.setValue(trigFuncVar, triggerIndex);
    	}
    	
    	world.setValue(var, evidence.getObservedValue(var));
    }

    // Create hashmap where key is a unique relation/arg pair, value is a fact
    factMap = new HashMap<String, Object>(); 
    
    for (Object rel : relType.getGuaranteedObjects()) {
    	for (Object arg1 : entType.getGuaranteedObjects()) {
    		for (Object arg2 : entType.getGuaranteedObjects()) {
    			
    			// The key
    			String key = generateKey(rel, arg1, arg2);
    			
    			// Use a NumberVar to grab the fact
    			Object[] factArgs = {rel, arg1, arg2};
    		    NumberVar nFacts = new NumberVar(factPOP, factArgs);
    		    
    		    // 4a) Instantiate the number variable, by definition of the model
    		    world.setValue(nFacts, 1);
    		    
    			Object value = world.getSatisfiers(nFacts).iterator().next(); // Should only be one fact that satisfies that number statement
    			
    			factMap.put(key, value);
    		}
    	}
    }
    
    // 2) For each observed sentence, choose SourceFact (basically randomly choose a relation
    // to go with the argument pair). This assumes sentences are distinct.
    // 3) Also, set the holds variable for this fact to be true
    rng = new Random();
    for (Object sentence : sentType.getGuaranteedObjects()) {
    	
    	// Choose relation randomly from all relations
    	int relNum = rng.nextInt(relType.getGuaranteedObjects().size());
    	Object rel = relType.getGuaranteedObject(relNum);
    	Object arg1 = world.getValue(new RandFuncAppVar(subjectFunc, Collections.singletonList(sentence)));
    	Object arg2 = world.getValue(new RandFuncAppVar(objectFunc, Collections.singletonList(sentence)));
    	
    	// 2) Set the sourceFact value in the world
    	Object fact = factMap.get(generateKey(rel, arg1, arg2));
    	world.setValue(new RandFuncAppVar(sourceFactFunc, Collections.singletonList(sentence)), fact);
    	    	
    	// 3) Set the Holds value in the world
    	world.setValue(new RandFuncAppVar(holdsFunc, Collections.singletonList(fact)), true); // I hope this is correct, and there isn't some class for BLOG booleans
    }
    
    // 4b and c) Sample Sparsity(r) and Theta(r) for each relation r
    Integer[] params = {alpha, beta};
    Beta sparsitySampler = new Beta(Arrays.asList(params));
    Dirichlet thetaSampler = new Dirichlet(trigType.getGuaranteedObjects().size(), dir_alpha);
    for (Object rel : relType.getGuaranteedObjects()) {
    	
    	// 4b) Sparsity(r)
    	RandFuncAppVar sparsity = new RandFuncAppVar(sparsityFunc, Collections.singletonList(rel));
    	world.setValue(sparsity, sparsitySampler.sampleVal(Collections.EMPTY_LIST, relType));
    	
    	// 4c) Theta(r)
    	RandFuncAppVar theta = new RandFuncAppVar(thetaFunc, Collections.singletonList(rel));
    	world.setValue(theta, thetaSampler.sampleVal(Collections.EMPTY_LIST, relType)); 
    	    	
    }
    
    
    // 4d) Sample all holds(f) values if they haven't been instantiated
    for (Object fact: factMap.values()) {
    	
    	RandFuncAppVar holds = new RandFuncAppVar(holdsFunc, Collections.singletonList(fact));
    	if (!world.isInstantiated(holds)) {
    		
    		Object rel = ((NonGuaranteedObject) fact).getOriginFuncValue(relFunc);
    		if (rng.nextDouble() < (Double) world.getValue(new RandFuncAppVar(sparsityFunc, Collections.singletonList(rel)))) {
    			world.setValue(holds, true);
    		} else {
    			world.setValue(holds, false);
    		}	
    	}
    }
    
    // Debug
    if (Util.verbose()) {
	    for (Object fact : factMap.values()) {
	    	
	    	Object holds = world.getValue(new RandFuncAppVar(holdsFunc, Collections.singletonList(fact)));
	    	System.out.println("Fact: " + fact + ", Holds: " + holds);
	    	
	    }
	    System.out.println(world.toString());
    }
    
    return world;

  }

  /**
   * Proposes a next state for the Markov chain given the current state. The
   * world argument is a PartialWorldDiff that the proposer can modify to create
   * the proposal; the saved version of this PartialWorldDiff is the state
   * before the proposal. Returns the log proposal ratio: log (q(x | x') / q(x'
   * | x))
   * 
   * The details of the proposal has been arranged nicely in a PDF file.
   * Please look to that file for details.
   */
  public double proposeNextState(PartialWorldDiff proposedWorld) {
	double sample = rng.nextDouble();
	if (sample < 0.25) {
	  return sourceFactSwitch(proposedWorld);
	} else if (sample < 0.5) {
	  return holdsSwitch(proposedWorld);
    } else if (sample < 0.75) {
	  return sparsitySample(proposedWorld);
    } else {
	  return thetaSample(proposedWorld);
    }
  }

  /**
   * Method for performing sourceFact(s) switch
   */
  private double sourceFactSwitch(PartialWorldDiff proposedWorld) {
    return 0;

  }

  /**
   * Method for performing Holds(f) switch
   */
  private double holdsSwitch(PartialWorldDiff proposedWorld) {
    return 0;

  }

  /**
   * Method for performing sparsity sampling (this is Gibbs)
   * 
   * Hr := {Holds(f) : Rel(f) = r), refer for the Tex file for more details
   */
  private double sparsitySample(PartialWorldDiff proposedWorld) {
	
  	// Choose relation randomly from all relations
  	int relNum = rng.nextInt(relType.getGuaranteedObjects().size());
  	Object rel = relType.getGuaranteedObject(relNum);
	  
	// The size of Hr is simply the number of unique combinations of different entities
	int sizeOfHr = (int) Math.pow(entType.getGuaranteedObjects().size(), 2);
	int numOfTrueFactsInHr = 0;
	for (Object fact : factMap.values()) {
		Object factRel = ((NonGuaranteedObject) fact).getOriginFuncValue(relFunc);
		if (factRel == rel) {
			if (((Boolean) proposedWorld.getValue(new RandFuncAppVar(holdsFunc, Collections.singletonList(fact)))).booleanValue()) {
				numOfTrueFactsInHr++;
			}
		}
	}
	
	// Sample from posterior distribution, set the value in the world
    Integer[] params = {alpha + numOfTrueFactsInHr, beta + sizeOfHr - numOfTrueFactsInHr};
    Beta sparsitySampler = new Beta(Arrays.asList(params));
    RandFuncAppVar sparsity = new RandFuncAppVar(sparsityFunc, Collections.singletonList(rel));
    proposedWorld.setValue(sparsity, sparsitySampler.sampleVal(Collections.EMPTY_LIST, relType));
    
    return 0; // Gibbs, may have to figure this out later with MHSampler.java
  }

  /**
   * Method for performing theta sampling (this is Gibbs)
   * 
   * Refer to the Tex file for more details
   */
  private double thetaSample(PartialWorldDiff proposedWorld) {
	
  	// Choose relation randomly from all relations
  	int relNum = rng.nextInt(relType.getGuaranteedObjects().size());
  	Object rel = relType.getGuaranteedObject(relNum);
	  
	// Create parameter vector
	Double[] params = new Double[trigType.getGuaranteedObjects().size()];
	Arrays.fill(params, dir_alpha);
	
	// For each trigger (indexed by i), find how many TriggerID(s) rv's have that value
	for (int i = 0; i < params.length; i++) {
		for (Object sentence : sentType.getGuaranteedObjects()) {
			RandFuncAppVar triggerID = new RandFuncAppVar(triggerIDFunc, Collections.singletonList(sentence));
			if (proposedWorld.getValue(triggerID).equals(i)) {
				params[i]++;
			}
		}
	}
	// Sample from posterior distribution, set the value in the world
    Dirichlet thetaSampler = new Dirichlet(Arrays.asList(params));
    RandFuncAppVar theta = new RandFuncAppVar(thetaFunc, Collections.singletonList(rel));
    proposedWorld.setValue(theta, thetaSampler.sampleVal(Collections.EMPTY_LIST, relType));
  
    return 0; // Gibbs, may have to figure this out later with MHSampler.java
  }

  /**
   * These methods will not be used, but must be implemented to satisfy the
   * Proposer interface...
   */

  public void updateStats(boolean accepted) {
    // no stats maintained
  }

  public void printStats() {
    // no stats to print
  }

  public PartialWorldDiff reduceToCore(PartialWorld curWorld, BayesNetVar var) {
    return null;
  }

  public double proposeNextState(PartialWorldDiff proposedWorld,
      BayesNetVar var, int i) {
    return 0;
  }

  public double proposeNextState(PartialWorldDiff proposedWorld, BayesNetVar var) {
    return 0;
  }
  
  // A method to create the key specific to the factMap (HashMap)
  private String generateKey(Object rel, Object arg1, Object arg2) {
	  return "Rel: " + rel.toString() + ", Arg1: " + arg1.toString() + ", Arg2: " + arg2.toString();
  }
  
  private Set getTypes() {
      Set idTypes = new HashSet();
      idTypes.add(relType); 
      idTypes.add(entType);
      //idTypes.add(factType); // If commented, Fact objects result in NonGuaranteedObjects
      idTypes.add(trigType);
      idTypes.add(sentType);
      return idTypes;
  }


}
