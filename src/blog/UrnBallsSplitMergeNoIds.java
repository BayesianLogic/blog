/*
 * Copyright (c) 2005, Regents of the University of California
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

package blog;

import java.util.*;
import common.Util;

/**
 * Split-merge proposal distribution for the urn and balls scenario that 
 * doesn't use identifiers.  Instead, when it does a split move, it 
 * chooses a non-guaranteed object uniformly at random from the set of 
 * non-guaranteed objects (balls) that are not used.
 */
public class UrnBallsSplitMergeNoIds implements Proposer {
    /**
     * Creates a new UrnBallSplitMerge proposer for the given model.
     */
    public UrnBallsSplitMergeNoIds(Model model, Properties properties) {
	this.model = model;

	ballType = model.getType("Ball");
	drawType = model.getType("Draw");
	
	ballPOP = (POP) ballType.getPOPs().iterator().next();
	fBallDrawn = (RandomFunction) model.getFunction
	    (new Function.Sig("BallDrawn", drawType));
	fColor = (RandomFunction) model.getFunction
	    (new Function.Sig("Color", ballType));
	fObsColor = (RandomFunction) model.getFunction
	    (new Function.Sig("ObsColor", drawType));

	colorType = fColor.getRetType();
    }

    /**
     * Returns a PartialWorldDiff to serve as the initial state of a
     * Markov chain.  In this world, each draw yields a distinct ball,
     * and the total number of balls equals the number of draws.
     */
    public PartialWorldDiff initialize(Evidence evidence, 
				       List queries) {
	this.evidence = evidence;
	this.queries = queries;

	PartialWorld underlying 
	    = new DefaultPartialWorld(Collections.EMPTY_SET);
	PartialWorldDiff world = new PartialWorldDiff(underlying);

	// Set observed ball color for each draw
	for (Iterator iter = evidence.getEvidenceVars().iterator(); 
	     iter.hasNext(); ) {
	    BasicVar var = (BasicVar) iter.next();
	    world.setValue(var, evidence.getObservedValue(var));
	}

	// Set number of balls
	NumberVar nv = new NumberVar(ballPOP, Collections.EMPTY_LIST);
	int numBalls = drawType.getGuaranteedObjects().size();
	world.setValue(nv, new Integer(numBalls));
	//doNumBallsMove(world);

	// Set BallDrawn variables, and sample colors for the used balls
	int index = 1;
	for (Iterator iter = drawType.getGuaranteedObjects().iterator(); 
	     iter.hasNext(); ) {
	    Object draw = iter.next();
	    RandFuncAppVar bdVar 
		= new RandFuncAppVar(fBallDrawn, 
				     Collections.singletonList(draw));
	    Object ball = NonGuaranteedObject.get(ballPOP, new Object[0],
						  index++);
	    world.setValue(bdVar, ball);

	    if (Util.verbose()) {
		System.out.println("Color probability for ball " + ball + ":");
	    }
	    sampleColor(world, ball);
	}

	return world;
    }

    /**
     * Proposes a next state for the Markov chain given the current state.  
     * The world argument is a PartialWorldDiff that the proposer 
     * can modify to create the proposal; the saved version of 
     * this PartialWorldDiff is the state before the proposal.  Returns 
     * the log proposal ratio:
     *    log (q(x | x') / q(x' | x))
     *
     * <p>This method implements a split-merge proposal distribution.  Two 
     * draws are chosen uniformly without replacement from the set of 
     * draws.  If these two draws both yielded the same ball, that ball is 
     * "split" (its draws are reassigned randomly to that ball and a new 
     * ball) and new colors are sampled for that ball and the new ball.  
     * If the two draws yielded different balls, those balls are "merged" 
     * (one is deleted and all its draws are moved to the other ball) and 
     * a new color is sampled for the remaining ball.  Note that these 
     * split-merge moves increase or decrease the number of balls in the 
     * world, so the number of unused balls stays constant.
     *
     * <p>There is also another move type that changes the total number 
     * of balls without changing the values of any BallDrawn variables.  
     * We do this type of move with probability NUM_BALLS_MOVE_PROB.
     *
     * <p>The log proposal ratio returned is specific to the chosen move 
     * type, and the chosen draws for a split-merge move.  That is, we 
     * don't compute the probability of making the same proposal through 
     * a different move type or a different choice of draws.  This is ok 
     * because each move type or choice of draws can be thought of as 
     * a separate MCMC kernel; we're just mixing these kernels together.  
     */
    public double proposeNextState(PartialWorldDiff proposedWorld) {
        if (Util.random() < NUM_BALLS_MOVE_PROB) {
	    return doNumBallsMove(proposedWorld);
	} 

	List chosenDraws = Util.sampleWithoutReplacement
	    (drawType.getGuaranteedObjects(), 2);

	if (chosenDraws.size() == 0) {
	    // no draws, so don't change anything
	    if (Util.verbose()) {
		System.out.println("No draws, so can't change anything.");
	    }
	    return 0;
	}
	if (chosenDraws.size() == 1) {
	    // only one draw, but we can resample the color of the 
	    // drawn ball
	    if (Util.verbose()) {
		System.out.println("Only one draw; resampling ball color.");
	    }
	    Object draw = chosenDraws.get(0);
	    Object ball = fBallDrawn.getValueSingleArg(draw, proposedWorld);

	    double logProposalRatio =
		getColorSamplingLogProb(proposedWorld, ball);
	    logProposalRatio -= sampleColor(proposedWorld, ball);
	    return logProposalRatio;
	}
		
	double logProbRatio = doSplitMerge(proposedWorld, chosenDraws.get(0), 
					   chosenDraws.get(1));
	return logProbRatio;
    }

    public void updateStats(boolean accepted) {
	// no stats to update
    }

    public void printStats() {
	// no stats to print
    }

    private double doNumBallsMove(PartialWorld world) {
	/*
	// Just resample from prior.  This may yield number smaller 
	// than the number of ball identifiers, but then the proposal 
	// will just be rejected.
	
	NumberVar var = new NumberVar(ballPOP, Collections.EMPTY_LIST);
	ParentsAndValue info = var.getParentsAndCPD
	    (world, ValueChooser.NO_INSTANTIATION);
	DependencyModel.Distrib distrib
	    = (DependencyModel.Distrib) info.getValue();
	CondProbDistrib cpd = distrib.getCPD();
	List cpdArgs = distrib.getArgValues();

	double logProbBackward = 0;
        Object oldValue = world.getValue(var);
	if (oldValue != null) {
	    logProbBackward = Math.log(cpd.getProb(cpdArgs, oldValue));
	}
	
	Object newValue = cpd.sampleVal(cpdArgs, var.getType());
	world.setValue(var, newValue);
	double logProbForward = Math.log(cpd.getProb(cpdArgs, newValue));

	return (logProbBackward - logProbForward);
	*/

	if (Util.verbose()) {
	    System.out.println("Changing number of balls...");
	}

	int offset = (Util.random() < 0.5) ? -1 : 1;
	adjustNumBalls(world, offset);
	return 0;
    }

    private double doSplitMerge(PartialWorld world, Object draw1, 
				Object draw2) {
	double logProposalRatio = 0; // log prob backward - log prob forward
	RandFuncAppVar bd1 
	    = new RandFuncAppVar(fBallDrawn, Collections.singletonList(draw1));
	Object ball1 = world.getValue(bd1);

	RandFuncAppVar bd2
	    = new RandFuncAppVar(fBallDrawn, Collections.singletonList(draw2));
	Object ball2 = world.getValue(bd2);

	if (ball1 == ball2) {
	    // Split, if there's an unused ball
	    Set unusedBalls = getUnusedBalls(world);
	    if (unusedBalls.isEmpty()) {
		return 0; // don't change anything; equivalent to rejection
	    }

	    if (Util.verbose()) {
		System.out.println("Splitting ball " + ball1);
	    }

	    // account for re-sampling old ball color in backward move
	    if (Util.verbose()) System.out.println("Backward color sampling prob: ");
	    logProposalRatio += getColorSamplingLogProb(world, ball1);
	    
	    // record the set of vars that point to ball1
	    Set varsToSplit = world.getVarsWithValue(ball1);

	    // make bd2 point to a new ball2
	    //adjustNumBalls(world, 1);
	    ball2 = Util.uniformSample(unusedBalls);
	    logProposalRatio -= (-Math.log(unusedBalls.size()));
	    world.setValue(bd2, ball2);

	    // Each other var that pointed to ball1 has 50-50 chance of 
	    // pointing to ball2
	    for (Iterator iter = varsToSplit.iterator(); iter.hasNext(); ) {
		BasicVar var = (BasicVar) iter.next();
		if ((!var.equals(bd1)) && (!var.equals(bd2))) {
		    if (Util.random() < 0.5) {
			world.setValue(var, ball2);
		    }
		}
	    }

	    logProposalRatio -= ((varsToSplit.size() - 2) * Math.log(0.5));
	    if (Util.verbose()) {
		System.out.println("Split probability " 
				   + Math.exp(((varsToSplit.size() - 2) * Math.log(0.5))));
	    }

	    // sample colors for these two balls
	    if (Util.verbose()) System.out.println("Forward color sampling prob for " + ball1);
	    logProposalRatio -= sampleColor(world, ball1);
	    if (Util.verbose()) System.out.println("Forward color sampling prob for " + ball2);
	    logProposalRatio -= sampleColor(world, ball2);

	} else {
	    // Merge
	    if (Util.verbose()) {
		System.out.println("Merging balls " + ball1 + " and " + ball2);
	    }
	    
	    // account for re-sampling colors of two balls in backward move
	    if (Util.verbose()) System.out.println("Backward color sampling prob for " + ball1);
	    logProposalRatio += getColorSamplingLogProb(world, ball1);
	    if (Util.verbose()) System.out.println("Backward color sampling prob for " + ball2);
	    logProposalRatio += getColorSamplingLogProb(world, ball2);

	    Collection varsToMerge 
		= new ArrayList(world.getVarsWithValue(ball1));
	    varsToMerge.addAll(world.getVarsWithValue(ball2));

	    // all these vars now point to ball1
	    for (Iterator iter = varsToMerge.iterator(); iter.hasNext(); ) {
		BasicVar var = (BasicVar) iter.next();
		world.setValue(var, ball1);
	    }

	    // uninstantiate newly barren color variable, and take
	    // into account probability of selecting ball2 in split
	    // move
	    world.setValue(new RandFuncAppVar
			   (fColor, Collections.singletonList(ball2)), null);
	    logProposalRatio += (-Math.log(getUnusedBalls(world).size()));
	    //adjustNumBalls(world, -1);

	    // take into account proposal prob for reverse move
	    logProposalRatio += ((varsToMerge.size() - 2) * Math.log(0.5));
	    if (Util.verbose()) {
		System.out.println("Backward split sampling prob: " 
				   + Math.exp((varsToMerge.size() - 2) * Math.log(0.5)));
	    }

	    // sample color for remaining ball
	    if (Util.verbose()) System.out.println("Forward color sampling prob: ");
	    logProposalRatio -= sampleColor(world, ball1);
	}

	return logProposalRatio;
    }

    private double sampleColor(PartialWorld world, Object ball) {
	RandFuncAppVar var 
	    = new RandFuncAppVar(fColor, Collections.singletonList(ball));
	List values = colorType.getGuaranteedObjects();
	double[] probsGivenNeighbors = computeGibbsProbsForColor(world, ball);

	int newColorIndex = Util.sampleWithProbs(probsGivenNeighbors);
	world.setValue(var, values.get(newColorIndex));
	if (Util.verbose()) System.out.println("\t" + probsGivenNeighbors[newColorIndex]);
	return Math.log(probsGivenNeighbors[newColorIndex]);
    }

    private double getColorSamplingLogProb(PartialWorld world, 
					   Object ball) {
	RandFuncAppVar var 
	    = new RandFuncAppVar(fColor, Collections.singletonList(ball));
	double[] probsGivenNeighbors = computeGibbsProbsForColor(world, ball);

	int colorIndex = colorType.getGuaranteedObjIndex(world.getValue(var));
	if (Util.verbose()) System.out.println("\t" + probsGivenNeighbors[colorIndex]);
	return Math.log(probsGivenNeighbors[colorIndex]);	
    }

    /**
     * Returns an array of probabilities of possible colors for the
     * given ball, in the same order as the list returned by
     * colorType.getGuaranteedObjects().
     */
    private double[] computeGibbsProbsForColor(PartialWorld world, 
					       Object ball) {
	RandFuncAppVar colorVar 
	    = new RandFuncAppVar(fColor, Collections.singletonList(ball));
	Object origValue = world.getValue(colorVar);
	List colorValues = colorType.getGuaranteedObjects();

	// The children of the ball color variable are the 
	// ObsColor variables for those draws whose BallDrawn is this ball.
	Set ballDrawnVars = world.getVarsWithValue(ball);
	Set obsColorVars = new HashSet();
	for (Iterator iter = ballDrawnVars.iterator(); iter.hasNext(); ) {
	    Object draw = ((BasicVar) iter.next()).args()[0];
	    BasicVar obsColorVar 
		= new RandFuncAppVar(fObsColor, 
				     Collections.singletonList(draw));
	    obsColorVars.add(obsColorVar);
	}

	double[] probs = new double[colorValues.size()];
	double sum = 0;
	for (int i = 0; i < colorValues.size(); ++i) {
	    world.setValue(colorVar, colorValues.get(i));
	    probs[i] = Math.exp(world.getLogProbOfValue(colorVar));
	    
	    for (Iterator iter = obsColorVars.iterator(); iter.hasNext(); ) {
	    	BasicVar child = (BasicVar) iter.next();
	    	probs[i] *= Math.exp(world.getLogProbOfValue(child));
	    }

	    sum += probs[i];
	}

	for (int i = 0; i < probs.length; ++i) {
	    probs[i] /= sum;
	}

	if (origValue == null) {
	    world.setValue(colorVar, null);
	} else {
	    world.setValue(colorVar, origValue);
	}
	
	return probs;
    }

    private void adjustNumBalls(PartialWorld world, int offset) {
	NumberVar nv = new NumberVar(ballPOP, Collections.EMPTY_LIST);
	int oldValue = ((Integer) world.getValue(nv)).intValue();
	world.setValue(nv, new Integer(oldValue + offset));

	// If this change deleted any objects, then replace the deleted 
	// objects with Model.NULL wherever they're used as RV values.
	for (int i = oldValue; i > oldValue + offset; --i) {
	    NonGuaranteedObject obj = NonGuaranteedObject.get(nv, i);
	    Set usesAsValue = world.getVarsWithValue(obj);
	    for (Iterator iter = usesAsValue.iterator(); iter.hasNext(); ) {
		BasicVar var = (BasicVar) iter.next();
		if (Util.verbose()) {
		    System.out.println("Changing " + var + " from " + obj 
				       + " to null.");
		}
		world.setValue(var, Model.NULL);
	    }
	}
    }

    private Set getUnusedBalls(PartialWorld world) {
	NumberVar ballsPOPApp = new NumberVar(ballPOP, Collections.EMPTY_LIST);
	Set unusedBalls = new LinkedHashSet(world.getSatisfiers(ballsPOPApp));
	for (Iterator iter = drawType.getGuaranteedObjects().iterator(); 
	     iter.hasNext(); ) {
	    Object draw = iter.next();
	    RandFuncAppVar bdVar 
		= new RandFuncAppVar(fBallDrawn, 
				     Collections.singletonList(draw));
	    unusedBalls.remove(world.getValue(bdVar));
	}
	return unusedBalls;
    }

    private Model model;
    private Evidence evidence;
    private List queries; // of Query

    private Type ballType;
    private Type drawType;
    private Type colorType;
    private POP ballPOP;
    private RandomFunction fBallDrawn;
    private RandomFunction fColor;
    private RandomFunction fObsColor;

    private static final double NUM_BALLS_MOVE_PROB = 0.25;
}
