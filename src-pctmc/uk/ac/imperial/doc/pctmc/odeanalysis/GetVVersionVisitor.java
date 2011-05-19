package uk.ac.imperial.doc.pctmc.odeanalysis;

import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.ICombinedProductExpressionVisitor;
import uk.ac.imperial.doc.pctmc.expressions.PopulationExpression;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;
import uk.ac.imperial.doc.pctmc.representation.State;


public class GetVVersionVisitor extends MomentCountTransformerWithParameters implements ICombinedProductExpressionVisitor {

	protected PopulationProduct moment;

	public GetVVersionVisitor(PopulationProduct moment) {
		super();
		this.moment = moment;
	}

	@Override
	public void visit(PopulationExpression e) {
		result = CombinedProductExpression.create(new CombinedPopulationProduct(moment.getV(e.getState())));
	}

	@Override
	public void visit(CombinedProductExpression e) {
		if (e.getProduct().getOrder()>1 || e.getProduct().getNakedProduct().getOrder()!=1){
			throw new AssertionError("Unsupported rate function!");			
		}
		State state = e.getProduct().getNakedProduct().getProduct().keySet().iterator().next();
		result = CombinedProductExpression.create(new CombinedPopulationProduct(moment.getV(state)));
	}	
	
	
}
