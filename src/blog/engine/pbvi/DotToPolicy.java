package blog.engine.pbvi;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import blog.model.Evidence;

public class DotToPolicy {
	private Map<String, String> nodeIDAction;
	private Map<String, Map<Set<String>, String>> nodeIDObsNext;
	private String topID = null;
	
	public DotToPolicy() {
		nodeIDAction = new HashMap<String, String>();
		nodeIDObsNext = new HashMap<String, Map<Set<String>, String>>();
	}
	
	private String curPolicyID;
	
	public Evidence getAction(Set<Evidence> nextActions) {
		String actionString = nodeIDAction.get(curPolicyID);
		Evidence action = null;
		//System.out.println("the right action" + actionString);
		for (Evidence a : nextActions) {
			if (a.toString().equals(actionString)) {
				action = a;
				break;
			}
			//System.out.println("is it this action" + a);
		}
		return action;
	}
	
	public int unhandledObs = 0;
	public boolean advancePolicy(Evidence obs) {
		if (!nodeIDObsNext.containsKey(curPolicyID)) return false;
		Set<String> obsStringSet = obs.stringSet();
		String newPolicyID = nodeIDObsNext.get(curPolicyID).get(obsStringSet);
		if (newPolicyID == null) {
			//System.out.println("missing" + obsStringSet);
			Set<Set<String>> handledObservations = nodeIDObsNext.get(curPolicyID).keySet();
			//System.out.println("handled" + handledObservations);
			//System.exit(0);
			unhandledObs++;
			for (Set<String> handledObs : handledObservations) {
				newPolicyID = nodeIDObsNext.get(curPolicyID).get(handledObs);
				break;
			}
		}
		curPolicyID = newPolicyID;
		return true;
	}
	
	public void resetSim() {
		curPolicyID = topID;
	}
	
	private boolean isNodeLine(String line) {
		if (line.contains("->"))
			return false;
		return true;
	}
	
	private void parseLine(String line) {
		if (isNodeLine(line)) {
			String[] splitted = line.split(" ", 2);
			String nodeID = splitted[0];
			String action = splitted[1];
			action = action.split("\"")[1];
			nodeIDAction.put(nodeID, action);
			if (topID == null) {
				topID = nodeID;

				System.out.println("top nodeid " + nodeID);
				System.out.println("top action " + action);
			}
		} else {
			String[] splitted = line.split(" ", 4);
			String firstNode = splitted[0];
			String secondNode = splitted[2];
			String obs = splitted[3];
			obs = obs.split("\"")[1];
			if (!nodeIDObsNext.containsKey(firstNode)) {
				nodeIDObsNext.put(firstNode, new HashMap<Set<String>, String>());
			}
			String observations[] = obs.split("\\\\n");
			Set<String> observationSet = new HashSet<String>();
			for (String o : observations) {
				if (o.contains("random") || o.contains("merged"))
					continue;
				String temp = o.replaceAll("\\s+","");
				if (temp.isEmpty()) continue;
				//System.out.println(o);
				observationSet.add(o);
			}
			//System.exit(0);
			
			if (!nodeIDObsNext.containsKey(observationSet))
				nodeIDObsNext.get(firstNode).put(observationSet, secondNode);
			else {
				System.err.println("same observation edge in policy");
				System.exit(0);
			}
			//if (firstNode.equals("p0_134")) System.out.println(observationSet);
			//System.out.println("first" + firstNode);
			//System.out.println("second" + secondNode);
			//System.out.println("obs" + observationSet);
		}
	}
	
	public void createPolicy(String filename) {
		InputStream fis = null;
		BufferedReader br;
		String line;

		try {
			fis = new FileInputStream(filename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
		try {
			while ((line = br.readLine()) != null) {
			    parseLine(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Done with the file
		try {
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		br = null;
		fis = null;
	}
	
	public static void main(String[] args) {
		DotToPolicy p = new DotToPolicy();
		p.parseLine("p0_134_0 [label=\"[/*DerivedVar*/ apply_listen(@0) = true]\"]");
		p.parseLine("p0_134_0_0_0 -> p0_134_0_0_0_0 [label=\"Num_nonstate_Sound(@1) = 1\nnonstate_SoundAtDoor(3, @1) = true\nnonstate_SoundAtDoor(2, @1) = true\nnonstate_SoundAtDoor(1, @1) = true\nnonstate_SoundAtDoor(0, @1) = false\nobservable_nonstate_SoundAtDoor(3, @1) = true\nobservable_nonstate_SoundAtDoor(2, @1) = true\nobservable_nonstate_SoundAtDoor(1, @1) = true\nobservable_Num_nonstate_Sound(@1) = true\nobservable_nonstate_SoundAtDoor(0, @1) = true\n \"];");
	}
	
}
