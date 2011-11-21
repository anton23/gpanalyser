package uk.ac.imperial.doc.pctmc.odeanalysis;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;

public class NormalMomentClosure implements IMomentClosure {
	
	protected int maxOrder;
	
	
	public NormalMomentClosure(int maxOrder) {
		this.maxOrder = maxOrder;
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
	
	
}
