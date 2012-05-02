/**
 * 
 */
package test.blog.sample.modular;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;

import blog.common.Histogram;
import blog.model.ArgSpecQuery;

/**
 * @author leili
 * @date Apr 30, 2012
 */
public class ModularLWExperimentOnAircraft {

	private static final int N = 100;
	private static final int REPEATS = 10;
	private static final int MAX = 10000000;
	private static PrintWriter out;

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		String[] airargs = new String[args.length + 4];
		airargs[0] = "-n";
		airargs[2] = "example/simple-aircraft.blog";
		airargs[3] = "-r";
		// airargs[4] = "-s";
		// airargs[5] = "blog.sample.modular.ModularLWSampler";
		for (int n = 0; n < args.length; n++) {
			airargs[n + 4] = args[n];
		}

		out = new PrintWriter(new FileWriter("simple-aircraft-" + Math.random()
				+ ".result"));
		out.println(Arrays.toString(airargs));

		double[] answer = new double[] { 0, 0.711148719206, 0.260502862743,
				0.0268384295446, 0.0014564829152, 5.21023732859e-05, 1.37417508627e-06 };
		double cumerr = 0;
		for (int i = 1; i < MAX;) {
			i = i * 10;
			out.print(i);
			airargs[1] = Integer.toString(i);
			cumerr = 0;
			for (int rep = 0; rep < REPEATS; rep++) {
				blog.Main.main(airargs);
				ArgSpecQuery q = (ArgSpecQuery) blog.Main.getQueries().get(0);
				Histogram hist = q.getHistogram();
				double[] res = new double[N];
				for (int j = 0; j < N; j++)
					res[j] = 0;
				for (Iterator iter = hist.entrySet().iterator(); iter.hasNext();) {
					Histogram.Entry entry = (Histogram.Entry) iter.next();
					int x = ((Integer) entry.getElement()).intValue();
					double prob = entry.getWeight() / hist.getTotalWeight();
					if (x < N)
						res[x] = prob;
				}
				double error = computeError(answer, res);
				out.print("\t");
				out.print(error);
				cumerr += error;
			}
			double avgerr = cumerr / REPEATS;
			out.print("\t\t\t");
			out.println(avgerr);
			out.flush();
		}
		out.close();
	}

	/**
	 * @param answer
	 * @param entries
	 * @return
	 */
	private static double computeError(double[] answer, double[] res) {
		double error = 0;
		int i;
		for (i = 0; i < answer.length; i++) {
			error += Math.abs(answer[i] - res[i]);
		}
		for (; i < res.length; i++) {
			error += Math.abs(res[i]);
		}
		return error;
	}

}
