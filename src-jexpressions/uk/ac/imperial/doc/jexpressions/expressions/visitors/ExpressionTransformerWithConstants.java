package uk.ac.imperial.doc.jexpressions.expressions.visitors;

import uk.ac.imperial.doc.jexpressions.constants.ConstantExpression;
import uk.ac.imperial.doc.jexpressions.constants.IConstantExpressionVisitor;

/**
 * An extesions of ExpressionTransformer supporting constants.
 * @author as1005
 *
 */
public class ExpressionTransformerWithConstants extends ExpressionTransformer implements
		IConstantExpressionVisitor {

	@Override
	public void visit(ConstantExpression e) {
		result = e;
	}

}
