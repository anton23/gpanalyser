package uk.ac.imperial.doc.pctmc.odeanalysis;

import java.util.Map;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;

public abstract class MomentClosure {
	public MomentClosure(Map<String, Object> parameters) {}
	
	public abstract AbstractExpression insertProductIntoRate(AbstractExpression rate, PopulationProduct moment);
	public abstract AbstractExpression insertAccumulations(AbstractExpression derivative, CombinedPopulationProduct moment);
	public abstract String toString();

}
