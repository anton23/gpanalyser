 package uk.ac.imperial.doc.pctmc.analysis.plotexpressions;
import java.util.List;
import java.util.Map;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IExpressionVisitor;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;

public class CentralMomentOfLinearCombinationExpression extends AbstractExpression {
	
	@Override
	public void accept(IExpressionVisitor v) {
		internalExpression.accept(v);
		
	}

	@Override
	public String toString() {
		if (order==2) return "Var{" + originalExpression.toString() + "}";
		return "CM{"+originalExpression.toString() + "," + order +"}"; 
	}

	AbstractExpression internalExpression; 	
	AbstractExpression originalExpression;
	int order; 
	
	public CentralMomentOfLinearCombinationExpression(
			AbstractExpression e, int order, Map<ExpressionVariable,AbstractExpression> var) {
		super();	
		originalExpression = e;
		this.order = order; 
		MeanOfLinearCombinationExpression mean = new MeanOfLinearCombinationExpression(e, var);
		internalExpression=CentralMomentOfLinearCombination.createExpression(mean.getCoefficients(), mean.getCombinedProducts(), order); 		
	}
	
	
	private List<AbstractExpression> coefficients; 
	private List<CombinedPopulationProduct> combinedProducts;
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((coefficients == null) ? 0 : coefficients.hashCode());
		result = prime
				* result
				+ ((combinedProducts == null) ? 0 : combinedProducts.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CentralMomentOfLinearCombinationExpression other = (CentralMomentOfLinearCombinationExpression) obj;
		if (coefficients == null) {
			if (other.coefficients != null)
				return false;
		} else if (!coefficients.equals(other.coefficients))
			return false;
		if (combinedProducts == null) {
			if (other.combinedProducts != null)
				return false;
		} else if (!combinedProducts.equals(other.combinedProducts))
			return false;
		return true;
	}
	
	

}
