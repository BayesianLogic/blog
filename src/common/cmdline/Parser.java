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

package common.cmdline;

import java.util.*;
import java.io.*;

/**
 * Parses options on a command line. This class should not be instantiated; all
 * its methods are static. The application should first create options (objects
 * that implement the <code>Option</code> interface); the options will
 * automatically register themselves with the Parser. Then the application
 * should call <code>Parser.parse</code>, passing in the argument array that was
 * given to the application's <code>main</code> method. The <code>parse</code>
 * method uses the <code>Option</code> objects to parse all the options on the
 * command line; if the command line is ill-formed, it prints an error message
 * and exits the program. The resulting option values are stored by the
 * <code>Option</code> objects. Any non-option arguments are returned by the
 * <code>parse</code> method.
 * 
 * <p>
 * This class supports the same command line syntax as the Unix getopt function.
 * If a command line argument begins with a single dash followed by a non-dash
 * character, the parser looks for an option that has that character as a short
 * form. If such an option exists and does not expect a value, then the parser
 * moves on to the next character in the argument, treating it as a short-form
 * option as well. This continues until the parser reaches an option that
 * expects a value. If there are any characters remaining in the argument at
 * this point, they constitute the option value. Otherwise, the next command
 * line argument is interpreted as the option value. Here are some examples of
 * how short-form options are parsed, assuming that options "d" and "P" take
 * values but "c", "e", and "k" don't.
 * 
 * <pre>
 * -c                 // option "c", no value
 * -d 12              // option "d", value "12"
 * -d12               // option "d", value "12"
 * -cek               // options "c", "e", "k", with no values
 * -Pprinter          // option "P", value "printer"
 * -cP printer        // option "c" with no value, "P" with value "printer"
 * -Palpha=12         // option "P" with value "alpha=12"
 * </pre>
 * 
 * <p>
 * If a command line argument begins with two dashes ("--"), then all remaining
 * characters in the argument (up to the first = sign, if any) are interpreted
 * as a long option string. If there is an equals sign in the argument, then all
 * characters after the first equals sign are interpreted as the option value.
 * Otherwise, if the option expects a value, the next command line argument is
 * interpreted as the value. Some examples:
 * 
 * <pre>
 * --copy             // option "copy", no value
 * --ncopies=12       // option "ncopies", value "12"
 * --ncopies 12       // option "ncopies", value "12"
 * --ncopies12        // option "ncopies12", no value
 * </pre>
 */
public class Parser {
	private Parser() {
		// prevent instantiation
	}

	/**
	 * Sets the program description that will be printed by
	 * <code>printUsage</code>.
	 */
	public static void setProgramDesc(String description) {
		Parser.description = description;
	}

	/**
	 * Sets the basic usage message that will be printed by
	 * <code>printUsage</code>. This message should include the non-option
	 * arguments that the program expects.
	 */
	public static void setUsageLine(String usage) {
		Parser.usage = usage;
	}

	/**
	 * Adds the given option to the set that this parser will recognize. The order
	 * in which options are added only influences the order in which they're
	 * listed by the <code>printUsage</code> method.
	 * 
	 * @throws IllegalArgumentException
	 *           if any of the short or long forms of the given option also belong
	 *           to an option that was already added
	 */
	public static void addOption(Option opt) {
		ensureVarsInited();

		options.add(opt);

		for (Iterator iter = opt.getShortForms().iterator(); iter.hasNext();) {
			Character ch = (Character) iter.next();
			if (shortFormTable.containsKey(ch)) {
				throw new IllegalArgumentException("Short form \"" + ch
						+ "\" is already in use.");
			}
			shortFormTable.put(ch, opt);
		}

		for (Iterator iter = opt.getLongForms().iterator(); iter.hasNext();) {
			String str = (String) iter.next();
			if (longFormTable.containsKey(str)) {
				throw new IllegalArgumentException("Long form \"" + str
						+ "\" is already in use.");
			}
			longFormTable.put(str, opt);
		}
	}

	/**
	 * Parses the given array of command line arguments. Option occurrences and
	 * values are recorded by the Option objects. If there is a syntax error in
	 * the command line, this method prints an error message and exits the
	 * program.
	 * 
	 * @return List of Strings representing the non-option command line arguments,
	 *         in the order they were given
	 */
	public static List parse(String[] args) {
		ensureVarsInited();

		List otherArgs = new ArrayList();

		int curPos = 0;
		while (curPos < args.length) {
			if (args[curPos].startsWith("--")) {
				curPos = parseLongOpt(args, curPos);
			} else if (args[curPos].startsWith("-")) {
				curPos = parseShortOpts(args, curPos);
			} else {
				otherArgs.add(args[curPos]);
			}

			++curPos;
		}

		return otherArgs;
	}

	/**
	 * Prints a usage message for the program to the given stream. The message
	 * consists of the program description, then the program usage message, and
	 * then a list of available options. Option documentation strings are obtained
	 * by calling <code>getUsageString</code> on the Option objects.
	 */
	public static void printUsage(PrintStream s) {
		ensureVarsInited();

		if (description != null)
			s.println(description);
		if (usage != null)
			s.println(usage);

		if (!options.isEmpty()) {
			s.println("Optional flags:");
		}
		for (Iterator iter = options.iterator(); iter.hasNext();) {
			Option opt = (Option) iter.next();
			s.println(opt.getUsageString());
		}
	}

	/**
	 * Parses a long option at the given argIndex, and possibly the next argument
	 * as well (if it serves as the option value). Returns the index of the last
	 * argument processed.
	 */
	private static int parseLongOpt(String[] args, int argIndex) {
		int equalsIndex = args[argIndex].indexOf("=");
		int optionEnd = (equalsIndex == -1) ? args[argIndex].length() : equalsIndex;
		String optStr = args[argIndex].substring(2, optionEnd); // skip "--"
		Option opt = (Option) longFormTable.get(optStr);
		if (opt == null) {
			System.err.println("Unrecognized option: --" + optStr);
			printUsage(System.err);
			System.exit(1);
		}

		String value = null;
		if (opt.expectsValue()) {
			if (equalsIndex >= 0) {
				value = args[argIndex].substring(equalsIndex + 1);
			} else if (argIndex + 1 < args.length) {
				++argIndex;
				value = args[argIndex];
			} else {
				System.err
						.println("Missing value for --" + optStr + " option.  Usage:");
				System.err.println(opt.getUsageString());
				System.exit(1);
			}
		} else {
			if (equalsIndex >= 0) {
				System.err.println("Warning: ignoring superfluous " + "value for --"
						+ optStr + " option.  Usage:");
				System.err.println(opt.getUsageString());
			}
		}
		opt.recordOccurrence(optStr, value);

		return argIndex;
	}

	/**
	 * Parses a cluster of short options at the given argIndex. If the following
	 * argument serves as a value for the last short option in the cluster, it is
	 * processed as well. Returns the index of the last argument processed.
	 */
	private static int parseShortOpts(String[] args, int argIndex) {
		String curArg = args[argIndex];
		for (int posInArg = 1; posInArg < curArg.length(); ++posInArg) {
			Character ch = new Character(curArg.charAt(posInArg));
			Option opt = (Option) shortFormTable.get(ch);
			if (opt == null) {
				System.err.println("Unrecognized option: -" + ch);
				if (posInArg > 1) {
					System.err.println("(in option cluster beginning with -"
							+ curArg.charAt(1) + ")");
				}
				printUsage(System.err);
				System.exit(1);
			}

			String value = null;
			if (opt.expectsValue()) {
				if (posInArg + 1 < curArg.length()) {
					value = curArg.substring(posInArg + 1);
					posInArg = curArg.length(); // done with this argument
				} else if (argIndex + 1 < args.length) {
					++argIndex; // value is next argument
					value = args[argIndex];
				} else {
					System.err
							.println("Missing argument for -" + ch + " option.  Usage:");
					System.err.println(opt.getUsageString());
					System.exit(1);
				}
			}
			opt.recordOccurrence(ch.toString(), value);
		}

		return argIndex;
	}

	/**
	 * Test program. This program defines some options, parses the command line
	 * (which, of course, is specified by the user), and then prints the option
	 * values and the non-option arguments.
	 */
	public static void main(String[] args) {
		Parser.setProgramDesc("Test program for command line parser.");
		Parser.setUsageLine("Usage: java common.cmdline.Parser <args>");

		BooleanOption alpha = new BooleanOption("a", "alpha", true,
				"Enable feature alpha");
		BooleanOption beta = new BooleanOption("b", "beta", false,
				"Enable feature beta");
		IntOption ncopies = new IntOption("n", "ncopies", 1, "Print <n> copies");
		DoubleOption scale = new DoubleOption(null, "scale", 1.0,
				"Scale by factor of <x>");
		StringOption header = new StringOption("h", "header", "",
				"Print <s> at top of page");
		PropertiesOption prop = new PropertiesOption("P", null, null,
				"Set printer property");
		List otherArgs = Parser.parse(args);

		System.out.println("alpha: " + alpha.getValue());
		System.out.println("beta: " + beta.getValue());
		System.out.println("ncopies: " + ncopies.getValue());
		System.out.println("scale: " + scale.getValue());
		System.out.println("header: \"" + header.getValue() + "\"");

		System.out.println("Properties:");
		for (Iterator iter = prop.getValue().entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			System.out.println(entry.getKey() + " = " + entry.getValue());
		}
		System.out.println();

		System.out.println("Remaining args: " + otherArgs);
	}

	/**
	 * This method ensures that the static member variables are initialized. This
	 * is necessary because addOption might be called by a static initializer (or
	 * indirectly by a static member variable initializer) of another class. At
	 * that point, this class's static member variables might not be initialized
	 * yet.
	 */
	private static void ensureVarsInited() {
		if (options == null) {
			options = new ArrayList();
			shortFormTable = new HashMap();
			longFormTable = new HashMap();
		}
	}

	private static List options; // of Option
	private static Map shortFormTable; // from Character to Option
	private static Map longFormTable; // from String to Option

	private static String description;
	private static String usage;
}
