package uk.ac.imperial.doc.pctmc.odeanalysis;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;
import uk.ac.imperial.doc.pctmc.representation.State;

import com.google.common.collect.Multiset;

// TODO Generalise this for other closures
public class AccumulatedIntegralInsterterVisitor extends
		IntegralInsterterVisitor {

	int maxOrder;
	
	public AccumulatedIntegralInsterterVisitor(
			CombinedPopulationProduct toInsert, int maxOrder) {
		super(toInsert);
		this.maxOrder = maxOrder;
	}
	
	@Override
	public void visit(CombinedProductExpression e) {
		// assumes the combined product has no accumulated products
		foundMoment = true;
		if (insert) {
				int order = toInsert.getOrder() + e.getProduct().getOrder();
				if (order <= maxOrder) {
					result = CombinedProductExpression
							.create(CombinedPopulationProduct.getProductOf(toInsert, e.getProduct()));
				} else
					if (maxOrder == 1) {
						List<AbstractExpression> terms = new LinkedList<AbstractExpression>();
						
						for (Multiset.Entry<PopulationProduct> entry:toInsert.getAccumulatedProducts().entrySet()) {
							for (int i = 0; i<entry.getCount(); i++) {
								terms.add(CombinedProductExpression.create(CombinedPopulationProduct.getMeanAccumulatedProduct(entry.getElement())));
							}
						}
						for (Entry<State, Integer> entry: e.getProduct().getNakedProduct().getRepresentation().entrySet()) {
							for (int i = 0; i<entry.getValue(); i++) {
								terms.add(CombinedProductExpression.create(CombinedPopulationProduct.getMeanPopulation(entry.getKey())));
							}
						}
						for (com.google.common.collect.Multiset.Entry<PopulationProduct> entry : e.getProduct().getAccumulatedProducts().entrySet()) {
							for (int i = 0; i<entry.getCount(); i++) {
								terms.add(CombinedProductExpression.create(CombinedPopulationProduct.getMeanAccumulatedProduct(entry.getElement())));
							}
						}
						result = ProductExpression.create(terms);
					} else {
						CombinedPopulationProduct[] x = new CombinedPopulationProduct[order];
						int i = 0;
						for (State s : e.getProduct().getNakedProduct()
								.asMultiset()) {
	 						if (s != null)
								x[i++] = CombinedPopulationProduct.getMeanPopulation(s);
						}
						for (PopulationProduct p:e.getProduct().getAccumulatedProducts()) {
							x[i++] = CombinedPopulationProduct.getMeanAccumulatedProduct(p);
						}
						for (PopulationProduct p:toInsert.getAccumulatedProducts()) {
							x[i++] = CombinedPopulationProduct.getMeanAccumulatedProduct(p);
						}
						if (order % 2 == 1) {
							result = AccumulatedNormalClosureVisitor.getOddMomentInTermsOfCovariances(x);
						} else {
							result = AccumulatedNormalClosureVisitor.getEventMomentInTermsOfCovariances(x);
							
						}
					}
			} else {
				result = e;
			}
	}

	@Override
	public void visit(ProductExpression e) {
		List<AbstractExpression> newTerms = new LinkedList<AbstractExpression>();
		boolean oldFoundMinimum = foundMinimum;
		boolean oldFoundMoment = foundMoment;
		boolean oldInsert = insert;
		foundMinimum = false;
		foundMoment = false;

		for (AbstractExpression term : e.getTerms()) {
			term.accept(this);
			if (foundMinimum || foundMoment) {
				insert = false;
			}
			newTerms.add(result);
		}
		if (!foundMinimum && !foundMoment) {
			newTerms.add(CombinedProductExpression.create(toInsert));
		}
		foundMinimum |= oldFoundMinimum;
		foundMoment = oldFoundMoment;
		insert = oldInsert;
		result = ProductExpression.create(newTerms);
	}
}
