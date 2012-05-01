/**
 * 
 */
package test.blog.sample.modular;

/**
 * @author leili
 * @date Apr 30, 2012
 */
public class ModularLWExperimentOnAircraft {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String[] airargs = new String[3];
		airargs[0] = "-n";
		airargs[2] = "example/simple-aircraft.blog";
		for (int i = 1; i <= 1000000; i++) {
			i = i * 10;
			airargs[1] = Integer.toString(i);
			blog.Main.main(args);
		}

	}

}
