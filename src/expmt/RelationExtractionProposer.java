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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import blog.bn.BayesNetVar;
import blog.bn.RandFuncAppVar;
import blog.bn.NumberVar;
import blog.model.Evidence;
import blog.model.FunctionSignature;
import blog.model.Model;
import blog.model.RandomFunction;
import blog.model.Type;
import blog.model.POP;

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

  // random variable functions as described by the model
  private RandomFunction sparsityFunc;
  private RandomFunction holdsFunc;
  private RandomFunction thetaFunc;
  private RandomFunction sourceFactFunc;
  private RandomFunction subjectFunc;
  private RandomFunction objectFunc;
  private RandomFunction triggerIDFunc;
  private RandomFunction verbFunc;

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

    // Set POPs
    relPOP = (POP) relType.getPOPs().iterator().next();
    entPOP = (POP) entType.getPOPs().iterator().next();
    //factPOP = (POP) factType.getPOPs().iterator().next(); // Not needed..?
    trigPOP = (POP) trigType.getPOPs().iterator().next();
    sentPOP = (POP) sentType.getPOPs().iterator().next();


  }

  /**
   * Initialization:
   * - Set all observed variables (Subject, Object, Verb, TriggerID)
   * - Choose SourceFact for all observed sentences uniformly from all facts
   * such that the argument pair matches
   * - Set the Holds(f) variable for this fact to be True
   * - Set TriggerID for observed sentences such that the observed Verb matches
   * the TriggerID
   * - Sample everything else
   */
  public PartialWorldDiff initialize(Evidence evidence, List queries) {

    // Set evidence and queries
    this.evidence = evidence;
    this.queries = queries;

    PartialWorld underlying = new DefaultPartialWorld(
        Collections.singleton(relType));
    PartialWorldDiff world = new PartialWorldDiff(underlying);

    // Set number variables: Assume #Triggers, #Sentences, #Relations, #Entities are specified in model
    NumberVar nRels = new NumberVar(relPOP, Collections.EMPTY_LIST);
    world.setValue(nRels, relType.getGuaranteedObjects().size());
    NumberVar nEnts = new NumberVar(entPOP, Collections.EMPTY_LIST);
    world.setValue(nEnts, entType.getGuaranteedObjects().size());
    //NumberVar nFacts = new NumberVar(factPOP, Collections.EMPTY_LIST);
    //world.setValue(nFacts, factType.getGuaranteedObjects());          // Not needed because it's already set..?
    NumberVar nTrigs = new NumberVar(trigPOP, Collections.EMPTY_LIST);
    world.setValue(nTrigs, trigType.getGuaranteedObjects().size());
    NumberVar nSents = new NumberVar(sentPOP, Collections.EMPTY_LIST);
    world.setValue(nSents, sentType.getGuaranteedObjects().size());

    // Set observed variables
    for (Iterator iter = evidence.getEvidenceVars().iterator(); iter.hasNext();) {
    	RandFuncAppVar var = (RandFuncAppVar) iter.next();
    	RandomFunction varFunc = var.func();
    	if (varFunc == verbFunc) {
    		Object sentence = var.args()[0];
    		int triggerIndex = trigType.getGuaranteedObjIndex(evidence.getObservedValue(var)); // Assumes trigger Type knows all triggers
    		RandFuncAppVar trigFuncVar = new RandFuncAppVar(triggerIDFunc, Collections.singletonList(sentence));
    		world.setValue(trigFuncVar, triggerIndex);
    	} 
    	world.setValue(var, evidence.getObservedValue(var));
    }
    System.out.println(world.toString());

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
    
    return 0; // Gibbs
  }

  /**
   * Method for performing theta sampling (this is Gibbs)
   */
  private double thetaSample(PartialWorldDiff proposedWorld) {
    return 0;
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

}
