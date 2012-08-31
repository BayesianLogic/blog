package test.blog;

import static blog.common.Experiment.YSeriesSpec;
import static blog.common.Experiment.experiment;
import static blog.common.RangeOperations.Averaging;
import static blog.common.RangeOperations.Axis;
import static blog.common.RangeOperations.PredicatedAveraging;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;

import blog.BLOGUtil;
import blog.DBLOGUtil;
import blog.TemporalEvidenceGenerator;
import blog.bn.BayesNetVar;
import blog.bn.RandFuncAppVar;
import blog.common.CountedIterator;
import blog.common.DAEFunction;
import blog.common.DependencyAwareEnvironment;
import blog.common.Distance;
import blog.common.EZIterator;
import blog.common.HashMapWithGetWithDefault;
import blog.common.Multiset;
import blog.common.Mutable;
import blog.common.RangeOperations.AbstractRange;
import blog.common.Timer;
import blog.common.UnaryFunction;
import blog.common.UnaryPredicate;
import blog.common.Util;
import blog.engine.InferenceEngine;
import blog.engine.Particle;
import blog.engine.ParticleFilter;
import blog.engine.SamplingEngine;
import blog.model.ArgSpecQuery;
import blog.model.Evidence;
import blog.model.ExplicitSetSpec;
import blog.model.Model;
import blog.model.Term;
import blog.model.ValueEvidenceStatement;
import blog.type.Timestep;
import blog.world.PartialWorld;

public class DBLOGExperiments {

	static boolean randomize = true;

	public static void main(String[] args) {
		Util.initRandom(randomize);

		// testProblem(getAircraft());
		// System.exit(-1);

		if (true)
			experiment(
					"file",
					"false",
					"title",
					"Test for experiments",
					"xlabel",
					"Number of samples",
					"ylabel",
					"Average distributions divergence",
					"problem",
					getUmbrella(),
					"numSamplesForTrueDistribution",
					10,
					Axis("inferenceEngineClassName",
							Util.list("blog.SamplingEngine", "blog.ParticleFilter")),
					Axis("numSamples", 10, 200, 50),
					Averaging("run", 1, 2),
					getDistanceGivenTrueAndEstimatedDistributions,
					YSeriesSpec(
							"inferenceEngineClassName",
							Util.list(Util.list("title 'LW'", "w linespoints"),
									Util.list("title 'DBLOG PF'", "w linespoints"))));

		if (false)
			experiment(
					"title",
					"Comparison between LW and DBLOG PF (for one given piece of evidence)",
					"xlabel",
					"Number of samples",
					"ylabel",
					"Average distributions divergence",
					"problem",
					getUmbrella(),
					"numSamplesForTrueDistribution",
					100000,
					Axis("inferenceEngineClassName",
							Util.list("blog.SamplingEngine", "blog.ParticleFilter")),
					Axis("numSamples", 10, 600, 40),
					Averaging("run", 1, 10),
					getDistanceGivenTrueAndEstimatedDistributions,
					YSeriesSpec(
							"inferenceEngineClassName",
							Util.list(Util.list("title 'LW'", "w linespoints"),
									Util.list("title 'DBLOG PF'", "w linespoints"))));

		if (false) {
			int finalTimestepIndex = 10;
			experiment(
					"title",
					"Comparison between LW and DBLOG PF as time progresses",
					"xlabel",
					"Number of time steps",
					"ylabel",
					"Average distributions divergence",
					"problem",
					getWeatherWithoutAtemporal(),
					"numSamplesForTrueDistribution",
					50000,
					"numSamples",
					1000,
					"finalTimestepIndex",
					finalTimestepIndex,
					Axis("timestepIndex", 1, finalTimestepIndex),
					Axis("inferenceEngineClassName",
							Util.list("blog.SamplingEngine", "blog.ParticleFilter")),
					Averaging("run", 1, 10),
					getDistanceGivenTrueAndEstimatedDistributions,
					YSeriesSpec(
							"inferenceEngineClassName",
							Util.list(Util.list("title 'LW'", "w linespoints"),
									Util.list("title 'DBLOG PF'", "w linespoints"))));
		}

		if (false) {
			int finalTimestepIndex = 5;
			experiment(
					"title",
					"LW and DBLOG PF, time, aircraft",
					"xlabel",
					"Number of time steps",
					"ylabel",
					"Average distributions divergence",
					"problem",
					getAircraft(),
					"numSamplesForTrueDistribution",
					100000,
					"numSamples",
					5000,
					"samplerClassName",
					"blog.LWImportanceSampler",
					"finalTimestepIndex",
					finalTimestepIndex,
					Axis("timestepIndex", 1, finalTimestepIndex),
					Axis("inferenceEngineClassName",
							Util.list("blog.SamplingEngine", "blog.ParticleFilter")),
					Averaging("run", 1, 5),
					getDistanceGivenTrueAndEstimatedDistributions,
					YSeriesSpec(
							"inferenceEngineClassName",
							Util.list(Util.list("title 'LW'", "w linespoints"),
									Util.list("title 'DBLOG PF'", "w linespoints"))));
		}

		if (false) {
			experiment(
					"title",
					"Performance (over random evidence)",
					"xlabel",
					"Number of samples",
					"ylabel",
					"Average distributions divergence",
					"numSamplesForTrueDistribution",
					50000,
					Averaging(new RandomProblemRange("problem", 2 /* num of problems */,
							8 /* finalTimestepIndex */)),
					Axis("numSamples", 10, 500, 100),
					Axis("inferenceEngineClassName",
							Util.list("blog.SamplingEngine", "blog.ParticleFilter")),
					getDistanceGivenTrueAndEstimatedDistributions,
					YSeriesSpec(
							"inferenceEngineClassName",
							Util.list(Util.list("title 'LW'", "w linespoints"),
									Util.list("title 'DBLOG PF'", "w linespoints"))));
		}

		if (false) {
			// this one turned out to be a bit of a hack, at least so far,
			// because timestepIndex is not used to generate the evidence.
			// Instead, a TemporalEvidenceGenerator is.
			// This forces us to hard-code that the generator should be recalculated
			// when 'run' changes
			experiment(
					"title",
					"PF update time",
					"xlabel",
					"Time steps",
					"ylabel",
					"Update time (ms)",
					"problem",
					getAircraftWithNumberOnlyAsEvidence(),
					"numSamples",
					50,
					Averaging("run", 1, 3),
					Axis("timestepIndex", 1, 5),
					computeUpdateTime,
					YSeriesSpec("",
							Util.list(Util.list("title 'LW SEIS'", "w linespoints"))));
		}

		if (false)
			experiment("title",
					"DBLOG PF on aircraft (over random pieces of evidence)", "xlabel",
					"Number of samples", "ylabel", "Average distributions divergence",
					"distance value for estimation failure", 100,
					"new evidence at each run", "true", "problem", getAircraft(),
					"finalTimestepIndex", 5, Axis("numSamples", 10, 2000, 1.8f), /* geometric */
					Averaging("run", 1, 100), computeRadarError,
					YSeriesSpec("", Util.list(Util.list("notitle", "w linespoints"))));

		UnaryPredicate positiveOnly = new UnaryPredicate() {
			public boolean evaluate(Object item) {
				return item instanceof Number && ((Number) item).doubleValue() >= 0;
			}
		};

		if (false)
			experiment("title",
					"DBLOG PF on 3 aircraft (over random pieces of evidence)", "xlabel",
					"Number of samples", "ylabel", "Average distributions divergence",
					"distance value for estimation failure", -1,
					"new evidence at each run", "true", "problem", get3Aircraft(),
					"finalTimestepIndex", 5, Axis("numSamples", 10, 1800, 1.4f), /* geometric */
					PredicatedAveraging("run", positiveOnly, 1, 200), computeRadarError,
					YSeriesSpec("", Util.list(Util.list("notitle", "w linespoints"))));

		if (false)
			experiment("title",
					"DBLOG PF on aircraft (over random pieces of evidence)", "xlabel",
					"Number of samples", "ylabel", "Average distributions divergence",
					"distance value for estimation failure", -1,
					"new evidence at each run", "true", "problem", getAircraft(),
					"finalTimestepIndex", 5, Axis("numSamples", 10, 1800, 1.4f), /* geometric */
					PredicatedAveraging("run", positiveOnly, 1, 50), computeRadarError,
					YSeriesSpec("", Util.list(Util.list("notitle", "w linespoints"))));

		if (false)
			experiment(
					"title",
					"DBLOG PF on greater variance aircraft (over random pieces of evidence)",
					"xlabel", "Number of samples", "ylabel",
					"Average distributions divergence",
					"distance value for estimation failure", -1,
					"new evidence at each run", "true", "problem",
					getAircraftWithGreaterVariance(), "finalTimestepIndex", 5,
					Axis("numSamples", 10, 1800, 1.4f), /* geometric */
					PredicatedAveraging("run", positiveOnly, 1, 70), computeRadarError,
					YSeriesSpec("", Util.list(Util.list("notitle", "w linespoints"))));

		if (false) {
			experiment(
					"title",
					"DBLOG, 3 aircraft, no false alarms (over random pieces of evidence)",
					"xlabel", "Number of samples", "ylabel",
					"Average distributions divergence",
					"distance value for estimation failure", -1,
					"new evidence at each run", "true", "problem",
					get3AircraftNoFalseAlarmsNoDetectionFailure(), "finalTimestepIndex",
					5, Axis("numSamples", 10, 1800, 1.4f), /* geometric */
					PredicatedAveraging("run", positiveOnly, 1, 100), computeRadarError,
					YSeriesSpec("", Util.list(Util.list("notitle", "w linespoints"))));
		}

		if (false) {
			experiment(
					"title",
					"DBLOG, 3 aircraft, no false alarms, no detection failure (one piece of evidence)",
					"xlabel", "Number of samples", "ylabel",
					"Average distributions divergence",
					"distance value for estimation failure", -1,
					"new evidence at each run", "false", "problem",
					get3AircraftNoFalseAlarmsNoDetectionFailure(), "finalTimestepIndex",
					5, Axis("numSamples", 10, 500, 1.8f), /* geometric */
					PredicatedAveraging("run", positiveOnly, 1, 50), computeRadarError,
					YSeriesSpec("", Util.list(Util.list("notitle", "w linespoints"))));
		}

		if (false) {
			experiment(
					"title",
					"DBLOG, 1 aircraft, no false alarms, no detection failure, 1 timestep, random pieces of evidence",
					"xlabel", "Number of samples", "ylabel",
					"Average distributions divergence",
					"distance value for estimation failure", -1,
					"new evidence at each run", "true", "problem factory",
					computeAircraftProblem, "number of aircraft distribution", " = 1",
					"number of false alarms distribution", " = 0", "finalTimestepIndex",
					1, Axis("numSamples", 10, 400, 1.8f), /* geometric */
					PredicatedAveraging("run", positiveOnly, 1, 50), computeRadarError,
					YSeriesSpec("", Util.list(Util.list("notitle", "w linespoints"))));
		}

		if (false) {
			experiment(
					"title",
					"DBLOG, 2 aircraft, no false alarms, no detection failure, 1 timestep, random pieces of evidence",
					"xlabel", "Number of samples", "ylabel",
					"Average distributions divergence",
					"distance value for estimation failure", -1,
					"new evidence at each run", "true", "problem factory",
					computeAircraftProblem, "number of aircraft distribution", " = 2",
					"number of false alarms distribution", " = 0", "finalTimestepIndex",
					1, Axis("numSamples", 10, 400, 1.8f), /* geometric */
					PredicatedAveraging("run", positiveOnly, 1, 50), computeRadarError,
					YSeriesSpec("", Util.list(Util.list("notitle", "w linespoints"))));
		}

		if (false) {
			radarExperiment("problem factory", computeAircraftProblem,
					"new evidence at each run", "true",
					"number of aircraft distribution", "~Poisson[3]()",
					"number of blips per aircraft distribution", " = 1",
					"number of false alarms distribution", "~Poisson[0.5]()",
					"transition variance", "0.01", "noise variance", "0.005",
					"finalTimestepIndex", 3, Axis("numSamples", 10, 400, 1.8f), /* geometric */
					PredicatedAveraging("run", positiveOnly, 1, 3), computeRadarError,
					YSeriesSpec("", Util.list(Util.list("notitle", "w linespoints"))));
		}

		int numRuns = 70;
		String toFile = "false";

		if (false) {
			radarExperiment("file", toFile, "problem factory",
					computeVelocityAircraftProblem, "number of runs", numRuns,
					"new evidence at each run", "false",
					"number of aircraft distribution", "=3",
					"number of blips per aircraft distribution", "=1",
					"number of false alarms distribution", "=0", "transition variance",
					"0.05", "noise variance", "0.01", "finalTimestepIndex", 4,
					Axis("numSamples", 10, 400, 1.8f), /* geometric */
					PredicatedAveraging("run", positiveOnly, 1, numRuns),
					computeRadarError,
					YSeriesSpec("", Util.list(Util.list("notitle", "w linespoints"))));
		}

		if (false) {
			radarExperiment("file", toFile, "problem factory",
					computeVelocityAircraftProblem, "number of runs", numRuns,
					"new evidence at each run", "false",
					"number of aircraft distribution", "=3",
					"number of blips per aircraft distribution", "=1",
					"number of false alarms distribution", "~Poisson[0.5]()",
					"transition variance", "0.05", "noise variance", "0.01",
					"finalTimestepIndex", 4, Axis("numSamples", 10, 400, 1.8f), /* geometric */
					PredicatedAveraging("run", positiveOnly, 1, numRuns),
					computeRadarError,
					YSeriesSpec("", Util.list(Util.list("notitle", "w linespoints"))));
		}

		if (false) {
			radarExperiment("file", toFile, "problem factory",
					computeVelocityAircraftProblem, "number of runs", numRuns,
					"new evidence at each run", "false",
					"number of aircraft distribution", "=3",
					"number of blips per aircraft distribution",
					"~TabularCPD[0.05,0.95]()", "number of false alarms distribution",
					"~Poisson[0.5]()", "transition variance", "0.05", "noise variance",
					"0.01", "finalTimestepIndex", 4, Axis("numSamples", 10, 400, 1.8f), /* geometric */
					PredicatedAveraging("run", positiveOnly, 1, numRuns),
					computeRadarError,
					YSeriesSpec("", Util.list(Util.list("notitle", "w linespoints"))));
		}

		if (false) {
			radarExperiment("file", toFile, "problem factory",
					computeVelocityAircraftProblem, "number of runs", numRuns,
					"new evidence at each run", "false",
					"number of aircraft distribution", "~Poisson[3]()",
					"number of blips per aircraft distribution",
					"~TabularCPD[0.05,0.95]()", "number of false alarms distribution",
					"~Poisson[0.5]()", "transition variance", "0.05", "noise variance",
					"0.01", "finalTimestepIndex", 4, Axis("numSamples", 10, 400, 1.8f), /* geometric */
					PredicatedAveraging("run", positiveOnly, 1, numRuns),
					computeRadarError,
					YSeriesSpec("", Util.list(Util.list("notitle", "w linespoints"))));
		}

		if (false) {
			radarExperiment("file", toFile, "problem factory",
					computeVelocityAircraftProblem, "number of runs", numRuns,
					"new evidence at each run", "true",
					"number of aircraft distribution", "~Poisson[3]()",
					"number of blips per aircraft distribution",
					"~TabularCPD[0.05,0.95]()", "number of false alarms distribution",
					"~Poisson[0.5]()", "transition variance", "0.05", "noise variance",
					"0.01", "finalTimestepIndex", 4, Axis("numSamples", 10, 400, 1.8f), /* geometric */
					PredicatedAveraging("run", positiveOnly, 1, numRuns),
					computeRadarError,
					YSeriesSpec("", Util.list(Util.list("notitle", "w linespoints"))));
		}

		if (false) {
			radarExperiment("file", "false", "problem factory",
					computeVelocityAircraftProblem, "number of runs", numRuns,
					"new evidence at each run", "false",
					"number of aircraft distribution", "~Poisson[3]()",
					"number of blips per aircraft distribution",
					"~TabularCPD[0.05,0.95]()", "number of false alarms distribution",
					"~Poisson[0.5]()", "transition variance", "0.1", "noise variance",
					"0.1", "finalTimestepIndex", 4, Axis("numSamples", 100, 2000, 1.8f), /* geometric */
					PredicatedAveraging("run", positiveOnly, 1, numRuns),
					computeRadarError,
					YSeriesSpec("", Util.list(Util.list("notitle", "w linespoints"))));
		}
	}

	private static void radarExperiment(Object... args) {
		HashMapWithGetWithDefault properties = Util.getMapWithStringKeys(args);
		String title = numberOf("aircraft", "number of aircraft distribution",
				"~Poisson[3]", properties)
				+ ", "
				+ numberOf("false blips", "number of false alarms distribution",
						"~Poisson[0.5]", properties)
				+ ", "
				+ numberOf("blips p aircraft",
						"number of blips per aircraft distribution", " = 1", properties)
				+ ", "
				+ "variances "
				+ properties.getWithDefault("transition variance", "0.01")
				+ " (pos), "
				+ properties.getWithDefault("noise variance", "0.005")
				+ " (false), "
				+ properties.getWithDefault("finalTimestepIndex", "1")
				+ " steps, "
				+ properties.get("number of runs")
				+ " runs, "
				+ (properties.getWithDefault("new evidence at each run", "false")
						.equals("true") ? "random evidence" : "fixed evidence");
		List list = new LinkedList(Arrays.asList(args));
		// System.out.println(title);System.exit(-1);
		list.add("title");
		list.add(title);
		list.add("xlabel");
		list.add("Number of particles");
		list.add("ylabel");
		list.add("Average distributions divergence");
		list.add("distance value for estimation failure");
		list.add(-1);
		experiment(Util.asArray(list));
	}

	private static String numberOf(String noun, String key, String defaultValue,
			HashMapWithGetWithDefault properties) {
		String value = (String) properties.getWithDefault(key, defaultValue);
		Matcher matcher = Util.regexMatcher("\\s*=\\s*(.*)", value);
		if (matcher.matches()) {
			value = matcher.group(1);
		}
		return value + " " + noun;
	}

	private static DAEFunction getDistanceGivenTrueAndEstimatedDistributions = new DAEFunction() {
		public Object evaluate(DependencyAwareEnvironment environment) {
			ArgSpecQuery trueDistribution = (ArgSpecQuery) environment
					.getResultOrRecompute(getTrueDistribution);
			trueDistribution.prune(0.01);
			System.out.println("True distribution of " + trueDistribution);
			trueDistribution.printResults(System.out);

			ArgSpecQuery estimatedDistribution = (ArgSpecQuery) environment
					.getResultOrRecompute(computeEstimatedDistribution);
			estimatedDistribution.prune(0.01);
			System.out.println("Estimated distribution of " + estimatedDistribution);
			estimatedDistribution.printResults(System.out);

			double estimateEvaluation = divergence(trueDistribution,
					estimatedDistribution);
			System.out.println("Distributions divergence: " + estimateEvaluation);
			return estimateEvaluation;
		}
	};

	private static DAEFunction getTrueDistribution = new DAEFunction() {
		public Object evaluate(DependencyAwareEnvironment environment) {
			return computeQuery(environment,
					"inferenceEngineForTrueDistributionClassName",
					"numSamplesForTrueDistribution");
		}
	};

	private static DAEFunction computeEstimatedDistribution = new DAEFunction() {
		public boolean isRandom() {
			return true;
		}

		public Object evaluate(DependencyAwareEnvironment environment) {
			return computeQuery(environment, "inferenceEngineClassName", "numSamples");
		}
	};

	private static DAEFunction getTotalEvidence = new DAEFunction() {
		public Object evaluate(DependencyAwareEnvironment environment) {
			int finalTimestepIndex = environment.getInt("finalTimestepIndex");
			Problem problem = (Problem) environment.getResultOrRecompute(getProblem);
			return TemporalEvidenceGenerator.collectEvidenceUpTo(
					finalTimestepIndex - 1, problem.model, problem.linkStrings);
		}
	};

	private static DAEFunction getEvidenceUpToTimestepIndexMinus1 = new DAEFunction() {
		public Object evaluate(DependencyAwareEnvironment environment) {
			int timestepIndex = environment.getInt("timestepIndex");
			Evidence totalEvidence = (Evidence) environment
					.getResultOrRecompute(getTotalEvidence);
			return DBLOGUtil.getEvidenceUpTo(timestepIndex - 1, totalEvidence);
		}
	};

	private static DAEFunction getQueryTemplateInstantiatedToTimestepIndex = new DAEFunction() {
		public Object evaluate(DependencyAwareEnvironment environment) {
			int timestepIndex = environment.getInt("timestepIndex");
			Problem problem = (Problem) environment.getResultOrRecompute(getProblem);
			return problem.getInstantiatedQuery(timestepIndex);
		}
	};

	private static DAEFunction getRandomProblem = new DAEFunction() {
		public Object evaluate(DependencyAwareEnvironment environment) {
			environment.get("problemRun");
			int finalTimestepIndex = environment.getInt("finalTimestepIndex");
			Problem problem = (Problem) environment.getResultOrRecompute(getProblem);
			return problem.getRandomProblem(finalTimestepIndex);
		}
	};

	private static Object computeQuery(DependencyAwareEnvironment environment,
			String inferenceEngineClassNamePropertyName, String numSamplesPropertyName) {
		String engineClassName = (String) environment.getWithDefault(
				inferenceEngineClassNamePropertyName, "blog.SamplingEngine");
		String samplerClassName = (String) environment.getWithDefault(
				"samplerClassName", "blog.LWImportanceSampler");
		Problem problem = (Problem) environment.getResultOrRecompute(getProblem);
		Integer numSamples = (Integer) environment.get(numSamplesPropertyName);

		Properties properties = new Properties();
		properties.setProperty("reportInterval", "-1");
		properties.setProperty("numSamples", numSamples.toString());
		properties.setProperty("samplerClass", samplerClassName);

		InferenceEngine engine = InferenceEngine.constructEngine(engineClassName,
				problem.model, properties);

		ArgSpecQuery query;
		Evidence evidence;

		if (environment.containsKey("timestepIndex")) {
			evidence = (Evidence) environment
					.getResultOrRecompute(getEvidenceUpToTimestepIndexMinus1);
			query = (ArgSpecQuery) environment
					.getResultOrRecompute(getQueryTemplateInstantiatedToTimestepIndex);
		} else {
			evidence = problem.evidence;
			query = problem.query();
		}

		query = new ArgSpecQuery(query); // the query is also its own result, so we
																			// need to create new queries for storing
																			// new results.

		engine.solve(query, evidence);
		return query;
	}

	public static double klDivergence(ArgSpecQuery query1, ArgSpecQuery query2) {
		Set entries = new HashSet();
		entries.addAll(query1.elementSet());
		entries.addAll(query2.elementSet());

		double sum = 0;
		for (Iterator it = entries.iterator(); it.hasNext();) {
			Object entry = it.next();
			double p1 = query1.getProb(entry);
			double p2 = query2.getProb(entry);
			if ((p1 == 0 && p2 < 0.01) || (p2 == 0 && p1 < 0.01))
				continue;
			double log = Util.log2(p1 / p2);
			double expectedEntropyDifference = p1 * log;
			sum += expectedEntropyDifference;
			sum = sum;
		}
		return sum;
	}

	public static double divergence(ArgSpecQuery query1, ArgSpecQuery query2) {
		if (Util.baseTypeIsContinuous(query1.elementSet())) {
			Distance distance = new Distance();
			distance.noMatchDistance = 100; // TODO: remove this TEMPORARY HACK, or at
																			// least make it permanent ;-)
			UnaryFunction weight1 = getWeightFunctionFor(query1);
			UnaryFunction weight2 = getWeightFunctionFor(query2);
			double d = distance.distance(query1.elementSet(), weight1,
					query2.elementSet(), weight2);
			return d;
		}
		return klDivergence(query1, query2);
	}

	private static UnaryFunction getWeightFunctionFor(final ArgSpecQuery query) {
		return new UnaryFunction() {
			public Object evaluate(Object element) {
				return query.getProb(element);
			}
		};
	}

	// /////////////////////////// METHODS FOR UPDATE TIME EXPERIMENTS
	// ////////////////

	private static DAEFunction getProblem = new DAEFunction() {
		public Object evaluate(DependencyAwareEnvironment environment) {
			DAEFunction factory = (DAEFunction) environment.get("problem factory");
			if (factory != null) {
				return environment.getResultOrRecompute(factory);
			}
			return environment.get("problem");
		}
	};

	private static DAEFunction getModel = new DAEFunction() {
		public Object evaluate(DependencyAwareEnvironment environment) {
			environment.get("run"); // forces a dependence so that model is obtained
															// every time.
			Problem problem = (Problem) environment.getResultOrRecompute(getProblem);
			return new Model(problem.model); // returns a copy in order to prevent
																				// original model from getting symbol
																				// evidence symbols.
		}
	};

	private static DAEFunction getParticleFilter = new DAEFunction() {
		public Object evaluate(DependencyAwareEnvironment environment) {
			String samplerClassName = (String) environment.getWithDefault(
					"samplerClassName", "blog.LWImportanceSampler");
			Model model = (Model) environment.getResultOrRecompute(getModel);
			Integer numSamples = (Integer) environment.get("numSamples");

			Properties properties = new Properties();
			properties.setProperty("reportInterval", "-1");
			properties.setProperty("numSamples", numSamples.toString());
			properties.setProperty("samplerClass", samplerClassName);

			ParticleFilter particleFilter = new ParticleFilter(model, properties);
			return particleFilter;
		}
	};

	private static DAEFunction getTemporalEvidenceGenerator = new DAEFunction() {
		public Object evaluate(DependencyAwareEnvironment environment) {
			Problem problem = (Problem) environment.getResultOrRecompute(getProblem);
			Model model = (Model) environment.getResultOrRecompute(getModel);
			return new TemporalEvidenceGenerator(model, problem.linkStrings,
					Util.list());
		}
	};

	private static DAEFunction getNextEvidence = new DAEFunction() {
		public boolean isRandom() {
			return true;
		}

		public Object evaluate(DependencyAwareEnvironment environment) {
			TemporalEvidenceGenerator generator = (TemporalEvidenceGenerator) environment
					.getResultOrRecompute(getTemporalEvidenceGenerator);
			generator.moveOn();
			return generator.getEvidence();
		}
	};

	private static DAEFunction computeUpdateTime = new DAEFunction() {
		public boolean isRandom() {
			return true;
		}

		public Object evaluate(DependencyAwareEnvironment environment) {
			ParticleFilter particleFilter = (ParticleFilter) environment
					.getResultOrRecompute(getParticleFilter);
			Evidence evidence = (Evidence) environment
					.getResultOrRecompute(getNextEvidence);

			System.out.println("Taking evidence " + evidence);
			Timer timer = new Timer();
			timer.start();
			particleFilter.take(evidence);
			timer.stop();

			return timer.elapsedTime() * 1000;
		}
	};

	// /////////////////////////// END OF METHODS FOR UPDATE TIME EXPERIMENTS
	// ////////////////

	// /////////////////////////// METHODS FOR AIRCRAFT EXPERIMENTS
	// ///////////////////

	/**
	 * A function computing the error between a set of estimated aircraft
	 * positions and a set of true positions.
	 */
	private static DAEFunction computeRadarError = new DAEFunction() {
		public boolean isRandom() {
			return true;
		}

		public Object evaluate(DependencyAwareEnvironment environment) {
			Multiset trueAircraftPositions = (Multiset) environment
					.getResultOrRecompute(getTrueAircraftPositions);
			ArgSpecQuery estimatedAircraftPositions = (ArgSpecQuery) environment
					.getResultOrRecompute(getEstimatedAircraftPositions);

			double distance;
			Distance distanceComputation = new Distance();
			distanceComputation.noMatchDistance = 100;
			if (estimatedAircraftPositions == null) {
				distance = ((Number) environment
						.get("distance value for estimation failure")).doubleValue();
				System.out.println("Couldn't estimate. Distance: " + distance);
			} else {
				UnaryFunction estimatesWeightFunction = getWeightFunctionFor(estimatedAircraftPositions);
				distance = distanceComputation.distance(trueAircraftPositions,
						estimatedAircraftPositions.elementSet(), estimatesWeightFunction);
				System.out.println("Calculated distance for");
				estimatedAircraftPositions.printResults(System.out);
				System.out.println("\nand true positions");
				System.out.println(trueAircraftPositions);
				System.out.println("Distance: " + distance);
				if (Double.isNaN(distance))
					System.exit(-1);

				String trueTextGraph = aircraftTextGraph(trueAircraftPositions, "X");
				System.out.println(trueTextGraph);

				Object[] evidenceAndTruth = (Object[]) environment
						.getResultOrRecompute(getEvidenceAndTruth);
				Evidence evidence = (Evidence) evidenceAndTruth[0];
				System.out.println(aircraftEvidenceRepresentation(evidence));

				System.out.println(aircraftWorldQueryResultsRepresentation(
						estimatedAircraftPositions, 10, 0.8));
			}

			return distance;
		}
	};

	private static String aircraftTextGraph(Collection trueAircraftPositions,
			String mark) {
		return Util.setMark(trueAircraftPositions, -5, 15, mark, 100).toString();
	}

	private static String aircraftEvidenceRepresentation(Evidence evidence) {
		StringBuffer result = new StringBuffer();
		Collection evidenceDoublesInTimeSequence = new LinkedList();
		for (ValueEvidenceStatement statement : (Collection<ValueEvidenceStatement>) evidence
				.getValueEvidence()) {
			ExplicitSetSpec evidencePositions = (ExplicitSetSpec) statement
					.getOutput();
			Collection evidenceDoubles = getBlipPositionsOutOfValueEvidenceStatement(evidencePositions);
			evidenceDoublesInTimeSequence.add(evidenceDoubles);
			String evidenceTextGraph = aircraftTextGraph(evidenceDoubles, "B");
			result.append(evidenceTextGraph + "\n");
		}
		return result.toString();
	}

	private static String aircraftWorldRepresentation(PartialWorld world,
			Model model) {
		StringBuffer result = new StringBuffer();
		// System.out.println("WorldRepresentation: world: " +
		// Util.abbreviation(world.toString(), 100));
		for (Iterator timestepIndexIt = DBLOGUtil.getTimestepIndicesIterator(world); timestepIndexIt
				.hasNext();) {
			int t = ((Integer) timestepIndexIt.next()).intValue();
			Collection positions = getPositionsAtTimestep(world, t);
			if (!positions.isEmpty()) {
				String timestepRepresentation = aircraftTextGraph(positions, "X");
				result.append(timestepRepresentation + "\n");
			}
		}
		return result.toString();
	}

	private static Collection getPositionsAtTimestep(PartialWorld world, int t) {
		Collection result = new LinkedList();
		for (BayesNetVar var : (Collection<BayesNetVar>) world
				.getInstantiatedVars()) {
			if (var instanceof RandFuncAppVar) {
				RandFuncAppVar rfav = (RandFuncAppVar) var;
				String functionName = rfav.func().getName();
				if (functionName.equals("Position")) {
					int timestepIndex = ((Timestep) rfav.args()[1]).intValue();
					if (timestepIndex == t) {
						result.add(world.getValue(var));
					}
				}
			}
		}
		return result;
	}

	private static String aircraftWorldQueryResultsRepresentation(
			ArgSpecQuery query, int maxNumEstimates, double proportion) {
		StringBuffer result = new StringBuffer();
		Collection bestEstimates = query.getNBestButInUpper(maxNumEstimates,
				proportion);
		for (Collection estimate : (Collection<Collection>) bestEstimates) {
			String estimateTextGraph = aircraftTextGraph(estimate, "O");
			result.append(estimateTextGraph + "\n");
		}
		return result.toString();
	}

	private static DAEFunction getEvidenceAndTruth = new DAEFunction() {
		public Object evaluate(DependencyAwareEnvironment environment) {
			if (environment.containsKey("new evidence at each run")
					&& environment.get("new evidence at each run").equals("true"))
				environment.get("run");

			int finalTimestepIndex = environment.getInt("finalTimestepIndex");
			Problem problem = (Problem) environment.getResultOrRecompute(getProblem);

			TemporalEvidenceGenerator generator = new TemporalEvidenceGenerator(
					problem.model, problem.linkStrings, Util.list());
			Evidence evidence = generator.collectEvidenceUpTo(finalTimestepIndex - 1);

			String aircraftPositionsString = DBLOGUtil.replaceTByTimeStep(
					"{Position(a,t) for Aircraft a}", finalTimestepIndex);
			BayesNetVar aircraftPositionsVar = BLOGUtil.parseVariable_NE(
					aircraftPositionsString, generator.getModel());
			BLOGUtil.ensureDetAndSupported(aircraftPositionsVar,
					generator.currentPartialWorld);
			Multiset aircraftPositions = (Multiset) aircraftPositionsVar
					.getValue(generator.currentPartialWorld);

			System.out.println("Calculated evidence and true positions:");
			System.out.println(evidence);
			System.out.println(aircraftPositions);

			return new Object[] { evidence, aircraftPositions };
		}
	};

	/**
	 * A function computing an estimate for aircraft positions.
	 */
	private static DAEFunction getEstimatedAircraftPositions = new DAEFunction() {
		public boolean isRandom() {
			return true;
		}

		public Object evaluate(DependencyAwareEnvironment environment) {
			ParticleFilter particleFilter = (ParticleFilter) environment
					.getResultOrRecompute(getParticleFilter);
			Object[] evidenceAndTruth = (Object[]) environment
					.getResultOrRecompute(getEvidenceAndTruth);
			Evidence evidence = (Evidence) evidenceAndTruth[0];

			final Model model = (Model) environment.getResultOrRecompute(getModel);
			int finalTimestepIndex = environment.getInt("finalTimestepIndex");
			String queryString = DBLOGUtil.replaceTByTimeStep(
					"query {Position(a,t) for Aircraft a};", finalTimestepIndex);
			ArgSpecQuery query = BLOGUtil.parseQuery_NE(queryString, model);

			final Mutable preSamplingWorldRepresentation = new Mutable();

			final Mutable samplingTrace = new Mutable();

			particleFilter.beforeParticleTakesEvidence = new ParticleFilter.ParticleTakesEvidenceHandler() {
				public void evaluate(Particle particle, Evidence evidence,
						ParticleFilter particleFilter) {
					preSamplingWorldRepresentation.object = aircraftWorldRepresentation(
							particle.curWorld, model);
					samplingTrace.object = "";
				}
			};

			// InstantiatingEvalContext.staticAfterSamplingListener = new
			// AfterSamplingListener() { public void evaluate(BasicVar var, Object
			// newValue, double prob) {
			// if (prob < 1)
			// samplingTrace.object = ((String)samplingTrace.object) +
			// var + " <-- " + newValue + " with probability " + prob + "\n";
			// }};

			// particleFilter.afterParticleTakesEvidence = new
			// ParticleFilter.ParticleTakesEvidenceHandler() { public void
			// evaluate(Particle particle, Evidence evidence, ParticleFilter
			// particleFilter) {
			// if (particle.getLatestWeight() > 0) {
			// System.out.println(Util.times("----------", 10));
			// System.out.println("Evidence slice: " + evidence);
			// System.out.print((String)preSamplingWorldRepresentation.object);
			// System.out.print(aircraftEvidenceRepresentation(evidence));
			// System.out.print(samplingTrace.object);
			// System.out.print(aircraftWorldRepresentation(particle.curWorld,
			// model));
			// System.out.println("Weight: " + particle.getLatestWeight());
			// }
			// }};

			try {
				particleFilter.solve(query, evidence);
			} catch (IllegalArgumentException e) {
				if (e.getMessage().contains("All particles have zero weight")) {
					System.out.println("All particles have zero weight.");
					return null;
				}
			}

			debug++;
			System.out.println("debug: " + debug + ". Before pruning:");
			query.printResults(System.out);
			query.prune(0.05);
			return query;
		}
	};

	static int debug = 0;

	/**
	 * A function computing true aircraft positions.
	 */
	private static DAEFunction getTrueAircraftPositions = new DAEFunction() {
		public boolean isRandom() {
			return true;
		}

		public Object evaluate(DependencyAwareEnvironment environment) {
			Object[] evidenceAndTruth = (Object[]) environment
					.getResultOrRecompute(getEvidenceAndTruth);
			return evidenceAndTruth[1];
		}
	};

	private static Problem getTestExample() {
		String modelDescription = "type RainEvent;"
				+ "guaranteed RainEvent Rainy, Dry;"
				+

				"random RainEvent Weather(Timestep);"
				+ "random Boolean RainyRegion();"
				+

				"RainyRegion ~ Bernoulli[0.5]();"
				+

				"Weather(d) ~ TabularCPD[[0.3, 0.7],[0.7, 0.3]](RainyRegion);"
				+ ""
				+ "random NaturalNum Indicator();"
				+ "Indicator ~ TabularCPD[[1,0,0,0],[0,1,0,0],[0,0,1,0],[0,0,0,1]](RainyRegion, Weather(@0));";

		String queryString = "query Indicator;";

		String evidenceString = "obs Weather(@0)=Rainy;";

		List linkStrings = Util.list("Weather(t)");

		String queryTemplate = "query Indicator;";

		return new Problem(modelDescription, evidenceString, queryString,
				linkStrings, queryTemplate);
	}

	private static Problem getUmbrella() {
		String modelDescription = "type RainEvent;"
				+ "guaranteed RainEvent Rainy, Dry;" +

				"random RainEvent Weather(Timestep);" +

				"Weather(d) " + " 	if (d = @0) then ~ TabularCPD[[0.2, 0.8]]()"
				+ "	else ~ TabularCPD[[0.2, 0.8]," + "	                  [0.1, 0.9]]"
				+ "	             (Weather(Prev(d)));" + ""
				+ "random Boolean Umbrella(Timestep);"
				+ "Umbrella(d) ~ TabularCPD[[0.8, 0.2],"
				+ "                         [0.2, 0.8]]"
				+ "                      (Weather(d));";

		String queryString = "query Weather(@8);";

		String evidenceString = "obs Umbrella(@0) = true;"
				+ "obs Umbrella(@1) = false;" + "obs Umbrella(@2) = true;"
				+ "obs Umbrella(@3) = false;" + "obs Umbrella(@4) = false;"
				+ "obs Umbrella(@5) = false;" + "obs Umbrella(@6) = true;"
				+ "obs Umbrella(@7) = false;";

		List linkStrings = Util.list("Umbrella(t)");

		String queryTemplate = "query Umbrella(t);";

		return new Problem(modelDescription, evidenceString, queryString,
				linkStrings, queryTemplate);
	}

	private static Problem getAircraftWithNumberOnlyAsEvidence() {
		String modelDescription = "type Aircraft;"
				+ "#Aircraft ~ Poisson[3];"
				+ "random Real Position(Aircraft a, Timestep t)"
				+ "    if (t = @0)"
				+ "        then ~ UniformReal[0,10]()"
				+ "        else ~ Gaussian(Position(a, Prev(t)));"
				+

				"type Blip;"
				+ "origin Aircraft Source(Blip);"
				+ "origin Timestep Time(Blip);"
				+ "#Blip(Source = a, Time = t) ~ TabularCPD[[0.9, 0.1]]();"
				+

				"#Blip(Time = t) ~ Poisson[2];"
				+ ""
				+ "random Real AppPos(Blip b, Timestep t)"
				+ "    if (Source(b) != null)"
				+ "        then ~ Gaussian(Position(Source(b), t))"
				+ "        else ~ UniformReal[0,10]();"
				+ "random Boolean twoAircraft;"
				+ "twoAircraft ~ TabularCPD[[0.0, 1.0], [0.0, 1.0], [1.0, 0.0], [0.0, 1.0], [0.0, 1.0], [0.0, 1.0], [0.0, 1.0], [0.0, 1.0], [0.0, 1.0], [0.0, 1.0], [0.0, 1.0], [0.0, 1.0], [0.0, 1.0], [0.0, 1.0], [0.0, 1.0], [0.0, 1.0], [0.0, 1.0], [0.0, 1.0], [0.0, 1.0], [0.0, 1.0], [0.0, 1.0], [0.0, 1.0], [0.0, 1.0], [0.0, 1.0], [0.0, 1.0], [0.0, 1.0]](#{Aircraft a});"
				+ "";

		String queryString = "query twoAircraft;";

		String evidenceString = "obs {Blip b : Time(b) = @0} = {B01, B02, B03};"
				+ "obs {Blip b : Time(b) = @1} = {B11, B12};"
				+ "obs {Blip b : Time(b) = @2} = {B21};"
				+ "obs {Blip b : Time(b) = @3} = {B31, B32, B33};"
				+ "obs {Blip b : Time(b) = @4} = {B41};"
				+ "obs {Blip b : Time(b) = @5} = {B51, B52};"
				+ "obs {Blip b : Time(b) = @6} = {B61, B62};"
				+ "obs {Blip b : Time(b) = @7} = {B71, B72};" + "";

		List linkStrings = Util.list("#{Blip b : Time(b) = t}");

		String queryTemplate = "query twoAircraft;";

		return new Problem(modelDescription, evidenceString, queryString,
				linkStrings, queryTemplate);
	}

	private static Problem getAircraft() {
		String modelDescription = "type Aircraft;" + "#Aircraft ~ Poisson[3];"
				+ "random Real Position(Aircraft a, Timestep t)" + "    if (t = @0)"
				+ "        then ~ UniformReal[0,10]()"
				+ "        else ~ Gaussian(Position(a, Prev(t)), 0.01);" +

				"type Blip;" + "origin Aircraft Source(Blip);"
				+ "origin Timestep Time(Blip);"
				+ "#Blip(Source = a, Time = t) ~ TabularCPD[[0.05, 0.95]]();" +

				"#Blip(Time = t) ~ Poisson[0.5];" + "" + "random Real AppPos(Blip b)"
				+ "    if (Source(b) != null)"
				+ "        then ~ Gaussian(Position(Source(b), Time(b)), 0.005)"
				+ "        else ~ UniformReal[0,10]();" + "";

		String queryString = "";

		String evidenceString = "";

		List linkStrings = Util.list("{AppPos(b) for Blip b : Time(b) = t}");

		String queryTemplate = "query {Position(a,t) for Aircraft a};";

		return new Problem(modelDescription, evidenceString, queryString,
				linkStrings, queryTemplate /* , discretizeAndSortNormalizer */);
	}

	private static Problem getAircraftWithGreaterVariance() {
		String modelDescription = "type Aircraft;" + "#Aircraft ~ Poisson[3];"
				+ "random Real Position(Aircraft a, Timestep t)" + "    if (t = @0)"
				+ "        then ~ UniformReal[0,10]()"
				+ "        else ~ Gaussian(Position(a, Prev(t)), 0.1);" +

				"type Blip;" + "origin Aircraft Source(Blip);"
				+ "origin Timestep Time(Blip);"
				+ "#Blip(Source = a, Time = t) ~ TabularCPD[[0.05, 0.95]]();" +

				"#Blip(Time = t) ~ Poisson[0.5];" + "" + "random Real AppPos(Blip b)"
				+ "    if (Source(b) != null)"
				+ "        then ~ Gaussian(Position(Source(b), Time(b)), 0.05)"
				+ "        else ~ UniformReal[0,10]();" + "";

		String queryString = "";

		String evidenceString = "";

		List linkStrings = Util.list("{AppPos(b) for Blip b : Time(b) = t}");

		String queryTemplate = "query {Position(a,t) for Aircraft a};";

		return new Problem(modelDescription, evidenceString, queryString,
				linkStrings, queryTemplate /* , discretizeAndSortNormalizer */);
	}

	private static Problem get3Aircraft() {
		String modelDescription = "type Aircraft;" + "#Aircraft = 3;"
				+ "random Real Position(Aircraft a, Timestep t)" + "    if (t = @0)"
				+ "        then ~ UniformReal[0,10]()"
				+ "        else ~ Gaussian(Position(a, Prev(t)), 0.01);" +

				"type Blip;" + "origin Aircraft Source(Blip);"
				+ "origin Timestep Time(Blip);"
				+ "#Blip(Source = a, Time = t) ~ TabularCPD[[0.05, 0.95]]();" +

				"#Blip(Time = t) ~ Poisson[0.5];" + "" + "random Real AppPos(Blip b)"
				+ "    if (Source(b) != null)"
				+ "        then ~ Gaussian(Position(Source(b), Time(b)), 0.005)"
				+ "        else ~ UniformReal[0,10]();" + "";

		String queryString = "";

		String evidenceString = "";

		List linkStrings = Util.list("{AppPos(b) for Blip b : Time(b) = t}");

		String queryTemplate = "query {Position(a,t) for Aircraft a};";

		return new Problem(modelDescription, evidenceString, queryString,
				linkStrings, queryTemplate /* , discretizeAndSortNormalizer */);
	}

	private static Problem get3AircraftNoFalseAlarmsNoDetectionFailure() {
		String modelDescription = "type Aircraft;" + "#Aircraft = 3;"
				+ "random Real Position(Aircraft a, Timestep t)" + "    if (t = @0)"
				+ "        then ~ UniformReal[0,10]()"
				+ "        else ~ Gaussian(Position(a, Prev(t)), 0.01);" +

				"type Blip;" + "origin Aircraft Source(Blip);"
				+ "origin Timestep Time(Blip);" + "#Blip(Source = a, Time = t) = 1;" +

				"/*#Blip(Time = t) ~ Poisson[0.5];*/" + ""
				+ "random Real AppPos(Blip b)" + "    if (Source(b) != null)"
				+ "        then ~ Gaussian(Position(Source(b), Time(b)), 0.005)"
				+ "        else ~ UniformReal[0,10]();" + "";

		String queryString = "";

		String evidenceString = "";

		List linkStrings = Util.list("{AppPos(b) for Blip b : Time(b) = t}");

		String queryTemplate = "query {Position(a,t) for Aircraft a};";

		return new Problem(modelDescription, evidenceString, queryString,
				linkStrings, queryTemplate /* , discretizeAndSortNormalizer */);
	}

	private static DAEFunction computeAircraftProblem = new DAEFunction() {
		public Object evaluate(DependencyAwareEnvironment environment) {

			String numberOfAircraftDistribution = (String) environment
					.getWithDefault("number of aircraft distribution", " ~ Poisson[3]()");
			String numberOfFalseAlarmsDistribution = (String) environment
					.getWithDefault("number of false alarms distribution",
							" ~ Poisson[0.5]()");
			String numberOfBlipsPerAircraftDistribution = (String) environment
					.getWithDefault("number of blips per aircraft distribution", " = 1");
			String positionTransitionVariance = (String) environment.getWithDefault(
					"transition variance", "0.01");
			String noiseVariance = (String) environment.getWithDefault(
					"noise variance",
					Double.toString(Double.valueOf(positionTransitionVariance) / 2.0));

			String modelDescription = "type Aircraft;" + "#Aircraft "
					+ numberOfAircraftDistribution + ";"
					+ "random Real Position(Aircraft a, Timestep t)" + "    if (t = @0)"
					+ "        then ~ UniformReal[0,10]()"
					+ "        else ~ Gaussian(Position(a, Prev(t)), "
					+ positionTransitionVariance + ");" +

					"type Blip;" + "origin Aircraft Source(Blip);"
					+ "origin Timestep Time(Blip);" + "#Blip(Source = a, Time = t) = 1;" +

					"#Blip(Time = t) " + numberOfFalseAlarmsDistribution + ";" + ""
					+ "random Real AppPos(Blip b)" + "    if (Source(b) != null)"
					+ "        then ~ Gaussian(Position(Source(b), Time(b)), "
					+ noiseVariance + ")" + "        else ~ UniformReal[0,10]();" + "";

			String queryString = "";

			String evidenceString = "";

			List linkStrings = Util.list("{AppPos(b) for Blip b : Time(b) = t}");

			String queryTemplate = "query {Position(a,t) for Aircraft a};";

			return new Problem(modelDescription, evidenceString, queryString,
					linkStrings, queryTemplate /* , discretizeAndSortNormalizer */);
		}
	};

	private static DAEFunction computeVelocityAircraftProblem = new DAEFunction() {
		public Object evaluate(DependencyAwareEnvironment environment) {

			String numberOfAircraftDistribution = (String) environment
					.getWithDefault("number of aircraft distribution", " ~ Poisson[3]()");
			String numberOfFalseAlarmsDistribution = (String) environment
					.getWithDefault("number of false alarms distribution",
							" ~ Poisson[0.5]()");
			String numberOfBlipsPerAircraftDistribution = (String) environment
					.getWithDefault("number of blips per aircraft distribution", " = 1");
			String transitionVariance = (String) environment.getWithDefault(
					"transition variance", "0.01");
			String noiseVariance = (String) environment.getWithDefault(
					"noise variance",
					Double.toString(Double.valueOf(transitionVariance) / 2.0));

			String modelDescription = "type Aircraft;" + "#Aircraft "
					+ numberOfAircraftDistribution + ";" +

					"random Real Velocity(Aircraft a, Timestep t)" + "    if (t = @0)"
					+ "        then ~ UniformReal[-0.5,0.5]()"
					+ "        else ~ Gaussian(Velocity(a, Prev(t)), "
					+ transitionVariance + ");" +

					"random Real Position(Aircraft a, Timestep t)" + "    if (t = @0)"
					+ "        then ~ UniformReal[0,10]()"
					+ "        else = RSum(Velocity(a,Prev(t)),  Position(a, Prev(t)));" +

					"type Blip;" + "origin Aircraft Source(Blip);"
					+ "origin Timestep Time(Blip);" + "#Blip(Source = a, Time = t) = 1;" +

					"#Blip(Time = t) " + numberOfFalseAlarmsDistribution + ";" + ""
					+ "random Real AppPos(Blip b)" + "    if (Source(b) != null)"
					+ "        then ~ Gaussian(Position(Source(b), Time(b)), "
					+ noiseVariance + ")" + "        else ~ UniformReal[0,10]();" + "";

			String queryString = "";

			String evidenceString = "";

			List linkStrings = Util.list("{AppPos(b) for Blip b : Time(b) = t}");

			String queryTemplate = "query {Position(a,t) for Aircraft a};";

			return new Problem(modelDescription, evidenceString, queryString,
					linkStrings, queryTemplate /* , discretizeAndSortNormalizer */);
		}
	};

	private static Problem getHMM() {
		String modelDescription = "type State;"
				+ "guaranteed State s1, s2, s3, s4;" +

				"random State StateAt(Timestep);" +

				"StateAt(t) "
				+ " 	if (t = @0) then ~ TabularCPD[[0.3, 0.3, 0.3, 0.1]]()"
				+ "	else ~ TabularCPD[[0.3, 0.3, 0.3, 0.1],"
				+ "	                  [0.3, 0.3, 0.3, 0.1],"
				+ "	                  [0.3, 0.3, 0.3, 0.1],"
				+ "                         [0.04, 0.03, 0.03, 0.9]]"
				+ "	             (StateAt(Prev(t)));" + ""
				+ "random Boolean Observation(Timestep);"
				+ "Observation(t) ~ TabularCPD[[0.9, 0.1],"
				+ "	                     [0.9, 0.1],"
				+ "	                     [0.9, 0.1],"
				+ "                            [0.1, 0.9]]"
				+ "                          (StateAt(t));";

		String queryString = "query StateAt(@8);";

		String evidenceString = "obs Observation(@0)=true;"
				+ "obs Observation(@1)=false;" + "obs Observation(@2)=true;"
				+ "obs Observation(@3)=true;" + "obs Observation(@4)=false;"
				+ "obs Observation(@5)=false;" + "obs Observation(@6)=false;"
				+ "obs Observation(@7)=true;";

		List linkStrings = Util.list("Observation(t)");

		String queryTemplate = "query StateAt(t);";

		return new Problem(modelDescription, evidenceString, queryString,
				linkStrings, queryTemplate);
	}

	private static Problem getWeatherWithoutAtemporal() {
		String modelDescription = "type RainEvent;"
				+ "guaranteed RainEvent Rainy, Dry;" +

				"random RainEvent Weather(Timestep);" +

				"Weather(d) " + " 	if (d = @0) then ~ TabularCPD[[0.7, 0.3]]()"
				+ "	else ~ TabularCPD[[0.8, 0.2]," + "	                  [0.3, 0.7]]"
				+ "	             (Weather(Prev(d)));";

		String queryString = "query Weather(@1);";

		String evidenceString = "";

		List linkStrings = Util.list("Weather(t)");

		String queryTemplate = "query Weather(t);";

		return new Problem(modelDescription, evidenceString, queryString,
				linkStrings, queryTemplate);
	}

	private static Problem getWeatherWithAtemporal() {
		String modelDescription = "type RainEvent;"
				+ "guaranteed RainEvent Rainy, Dry;" +

				"random RainEvent Weather(Timestep);" + "random Boolean RainyRegion();"
				+

				"RainyRegion ~ Bernoulli[0.5]();" +

				"Weather(d) "
				+ " 	if (d = @0) then ~ TabularCPD[[0.7, 0.3],[0.3, 0.7]](RainyRegion)"
				+ "	else ~ TabularCPD[[0.8, 0.2]," + "	                  [0.3, 0.7],"
				+ "	                  [0.5, 0.5]," + "	                  [0.2, 0.8]]"
				+ "	             (RainyRegion, Weather(Prev(d)));";

		String queryString = "query Weather(@6);";

		String evidenceString = "obs Weather(@0)=Rainy;" + "obs Weather(@1)=Rainy;"
				+ "obs Weather(@2)=Rainy;" + "obs Weather(@3)=Rainy;"
				+ "obs Weather(@4)=Rainy;" + "obs Weather(@5)=Dry;";

		List linkStrings = Util.list("Weather(t)");

		String queryTemplate = "query Weather(t);";

		return new Problem(modelDescription, evidenceString, queryString,
				linkStrings, queryTemplate);
	}

	static class RandomProblemIterator extends EZIterator {
		public RandomProblemIterator(int numOfProblems, int finalTimestepIndex) {
			this.numOfProblems = numOfProblems;
			this.finalTimestepIndex = finalTimestepIndex;
		}

		public Object calculateNext() {
			if (counter++ == numOfProblems)
				return null;
			return getWeatherWithoutAtemporal().getRandomProblem(8);
		}

		private int numOfProblems;
		private int finalTimestepIndex;
		private int counter = 0;
	};

	static class RandomProblemRange extends AbstractRange {
		public RandomProblemRange(String name, int numOfProblems,
				int finalTimestepIndex) {
			super(name);
			this.numOfProblems = numOfProblems;
			this.finalTimestepIndex = finalTimestepIndex;
		}

		public Object evaluate() {
			return new RandomProblemIterator(numOfProblems, finalTimestepIndex);
		}

		private int numOfProblems;
		private int finalTimestepIndex;
	};

	static class TemporalEvidenceRange extends AbstractRange {
		public TemporalEvidenceRange(String name, int numOfSteps, Model model,
				Collection linkStrings) {
			super(name);
			this.numOfSteps = numOfSteps;
			this.model = model;
			this.linkStrings = linkStrings;
		}

		public Object evaluate() {
			return new CountedIterator(numOfSteps,
					new TemporalEvidenceGenerator.EvidenceIterator(model, linkStrings));
		}

		private int numOfSteps;
		private Model model;
		private Collection linkStrings;
	};

	public static void testProblem(Problem problem) {
		problem = problem.getRandomProblem(5);
		InferenceEngine engine = new SamplingEngine(problem.model, Util.properties(
				"numSamples", "5000", "samplerClass", "blog.LWImportanceSampler"));
		engine.solve(problem.queries, problem.evidence);
		ArgSpecQuery query = (ArgSpecQuery) Util.getFirst(problem.queries);
		query.printResults(System.out);
	}

	/**
	 * Given a ValueEvidenceStatement of blips, returns the set of Doubles of
	 * their positions.
	 */
	private static Collection getBlipPositionsOutOfValueEvidenceStatement(
			ExplicitSetSpec evidencePositions) {
		Collection evidenceDoubles = new LinkedList();
		for (Term position : (Collection<Term>) evidencePositions.getElts()) {
			// System.out.println("Evidence element: " + position.asDouble());
			evidenceDoubles.add(position.asDouble());
		}
		return evidenceDoubles;
	}

}
