package uk.ac.imperial.doc.pctmc.odeanalysis;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import uk.ac.imperial.doc.pctmc.representation.EvolutionEvent;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.representation.State;
import uk.ac.imperial.doc.pctmc.statements.odeanalysis.ODEMethod;
import uk.ac.imperial.doc.pctmc.utils.Binomial;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class NewODEGenerator {
	
	protected IMomentClosure momentClosure;
	
	protected PCTMC pctmc;
	
	protected Map<CombinedPopulationProduct, AbstractExpression> rhs;
	protected Set<CombinedPopulationProduct> processing;
	
	protected BiMap<CombinedPopulationProduct, Integer> momentIndex;
	
	public ODEMethod getODEMethodWithCombinedMoments(Collection<CombinedPopulationProduct> combinedMoments) {
		generateODESystem(combinedMoments);
		AbstractStatement[] ret = new AbstractStatement[rhs.keySet().size()];
		int i = 0;
		for (Map.Entry<CombinedPopulationProduct, AbstractExpression> e:rhs.entrySet()) {
			ret[i++] = new Assignment(CombinedProductExpression
					.create(e.getKey()), e.getValue());
		}			
		return new ODEMethod(ret);
	}
	
	protected void generateODESystem(Collection<CombinedPopulationProduct> usedMoments) {
		rhs = new HashMap<CombinedPopulationProduct, AbstractExpression>();
		processing = new HashSet<CombinedPopulationProduct>();
		for (CombinedPopulationProduct moment:usedMoments) {
			generateODEforCombinedMoment(moment);
		}
		// Generates moment index
		int i = 0;
		momentIndex = HashBiMap.<CombinedPopulationProduct, Integer>create();
		for (CombinedPopulationProduct m:rhs.keySet()) {
			momentIndex.put(m, i++);
		}
	}
	
	

	public NewODEGenerator(PCTMC pctmc, IMomentClosure momentClosure) {
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
			derivative = getDerivativeOfMoment(moment.getNakedProduct());
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

		for (Multiset.Entry<PopulationProduct> accumulatedMoment : combinedProduct
				.getAccumulatedProducts().entrySet()) {
			DoubleExpression coefficient = new DoubleExpression(
					(double) accumulatedMoment.getCount());
			Multiset<PopulationProduct> newAccumulatedMoments = HashMultiset
					.<PopulationProduct> create();
			for (Multiset.Entry<PopulationProduct> e : combinedProduct
					.getAccumulatedProducts().entrySet()) {
				newAccumulatedMoments.add(e.getElement(), e.getCount());
			}
			newAccumulatedMoments.remove(accumulatedMoment.getElement(), 1);
			PopulationProduct newNakedMoment = PopulationProduct.getProduct(
					combinedProduct.getNakedProduct(), accumulatedMoment
							.getElement());
			CombinedPopulationProduct tmp = new CombinedPopulationProduct(
					newNakedMoment, newAccumulatedMoments);
			AbstractExpression diff = ProductExpression.create(coefficient,
					CombinedProductExpression.create(tmp));
			sum.add(diff);
		}
		if (combinedProduct.getNakedProduct().getOrder() > 0) {
			CombinedPopulationProduct nakedMoment = new CombinedPopulationProduct(
					combinedProduct.getNakedProduct());
			generateODEforCombinedMoment(nakedMoment);
			AbstractExpression diffNakedMoment = rhs.get(nakedMoment);
			// Need to insert the accumulated moments into the RHS			
			sum.add(momentClosure.insertAccumulations(diffNakedMoment, new CombinedPopulationProduct(null, combinedProduct
							.getAccumulatedProducts())));
		}
		return SumExpression.create(sum);
	}

	protected AbstractExpression getDerivativeOfMoment(PopulationProduct moment) {		
		AbstractExpression ret = DoubleExpression.ZERO;
		// Finds all events that affect the moment value
		for (EvolutionEvent event : pctmc.getEvolutionEvents()) {
			Map<State, Integer> changeVector = event.getChangeVector();
			Map<State, Integer> unchangedPopulations = new HashMap<State, Integer>();
			Map<State, Integer> changingPopulations = new HashMap<State, Integer>();
			for (Entry<State, Integer> e : moment.getRepresentation()
					.entrySet()) {
				if (!changeVector.containsKey(e.getKey())) {
					unchangedPopulations.put(e.getKey(),
							moment.getPowerOf(e.getKey()));
				} else {
					changingPopulations.put(e.getKey(),
							moment.getPowerOf(e.getKey()));
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
				for (Entry<State, Integer> e : new PopulationProduct(
						changingPopulations).getRepresentation().entrySet()) {
					List<CoefficientMoment> newSummands = new LinkedList<CoefficientMoment>();
					// Expansion of (X_p + delta_p)^k_p
					for (int i = 0; i <= e.getValue(); i++) {
						DoubleExpression binomialCoefficient = new DoubleExpression(new Double(
								Binomial.choose(
										e.getValue(), i)));
						DoubleExpression powerOfDelta = new DoubleExpression(
								Math.pow(changeVector.get(e
										.getKey()),
										e.getValue() - i));
						for (CoefficientMoment c : summands) {
							AbstractExpression newCoefficient = ProductExpression
									.create(c.coefficient,
											binomialCoefficient,
											powerOfDelta);
							Multiset<State> representation = c.moment
									.asMultiset();
							representation.add(e.getKey(), i);
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
						GetVVersionVisitorMomentClosure getVVersion = new GetVVersionVisitorMomentClosure(
								c.moment, 2);
						event.getRate().accept(getVVersion);
						tmp.add(ProductExpression.create(c.coefficient,
								getVVersion.getResult()));
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

	public BiMap<CombinedPopulationProduct, Integer> getMomentIndex() {
		return momentIndex;
	}
}