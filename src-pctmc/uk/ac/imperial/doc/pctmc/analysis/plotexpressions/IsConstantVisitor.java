package uk.ac.imperial.doc.pctmc.analysis.plotexpressions;

import uk.ac.imperial.doc.jexpressions.constants.visitors.ExpressionWalkerWithConstants;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.ICombinedProductExpressionVisitor;

public class IsConstantVisitor extends ExpressionWalkerWithConstants implements
		ICombinedProductExpressionVisitor {

	private boolean constant = true;
	
	@Override
	public void visit(CombinedProductExpression e) {
		constant = false;
	}

	public boolean isConstant() {
		return constant;
	}
}
