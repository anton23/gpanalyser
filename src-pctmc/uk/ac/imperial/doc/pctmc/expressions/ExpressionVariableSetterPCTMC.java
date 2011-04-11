package uk.ac.imperial.doc.pctmc.expressions;

import java.util.Map;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariableSettter;


public class ExpressionVariableSetterPCTMC extends ExpressionVariableSettter implements ICombinedProductExpressionVisitor{
	@Override
	public void visit(CombinedProductExpression e) {
	}

	public ExpressionVariableSetterPCTMC(
			Map<ExpressionVariable, AbstractExpression> variables) {
		super(variables);
	}

	@Override
	public void visit(ExpressionVariable e) {
		AbstractExpression unfolded = variables.get(e);
		if (unfolded!=null) e.setUnfolded(unfolded);  
	}

}
