package uk.ac.imperial.doc.jexpressions.expressions.visitors;

import uk.ac.imperial.doc.jexpressions.constants.ConstantExpression;
import uk.ac.imperial.doc.jexpressions.constants.IConstantExpressionVisitor;

/**
 * An extension of ExpressionWalker supporting constants.
 * @author as1005
 *
 */
public class ExpressionWalkerWithConstants extends ExpressionWalker implements
		IConstantExpressionVisitor {

	@Override
	public void visit(ConstantExpression e) {}

}
