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
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.utils.Multinomial;

import com.google.common.collect.Multiset;

/**
 * Expression for a moment of linear combination of combined population
 * products.
 * 
 * @author Anton Stefanek
 */
public class MomentOfLinearCombinationExpression extends ExpressionWrapper {

	private List<AbstractExpression> coefficients;
	private List<CombinedPopulationProduct> combinedMoments;	
	protected final AbstractExpression originalExpression;

	private int order;

	public MomentOfLinearCombinationExpression(
			List<AbstractExpression> coefficients,
			List<CombinedPopulationProduct> combinedMoments, int order) {
		super(createExpression(coefficients, combinedMoments, order));
		this.coefficients = coefficients;
		this.combinedMoments = combinedMoments;
		this.order = order;
		this.originalExpression = this.internalExpression;
	}
	
	public MomentOfLinearCombinationExpression(AbstractExpression e, int order, Map<ExpressionVariable, AbstractExpression> var) {
		super(MomentOfLinearCombinationExpression
				.createExpression(new MeanOfLinearCombinationExpression(e, var)
						.getCoefficients(),
						new MeanOfLinearCombinationExpression(e, var)
								.getCombinedProducts(), order));
		this.originalExpression = e;
		this.order = order;
	}

	public static AbstractExpression createExpression(
			List<AbstractExpression> coefficients,
			List<CombinedPopulationProduct> combinedMoments, int order) {
		List<AbstractExpression> sum = new LinkedList<AbstractExpression>();
		List<Multiset<Integer>> partitions = Multinomial.getPartitions(order,
				combinedMoments.size());
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
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof MomentOfLinearCombinationExpression))
			return false;
		MomentOfLinearCombinationExpression asCMLC = (MomentOfLinearCombinationExpression) o;
		return combinedMoments.equals(asCMLC.getCombinedMoments())
				&& coefficients.equals(asCMLC.getCoefficients())
				&& order == asCMLC.getOrder();
	}

	public List<AbstractExpression> getCoefficients() {
		return coefficients;
	}

	public List<CombinedPopulationProduct> getCombinedMoments() {
		return combinedMoments;
	}

	public int getOrder() {
		return order;
	}

	@Override
	public String toString() {
		String ret = "Moment[" + originalExpression;		
		return ret + "," + order + "]";
	}

}
