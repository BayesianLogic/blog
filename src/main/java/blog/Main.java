/*
 * Copyright (c) 2005, 2006, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the University of California, Berkeley nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior
 * written permission.
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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
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
import blog.io.ResultWriter;
import blog.model.Evidence;
import blog.model.Model;
import blog.model.Queries;
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
 * blog.engine.SamplingEngine
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
 * <dt>--generate
 * <dd>Rather than answering queries, just sample possible worlds from the prior
 * distribution defined by the model, and print them out. Default: false
 * 
 * <dt>--package=<i>package</i>
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
 * <dt>--debug
 * <dd>Print model, evidence, and queries for debugging. Default: false
 * 
 * <dt>-o <i>file</i>, --output=<i>file</i>
 * <dd>Output query results in JSON format to this file.
 * 
 * <dt>--writer <i>writer class</i>
 * <dd>Using the writer class to print query result (default is
 * blog.io.TableWriter)
 * 
 * <dt>--interval=<i>num</i>
 * <dd>Report query results to stdout every num queries.
 * 
 * <dt>-P <i>key</i>=<i>value</i>
 * <dd>Include the entry <i>key</i>=<i>value</i> in the properties table that is
 * passed to the inference engine. This feature can be used to set configuration
 * parameters for various inference engines (and the components they use, such
 * as samplers). See the individual inference classes for documentation. Note:
 * The -P option cannot be used to specify values for properties for which there
 * exist special-purpose options, such as --engine or --num_samples.
 * 
 * </dl>
 */
public class Main {

  public static void main(String[] args) {
    fromString = false;
    init(args);
    List<Object[]> readersAndOrigins = makeReaders(filenames);
    if (setup(model, evidence, queries, readersAndOrigins, Util.verbose(), true)) {
      run();
    } else {
      System.exit(1);
    }
  }

  public static void runFromString(String modelstring, String[] args) {
    fromString = true;
    if (args == null) {
      args = new String[0];
    }
    init(args);
    if (stringSetup(modelstring))
      run();
  }

  public static void init(String[] args) {
    ok = true;
    model = new Model();
    evidence = new Evidence(model);
    queries = new Queries(model);
    parseOptions(args);
    Util.setVerbose(verbose);
    Util.setPrint(print);
    Util.initRandom(randomize);
  }

  public static void run() {
    if (!ok) {
      return;
    }
    System.out.println("............................................");
    if (generate) {
      generateWorlds();
    } else {
      // Run inference.
      Timer timer = new Timer();
      timer.start();
      InferenceEngine engine = InferenceEngine.constructEngine(model,
          inferenceProps);
      engine.setEvidence(evidence);
      engine.setQueries(queries);

      ResultWriter writer = null;
      try {
        Class<ResultWriter> writerClass = (Class<ResultWriter>) Class
            .forName(writerName);
        Constructor<ResultWriter> constructor = writerClass.getConstructor();
        writer = constructor.newInstance();
      } catch (Exception ex) {
        ex.printStackTrace();
        Util.fatalError("cannot find Query Writer class");
      }
      writer.setHeader("======== Query Results =========\n"
          + "Number of samples: " + numSamples);
      if (outputPath != null) {
        writer.setOutput(outputPath);
      } else {
        writer.setOutput(System.out);
      }
      engine.setResultWriter(writer);
      engine.answerQueries();
      timer.stop();
      System.out.print("Total elapsed time: ");
      System.out.print(timer.elapsedTime());
      System.out.println("s");
      System.out.println("======== Done ========");
    }

  }

  public static List<Object[]> makeReaders(Collection<String> filenames) {
    List<Object[]> readersAndOrigins = new LinkedList<Object[]>();
    for (String filename : filenames) {
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
      List<Query> queries) {
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
    Map<String, Option> specialOptions = new HashMap<String, Option>();

    blog.common.cmdline.Parser
        .setProgramDesc("Bayesian Logic (BLOG) inference engine");
    blog.common.cmdline.Parser
        .setUsageLine("Usage: runblog <file1> ... <fileN>");

    BooleanOption optRandomize = new BooleanOption("r", "randomize", false,
        "Use clock time as random seed");

    StringOption optEngine = new StringOption("e", "engine",
        "blog.engine.SamplingEngine", "Use inference engine class <s>");
    specialOptions.put("engineClass", optEngine);

    IntOption optNumSamples = new IntOption("n", "num_samples", 10000,
        "Run inference engine for <n> samples");
    specialOptions.put("numSamples", optNumSamples);

    IntOption optQueryReportInterval = new IntOption("q",
        "query_report_interval", 10000, "Report Query values after <n> samples");
    specialOptions.put("queryReportInterval", optQueryReportInterval);

    IntOption optInterval = new IntOption(null, "interval", 1000,
        "Report progress after every <n> samples");
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

    BooleanOption optGenerate = new BooleanOption(null, "generate", false,
        "Sample worlds from prior and print them");
    IntOption optTimestepBound = new IntOption(null, "max_timestep", 10,
        "If model is dynamic, generate up to <n> timesteps");
    StringListOption optPackages = new StringListOption(null, "package",
        "Parser looks for classes in package <s>");
    BooleanOption optVerbose = new BooleanOption("v", "verbose", false,
        "Print info about every world sampled");
    BooleanOption optPrint = new BooleanOption(null, "displaycbn", false,
        "Print the CBN of the sampled world");
    BooleanOption optDebug = new BooleanOption(null, "debug", false,
        "Print model, evidence, and queries");
    StringOption writerClsName = new StringOption(null, "writer",
        "blog.io.TableWriter", "Writer class for queries");
    StringOption optOutput = new StringOption("o", "output", null,
        "Output query results to file");
    PropertiesOption optInferenceProps = new PropertiesOption("P", null, null,
        "Set inference configuration properties");

    // IntOption optNumMoves = new IntOption("m", "num_moves", 1,
    // "Use <m> moves per rejuvenation step (PF only)"); // this should be used
    // // through -P

    filenames = blog.common.cmdline.Parser.parse(args);
    if ((!fromString) & filenames.isEmpty()) {
      Util.fatalError("Error: no BLOG input files specified.");
      Parser.printUsage(System.err);
    }

    randomize = optRandomize.getValue();
    generate = optGenerate.getValue();
    packages = optPackages.getValue();
    verbose = optVerbose.getValue();
    print = optPrint.getValue();
    debug = optDebug.getValue();
    outputPath = optOutput.getValue();
    writerName = writerClsName.getValue();

    // Make sure properties that have special-purpose options weren't
    // specified with -P.
    inferenceProps = optInferenceProps.getValue();
    for (Iterator<Object> iter = inferenceProps.keySet().iterator(); iter
        .hasNext();) {
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
    inferenceProps.setProperty("reportInterval",
        String.valueOf(optInterval.getValue()));
    inferenceProps.setProperty("burnIn", String.valueOf(optBurnIn.getValue()));
    inferenceProps.setProperty("samplerClass", optSampler.getValue());
    inferenceProps.setProperty("proposerClass", optProposer.getValue());
    inferenceProps.setProperty("timestepBound",
        String.valueOf(optTimestepBound.getValue()));
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
   * A simplified version of
   * {@link #setupFromFiles(Model, Evidence, List, Collection, Collection, boolean, boolean)}
   * , which has no setup extends, is not verbose and does prints "read from"
   * messages.
   * 
   * @return
   *         true if the input is valid BLOG program
   *         false if there is error in syntax or semantics
   */
  public static boolean simpleSetupFromFiles(Model model, Evidence evidence,
      Queries queries, Collection<String> filenames) {
    return setupFromFiles(model, evidence, queries, filenames, false, true);
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
   * @return
   *         true if the input is valid BLOG program
   *         false if there is error in syntax or semantics
   */
  public static boolean setupFromFiles(Model model, Evidence evidence,
      Queries queries, Collection<String> filenames, boolean verbose,
      boolean parseFromMessage) {
    List<Object[]> readersAndOrigins = makeReaders(filenames);
    return setup(model, evidence, queries, readersAndOrigins, verbose,
        parseFromMessage);
  }

  /**
   * Reads and prepares model, evidence and queries for inference.
   * 
   * @param model
   *          A blog Model
   * 
   * @param evidence
   *          All evidence
   * @param queries
   *          A list of Query
   * 
   * @param readersAndOrigins
   *          A list of Object[] of size two, containing a
   *          {@link java.io.Reader} with text to be parsed and origin name
   *          (such as file name).
   * 
   * @param verbose
   *          Whether the procedure is verbose.
   * 
   * @param parseFromMessage
   *          Whether to print a message indicating "Parsing from..."
   * @return
   *         true if the input is valid BLOG program
   *         false if there is error in syntax or semantics
   */
  public static boolean setup(Model model, Evidence evidence, Queries queries,
      Collection<Object[]> readersAndOrigins, boolean verbose,
      boolean parseFromMessage) {
    // Parse input readers
    for (Object[] readerAndOrigin : readersAndOrigins) {
      Reader reader = (Reader) readerAndOrigin[0];
      String origin = (String) readerAndOrigin[1];
      try {
        if (!parseAndTranslate(model, evidence, queries, reader, origin)) {
          ok = false;
          return false;
        }
      } catch (Exception e) {
        ok = false;
        System.err.println("Error parsing file: " + origin);
        Util.fatalError(e);
      }
    }

    if (debug || verbose) {
      // Print model for debugging
      System.out.println("............................................");
      System.out.println(model);
      System.out.println("............................................");

      // Print evidence for debugging
      System.out.println("\nEvidence:");
      System.out.println(evidence);
      System.out.println("............................................");

      // Print queries for debugging
      System.out.println("\nQueries:");
      System.out.println(queries);
    }

    // Run semantic checks on model, evidence and queries
    if (!semanticsCorrect(model, evidence, queries)) {
      System.err.println("The model failed one or more checks.");
      return false;
    }

    // Do compilation pass
    int errors = model.compile();
    errors += evidence.compile();
    for (Query q : queries) {
      errors += q.compile();
    }
    if (errors > 0) {
      System.err.println("Encountered " + errors
          + " errors in compilation phase.");
      return false;
    } else
      return true;
  }

  private static boolean parseAndTranslate(Model m, Evidence e, Queries qs,
      Reader reader, String origin) {
    ErrorMsg msg = new ErrorMsg(origin);
    Parse parse = new Parse(reader, msg, origin);
    Semant sem = new Semant(m, e, qs, msg);
    sem.addPackages(packages);
    if (msg.OK())
      sem.transProg(parse.getResult());
    return msg.OK();
  }

  /**
   * A version of
   * {@link #setup(Model, Evidence, List, Collection, Collection, boolean, boolean)}
   * receiving a single string, no setup extenders, and not verbose.
   * 
   * @return
   *         true if the input is valid BLOG program
   *         false if there is error in syntax or semantics
   */
  public static boolean stringSetup(Model model, Evidence evidence,
      Queries queries, String modelString) {
    Reader reader = new StringReader(modelString);
    String origin = Util.abbreviation(modelString);
    List<Object[]> readersAndOrigins = new LinkedList<Object[]>();
    readersAndOrigins.add(new Object[] { reader, origin });
    return setup(model, evidence, queries, readersAndOrigins,
        false /* verbose */, false);
  }

  /**
   * 
   * @param modelString
   * @return
   *         true if the input is valid BLOG program
   *         false if there is error in syntax or semantics
   */
  public static boolean stringSetup(String modelString) {
    return stringSetup(model, evidence, queries, modelString);
  }

  public static int numSamples() {
    return numSamples;
  }

  public static Queries getQueries() {
    return queries;
  }

  private static boolean ok = true;
  private static List<String> filenames; // of String
  private static Properties inferenceProps;
  private static boolean randomize = false;
  private static int numSamples;
  private static Model model;
  private static Evidence evidence;
  private static Queries queries;
  private static boolean generate;
  private static List<String> packages = new LinkedList<String>();
  private static String writerName;
  private static boolean verbose;
  private static boolean print;
  private static boolean debug;
  private static boolean fromString;
  private static String outputPath;
}
