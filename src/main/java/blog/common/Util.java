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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.util.ArithmeticUtils;

/**
 * Provides common utilities to FOMIE programs.
 * Original created by
 * 
 * updated by
 * 
 * @author leili
 * @since Apr 23, 2012
 */
public class Util {
  /**
   * Initializes random number generator in the default random context (see
   * {@link #pushRandomContext(String)}) using either the clock time or a fixed
   * seed. If the fixed seed is used, the behavior of the default random context
   * will be repeatable across runs.
   * 
   * @param randomize
   *          set seed using clock time rather than fixed value
   */
  public static void initRandom(boolean randomize) {
    if (randomize) {
      long seed = System.currentTimeMillis();
      System.out.println("Using clock time " + seed + " as random seed.");
      rand = new Random(seed);
    } else {
      System.out.println("Using fixed random seed for repeatability.");
      rand = new Random(0xad527c2b74e10cb3L);
    }
  }

  /**
   * Uniformly sample from a collection.
   */
  public static Object uniformSample(Collection set) {
    if (set.isEmpty()) {
      return null;
    }

    int index = randInt(set.size());
    Iterator setIterator = set.iterator();

    for (int counter = 0; counter < index; ++counter, setIterator.next())
      ;
    return setIterator.next();
  }

  /**
   * Returns a pseudorandom number uniformly distributed in the range [0, 1).
   * This method must not be called before initRandom() is called.
   */
  public static double random() {
    return rand.nextDouble(); // null pointer exception if not initialized
  }

  /**
   * Returns a pseudorandom integer sampled uniformly from {0, ..., n-1} Assumes
   * n > 0
   */
  public static int randInt(int n) {
    return rand.nextInt(n);
  }

  /**
   * Return a pseudorandom number sampled from a standard Gaussian.
   * This method must not be called before initRandom() is called.
   */
  public static double randGaussian() {
    return rand.nextGaussian();
  }

  /** Returns the sum of an array of doubles. */
  public static double sum(double[] array) {
    double result = 0;
    for (int i = 0; i != array.length; i++)
      result += array[i];
    return result;
  }

  /**
   * Returns an array of doubles resulting from the normalization of a given
   * array of (positive) doubles.
   */
  public static double[] normalize(double[] weights) {
    double[] result = new double[weights.length];
    double sum = sum(weights);
    for (int i = 0; i != weights.length; i++)
      result[i] = weights[i] / sum;
    return result;
  }

  /**
   * Returns an integer in the range {0, ..., probs.length - 1}, according to
   * the distribution specified by probs. If probs has length 0, returns -1.
   */
  public static int sampleWithProbs(double[] probs) {
    double u = random();
    double cumProb = 0;

    for (int i = 0; i < probs.length - 1; ++i) {
      cumProb += probs[i];
      if (u < cumProb) { // use < because random() could return 0
        return i;
      }
    }

    return (probs.length - 1);
  }

  public static class SampledMapEntry {
    public SampledMapEntry(Entry entry, double probability) {
      super();
      this.entry = entry;
      this.probability = probability;
    }

    public Object getKey() {
      return entry.getKey();
    }

    public Object getValue() {
      return entry.getValue();
    }

    /** Returns the entry's value as a double. */
    public double doubleValue() {
      return ((Number) entry.getValue()).doubleValue();
    }

    public double getProbability() {
      return probability;
    }

    public Map.Entry entry;
    public double probability;
  }

  /**
   * Samples an entry from a map with {@link Number} entries representing
   * weights, returning an instance of {@link SampledMapEntry} with entry and
   * probability of sampled entry, or <code>null</code> if the map is empty.
   */
  public static SampledMapEntry sampleBasedOnValue(Map map) {
    double sumOfValues = getSumOfValues(map);
    if (sumOfValues > 0) {
      double u = random();
      double cumProb = 0;
      int counter = 0;
      for (Map.Entry<Object, Number> entry : (Collection<Map.Entry<Object, Number>>) map
          .entrySet()) {
        double probability = entry.getValue().doubleValue() / sumOfValues;
        cumProb += probability;
        counter++;
        if (u <= cumProb || counter == map.size())
          return new SampledMapEntry(entry, probability);
      }
    }
    return null;
  }

  /**
   * Returns the sum of values (assumed to be {@link Number}s) in a map.
   */
  public static double getSumOfValues(Map map) {
    double sum = 0;
    for (Number value : (Collection<Number>) map.values()) {
      sum += value.doubleValue();
    }
    return sum;
  }

  /**
   * Returns log(n! / (n-m)!), that is, the log of the product of the first m
   * factors in the factorial of n.
   */
  public static double logPartialFactorial(int n, int m) {
    // Modified by yiwu @ Oct.8.2014
    return ArithmeticUtils.factorialLog(n)
        - ArithmeticUtils.factorialLog(n - m);
  }

  /**
   * Returns the factorial of n.
   */
  public static int factorial(int n) {
    // Modified by yiwu @ Oct.8.2014
    return (int) ArithmeticUtils.factorial(n);
  }

  /**
   * Returns the log of the factorial of n. This may be faster than just calling
   * Math.log(Util.factorial(n)).
   */
  public static double logFactorial(int n) {
    // Modified by yiwu @ Oct.8.2014
    return ArithmeticUtils.factorialLog(n);
  }

  /**
   * Returns (n choose k) = n! / (n-k)! k!.
   */
  public static int choose(int n, int k) {
    // Modified by yiwu on Oct.8.2014
    return (int) ArithmeticUtils.binomialCoefficient(n, k);
  }

  /**
   * Returns the number of ways to make k choices from a set of n bins, where
   * the same bin may be chosen multiple times. This is (n + k - 1) choose (n -
   * 1).
   */
  public static int multichoose(int n, int k) {
    return choose(n + k - 1, n - 1);
  }

  /**
   * Addition in the log domain.
   * 
   * @return approximation to ln(e^logX + e^logY)
   */
  public static double logSum(double logX, double logY) {
    // Ensure X is greater than Y.
    if (logY > logX) {
      double temp = logX;
      logX = logY;
      logY = temp;
    }

    // Adding zero is a no-op.
    if (logX == Double.NEGATIVE_INFINITY) {
      return logX;
    }

    // If Y is many orders of magnitude less than X, then ignore it.
    double negDiff = logY - logX;
    if (negDiff < -20) {
      return logX;
    }

    // Otherwise use some nice algebra to stay in the log domain.
    // See proof at
    // https://facwiki.cs.byu.edu/nlp/index.php/Log_Domain_Computations
    return logX + java.lang.Math.log(1.0 + java.lang.Math.exp(negDiff));
  }

  /**
   * Maximum difference that we are willing to ignore between two floating-point
   * values. For instance, the sum of some probabilities may not be exactly 1.0,
   * but this may just be due to floating-point inaccuracies, so we may want to
   * consider it "close enough" to 1.0.
   */
  public static final double TOLERANCE = 1e-10;

  /**
   * Returns true if the two given values differ by no more than Util.TOLERANCE.
   */
  public static boolean withinTol(double x, double y) {
    return (Math.abs(x - y) <= Util.TOLERANCE);
  }

  /**
   * Returns true if <code>x</code> is greater than <code>y</code> by at least
   * Util.TOLERANCE.
   */
  public static boolean signifGreaterThan(double x, double y) {
    return (x - y >= Util.TOLERANCE);
  }

  /**
   * Returns true if <code>x</code> is less than <code>y</code> by at least
   * Util.TOLERANCE.
   */
  public static boolean signifLessThan(double x, double y) {
    return (y - x >= Util.TOLERANCE);
  }

  /**
   * Prints the error message and stack trace for the given exception, and exits
   * the program, returning code 1.
   */
  public static void fatalError(Throwable e) {
    fatalError(e, true);
  }

  /**
   * Prints a top level message, the error message and stack trace for the given
   * exception, and exits the program, returning code 1.
   */
  public static void fatalError(String topLevelMessage, Throwable e) {
    fatalError(topLevelMessage, e, true);
  }

  /**
   * Prints the error message for the given exception, and optionally prints a
   * stack trace. Then exits the program with return code 1.
   */
  public static void fatalError(Throwable e, boolean trace) {
    fatalError("Fatal error: ", e, trace);
  }

  private static boolean suppress_error = false;

  public static void setSuppressError(boolean b) {
    suppress_error = b;
  }

  /**
   * Prints a top level message, the error message for the given exception, and
   * optionally prints a stack trace. Then exits the program with return code 1.
   */
  public static void fatalError(String topLevelMessage, Throwable e,
      boolean trace) {
    if (suppress_error)
      return;
    System.err.println(topLevelMessage + "\n" + e.getMessage());
    if (trace) {
      e.printStackTrace();
      Throwable cause = e.getCause();
      if (cause != null) {
        System.err.println("Cause: " + cause.getMessage());
        cause.printStackTrace();
      }
    }

    throw new Error(topLevelMessage);
  }

  /**
   * Prints error message and exits.
   * 
   * @param msg
   *          the error message
   * 
   */
  public static void fatalError(String msg) {
    fatalError(msg, true);
  }

  /**
   * Prints error message, optionally prints stack trace, and exits.
   * 
   * @param msg
   *          the error message
   * 
   * @param trace
   *          if true, print a stack trace
   */
  public static void fatalError(String msg, boolean trace) {
    if (suppress_error)
      return;
    System.err.println("Fatal error: " + msg);
    if (trace) {
      Thread.currentThread().dumpStack();
    }
    throw new Error(msg);
  }

  /**
   * Prints error message without printing stack trace, and exits.
   * 
   * @param msg
   *          the error message
   */
  public static void fatalErrorWithoutStack(String msg) {
    fatalError(msg, false);
  }

  /**
   * Returns true if the program should print extra status messages. This value
   * is false unless it has been set to true using <code>setVerbose</code>.
   */
  public static boolean verbose() {
    return verbose;
  }

  /**
   * Returns true if the program should print extra the model. This value
   * is false unless it has been set to true using <code>setPrint</code>.
   */
  public static boolean print() {
    return print;
  }

  /**
   * Sets a flag indicating whether the program should print extra status
   * messages. If this message is not called, the flag defaults to false.
   */
  public static void setVerbose(boolean v) {
    verbose = v;
  }

  /**
   * Sets a flag indicating whether the program should print the generated
   * model. If this message is not called, the flag defaults to false.
   */
  public static void setPrint(boolean p) {
    print = p;
  }

  /**
   * Returns a string formed by the concatenation of string versions of the
   * elements in a collection, separated by a given separator.
   */
  public static String join(String separator, Collection c) {
    StringBuffer buffer = new StringBuffer();
    Iterator it = c.iterator();
    if (it.hasNext())
      buffer.append(it.next());
    while (it.hasNext())
      buffer.append(separator + it.next());
    return buffer.toString();
  }

  /**
   * Returns an unmodifiable list equal to the concatenation of the two given
   * lists.
   */
  public static <T> List<T> concat(List<? extends T> list1,
      List<? extends T> list2) {
    return new ConcatenationList<T>(list1, list2);
  }

  /**
   * Nested class for implementing the <code>concat</code> method.
   */
  private static class ConcatenationList<T> extends AbstractList<T> {

    ConcatenationList(List<? extends T> list1, List<? extends T> list2) {
      this.list1 = list1;
      this.list2 = list2;
    }

    public int size() {
      return (list1.size() + list2.size());
    }

    public T get(int index) {
      if (index < list1.size()) {
        return list1.get(index);
      }
      return list2.get(index - list1.size());
    }

    private List<? extends T> list1;
    private List<? extends T> list2;
  }

  /**
   * Returns an unmodifiable collection equal to the union of the two given
   * collections, which are assumed to be disjoint. The iteration order is the
   * iteration order of <code>s1</code> followed by the iteration order of
   * <code>s2</code>.
   */
  public static <T> Collection<T> disjointUnion(Collection<? extends T> s1,
      Collection<? extends T> s2) {
    return new DisjointUnionCollection<T>(s1, s2);
  }

  /**
   * Nested class for implementing the <code>disjointUnion</code> method.
   */
  private static class DisjointUnionCollection<T> extends AbstractCollection<T> {
    DisjointUnionCollection(Collection<? extends T> s1,
        Collection<? extends T> s2) {
      this.s1 = s1;
      this.s2 = s2;
    }

    public int size() {
      return (s1.size() + s2.size());
    }

    public boolean contains(Object o) {
      return (s1.contains(o) || s2.contains(o));
    }

    public Iterator<T> iterator() {
      return new DisjointUnionIterator();
    }

    private class DisjointUnionIterator implements Iterator<T> {
      public boolean hasNext() {
        return (s1iter.hasNext() || s2iter.hasNext());
      }

      public T next() {
        if (s1iter.hasNext()) {
          return s1iter.next();
        } else if (s2iter.hasNext()) {
          return s2iter.next();
        }
        throw new NoSuchElementException();
      }

      public void remove() {
        throw new UnsupportedOperationException(
            "Can't remove from DisjointUnionSet.");
      }

      private Iterator<? extends T> s1iter = s1.iterator();
      private Iterator<? extends T> s2iter = s2.iterator();
    }

    private Collection<? extends T> s1;
    private Collection<? extends T> s2;
  }

  /**
   * Returns an unmodifiable set equal to the intersection of the two given
   * sets.
   */
  public static <T> Set<T> intersection(Set<? extends T> s1, Set<? extends T> s2) {
    return new IntersectionSet<T>(s1, s2);
  }

  /**
   * Nested class for implementing the <code>intersection</code> method.
   */
  private static class IntersectionSet<T> extends AbstractSet<T> {

    IntersectionSet(Set<? extends T> s1, Set<? extends T> s2) {
      this.s1 = s1;
      this.s2 = s2;
    }

    public int size() {
      Set<? extends T> smaller = (s1.size() <= s2.size()) ? s1 : s2;
      Set<? extends T> larger = (smaller == s1) ? s2 : s1;

      int size = 0;
      for (T obj : smaller) {
        if (larger.contains(obj)) {
          ++size;
        }
      }
      return size;
    }

    public boolean contains(Object obj) {
      return (s1.contains(obj) && s2.contains(obj));
    }

    public Iterator<T> iterator() {
      return new IntersectionSetIterator();
    }

    private class IntersectionSetIterator implements Iterator<T> {
      IntersectionSetIterator() {
        Set<? extends T> smaller = (s1.size() <= s2.size()) ? s1 : s2;
        Set<? extends T> larger = (smaller == s1) ? s2 : s1;

        smallerIter = smaller.iterator();
        nextObj = findNext();
      }

      public boolean hasNext() {
        return (nextObj != null);
      }

      public T next() {
        if (nextObj == null) {
          throw new NoSuchElementException();
        }

        T toReturn = nextObj;
        nextObj = findNext();
        return toReturn;
      }

      public void remove() {
        throw new UnsupportedOperationException(
            "Tried to remove element from IntersectionSet.");
      }

      private T findNext() {
        while (smallerIter.hasNext()) {
          T obj = smallerIter.next();
          if (larger.contains(obj)) {
            return obj;
          }
        }

        return null;
      }

      private Iterator<? extends T> smallerIter;
      private Set<? extends T> larger;
      private T nextObj;
    }

    private Set<? extends T> s1;
    private Set<? extends T> s2;
  }

  public static SetWithDistrib uniformDistrib(IndexedSet s) {
    return new UniformDistrib(s);
  }

  private static class UniformDistrib implements SetWithDistrib {
    public UniformDistrib(IndexedSet s) {
      this.s = s;
    }

    public double getProb(Object o) {
      return (s.contains(o) ? (1.0 / s.size()) : 0);
    }

    public double getLogProb(Object o) {
      return Math.log(getProb(o));
    }

    public Object sample() {
      if (s.isEmpty()) {
        return null;
      }
      return s.get(Util.randInt(s.size()));
    }

    private IndexedSet s;
  }

  /**
   * Returns the number of lines in the given file. This is the number of times
   * that BufferedReader's readLine method can be called on this file before it
   * returns null.
   */
  public static int getNumLines(File file) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(file));
    int numLines = 0;
    while (reader.readLine() != null) {
      ++numLines;
    }
    return numLines;
  }

  /**
   * Returns an iterator over the integers in the range from <code>lower</code>
   * to <code>upper</code>, inclusive. The iterator returns integers in
   * ascending order. If <code>lower</code> is greater than <code>upper</code>,
   * the iterator has no elements.
   * 
   * @return Iterator over Integer
   */
  public static Iterator getIntegerRangeIterator(int lower, int upper) {
    return new IntRangeIterator(lower, upper);
  }

  /**
   * Nested class for implementing getIntegerRangeIterator.
   */
  private static class IntRangeIterator implements Iterator {
    IntRangeIterator(int lower, int upper) {
      this.upper = upper;
      if (lower <= upper) {
        nextInt = new Integer(lower);
      }
    }

    public boolean hasNext() {
      return (nextInt != null);
    }

    public Object next() {
      if (nextInt == null) {
        throw new NoSuchElementException();
      }

      Integer toReturn = nextInt;
      if (nextInt.intValue() < upper) {
        nextInt = new Integer(nextInt.intValue() + 1);
      } else {
        // Note that we don't increment nextInt in this case,
        // so we won't get overflow if upper is Integer.MAX_VALUE.
        nextInt = null;
      }
      return toReturn;
    }

    public void remove() {
      throw new UnsupportedOperationException(
          "Can't remove from IntRangeIterator");
    }

    Integer nextInt = null;
    int upper;
  }

  /**
   * Returns an iterator over the integers greater than or equal to
   * <code>lower</code>, in ascending order. This iterator uses the mathematical
   * rather than the computational notion of an integer, so its
   * <code>hasNext</code> method never returns false, even when it has already
   * iterated over <code>Integer.MAX_VALUE</code>. If this iterator's
   * <code>next</code> method is called enough times, it will eventually throw
   * an ArithmeticException indicating that the next integer cannot be
   * represented.
   * 
   * @return Iterator over Integer
   */
  public static Iterator getAscendingIntegerIterator(int lower) {
    return new AscendingIntIterator(lower);
  }

  private static class AscendingIntIterator implements Iterator {
    AscendingIntIterator(int lower) {
      nextInt = new Integer(lower);
    }

    public boolean hasNext() {
      return true;
    }

    public Object next() {
      if (nextInt == null) {
        throw new ArithmeticException(
            "Next integer in ascending order is not representable.");
      }

      Integer toReturn = nextInt;
      if (nextInt.intValue() < Integer.MAX_VALUE) {
        nextInt = new Integer(nextInt.intValue() + 1);
      } else {
        nextInt = null;
      }
      return toReturn;
    }

    public void remove() {
      throw new UnsupportedOperationException(
          "Can't remove from AscendingIntIterator");
    }

    Integer nextInt;
  }

  /**
   * Returns an iterator over the integers less than or equal to
   * <code>upper</code>, in descending order. This iterator uses the
   * mathematical rather than the computational notion of an integer, so its
   * <code>hasNext</code> method never returns false, even when it has already
   * iterated over <code>Integer.MIN_VALUE</code>. If this iterator's
   * <code>next</code> method is called enough times, it will eventually throw
   * an ArithmeticException indicating that the next integer cannot be
   * represented.
   * 
   * @return Iterator over Integer
   */
  public static Iterator getDescendingIntegerIterator(int upper) {
    return new DescendingIntIterator(upper);
  }

  private static class DescendingIntIterator implements Iterator {
    DescendingIntIterator(int upper) {
      nextInt = new Integer(upper);
    }

    public boolean hasNext() {
      return true;
    }

    public Object next() {
      if (nextInt == null) {
        throw new ArithmeticException(
            "Next integer in descending order is not representable.");
      }

      Integer toReturn = nextInt;
      if (nextInt.intValue() > Integer.MIN_VALUE) {
        nextInt = new Integer(nextInt.intValue() - 1);
      } else {
        nextInt = null;
      }
      return toReturn;
    }

    public void remove() {
      throw new UnsupportedOperationException(
          "Can't remove from DescendingIntIterator");
    }

    Integer nextInt;
  }

  /**
   * Returns an iterator over all integers, in order by magnitude, with positive
   * integers coming before negative integers of the same magnitude. The
   * iterator uses the mathematical rather than computational notion of an
   * integer, so its <code>hasNext</code> method always returns true, even when
   * <code>Integer.MAX_VALUE</code> has already been returned (note that
   * <code>Integer.MAX_VALUE</code> has a smaller magnitude than
   * <code>Integer.MIN_VALUE</code>, so it will be reached first). If the
   * iterator's <code>next</code> method is called enough times, it will
   * eventually throw an <code>ArithmeticException</code> indicating that the
   * next integer is not representable.
   * 
   * @return Iterator over Integer
   */
  public static Iterator getIntegerIterator() {
    return new IntIterator();
  }

  private static class IntIterator implements Iterator {
    IntIterator() {
      nextInt = new Integer(0);
    }

    public boolean hasNext() {
      return true;
    }

    public Object next() {
      if (nextInt == null) {
        throw new ArithmeticException(
            "Next integer by magnitude is not representable.");
      }

      Integer toReturn = nextInt;
      int inverse = -nextInt.intValue();
      if (inverse >= 0) {
        // Next integer will be positive; increase magnitude
        if (inverse < Integer.MAX_VALUE) { // don't exceed MAX_VALUE
          nextInt = new Integer(inverse + 1);
        } else {
          nextInt = null;
        }
      } else {
        // Next integer will be negative; same magnitude as previous.
        // Don't need to worry about MIN_VALUE here because
        // its magnitude is >= MAX_VALUE.
        nextInt = new Integer(inverse);
      }
      return toReturn;
    }

    public void remove() {
      throw new UnsupportedOperationException("Can't remove from IntSet.");
    }

    Integer nextInt;
  }

  /**
   * Returns the first element <code>x</code> in <code>c</code> such that
   * <code>f(x) != null</code>, or <code>null</code> if there is no such
   * element.
   */
  public static Object findFirst(Collection c, UnaryFunction f) {
    Iterator i = c.iterator();
    while (i.hasNext()) {
      Object x = i.next();
      if (f.evaluate(x) != null)
        return x;
    }
    return null;
  }

  /**
   * Returns the first element <code>x</code> in <code>c</code> satisfying
   * <code>p</code>, or <code>null</code> if there is no such element.
   */
  public static Object findFirst(Collection c, UnaryPredicate p) {
    Iterator i = c.iterator();
    while (i.hasNext()) {
      Object x = i.next();
      if (p.evaluate(x))
        return x;
    }
    return null;
  }

  /**
   * Returns the first element <code>x</code> in <code>c</code> satisfying
   * <code>p</code>, or <null> if there is no such element.
   */
  public static Object findFirstEquals(Collection c, final Object o) {
    UnaryPredicate equals = new UnaryPredicate() {
      public boolean evaluate(Object another) {
        return another.equals(o);
      }
    };
    return findFirst(c, equals);
  }

  /**
   * Returns the first element in a collection (according to its iterator).
   * 
   * @throws java.util.NoSuchElementException
   *           if collection is empty.
   */
  public static Object getFirst(Collection c) {
    return c.iterator().next();
  }

  /**
   * Returns the first element in a collection (according to its iterator) or
   * <code>null</code> if there are none.
   */
  public static Object getFirstOrNull(Collection c) {
    if (c.size() == 0)
      return null;
    return c.iterator().next();
  }

  /**
   * Returns the last element in a collection (by exhausting its iterator in
   * case it is not a list).
   * 
   * @throws java.util.NoSuchElementException
   *           if collection is empty.
   */
  public static Object getLast(Collection c) {
    if (c instanceof List)
      return getLast((List) c);
    Object result = null;
    Iterator it = c.iterator();
    do { // we use 'do' to ensure next() is called at least once, throwing
      // exception if c is empty.
      result = it.next();
    } while (it.hasNext());
    return result;
  }

  /**
   * Specialization of {@link #getLast(Collection)} for lists, using
   * get(size-1).
   */
  public static Object getLast(List c) {
    return c.get(c.size() - 1);
  }

  /**
   * Given a string <code>description</code>, returns <code>description</code>
   * if its length is no greater than 10, or <code>description</code> + "..."
   * otherwise.
   */
  public static String abbreviation(String description) {
    return "\"" + description.substring(0, Math.min(10, description.length()))
        + (description.length() > 10 ? "(...)" : "") + "\"";
  }

  /**
   * print the debug information, the debug info can be a list of String
   * If the verbose tag is set, the message will be printed in one line with a
   * line break.
   * 
   * Note that to improve efficiency, <code>debug(a, " ok", b)</code> is
   * preferred, instead of <code>debug(a+" ok"+b)</code>
   * 
   * @param msgs
   */
  public static void debug(Object... msgs) {
    if (verbose) {
      for (Object s : msgs) {
        System.out.print(s);
      }
      System.out.println();
    }
  }

  /**
   * Indicates whether two objects are both null or equal.
   */
  public static boolean equalsOrBothNull(Object a, Object b) {
    return (a == null && b == null) || a.equals(b);
  }

  /** Returns an array with the elements in a given collection. */
  public static Object[] asArray(Collection c) {
    Object[] array = new Object[c.size()];
    int i = 0;
    for (Iterator it = c.iterator(); it.hasNext();) {
      array[i++] = it.next();
    }
    return array;
  }

  /** Wait for a timeout, issuing a fatal error in case of interruption. */
  public static void wait_NE(long time) {
    try {
      Thread.currentThread().sleep(time);
    } catch (Exception e) {
      Util.fatalError("Unexpected interruption:", e);
    }
  }

  /**
   * Returns a {@link NullaryFunction} that returns a new iterator to the given
   * collection each time is it invoked.
   */
  static public NullaryFunction getIteratorNullaryFunction(final Collection c) {
    return new NullaryFunction() {
      public Object evaluate() {
        return c.iterator();
      }
    };
  }

  /**
   * Returns a new instance of a class given constructor signature and
   * arguments, throwing a fatal error if an exception is raised.
   * 
   * @param className
   *          the class.
   * @param paramTypes
   *          the parameter types defining which constructor to use.
   * @param args
   *          the arguments to be passed to constructor.
   * @return the new object.
   */
  public static Object makeInstance_NE(String className, Class[] paramTypes,
      Object[] args) {
    Object result = null;
    try {
      Class clazz = Class.forName(className);
      Constructor constructor = clazz.getConstructor(paramTypes);
      result = constructor.newInstance(args);
    } catch (Exception e) {
      Util.fatalError(e);
    }
    return result;
  }

  /**
   * Returns true if <code>number</code> is really close to zero. Uses global
   * constant <code>APPROXIMATE_ZERO</code> as the threshold.
   */
  public static boolean closeToZero(double number) {
    /**
     * For all intents and purposes, numbers below this constant are considered
     * zero in terms of floating point. The use case for this is when the sum of
     * a vector of unnormalized probabilities is too small to effectively
     * normalize using floating point precision.
     * note a single precision can represent up to 1E-38
     * double precision 1E-308
     */
    final double APPROXIMATE_ZERO = 1e-12;
    return (number < APPROXIMATE_ZERO) && (number > -1 * APPROXIMATE_ZERO);
  }

  /**
   * Copies a two-dimensional array with fixed length
   */
  public static double[][] copy2DArray(double[][] array) {
    double[][] newAry = new double[array.length][array[0].length];
    for (int i = 0; i < newAry.length; i++) {
      System.arraycopy(array[i], 0, newAry[i], 0, array[i].length);
    }
    return newAry;
  }

  private static Random rand;
  private static boolean verbose = false;
  private static boolean print = false;

}
