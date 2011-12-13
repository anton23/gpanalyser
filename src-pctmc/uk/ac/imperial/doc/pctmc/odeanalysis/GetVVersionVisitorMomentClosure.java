package uk.ac.imperial.doc.pctmc.odeanalysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ExpressionCondition;
import uk.ac.imperial.doc.jexpressions.expressions.IndicatorFunction;
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
	
	public GetVVersionVisitorMomentClosure(PopulationProduct moment,
			int maxOrder) {
		super(moment);
		this.maxOrder = maxOrder;
	}

	@Override
	public void visit(PEPADivExpression e) {
		if (insert) {
			e.getNumerator().accept(this);
			result = PEPADivExpression.create(result, e.getDenominator());
		} else {
			insert = true;
			e.getNumerator().accept(this);
			AbstractExpression newNumerator = result;
			e.getDenominator().accept(this);
			insert = false;
			result = PEPADivExpression.create(newNumerator, result);
		}
	}
	
	@Override
	public void visit(IndicatorFunction e) {
		boolean oldInserted = inserted;
		boolean oldInsert = insert;
		insert = true;
		e.getCondition().getLeft().accept(this);
		AbstractExpression newLeft = result;
		
		e.getCondition().getRight().accept(this);
		AbstractExpression newRight = result;
		insert = oldInsert;
		inserted = oldInserted;
		result = new IndicatorFunction(
				new ExpressionCondition(newLeft, e.getCondition().getOperator(), newRight));
	}

	@Override
	public void visit(PopulationExpression e) {
		CombinedPopulationProduct product;
		if (insert) {
			// TODO handle case if (moment.getOrder()>= maxOrder)
			product = new CombinedPopulationProduct(moment.getV(e.getState()));
			inserted = true;
		} else {
			product = new CombinedPopulationProduct(PopulationProduct
					.getMeanProduct(e.getState()));
		}
		result = CombinedProductExpression.create(product);
	}

	protected boolean inserted = false;

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
	
	public AbstractExpression getEventMomentInTermsOfCovariances(State[] states) {
		assert(states.length % 2 == 0);
		
		List<AbstractExpression> summands = new LinkedList<AbstractExpression>();
		summands.add(getOddMomentInTermsOfCovariances(states));
		for (Set<List<State>> partition:GetVVersionVisitorMomentClosure.<State>getAllPartitionsIntoPairs(Arrays.asList(states))) {
			// Needs to evaluate the product E[(X1-u1)(X2-u2)]*E[(X3-u3)(X4-u4)]*...* + E[(X1-u1)(X3-u3)]*... + ...
			List<List<State>> tmp = new ArrayList<List<State>>(partition);
			for (long i = 0; i<Math.pow(2,partition.size()); i++){
				long iBit = i;
				int j = 0;
				int sign = 1;
				List<AbstractExpression> terms = new LinkedList<AbstractExpression>();
				while (j < partition.size()) {
					CombinedPopulationProduct m1 = CombinedPopulationProduct.getMeanPopulation(tmp.get(j).get(0));
					CombinedPopulationProduct m2 = CombinedPopulationProduct.getMeanPopulation(tmp.get(j).get(1));
					CombinedPopulationProduct m1m2 = CombinedPopulationProduct.getProductOf(m1, m2);
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
	
	// This is quite inefficient
	public static <T> Set<Set<List<T>>> getAllPartitionsIntoPairs(List<T> l) {
		assert(l.size() % 2 == 0);
		Set<Set<List<T>>> ret = new HashSet<Set<List<T>>>();
		if (l.size()==2) {
			Set<List<T>> tmp = new HashSet<List<T>>();
			tmp.add(l);
			ret.add(tmp);
			return ret;
		}
		for (int i = 0; i<l.size(); i++) {
			for (int j = i+1; j<l.size(); j++) {
					List<T> smaller = new ArrayList<T>(l.size()-2);
					for (int k = 0; k<l.size(); k++) {
						if (k!=j && k!=i) {
							smaller.add(l.get(k));
						}
					}
					List<T> pair = new LinkedList<T>();
					pair.add(l.get(i));
					pair.add(l.get(j));
					Set<Set<List<T>>> smallerPartitions = getAllPartitionsIntoPairs(smaller);
					for (Set<List<T>> partition:smallerPartitions) {
						partition.add(pair);
						ret.add(partition);
					}
			}
		}
		return ret;
	}
	
}
