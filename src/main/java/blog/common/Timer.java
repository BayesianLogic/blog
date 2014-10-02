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


/**
 * A class for measuring how much time a program spends doing certain
 * operations. Each Timer object acts like a stopwatch that keeps track of an
 * elapsed time. When you call the start method, the elapsed time starts
 * increasing; when you call the stop method, it stops. Unlike a stopwatch,
 * there is no "reset" method.
 * 
 * @author unknown
 * @author leili (for fixes in 0.10)
 * @date 2014/09/25
 */
public class Timer {
  /**
   * Creates a new Timer that will not be registered to have its its time
   * printed by Timer.printAllTimers.
   */
  public Timer() {
  }

  /**
   * Starts the timer.
   */
  public final void start() {
    timeStarted = System.currentTimeMillis();
  }

  /**
   * Stops the timer, and adds the time elapsed since the last call to start()
   * to the running total.
   */
  public final void stop() {
    if (timeStarted > 0) {
      millisSoFar += (System.currentTimeMillis() - timeStarted);
      timeStarted = -1;
    }
  }

  /**
   * Returns the total elapsed time in seconds.
   */
  public double elapsedTime() {
    long elapsedMillis = millisSoFar;
    if (timeStarted > 0) {
      elapsedMillis += (System.currentTimeMillis() - timeStarted);
    }
    return (elapsedMillis / 1000.0);
  }

  private long millisSoFar = 0;
  private long timeStarted = -1;
}
