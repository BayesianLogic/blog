package blog.engine.onlinePF.absyn;

import java.util.ArrayList;


public class LoopAssignment{
	String loopVariable;
	ArrayList<String> assignments;
	public LoopAssignment(String loopVariable, ArrayList<String> assignments){
		this.loopVariable = loopVariable;
		this.assignments = assignments;
	}
}