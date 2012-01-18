package uk.ac.imperial.doc.pctmc.odeanalysis;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import uk.ac.imperial.doc.jexpressions.constants.ConstantExpression;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.FunctionCallExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;
import uk.ac.imperial.doc.pctmc.representation.State;

public class AccumulatedNormalClosureVisitor extends GetVVersionVisitorMomentClosure {

	public AccumulatedNormalClosureVisitor(PopulationProduct moment,
			int maxOrder) {
		super(moment, maxOrder);
	}
	
	@Override
	public void visit(ConstantExpression e) {
		if (insert) {
			result = ProductExpression.create(e, CombinedProductExpression
					.create(new CombinedPopulationProduct(moment)));
			inserted = true;
		} else {
			result = e;
		}
	}
	
/*	
	@Override
	public void visit(IndicatorFunction e) {
		boolean oldInserted = inserted;
		e.getCondition().getLeft().accept(this);
		AbstractExpression left = result;
		e.getCondition().getRight().accept(this);
		AbstractExpression right = result;
		result = new IndicatorFunction(new ExpressionCondition(left, e.getCondition().getOperator(), right));
		inserted = oldInserted;

	}*/

	@Override
	public void visit(FunctionCallExpression e) {
		
		if (insert) {
			List<AbstractExpression> newArguments = new LinkedList<AbstractExpression>();
			for (AbstractExpression a:e.getArguments()) {
				a.accept(this);
				newArguments.add(result);
			}
			result = FunctionCallExpression.create(e.getName(), newArguments);
			inserted = true;
		} else {
			result = e;
		}
	}

	@Override
	public void visit(CombinedProductExpression e) {
		if (e.getProduct().getAccumulatedProducts().isEmpty()) {
			super.visit(e);
		} else {
			int order = moment.getOrder() + e.getProduct().getOrder();
			if (order <= maxOrder) {
				result = CombinedProductExpression
						.create(CombinedPopulationProduct.getProductOf(new CombinedPopulationProduct(moment), e.getProduct()));
			} else
				if (maxOrder == 1) {
					List<AbstractExpression> terms = new LinkedList<AbstractExpression>();
					for (Entry<State, Integer> entry: moment.getRepresentation().entrySet()) {
						for (int i = 0; i<entry.getValue(); i++) {
							terms.add(CombinedProductExpression.create(CombinedPopulationProduct.getMeanPopulation(entry.getKey())));
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
					throw new AssertionError("Not implemented yet!");
				}
		}
	}
	
	
	public void bla(CombinedProductExpression e) {
		if (insert) {
			inserted = true;
		if (e.getProduct().getAccumulatedProducts().size() > 0) {
			throw new AssertionError("Accumulations not allowed in rates!");
		}
		PopulationProduct nakedProduct = e.getProduct().getNakedProduct();
		int order = moment.getOrder() + nakedProduct.getOrder();
		if (order <= maxOrder) {
			result = CombinedProductExpression
					.create(new CombinedPopulationProduct(PopulationProduct
							.getProduct(moment, nakedProduct)));
		} else
		if (maxOrder == 1) {
			List<AbstractExpression> terms = new LinkedList<AbstractExpression>();
			for (Entry<State, Integer> entry: moment.getRepresentation().entrySet()) {
				for (int i = 0; i<entry.getValue(); i++) {
					terms.add(CombinedProductExpression.create(CombinedPopulationProduct.getMeanPopulation(entry.getKey())));
				}
			}
			for (Entry<State, Integer> entry: nakedProduct.getRepresentation().entrySet()) {
				for (int i = 0; i<entry.getValue(); i++) {
					terms.add(CombinedProductExpression.create(CombinedPopulationProduct.getMeanPopulation(entry.getKey())));
				}
			}
			result = ProductExpression.create(terms);
		} else  {
			State[] x = new State[order];
			int i = 0;
			for (State s : PopulationProduct.getProduct(moment, nakedProduct)
					.asMultiset()) {
				if (s != null)
					x[i++] = s;
			}
			if (order % 2 == 1) {
				result = getOddMomentInTermsOfCovariances(x);
			} else {
				result = getEventMomentInTermsOfCovariances(x);
			}
			
		} 
		} else {
			result = e;
		}
	}
}
