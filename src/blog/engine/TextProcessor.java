package blog.engine;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import blog.engine.onlinePF.FileCommunicator;

public class TextProcessor {
	public static void main (String[] args) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader("ex_inprog//logistics//generatedModel2.mblog"));
		FileCommunicator fc = new FileCommunicator("randomstuff//generatedEvidence.eblog");
		String output;
                String[]eva = new String[1001];
                String[]evb = new String[1001];
		for (int i = 0; i<=1000; i++){
                        eva[i] = br.readLine();
		}
                for (int i = 0; i<=1000; i++){
                        evb[i] = br.readLine();
		}
                for (int i = 0; i<=1000; i++){
			fc.printInput(eva[i]+evb[i]);
		}
	
		fc.p.flush();
		fc.p.close();
		/*
		String[] args1 = {"generatedModel.mblog", "-e", "blog.engine.ParticleFilter", "-n", "1", "-r" };
		blog.Main.main(args1);
		
		br = new BufferedReader(new FileReader("ex_inprog//logistics//monopoly_color.mblog"));
		fc = new FileCommunicator("randomstuff//generatedModel2.mblog");
		while ((output = br.readLine())!=null){
			fc.printInput(output);
		}
		
		
		BufferedReader br2 = new BufferedReader(new FileReader("randomstuff//observations.mblog"));
		String var = "";
		String val = "";
		while ((var = br2.readLine())!=null){
			val = br2.readLine();
			var = var.substring(27);
			val = val.substring(5);
			fc.printInput("obs " + var + "=" + val + ";");
		}
		*/
	}
}
