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
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

  public static void initRandom(long seed) {
    System.out.println("Using given random seed " + seed + ".");
    rand = new Random(seed);
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

  /**
   * Returns an integer in the range {0, ..., probs.length - 1}, according to
   * the given weights. If <code>weights</code> has length 0, returns -1.
   */
  public static int sampleWithWeights(double[] weights, double sum) {
    double u = sum * random();
    double cumWeight = 0;

    for (int i = 0; i < weights.length - 1; ++i) {
      cumWeight += weights[i];
      if (u < cumWeight) { // use < because random() could return 0
        return i;
      }
    }

    return (weights.length - 1);
  }

  /**
   * Returns a list of min(<code>list.size()</code>, <code>n</code>) objects
   * sampled without replacement from <code>list</code>.
   */
  public static List sampleWithoutReplacement(List list, int n) {
    List sampledObjs = new ArrayList();
    List sampledIndices = new LinkedList(); // sorted list of Integer
    n = Math.min(list.size(), n);

    for (int i = 0; i < n; ++i) {
      int index = randInt(list.size() - i);

      // find the index-th unsampled element of the list
      ListIterator iter = sampledIndices.listIterator();
      while (iter.hasNext()) {
        int alreadySampled = ((Integer) iter.next()).intValue();
        if (alreadySampled <= index) {
          ++index; // don't count the already-sampled index
        } else {
          iter.previous(); // so we can insert new index correctly
          break;
        }
      }

      sampledObjs.add(list.get(index));
      iter.add(new Integer(index));
    }

    return sampledObjs;
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
   * Randomly shuffles the given list, in a way such that all permutations are
   * equally likely.
   */
  public static void shuffle(List list) {
    Collections.shuffle(list, rand);
    fatalError("Shuffle used.");
  }

  /**
   * A sampler of nonnegative reals following an inverse polynomial density
   * <code>1/((x+\delta)^n), \delta > 0</code>. The purpose of
   * <code>/delta</code> is to avoid 0 having infinite density. For practical
   * purposes, the sampler only returns numbers in <code>[0, u)</code>, for
   * <code>u</code> an upper limit given by the user. The class also lets the
   * user declare how much mass should be left beyond the upper limit; this
   * should be a small probability. There is a constructor with a default mass
   * beyond upper limit set to 0.01. Given n and these two parameters, the class
   * automatically sets \delta to a value that makes it so.
   * 
   * @author Rodrigo
   */
  public static class InversePolynomialSampler {

    /**
     * Creates sampler with mass beyond upper limit set to 0.01, with
     * <code>n</code> = 2..
     */
    public InversePolynomialSampler(double upperLimit) {
      constructor(2, upperLimit, 0.01);
    }

    /**
     * Creates sampler with mass beyond upper limit set to 0.01.
     */
    public InversePolynomialSampler(double n, double upperLimit) {
      constructor(n, upperLimit, 0.01);
    }

    public InversePolynomialSampler(double n, double upperLimit,
        double massBeyondUpperLimit) {
      constructor(n, upperLimit, massBeyondUpperLimit);
    }

    private void constructor(double n, double upperLimit,
        double massBeyondUpperLimit) {
      this.n = n;
      this.massBeyondUpperLimit = massBeyondUpperLimit;
      this.upperLimit = upperLimit;
      delta = upperLimit
          / ((1 / Math.pow(massBeyondUpperLimit, 1 / (n - 1))) - 1);
    }

    public int nextSample() {
      int result;
      do {
        double roll = Util.random();
        result = (int) Math.floor(delta / Math.pow(1 - roll, 1.0 / (n - 1))
            - delta);
      } while (result >= upperLimit);
      return result;
    }

    private double n;
    private double upperLimit;
    private double massBeyondUpperLimit;
    private double delta;
  }

  /**
   * Returns log(n! / (n-m)!), that is, the log of the product of the first m
   * factors in the factorial of n.
   */
  public static double logPartialFactorial(int n, int m) {
    double sum = 0;
    for (int i = n; i > n - m; i--) {
      sum += Math.log(i);
    }
    return sum;
  }

  /**
   * Returns the factorial of n.
   */
  public static int factorial(int n) {
    int prod = 1;
    for (int i = 2; i <= n; i++) {
      prod *= i;
    }
    return prod;
  }

  /** Returns the base 2 logarithm of a number. */
  public static double log2(double x) {
    return Math.log10(x) / Math.log10(2);
  }

  /**
   * Returns the log of the factorial of n. This may be faster than just calling
   * Math.log(Util.factorial(n)).
   */
  public static double logFactorial(int n) {
    return logPartialFactorial(n, n);
  }

  /**
   * Returns (n choose k) = n! / (n-k)! k!.
   */
  public static int choose(int n, int k) {
    int prod = 1;
    for (int i = n; i > n - k; --i) {
      prod *= i;
    }
    return (prod / factorial(k));
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
   * Given two substrings defined by "begin" and "end" indices in some original
   * string, returns the index of the first character that is in either of these
   * two strings, or 0 if both strings are empty.
   * 
   * @param begin1
   *          index of first char in substring 1
   * @param end1
   *          one plus index of last char in substring 1
   * @param begin2
   *          index of first char in substring 2
   * @param end2
   *          one plus index of last char in substring 2
   */
  public static int substringPairBegin(int begin1, int end1, int begin2,
      int end2) {
    if (begin1 == end1) {
      if (begin2 == end2) {
        return 0;
      }
      return begin2;
    }
    if (begin2 == end2) {
      return begin1;
    }
    return Math.min(begin1, begin2);
  }

  /**
   * Returns the substring of <code>str</code> from
   * <code>substringPairBegin(begin1, end1, begin2, end2)</code> to
   * <code>substringPairEnd(begin1, end1, begin2, end2)</code>.
   */
  public static String spannedSubstring(String str, int begin1, int end1,
      int begin2, int end2) {
    return str.substring(substringPairBegin(begin1, end1, begin2, end2),
        substringPairEnd(begin1, end1, begin2, end2));
  }

  /**
   * Given two substrings defined by "begin" and "end" indices in some original
   * string, returns one plus the index of the last character that is in one of
   * these two strings, or 0 if both strings are empty.
   * 
   * @param begin1
   *          index of first char in substring 1
   * @param end1
   *          one plus index of last char in substring 1
   * @param begin2
   *          index of first char in substring 2
   * @param end2
   *          one plus index of last char in substring 2
   */
  public static int substringPairEnd(int begin1, int end1, int begin2, int end2) {
    if (begin1 == end1) {
      if (begin2 == end2) {
        return 0;
      }
      return end2;
    }
    if (begin2 == end2) {
      return end1;
    }
    return Math.max(end1, end2);
  }

  /**
   * Returns the string formed by concatenating the two given strings, with a
   * space in between if both strings are non-empty.
   */
  public static String join(String str1, String str2) {
    if (str1.length() == 0) {
      return str2;
    }
    if (str2.length() == 0) {
      return str1;
    }

    StringBuffer buf = new StringBuffer(str1);
    buf.append(' ');
    buf.append(str2);
    return buf.toString();
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
   * Same as {@link #join(String, Collection)}.
   */
  public static String join(Collection c, String separator) {
    return join(separator, c);
  }

  /**
   * Calls {@link #join(String, Collection)} with ", " as separator.
   */
  public static String join(Collection c) {
    return join(", ", c);
  }

  /**
   * Calls {@link #join(Collection)} on the given array as a collection.
   */
  public static String join(Object[] a) {
    return join(Arrays.asList(a));
  }

  /**
   * Calls {@link #join(String, Collection)} on the given array as a collection.
   */
  public static String join(String separator, Object[] a) {
    return join(separator, Arrays.asList(a));
  }

  /**
   * Produces a string with map entry representations separated by a given entry
   * separator, where entry representations are the key and value
   * representations separated by a key-value separator.
   */
  public static String join(String entrySeparator, String keyValueSeparator,
      Map map) {
    List c = new LinkedList();
    for (Map.Entry entry : (Collection<Map.Entry>) map.entrySet()) {
      c.add(entry.getKey() + keyValueSeparator + entry.getValue());
    }
    return join(entrySeparator, c);
  }

  /**
   * Same as {@link #join(String, String, Map)} with key-value separator equal
   * to <code>" -> "</code>.
   */
  public static String join(String entrySeparator, Map map) {
    return join(entrySeparator, " -> ", map);
  }

  /**
   * Same as {@link #join(String, String, Map)} with entry separator equal to
   * <code>", "</code> and key-value separator equal to <code>" -> "</code>.
   */
  public static String join(Map map) {
    return join(", ", " -> ", map);
  }

  /**
   * Given a string, returns a version of that string where all letters have
   * been converted to lower case, and all characters that are not letters or
   * digits have been removed.
   */
  public static String normalize(String input) {
    StringBuffer output = new StringBuffer();
    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);
      if (Character.isLetterOrDigit(c)) {
        output.append(Character.toLowerCase(c));
      }
    }
    return output.toString();
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
   * Returns the element in a collection obtained with
   * {@link #getFirst(Collection)}, after removing it from the collection.
   */
  public static Object pop(Collection c) {
    Object first = getFirst(c);
    c.remove(first);
    return first;
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
   * Given a string <code>description</code>, returns <code>description</code>
   * if its length is no greater than <code>n</code>, or
   * <code>description</code> + "..." otherwise.
   */
  public static String abbreviation(String description, int n) {
    return "\"" + description.substring(0, Math.min(n, description.length()))
        + (description.length() > n ? "(...)" : "") + "\"";
  }

  /**
   * Returns the mean of a collection of objects assumed to be {@link Number}s.
   */
  public static double mean(Collection values) {
    double sum = 0;
    for (Iterator it = values.iterator(); it.hasNext();) {
      sum += ((Number) it.next()).doubleValue();
    }
    return sum / values.size();
  }

  /**
   * Returns the variance of a collection of objects assumed to be
   * {@link Number}s.
   */
  public static double variance(Collection values) {
    double mean = mean(values);
    double sum = 0;
    for (Iterator it = values.iterator(); it.hasNext();) {
      sum += Math.pow(((Number) it.next()).doubleValue() - mean, 2);
    }
    return sum / values.size();
  }

  public static double sum(Collection values) {
    double sum = 0;
    for (Number value : (Collection<Number>) values) {
      sum += value.doubleValue();
    }
    return sum;
  }

  public static double average(Collection values) {
    return sum(values) / values.size();
  }

  /** Returns an empty LinkedList. */
  public static LinkedList list() {
    return new LinkedList();
  }

  /** Returns a LinkedList containing <code>object</code> only. */
  public static LinkedList list(Object object) {
    LinkedList list = new LinkedList();
    list.add(object);
    return list;
  }

  /** Returns a LinkedList containing the objects given as arguments. */
  public static LinkedList list(Object o1, Object o2) {
    LinkedList list = new LinkedList();
    list.add(o1);
    list.add(o2);
    return list;
  }

  /** Returns a LinkedList containing the objects given as arguments. */
  public static LinkedList list(Object o1, Object o2, Object o3) {
    LinkedList list = Util.list(o1, o2);
    list.add(o3);
    return list;
  }

  /** Returns a LinkedList containing the objects given as arguments. */
  public static LinkedList list(Object o1, Object o2, Object o3, Object o4) {
    LinkedList list = Util.list(o1, o2, o3);
    list.add(o4);
    return list;
  }

  /** Returns a LinkedList containing the objects given as arguments. */
  public static LinkedList list(Object o1, Object o2, Object o3, Object o4,
      Object o5) {
    LinkedList list = Util.list(o1, o2, o3, o4);
    list.add(o5);
    return list;
  }

  /** Returns a LinkedList containing the objects given as arguments. */
  public static LinkedList list(Object o1, Object o2, Object o3, Object o4,
      Object o5, Object o6) {
    LinkedList list = Util.list(o1, o2, o3, o4, o5);
    list.add(o6);
    return list;
  }

  /** Returns an empty ArrayList. */
  public static ArrayList arrayList() {
    return new ArrayList();
  }

  /** Returns a ArrayList containing <code>object</code> only. */
  public static ArrayList arrayList(Object object) {
    ArrayList arrayList = new ArrayList();
    arrayList.add(object);
    return arrayList;
  }

  /** Returns a ArrayList containing the objects given as arguments. */
  public static ArrayList arrayList(Object o1, Object o2) {
    ArrayList arrayList = new ArrayList();
    arrayList.add(o1);
    arrayList.add(o2);
    return arrayList;
  }

  /** Returns a ArrayList containing the objects given as arguments. */
  public static ArrayList arrayList(Object o1, Object o2, Object o3) {
    ArrayList arrayList = Util.arrayList(o1, o2);
    arrayList.add(o3);
    return arrayList;
  }

  /** Returns a ArrayList containing the objects given as arguments. */
  public static ArrayList arrayList(Object o1, Object o2, Object o3, Object o4) {
    ArrayList arrayList = Util.arrayList(o1, o2, o3);
    arrayList.add(o4);
    return arrayList;
  }

  /** Returns a ArrayList containing the objects given as arguments. */
  public static ArrayList arrayList(Object o1, Object o2, Object o3, Object o4,
      Object o5) {
    ArrayList arrayList = Util.arrayList(o1, o2, o3, o4);
    arrayList.add(o5);
    return arrayList;
  }

  /** Returns a ArrayList containing the objects given as arguments. */
  public static ArrayList arrayList(Object o1, Object o2, Object o3, Object o4,
      Object o5, Object o6) {
    ArrayList arrayList = Util.arrayList(o1, o2, o3, o4, o5);
    arrayList.add(o6);
    return arrayList;
  }

  /**
   * Generates a LinkedList with integers <code>{start, ..., end - 1}</code>,
   * skipping <code>step</code> values at a time.
   */
  public static LinkedList listFromTo(int start, int end, int step) {
    LinkedList result = new LinkedList();
    for (int i = start; i < end; i += step)
      result.add(i);
    return result;
  }

  /**
   * Adds <code>o</code> to beginning of <code>list</code> and returns
   * <code>list</code>.
   */
  public static List addAtBeginning(Object o, List list) {
    list.add(0, o);
    return list;
  }

  /**
   * Adds <code>o1</code> and <code>o2</code> to beginning of <code>list</code>
   * and returns <code>list</code>. <code>o1</code> is placed before
   * <code>o2</code> in the list.
   */
  public static List addAtBeginning(Object o1, Object o2, List list) {
    list.add(0, o2);
    list.add(0, o1);
    return list;
  }

  public static HashSet set() {
    return new HashSet();
  }

  public static HashSet set(Object o1) {
    HashSet result = new HashSet();
    result.add(o1);
    return result;
  }

  public static HashSet set(Object o1, Object o2) {
    HashSet result = new HashSet();
    result.add(o1);
    result.add(o2);
    return result;
  }

  public static HashSet set(Object o1, Object o2, Object o3) {
    HashSet result = new HashSet();
    result.add(o1);
    result.add(o2);
    result.add(o3);
    return result;
  }

  public static HashMultiset multiset() {
    return new HashMultiset();
  }

  public static HashMultiset multiset(Object o1) {
    HashMultiset result = new HashMultiset();
    result.add(o1);
    return result;
  }

  public static HashMultiset multiset(Object o1, Object o2) {
    HashMultiset result = new HashMultiset();
    result.add(o1);
    result.add(o2);
    return result;
  }

  public static HashMultiset multiset(Object o1, Object o2, Object o3) {
    HashMultiset result = new HashMultiset();
    result.add(o1);
    result.add(o2);
    result.add(o3);
    return result;
  }

  public static HashMap map() {
    return new HashMap();
  }

  public static HashMap map(Object key1, Object value1) {
    HashMap result = new HashMap();
    result.put(key1, value1);
    return result;
  }

  public static HashMap map(Object key1, Object value1, Object key2,
      Object value2) {
    HashMap result = new HashMap();
    result.put(key1, value1);
    result.put(key2, value2);
    return result;
  }

  public static HashMap map(Object key1, Object value1, Object key2,
      Object value2, Object key3, Object value3) {
    HashMap result = new HashMap();
    result.put(key1, value1);
    result.put(key2, value2);
    result.put(key3, value3);
    return result;
  }

  public static Properties properties() {
    Properties result = new Properties();
    return result;
  }

  public static Properties properties(Object key1, Object value1) {
    Properties result = new Properties();
    result.put(key1, value1);
    return result;
  }

  public static Properties properties(Object key1, Object value1, Object key2,
      Object value2) {
    Properties result = new Properties();
    result.put(key1, value1);
    result.put(key2, value2);
    return result;
  }

  public static Properties properties(Object key1, Object value1, Object key2,
      Object value2, Object key3, Object value3) {
    Properties result = new Properties();
    result.put(key1, value1);
    result.put(key2, value2);
    result.put(key3, value3);
    return result;
  }

  /** Indicates whether two collections intersect. */
  public static boolean intersect(Collection a, Collection b) {
    Collection smallest = a.size() < b.size() ? a : b;
    Collection largest = a.size() < b.size() ? b : a;
    for (Iterator it = smallest.iterator(); it.hasNext();) {
      if (largest.contains(it.next()))
        return true;
    }
    return false;
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

  /**
   * Reads a string from the console, printing a prompt message first, aborting
   * when exceptions are thrown.
   */
  static public String prompt_NE(String message) {
    try {
      BufferedReader console = new BufferedReader(new InputStreamReader(
          System.in));
      if (message != null && message != "")
        System.out.println(message);
      return console.readLine();
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(-1);
    }
    return null;
  }

  /** Prompts for a string with a default message. */
  static public String prompt_NE() {
    return prompt_NE("Enter anything in order to continue...");
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
   * Incrementally calculates component-wise averages, given previously
   * calculated averages (out of n numbers) and a list of new numbers. The
   * average list is filled with the appropriate number of zeros if it is empty.
   * The result is stored in-place, destroying the previous average list.
   */
  static public List incrementalComputationOfComponentWiseAverage(List average,
      int n, List newItems) {
    if (average == null)
      Util.fatalError("Util.incrementalComputationOfComponentWiseAverage must receive a non-null List");

    if (average.size() == 0)
      for (int i = 0; i != newItems.size(); i++)
        average.add(new Double(0));

    for (int i = 0; i != newItems.size(); i++) {
      double currentAverage = ((Double) average.get(i)).doubleValue();
      double newItem = ((Double) newItems.get(i)).doubleValue();
      double newAverage = (currentAverage * n + newItem) / (n + 1);
      average.set(i, new Double(newAverage));
    }

    return average;
  }

  /**
   * Returns the component-wise difference of two lists of numbers.
   */
  static public List componentWiseDifference(List list1, List list2) {
    List result = new ArrayList(list1.size());
    Iterator it1 = list1.iterator();
    Iterator it2 = list2.iterator();
    while (it1.hasNext()) {
      Number n1 = (Number) it1.next();
      Number n2 = (Number) it2.next();
      result.add(new Double(n1.doubleValue() - n2.doubleValue()));
    }
    return result;
  }

  /**
   * A more general version of
   * {@link #incrementalComputationOfComponentWiseAverage(List, int, List)} that
   * operates on lists of lists of arbitrary depth, including depth 0, that is,
   * on {@link Number}s. It is in-place and returns <code>average</code> if
   * given objects are lists, or returns a new Number otherwise.
   */
  public static Object incrementalComponentWiseAverageArbitraryDepth(
      Object average, int n, Object newItems) {
    if (average instanceof Number) {
      return (((Number) average).doubleValue() * n + ((Number) newItems)
          .doubleValue()) / (n + 1);
    }
    ListIterator averageIt = ((List) average).listIterator();
    ListIterator newItemsIt = ((List) newItems).listIterator();
    while (averageIt.hasNext()) {
      Object averageElement = averageIt.next();
      Object newItemsElement = newItemsIt.next();
      Object newAverageElement = incrementalComponentWiseAverageArbitraryDepth(
          averageElement, n, newItemsElement);
      if (newAverageElement != averageElement)
        averageIt.set(newAverageElement);
    }
    return average;
  }

  /**
   * Returns a {@link NullaryFunction} that always returns the same given
   * object.
   */
  static public NullaryFunction constantNullaryFunction(final Object constant) {
    return new NullaryFunction() {
      public Object evaluate() {
        return constant;
      }
    };
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
   * Returns the first object in an array that is an instance of a given class.
   */
  public static Object getObjectOfClass(Class clazz, Object[] args) {
    for (Object object : args) {
      if (clazz.isInstance(object))
        return object;
    }
    return null;
  }

  /**
   * Takes a list of lists, a dimension (0 for rows, 1 for columns) and an index
   * (either row or column index), and returns the corresponding slice
   * (data[index,*] if dimension is 0, or data[*,index] if dimension is 1).
   */
  public static List matrixSlice(List data, int dimension, int index) {
    if (dimension == 0)
      return (List) data.get(index);
    List result = new LinkedList();
    for (Iterator rowIt = data.iterator(); rowIt.hasNext();) {
      List row = (List) rowIt.next();
      result.add(row.get(index));
    }
    return result;
  }

  /**
   * Given a collection, returns a collection of the same type, with the results
   * of the application of a function to each value.
   */
  public static Collection applyToAll(Collection c, UnaryFunction f) {
    Collection results = null;
    try {
      results = (Collection) c.getClass().newInstance();
    } catch (Exception e) {
      fatalError("applyToAll(Collection, UnaryFunction) called on class without default constructor.");
    }
    for (Object value : c) {
      Object result = f.evaluate(value);
      results.add(result);
    }
    return results;
  }

  /**
   * Returns a number's greatest lower bound which is a multiple of a given
   * granularity.
   */
  private static double discretize(double number, final double granularity) {
    return Math.floor(number / granularity) * granularity;
  }

  /**
   * Returns an UnaryFunction that, given an argument (assumed to be a Number),
   * returns its greatest lower bound which is a multiple of a given
   * granularity.
   */
  public static UnaryFunction getDiscretizingFunction(final double granularity) {
    return new UnaryFunction() {
      public Object evaluate(Object arg) {
        double number = ((Number) arg).doubleValue();
        return discretize(number, granularity);
      }
    };
  }

  /**
   * Returns a LinkedList filled with integers from <code>begin</code> to
   * <code>end-1</code>.
   */
  public static List makeIntegerSequenceList(int begin, int end) {
    LinkedList result = new LinkedList();
    for (int i = begin; i != end; i++)
      result.add(i);
    return result;
  }

  /**
   * Given a collection of {@link Number}s and a granularity, returns a
   * collection of the same type, with each value discretized to its greatest
   * lower bound which is a multiple of the granularity.
   */
  public static Collection discretize(Collection numbers, double granularity) {
    return applyToAll(numbers, getDiscretizingFunction(granularity));
  }

  /**
   * Fills an output collection with elements of elements of a given input
   * collection, returning output collection.
   */
  public static Collection addAllElementsOfElements(Collection input,
      Collection output) {
    for (Collection collection : (Collection<Collection>) input) {
      output.addAll(collection);
    }
    return output;
  }

  /**
   * If given object is not a collection, return it; otherwise, return
   * <code>getFirstLeaf(o1)</code>, where <code>o1</code> is the first element
   * of <code>o</code>, or <code>null</code> if collection is empty.
   */
  public static Object getFirstLeaf(Object o) {
    if (o instanceof Collection)
      return getFirstLeaf(getFirstOrNull((Collection) o));
    return o;
  }

  /**
   * Returns whether the type of the first leaf of a given object {@see
   * #getFirstLeaf(Object)} is a double or a float.
   */
  public static boolean baseTypeIsContinuous(Object o) {
    Object leaf = getFirstLeaf(o);
    boolean result = leaf instanceof Double || leaf instanceof Float;
    return result;
  }

  /**
   * Given an array a, returns a {@link HashMapWithGetWithDefault} object
   * mapping each String s in position i of a to the object in position i+1 of
   * array, ignoring its remaining elements.
   */
  public static HashMapWithGetWithDefault getMapWithStringKeys(Object[] args) {
    HashMapWithGetWithDefault map = new HashMapWithGetWithDefault();
    for (int i = 0; i < args.length; i++) {
      Object arg = args[i];
      if (arg instanceof String) {
        String variable = (String) arg;
        Object value = args[++i];
        map.put(variable, value);
      }
    }
    return map;
  }

  /**
   * A simpler substitute for
   * <code>Pattern.compile(pattern).matcher(value)</code>.
   */
  public static Matcher regexMatcher(String pattern, String value) {
    return Pattern.compile(pattern).matcher(value);
  }

  /**
   * Replaces the <code>i</code>-th character in <code>background</code> to
   * <code>mark</code>, where <code>i</code> is
   * <code>floor((position-min)/(max-min)*background.length())</code>, that is,
   * the corresponding index in <code>background</code> corresponding to
   * <code>position</code> in a scale from <code>min</code> to <code>max</code>.
   */
  public static void setMark(double position, double min, double max,
      String mark, StringBuffer background) {
    int index = (int) Math.floor((position - min) / (max - min)
        * background.length());
    background.replace(index, index + 1, mark);
  }

  /**
   * Same as {@link #setMark(Collection, double, double, String, StringBuffer)}
   * but for a collection.
   */
  public static void setMark(Collection positions, double min, double max,
      String mark, StringBuffer background) {
    for (Number number : (Collection<Number>) positions) {
      setMark(number.doubleValue(), min, max, mark, background);
    }
  }

  /**
   * Same as {@link #setMark(Collection, double, String, StringBuffer)} but
   * creating a buffer of space characters of a given size, and returning that
   * buffer.
   */
  public static StringBuffer setMark(Collection positions, double min,
      double max, String mark, int backgroundSize) {
    StringBuffer buffer = timesBuffer(" ", backgroundSize);
    for (Object position : positions) {
      double value = uniDimensionalNumber(position);
      setMark(value, min, max, mark, buffer);
    }
    return buffer;
  }

  /**
   * Same as {@link #setMark(Collection, double, String, StringBuffer)} but
   * returning a String instead.
   */
  public static String textGraph(Collection positions, double min, double max,
      String mark, int backgroundSize) {
    return setMark(positions, min, max, mark, backgroundSize).toString();
  }

  /** Returns the result of concatenating a string n times to the empty string. */
  public static String times(String string, int n) {
    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i != n; i++) {
      buffer.append(string);
    }
    return buffer.toString();
  }

  /**
   * Returns the StringBuffer resulting from concatenating a string n times to
   * the empty string.
   */
  public static StringBuffer timesBuffer(String string, int n) {
    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i != n; i++) {
      buffer.append(string);
    }
    return buffer;
  }

  /**
   * If object is a Number, return its double value; otherwise, it must be a
   * collection, so recursively apply to its first member.
   */
  public static double uniDimensionalNumber(Object object) {
    if (object instanceof Number)
      return ((Number) object).doubleValue();
    return uniDimensionalNumber(Util.getFirst((Collection) object));
  }

  /**
   * Prints string with <code>System.out.println()</code> if it is not empty.
   */
  public static void printlnIfNotEmpty(String string) {
    if (!string.equals(""))
      System.out.println(string);
  }

  public static double incrementalWeightedAverage(double value, double weight,
      double currentAverage, double currentTotalWeight) {
    return (currentAverage * currentTotalWeight + value)
        / (currentTotalWeight + weight);
  }

  private static Random rand;
  private static boolean verbose = false;
  private static boolean print = false;
}
