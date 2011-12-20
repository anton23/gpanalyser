package uk.ac.imperial.doc.pctmc.odeanalysis;

import java.util.LinkedList;
import java.util.List;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;

// TODO Generalise this for other closures
public class AccumulatedIntegralInsterterVisitor extends
		IntegralInsterterVisitor {

	int maxOrder = 1;
	
	public AccumulatedIntegralInsterterVisitor(
			CombinedPopulationProduct toInsert) {
		super(toInsert);
	}
	
	@Override
	public void visit(CombinedProductExpression e) {
		// assumes the combined product has no accumulated products
		foundMoment = true;
		if (insert) {
			result = CombinedProductExpression
					.create(CombinedPopulationProduct.getProductOf(e.getProduct(), toInsert));
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
		foundMinimum = oldFoundMinimum;
		foundMoment = oldFoundMoment;
		insert = oldInsert;
		result = ProductExpression.create(newTerms);
	}
}
