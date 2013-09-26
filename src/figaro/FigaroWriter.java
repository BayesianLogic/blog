package figaro;

import java.util.*;
import java.io.*;

/**
 * @author Yi Wu
 * @date Sept 25, 2013
 *  
 *  Used to print Figaro tokens to File
 */

public class FigaroWriter {
	ArrayList<String> tokens;
	PrintWriter writer;
	
	public FigaroWriter(ArrayList<String> t, FileWriter w){tokens=t; writer =new PrintWriter(w);}
	
	final static int LINE_LENGTH = 100;
	
	public boolean process()
	{
		// TODO: Control Output Format more elegantly
		
		int cur = 0;
		for(int i=0;i<tokens.size();++i)
		{
			writer.print(tokens.get(i));
			cur += tokens.get(i).length();
			if((cur > LINE_LENGTH && !tokens.get(i+1).equals(";") && !tokens.get(i+1).equals(")")&& !tokens.get(i+1).equals(",")) 
					|| tokens.get(i).equals("{") || tokens.get(i).equals(";")) {
				writer.println("");
				cur = 0;
			} else
			if(tokens.get(i).equals("\n")) cur = 0;
		}
		writer.close();
		return true;
	}
}
