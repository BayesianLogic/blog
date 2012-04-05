package blog.common;

import java.util.*;
import java.io.*;

/**
 * A class for plotting graphs, using gnuplot, programmatically.
 * 
 * @author Rodrigo
 */
public class Gnuplot {
	/**
	 * Receives data (<code>xSeries</code> is a {@link NullaryFunction}, returning
	 * a list of numbers, or <code>null</code>, and <code>ySeries</code> is a list
	 * of {@link #YSeries}), and calls gnuplot on them, first issues a list of
	 * pre-commands to it. If one of the precommands is "persist", gnuplot
	 * persists after execution.
	 */
	static public void plot(Collection precommands, NullaryFunction xSeries,
			List ySeriesList) {
		Pipe pipe = new Pipe(precommands);
		Data data = new Data(xSeries, ySeriesList);
		try {
			sendPrecommands(precommands, pipe);
			pipe.send("plot " + data.getDescription());
			Util.wait_NE(gnuplotLaunchTime); // make sure this process lasts long
																				// enough for gnuplot to do read temp
																				// files before they are deleted.
		} catch (IOException e) {
			Util.fatalError("Could not pipe commands to gnuplot.", e);
		}
		data.cleanUp();
	}

	private static void sendPrecommands(Collection precommands, Pipe pipe)
			throws IOException {
		for (Iterator it = precommands.iterator(); it.hasNext();) {
			String precommand = (String) it.next();
			if (precommand.equals("persist"))
				continue;
			pipe.send(precommand);
		}
	}

	static private class Pipe extends BufferedOutputStream {
		public Pipe(Collection precommands) {
			super(getGnuplotOutputStream(precommands));
		}

		static public OutputStream getGnuplotOutputStream(Collection precommands) {
			Process process = null;
			String persist = precommands.contains("persist") ? " -persist" : " ";
			try {
				String executableName = System.getProperty("os.name").contains(
						"Windows") ? "pgnuplot.exe" : "gnuplot";
				process = Runtime.getRuntime().exec(executableName + persist);
			} catch (IOException e) {
				Util.fatalError("Could not launch gnuplot", e);
			}
			return process.getOutputStream();
		}

		public void send(String command) throws IOException {
			write((command + "\n").getBytes());
			flush();
		}
	}

	static private class Data {
		public Data(NullaryFunction xSeries, List ySeriesList) {
			try {
				descriptions = new LinkedList();
				for (Iterator it = ySeriesList.iterator(); it.hasNext();) {
					YSeries ySeries = (YSeries) it.next();
					descriptions.add(getDescription(xSeries, ySeries));
				}
			} catch (IOException e) {
				Util.fatalError("Could not generate data files for gnuplot.", e);
			}
		}

		/** Returns a comma-separated string of descriptions. */
		public String getDescription() {
			return Util.join(",", descriptions);
		}

		/** Get data series description for a particular list of data. */
		private String getDescription(NullaryFunction xSeries, YSeries ySeries)
				throws IOException {
			String titleClause = extractClause(ySeries.directives, "title ", "t ");
			if (titleClause.equals(""))
				titleClause = extractClause(ySeries.directives, "notitle", "notitle");
			String withClause = extractClause(ySeries.directives, "with ", "w ");
			String path = storeDataAndReturnPath(xSeries == null ? null
					: (Iterator) xSeries.evaluate(), (Iterator) ySeries.data.evaluate());
			StringBuffer command = new StringBuffer();
			command.append("'" + path + "'");
			command.append(xSeries != null ? " using 1:2" : " using 1");
			command.append(" " + titleClause);
			command.append(" " + withClause);
			return command.toString();
		}

		private String extractClause(List directives, String fullName,
				String abbreviation) {
			String clause = "";
			for (Iterator it = directives.iterator(); it.hasNext();) {
				String string = (String) it.next();
				if (string.startsWith(fullName) || string.startsWith(abbreviation)) {
					clause = string;
				}
			}
			return clause;
		}

		private String storeDataAndReturnPath(Iterator xSeries, Iterator ySeries)
				throws IOException {
			// File tempFile = File.createTempFile("gnuplot", ".dat");
			File tempFile = new File("gnuplot" + dataFileIndex++ + ".dat");
			BufferedWriter w = new BufferedWriter(new FileWriter(tempFile));
			if (xSeries != null) {
				writeDataWithXSeries(xSeries, ySeries, w);
			} else {
				writeDataWithoutXSeries(ySeries, w);
			}
			w.close();
			filesToDelete.add(tempFile);
			return tempFile.getPath();
		}

		private void writeDataWithoutXSeries(Iterator yIt, BufferedWriter w)
				throws IOException {
			while (yIt.hasNext()) {
				Object y = yIt.next();
				w.write(y.toString());
				w.newLine();
			}
		}

		private void writeDataWithXSeries(Iterator xSeries, Iterator ySeries,
				BufferedWriter w) throws IOException {
			while (xSeries.hasNext()) {
				w.write(xSeries.next() + " " + ySeries.next());
				w.newLine();
			}
		}

		public void cleanUp() {
			for (Iterator it = filesToDelete.iterator(); it.hasNext();) {
				File file = (File) it.next();
				// file.deleteOnExit();
			}
		}

		/** A list of string descriptions of each data series. */
		private List descriptions;

		/** A list of File objects to be deleted at clean up. */
		private List filesToDelete = new LinkedList();

		/** Index used to name data files. */
		private int dataFileIndex = 0;
	}

	/**
	 * Class representing a y series. If a list <code>directives</code> of Strings
	 * is given, each of them is interpreted in the following way: if it starts
	 * with either "title " or "t ", it is considered a 'title' directive for the
	 * series; if it starts with either "with " or "w ", it is considered a 'with'
	 * directive for the series. Their order does not matter and they are
	 * correctly placed in the gnuplot command.
	 */
	public static class YSeries {
		public YSeries(List data) {
			this.directives = Util.list();
			this.data = Util.getIteratorNullaryFunction(data);
		}

		public YSeries(List directives, List data) {
			this.directives = directives;
			this.data = Util.getIteratorNullaryFunction(data);
		}

		public YSeries(List directives, NullaryFunction data) {
			this.directives = directives;
			this.data = data;
		}

		public List directives;
		public NullaryFunction data;
	}

	/**
	 * Time in milliseconds that is enough for gnuplot to launch and read temp
	 * files safely, with default of 2s.
	 */
	public static long gnuplotLaunchTime = 2000;

	public static void main(String[] args) {
		plot(Util.list( // precommands
				"persist",
				// "set term postscript color",
				// "set output 'test.ps'",
				"set xlabel 'Time'", "set ylabel 'Intensity'"),
				Util.getIteratorNullaryFunction(Util.list(10, 20, 30, 40)), // xSeries
				Util.list(
						// list of ySeries
						new YSeries(
								Util.list("title 'random sequence 1'", "w linespoints"), Util
										.list(1, 2, 4, 3)),
						new YSeries(
								Util.list("title 'random sequence 2'", "w linespoints"), Util
										.list(5, 4, 3, 2))));
	}
}
