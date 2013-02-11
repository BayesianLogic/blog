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

package blog.common;

import java.util.Iterator;

/**
 * A histogram that represents a partition of a set of references by showing the
 * size of each block in the partition. The references in a block all refer to
 * the same underlying object, identified by an object ID. We are interested in
 * such histograms because the prior probability of a partition of the
 * references depends only on this histogram, not on which particular references
 * refer to which particular objects. In other words, a ReferenceHistogram
 * encapsulates the sufficient statistics for evaluating the probability of a
 * partition.
 * 
 * We will sometimes divide the set of references into two subsets, and create a
 * ReferenceHistogram for each subset. The same object IDs may be used in both
 * subset histograms, defining a partition for the entire set where some of the
 * blocks include references from both subsets. So a ReferenceHistogram actually
 * represents a bit more than a histogram: it specifies how to combine this
 * histogram with other histograms.
 * 
 * Of course, a ReferenceHistogram also represents less than a partition, in
 * that it doesn't specify <i>which</i> citations are grouped together: it just
 * specifies the group sizes.
 */
public interface ReferenceHistogram {
	/**
	 * A ReferenceHistogram with no citations.
	 */
	ReferenceHistogram EMPTY_HISTOGRAM = new DefaultReferenceHistogram();

	/**
	 * Returns the number of objects with at least one reference.
	 */
	int numReferencedObjects();

	/**
	 * Returns the total number of references in this histogram.
	 */
	int totalReferences();

	/**
	 * Returns the number of references for the given object. If the object has no
	 * references, the method returns zero (rather than throwing an exception or
	 * something like that).
	 * 
	 * @param obj
	 *          a Long object representing an object ID
	 */
	int getNumReferences(Long obj);

	/**
	 * Allows iteration over the objects that have at least one reference.
	 * 
	 * @return an Iterator over Long objects representing object IDs
	 */
	Iterator iterator();
}
