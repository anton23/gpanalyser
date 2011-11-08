package uk.ac.imperial.doc.jexpressions.expanded;

import java.util.Set;

import uk.ac.imperial.doc.jexpressions.constants.visitors.ExpressionWalkerWithConstants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.ICombinedProductExpressionVisitor;

public class IsDoubleOrConstantExpressionWalker extends
		ExpressionWalkerWithConstants implements
		ICombinedProductExpressionVisitor, IExpandedExpressionVisitor {

	private boolean is;

	public IsDoubleOrConstantExpressionWalker() {
		super();
		this.is = true;
	}

	public boolean isIs() {
		return is;
	}

	@Override
	public void visit(CombinedProductExpression e) {
		is = false;
	}

	@Override
	public void visit(ExpandedExpression e) {
		e.toAbstractExpression().accept(this);
	}

}
