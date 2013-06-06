package blog.engine.onlinePF;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import blog.engine.onlinePF.Util.Communicator;

public class CmdCommunicator extends Communicator{
	private BufferedReader b;
	public CmdCommunicator (){
		b = new BufferedReader(new InputStreamReader(System.in));
	}
	
	@Override
	public void printInput(String message) {
		System.err.println("not supported");
		System.exit(1);
	}
	
	public void printInputNL(String message) {
		System.err.println("not supported");
		System.exit(1);
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
