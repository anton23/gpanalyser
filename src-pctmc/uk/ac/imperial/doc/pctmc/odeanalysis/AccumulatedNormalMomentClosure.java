package uk.ac.imperial.doc.pctmc.odeanalysis;

import java.util.Map;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;

public class AccumulatedNormalMomentClosure extends NormalMomentClosure {

	public final static String NAME = "AccumulatedNormalClosure";
	
	public AccumulatedNormalMomentClosure(int maxOrder) {
		super(maxOrder);		
	}
	
	public AccumulatedNormalMomentClosure(Map<String, Object> parameters) {
		super(parameters);
	}

	@Override
	public AbstractExpression insertProductIntoRate(AbstractExpression rate,
			PopulationProduct moment) {
		AccumulatedNormalClosureVisitor visitor = new AccumulatedNormalClosureVisitor(moment, maxOrder);
		rate.accept(visitor);
		return visitor.getResult();
	}
	
	@Override
	public AbstractExpression insertAccumulations(
			AbstractExpression derivative,
			CombinedPopulationProduct moment) {
		IntegralInsterterVisitor visitor = new AccumulatedIntegralInsterterVisitor(
				new CombinedPopulationProduct(null, moment
						.getAccumulatedProducts()), maxOrder);
		derivative.accept(visitor);
		return visitor.getResult();
	}

}
