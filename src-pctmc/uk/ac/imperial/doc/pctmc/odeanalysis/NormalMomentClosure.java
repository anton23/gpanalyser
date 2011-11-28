package uk.ac.imperial.doc.pctmc.odeanalysis;

import java.util.HashMap;
import java.util.Map;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;

public class NormalMomentClosure extends MomentClosure {
	
	protected int maxOrder;
	
	public NormalMomentClosure(int maxOrder) {
		super(new HashMap<String, Object>());		
		this.maxOrder = maxOrder;		
	}
		
	public NormalMomentClosure(Map<String, Object> parameters) {
		super(parameters);
		if (parameters.containsKey("maxOrder")) {
			this.maxOrder = (Integer) parameters.get("maxOrder");
		}
	}

	@Override
	public AbstractExpression closeRate(AbstractExpression rate,
			PopulationProduct moment) {
		GetVVersionVisitorMomentClosure visitor = new GetVVersionVisitorMomentClosure(moment, maxOrder);
		rate.accept(visitor);
		return visitor.getResult();
	}


	@Override
	public AbstractExpression insertAccumulations(
			AbstractExpression derivative,
			CombinedPopulationProduct moment) {
		IntegralInsterterVisitor visitor = new IntegralInsterterVisitor(
				new CombinedPopulationProduct(null, moment
						.getAccumulatedProducts()));
		derivative.accept(visitor);
		return visitor.getResult();
	}

	@Override
	public String toString() {
		return "momentClosure=NormalClosure" + ", maxOrder="+maxOrder;
	}
	
	
}
