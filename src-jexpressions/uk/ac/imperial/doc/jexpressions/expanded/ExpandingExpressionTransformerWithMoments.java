package uk.ac.imperial.doc.jexpressions.expanded;

import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.ICombinedProductExpressionVisitor;

public class ExpandingExpressionTransformerWithMoments extends ExpandingExpressionTransformer implements ICombinedProductExpressionVisitor{

	public ExpandingExpressionTransformerWithMoments(
			ICoefficientSpecification normaliser) {
		super(normaliser);
	}

	@Override
	public void visit(CombinedProductExpression e) {
		result = new UnexpandableExpression(e, normaliser);
	}
}
