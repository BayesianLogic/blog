package blog.model;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import blog.Substitution;
import blog.common.UnaryProcedure;
import blog.common.Util;
import blog.common.numerical.JamaMatrixLib;
import blog.common.numerical.MatrixLib;
import blog.sample.EvalContext;
import blog.world.DefaultPartialWorld;

/**
 * Representation of a matrix within BLOG.  
 * 
 * @author awong
 * @date November 5, 2012
 */
public class MatrixSpec extends ArgSpec {
	
	List<ArgSpec> matrixContents;
	MatrixLib matrix;
	Boolean compiled;
	
	public MatrixSpec(List<ArgSpec> values) {
		matrixContents = values;
		compiled = false;
	}
	
	/**
	 * Iterates through the values contained in the matrix and converts
	 * them into a double array, from which a matrix is generated
	 * 
	 * @param values a List (potentially once-nested) of ArgSpecs
	 * @return a double array containing the contents of the matrix
	 */
	private double[][] getValues(List<ArgSpec> values) {
		ArgSpec first = values.get(0);
		if (first instanceof ListSpec) {
			return getValues2D(values);
		}
		else {
			return getValues1D(values);
		}
	}
	
	private double[][] getValues2D(List<ArgSpec> values) {
		ListSpec first = (ListSpec) values.get(0);
		int num_rows = values.size();
		int num_cols = first.elements.size();
		
		double[][] matVals = new double[num_rows][num_cols];
		for (int i = 0; i < num_rows; i++) {
			List<ArgSpec> rowList = ((ListSpec) values.get(i)).elements;
			if (rowList.size() != num_cols) {
				Util.fatalError("Matrix is not square!");
			}
			
			for (int j = 0; j < num_cols; j++) {
				ArgSpec elementSpec = rowList.get(j);
				if (!(elementSpec instanceof FuncAppTerm)) {
					Util.fatalError("Matrix can only contain constants!");
				}
				FuncAppTerm elementTerm = (FuncAppTerm) elementSpec;
				Number val = (Number) elementTerm.evaluate(new DefaultPartialWorld());
				matVals[i][j] = val.doubleValue();
			}
		}
		
		return matVals;
	}
	
	private double[][] getValues1D(List<ArgSpec> values) {
		double[][] matVals = new double[1][values.size()];
		for (int j = 0; j < values.size(); j++) {
			ArgSpec elementSpec = values.get(j);
			if (!(elementSpec instanceof FuncAppTerm)) {
				Util.fatalError("Matrix can only contain constants!");
			}
			FuncAppTerm elementTerm = (FuncAppTerm) elementSpec;
			Number val = (Number) elementTerm.evaluate(new DefaultPartialWorld());
			matVals[0][j] = val.doubleValue();
		}
		
		return matVals;
	}
	
	/**
	 * To compile a matrix, no assembly is required.
	 */
	public int compile(LinkedHashSet callStack) {
		callStack.add(this);
		
		for (ArgSpec spec : matrixContents) {
			spec.compile(callStack);
		}
		double[][] contents = getValues(matrixContents);
		matrix = new JamaMatrixLib(contents);
		
		callStack.remove(this);
		return 0;
	}

	@Override
	public Object evaluate(EvalContext context) {
		// TODO FIX FIX FIX
		return matrix;
	}

	@Override
	public boolean containsRandomSymbol() {
		return false;
	}
	
	public Object getValueIfNonRandom() {
		return matrix;
	}

	@Override
	public boolean checkTypesAndScope(Model model, Map scope) {
		// TODO FIX FIX FIX
		return true;
	}

	@Override
	public ArgSpec find(Term t) {
		// TODO FIX FIX FIX
		return null;
	}

	@Override
	public void applyToTerms(UnaryProcedure procedure) {
	}

	@Override
	public ArgSpec replace(Term t, ArgSpec another) {
		return null;
	}

	@Override
	public ArgSpec getSubstResult(Substitution subst, Set<LogicalVar> boundVars) {
		return null;
	}
}
