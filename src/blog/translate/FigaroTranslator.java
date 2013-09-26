package blog.translate;

import java.io.*;
import java.util.*;

import blog.model.Evidence;
import blog.model.Model;
import blog.model.Query;
import blog.msg.ErrorMsg;
import blog.parse.Parse;
import blog.semant.Semant;

import figaro.*;

/**
 * @author Yi Wu
 * @date Sept 25, 2013
 * 
 *  Generate instance of FigaroTranslator, putting parameters and call translate() to execute compiler
 */
public class FigaroTranslator {
	/**
	 * 
	 * Usage: translator [< BLOG file >] [< Target Figaro File > = "output.figaro"] [< Figaro Structure Log File >] 
	 * 
	 * @param args
	 */
	
	private Figaro figaro = null;

	private boolean checkBlogSemant(Parse parse, ErrorMsg msg)
	{
		Model m = new Model();
		Evidence e = new Evidence();
		ArrayList<Query> qs = new ArrayList<Query>();
		Semant sem = new Semant(m, e, qs, msg);
		if(msg.OK())
			sem.transProg(parse.getParseResult());
		return msg.OK();
	}
	
	private boolean translate(FileReader inp, FileWriter oup, File lg, String name)
	{
		figaro = new Figaro();
		
		System.out.println(" >> Parsing input Blog file ...");
		
		ErrorMsg msg = new ErrorMsg(name);
		Parse parse = new Parse(inp, msg);
		
		if(!msg.OK()) return false;
		
		System.out.println("  >> Start BLOG semantic checking ...");
		
		if(!checkBlogSemant(parse, msg)) return false;
		
		System.out.println("  >> Checking BLOG program compatibility...");
		
		FigaroSemant sem = new FigaroSemant(figaro, msg);
		sem.setup(parse.getParseResult());
		
		if(!msg.OK()) return false;
		
		if(lg != null)
		{
			System.out.println("    --> Printing sructures to Log File ...");
			figaro.printLog(lg);
		}
		
		System.out.println("  >> Generation target Figaro tokens...");
		
		FigaroTokenizer trans = new FigaroTokenizer(figaro, msg);
		if(!trans.translate()) return false;
		
		System.out.println("  >> Writing Figaro program to target file...");
		
		FigaroWriter wrt = new FigaroWriter(trans.getTokenList(), oup);
		if(!wrt.process()) return false;
		
		return msg.OK();
	}
	
	public boolean trainslate(String[] args) {
		if(args.length < 2)
		{
			System.out.println("Compiler Usage: translator <BLOG File> [ < Target Figaro File > = \"output.scala\" ] [ < Figaro Structure Log File > ]");
			return false;
		}
		
		File blogFile = new File(args[1]);
		
		if(!blogFile.exists())
		{
			System.out.println("Error: BLOG File does not exist!");
			return false;
		}
		if(!blogFile.canRead())
		{
			System.out.println("Error: BLOG File cannot be read!");
			return false;
		}
		
		File figaroFile = null;
		if(args.length >= 3)
			figaroFile = new File(args[2]);
		else {
			System.out.println("Default: output file is set to be < output.scala >");
			figaroFile = new File("output.scala");
		}
		if(!figaroFile.exists())
			try {
				if(!figaroFile.createNewFile())
				{
					System.out.println("Error:Target Output File cannot be created!");
					return false;
				}
			} catch (IOException e1) {
				System.out.println("Error: Java Exception found!\n" + e1);
				return false;
			}
		if(!figaroFile.canWrite())
		{
			System.out.println("Error: Target Output File cannot be written!");
			return false;
		}
		
		File logFile = null;
		if(args.length >= 4)
		{
			boolean ok=true;
			logFile = new File(args[3]);
			if(!logFile.exists())
				try {
					if(!logFile.createNewFile())
						ok=false;
				} catch (IOException e1) {
					ok=false;
				}
			if(!ok || !logFile.canWrite())
			{
				System.out.println("Warning: Log File cannot be written! no log will be printed.");
				logFile = null;
			}
		}

		try {
			if(!translate(new FileReader(blogFile), new FileWriter(figaroFile), logFile, blogFile.getName())){
				System.out.println("Translation Failed!");
				return false;
			} else
			{
				System.out.println("Translation Succeeded!");
			}
		} catch (IOException e) {
			System.out.println("Error: Java Exception found!\n" + e);
			return false;
		}
		return true;
	}
};
