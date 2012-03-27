package common;

import java.util.*;

import common.Gnuplot.YSeries;
import common.RangeOperations.Axis;
import common.RangeOperations.Range;

public class Experiment {

	public static String[] basicPreCommands = { "set xlabel font 'Arial, 20",
			"set ylabel font 'Arial, 20'", "set title font 'Arial, 10'" };

	/**
	 * Runs an experiment (see {@link RangeOperations} and {@link YSeriesSpec}.
	 * Arguments can be provided in any order, although the order of range
	 * operation does matter.
	 */
	public static void experiment(Object... args) {

		List data = (List) RangeOperations.run(args);

		HashMapWithGetWithDefault properties = Util.getMapWithStringKeys(args);

		// PreCommands preCommands = getPreCommands(args);
		LinkedList preCommands = new LinkedList();

		for (String preCommand : basicPreCommands) {
			preCommands.add(preCommand);
		}

		if (properties.containsKey("title"))
			preCommands.add("set title '" + properties.get("title") + "'");
		if (properties.containsKey("xlabel"))
			preCommands.add("set xlabel '" + properties.get("xlabel") + "'");
		if (properties.containsKey("ylabel"))
			preCommands.add("set ylabel '" + properties.get("ylabel") + "'");
		if (properties.containsKey("filename")
				|| properties.getWithDefault("print", "false").equals("true")
				|| properties.getWithDefault("file", "false").equals("true")) {
			String filename = (String) properties.getWithDefault(
					properties.get("filename"), properties.get("title"));
			preCommands.add("set term postscript color");
			preCommands.add("set output '" + filename + ".ps'");
		} else {
			preCommands.add("persist");
		}

		List axes = getAxes(args);
		YSeriesSpec seriesSpec = getSeriesSpec(args);

		Range xSeries = getXSeries(axes, seriesSpec);
		List ySeriesList = getYSeriesList(data, axes, seriesSpec);

		Gnuplot.plot(preCommands, xSeries, ySeriesList);
	}

	private static List getAxes(Object... args) {
		List result = new LinkedList();
		for (Object object : args) {
			if (object instanceof Axis)
				result.add(object);
		}
		return result;
	}

	/**
	 * A class indicating the variable corresponding to a Y series in a graph, as
	 * well as its directives (see {@link Gnuplot}).
	 */
	public static class YSeriesSpec {
		public YSeriesSpec(String variable, List directivesList) {
			this.variable = variable;
			this.directivesList = directivesList;
		}

		private String variable;
		private List directivesList;
	}

	/** Convenience method for constructing a {@link YSeriesSpec}. */
	public static YSeriesSpec YSeriesSpec(String variable, List directivesList) {
		return new YSeriesSpec(variable, directivesList);
	}

	private static YSeriesSpec getSeriesSpec(Object... args) {
		return (YSeriesSpec) Util.getObjectOfClass(YSeriesSpec.class, args);
	}

	private static Range getXSeries(List axes, YSeriesSpec seriesSpec) {
		for (Iterator it = axes.iterator(); it.hasNext();) {
			Axis axis = (Axis) it.next();
			if (!axis.getRange().getName().equals(seriesSpec.variable))
				return axis.getRange();
		}
		return null;
	}

	private static Axis getSeriesAxis(List axes, YSeriesSpec seriesSpec) {
		for (Iterator it = axes.iterator(); it.hasNext();) {
			Axis axis = (Axis) it.next();
			if (axis.getRange().getName().equals(seriesSpec.variable))
				return axis;
		}
		return null;
	}

	private static List getYSeriesList(List data, List axes,
			YSeriesSpec seriesSpec) {
		List ySeriesList = new LinkedList();
		Axis ySeriesAxis = getSeriesAxis(axes, seriesSpec);
		if (ySeriesAxis != null) {
			int dimension = axes.indexOf(ySeriesAxis);
			Iterator rangeIterator = (Iterator) ySeriesAxis.getRange().evaluate();
			Iterator directiveIterator = seriesSpec.directivesList.iterator();
			int sliceIndex = 0;
			while (rangeIterator.hasNext()) {
				rangeIterator.next();
				List directives = (List) directiveIterator.next();
				List ySeriesData = Util.matrixSlice(data, dimension, sliceIndex);
				ySeriesList.add(new YSeries(directives, ySeriesData));
				sliceIndex++;
			}
		} else {
			if (axes.size() > 1) {
				Util.fatalError("YSeriesSpec "
						+ seriesSpec
						+ " does not refer to any present axis and data is multidimensional.");
			}
			List directives = (List) Util.getFirst(seriesSpec.directivesList);
			ySeriesList.add(new YSeries(directives, data));
		}
		return ySeriesList;
	}

	/**
	 * An extension of {@link List} for keeping pre-commands for a {@link Gnuplot}
	 * graph.
	 */
	private static class PreCommands extends LinkedList {
		public PreCommands(String... preCommands) {
			addAll(Arrays.asList(preCommands));
		}
	}

	public static PreCommands preCommands(String... preCommands) {
		return new PreCommands(preCommands);
	}

	public static PreCommands getPreCommands(Object... args) {
		PreCommands preCommands = (PreCommands) Util.getObjectOfClass(
				PreCommands.class, args);
		if (preCommands == null)
			preCommands = new PreCommands();
		return preCommands;
	}

	private static class Title {
		public Title(String value) {
			buffer.append(value);
		}

		public String toString() {
			return buffer.toString();
		}

		private StringBuffer buffer = new StringBuffer();
	}

	public static Title Title(String value) {
		return new Title(value);
	}

	public static String getTitle(Object... args) {
		return (String) Util.getObjectOfClass(Title.class, args).toString();
	}

}
