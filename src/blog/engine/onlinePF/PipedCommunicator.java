package blog.engine.onlinePF;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

public class PipedCommunicator extends Communicator {
	public BufferedReader b;
	public PipedCommunicator (){
		PipedInputStream pin = new PipedInputStream();
		PipedOutputStream pout = null;
		try {
			 pout = new PipedOutputStream(pin);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		p = new PrintStream(pout);
		b = new BufferedReader(new InputStreamReader(pin));
	}
	@Override
	public void printInput(String message) {
		p.println(message);
		
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
	@Override
	public void printInputNL(String message) {
		p.print(message);
		
	}

}
