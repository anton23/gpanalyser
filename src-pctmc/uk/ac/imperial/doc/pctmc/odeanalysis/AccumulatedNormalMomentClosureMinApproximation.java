package uk.ac.imperial.doc.pctmc.odeanalysis;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;

public class AccumulatedNormalMomentClosureMinApproximation extends NormalMomentClosure {

	public final static String NAME = "AccumulatedNormalClosureMinApproximation";
	
	protected Map<AbstractExpression, ExpressionVariable> usedVariables;
	protected int lastVariable;
	
	public AccumulatedNormalMomentClosureMinApproximation(int maxOrder) {
		super(maxOrder);		
		this.name = NAME;
		usedVariables = new HashMap<AbstractExpression, ExpressionVariable>();
		lastVariable = 0;
	}
	
	public AccumulatedNormalMomentClosureMinApproximation(Map<String, Object> parameters) {
		super(parameters);
		this.name = NAME;
		usedVariables = new LinkedHashMap<AbstractExpression, ExpressionVariable>();
		lastVariable = 0;
	}

	@Override
	public AbstractExpression insertProductIntoRate(AbstractExpression rate,
			PopulationProduct moment) {
		AccumulatedNormalClosureMinApproximationVisitorUniversal visitor = new AccumulatedNormalClosureMinApproximationVisitorUniversal(new CombinedPopulationProduct(moment), maxOrder, usedVariables, lastVariable);
		rate.accept(visitor);
		lastVariable = visitor.getVariableIndex();
		return visitor.getResult();
	}
	
	@Override
	public AbstractExpression insertAccumulations(
			AbstractExpression derivative,
			CombinedPopulationProduct moment) {
		AccumulatedNormalClosureMinApproximationVisitorUniversal visitor = new AccumulatedNormalClosureMinApproximationVisitorUniversal(
				new CombinedPopulationProduct(null, moment
						.getAccumulatedProducts()), maxOrder, usedVariables, lastVariable);
		derivative.accept(visitor);
		lastVariable = visitor.getVariableIndex();
		return visitor.getResult();
	}
}
