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
import java.util.Set;

import blog.BLOGUtil;
import blog.DBLOGUtil;
import blog.common.Util;
import blog.model.Evidence;
import blog.model.Query;
import blog.sample.Sampler;
import blog.world.DefaultPartialWorld;
import blog.world.PartialWorld;

/**
 * A Particle for Particle Filtering. The particle is advanced as it receives
 * evidence and queries with calls to methods {@link #take(Evidence)} and
 * {@link #answer(Collection)}. Providing evidence also reweights the particle
 * according to it. A method {@link #copy()} can be used for resampling.
 */
public class Particle {

	/**
	 * Creates a new particle. <code>numTimeSlicesInMemory</code> indicates how
	 * many time slices need to be kept in memory. The properties table specifies
	 * configuration parameters.
	 */
	public Particle(Set idTypes, int numTimeSlicesInMemory, Sampler sampler) {
		this.sampler = sampler;
		this.numTimeSlicesInMemory = numTimeSlicesInMemory;
		this.idTypes = idTypes;
		curWorld = new DefaultPartialWorld(idTypes);
		weight = 1; // all particles are created equal.
	}

	/**
	 * Takes evidence relative to some new time step, updates current world based
	 * on it, and recalculates particle weight according to its probability.
	 */
	public void take(Evidence evidence) {
		sampler.initialize(evidence, Util.list());
		sampler.setBaseWorld(curWorld);

		// System.out.println("Particle taking evidence.");
		// System.out.println("Evidence: " + evidence);
		// System.out.println("Previous world: " + curWorld);

		sampler.nextSample();
		weight = sampler.getLatestWeight();
		// if (Double.isNaN(weight)) {
		// System.out.println("Particle.take: weight set to " + weight);
		// System.out.println("Evidence: " + evidence);
		// System.out.println("Updated world: " + curWorld);
		// System.exit(-1);
		// }

		// System.out.println("Particle.take: weight set to " + weight);
		curWorld = sampler.getLatestWorld();

		// System.out.println("Weight: " + weight);
		// System.out.println("Updated world: " + curWorld);
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
	 * Identifies the largest time step in the current world and uninstantiates
	 * all temporal random variables with a different time step.
	 */
	public void uninstantiatePreviousTimeslices() {
		DBLOGUtil.uninstantiatePreviousTimeslices(curWorld);
	}

	public void removeAllDerivedVars() {
		BLOGUtil.removeAllDerivedVars(curWorld);
	}

	public PartialWorld getLatestWorld() {
		if (curWorld == null) {
			throw new IllegalStateException("Particle has no latest sample.");
		}
		return curWorld;
	}

	public double getLatestWeight() {
		return weight;
	}

	public Particle copy() {
		Particle copy = new Particle(idTypes, numTimeSlicesInMemory, sampler);
		DefaultPartialWorld newWorld = (DefaultPartialWorld) ((DefaultPartialWorld) curWorld)
				.clone();
		copy.setWorld(newWorld);
		copy.weight = weight;
		return copy;
	}

	public void setWorld(PartialWorld curWorld) {
		this.curWorld = curWorld;
	}

	public void updateQueriesStats(Collection queries) {
		// System.out.println("Particle.updateQueriesStats: start");
		if (getLatestWeight() > 0) {
			// System.out.println("Particle.updateQueriesStats: going over queries");
			for (Iterator iter = queries.iterator(); iter.hasNext();) {
				Query q = (Query) iter.next();
				q.updateStats(getLatestWorld(), getLatestWeight());
			}
		}
		// else
		// System.out.println("Particle.updateQueriesStats: getLatestWeight was zero.");
	}

	public String toString() {
		return "(" + curWorld + "," + weight + ")";
	}

	protected Set idTypes; // of Type
	public PartialWorld curWorld = null;
	protected double weight;
	public int numTimeSlicesInMemory;
	private Sampler sampler;
}
