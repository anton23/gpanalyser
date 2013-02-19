package uk.ac.imperial.doc.pctmc.odeanalysis;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;
import uk.ac.imperial.doc.jexpressions.statements.AbstractStatement;
import uk.ac.imperial.doc.jexpressions.statements.Assignment;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.CollectUsedMomentsVisitor;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;
import uk.ac.imperial.doc.pctmc.odeanalysis.closures.MomentClosure;
import uk.ac.imperial.doc.pctmc.representation.EvolutionEvent;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.representation.State;
import uk.ac.imperial.doc.pctmc.representation.accumulations.AccumulationVariable;
import uk.ac.imperial.doc.pctmc.statements.odeanalysis.ODEMethod;
import uk.ac.imperial.doc.pctmc.utils.Binomial;
import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class NewODEGenerator {
	
	protected MomentClosure momentClosure;
	
	protected PCTMC pctmc;
	
	protected Map<CombinedPopulationProduct, AbstractExpression> rhs;
	protected Set<CombinedPopulationProduct> processing;
	
	protected Map<CombinedPopulationProduct, Integer> momentIndex;
	
	public ODEMethod getODEMethodWithCombinedMoments(Collection<CombinedPopulationProduct> combinedMoments) {
		generateODESystem(combinedMoments);
		AbstractStatement[] ret = new AbstractStatement[rhs.keySet().size()];
		int i = 0;
		for (Map.Entry<CombinedPopulationProduct, AbstractExpression> e:rhs.entrySet()) {
			ret[i++] = new Assignment(CombinedProductExpression
					.create(e.getKey()), e.getValue());
		}			

		return new ODEMethod(ret, momentClosure.getVariables());
	}
	
	protected void generateODESystem(Collection<CombinedPopulationProduct> usedMoments) {
		PCTMCLogging.info("Generating the underlying ODE system.");
		rhs = new HashMap<CombinedPopulationProduct, AbstractExpression>();
		processing = new HashSet<CombinedPopulationProduct>();
		for (CombinedPopulationProduct moment:usedMoments) {
			generateODEforCombinedMoment(moment);
		}
		// Generates moment index
		int i = 0;
		momentIndex = new HashMap<CombinedPopulationProduct, Integer>();
		for (CombinedPopulationProduct m:rhs.keySet()) {
			momentIndex.put(m, i++);
		}
		PCTMCLogging.info("The total number of ODEs is " + i);
	}
	
	

	public NewODEGenerator(PCTMC pctmc, MomentClosure momentClosure) {
		super();
		this.pctmc = pctmc;
		this.momentClosure = momentClosure;
	}

	private class CoefficientMoment {
		AbstractExpression coefficient;
		PopulationProduct moment;

		public CoefficientMoment(AbstractExpression coefficient,
				PopulationProduct moment) {
			super();
			this.coefficient = coefficient;
			this.moment = moment;
		}

		@Override
		public String toString() {
			return coefficient.toString() + "*" + moment.toString();
		}
	}
	
	protected void generateODEforCombinedMoment(CombinedPopulationProduct moment) {
		if (rhs.containsKey(moment) || processing.contains(moment)) {
			return;
		}
		processing.add(moment);
		AbstractExpression derivative;
		if (moment.getAccumulatedProducts().isEmpty()) {
			derivative = getDerivativeOfMoment(moment.getPopulationProduct());
		} else {
			derivative = getDerivativeOfAccumulatedMoment(moment);
		}
		
		rhs.put(moment, derivative);		
		// Generates ODEs for moments occurring on the RHS
		CollectUsedMomentsVisitor visitor = new CollectUsedMomentsVisitor();
		derivative.accept(visitor);
		for (CombinedPopulationProduct neededMoment : visitor
				.getUsedCombinedMoments()) {
			generateODEforCombinedMoment(neededMoment);
		}		
	}
	
	private AbstractExpression getDerivativeOfAccumulatedMoment(
			CombinedPopulationProduct combinedProduct) {
		List<AbstractExpression> sum = new LinkedList<AbstractExpression>();

		for (Multiset.Entry<AccumulationVariable> accumulatedMoment : combinedProduct
				.getAccumulatedProducts().entrySet()) {
			DoubleExpression coefficient = new DoubleExpression(
					(double) accumulatedMoment.getCount());
			Multiset<AccumulationVariable> newAccumulatedMoments = HashMultiset
					.<AccumulationVariable> create();
			for (Multiset.Entry<AccumulationVariable> e : combinedProduct
					.getAccumulatedProducts().entrySet()) {
				newAccumulatedMoments.add(e.getElement(), e.getCount());
			}
			newAccumulatedMoments.remove(accumulatedMoment.getElement(), 1);
			
			AbstractExpression product = momentClosure.insertProductIntoRate(accumulatedMoment.getElement().getDdt(), combinedProduct.getPopulationProduct());
			product = momentClosure.insertAccumulations(product, new CombinedPopulationProduct(null, newAccumulatedMoments));

			AbstractExpression diff = ProductExpression.create(coefficient, product);
			sum.add(diff);
		}
		if (combinedProduct.getPopulationProduct().getOrder() > 0) {
			CombinedPopulationProduct nakedMoment = new CombinedPopulationProduct(
					combinedProduct.getPopulationProduct());
			generateODEforCombinedMoment(nakedMoment);
			AbstractExpression diffNakedMoment = rhs.get(nakedMoment);
			// Need to insert the accumulated moments into the RHS			
			AbstractExpression insertAccumulations = momentClosure.insertAccumulations(diffNakedMoment, new CombinedPopulationProduct(null, combinedProduct
							.getAccumulatedProducts()));
			sum.add(insertAccumulations);
		}
		AbstractExpression result = SumExpression.create(sum);
		
		return result;
	}

	protected AbstractExpression getDerivativeOfMoment(PopulationProduct moment) {		
		AbstractExpression ret = DoubleExpression.ZERO;
		// Finds all events that affect the moment value
		for (EvolutionEvent event : pctmc.getEvolutionEvents()) {
			Map<State, Integer> changeVector = event.getChangeVector();
			Multiset<State> unchangedPopulations = HashMultiset.create();
			Multiset<State> changingPopulations = HashMultiset.create();
			for (Multiset.Entry<State> e : moment.getRepresentation().entrySet()) {
				if (!changeVector.containsKey(e.getElement())) {
					unchangedPopulations.add(e.getElement(),
							moment.getPowerOf(e.getElement()));
				} else {
					changingPopulations.add(e.getElement(),
							moment.getPowerOf(e.getElement()));
				}
			}
			// The event contributes to the RHS only if one of the 
			// populations within the moment changes
			if (!changingPopulations.isEmpty()) {
				// Expands M(X + delta) - M(X) into a linear combination of moments
				List<CoefficientMoment> summands = new LinkedList<CoefficientMoment>();
				// Each term will be divisible by the part of the moment that doesn't change
				summands.add(new CoefficientMoment(DoubleExpression.ONE,
						new PopulationProduct(unchangedPopulations)));
				// Goes through each changing population p and expands (X_p+delta_p)^k_p
				// and multiplies all the terms collected so far
				for (Multiset.Entry<State> e : new PopulationProduct(
						changingPopulations).getRepresentation().entrySet()) {
					List<CoefficientMoment> newSummands = new LinkedList<CoefficientMoment>();
					// Expansion of (X_p + delta_p)^k_p
					for (int i = 0; i <= e.getCount(); i++) {
						DoubleExpression binomialCoefficient = new DoubleExpression(new Double(
								Binomial.choose(
										e.getCount(), i)));
						DoubleExpression powerOfDelta = new DoubleExpression(
								Math.pow(changeVector.get(e
										.getElement()),
										e.getCount() - i));
						for (CoefficientMoment c : summands) {
							AbstractExpression newCoefficient = ProductExpression
									.create(c.coefficient,
											binomialCoefficient,
											powerOfDelta);
							Multiset<State> representation = c.moment
									.asMultiset();
							representation.add(e.getElement(), i);
							newSummands.add(new CoefficientMoment(
									newCoefficient, new PopulationProduct(
											representation)));
						}
					}
					summands = newSummands;
				}
				List<AbstractExpression> tmp = new LinkedList<AbstractExpression>();
				// Multiplies each moment by the rate, according to a provided moment closure
				for (CoefficientMoment c : summands) {
					if (c.moment.getOrder() < moment.getOrder()) {
						AbstractExpression closedRate = momentClosure.insertProductIntoRate(event.getRate(), c.moment);
						tmp.add(ProductExpression.create(c.coefficient,
								closedRate));
					}
				}

				AbstractExpression eventContribution = SumExpression
						.create(tmp);
				ret = SumExpression.create(ret, eventContribution);
			}
		}
		return ret;		
	}

	public Map<CombinedPopulationProduct, AbstractExpression> getRhs() {
		return rhs;
	}

	public Map<CombinedPopulationProduct, Integer> getMomentIndex() {
		return momentIndex;
	}
	
	public AbstractExpression getRHS(CombinedPopulationProduct moment) { 
		return rhs.get(moment);
	}
}