package uk.ac.imperial.doc.jexpressions.variables;

import java.util.Map;

import uk.ac.imperial.doc.jexpressions.constants.visitors.ExpressionWalkerWithConstants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;


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
