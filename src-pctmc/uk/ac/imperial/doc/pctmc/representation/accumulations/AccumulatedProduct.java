package uk.ac.imperial.doc.pctmc.representation.accumulations;

import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;

/**
 * @author anton
 *
 */
public class AccumulatedProduct extends AccumulationVariable {
	
	@Override
	public String toString() {
		return "acc(" + product.toString() + ")";
	}

	protected PopulationProduct product;	
	
	public AccumulatedProduct(PopulationProduct p) {
		super(CombinedProductExpression.create(new CombinedPopulationProduct(p)));
		this.product = p;
	}

	public PopulationProduct getProduct() {
		return product;
	}

	@Override
	public int getOrder() {
		return product.getOrder();
	}
	
	
	
	
}
