package figaro;

import java.util.*;

/**
 * @author Yi Wu
 * @date Sept 25, 2013
 *  
 *  Used to building DAG, and then using Topology Sort to generate the desired order
 */

public class FigaroDAG {
	int N;
	ArrayList<String> symbols;
	Map<String,Integer> index;
	ArrayList<ArrayList<Integer> > adj;
	int[] deg;
	ArrayList<String> order;
	
	public FigaroDAG(ArrayList<String> _symbols)
	{
		symbols = _symbols; N = symbols.size();
		deg = new int[N];
		index = new TreeMap<String,Integer>();
		order = new ArrayList<String>();
		
		adj = new ArrayList<ArrayList<Integer> >();
		for(int i=0;i<N;++i) {
			deg[i] = 0;
			adj.add(new ArrayList<Integer>());
			index.put(symbols.get(i), i);
		}
	}
	
	public boolean addEdge(String x, String y)
	{ // x --> y
		if(!index.containsKey(x) || !index.containsKey(y)) return false;
		int u = index.get(x), v = index.get(y);
		if(u == v) return false;
		
		//System.out.println("Add : " + x+ " --> " + y);
		
		adj.get(u).add(v);
		++ deg[v];
		return true;
	}
	
	public boolean compute(Figaro figaro)
	{
		Queue<Integer> Q = new LinkedList<Integer>();
		for(int i=0;i<N;++i)
			if(deg[i] == 0) Q.add(i);
		while(!Q.isEmpty())
		{
			int u = Q.poll();
			order.add(symbols.get(u));
			ArrayList<Integer> next = adj.get(u);
			for(int i=0;i<next.size();++i)
				if((--deg[next.get(i)]) == 0)
					Q.add(next.get(i));
		}
		if(order.size() != N)
		{
			order.clear();
			return false;
		}
		
		/*// TODO
		 * If we Already Pick a Class Definition, unless we pick a #Class, 
		 * we could not pick any other element outside the class
		 */
		
		Map<String,Integer> pos = new TreeMap<String,Integer>();
		Set<Integer> mark = new TreeSet<Integer>();
		ArrayList<String> array = new ArrayList<String>();
		ArrayList<ArrayList<Integer> > subject = new ArrayList<ArrayList<Integer> > ();
		for(int i=0;i<order.size();++i)
		{
			pos.put(order.get(i), i);
			if(figaro.hasClass(order.get(i)))
				subject.add(new ArrayList<Integer>());
			else {
				subject.add(null);
				if(order.get(i).startsWith("#"))
				{
					String clss = order.get(i).substring(1);
					subject.get(pos.get(clss).intValue()).add(i);
				} else
				if(figaro.getFuncCategory(order.get(i)) == Figaro.FUNC_FEATURE)
				{
					String clss = figaro.getFeatureBelong(order.get(i));
					subject.get(pos.get(clss).intValue()).add(i);
				}
			}
		}
		for(int i=0;i<order.size();++i)
		{
			if(mark.contains(i)) continue;
			mark.add(i);
			array.add(order.get(i));
			if(subject.get(i) != null)
			{
				for(int j=0;j<subject.get(i).size();++j)
				{
					mark.add(subject.get(i).get(j));
					array.add(order.get(subject.get(i).get(j)));
				}
			}
		}
		
		order = array;
		return true;
	}
	
	public ArrayList<String> getOrder(){return order;}
	
	///// Special Requirement During Recursion
	private boolean subFinal=false;
	private String memo_class="", memo_func="";
	private Set<String> memo_param;
	private Map<String,String>memo_type;
	public void setCurrClass(String s){memo_class=s;}
	public String getCurrClass(){return memo_class;}
	public void setCurrFunc(String s){memo_func=s;}
	public String getCurrFunc(){return memo_func;}
	public void addCurrParam(String s, String t){memo_param.add(s); memo_type.put(s,t);}
	public boolean hasCurrParam(String s){return memo_param.contains(s);}
	public String getCurrParamType(String s){return memo_type.get(s);}
	public void setCurr(String clss, String func){
		memo_class=clss;memo_func=func;
		memo_param=new TreeSet<String>();
		memo_type=new TreeMap<String,String>();
		subFinal=false;
	}
	public void setSubFinal(boolean s){subFinal=s;}
	public boolean isSubFinal(){return subFinal;} 
	// Specially Used By Expression of Evidence
	//   and Query
	//  When this is true, no dependency check will be done
}
