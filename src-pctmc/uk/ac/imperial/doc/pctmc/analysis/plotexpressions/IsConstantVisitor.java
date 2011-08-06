package uk.ac.imperial.doc.pctmc.analysis.plotexpressions;

import uk.ac.imperial.doc.jexpressions.constants.visitors.ExpressionWalkerWithConstants;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.ICombinedProductExpressionVisitor;

public class IsConstantVisitor extends ExpressionWalkerWithConstants implements ICombinedProductExpressionVisitor{
	@Override
	public void visit(CombinedProductExpression e) {
		constant = false; 
	}

	private boolean constant = true;

	public boolean isConstant() {
		return constant;
	}
}
