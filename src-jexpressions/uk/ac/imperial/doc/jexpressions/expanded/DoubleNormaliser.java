package uk.ac.imperial.doc.jexpressions.expanded;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;

public class DoubleNormaliser implements ICoefficientSpecification {

	@Override
	public AbstractExpression normaliseCoefficient(AbstractExpression e) {
		return e;
	}

	@Override
	public boolean isCoefficient(AbstractExpression e) {
		return (e instanceof DoubleExpression);
	}
	
	

}
