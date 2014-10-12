/*
 * Copyright (c) 2012, Regents of the University of California
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

package blog.bn;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import blog.common.DefaultDGraph;
import blog.common.Util;
import blog.sample.TraceParentRecEvalContext;
import blog.world.PartialWorld;

/**
 * This class provides a default implementation of CBNs.
 * Uses a DefaultCBN rather than a DefaultDGraph, so as to avoid
 * ClassCastExceptions.
 * 
 * @author Da Tang
 * @since Sep 7, 2014
 */

public class DefaultCBN extends DefaultDGraph implements CBN {
  /**
   * clone method for the class Default CBN.
   */
  public Object clone() {
    DefaultCBN clone = new DefaultCBN();
    clone.nodeInfo = (Map) ((HashMap) nodeInfo).clone();
    for (Iterator iter = clone.nodeInfo.entrySet().iterator(); iter.hasNext();) {
      Map.Entry entry = (Map.Entry) iter.next();
      entry.setValue(((NodeInfo) entry.getValue()).clone());
    }
    return clone;
  }

  @Override
  public boolean isContingentOn(PartialWorld world, BayesNetVar X,
      BayesNetVar Y, BayesNetVar Z) {
    TraceParentRecEvalContext context = new TraceParentRecEvalContext(world);
    if (Z instanceof VarWithDistrib) {
      ((VarWithDistrib) Z).getDistrib(context);
    } else if (Z instanceof DerivedVar) {
      ((DerivedVar) Z).getValue(context);
    } else {
      return true;
    }

    LinkedList<BayesNetVar> parentTrace = new LinkedList<BayesNetVar>();
    parentTrace.addAll(context.getParentTrace());

    int x = parentTrace.indexOf(X), y = parentTrace.indexOf(Y);
    if (x < 0 || y < 0) {
      return false;
    }
    if (X instanceof NumberVar) {
      if (x < y && world.getCBN().getAncestors(Y).contains(X)) {
        if (Util.verbose()) {
          System.out.println("\t Contingent relations type 1: " + X.toString()
              + " " + Y.toString() + " " + Z.toString());
        }
        return true;
      } else {
        return false;
      }
    } else {
      if (x < y) {
        if (Util.verbose()) {
          System.out.println("\t Contingent relations type 2: " + X.toString()
              + " " + Y.toString() + " " + Z.toString());
        }
        return true;
      } else {
        return false;
      }
    }
  }
}
