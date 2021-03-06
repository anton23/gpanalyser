package uk.ac.imperial.doc.pctmc.analysis.plotexpressions;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ExpressionWrapper;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;

// only works for expressions of the form  (c_1*)?CM_1 + (c_2*)? CM_2 ...
public class MeanOfLinearCombinationExpression extends ExpressionWrapper {

	private List<AbstractExpression> coefficients;
	private List<CombinedPopulationProduct> combinedProducts;

	public MeanOfLinearCombinationExpression(AbstractExpression e,
			Map<ExpressionVariable, AbstractExpression> var) {
		super(e);
		AbstractExpression lsum = e;
		if (e instanceof ExpressionVariable) {
			if (((ExpressionVariable) e).getUnfolded() != null) {
				lsum = ((ExpressionVariable) e).getUnfolded();
			} else {
				lsum = var.get(e);
			}
		}
		coefficients = new LinkedList<AbstractExpression>();
		combinedProducts = new LinkedList<CombinedPopulationProduct>();
		List<AbstractExpression> summands = new LinkedList<AbstractExpression>();
		if (lsum instanceof ProductExpression
				|| lsum instanceof CombinedProductExpression) {
			summands.add(lsum);
		}
		if (lsum instanceof SumExpression) {
			summands.addAll(((SumExpression) lsum).getSummands());
		}
		for (AbstractExpression s : summands) {
			if (s instanceof CombinedProductExpression) {
				coefficients.add(new DoubleExpression(1.0));
				combinedProducts.add(((CombinedProductExpression) s)
						.getProduct());
			}
			if (s instanceof ProductExpression) {
				List<AbstractExpression> terms = ((ProductExpression) s)
						.getTerms();
				AbstractExpression coefficient;			
									
				CombinedPopulationProduct product = null;
				List<AbstractExpression> coefficientTerms = new LinkedList<AbstractExpression>();
				for (AbstractExpression t:terms) {
					if (t instanceof CombinedProductExpression) {
						if (product != null) {
							throw new AssertionError("Expression " + e
									+ " is not a linear combination of moments!");
						} else {
							product = ((CombinedProductExpression) t).getProduct();;
						}
					} else {
						coefficientTerms.add(t);
					}
				}
				coefficient = ProductExpression.create(coefficientTerms);
				
				IsConstantVisitor visitor = new IsConstantVisitor();
				coefficient.accept(visitor);
				if (!visitor.isConstant()) {
					throw new AssertionError("Expression " + e
							+ " is not a linear combination of moments!");
				}
				coefficients.add(coefficient);
				if (product == null) {
					product = CombinedPopulationProduct.getConstantProduct();
				}
				combinedProducts.add(product);
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 0;
		result = prime * result
				+ ((coefficients == null) ? 0 : coefficients.hashCode());
		result = prime
				* result
				+ ((combinedProducts == null) ? 0 : combinedProducts.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (getClass() != obj.getClass())
			return false;
		MeanOfLinearCombinationExpression other = (MeanOfLinearCombinationExpression) obj;
		if (coefficients == null) {
			if (other.coefficients != null)
				return false;
		} else if (!coefficients.equals(other.coefficients))
			return false;
		if (combinedProducts == null) {
			if (other.combinedProducts != null)
				return false;
		} else if (!combinedProducts.equals(other.combinedProducts))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "E[" + internalExpression.toString() + "]";
	}

	public List<AbstractExpression> getCoefficients() {
		return coefficients;
	}

	public List<CombinedPopulationProduct> getCombinedProducts() {
		return combinedProducts;
	}

	public AbstractExpression getInternalExpression() {
		return internalExpression;
	}

}
