package uk.ac.imperial.doc.pctmc.analysis.plotexpressions;

import java.util.LinkedList;
import java.util.List;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.PowerExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;
import uk.ac.imperial.doc.jexpressions.expressions.UMinusExpression;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.utils.Multinomial;

import com.google.common.collect.Multiset;

public class CentralMomentOfLinearCombination  {


	public static AbstractExpression createExpression(List<AbstractExpression> coefficients, List<CombinedPopulationProduct> combinedMoments, int order) {
		for (int i = 0; i<combinedMoments.size(); i++){
			CombinedPopulationProduct c = combinedMoments.get(i); 
			AbstractExpression coefficient = coefficients.get(i);
			coefficients.add(new UMinusExpression(ProductExpression.create(CombinedProductExpression.create(c),coefficient)));
		}
		List<AbstractExpression> sum = new LinkedList<AbstractExpression>();
		List<Multiset<Integer>> partitions = Multinomial.getPartitions(order, combinedMoments.size()*2);
		for (Multiset<Integer> partition:partitions){
			int multinomialCoefficient = Multinomial.getMultinomialCoefficient(order, partition);
			List<AbstractExpression> terms = new LinkedList<AbstractExpression>();
			CombinedPopulationProduct moment = null;
			terms.add(new DoubleExpression((double)multinomialCoefficient));
			for (Multiset.Entry<Integer> entry:partition.entrySet()){
				AbstractExpression coefficientPower = PowerExpression.create(coefficients.get(entry.getElement()), 
						new DoubleExpression((double)entry.getCount()));
				if (entry.getElement()<combinedMoments.size()){
					CombinedPopulationProduct combinedMomentPower = combinedMoments.get(entry.getElement()).getPower(entry.getCount());
					moment = CombinedPopulationProduct.getProductOf(moment, combinedMomentPower);
				} 
				terms.add(coefficientPower);
				
			}
			if (moment!=null) {
				terms.add(CombinedProductExpression.create(moment));
			}
			sum.add(ProductExpression.create(terms));
		}
		return SumExpression.create(sum); 
	}
}
