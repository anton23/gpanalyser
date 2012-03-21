package uk.ac.imperial.doc.pctmc.analysis.plotexpressions;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ExpressionWrapper;
import uk.ac.imperial.doc.jexpressions.expressions.PowerExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;
import uk.ac.imperial.doc.jexpressions.expressions.UMinusExpression;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.utils.Multinomial;

import com.google.common.collect.Multiset;

public class CentralMomentOfLinearCombinationExpression extends
		ExpressionWrapper {

	private AbstractExpression originalExpression;
	private int order;

	private List<AbstractExpression> coefficients;
	private List<CombinedPopulationProduct> combinedProducts;

	public CentralMomentOfLinearCombinationExpression(AbstractExpression e,
			int order, Map<ExpressionVariable, AbstractExpression> var) {
		super(CentralMomentOfLinearCombinationExpression
				.createExpression(new MeanOfLinearCombinationExpression(e, var)
						.getCoefficients(),
						new MeanOfLinearCombinationExpression(e, var)
								.getCombinedProducts(), order));
		this.originalExpression = e;
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
	
	public static AbstractExpression createExpression(
			List<AbstractExpression> coefficients,
			List<CombinedPopulationProduct> combinedMoments, int order) {
		for (int i = 0; i < combinedMoments.size(); i++) {
			CombinedPopulationProduct c = combinedMoments.get(i);
			AbstractExpression coefficient = coefficients.get(i);
			coefficients.add(new UMinusExpression(ProductExpression.create(
					CombinedProductExpression.create(c), coefficient)));
		}
		List<AbstractExpression> sum = new LinkedList<AbstractExpression>();
		List<Multiset<Integer>> partitions = Multinomial.getPartitions(order,
				combinedMoments.size() * 2);
		for (Multiset<Integer> partition : partitions) {
			int multinomialCoefficient = Multinomial.getMultinomialCoefficient(
					order, partition);
			List<AbstractExpression> terms = new LinkedList<AbstractExpression>();
			CombinedPopulationProduct moment = null;
			terms.add(new DoubleExpression((double) multinomialCoefficient));
			for (Multiset.Entry<Integer> entry : partition.entrySet()) {
				AbstractExpression coefficientPower = PowerExpression.create(
						coefficients.get(entry.getElement()),
						new DoubleExpression((double) entry.getCount()));
				if (entry.getElement() < combinedMoments.size()) {
					CombinedPopulationProduct combinedMomentPower = combinedMoments
							.get(entry.getElement()).getPower(entry.getCount());
					moment = CombinedPopulationProduct.getProductOf(moment,
							combinedMomentPower);
				}
				terms.add(coefficientPower);

			}
			if (moment != null) {
				terms.add(CombinedProductExpression.create(moment));
			}
			sum.add(ProductExpression.create(terms));
		}
		return SumExpression.create(sum);
	}

	@Override
	public String toString() {
		if (order == 2)
			return "Var[" + originalExpression.toString() + "]";
		return "CM[" + originalExpression.toString() + "," + order + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((coefficients == null) ? 0 : coefficients.hashCode());
		result = prime
				* result
				+ ((combinedProducts == null) ? 0 : combinedProducts.hashCode());
		result = prime * result + order;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		CentralMomentOfLinearCombinationExpression other = (CentralMomentOfLinearCombinationExpression) obj;
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
		if (order != other.order)
			return false;
		return true;
	}


}