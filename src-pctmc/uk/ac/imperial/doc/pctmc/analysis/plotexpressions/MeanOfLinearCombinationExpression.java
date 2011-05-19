package uk.ac.imperial.doc.pctmc.analysis.plotexpressions;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IExpressionVisitor;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;


//only works for expressions of the form  (c_1*)?CM_1 + (c_2*)? CM_2 ...
public class MeanOfLinearCombinationExpression extends AbstractExpression{
	
	@Override
	public void accept(IExpressionVisitor v) {
		internalExpression.accept(v); 
		
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 0;
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
		
		if (getClass() != obj.getClass())
			return false;
		MeanOfLinearCombinationExpression other = (MeanOfLinearCombinationExpression) obj;
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


	@Override
	public String toString() {
		return "E["+internalExpression.toString()+"]"; 
	}

	private List<AbstractExpression> coefficients; 
	private List<CombinedPopulationProduct> combinedProducts;
	
	
	
	public List<AbstractExpression> getCoefficients() {
		return coefficients;
	}


	public List<CombinedPopulationProduct> getCombinedProducts() {
		return combinedProducts;
	}


	public AbstractExpression getInternalExpression() {
		return internalExpression;
	}

	AbstractExpression internalExpression; 
	
	public MeanOfLinearCombinationExpression(AbstractExpression e, Map<ExpressionVariable,AbstractExpression> var){		
		AbstractExpression lsum = e; 
		if (e instanceof ExpressionVariable){
			lsum = var.get(e); 
		}
		coefficients = new LinkedList<AbstractExpression>(); 
		combinedProducts = new LinkedList<CombinedPopulationProduct>(); 
		List<AbstractExpression> summands = new LinkedList<AbstractExpression>(); 
		if (lsum instanceof ProductExpression || lsum instanceof CombinedProductExpression){
			summands.add(lsum);
		}
		if (lsum instanceof SumExpression){
			summands.addAll(((SumExpression) lsum).getSummands());
		}
		for (AbstractExpression s:summands){
			if (s instanceof CombinedProductExpression){
				coefficients.add(new DoubleExpression(1.0));
				combinedProducts.add(((CombinedProductExpression) s).getProduct());
			}
			if (s instanceof ProductExpression){
				List<AbstractExpression> terms = ((ProductExpression) s).getTerms();
				if (terms.size()>2||!(terms.get(1) instanceof CombinedProductExpression)){
					throw new AssertionError("Expression " + e + " is not a linear combination of moments!");
				}
				AbstractExpression coefficient = terms.get(0); 
				CombinedPopulationProduct product = ((CombinedProductExpression)terms.get(1)).getProduct();
				IsConstantVisitor visitor = new IsConstantVisitor(); 
				coefficient.accept(visitor);
				if (!visitor.isConstant()){
					throw new AssertionError("Expression " + e + " is not a linear combination of moments!"); 
				}
				coefficients.add(coefficient); 
				combinedProducts.add(product); 
			}
		}
		internalExpression = e; 
	}
	
	


}
