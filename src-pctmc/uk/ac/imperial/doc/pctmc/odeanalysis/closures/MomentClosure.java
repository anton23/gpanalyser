package uk.ac.imperial.doc.pctmc.odeanalysis.closures;

import java.util.Map;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;

public abstract class MomentClosure
{
	public static final String MOMENT_CLOSURE = "momentClosure";
	public static final String MAX_ORDER = "maxOrder";
	public static final String MEAN_FIELD_STABILISER = "mfStabiliser";
	
	public MomentClosure(Map<String, Object> parameters) {}
	public abstract AbstractExpression insertProductIntoRate(AbstractExpression rate, PopulationProduct moment);
	public abstract AbstractExpression insertAccumulations(AbstractExpression derivative, CombinedPopulationProduct moment);
	public abstract String toString();
	public abstract Map<ExpressionVariable, AbstractExpression> getVariables();
}
