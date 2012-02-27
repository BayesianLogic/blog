package test;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintStream;

import blog.Main;

public class TestUrnBall {

	/**
	 * @param args
	 */
	public static void main(String[] args) {	
		StringBuilder sb = new StringBuilder();
		int numObs = 32;
		try {
			System.setOut(new PrintStream(new FileOutputStream("ball" + numObs + "_blog.output")));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		mblog = mblog + "guaranteed Draw Draw[" + numObs + "];\n";
		for (int i = 1; i <= numObs; i++) {
			if (i % 2 == 0) {
				sb.append("obs ObsColor(Draw");
				sb.append(i);
				sb.append(") = Green;\n");
			} else {
				sb.append("obs ObsColor(Draw");
				sb.append(i);
				sb.append(") = Blue;\n");				
			}
		}
		eblog = sb.toString();
		createFile();
		
		Main.main(cmdargs);
	}
	

	
	static void createFile() {
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(testfile));
			out.write(mblog);
			out.write(eblog);
			out.write(qblog);
			out.close();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	static final String testfile = "test_ball.blog";
	
	static final String[] cmdargs = {testfile, "-n", "1000000", "-r"};
	
	static String eblog = null;

	static String mblog = "type Ball;\n" + 
			"type Draw;\n" + 
			"type Color;\n" + 
			"\n" + 
			"random Color Color(Ball);\n" + 
			"random Ball BallDrawn(Draw);\n" + 
			"random Color ObsColor(Draw);\n" + 
			"\n" + 
			"guaranteed Color Blue, Green;\n" +  
			"\n" + 
			"#Ball {\n" + 
			"	~ UniformInt[1, 8]()\n" + 
			"};\n" + 
			"\n" + 
			"Color(b) {\n" + 
			"	~ TabularCPD[[0.5, 0.5]]()\n" + 
			"};\n" + 
			"\n" + 
			"BallDrawn(d) {\n" + 
			"	~ UniformChoice({Ball b})\n" + 
			"};\n" + 
			"\n" + 
			"ObsColor(d) {\n" + 
			"	if BallDrawn(d) != null then  \n" + 
			"		~ TabularCPD[[0.8, 0.2], [0.2, 0.8]](Color(BallDrawn(d)))\n" + 
			"};\n" +
			"\n";
	
	static final String qblog = "query #{Ball b};\n";
	
	
	
}
