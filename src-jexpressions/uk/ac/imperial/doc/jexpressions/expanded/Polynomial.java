package uk.ac.imperial.doc.jexpressions.expanded;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.PowerExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

public class Polynomial {
	
	private ICoefficientSpecification normaliser;

	private final Map<Multiset<ExpandedExpression>, AbstractExpression> representation;
	
	
	public Polynomial(
			Map<Multiset<ExpandedExpression>, AbstractExpression> representation, ICoefficientSpecification normaliser) {
		this.normaliser = normaliser;
		this.representation = normalise(representation);
	}

	public Polynomial(ICoefficientSpecification normaliser, Multiset<ExpandedExpression>... terms) {
		this(getRepresentation(terms), normaliser);
	}

	protected static Map<Multiset<ExpandedExpression>, AbstractExpression> getRepresentation(
			Multiset<ExpandedExpression>... terms) {
		Map<Multiset<ExpandedExpression>, AbstractExpression> ret = new HashMap<Multiset<ExpandedExpression>, AbstractExpression>();
		for (Multiset<ExpandedExpression> t : terms) {
			if (ret.containsKey(t)) {
				ret.put(t, SumExpression.create(ret.get(t),
						new DoubleExpression(1.0)));
			} else {
				ret.put(t, new DoubleExpression(1.0));
			}
		}
		return ret;
	}
	
	public AbstractExpression toAbstractExpression() {
		AbstractExpression ret = new DoubleExpression(0.0);
		for (Entry<Multiset<ExpandedExpression>, AbstractExpression> e:representation.entrySet()) {
			AbstractExpression term = e.getValue();
			if (term instanceof ExpandedExpression) {
				term = ((ExpandedExpression)term).toAbstractExpression();
			}
			for (Multiset.Entry<ExpandedExpression> f:e.getKey().entrySet()) {
				List<AbstractExpression> power = new LinkedList<AbstractExpression>();
				AbstractExpression tmp = f.getElement().toAbstractExpression();
				for (int i = 0; i<f.getCount(); i++) {
					power.add(tmp);
				}
				power.add(term);
				term = ProductExpression.create(power);
			}
			ret = SumExpression.create(ret, term);
		}
		return ret;
		
	}

	public static Multiset<ExpandedExpression> getGreatestCommonFactor(
			Polynomial a, Polynomial b) {
		if (a.equals(getEmptyPolynomial(a.getNormaliser())))
			return getCommonFactor(b);
		if (b.equals(getEmptyPolynomial(b.getNormaliser())))
			return getCommonFactor(a);
		Multiset<ExpandedExpression> fA = a.getCommonFactor(a);
		Multiset<ExpandedExpression> fB = a.getCommonFactor(b);
		Multiset<ExpandedExpression> commonFactor = Multisets.intersection(fA,
				fB);
		return commonFactor;
	}

	public static Multiset<ExpandedExpression> getCommonFactor(Polynomial p) {
		Multiset<ExpandedExpression> factor = null;
		for (Multiset<ExpandedExpression> term : p.getRepresentation().keySet()) {
			if (factor == null) {
				factor = term;
			} else {
				factor = Multisets.intersection(factor, term);
			}
		}
		if (factor == null) {
			factor = getOneTerm(p.getNormaliser());
		}
		return factor;
	}

	public static Polynomial product(Polynomial a, Polynomial b) {
		if (a.equals(getEmptyPolynomial(a.getNormaliser())))
			return getEmptyPolynomial(a.getNormaliser());
		if (b.equals(getEmptyPolynomial(b.getNormaliser())))
			return getEmptyPolynomial(b.getNormaliser());
		Map<Multiset<ExpandedExpression>, AbstractExpression> ret = new HashMap<Multiset<ExpandedExpression>, AbstractExpression>();
		for (Map.Entry<Multiset<ExpandedExpression>, AbstractExpression> eA : a
				.getRepresentation().entrySet()) {
			for (Map.Entry<Multiset<ExpandedExpression>, AbstractExpression> eB : b
					.getRepresentation().entrySet()) {
				AbstractExpression coefficient = ProductExpression.create(eA
						.getValue(), eB.getValue());
				// Product of the two terms
				Multiset<ExpandedExpression> newTerm = HashMultiset
						.<ExpandedExpression> create(eA.getKey());
				newTerm.addAll(eB.getKey());
				if (ret.containsKey(newTerm)) {
					coefficient = SumExpression.create(coefficient, ret
							.get(newTerm));
				}
				ret.put(newTerm, coefficient);
			}
		}
		return new Polynomial(ret, a.getNormaliser());
	}

	public static Polynomial times(Polynomial p, AbstractExpression coefficient) {
		Map<Multiset<ExpandedExpression>, AbstractExpression> ret = new HashMap<Multiset<ExpandedExpression>, AbstractExpression>();
		for (Entry<Multiset<ExpandedExpression>, AbstractExpression> e : p
				.getRepresentation().entrySet()) {
			ret.put(e.getKey(), ProductExpression.create(e.getValue(),
					coefficient));
		}
		return new Polynomial(ret, p.getNormaliser());
	}

	// Divides a by b if there is no remainder, otherwise returns null
	// TODO Implement proper polynomial division
	public static Polynomial divide(Polynomial p,
			Multiset<ExpandedExpression> dividingTerm,
			AbstractExpression dividingCoefficient) {
		// First, moves all the numerical coefficients from the dividingTerm to
		// the dividingCoefficients
		CoefficientTerm tmp = p.separateNumericalValuesFromTerm(dividingTerm);
		dividingCoefficient = ProductExpression.create(tmp.coefficient,
				dividingCoefficient);
		dividingTerm = tmp.term;
		Map<Multiset<ExpandedExpression>, AbstractExpression> ret = new HashMap<Multiset<ExpandedExpression>, AbstractExpression>();
		for (Entry<Multiset<ExpandedExpression>, AbstractExpression> e : p
				.getRepresentation().entrySet()) {
			if (!Multisets.containsOccurrences(e.getKey(), dividingTerm)) {
				return null;
			} else {
				Multiset<ExpandedExpression> newTerm = HashMultiset
						.<ExpandedExpression> create(e.getKey());
				Multisets.removeOccurrences(newTerm, dividingTerm);
				ret.put(newTerm, DivExpression.create(e.getValue(),
						dividingCoefficient));
			}
		}
		return new Polynomial(ret, p.getNormaliser());
	}

	// Adds one polynomial to another
	public static Polynomial plus(Polynomial a, Polynomial b) {
		Map<Multiset<ExpandedExpression>, AbstractExpression> ret = new HashMap<Multiset<ExpandedExpression>, AbstractExpression>(
				a.getRepresentation());
		for (Entry<Multiset<ExpandedExpression>, AbstractExpression> e : b
				.getRepresentation().entrySet()) {
			if (ret.containsKey(e.getKey())) {
				ret.put(e.getKey(), SumExpression.create(e.getValue(), ret
						.get(e.getKey())));
			} else {
				ret.put(e.getKey(), e.getValue());
			}
		}
		return new Polynomial(ret, a.getNormaliser());
	}

	private static class CoefficientTerm {
		AbstractExpression coefficient;
		Multiset<ExpandedExpression> term;

		public CoefficientTerm(AbstractExpression coefficient,
				Multiset<ExpandedExpression> term) {
			super();
			this.coefficient = coefficient;
			this.term = term;
		}
	}

	protected  CoefficientTerm separateNumericalValuesFromTerm(
			Multiset<ExpandedExpression> term) {
		Multiset<ExpandedExpression> ret = HashMultiset
				.<ExpandedExpression> create();
		AbstractExpression numericalCoefficient = new DoubleExpression(1.0);
		for (Multiset.Entry<ExpandedExpression> f : term.entrySet()) {
			if (f.getElement().isNumber()) {
				numericalCoefficient = ProductExpression.create(
						numericalCoefficient, PowerExpression.create(f
								.getElement().numericalValue(),
								new DoubleExpression((double) f.getCount())));
			} else {
				ret.add(f.getElement(), f.getCount());
			}
		}
		return new CoefficientTerm(numericalCoefficient, ret);
	}

	// Makes sure that the terms cannot be simplified further
	protected Map<Multiset<ExpandedExpression>, AbstractExpression> normalise(
			Map<Multiset<ExpandedExpression>, AbstractExpression> terms) {
		Map<Multiset<ExpandedExpression>, AbstractExpression> ret = new HashMap<Multiset<ExpandedExpression>, AbstractExpression>();
		AbstractExpression numericalSummands = DoubleExpression.ZERO;
		for (Map.Entry<Multiset<ExpandedExpression>, AbstractExpression> e : terms
				.entrySet()) {
			CoefficientTerm tmp = separateNumericalValuesFromTerm(e.getKey());
			Multiset<ExpandedExpression> term = tmp.term;
			AbstractExpression value = e.getValue();

			AbstractExpression numericalCoefficient = ProductExpression.create(tmp.coefficient, value);
			numericalCoefficient = ContractingExpressionTransformer.contractExpression(numericalCoefficient);
			if (!term.isEmpty()) {
				if (!numericalCoefficient.equals(DoubleExpression.ZERO)) {
					ret.put(term, normaliseCoefficient(numericalCoefficient));
				}
			} else {
				numericalSummands = SumExpression.create(numericalSummands, normaliseCoefficient(numericalCoefficient));
			}
		}
		numericalSummands = ContractingExpressionTransformer.contractExpression(numericalSummands);
		if (!numericalSummands.equals(DoubleExpression.ZERO)) {
			ret.put(getOneTerm(normaliser), normaliseCoefficient(numericalSummands));
		}
		return ret;
	}
	
	private AbstractExpression normaliseCoefficient(AbstractExpression coefficient) {
		coefficient = ContractingExpressionTransformer.contractExpression(coefficient);
		return normaliser.normaliseCoefficient(coefficient);
	}

	public static Multiset<ExpandedExpression> getOneTerm(ICoefficientSpecification normaliser) {
		Multiset<ExpandedExpression> ret = HashMultiset
				.<ExpandedExpression> create();
		ret.add(new UnexpandableExpression(new DoubleExpression(1.0), normaliser));
		return ret;
	}

	public static Multiset<ExpandedExpression> getMinusOneTerm(ICoefficientSpecification normaliser) {
		Multiset<ExpandedExpression> ret = HashMultiset
				.<ExpandedExpression> create();
		ret.add(new UnexpandableExpression(new DoubleExpression(-1.0), normaliser));
		return ret;
	}

	public static Polynomial getUnitPolynomial(ICoefficientSpecification normaliser) {
		return new Polynomial(normaliser, getOneTerm(normaliser));
	}

	public static Polynomial getMinusUnitPolynomial(ICoefficientSpecification normaliser) {
		return new Polynomial(normaliser, getMinusOneTerm(normaliser));
	}

	public static Polynomial getEmptyPolynomial(ICoefficientSpecification normaliser) {
		Map<Multiset<ExpandedExpression>, AbstractExpression> ret = new HashMap<Multiset<ExpandedExpression>, AbstractExpression>();
		return new Polynomial(ret, normaliser);
	}

	public boolean isNumber() {
		return representation.size() == 1
				&& representation.keySet().iterator().next().size() == 1
				&& representation.keySet().iterator().next().iterator().next()
						.isNumber();
	}

	public AbstractExpression numericalValue() {
		if (isNumber()) {
			Entry<Multiset<ExpandedExpression>, AbstractExpression> entry = representation
					.entrySet().iterator().next();
			return ProductExpression.create(entry.getKey().iterator().next().numericalValue()
					, entry.getValue());
		} else {
			return null;
		}
	}

	public Map<Multiset<ExpandedExpression>, AbstractExpression> getRepresentation() {
		return representation;
	}

	@Override
	public String toString() {
		String ret = "";
		boolean first = true;
		for (Map.Entry<Multiset<ExpandedExpression>, AbstractExpression> e : representation
				.entrySet()) {
			if (first) {
				first = false;
			} else {
				ret += " + ";
			}
			ret += e.getValue() + "*" + e.getKey();
		}
		return ret;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((representation == null) ? 0 : representation.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Polynomial other = (Polynomial) obj;
		if (representation == null) {
			if (other.representation != null)
				return false;
		} else if (!representation.equals(other.representation))
			return false;
		return true;
	}

	public ICoefficientSpecification getNormaliser() {
		return normaliser;
	}
	
	
}
