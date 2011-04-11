package uk.ac.imperial.doc.pctmc.analysis.plotexpressions;

import java.util.Set;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IExpressionVisitor;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;

/**
 * Wrapper for an abstract expression for plotting results from analyses. 
 * Keeps track of used moments and general expectations.
 * @author Anton Stefanek
 */
public class PlotExpression {
	protected AbstractExpression expression;

	public AbstractExpression getExpression() {
		return expression;
	}

	public PlotExpression(AbstractExpression expression) {
		super();
		this.expression = expression;
		CollectUsedMomentsVisitor visitor = new CollectUsedMomentsVisitor();
		expression.accept(visitor);
		usedGeneralExpectations = visitor.getUsedGeneralExpectations(); 
		usedCombinedMoments = visitor.getUsedCombinedMoments(); 
	}



	private Set<AbstractExpression> usedGeneralExpectations; 
	
	private Set<CombinedPopulationProduct> usedCombinedMoments; 
	
	

	public Set<CombinedPopulationProduct> getUsedCombinedMoments() {
		return usedCombinedMoments;
	}

	public Set<AbstractExpression> getUsedGeneralExpectations() {
		return usedGeneralExpectations;
	}

	public String toGnuplotString() {
		return toString().replace("_", "\\\\_").replace("\'", "\\'");
	}

	@Override
	public String toString() {
		return expression.toString();
	}

	public String print(IExpressionVisitor printer) {
		expression.accept(printer);
		return printer.toString();
	}
}
