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

package blog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import blog.common.Timer;
import blog.common.Util;
import blog.common.cmdline.BooleanOption;
import blog.common.cmdline.IntOption;
import blog.common.cmdline.Option;
import blog.common.cmdline.Parser;
import blog.common.cmdline.PropertiesOption;
import blog.common.cmdline.StringListOption;
import blog.common.cmdline.StringOption;
import blog.engine.InferenceEngine;
import blog.model.Evidence;
import blog.model.Model;
import blog.model.Query;
import blog.msg.ErrorMsg;
import blog.parse.Parse;
import blog.sample.RejectionSampler;
import blog.semant.Semant;

/**
 * Main program for the BLOG (Bayesian Logic) inference engine.
 * 
 * Usage: <blockquote> java blog.Main <i>file1 ... fileN</i> </blockquote>
 * 
 * Each file contains a sequence of BLOG statements. These may be declarations
 * (specifying the model itself), evidence statements, or queries. The
 * convention is to use one file for the model (with the "mblog" suffix), one
 * file for the evidence (with the "eblog" suffix), and one file for the queries
 * (with the "qblog" suffix). The files are loaded in the order specified, so
 * the first file must be a model file.
 * 
 * Optional flags:
 * <dl>
 * <dt>-r, --randomize
 * <dd>Initialize the random seed based on the clock time. If this flag is not
 * given, the program uses a fixed random seed so its behavior is reproducible.
 * Default: false
 * 
 * <dt>-e <i>classname</i>, --engine=<i>classname</i>
 * <dd>Use <i>classname</i> as the inference engine. Default:
 * blog.SamplingEngine
 * 
 * <dt>-n <i>num</i>, --num_samples=<i>num</i>
 * <dd>Run the sampling engine for <i>num</i> samples. Default: 10000 samples.
 * 
 * <dt>-b <i>num</i>, --burn_in=<i>num</i>
 * <dd>Treat first <i>num</i> samples as burn-in period (don't use them to
 * compute query results). Default: 0.
 * 
 * <dt>-s <i>classname</i> --sampler=<i>classname</i>
 * <dd>Use <i>classname</i> to generate each sample. Default: blog.LWSampler (a
 * likelihood weighting sampler)
 * 
 * <dt>-p <i>classname</i>, --proposer=<i>classname</i>
 * <dd>Use <i>classname</i> as the proposal distribution for the
 * Metropolis-Hastings sampler. Default: blog.GenericProposer (samples each var
 * given its parents)
 * 
 * <dt>-t, <i>num</i>, --num_trials=<i>num</i>
 * <dd>Do <i>num</i> independent runs (trials) of the inference algorithm.
 * Default: 1
 * 
 * <dt>--generate
 * <dd>Rather than answering queries, just sample possible worlds from the prior
 * distribution defined by the model, and print them out. Default: false
 * 
 * <dt>-k <i>package</i>, --package <i>package</i>
 * <dd>Look in <i>package</i> (e.g., "blog.distrib") when resolving the names of
 * CondProbDistrib and NonRandomFunction classes in the model file. This option
 * can be included several times with different packages; the packages are
 * searched in the order given. The last places searched are the top-level
 * package ("") and finally the default package blog.distrib. Note that you
 * still need to set the Java classpath so that it includes all these packages.
 * 
 * <dt>-v, --verbose
 * <dd>Print information about the world generated at each iteration. Off by
 * default (for performance reasons, consider leaving this option off).
 * 
 * <dt>-g, --debug
 * <dd>Print model, evidence, and queries for debugging. Default: false
 * 
 * <dt>-w <i>file-prefix</i>, --write=<i>file-prefix</i>
 * <dd>Write sampling results to file specified by this argument. Use with the
 * -i flag.
 * 
 * <dt>-i, --interval
 * <dd>To be used with the -w option, specify the interval at which output is
 * written. Note that if -i and -n are the same, output is written only once, at
 * the last iteration.
 * 
 * <dt>-h <i>file-prefix</i>, --histogram_output=<i>file-prefix</i>
 * <dd>Output the histogram of an ArgSpecQuery to a file. The results are taken
 * after the final sample completes.
 * 
 * <dt>-P <i>key</i>=<i>value</i>
 * <dd>Include the entry <i>key</i>=<i>value</i> in the properties table that is
 * passed to the inference engine. This feature can be used to set configuration
 * parameters for various inference engines (and the components they use, such
 * as samplers). See the individual inference classes for documentation. Note:
 * The -P option cannot be used to specify values for properties for which there
 * exist special-purpose options, such as --engine or --num_samples.
 * 
 * <dt>-x, --extend <i>setup-extender</i>[,<i>key</i>=<i>value</i>,...]
 * <dd>Extend the problem setup (model, evidence and queries) using an object of
 * class <i>setup-extender</i>. This class must implement the SetupExtender
 * interface. The class name can optionally be followed by a series of
 * <i>key</i>=<i>value</i> pairs (each preceded by a comma) that will be passed
 * to the setup extender's constructor in a properties table.
 * 
 * </dl>
 */
public class Main {

	public static void main(String[] args) {
		fromString = false;
		init(args);
		List readersAndOrigins = makeReaders(filenames);
		setup(model, evidence, queries, readersAndOrigins, setupExtenders,
				Util.verbose(), true);
		run();
	}

	public static void runFromString(String modelstring, String[] args) {
		fromString = true;
		if (args == null) {
			args = new String[0];
		}
		init(args);
		stringSetup(modelstring);
		run();
	}

	public static void init(String[] args) {
		ok = true;
		model = new Model();
		evidence = new Evidence();
		queries = new ArrayList<Query>();
		parseOptions(args);
		Util.setVerbose(verbose);
		Util.setPrint(print);
		Util.initRandom(randomize);
		// BLOGParser.setPackagesToSearch(packages);
	}

	public static void run() {
		if (!ok) {
			return;
		}
		System.out.println("............................................");
		if (generate) {
			generateWorlds();
		} else {
			// Run inference for the specified number of trials
			InferenceEngine engine = InferenceEngine.constructEngine(model,
					inferenceProps);
			for (int i = 0; i < numStatSamples; i++) {
				printTimes(System.out, "-", 80);
				System.out.println("Trial " + i + ": ");

				engine.setEvidence(evidence);
				engine.setQueries(queries);
				engine.answerQueries();

				// Print query results
				System.out.println("======== Query Results =========");
				System.out.println("Iteration: " + numSamples);
				for (Iterator iter = queries.iterator(); iter.hasNext();) {
					Query q = (Query) iter.next();
					q.printResults(System.out);
					// leili: why to zero out???
					// q.zeroOut();
				}
				System.out.println("======== Done ========");

				System.out.println();
			}

			if (numStatSamples > 1) {
				printTimes(System.out, "=", 80);
				System.out.println("Summary of statistics for all trials:");
				for (Iterator iter = queries.iterator(); iter.hasNext();) {
					Query q = (Query) iter.next();
					q.printVarianceResults(System.out);
				}
			}
		}

		Timer.printAllTimers();
	}

	public static List makeReaders(Collection filenames) {
		List readersAndOrigins = new LinkedList();
		for (Iterator iter = filenames.iterator(); iter.hasNext();) {
			String filename = (String) iter.next();
			try {
				readersAndOrigins
						.add(new Object[] { new FileReader(filename), filename });
			} catch (FileNotFoundException e) {
				System.err.println("File not found: " + filename);
				Util.fatalError(e);
			}
		}
		return readersAndOrigins;
	}

	private static boolean semanticsCorrect(Model model, Evidence evidence,
			List queries) {
		boolean correct = true;

		if (!model.checkCompleteness()) {
			correct = false;
		}

		return correct;
	}

	private static void generateWorlds() {
		RejectionSampler sampler = new RejectionSampler(model, inferenceProps);
		sampler.initializeCompleteSampling();

		System.out.println("Sampling " + numSamples + " worlds from prior...");
		System.out.println();
		for (int i = 0; i < numSamples; ++i) {
			sampler.nextSample();
			sampler.getLatestWorld().print(System.out);
			System.out.println();
		}
	}

	private static void parseOptions(String[] args) {
		Map specialOptions = new HashMap(); // from String to Option

		blog.common.cmdline.Parser
				.setProgramDesc("Bayesian Logic (BLOG) inference engine");
		blog.common.cmdline.Parser
				.setUsageLine("Usage: runblog <file1> ... <fileN>");

		BooleanOption optRandomize = new BooleanOption("r", "randomize", false,
				"Use clock time as random seed");

		StringOption optEngine = new StringOption("e", "engine",
				"blog.engine.SamplingEngine", "Use inference engine class <s>");
		specialOptions.put("engineClass", optEngine);

		IntOption optNumSamples = new IntOption("n", "num_samples", 50000,
				"Run inference engine for <n> samples");
		specialOptions.put("numSamples", optNumSamples);

		IntOption optQueryReportInterval = new IntOption("q",
				"query_report_interval", 10000, "Report Query values after <n> samples");
		specialOptions.put("queryReportInterval", optQueryReportInterval);

		IntOption optInterval = new IntOption("i", "interval", 500,
				"Write results after every <n> samples");
		specialOptions.put("reportInterval", optInterval);

		IntOption optBurnIn = new IntOption("b", "burn_in", 0,
				"Treat first <n> samples as burn-in");
		specialOptions.put("burnIn", optBurnIn);

		StringOption optSampler = new StringOption("s", "sampler",
				"blog.sample.LWSampler", "Use sampler class <s>");
		specialOptions.put("samplerClass", optSampler);

		StringOption optProposer = new StringOption("p", "proposer",
				"blog.sample.GenericProposer",
				"Use Metropolis-Hastings proposer class <s>");
		specialOptions.put("proposerClass", optProposer);

		IntOption optNumTrials = new IntOption("t", "num_trials", 1,
				"Do <n> independent runs of inference");
		BooleanOption optGenerate = new BooleanOption(null, "generate", false,
				"Sample worlds from prior and print them");
		StringListOption optPackages = new StringListOption("k", "package",
				"Parser looks for classes in package <s>");
		BooleanOption optVerbose = new BooleanOption("v", "verbose", false,
				"Print info about every world sampled");
		BooleanOption optPrint = new BooleanOption("d", "displaycbn", false,
				"Print the CBN of the sampled world");
		BooleanOption optDebug = new BooleanOption("g", "debug", false,
				"Print model, evidence, and queries");
		StringOption optWrite = new StringOption("w", "write", null,
				"Write sampling results to file <s>");
		StringOption optHist = new StringOption("h", "histogram_output", null,
				"Write histogram output to file <s>");
		PropertiesOption optInferenceProps = new PropertiesOption("P", null, null,
				"Set inference configuration properties");

		StringListOption optSetupExtenders = new StringListOption("x", "extend",
				"Extend setup with object of class <s>");

		IntOption optNumMoves = new IntOption("m", "num_moves", 1,
				"Use <m> moves per rejuvenation step (PF only)");

		filenames = blog.common.cmdline.Parser.parse(args);
		if ((!fromString) & filenames.isEmpty()) {
			Util.fatalError("Error: no BLOG input files specified.");
			Parser.printUsage(System.err);
		}

		randomize = optRandomize.getValue();
		numStatSamples = optNumTrials.getValue();
		generate = optGenerate.getValue();
		packages = optPackages.getValue();
		verbose = optVerbose.getValue();
		print = optPrint.getValue();
		debug = optDebug.getValue();

		outputPath = optWrite.getValue();
		if (outputPath != null) {
			// Need to determine output interval
			outputInterval = optInterval.getValue();
			if (outputInterval == 0) {
				// default value is num samples / 100
				outputInterval = Math.max(optNumSamples.getValue() / 100, 1);
			}
		} else if (optInterval.wasPresent()) {
			System.err.println("Warning: ignoring --interval option "
					+ "because no output file specified.");
		}

		histOut = optHist.getValue();

		// Make sure properties that have special-purpose options weren't
		// specified with -P.
		inferenceProps = optInferenceProps.getValue();
		for (Iterator iter = inferenceProps.keySet().iterator(); iter.hasNext();) {
			String property = (String) iter.next();
			Option specialOpt = (Option) specialOptions.get(property);
			if (specialOpt != null) {
				Util.fatalError("Can't use -P to set value for \"" + property
						+ "\".  Use special-purpose " + "option " + specialOpt
						+ " instead.", false);
			}
		}

		// Enter properties from special-purpose options
		inferenceProps.setProperty("engineClass", optEngine.getValue());
		inferenceProps.setProperty("numSamples",
				String.valueOf(optNumSamples.getValue()));
		numSamples = optNumSamples.getValue();
		inferenceProps.setProperty("queryReportInterval",
				String.valueOf(optQueryReportInterval.getValue()));
		queryReportInterval = optQueryReportInterval.getValue();
		inferenceProps.setProperty("reportInterval",
				String.valueOf(optInterval.getValue()));
		reportInterval = optQueryReportInterval.getValue();
		inferenceProps.setProperty("burnIn", String.valueOf(optBurnIn.getValue()));
		inferenceProps.setProperty("samplerClass", optSampler.getValue());
		inferenceProps.setProperty("proposerClass", optProposer.getValue());

		for (Iterator iter = optSetupExtenders.getValue().iterator(); iter
				.hasNext();) {
			addSetupExtender((String) iter.next());
		}
	}

	private static void addSetupExtender(String extenderSpec) {
		int curCommaIndex = extenderSpec.indexOf(',');
		String classname = (curCommaIndex == -1) ? extenderSpec : extenderSpec
				.substring(0, curCommaIndex);

		Properties params = new Properties();
		while (curCommaIndex != -1) {
			int nextCommaIndex = extenderSpec.indexOf(',', curCommaIndex + 1);
			String paramSpec = (nextCommaIndex == -1) ? extenderSpec
					.substring(curCommaIndex + 1) : extenderSpec.substring(
					curCommaIndex + 1, nextCommaIndex);

			int equalsIndex = paramSpec.indexOf('=');
			if (equalsIndex == -1) {
				Util.fatalError("Setup extender parameter \"" + paramSpec
						+ "\" is not of the form key=value.", false);
			}
			params.setProperty(paramSpec.substring(0, equalsIndex),
					paramSpec.substring(equalsIndex + 1));

			curCommaIndex = nextCommaIndex;
		}

		SetupExtender extender = null;
		try {
			Class extenderClass = Class.forName(classname);
			Class[] constrArgTypes = { Properties.class };
			Constructor ct = extenderClass.getConstructor(constrArgTypes);
			Object[] constrArgs = { params };
			extender = (SetupExtender) ct.newInstance(constrArgs);
		} catch (ClassNotFoundException e) {
			Util.fatalError("Setup extender class not found: " + classname, false);
		} catch (NoSuchMethodException e) {
			Util.fatalError("Setup extender class " + classname
					+ " does not have a constructor with a single "
					+ "argument of type java.util.Properties.", false);
		} catch (ClassCastException e) {
			Util.fatalError("Setup extender class " + classname + " does not "
					+ "implement the SetupExtender interface.", false);
		} catch (Exception e) {
			Util.fatalError(e, true);
		}

		setupExtenders.add(extender);
	}

	/**
	 * Print to the PrintStream ps the String s exactly n times, followed by a
	 * newline.
	 */
	public static void printTimes(PrintStream ps, String s, int n) {
		for (int i = 0; i < n; i++) {
			ps.print(s);
		}
		ps.println();
	}

	/**
	 * Returns a PrintStream representing the newly created file, with given
	 * pathname s. Guaranteed not to be null.
	 */
	public static PrintStream filePrintStream(String s) {
		try {
			File f = new File(s);
			if (!f.createNewFile()) {
				System.err.println("Cannot create file (already exists): "
						+ f.getPath());
				System.exit(1);
			}
			if (!f.canWrite()) {
				System.err.println("Cannot write to file: " + f.getPath());
				System.exit(1);
			}
			return new PrintStream(new FileOutputStream(f));
		} catch (Exception e) {
			System.err.println("Cannot create/open a file for output: " + s);
			System.err.println(e);
			System.exit(1);
			return null; // for some reason, the compiler needs this.
		}
	}

	/**
	 * A simplified version of
	 * {@link #setupFromFiles(Model, Evidence, List, Collection, Collection, boolean, boolean)}
	 * , which has no setup extends, is not verbose and does prints "read from"
	 * messages.
	 */
	public static void simpleSetupFromFiles(Model model, Evidence evidence,
			List queries, Collection filenames) {
		setupFromFiles(model, evidence, queries, filenames, Util.list(), false,
				true);
	}

	/**
	 * Reads and prepares model, evidence and queries for inference.
	 * 
	 * @param filenames
	 *          A list of Strings with the names of files to be used.
	 * @param setupExtenders
	 *          A collection of {@link SetupExtender}(s) to be run.
	 * @param verbose
	 *          Whether the procedure is verbose.
	 * @param parseFromMessage
	 *          whether to print a message indicating "Parsing from..."
	 */
	public static void setupFromFiles(Model model, Evidence evidence,
			List queries, Collection filenames, Collection setupExtenders,
			boolean verbose, boolean parseFromMessage) {
		List readersAndOrigins = makeReaders(filenames);
		setup(model, evidence, queries, readersAndOrigins, setupExtenders, verbose,
				parseFromMessage);
	}

	/**
	 * Reads and prepares model, evidence and queries for inference.
	 * 
	 * @param readersAndOrigins
	 *          A list of Object[] of size two, containing a
	 *          {@link java.io.Reader} with text to be parsed and origin name
	 *          (such as file name).
	 * 
	 * @param setupExtenders
	 *          A collection of {@link SetupExtender}(s) to be run.
	 * 
	 * @param verbose
	 *          Whether the procedure is verbose.
	 * 
	 * @param parseFromMessage
	 *          Whether to print a message indicating "Parsing from..."
	 */
	public static void setup(Model model, Evidence evidence, List queries,
			Collection readersAndOrigins, Collection setupExtenders, boolean verbose,
			boolean parseFromMessage) {
		// Parse input readers
		for (Iterator iter = readersAndOrigins.iterator(); iter.hasNext();) {
			Object[] readerAndOrigin = (Object[]) iter.next();
			Reader reader = (Reader) readerAndOrigin[0];
			String origin = (String) readerAndOrigin[1];
			try {
				if (!parseAndTranslate(model, evidence, queries, reader, origin)) {
					ok = false;
					System.err.println();
					Util.fatalErrorWithoutStack("File interpretation halted "
							+ "due to error(s) in \"" + origin + "\".");
				}
			} catch (Exception e) {
				ok = false;
				System.err.println("Error parsing file: " + origin);
				Util.fatalError(e);
			}
		}

		// Run setup extenders
		for (Iterator iter = setupExtenders.iterator(); iter.hasNext();) {
			SetupExtender extender = (SetupExtender) iter.next();
			try {
				extender.extendSetup(model, evidence, queries);
			} catch (Exception e) {
				ok = false;
				System.err.println("Error running setup extender: "
						+ extender.getClass().getName());
				Util.fatalError(e);
			}
		}

		if (debug || verbose) {
			// Print model for debugging
			System.out.println("............................................");
			model.print(System.out);
			System.out.println("............................................");

			// Print evidence for debugging
			System.out.println("\nEvidence:");
			evidence.print(System.out);
			System.out.println("............................................");

			// Print queries for debugging
			System.out.println("\nQueries:");
			for (Iterator iter = queries.iterator(); iter.hasNext();) {
				System.out.println(iter.next());
			}
		}

		// Run semantic checks on model, evidence and queries
		if (!semanticsCorrect(model, evidence, queries)) {
			System.err.println("The model failed one or more checks.");
			System.err.println("Quitting...");
			System.exit(1);
		}

		// Do compilation pass
		int errors = model.compile();
		errors += evidence.compile();
		for (Iterator iter = queries.iterator(); iter.hasNext();) {
			errors += ((Query) iter.next()).compile();
		}
		if (errors > 0) {
			System.err.println("Encountered " + errors
					+ " errors in compilation phase.");
			System.exit(1);
		}
	}

	private static boolean parseAndTranslate(Model m, Evidence e, List<Query> qs,
			Reader reader, String origin) {
		ErrorMsg msg = new ErrorMsg(origin);
		Parse parse = new Parse(reader, msg);
		Semant sem = new Semant(m, e, qs, msg);
		if (msg.OK())
			sem.transProg(parse.getParseResult());
		return msg.OK();
	}

	/**
	 * A version of
	 * {@link #setup(Model, Evidence, List, Collection, Collection, boolean, boolean)}
	 * receiving a single string, no setup extenders, and not verbose.
	 */
	public static void stringSetup(Model model, Evidence evidence, List queries,
			String modelString) {
		Reader reader = new StringReader(modelString);
		String origin = Util.abbreviation(modelString);
		List readersAndOrigins = new LinkedList();
		readersAndOrigins.add(new Object[] { reader, origin });
		setup(model, evidence, queries, readersAndOrigins, new LinkedList(),
				false /* verbose */, false);
	}

	/**
	 * 
	 * @param modelString
	 */
	public static void stringSetup(String modelString) {
		stringSetup(model, evidence, queries, modelString);
	}

	public static String outputPath() {
		return outputPath;
	}

	public static int outputInterval() {
		return outputInterval;
	}

	public static int numSamples() {
		return numSamples;
	}

	public static int numTrials() {
		return numStatSamples;
	}

	public static String histOut() {
		return histOut;
	}

	public static List<Query> getQueries() {
		return queries;
	}

	private static boolean ok = true;
	private static List<String> filenames; // of String
	private static Properties inferenceProps;
	private static boolean randomize = false;
	private static int numSamples;
	private static int queryReportInterval;
	private static int reportInterval;
	private static int numStatSamples;
	private static Model model;
	private static Evidence evidence;
	private static List<Query> queries;
	private static boolean generate;
	private static List<String> packages = new LinkedList<String>(); // of String
	private static boolean verbose;
	private static boolean print;
	private static boolean debug;
	private static boolean fromString;
	private static String outputPath;
	private static int outputInterval;
	private static String histOut;
	private static List setupExtenders = new ArrayList(); // of SetupExtender
}
