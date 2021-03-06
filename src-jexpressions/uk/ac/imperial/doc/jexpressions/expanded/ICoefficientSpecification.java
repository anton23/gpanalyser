package uk.ac.imperial.doc.jexpressions.expanded;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;

public interface ICoefficientSpecification {
	public AbstractExpression normaliseCoefficient(AbstractExpression e);

	public boolean isCoefficient(AbstractExpression e);

	public boolean isOne(AbstractExpression e);

	public boolean isZero(AbstractExpression e);
}
