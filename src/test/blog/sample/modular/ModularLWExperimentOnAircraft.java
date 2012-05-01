/**
 * 
 */
package test.blog.sample.modular;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import blog.common.Histogram;
import blog.model.ArgSpecQuery;

/**
 * @author leili
 * @date Apr 30, 2012
 */
public class ModularLWExperimentOnAircraft {

	private static final int N = 100;
	private static PrintWriter out;

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		out = new PrintWriter(new FileWriter("simple-aircraft-lw.result"));
		String[] airargs = new String[4];
		airargs[0] = "-n";
		airargs[2] = "example/simple-aircraft.blog";
		airargs[3] = "-r";
		double[] answer = new double[] { 0, 0.711148719206, 0.260502862743,
				0.0268384295446, 0.0014564829152, 5.21023732859e-05, 1.37417508627e-06 };
		double cumerr = 0;
		int t = 0;
		for (int i = 1; i <= 1000000;) {
			i = i * 10;
			airargs[1] = Integer.toString(i);
			cumerr = 0;
			for (int rep = 0; rep < 10; rep++) {
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
				cumerr += error;
			}
			double avgerr = cumerr / 10;
			out.print(i);
			out.print("\t");
			out.println(avgerr);
			t++;
		}
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
