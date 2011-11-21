package uk.ac.imperial.doc.pctmc.odeanalysis;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;

public interface IMomentClosure {
	
	public AbstractExpression closeRate(AbstractExpression rate, PopulationProduct moment);
	public AbstractExpression insertAccumulations(AbstractExpression derivative, CombinedPopulationProduct moment);

}
