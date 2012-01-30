package uk.ac.imperial.doc.pctmc.odeanalysis;

import java.util.Map;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;

public class AccumulatedNormalMomentClosureMinApproximation extends NormalMomentClosure {

	public final static String NAME = "AccumulatedNormalClosureMinApproximation";
	
	public AccumulatedNormalMomentClosureMinApproximation(int maxOrder) {
		super(maxOrder);		
		this.name = NAME;
	}
	
	public AccumulatedNormalMomentClosureMinApproximation(Map<String, Object> parameters) {
		super(parameters);
		this.name = NAME;
	}

	@Override
	public AbstractExpression insertProductIntoRate(AbstractExpression rate,
			PopulationProduct moment) {
		AccumulatedNormalClosureVisitorUniversal visitor = new AccumulatedNormalClosureMinApproximationVisitorUniversal(new CombinedPopulationProduct(moment), maxOrder);
		rate.accept(visitor);
		return visitor.getResult();
	}
	
	@Override
	public AbstractExpression insertAccumulations(
			AbstractExpression derivative,
			CombinedPopulationProduct moment) {
		AccumulatedNormalClosureVisitorUniversal visitor = new AccumulatedNormalClosureMinApproximationVisitorUniversal(
				new CombinedPopulationProduct(null, moment
						.getAccumulatedProducts()), maxOrder);
		derivative.accept(visitor);
		return visitor.getResult();
	}

}
