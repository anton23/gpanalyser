package uk.ac.imperial.doc.jexpressions.expanded;

import java.util.Set;

import uk.ac.imperial.doc.jexpressions.constants.ConstantExpression;
import uk.ac.imperial.doc.jexpressions.constants.visitors.ExpressionTransformerWithConstants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;

import com.google.common.collect.Multiset;

public class ExpandingExpressionTransformer extends ExpressionTransformerWithConstants{
	private ExpandedExpression result;

	
	@Override
	public void visit(ConstantExpression e) {
		result = new UnexpandableExpression(e);
	}
	
	@Override
	public void visit(DoubleExpression e) {
		result = new UnexpandableExpression(e);
	}
}