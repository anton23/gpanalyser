package uk.ac.imperial.doc.pctmc.expressions;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IExpressionVisitor;
import uk.ac.imperial.doc.pctmc.representation.State;

/**
 * AbstractExpression wrapper for a combined population product.
 * @author Anton Stefanek
 *
 */
public class CombinedProductExpression extends AbstractExpression {
	private CombinedPopulationProduct product;

	public CombinedPopulationProduct getProduct() {
		return product;
	}

	private CombinedProductExpression(CombinedPopulationProduct product) {
		super();
		this.product = product;
	}
	
	public static AbstractExpression create(CombinedPopulationProduct product){
		if (product.getOrder()==0){
			return new DoubleExpression(1.0); 
		} else {
			return new CombinedProductExpression(product); 
		}
	}
	
	public static AbstractExpression createMeanExpression(State state) {
		return CombinedProductExpression.create(CombinedPopulationProduct.getMeanPopulation(state));
	}

	@Override
	public void accept(IExpressionVisitor v) {
		if (v instanceof ICombinedProductExpressionVisitor){
			((ICombinedProductExpressionVisitor)v).visit(this);
		} else {
			throw new AssertionError("Not supported visit for a Combined moment expression!");
		}		
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true; 
		if (!(o instanceof CombinedProductExpression)) return false;
		CombinedProductExpression asCombinedProduct = (CombinedProductExpression)o;
		return product.equals(asCombinedProduct.getProduct()); 
		
	}

	@Override
	public int hashCode() {
		return product.hashCode(); 
	}

	@Override
	public String toString() {
		return product.toString();
	} 


}
