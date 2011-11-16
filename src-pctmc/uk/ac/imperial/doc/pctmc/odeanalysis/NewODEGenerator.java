package uk.ac.imperial.doc.pctmc.odeanalysis;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;
import uk.ac.imperial.doc.pctmc.representation.EvolutionEvent;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.representation.State;
import uk.ac.imperial.doc.pctmc.utils.Binomial;

import com.google.common.collect.Multiset;

public class NewODEGenerator {
	protected PCTMC pctmc;
	
	
	
	public NewODEGenerator(PCTMC pctmc) {
		super();
		this.pctmc = pctmc;
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
	
	protected AbstractExpression generateODE(PopulationProduct moment) {
		AbstractExpression ret = DoubleExpression.ZERO;
		for (EvolutionEvent event:pctmc.getEvolutionEvents()) {
			Map<State, Integer> changeVector = event.getChangeVector();
			Map<State, Integer> unchangedPopulations = new HashMap<State, Integer>();
			Map<State, Integer> changingPopulations = new HashMap<State, Integer>();
			for (Entry<State, Integer> e: moment.getRepresentation().entrySet()) {
				if (!changeVector.containsKey(e.getKey())) {
					unchangedPopulations.put(e.getKey(), moment.getPowerOf(e.getKey()));
				} else {
					changingPopulations.put(e.getKey(), moment.getPowerOf(e.getKey()));
				}
			}
			List<CoefficientMoment> summands = new LinkedList<CoefficientMoment>();
			summands.add(new CoefficientMoment(DoubleExpression.ONE, new PopulationProduct(unchangedPopulations)));
			for (Entry<State, Integer> e: new PopulationProduct(changingPopulations).getRepresentation().entrySet()) {
				List<CoefficientMoment> newSummands = new LinkedList<CoefficientMoment>();
				for (int i = 0; i<=e.getValue(); i++) {
					for (CoefficientMoment c:summands) {
						AbstractExpression newCoefficient = ProductExpression.create(c.coefficient,
							new DoubleExpression(new Double(Binomial.choose(e.getValue(), i))),
							new DoubleExpression(Math.pow(changeVector.get(e.getKey()), e.getValue()-i)));
						Multiset<State> representation = c.moment.asMultiset();
						representation.add(e.getKey(), i);
						newSummands.add(new CoefficientMoment(newCoefficient, new PopulationProduct(representation)));
					}
				}
				summands = newSummands;
			}
			List<AbstractExpression> tmp = new LinkedList<AbstractExpression>();
			for (CoefficientMoment c:summands) {
				if (c.moment.getOrder() < moment.getOrder()) {
					GetVVersionVisitorMomentClosure getVVersion = new GetVVersionVisitorMomentClosure(c.moment, 2);
					event.getRate().accept(getVVersion);
					tmp.add(ProductExpression.create(c.coefficient, getVVersion.getResult()));					
				}
			}

			AbstractExpression eventContribution = SumExpression.create(tmp);			
			ret = SumExpression.create(ret, eventContribution);
			
		}
		return ret;
	}
}