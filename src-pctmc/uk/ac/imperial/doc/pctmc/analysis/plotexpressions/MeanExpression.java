package uk.ac.imperial.doc.pctmc.analysis.plotexpressions;


import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.representation.State;

/**
 * Expression for mean population of a single state.
 * @author Anton Stefanek
 *
 */
public class MeanExpression extends ExpressionWrapper {

	public MeanExpression(State state) {
		super(createExpression(state));
	}

	public static AbstractExpression createExpression(State state) {
		return CombinedProductExpression.create(CombinedPopulationProduct.getMeanPopulation(state));
	}

}
