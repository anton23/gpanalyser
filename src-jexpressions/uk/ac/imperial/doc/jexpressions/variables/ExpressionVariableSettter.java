package uk.ac.imperial.doc.jexpressions.variables;

import java.util.Map;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.visitors.ExpressionWalkerWithConstants;


public class ExpressionVariableSettter extends ExpressionWalkerWithConstants implements IExpressionVariableVisitor{
	protected Map<ExpressionVariable, AbstractExpression> variables;

	public ExpressionVariableSettter(
			Map<ExpressionVariable, AbstractExpression> variables) {
		super();
		this.variables = variables;
	}

	@Override
	public void visit(ExpressionVariable e) {
		e.setUnfolded(variables.get(e));  
	}

}
