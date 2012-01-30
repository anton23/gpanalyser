package uk.ac.imperial.doc.pctmc.odeanalysis;

import java.util.HashMap;
import java.util.Map;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.FunctionCallExpression;
import uk.ac.imperial.doc.jexpressions.expressions.MinExpression;
import uk.ac.imperial.doc.jexpressions.expressions.MinusExpression;
import uk.ac.imperial.doc.jexpressions.expressions.PowerExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.CentralMomentOfLinearCombinationExpression;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.CovarianceOfLinearCombinationsExpression;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;

import com.google.common.collect.Lists;

public class AccumulatedNormalClosureMinApproximationVisitorUniversal extends AccumulatedNormalClosureVisitorUniversal {

	
	public AccumulatedNormalClosureMinApproximationVisitorUniversal(
			CombinedPopulationProduct moment, int maxOrder) {
		super(moment, maxOrder);
	}
	
	@Override
	public void visit(MinExpression e) {

		AbstractExpression muA = e.getA();
		AbstractExpression muB = e.getB();

		Map<ExpressionVariable, AbstractExpression> var = new HashMap<ExpressionVariable, AbstractExpression>();
		AbstractExpression covAB = new CovarianceOfLinearCombinationsExpression(
				muA, muB, var);
		AbstractExpression varA = new CentralMomentOfLinearCombinationExpression(
				muA, 2, var);
		AbstractExpression varB = new CentralMomentOfLinearCombinationExpression(
				muB, 2, var);
		AbstractExpression theta = PowerExpression.create(
				FunctionCallExpression.create(
						"max", Lists.newArrayList(
				SumExpression.create(
				varA, varB, ProductExpression.create(
						new DoubleExpression(-2.0), covAB)), new DoubleExpression(0.0))),
				new DoubleExpression(0.5));
		
//		if (e.getB().equals(new DoubleExpression(0.0))) {
//			AbstractExpression nonNegative = FunctionCallExpression.create(
//					"max", Lists.newArrayList(varA, new DoubleExpression(0.0)));
//			theta = FunctionCallExpression.create("sqrt", Lists
//					.newArrayList(nonNegative));
//		}

		AbstractExpression muA2 = e.getA();
		AbstractExpression muB2 = e.getB();
		AbstractExpression theta2 = theta;
		if (moment.getOrder() > 0 && insert) {
			muA.accept(this);
			muA2 = result;
			inserted = false;
			muB.accept(this);
			muB2 = result;
			theta2 = ProductExpression.create(CombinedProductExpression
					.create(moment), theta);
		}
		MinusExpression mAmB = new MinusExpression(muA, muB);

		MinusExpression mBmA = new MinusExpression(muB, muA);

		result = SumExpression.create(ProductExpression.create(muA2,
				FunctionCallExpression.create("safe_Phi", Lists.newArrayList(mBmA, theta))), ProductExpression.create(muB2,
				FunctionCallExpression.create("safe_Phi", Lists.newArrayList(mAmB, theta))), ProductExpression.create(
				new DoubleExpression(-1.0), theta2, FunctionCallExpression
						.create("safe_phi", Lists.newArrayList(mBmA, theta))));
		inserted = true;

	}

}
