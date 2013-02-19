package uk.ac.imperial.doc.pctmc.representation.accumulations;

import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;

public class AccumulatedProduct extends AccumulationVariable {
	
	protected PopulationProduct product;	
	
	public AccumulatedProduct(PopulationProduct p) {
		super(CombinedProductExpression.create(new CombinedPopulationProduct(p)));
		this.product = p;
	}

	public PopulationProduct getProduct() {
		return product;
	}
}
