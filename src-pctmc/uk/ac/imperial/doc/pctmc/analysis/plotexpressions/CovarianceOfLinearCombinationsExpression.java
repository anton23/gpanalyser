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

public class CovarianceOfLinearCombinationsExpression extends ExpressionWrapper {
	AbstractExpression a;
	AbstractExpression b;

	public CovarianceOfLinearCombinationsExpression(AbstractExpression a, AbstractExpression b, Map<ExpressionVariable, AbstractExpression> var) {
		super(createExpression(a, b, var));
		this.a = a;
		this.b = b;
	}
	
	public static AbstractExpression createExpression(AbstractExpression a, AbstractExpression b, Map<ExpressionVariable, AbstractExpression> var) {
		MeanOfLinearCombinationExpression mA = new MeanOfLinearCombinationExpression(a, var);
		MeanOfLinearCombinationExpression mB = new MeanOfLinearCombinationExpression(b, var);
		List<AbstractExpression> coefficients = new LinkedList<AbstractExpression>();
		List<CombinedPopulationProduct> moments = new LinkedList<CombinedPopulationProduct>();
		for (int i = 0; i < mA.getCoefficients().size(); i++) {
			for (int j = 0; j < mB.getCoefficients().size(); j++) {
				coefficients.add(ProductExpression.create(mA.getCoefficients().get(i), mB.getCoefficients().get(j)));
				moments.add(CombinedPopulationProduct.getProductOf(mA.getCombinedProducts().get(i), mB.getCombinedProducts().get(j)));
			}
		}
		AbstractExpression mAB = new MomentOfLinearCombinationExpression(coefficients, moments, 1);
		return SumExpression.create(mAB, ProductExpression.create(mA, mB, new DoubleExpression(-1.0)));		
	}
	
	@Override
	public String toString() {
		return "Cov["+a.toString() + ", " + b.toString() + "]";
	}
}
