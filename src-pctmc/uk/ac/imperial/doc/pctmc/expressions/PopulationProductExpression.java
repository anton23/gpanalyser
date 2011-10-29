package uk.ac.imperial.doc.pctmc.expressions;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IExpressionVisitor;

/**
 * AbstractExpression wrapper for a PopulationProduct.
 * 
 * @author Anton Stefanek
 * 
 */
public class PopulationProductExpression extends AbstractExpression {
	protected PopulationProduct product;

	public PopulationProductExpression(PopulationProduct product) {
		super();
		this.product = product;
	}

	@Override
	public void accept(IExpressionVisitor v) {
		if (v instanceof IPopulationProductVisitor) {
			((IPopulationProductVisitor) v).visit(this);
		} else {
			throw new AssertionError(
					"Unsupported visit to a PopulationProductExpression!");
		}
	}

	@Override
	public String toString() {
		return "E[" + product.toString() + "]";
	}

	@Override
	public int hashCode() {
		return product.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof PopulationProductExpression))
			return false;
		PopulationProductExpression asProductCount = (PopulationProductExpression) o;
		return this.product.equals(asProductCount.getProduct());
	}

	public PopulationProduct getProduct() {
		return product;
	}
}
