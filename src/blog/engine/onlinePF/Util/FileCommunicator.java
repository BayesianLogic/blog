package blog.engine.onlinePF.Util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 * Communicator implemented using filereaders/filewriters
 * @author cheng
 *
 */
public class FileCommunicator extends Communicator{
	private BufferedReader b;
	/**
	 * Overloaded constructor
	 * @param filename the string of the file to be used as a medium for printing/reading
	 */
	public FileCommunicator (String filename){
		try {
			p = new PrintStream(filename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
		FileReader f = null;
		try {
			f = new FileReader(filename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
		b = new BufferedReader(f);
		
		
	}
	@Override
	public void printInput(String message) {
		p.println(message);
		
	}
	
	public void printInputNL(String message) {
		p.print(message);
		
	}

	@Override
	public String readInput() {
		try {
			return b.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}
	
	

}
