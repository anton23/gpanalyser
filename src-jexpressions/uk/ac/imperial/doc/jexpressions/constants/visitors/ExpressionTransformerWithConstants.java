package uk.ac.imperial.doc.jexpressions.constants.visitors;

import uk.ac.imperial.doc.jexpressions.constants.ConstantExpression;
import uk.ac.imperial.doc.jexpressions.constants.IConstantExpressionVisitor;
import uk.ac.imperial.doc.jexpressions.expressions.visitors.ExpressionTransformer;

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
