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

package blog.engine;

import java.util.Collection;
import java.util.Iterator;

import blog.BLOGUtil;
import blog.DBLOGUtil;
import blog.model.Evidence;
import blog.model.Queries;
import blog.model.Query;
import blog.sample.Sampler;
import blog.type.Timestep;
import blog.world.DefaultPartialWorld;
import blog.world.PartialWorld;

/**
 * A Particle for Particle Filtering. The particle is advanced as it receives
 * evidence and queries with calls to methods {@link #take(Evidence)} and
 * {@link #answer(Collection)}. Providing evidence also reweights the particle
 * according to it. A method {@link #copy()} can be used for resampling.
 */
public class Particle {

  public Particle(Sampler sampler, PartialWorld world) {
    this.sampler = sampler;
    this.curWorld = world;
    logWeight = 1; // FIXME: shouldn't it be 0???
  }

  /**
   * Takes evidence relative to some new time step, updates current world based
   * on it, and recalculates particle logWeight according to its probability.
   */
  public void take(Evidence evidence) {
    sampler.initialize(evidence, new Queries(null));
    sampler.setBaseWorld(curWorld);

    sampler.nextSample();
    logWeight = sampler.getLatestLogWeight();
    curWorld = sampler.getLatestWorld();
  }

  /**
   * Takes a collection of queries and answers them based on current world.
   */
  public void answer(Collection queries) {
    // System.out.println("Particle.answer");
    Collection queriesVars = BLOGUtil.getQueriesVars(queries);
    BLOGUtil.ensureDetAndSupported(queriesVars, curWorld);
    updateQueriesStats(queries);
  }

  /**
   * remove all the temporal variables in this particle (possible world) that
   * are prior to the specified timestep (not including the current timestep)
   * 
   * @param timestep
   */
  public void removePriorTimeSlice(Timestep timestep) {
    DBLOGUtil.removeVarsAtDiffTimestep(timestep, curWorld);
  }

  public PartialWorld getLatestWorld() {
    if (curWorld == null) {
      throw new IllegalStateException("Particle has no latest sample.");
    }
    return curWorld;
  }

  public double getLatestLogWeight() {
    return logWeight;
  }

  public Particle copy() {
    DefaultPartialWorld worldCopy = (DefaultPartialWorld) ((DefaultPartialWorld) curWorld)
        .clone();
    Particle copy = new Particle(sampler, worldCopy);
    copy.logWeight = logWeight;
    return copy;
  }

  public void updateQueriesStats(Collection queries) {
    // System.out.println("Particle.updateQueriesStats: start");
    if (getLatestLogWeight() > Sampler.NEGLIGIBLE_LOG_WEIGHT) {
      // System.out.println("Particle.updateQueriesStats: going over queries");
      for (Iterator iter = queries.iterator(); iter.hasNext();) {
        Query q = (Query) iter.next();
        q.updateStats(getLatestWorld(), getLatestLogWeight()); // XXX
      }
    }
    // else
    // System.out.println("Particle.updateQueriesStats: getLatestLogWeight was zero.");
  }

  public String toString() {
    return "(" + curWorld + "," + logWeight + ")";
  }

  public PartialWorld curWorld = null;
  protected double logWeight;
  private Sampler sampler;
}
