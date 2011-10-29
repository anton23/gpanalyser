package uk.ac.imperial.doc.pctmc.odeanalysis;

import java.util.LinkedList;
import java.util.List;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.PEPADivExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.PopulationExpression;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;
import uk.ac.imperial.doc.pctmc.representation.State;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class GetVVersionVisitorMomentClosure extends GetVVersionVisitor {

	protected boolean insert = true;

	@Override
	public void visit(PEPADivExpression e) {
		if (insert) {
			e.getNumerator().accept(this);
			result = PEPADivExpression.create(result, e.getDenominator());
		} else {
			e.getNumerator().accept(this);
			AbstractExpression newNumerator = result;
			e.getDenominator().accept(this);
			result = PEPADivExpression.create(newNumerator, result);
		}
	}

	public GetVVersionVisitorMomentClosure(PopulationProduct moment,
			int maxOrder) {
		super(moment);
		this.maxOrder = maxOrder;
	}

	@Override
	public void visit(PopulationExpression e) {
		CombinedPopulationProduct product;
		if (insert) {
			product = new CombinedPopulationProduct(moment.getV(e.getState()));
			inserted = true;
		} else {
			product = new CombinedPopulationProduct(PopulationProduct
					.getMeanProduct(e.getState()));
		}
		result = CombinedProductExpression.create(product);
	}

	private boolean inserted = false;

	@Override
	public void visit(ProductExpression e) {
		List<AbstractExpression> terms = new LinkedList<AbstractExpression>();
		boolean oldInsert = insert;
		boolean isInserted = false;
		for (AbstractExpression t : e.getTerms()) {
			inserted = false;
			t.accept(this);
			isInserted = inserted;
			if (isInserted) {
				insert = false;
			}
			terms.add(result);
		}
		insert = oldInsert;
		result = ProductExpression.create(terms);
	}

	int maxOrder;

	@Override
	public void visit(CombinedProductExpression e) {
		// TODO investigate normal approximation, i.e.
		if (e.getProduct().getAccumulatedProducts().size() > 0) {
			throw new AssertionError("Accumulations not allowed in rates!");
		}
		PopulationProduct nakedProduct = e.getProduct().getNakedProduct();
		int order = moment.getOrder() + nakedProduct.getOrder();
		if (order <= maxOrder) {
			result = CombinedProductExpression
					.create(new CombinedPopulationProduct(PopulationProduct
							.getProduct(moment, nakedProduct)));
		} else if (order % 2 == 1) {
			State[] x = new State[order];
			int i = 0;
			for (State s : PopulationProduct.getProduct(moment, nakedProduct)
					.asMultiset()) {
				if (s != null)
					x[i++] = s;
			}
			result = getOddMomentInTermsOfCovariances(x);
		} else {

			Multiset<State> momentMset = moment.asMultiset();
			Multiset<State> nakedMset = nakedProduct.asMultiset();
			Multiset<State> remains = HashMultiset.<State> create();

			for (State s : nakedMset.elementSet()) {
				int count = nakedMset.count(s);
				int toAdd = Math.min(count, Math.max(maxOrder
						- momentMset.size(), 0));
				count -= toAdd;
				momentMset.add(s, toAdd);
				remains.add(s, count);
			}
			List<AbstractExpression> terms = new LinkedList<AbstractExpression>();
			terms.add(CombinedProductExpression
					.create(new CombinedPopulationProduct(
							new PopulationProduct(momentMset))));
			for (State s : remains.elementSet()) {
				int count = remains.count(s);
				for (int i = 0; i < count; i++) {
					CombinedPopulationProduct product = new CombinedPopulationProduct(
							PopulationProduct.getMeanProduct(s));
					terms.add(CombinedProductExpression.create(product));
				}
			}

			result = ProductExpression.create(terms);
		}
	}

	public AbstractExpression getOddMomentInTermsOfCovariances(State[] states) {
		List<AbstractExpression> summands = new LinkedList<AbstractExpression>();
		double numberOfTerms = Math.pow(2.0, states.length);
		for (long i = 1; i < numberOfTerms; i++) {
			List<AbstractExpression> terms = new LinkedList<AbstractExpression>();
			Multiset<State> product = HashMultiset.<State> create();
			long tmp = i;
			int j = 0;
			int sign = 1;
			while (j < states.length) {
				if (tmp % 2 == 0) {
					product.add(states[j]);
				} else {
					terms
							.add(CombinedProductExpression
									.create(new CombinedPopulationProduct(
											PopulationProduct
													.getMeanProduct(states[j]))));
					sign = -sign;
				}
				tmp /= 2;
				j++;
			}
			terms.add(CombinedProductExpression
					.create(new CombinedPopulationProduct(
							new PopulationProduct(product))));
			if (sign == 1) {
				terms.add(new DoubleExpression(-1.0));
			}
			summands.add(ProductExpression.create(terms));
		}
		return SumExpression.create(summands);
	}
}
