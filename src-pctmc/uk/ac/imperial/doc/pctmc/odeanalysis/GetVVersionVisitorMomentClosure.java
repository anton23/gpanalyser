package uk.ac.imperial.doc.pctmc.odeanalysis;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.PopulationExpression;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;
import uk.ac.imperial.doc.pctmc.representation.State;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;


public class GetVVersionVisitorMomentClosure extends GetVVersionVisitor{

	protected boolean insert = true; 
	
	public GetVVersionVisitorMomentClosure(PopulationProduct moment, int maxOrder) {
		super(moment);
		this.maxOrder = maxOrder;
	}
	
	@Override
	public void visit(PopulationExpression e) {
		CombinedPopulationProduct product;
		if (insert){
			product = new CombinedPopulationProduct(moment.getV(e.getState()));
			inserted = true; 
		}else{
			product =  new CombinedPopulationProduct(PopulationProduct.getMeanProduct(e.getState()));
		}
		result = CombinedProductExpression.create(product);
	}
	
	private boolean inserted = false; 
	
	@Override
	public void visit(ProductExpression e) {
		List<AbstractExpression> terms = new LinkedList<AbstractExpression>();
		boolean oldInsert = insert;  
		boolean isInserted = false; 
		for (AbstractExpression t:e.getTerms()){
			inserted = false; 
			t.accept(this);
			isInserted = inserted; 
			if (isInserted){
				insert = false; 
			}
			terms.add(result); 
		}
		insert = oldInsert; 
		result =  ProductExpression.create(terms); 
	}

	int maxOrder; 
	
	@Override
	public void visit(CombinedProductExpression e) {
		//TODO investigate normal approximation, i.e.
		//close moments which are too high by e.g. Isserlis’ theorem
		if (e.getProduct().getAccumulatedProducts().size()>0){
			throw new AssertionError("Accumulations not allowed in rates!"); 
		}
		PopulationProduct nakedProduct = e.getProduct().getNakedProduct();
		if (moment.getOrder() + nakedProduct.getOrder() <= maxOrder){
			result = CombinedProductExpression.create(new CombinedPopulationProduct(PopulationProduct.getProduct(moment, nakedProduct)));
		} else {
			Multiset<State> momentMset = moment.asMultiset(); 
			Multiset<State> nakedMset = nakedProduct.asMultiset(); 
			Multiset<State> remains = HashMultiset.<State>create(); 
			for (State s:nakedMset.elementSet()){
				int count = nakedMset.count(s); 
				int toAdd = Math.min(count, Math.max(maxOrder-momentMset.size(),0));
				count -= toAdd;
				momentMset.add(s,toAdd);
				remains.add(s,count);

			}
			List<AbstractExpression> terms = new LinkedList<AbstractExpression>();
			terms.add(CombinedProductExpression.create(new CombinedPopulationProduct(new PopulationProduct(momentMset))));
			for (State s:remains.elementSet()){
				int count = remains.count(s);
				for (int i = 0; i<count; i++){
					CombinedPopulationProduct product = new CombinedPopulationProduct(PopulationProduct.getMeanProduct(s));
					terms.add(CombinedProductExpression.create(product));
				}
			}
			
			
			result = ProductExpression.create(terms);
		}
	
	}	
	
	
}
