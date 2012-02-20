package uk.ac.imperial.doc.pctmc.odeanalysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.FunctionCallExpression;
import uk.ac.imperial.doc.jexpressions.expressions.MinExpression;
import uk.ac.imperial.doc.jexpressions.expressions.MinusExpression;
import uk.ac.imperial.doc.jexpressions.expressions.PEPADivExpression;
import uk.ac.imperial.doc.jexpressions.expressions.PowerExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.CentralMomentOfLinearCombinationExpression;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.CovarianceOfLinearCombinationsExpression;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;
import uk.ac.imperial.doc.pctmc.representation.State;

import com.google.common.collect.Lists;

public class AccumulatedNormalClosureVisitor extends GetVVersionVisitorMomentClosure {

	public AccumulatedNormalClosureVisitor(PopulationProduct moment,
			int maxOrder) {
		super(moment, maxOrder);
	}
	
	@Override
	public void visit(MinExpression e) {
			if (insert) {
				//if (moment.getOrder() == 0) {
					AbstractExpression muA = e.getA();
					AbstractExpression muB = e.getB();
					

					
					Map<ExpressionVariable, AbstractExpression> var = new HashMap<ExpressionVariable, AbstractExpression>();
					AbstractExpression covAB = new CovarianceOfLinearCombinationsExpression(muA, muB, var);
					AbstractExpression varA = new CentralMomentOfLinearCombinationExpression(
							muA, 2, var);
					AbstractExpression varB = new CentralMomentOfLinearCombinationExpression(
							muB, 2, var);
					AbstractExpression theta = PowerExpression
							.create(
									SumExpression.create(varA, varB,
											ProductExpression.create(
													new DoubleExpression(-2.0),
													covAB)),
									new DoubleExpression(0.5));
					
					
					if (e.getB().equals(new DoubleExpression(0.0))) {
						AbstractExpression nonNegative = FunctionCallExpression.create("max", Lists.newArrayList(varA, new DoubleExpression(0.0)));
						theta = FunctionCallExpression.create("sqrt", Lists.newArrayList(nonNegative));
					}
					
					
					AbstractExpression muA2 = e.getA();
					AbstractExpression muB2 = e.getB();
					AbstractExpression theta2 = theta;
					if (moment.getOrder() > 0) {
						muA.accept(this);
						muA2 = result;
						inserted = false;
						muB.accept(this);
						muB2 = result;						
						theta2 = ProductExpression.create(CombinedProductExpression.create(new CombinedPopulationProduct(moment)), theta);
					}
					MinusExpression mAmB = new MinusExpression(muA, muB);
					AbstractExpression arg1 = PEPADivExpression.create(
							mAmB, theta);
					MinusExpression mBmA = new MinusExpression(muB, muA);
					AbstractExpression arg2 = PEPADivExpression.create(
							mBmA, theta);
					result = SumExpression.create(ProductExpression.create(muA2,
							FunctionCallExpression.create("phiC", Lists
									.newArrayList(arg2, mBmA, theta))), ProductExpression
							.create(muB2, FunctionCallExpression.create("phiC",
									Lists.newArrayList(arg1, mAmB, theta))),
							ProductExpression.create(
									new DoubleExpression(-1.0), theta2,
									FunctionCallExpression.create("phi", Lists
											.newArrayList(arg2, mBmA, theta))));
					inserted = true;
			} else {
				result = e;
			}	
	}
	
	@Override
	public void visit(FunctionCallExpression e) {
		//result = e;
		if (insert) {
			/*List<AbstractExpression> newArguments = new LinkedList<AbstractExpression>();
			for (AbstractExpression a:e.getArguments()) {
				a.accept(this);
				newArguments.add(result);
			}
			result = FunctionCallExpression.create(e.getName(), newArguments);*/
			result = ProductExpression.create(e, CombinedProductExpression
					.create(new CombinedPopulationProduct(moment)));
			inserted = true;
		} else {
			result = e;
		}
	}

	@Override
	public void visit(CombinedProductExpression e) {
		if (insert) {
		/*if (e.getProduct().getAccumulatedProducts().isEmpty()) {
			super.visit(e);
		} else {*/
			int order = moment.getOrder() + e.getProduct().getOrder();
			if (order <= maxOrder) {
				result = CombinedProductExpression
						.create(CombinedPopulationProduct.getProductOf(new CombinedPopulationProduct(moment), e.getProduct()));
				inserted = true;
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
					inserted = true;
				} /*else {
					throw new AssertionError("Not implemented yet!");
				}*/ else {
					CombinedPopulationProduct[] x = new CombinedPopulationProduct[order];
					int i = 0;
					for (State s : PopulationProduct.getProduct(moment, e.getProduct().getNakedProduct())
							.asMultiset()) {
 						if (s != null)
							x[i++] = CombinedPopulationProduct.getMeanPopulation(s);
					}
					for (PopulationProduct p:e.getProduct().getAccumulatedProducts()) {
						x[i++] = CombinedPopulationProduct.getMeanAccumulatedProduct(p);
					}
					if (order % 2 == 1) {
						result = getOddMomentInTermsOfCovariances(x);
					} else {
						result = getEventMomentInTermsOfCovariances(x);
						
					}
				}
			
//		}
		} else {
			result = e;
		}
	}
	
	
	public static AbstractExpression getOddMomentInTermsOfCovariances(CombinedPopulationProduct[] states) {
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
						product = CombinedPopulationProduct.getProductOf(product, states[j]);
					}
				} else {
					terms
							.add(CombinedProductExpression
									.create(states[j]));
					sign = -sign;
				}
				tmp /= 2;
				j++;
			}
			if (product!=null){
				terms.add(CombinedProductExpression
						.create(product));
			}
			if (sign == 1) {
				terms.add(new DoubleExpression(-1.0));
			}
			summands.add(ProductExpression.create(terms));
		}
		return SumExpression.create(summands);
	}
	
	public static AbstractExpression getEventMomentInTermsOfCovariances(CombinedPopulationProduct[] states) {
		assert(states.length % 2 == 0);
		
		List<AbstractExpression> summands = new LinkedList<AbstractExpression>();
		summands.add(getOddMomentInTermsOfCovariances(states));
		Set<Set<List<CombinedPopulationProduct>>> allPartitionsIntoPairs = GetVVersionVisitorMomentClosure.<CombinedPopulationProduct>getAllPartitionsIntoPairs(Arrays.asList(states));
		for (Set<List<CombinedPopulationProduct>> partition:allPartitionsIntoPairs) {
			// Needs to evaluate the product E[(X1-u1)(X2-u2)]*E[(X3-u3)(X4-u4)]*...* + E[(X1-u1)(X3-u3)]*... + ...
			List<List<CombinedPopulationProduct>> tmp = new ArrayList<List<CombinedPopulationProduct>>(partition);
			for (long i = 0; i<Math.pow(2,partition.size()); i++){
				long iBit = i;
				int j = 0;
				int sign = 1;
				List<AbstractExpression> terms = new LinkedList<AbstractExpression>();
				while (j < partition.size()) {
					CombinedPopulationProduct m1 = tmp.get(j).get(0);
					CombinedPopulationProduct m2 = tmp.get(j).get(1);
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
	
}
