package uk.ac.imperial.doc.pctmc.odeanalysis;

import java.util.HashMap;
import java.util.Map;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;

public class NormalMomentClosure extends MomentClosure {

	public final static String NAME = "NormalClosure";
	
	protected String name = NAME;
	
	protected int maxOrder;
	
	
	
	@Override
	public Map<ExpressionVariable, AbstractExpression> getVariables() {	
		return null;
	}

	public NormalMomentClosure(int maxOrder) {
		super(new HashMap<String, Object>());		
		this.maxOrder = maxOrder;		
	}
		
	public NormalMomentClosure(Map<String, Object> parameters) {
		super(parameters);
		if (parameters.containsKey(MomentClosure.MAX_ORDER)) {
			this.maxOrder = (Integer) parameters.get(MomentClosure.MAX_ORDER);
		}
	}

	@Override
	public AbstractExpression insertProductIntoRate(AbstractExpression rate,
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
		return MomentClosure.MOMENT_CLOSURE + "=" + name + ", " + MomentClosure.MAX_ORDER + "=" + maxOrder;
	}
	
	
}
