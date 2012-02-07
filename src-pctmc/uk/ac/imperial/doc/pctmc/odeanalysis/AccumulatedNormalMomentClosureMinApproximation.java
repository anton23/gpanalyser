package uk.ac.imperial.doc.pctmc.odeanalysis;

import java.util.HashMap;
import java.util.Map;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;

public class AccumulatedNormalMomentClosureMinApproximation extends NormalMomentClosure {

	public final static String NAME = "AccumulatedNormalClosureMinApproximation";
	
	Map<AbstractExpression, ExpressionVariable> usedVariables;
	int variableIndex;
	
	public AccumulatedNormalMomentClosureMinApproximation(int maxOrder) {
		super(maxOrder);		
		this.name = NAME;
		usedVariables = new HashMap<AbstractExpression, ExpressionVariable>();
		variableIndex = 0;
	}
	
	public AccumulatedNormalMomentClosureMinApproximation(Map<String, Object> parameters) {
		super(parameters);
		this.name = NAME;
		usedVariables = new HashMap<AbstractExpression, ExpressionVariable>();
		variableIndex = 0;
	}

	@Override
	public AbstractExpression insertProductIntoRate(AbstractExpression rate,
			PopulationProduct moment) {
		AccumulatedNormalClosureMinApproximationVisitorUniversal visitor = new AccumulatedNormalClosureMinApproximationVisitorUniversal(new CombinedPopulationProduct(moment), maxOrder, usedVariables, variableIndex);
		rate.accept(visitor);
		variableIndex = visitor.getVariableIndex();
		return visitor.getResult();
	}
	
	@Override
	public AbstractExpression insertAccumulations(
			AbstractExpression derivative,
			CombinedPopulationProduct moment) {
		AccumulatedNormalClosureMinApproximationVisitorUniversal visitor = new AccumulatedNormalClosureMinApproximationVisitorUniversal(
				new CombinedPopulationProduct(null, moment
						.getAccumulatedProducts()), maxOrder, usedVariables, variableIndex);
		derivative.accept(visitor);
		variableIndex = visitor.getVariableIndex();
		return visitor.getResult();
	}

}
