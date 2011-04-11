package uk.ac.imperial.doc.pctmc.expressions;

import java.util.Map;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariableUnfolder;


public class ExpressionVariableUnfolderPCTMC extends ExpressionVariableUnfolder implements IPopulationVisitor,ICombinedProductExpressionVisitor{

	public ExpressionVariableUnfolderPCTMC(
			Map<ExpressionVariable, AbstractExpression> variables) {
		super(variables);
	}

	@Override
	public void visit(ExpressionVariable e) {
		AbstractExpression rhs = variables.get(e);
		if (rhs!=null) rhs.accept(this);
		else {
			result = e; 
		}
	}

	@Override
	public void visit(PopulationExpression e) {
		result = e; 
	}

	@Override
	public void visit(CombinedProductExpression e) {
		result = e; 
	}
}