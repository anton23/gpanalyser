package uk.ac.imperial.doc.pctmc.odeanalysis;

import java.util.LinkedList;
import java.util.List;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;
import uk.ac.imperial.doc.pctmc.representation.State;


public class GetVVersionVisitorMomentClosure extends GetVVersionVisitor{

	public GetVVersionVisitorMomentClosure(PopulationProduct moment) {
		super(moment);
	}

	@Override
	public void visit(CombinedProductExpression e) {
		if (e.getProduct().getOrder()>2){
			throw new AssertionError("Unsupported rate function!");			
		}
		if (e.getProduct().getNakedProduct().getOrder() == 2){
			List<AbstractExpression> terms = new LinkedList<AbstractExpression>();
			boolean first = true; 
			for (State s:e.getProduct().getNakedProduct().getProduct().keySet()){
				if (first) {
					terms.add(CombinedProductExpression.create(new CombinedPopulationProduct(moment.getV(s))));
					first = false; 
				}
				else {
					terms.add(CombinedProductExpression.create(new CombinedPopulationProduct(PopulationProduct.getMeanProduct(s))));
				}
			}
			result = ProductExpression.create(terms);
			
		} else {
			State state = e.getProduct().getNakedProduct().getProduct().keySet().iterator().next();
			result = CombinedProductExpression.create(new CombinedPopulationProduct(moment.getV(state)));
		}
	}	
	
	
}
