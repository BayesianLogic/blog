/*
 * Copyright (c) 2005, 2006, Regents of the University of California
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

package blog.old_distrib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import blog.common.Util;

/**
 * Represents a probability model for output strings Y given input strings X. We
 * assume X is transformed into Y by a sequence of edit operations: insertions,
 * deletions, and substitutions. The StringEditModel class implements a very
 * simple model for the probabilities of these edit operations, but a subclass
 * can refine the model by overriding some methods. For computing P(Y | X), we
 * use the dynamic programming algorithm of Bahl, L. R., and Jelinek, F. (1975)
 * "Decoding for channels with insertions, deletions, and substitutions with
 * applications to speech recognition". IEEE Trans. Inform. Theory
 * IT-21(4):404-411. Our model is basically the one they show in Fig. 3,
 * although we also allow insertions after the last input character has been
 * written out.
 * 
 * <p>
 * One way to think about our model is as follows: a scribe is copying the input
 * string X, moving his finger along the string as he does so. At any given
 * time, his finger is either before some character in X, or after the last
 * character of X. He does one of four things at each time step:
 * <ul>
 * <li>Append a character to the output and move his finger to the next
 * position. If he chooses the character to write by just copying the character
 * he moved his finger over, this is called a <em>copy</em>. He may also choose
 * the character to write according to some distribution, thus making a
 * <em>substitution</em>.
 * <li>Move his finger to the next position without appending a character to the
 * output. This is called a <em>deletion</em>.
 * <li>Append a character to the output without moving his finger. This is
 * called an <em>insertion</em>.
 * <li>Stop writing. This is only possible when his finger is after the last
 * character in X (if he doesn't stop at this point, his only other option is an
 * insertion -- he can't move his finger any farther).
 * </ul>
 * 
 * The flag IGNORE_CASE, set to true by default, means the input and output
 * strings are converted to lower case before probabilities are computed.
 */
public class StringEditModel extends AbstractCondProbDistrib {
  public static final double PROB_INSERT = 0.0025;
  public static final double PROB_DELETE = 0.0025;
  public static final double PROB_SUBST = 0.005;
  public static final double PROB_COPY = 1 - PROB_INSERT - PROB_DELETE
      - PROB_SUBST;

  public static final double PROB_INSERT_AT_END = 0.0025;
  public static final double PROB_STOP = 1 - PROB_INSERT_AT_END;

  public static final boolean IGNORE_CASE = true;

  // Parameters describing the distribution over characters that are
  // written in an insertion or substitution
  public static final String LETTERS = "abcdefghijklmnopqrstuvwxyz";
  public static final double LETTER_PROB = 0.8;
  public static final double SPACE_PROB = 0.1;
  public static final String DIGITS = "0123456789";
  public static final double DIGIT_PROB = 0.05;
  public static final String PUNCT = "`~!@#$%^&*()_-+=[{]}\\|;:'\",<.>/?";
  public static final double PUNCT_PROB = 0.05;
  public static final double UNIFORM_WEIGHT = 0.005;

  public StringEditModel() {
    initCharDistrib();
  }

  /**
   * Creates a new StringEditModel. This constructor expects an empty list of
   * configuration parameters.
   */
  public StringEditModel(List params) {
    if (!params.isEmpty()) {
      throw new IllegalArgumentException(
          "StringEditModel expects no parameters.");
    }
    initCharDistrib();
  }

  /**
   * Returns the probability of the output string given the input string. Uses a
   * cache if the same (input, output) pair has been processed before.
   */
  public final double getProb(String input, String output) {
    // Use cached result if available
    List args = new ArrayList();
    args.add(input);
    args.add(output);
    Double cachedProb = (Double) probCache.get(args);
    if (cachedProb != null) {
      return cachedProb.doubleValue();
    }

    double prob = getProbInternal(input, output);
    probCache.put(args, new Double(prob));
    return prob;
  }

  /**
   * Returns the probability of the given output string conditioned on the given
   * input string.
   * 
   * @param args
   *          List with one element, namely the input string
   * @param childVal
   *          output string
   */
  public double getProb(List args, Object childVal) {
    return getProb((String) args.get(0), (String) childVal);
  }

  /**
   * Method that can be overridden by subclasses to compute the probability of
   * the output string given the input string.
   */
  protected double getProbInternal(String input, String output) {
    // p[i][n] = P(at some time, scribe has written i characters and his
    // finger is before character n of the input)
    double[][] p = new double[output.length() + 1][input.length() + 1];

    p[0][0] = 1; // other entries automatically initialized to 0
    for (int n = 0; n <= input.length(); n++) {
      for (int i = 0; i <= output.length(); i++) {
        // Ways of generating i characters of output (indexed 0
        // through i-1) using input characters before position n

        // First way: generate i-1 characters using input before
        // position n-1, then generate output[i-1] using input[n-1]
        if ((n > 0) && (i > 0)) {
          p[i][n] += (p[i - 1][n - 1] * probSubst(input, n - 1, output, i - 1,
              output.charAt(i - 1)));
        }

        // Second way: generate i characters using input before
        // position n-1, then do a deletion, skipping input[n-1]
        if (n > 0) {
          p[i][n] += (p[i][n - 1] * probDelete(input, n - 1, output, i));
        }

        // Third way: generate i-1 characters using input before
        // position n, then generate output[i-1] by insertion
        if (i > 0) {
          p[i][n] += (p[i - 1][n] * probInsert(input, n, output, i - 1,
              output.charAt(i - 1)));
        }
        // System.out.println("p[" + i + "][" + n + "] = " + p[i][n]);
      }
    }

    // To generate the given output string, scribe must write it down and
    // have his finger after the last input character, *and* decide to
    // stop.
    return (p[output.length()][input.length()] * probStop(input,
        input.length(), output, output.length()));
  }

  /**
   * Samples an output string given the string <code>input</code>
   */
  public String sampleVal(String input) {
    int pos = 0;
    int length = input.length();
    StringBuffer output = new StringBuffer(length);

    // input[pos] is the character to the right of the scribe's finger
    while (pos <= length) {

      char nextChar;
      double r = Util.random();

      // If we're not past the last character in the input yet
      if (pos < length) {
        if (r < PROB_INSERT) {
          nextChar = charDistrib.sampleVal_();
          output.append(nextChar);
        } else if (r < PROB_INSERT + PROB_DELETE) {
          pos++;
        } else if (r < PROB_INSERT + PROB_DELETE + PROB_SUBST) {
          nextChar = charDistrib.sampleVal_();
          output.append(nextChar);
          pos++;
        } else {
          output.append(input.charAt(pos));
          pos++;
        }
      }

      // The case when we're past the last input character and
      // are just doing insertions
      else {
        if (r < PROB_INSERT_AT_END) {
          nextChar = charDistrib.sampleVal_();
          output.append(nextChar);
        } else {
          pos++;
        }
      }
    }

    return new String(output);
  }

  public Object sampleVal(List args) {
    return sampleVal((String) args.get(0));
  }

  protected void initCharDistrib() {
    int numEnumChars = (LETTERS.length() + 1 // for space
        + DIGITS.length() + PUNCT.length());
    if (!IGNORE_CASE) {
      numEnumChars += LETTERS.length(); // for uppercase letters
    }

    char[] enumChars = new char[numEnumChars];
    double[] pi = new double[numEnumChars];
    int index = 0;

    double lowercaseProb = IGNORE_CASE ? LETTER_PROB : (LETTER_PROB / 2);
    for (int i = 0; i < LETTERS.length(); i++) {
      enumChars[index] = LETTERS.charAt(i);
      pi[index] = lowercaseProb / LETTERS.length();
      index++;
    }

    if (!IGNORE_CASE) {
      String uppercase = LETTERS.toUpperCase();
      for (int i = 0; i < uppercase.length(); i++) {
        enumChars[index] = uppercase.charAt(i);
        pi[index] = (LETTER_PROB / 2) / uppercase.length();
        index++;
      }
    }

    enumChars[index] = ' ';
    pi[index] = SPACE_PROB;
    index++;

    for (int i = 0; i < DIGITS.length(); i++) {
      enumChars[index] = DIGITS.charAt(i);
      pi[index] = DIGIT_PROB / DIGITS.length();
      index++;
    }

    for (int i = 0; i < PUNCT.length(); i++) {
      enumChars[index] = PUNCT.charAt(i);
      pi[index] = PUNCT_PROB / PUNCT.length();
      index++;
    }

    charDistrib = new CharDistrib(enumChars, pi, UNIFORM_WEIGHT);
  }

  /**
   * Returns the probability that the scribe writes character
   * <code>outChar</code> and moves his finger to the next position, given that:
   * <ul>
   * <li>the scribe's finger is before character <code>n</code> in
   * <code>input</code>;
   * <li>so far, he's written the substring of <code>output</code> up to (not
   * including) index <code>i</code>.
   * </ul>
   * Note that this probability cannot depend on moves the scribe hasn't made
   * yet, i.e., the elements of <code>output</code> from position <code>i</code>
   * onwards.
   */
  protected double probSubst(String input, int n, String output, int i,
      char outChar) {
    if (n == input.length()) {
      return 0;
    }
    char inChar = input.charAt(n);
    if (IGNORE_CASE) {
      inChar = Character.toLowerCase(inChar);
      outChar = Character.toLowerCase(outChar);
    }

    double prob = PROB_SUBST * charDistrib.getProb(outChar);
    if (inChar == outChar) {
      // We could have written output by substituting or copying
      prob += PROB_COPY;
    }
    return prob;
  }

  /**
   * Returns the probability that the scribe moves his finger to the next
   * position without writing anything, given that:
   * <ul>
   * <li>the scribe's finger is before character <code>n</code> in
   * <code>input</code>;
   * <li>so far, he's written the substring of <code>output</code> up to (not
   * including) index <code>i</code>.
   * </ul>
   * Note that this probability cannot depend on moves the scribe hasn't made
   * yet, i.e., the elements of <code>output</code> from position <code>i</code>
   * onwards.
   */
  protected double probDelete(String input, int n, String output, int i) {
    if (n == input.length()) {
      return 0;
    }
    return PROB_DELETE;
  }

  /**
   * Returns the probability that the scribe writes the character
   * <code>outChar</code> without moving his finger, given that:
   * <ul>
   * <li>the scribe's finger is before character <code>n</code> in
   * <code>input</code>;
   * <li>so far, he's written the substring of <code>output</code> up to (not
   * including) index <code>i</code>.
   * </ul>
   * Note that this probability cannot depend on moves the scribe hasn't made
   * yet, i.e., the elements of <code>output</code> from position <code>i</code>
   * onwards.
   */
  protected double probInsert(String input, int n, String output, int i,
      char outChar) {
    double prob = (n == input.length()) ? PROB_INSERT_AT_END : PROB_INSERT;
    if (IGNORE_CASE) {
      outChar = Character.toLowerCase(outChar);
    }
    return (prob * charDistrib.getProb(outChar));
  }

  /**
   * Returns the probability that the scribe stops writing, given that:
   * <ul>
   * <li>the scribe's finger is before character <code>n</code> in
   * <code>input</code>;
   * <li>so far, he's written the substring of <code>output</code> up to (not
   * including) index <code>i</code>.
   * </ul>
   * Note that this probability cannot depend on moves the scribe hasn't made
   * yet, i.e., the elements of <code>output</code> from position <code>i</code>
   * onwards.
   */
  protected double probStop(String input, int n, String output, int i) {
    if (n < input.length()) {
      return 0;
    }
    return PROB_STOP;
  }

  protected CharDistrib charDistrib;

  private Map probCache = new HashMap(); // from List (of String) to Double

  /**
   * Test program.
   */
  public static void main(String[] args) {
    StringEditModel model = new StringEditModel(Collections.EMPTY_LIST);
    double prob = model.getProb(args[0], args[1]);
    System.out.println(prob);
  }
}
