package examples.odes;

import java.util.Map;

import uk.ac.imperial.doc.jexpressions.expressions.visitors.ExpressionEvaluator;

public class ExpressionEvaluatorWithODEVariables extends ExpressionEvaluator implements IODEVariableExpressionVisitor{
	
	private double[] values; 
	private Map<String, Integer> variableIndex; 
	
	

	public ExpressionEvaluatorWithODEVariables(double[] values,
			Map<String, Integer> variableIndex) {
		super();
		this.values = values;
		this.variableIndex = variableIndex;
	}



	@Override
	public void visit(ODEVariableExpression e) {
		result = values[variableIndex.get(e.getName())];
	}
	

}
