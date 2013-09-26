package figaro;

import java.util.*;

import figaro.type.*; 
import blog.msg.ErrorMsg;

/**
 * @author Yi Wu
 * @date Sept 25, 2013
 *  
 *  Important!
 *  
 *  Collection information in Figaro Class, and then print the FIGARO code tokens
 */

public class FigaroTokenizer {
	private ErrorMsg errorMsg;
	private Figaro figaro;
	
	private int count = 0;
	
	ArrayList<String> tokens;
	
	Set<String> funcParams;
	
	public FigaroTokenizer(Figaro _figaro, ErrorMsg msg){
		figaro=_figaro; errorMsg = msg;
		tokens = new ArrayList<String>();
		funcParams = new TreeSet<String>();
	}
	
	boolean transArray(ArrayTerm term)
	{
		// We Directly transfer ArrayTerm to a String
		// No Internal Variable Required
		
		for(int i=0;i<term.paramSize();++i) {
			if(!transExpr(term.getParam(i))) return false;
		}
		
		term.setRandom(false);
		
		StringBuffer ref = new StringBuffer();
		ref.append("(");
		for(int i=0;i<term.paramSize();++i) {
			if(i > 0) ref.append(',');
			ref.append(term.getParam(i).getRefer());
		}
		ref.append(')');
		term.setRefer(ref.toString());
		return true;
	}
	
	boolean transConst(Constant c)
	{
		c.setRandom(false);
		if(c.isNull()) {
			if(c.basicType())
			{
				error(-1,-1,"Scala Does NOT Support < NULL > for basic type : " + c.getType());
				return false;
			}
			c.setRefer("null.asInstanceOf["+c.getType()+"]");
		}
		else c.setRefer(c.getValue());
		return true;
	}
	
	boolean transDistri(Distribution d)
	{
		// Special Case
		// Categorical And UniformChoice
		
		for(int i=0;i<d.paramSize();++i) {
			if(!transExpr(d.getParam(i))) return false;
			if(d.getParam(i).likelyNull())
			{
				if(!d.getOrigin().equals("TabularCPD") && i != 1)
				{
					error(-1,-1,"The input parameter for distribution < " + d.getOrigin() + " > cannot be NULL!");
					return false;
				}
			}
		}
		
		d.setRandom(true);
		
		if(d.getOrigin().equals("UniformChoice"))
		{
			SetTerm term = (SetTerm)d.getParam(0);
			if(term.paramSize() > 0)
			{ // Explicit Set
				StringBuffer buf = new StringBuffer();
				buf.append("Uniform(");
				for(int i=0;i<term.paramSize();++i)
				{
					if(i>0) buf.append(',');
					
					if(term.getParam(i).likelyNull())
						d.setLikelyNull(true);
					
					if(term.getParam(i).isRandom())
						buf.append(term.getParam(i).getRefer());
					else
						buf.append("Constant("+term.getParam(i).getRefer()+")");
				}
				buf.append(")");
				
				d.setRefer(buf.toString());
			} else
			{ // Random Select From a Whole Class
				d.setLikelyNull(true); // Might Select entry from a EmptySet
				
				String type = term.getType();
				
				String cond = INTERNAL + count; count ++;
				String n = NUMBER_STMT + type;
				// Select an Position
				token("val " + cond + " = ");
				token("Apply("+n+",(n:Int)=>n==0)");
				token(";");
				
				String var = INTERNAL + count; count ++;
				String lst = ALL_INSTANCE + type;
				token("val " + var + " = ");
				token("If("+cond+",Select(1.0->null.asInstanceOf["+type+"]),");
				if(figaro.getClass(type).randomNumber())
				{ 
					token("Apply(IntSelector(" + n + ")," + lst + ", ");
					token("(pos:Int,lst:Seq["+type+"])=>lst(pos)))");
				} else
				{
					token("Apply(IntSelector(" + n + "), ");
					token("(pos:Int)=>"+lst+"(pos)))");
				}
				token(";");
				
				d.setRefer(var);
			}
		} else
		if(d.getOrigin().equals("Categorical"))
		{
			/*
			 *  Categorical({ symbol -> double})
			 *  
			 *  Check If We Need Chain!
			 */
			MappingTerm term = (MappingTerm)d.getParam(0);
			
			if(term.getFromNull())
				d.setLikelyNull(true);
			
			boolean chain = false;
			for(int i=0;i<term.paramSize();++i)
				if(term.getFrom(i).isRandom()) chain = true;
			
			StringBuffer buf = new StringBuffer();
			
			if(chain) {
				buf.append("Chain(");
				for(int i=0;i<term.paramSize();++i) {
					if(term.getFrom(i).isRandom())
						buf.append(term.getFrom(i).getRefer()+",");
					else
						buf.append("Constant("+term.getFrom(i).getRefer()+"),");
				}
				buf.append("(");
				for(int i=0;i<term.paramSize();++i)
				{
					if(i>0) buf.append(",");
					buf.append("_v"+i+":"+term.getFrom(i).getType());
				}
				buf.append(")=>Select(");
				for(int i=0;i<term.paramSize();++i)
				{
					if(i>0)buf.append(",");
					buf.append(term.getTo(i).getRefer()+"->_v"+i);
				}
				buf.append("))");
			}
			else {
				buf.append("Select(");
				for(int i=0;i<term.paramSize();++i) {
					if(i>0)buf.append(','); 
					buf.append(term.getTo(i).getRefer());
					buf.append("->");
					buf.append(term.getFrom(i).getRefer());
				}
				buf.append(")");
			}
			
			d.setRefer(buf.toString());
		} else
		if(d.getOrigin().equals("TabularCPD")){ // CPD
			// Break into Tokens For Display Reason
			// CPD is Compound: No Need Chain
			
			// TODO: Note input parameter might be NULL!!!

			StringBuffer buf = new StringBuffer();
	//		String var = INTERNAL + count; count ++;
	//		token("val "+ var + " = CPD(");
			
			buf.append("CPD(");
			
			Expression param = d.getParam(1);
			if(param instanceof Constant)
			{
				error(-1,-1,"We Do Not Support Constant as Input Parameter for < CPD >");
				return false;
			}
			if(param instanceof ArrayTerm) {
				if(((ArrayTerm)param).getDim() != 1)
				{
					error(-1,-1,"Error Input Array for < CPD >");
					return false;
				}
				ArrayTerm arr = (ArrayTerm)param;
				for(int i=0;i<arr.paramSize();++i)
					//token(arr.getParam(i).getRefer()+",");
					buf.append(arr.getParam(i).getRefer()+",");
			}else
			{
				if(!param.isRandom())
					//token("Constant("+param.getRefer()+"),");
					buf.append("Constant("+param.getRefer()+"),");
				else
					//token(param.getRefer()+",");
					buf.append(param.getRefer()+",");
			}
			MappingTerm map = (MappingTerm)d.getParam(0);
			
			// Note that the input Parameter Might be NULL and not even matched!
			if(map.getToNull() 
					|| (param.likelyNull() && !map.getFromNull())) 
				d.setLikelyNull(true);
			
			for(int i=0;i<map.paramSize();++i)
			{
				buf.append(map.getFrom(i).getRefer()+"->"+map.getTo(i).getRefer());
				if(i < map.paramSize() - 1) buf.append(',');
			}
			buf.append(")");
			//token(")");token(";");
		
			if(param.likelyNull())
				d.setRefer(buf.toString());
			else
			{
				String var = INTERNAL + count; count ++;
				token("val "+ var + " = ");
				token(buf.toString());
				token(";");
				d.setRefer(var);
			}
		} else // General Case
		if(d.getOrigin().equals("UniformInt"))
		{
			Expression left = d.getParam(0), right = d.getParam(1);
			if((left instanceof Constant) && (right instanceof Constant))
			{ // Simplest Case
				int L = ((Constant)left).intValue(), R = ((Constant)right).intValue();
				int len = R - L + 1;
				String num = INTERNAL + count; count ++;
				token("val "+num+" = IntSelector(Constant("+len+"))"); token(";");
				String var = INTERNAL + count; count ++;
				token("val "+var+" = Apply("+num+",(num:Int)=>num+"+L+")");token(";");
				
				d.setRefer(var);
			} else
			{ // General Method
				String L,R;
				if(!left.isRandom()) {
					L = INTERNAL + count; count ++;
					token("val " + L + " = Constant( " + left.getRefer() + " )");
					token(";");
				} else
					L = left.getRefer();
				if(!right.isRandom()) {
					R = INTERNAL + count; count ++;
					token("val " + R + " = Constant( " + right.getRefer() + " )");
					token(";");
				} else
					R = right.getRefer();
				
				String len = INTERNAL + count; count ++;
				token("val " + len + " = Apply(" + L + ","+R+",(L:Int,R:Int)=>R-L+1)");
				token(";");
				
				String num = INTERNAL + count; count ++;
				token("val "+num+" = IntSelector("+num+")"); token(";");
				
				String var = INTERNAL + count; count ++;
				token("val " + var + " = Apply("+L+","+num+",(L:Int,num:Int)=>L+num)");
				token(";");
				
				d.setRefer(var);
			}
		}
		else
		{	
			String name = d.getSymbol();
			if(d.getOrigin().equals("UniformReal"))
				name = "library.atomic.continuous.Uniform";
			
			boolean chain = false;
			for(int i=0;i<d.paramSize();++i)
				if(d.getParam(i).isRandom()) chain = true;
			
			StringBuffer buf = new StringBuffer();
			
			if(chain)
			{
				buf.append("Chain(");
				for(int i=0;i<d.paramSize();++i)
					if(d.getParam(i).isRandom())
						buf.append(d.getParam(i).getRefer()+",");
					else
						buf.append("Constant("+d.getParam(i).getRefer()+",");
				buf.append("(");
				for(int i=0;i<d.paramSize();++i)
				{
					if(i>0)buf.append(",");
					buf.append("_v"+i+":"+d.getParam(i).getType());
				}
				buf.append(")=>"+name+"(");
				for(int i=0;i<d.paramSize();++i)
				{
					if(i>0)buf.append(",");
					buf.append("_v"+i);
				}
				buf.append("))");
			} else 
			{
				buf.append(name);
				buf.append('(');
			
				for(int i=0;i<d.paramSize();++i) {
					if(i>0) buf.append(',');
					buf.append(d.getParam(i).getRefer());
				}
			
				buf.append(")");
			}
			
			d.setRefer(buf.toString());
		}
		
		return true;
	}
	
	boolean transFuncCall(FunctionCallTerm func)
	{
		for(int i=0;i<func.paramSize();++i) {
			if(!transExpr(func.getParam(i))) return false;
			if(func.getParam(i).likelyNull())
			{// TODO: Just Do Warning!!!!
			// Since this should be prevented by author himself
				System.out.println("Warning: make sure input of FunctionCall is not NULL: " + func.toString() );
				//error(-1,-1,"Parameter of FunctionCall < " + func.getFunc() + " > cannot be NULL!");
				//return false;
			}
		}
		
		func.setRandom(true);
		
		if(func.getFunc().equals("isEmptyString"))
		{
			String str = func.getParam(0).getRefer();
			String var;
			if(func.getParam(0).likelyNull()) {
				func.setLikelyNull(true);
				var = "Apply("+str+",(string:String)=>str.length()==0)";
			}else{
				var = INTERNAL + count; count ++;
				token("val "+var+" = Apply("+str+",(str:String)=>str.length()==0)");
				token(";");
			}
			func.setRefer(var);
		} else // Fix Function or Feature
		if(figaro.getFuncCategory(func.getFunc()) == Figaro.FUNC_FIX_FUNCTION){ // fix Function
			String var = INTERNAL + count; count ++;
			token("val "+var+" = Apply(");
			for(int i=0;i<func.paramSize();++i)
				if(func.getParam(i).isRandom())
					token(func.getParam(i).getRefer()+",");
				else
					token("Constant("+func.getParam(i).getRefer()+"),");
			StringBuffer buf = new StringBuffer();
			buf.append("(");
			for(int i=0;i<func.paramSize();++i)
			{
				if(i>0)buf.append(',');
				buf.append("P"+i+":"+func.getParam(i).getType());
			}
			
			buf.append(")=>"+BLOG_PREFIX+func.getFunc()+"(");
			
			for(int i=0;i<func.paramSize();++i)
			{
				if(i>0) buf.append(',');
				buf.append("P"+i);
			}
			buf.append(")");
			token(buf.toString()); token(";");
			
			func.setRefer(var);
		}
		else
		{ // Feature : Within same class, or cross two classes (must refer to an object)
			if(figaro.getFunction(func.getFunc()).getExpr().likelyNull())
				func.setLikelyNull(true);
			
			if(func.isWithinClass())
				func.setRefer(BLOG_PREFIX+func.getFunc());
			else
			{
				Expression obj = func.getParam(0);
				
				if(!obj.isRandom())
					func.setRefer(obj.getRefer()+"."+
								BLOG_PREFIX + func.getFunc());
				else
				{
					// NOTE: NULL Might Appear
					String var;
					String body = "Chain("+obj.getRefer()+
							",(t:"+figaro.getFeatureBelong(func.getFunc())+")=>t."
							+BLOG_PREFIX + func.getFunc()+")";
					if(obj.likelyNull()) {
						func.setLikelyNull(true);
						var = body;
					} else {
						var = INTERNAL + count; count ++;
						token("val "+var+" = "+body);
						token(";");
					}
					func.setRefer(var);
				}
			}
		}
		
		return true;
	}
	
	boolean transIf(IfTerm t)
	{
		for(int i=0;i<t.paramSize();++i) 
			if(!transExpr(t.getParam(i))) return false;
		
		if(t.getParam(0).likelyNull())
		{
			error(-1,-1,"Condition of If Statement Cannot be NULL!");
			return false;
		}
		
		t.setRandom(true);
		
		if(t.getParam(1).likelyNull() || t.getParam(2).likelyNull())
			t.setLikelyNull(true);
		
		Expression cond = t.getParam(0), then = t.getParam(1), els = t.getParam(2);
		
		StringBuffer buf = new StringBuffer();
		
		buf.append("If(");
		if(cond.isRandom()) buf.append(cond.getRefer()+",");
		else buf.append("Constant("+cond.getRefer()+"),");
		
		if(then.isRandom())  buf.append(then.getRefer()+",");
		else buf.append("Constant("+then.getRefer()+"),");
		
		if(els.isRandom())  buf.append(els.getRefer()+")");
		else { //Note NULL Can appear here
			buf.append("Select(1.0->"+els.getRefer()+"))");
		}
		
		t.setRefer(buf.toString());
		
		return true;
	}
	
	boolean transMapping(MappingTerm map)
	{
		for(int i=0;i<map.paramSize();++i) {
			if(!transExpr(map.getFrom(i)) || !transExpr(map.getTo(i))) return false;
			if(map.getFrom(i).likelyNull())
				map.setFromNull(true);
			if(map.getTo(i).likelyNull())
				map.setToNull(true);
		}
		
		map.setRandom(false);
		
		map.setRefer(""); // DO NOTHING
		
		return true;
	}
	
	boolean transNumberRefer(NumberReference num)
	{
		// No Parameter
		// Number Statement Cannot Be NULL
		
		num.setRandom(true);
		num.setRefer(NUMBER_STMT+num.getBelong());	
		
		return true;
	}
	
	boolean transOpr(OprTerm op)
	{
		for(int i=0;i<op.paramSize();++i) {
			if(!transExpr(op.getParam(i))) return false;
			if(op.getParam(i).likelyNull())
			{
				if(op.getOpr() != OprTerm.EQ && op.getOpr() != OprTerm.NEQ)
				{
					error(-1,-1,"parameter for Operator < " + op.getSymbol() + " > cannot be NULL");
				}
			}
		}
		
		op.setRandom(false);
		
		// Special: Unary and Power
		if(op.isUnary())
		{
			Expression p = op.getParam(0);
			if(!p.isRandom())
			{
				op.setRefer("!"+p.getRefer());
				return true;
			}
			String var = INTERNAL + count; count ++;
			token("val "+var+"=Apply("+p.getRefer()+",(b:Boolean)=>!b)");
			token(";");
			
			op.setRandom(true);
			op.setRefer(var);
			return true;
		}
		Expression left = op.getParam(0), right = op.getParam(1);
		if(op.getOpr() == OprTerm.POWER) // Special Case for Power
		{
			if(!left.isRandom() && !right.isRandom())
			{
				op.setRefer("Math.pow("+left.getRefer()+","+right.getRefer()+")");
				return true;
			}
			String var = INTERNAL + count ; count ++;
			token("val " + var + " = Apply(");
			if(left.isRandom()) token(left.getRefer()+",");
			else token("Constant("+left.getRefer()+"),");
			if(right.isRandom()) token(right.getRefer()+",");
			else token("Constant("+right.getRefer()+"),");
			token("(x:"+left.getType()+",y:"+right.getType()+")=>Math.pow(x,y))"); token(";");
			
			op.setRandom(true);
			op.setRefer(var);
		} else
		{
			if(!left.isRandom() && !right.isRandom())
				op.setRefer(left.getRefer()+op.getSymbol()+right.getRefer());
			else
			{
				//Note : use Select here since NULL may appear
				String var = INTERNAL + count ; count ++;
				token("val " + var + " = Apply(");
				if(left.isRandom()) token(left.getRefer()+",");
				else token("Select(1.0->"+left.getRefer()+"),");
				if(right.isRandom()) token(right.getRefer()+",");
				else token("Select(1.0->"+right.getRefer()+"),");
				token("(x:"+left.getType()+",y:"+right.getType()+")=>x"+op.getSymbol()+"y)"); token(";");
				
				op.setRandom(true);
				op.setRefer(var);
			}
		}
		return true;
	}
	
	boolean transSet(SetTerm set)
	{
		for(int i=0;i<set.paramSize();++i) {
			if(!transExpr(set.getParam(i))) return false;
			if(set.getParam(i).likelyNull())
			{
				error(-1,-1,"Set cannot contain NULL!");
				return false;
			}
		}
		// Do Nothing
		set.setRandom(false);
		set.setRefer("");
		return true;
	}
	
	boolean transVar(Variable var)
	{
		// No Parameter
		/*
		 * 1. Fix Value Reference
		 * 2. Object
		 * 3. Assignment
		 * 4. a Function parameter
		 */
		
		// Function Parameter
		if(funcParams.contains(var.getPrefix()))
			var.setRandom(false);
		else
		// Fix Value Reference
		if(figaro.getFuncCategory(var.getPrefix()) == Figaro.FUNC_FIX_FUNCTION)
			var.setRandom(false);
		else
		// Object
		if(figaro.hasObject(var.getSymbol()))
			var.setRandom(false);
		else
		// Assignment
		if(figaro.getFuncCategory(var.getPrefix()) == Figaro.FUNC_ASSIGNMENT) {
			var.setRandom(true);
			if(figaro.getFunction(var.getPrefix()).getExpr().likelyNull())
				var.setLikelyNull(true);
		}
		else
		{
			error(-1,-1,"Error when processing < Variable > + " + var.getSymbol());
			return false;
		}
		
		var.setRefer(BLOG_PREFIX+var.getSymbol());
		return true;
	}
	
	boolean transExpr(Expression e)
	{
		if(e instanceof ArrayTerm)
			return transArray((ArrayTerm)e);
		else
		if(e instanceof Constant)
			return transConst((Constant)e);
		else
		if(e instanceof Distribution)
			return transDistri((Distribution)e);
		else
		if(e instanceof FunctionCallTerm)
			return transFuncCall((FunctionCallTerm)e);
		else
		if(e instanceof IfTerm)
			return transIf((IfTerm)e);
		else
		if(e instanceof MappingTerm) 
			return transMapping((MappingTerm)e);
		else
		if(e instanceof NumberReference)
			return transNumberRefer((NumberReference)e);
		else
		if(e instanceof OprTerm)
			return transOpr((OprTerm)e);
		else
		if(e instanceof SetTerm) 
			return transSet((SetTerm)e);
		else
		if(e instanceof Variable)
			return transVar((Variable)e);
		return true;
	}
	
	boolean transFixFunc(FigaroFunction func)
	{
		if(func.paramSize() == 0)
		{ // Define Constant Value
			if(!transExpr(func.expr)) return false;
			token("val " + (BLOG_PREFIX+func.getSymbol()) + " = " + func.expr.getRefer());
			token(";");
		} 
		else
		{ // FixFunction
			funcParams.clear();
			
			StringBuffer buf = new StringBuffer();
			buf.append("def " + BLOG_PREFIX+func.getSymbol() + "(");
			for(int i=0;i<func.paramSize();++i) {
				if(i>0) buf.append(',');
				
				funcParams.add(func.getParam(i).getSymbol());
				buf.append(func.getParam(i).getSymbol()+":"+func.getParam(i).getType());
			}
			buf.append("):"+func.getType());
			
			token(buf.toString());
			
			token("{");
			
			if(!transExpr(func.getExpr())) return false;
			
			token("return "+func.getExpr().getRefer()); token(";");
			token("}"); token(";");
			
			funcParams.clear();
		}
		
		if(func.getExpr().likelyNull())
		{
			error(-1,-1,"Illegal!! Output of Fix Function < " + func.getSymbol() + " > might be NULL!");
			return false;
		}
		
		return true;
	}
	
	boolean translateFixFunction()
	{
		for(FigaroFunction func : figaro.getFixFunction())
			if(!transFixFunc(func)) return false;
		return true;
	}
	
	
	boolean translateByOrder()
	{
		ArrayList<String> order = figaro.getOrder();
		
		for(int i=0;i<order.size();++i)
		{
			/*  3 Types:
			 *     1. class name
			 *     2. feature name
			 *     3. assignment name
			 *     4. # + class name
			 */
			String name = order.get(i);
			// class name
			if(figaro.hasClass(name))
			{
				if(name.startsWith(BLOG_PREFIX) ||
					name.startsWith(FIGARO_PREFIX) || name.startsWith(ALL_INSTANCE) ||
					name.startsWith(INTERNAL) || name.startsWith(NUMBER_STMT))
				{
					error(-1,-1,"Illegal Prefix for Class Name < " + name + " >. Please Change another name!");
					return false;
				}
				
				token("class " + name + "(__name:Symbol) extends ElementCollection ");
				token("{");
				token("val _name=__name");token(";");
			} else
			if(name.startsWith("#")) // Number Statement
			{
				token("}"); token(";"); // Finish the Declaration of Class
				
				name = name.substring(1);
				ArrayList<String> distinct = figaro.getClass(name).getDistinctList();
				ArrayList<Integer> distinctSize = figaro.getClass(name).getDistinctSizeList();
				
				for(int j=0;j<distinct.size();++j)
				{
					String p = distinct.get(j);
					int sz = distinctSize.get(j);
					if(sz == 1)
						token("val " +BLOG_PREFIX+ p + " = new " + name+"(\'"+p+")");
					else
						token("val " +BLOG_PREFIX+ p + " = Array.fill("+sz+")(new "+ name+"(\'#) )");
					token(";");
				}
				
				// Build Number Statement
				Expression expr = figaro.getClass(name).getNumber();
				if(!transExpr(expr))return false;
				
				token("val " + NUMBER_STMT + name + " = ");
				if(expr.isRandom()) token(expr.getRefer());
				else token("Constant("+expr.getRefer()+")");
				token(";");
				
				// Build Array of ALL Instances
				token("val " + ALL_INSTANCE + name + " = ");
				if(figaro.getClass(name).randomNumber())
					token("MakeList("+NUMBER_STMT+name+",()=>Select(1.0->new "+ name+"))");
				else
				{
					token("Array(");
					boolean flag = false;
					for(int j=0;j<distinct.size();++j) {
						if(distinctSize.get(j) == 1)
						{
							if(flag)token(",");
							else flag=true;
							token(BLOG_PREFIX+distinct.get(j));
						} else
						{
							for(int k=0;k<distinctSize.get(j);++k)
							{
								if(flag)token(",");
								else flag=true;	
								token(BLOG_PREFIX+distinct.get(j)+"("+k+")");
							}
						}
					}
					token(")");
				}
				
				token(";"); token("\n");
			}
			else // Feature
			if(figaro.getFuncCategory(name) == Figaro.FUNC_FEATURE) 
			{
				FigaroFunction func = figaro.getFunction(name);
				Expression expr = func.getExpr();
				
				funcParams.clear();
				funcParams.add(func.getParam(0).getSymbol());
				
				if(!transExpr(expr)) return false;
				
				funcParams.clear();
				
				if(expr.isRandom())
					token("val " +BLOG_PREFIX+ name + " = " + expr.getRefer());
				else
					token("val " +BLOG_PREFIX+ name + " = Constant(" + expr.getRefer()+")");
				token(";");
					
			} else // ASSIGNMENT
			{
				Expression expr =  figaro.getFunction(name).getExpr();
				
				if(!transExpr(expr)) return false;
				
				if(expr.isRandom())
					token("val " + BLOG_PREFIX+ name + " = " + expr.getRefer());
				else
					token("val " + BLOG_PREFIX+ name + " = Constant(" + expr.getRefer()+")");
				token(";");
			}
		}
		
		return true;
	}
	
	boolean translateEvidence()
	{
		ArrayList<FigaroEvidence> evidence = figaro.getEvidenceList();
		
		for(int i=0;i<evidence.size();++i)
		{
			if(!transExpr(evidence.get(i).getLeft()) ||
				!transExpr(evidence.get(i).getRight())) return false;
			
			Expression left = evidence.get(i).getLeft(),
						right = evidence.get(i).getRight();
			
			if(left.isNull() || right.isNull())
			{
				error(-1,-1,"Illegal Evidence!! Parameter of Evidence cannot be NULL!");
				return false;
			}
			
			// Deal With Special Case
			if((left instanceof Variable) && (right instanceof Constant))
			{
				token(left.getRefer() +".observe("+right.getRefer()+")");
				token(";");
				continue;
			}
			
			String var = INTERNAL + count; count ++;
			
			String left_ref = (left.isRandom() ? left.getRefer() : "Select(1.0->"+left.getRefer()+")");
			String right_ref = (right.isRandom() ? right.getRefer() : "Select(1.0->"+right.getRefer()+")");
			
			token("val " + var + " = Apply(" + left_ref + ","+right_ref+
									",(a:"+left.getType()+",b:"+right.getType()+")=>a==b)");
			token(";");
			
			token(var+".observe(true)"); token(";");
		}
		
		return true;
	}
	
	boolean translateQuery()
	{
		//TODO: Currently Only Use Importance Sampling
		
		token("def main(args : Array[String]) "); token("{");
		
		ArrayList<FigaroQuery> query = figaro.getQueryList();
		
		for(int i=0;i<query.size();++i)
		{
			Expression expr = query.get(i).getExpr();
			if(!transExpr(expr))return false;
			
			if(expr.isNull())
			{
				error(-1,-1,"Illegal Query!! Query Cannot be NULL!");
				return false;
			}
			
			String var = INTERNAL + count; count ++;
			
			token("val "+var+" = ");
			if(expr.isRandom()) token(expr.getRefer());
			else token("Constant("+expr.getRefer()+")");
			token(";");
			String alg = FIGARO_PREFIX + i;
			
			token("val " + alg + " = Importance(50000," + var+")"); token(";");
			token(alg+".start()");token(";");
			token(alg+".stop()");token(";");
			token("println(\"Distribution of " + query.get(i).getExpr().toString() + ":\")"); token(";");
			
			String distr = FIGARO_PREFIX+"_DIS"+i;
			token("val " + distr + " = " + alg + ".distribution("+var+")"); token(";");
			token("for ( i <- 0 until " + distr + ".length) "); token("{");
			if(figaro.hasClass(expr.getType()))
				token("println(\"( \"+ " + distr + "(i)._1 +\" , \" + "+distr+"(i)._2._name +\" )\")");
			else
				token("println(\"\" + " + distr + "(i))"); 
			token(";");
			token("}"); token(";");
			token(alg+".kill()"); token(";");
		}
		
		token("\n");
		token("}"); token(";");
		return true;
	}
	
	/* Translate Order
	 *    1. Fix Value/Function
	 *    2. By order
	 *    3. Evidence
	 *    4. Query
	 */
	public boolean translate()
	{
		count = 0;
		
		token("import com.cra.figaro._"); token(";");
		token("import language._"); token(";");
		token("import algorithm.sampling._"); token(";");
		token("import library.compound._"); token(";");
		token("import library.atomic.continuous._"); token(";");
		token("import library.atomic.discrete._"); token(";");
		token("\n");

		token("object BLOG_FIGARO_TRANSLATED_RES "); token("{");
		token("Universe.createNew()"); token(";");
		token("\n");
		
		if(!translateFixFunction()) return false;
		if(!translateByOrder()) return false;
		if(!translateEvidence()) return false;
		if(!translateQuery()) return false;
		
		token("}"); token(";");
		
		figaro.setTokens(tokens);
		return true;
	}
		
	void token(String t){tokens.add(t);}
	
	public ArrayList<String> getTokenList(){return tokens;}
	
	void error(int line, int col, String msg) {
		errorMsg.error(line, col, msg);
	}
	
	private final static String FIGARO_PREFIX = "F_";
	
	private final static String BLOG_PREFIX = "B_"; // Only For Variables(Assignments), Features and Fix Functions
	// Class Names DO NOT take this prefix
	
	private final static String INTERNAL = "_IV";
	private final static String NUMBER_STMT = "B_N_";
	private final static String ALL_INSTANCE = "B_AI_";
}
