package uk.ac.imperial.doc.pctmc.odeanalysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import uk.ac.imperial.doc.jexpressions.constants.ConstantExpression;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.FunctionCallExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IndicatorFunction;
import uk.ac.imperial.doc.jexpressions.expressions.PEPADivExpression;
import uk.ac.imperial.doc.jexpressions.expressions.PowerExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.jexpressions.variables.IExpressionVariableVisitor;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.ICombinedProductExpressionVisitor;
import uk.ac.imperial.doc.pctmc.expressions.PopulationExpression;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;
import uk.ac.imperial.doc.pctmc.representation.State;

import com.google.common.collect.Multiset;

public class AccumulatedNormalClosureVisitorUniversal extends
		MomentCountTransformerWithParameters implements
		ICombinedProductExpressionVisitor, IExpressionVariableVisitor {

	protected boolean insert;
	protected boolean inserted;
	protected int maxOrder;

	protected CombinedPopulationProduct moment;

	public AccumulatedNormalClosureVisitorUniversal(
			CombinedPopulationProduct moment, int maxOrder) {
		this.maxOrder = maxOrder;
		this.moment = moment;
		this.inserted = false;
		this.insert = true;
	}
	
	@Override
	public void visit(PowerExpression e) {
		onlyMultiply(e);
	}
	

	@Override
	public void visit(ExpressionVariable e) {
		if (moment.getOrder() == 0 ) {
			result = e;
		} else {
			e.getUnfolded().accept(this);
		}
	}

	@Override
	public void visit(PEPADivExpression e) {
		if (insert) {
			e.getNumerator().accept(this);
			result = PEPADivExpression.create(result, e.getDenominator());
			inserted = true;
		} else {
			boolean oldInsert = insert;
			insert = true;
			e.getNumerator().accept(this);
			AbstractExpression newNumerator = result;
			e.getDenominator().accept(this);
			insert = oldInsert;
			result = PEPADivExpression.create(newNumerator, result);
		}
	}
	
	protected void onlyMultiply(AbstractExpression e) {
		if (insert) {
			result = ProductExpression.create(e, CombinedProductExpression
					.create(moment));
			inserted = true;
		} else {
			result = e;
		}
	}

	@Override
	public void visit(ConstantExpression e) {
		onlyMultiply(e);
	}

	@Override
	public void visit(DoubleExpression e) {
		onlyMultiply(e);
	}

	@Override
	public void visit(IndicatorFunction e) {
		result = e;
	}

	@Override
	public void visit(PopulationExpression e) {
		CombinedPopulationProduct product;
		if (insert) {
			// TODO handle case if (moment.getOrder()>= maxOrder)
			product = new CombinedPopulationProduct(moment.getNakedProduct()
					.getV(e.getState()));
			inserted = true;
		} else {
			product = new CombinedPopulationProduct(PopulationProduct
					.getMeanProduct(e.getState()));
		}
		result = CombinedProductExpression.create(product);
	}

	@Override
	public void visit(ProductExpression e) {
		List<AbstractExpression> terms = new LinkedList<AbstractExpression>();
		boolean oldInsert = insert;
		boolean oldInserted = inserted;
		boolean isInserted = false;
		for (AbstractExpression t : e.getTerms()) {
			inserted = false;
			t.accept(this);
			isInserted |= inserted;
			if (isInserted) {
				insert = false;
			}
			terms.add(result);
		}
		insert = oldInsert;
		inserted = oldInserted | isInserted;
		result = ProductExpression.create(terms);
	}
	
	


	@Override
	public void visit(FunctionCallExpression e) {
		if (insert) {
			result = ProductExpression.create(e, CombinedProductExpression
					.create(moment));
			inserted = true;
		} else {
			result = e;
		}
	}
	
	@Override
	public void visit(DivExpression e) {
		onlyMultiply(e);
	}

	@Override
	public void visit(CombinedProductExpression e) {
		if (insert) {
			inserted = true;
			int order = moment.getOrder() + e.getProduct().getOrder();
			if (order <= maxOrder) {
				result = CombinedProductExpression
						.create(CombinedPopulationProduct.getProductOf(moment,
								e.getProduct()));
			} else if (maxOrder == 1) {
				List<AbstractExpression> terms = new LinkedList<AbstractExpression>();

				for (Entry<State, Integer> entry : moment.getNakedProduct()
						.getRepresentation().entrySet()) {
					for (int i = 0; i < entry.getValue(); i++) {
						terms.add(CombinedProductExpression
								.create(CombinedPopulationProduct
										.getMeanPopulation(entry.getKey())));
					}
				}

				for (Multiset.Entry<PopulationProduct> entry : moment
						.getAccumulatedProducts().entrySet()) {
					for (int i = 0; i < entry.getCount(); i++) {
						terms.add(CombinedProductExpression
								.create(CombinedPopulationProduct
										.getMeanAccumulatedProduct(entry
												.getElement())));
					}
				}
				for (Entry<State, Integer> entry : e.getProduct()
						.getNakedProduct().getRepresentation().entrySet()) {
					for (int i = 0; i < entry.getValue(); i++) {
						terms.add(CombinedProductExpression
								.create(CombinedPopulationProduct
										.getMeanPopulation(entry.getKey())));
					}
				}
				for (com.google.common.collect.Multiset.Entry<PopulationProduct> entry : e
						.getProduct().getAccumulatedProducts().entrySet()) {
					for (int i = 0; i < entry.getCount(); i++) {
						terms.add(CombinedProductExpression
								.create(CombinedPopulationProduct
										.getMeanAccumulatedProduct(entry
												.getElement())));
					}
				}
				result = ProductExpression.create(terms);
			} else {
				CombinedPopulationProduct[] x = new CombinedPopulationProduct[order];
				int i = 0;
				for (State s : e.getProduct().getNakedProduct().asMultiset()) {
					if (s != null)
						x[i++] = CombinedPopulationProduct.getMeanPopulation(s);
				}
				for (State s : moment.getNakedProduct().asMultiset()) {
					if (s != null)
						x[i++] = CombinedPopulationProduct.getMeanPopulation(s);
				}
				for (PopulationProduct p : e.getProduct()
						.getAccumulatedProducts()) {
					x[i++] = CombinedPopulationProduct
							.getMeanAccumulatedProduct(p);
				}
				for (PopulationProduct p : moment.getAccumulatedProducts()) {
					x[i++] = CombinedPopulationProduct
							.getMeanAccumulatedProduct(p);
				}
				if (order % 2 == 1) {
					result = AccumulatedNormalClosureVisitorUniversal
							.getOddMomentInTermsOfCovariances(x);
				} else {
					result = AccumulatedNormalClosureVisitorUniversal
							.getEventMomentInTermsOfCovariances(x);
				}
			}
		} else {
			result = e;
		}
	}

	public static AbstractExpression getOddMomentInTermsOfCovariances(
			CombinedPopulationProduct[] states) {
		List<AbstractExpression> summands = new LinkedList<AbstractExpression>();
		double numberOfTerms = Math.pow(2.0, states.length);
		for (long i = 1; i < numberOfTerms; i++) {
			List<AbstractExpression> terms = new LinkedList<AbstractExpression>();
			CombinedPopulationProduct product = null;
			long tmp = i;
			int j = 0;
			int sign = 1;
			while (j < states.length) {
				if (tmp % 2 == 0) {
					if (product == null) {
						product = states[j];
					} else {
						product = CombinedPopulationProduct.getProductOf(
								product, states[j]);
					}
				} else {
					terms.add(CombinedProductExpression.create(states[j]));
					sign = -sign;
				}
				tmp /= 2;
				j++;
			}
			if (product != null) {
				terms.add(CombinedProductExpression.create(product));
			}
			if (sign == 1) {
				terms.add(new DoubleExpression(-1.0));
			}
			summands.add(ProductExpression.create(terms));
		}
		return SumExpression.create(summands);
	}

	public static AbstractExpression getEventMomentInTermsOfCovariances(
			CombinedPopulationProduct[] states) {
		assert (states.length % 2 == 0);

		List<AbstractExpression> summands = new LinkedList<AbstractExpression>();
		summands.add(getOddMomentInTermsOfCovariances(states));
		Set<Set<List<CombinedPopulationProduct>>> allPartitionsIntoPairs = GetVVersionVisitorMomentClosure
				.<CombinedPopulationProduct> getAllPartitionsIntoPairs(Arrays
						.asList(states));
		for (Set<List<CombinedPopulationProduct>> partition : allPartitionsIntoPairs) {
			// Needs to evaluate the product
			// E[(X1-u1)(X2-u2)]*E[(X3-u3)(X4-u4)]*...* + E[(X1-u1)(X3-u3)]*...
			// + ...
			List<List<CombinedPopulationProduct>> tmp = new ArrayList<List<CombinedPopulationProduct>>(
					partition);
			for (long i = 0; i < Math.pow(2, partition.size()); i++) {
				long iBit = i;
				int j = 0;
				int sign = 1;
				List<AbstractExpression> terms = new LinkedList<AbstractExpression>();
				while (j < partition.size()) {
					CombinedPopulationProduct m1 = tmp.get(j).get(0);
					CombinedPopulationProduct m2 = tmp.get(j).get(1);
					CombinedPopulationProduct m1m2 = CombinedPopulationProduct
							.getProductOf(m1, m2);
					if (iBit % 2 == 0) {
						terms.add(CombinedProductExpression.create(m1m2));
					} else {
						sign *= -1;
						terms.add(CombinedProductExpression.create(m1));
						terms.add(CombinedProductExpression.create(m2));
					}
					iBit /= 2;
					j++;
				}
				if (sign == -1) {
					terms.add(new DoubleExpression(-1.0));
				}
				summands.add(ProductExpression.create(terms));
			}
		}
		return SumExpression.create(summands);
	}

}
