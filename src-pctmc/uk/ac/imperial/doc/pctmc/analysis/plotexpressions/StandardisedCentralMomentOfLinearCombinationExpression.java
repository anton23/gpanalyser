package uk.ac.imperial.doc.pctmc.analysis.plotexpressions;

import java.util.List;
import java.util.Map;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ExpressionWrapper;
import uk.ac.imperial.doc.jexpressions.expressions.PowerExpression;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;

public class StandardisedCentralMomentOfLinearCombinationExpression extends
		ExpressionWrapper {

	private List<AbstractExpression> coefficients;
	private List<CombinedPopulationProduct> combinedProducts;

	private AbstractExpression originalExpression;
	private int order;

	public StandardisedCentralMomentOfLinearCombinationExpression(
			AbstractExpression e, int order,
			Map<ExpressionVariable, AbstractExpression> var) {
		super(createExpression(e, order, var));
		originalExpression = e;
		this.order = order;
	}

	public final AbstractExpression getOriginalExpression()
	{
		return originalExpression;
	}

	public final int getOrder()
	{
		return order;
	}
	
	private static AbstractExpression createExpression(AbstractExpression e,
			int order, Map<ExpressionVariable, AbstractExpression> var) {
		MeanOfLinearCombinationExpression mean = new MeanOfLinearCombinationExpression(
				e, var);
		if (order == 2) {return PowerExpression.create(CentralMomentOfLinearCombinationExpression.createExpression(mean.getCoefficients(),mean.getCombinedProducts(),2),new DoubleExpression(0.5));}
		return DivExpression.create(CentralMomentOfLinearCombinationExpression
				.createExpression(mean.getCoefficients(), mean
						.getCombinedProducts(), order), PowerExpression.create(
				CentralMomentOfLinearCombinationExpression.createExpression(
						mean.getCoefficients(), mean.getCombinedProducts(), 2),
				new DoubleExpression((double) order / 2.0)));
	}

	@Override
	public String toString() {
		if (order == 3)
			return "Skew[" + originalExpression.toString() + "]";
		if (order == 4)
			return "Kurt[" + originalExpression.toString() + "]";
		return "SCM[" + originalExpression.toString() + "," + order + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StandardisedCentralMomentOfLinearCombinationExpression other = (StandardisedCentralMomentOfLinearCombinationExpression) obj;
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
}
