package uk.ac.imperial.doc.jexpressions.expanded;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

@SuppressWarnings("unchecked")
public class Polynomial {

	private final ICoefficientSpecification coefficientSpecification;

	private final Map<Multiset<UnexpandableExpression>, AbstractExpression> representation;

	public Polynomial(
			Map<Multiset<UnexpandableExpression>, AbstractExpression> representation,
			ICoefficientSpecification coefficientSpecification) {
		this.coefficientSpecification = coefficientSpecification;
		this.representation = normalise(representation);
	}

	public Polynomial(ICoefficientSpecification coefficientSpecification,
			Multiset<UnexpandableExpression>... terms) {
		this(getRepresentation(terms), coefficientSpecification);
	}

	protected static Map<Multiset<UnexpandableExpression>, AbstractExpression> getRepresentation(
			Multiset<UnexpandableExpression>... terms) {
		Map<Multiset<UnexpandableExpression>, AbstractExpression> ret = new HashMap<Multiset<UnexpandableExpression>, AbstractExpression>();
		for (Multiset<UnexpandableExpression> t : terms) {
			insertIntoMap(t, new DoubleExpression(1.0), ret);
		}
		return ret;
	}

	private AbstractExpression abstractExpression = null;

	public AbstractExpression toAbstractExpression() {
		if (abstractExpression != null)
			return abstractExpression;
		abstractExpression = new DoubleExpression(0.0);
		for (Entry<Multiset<UnexpandableExpression>, AbstractExpression> e : representation
				.entrySet()) {
			AbstractExpression term = e.getValue();
			if (term instanceof ExpandedExpression) {
				term = ((ExpandedExpression) term).toAbstractExpression();
			}
			for (Multiset.Entry<UnexpandableExpression> f : e.getKey()
					.entrySet()) {
				List<AbstractExpression> power = new LinkedList<AbstractExpression>();
				AbstractExpression tmp = f.getElement().toAbstractExpression();
				for (int i = 0; i < f.getCount(); i++) {
					power.add(tmp);
				}
				power.add(term);
				term = ProductExpression.create(power);
			}
			abstractExpression = SumExpression.create(abstractExpression, term);
		}
		return abstractExpression;
	}

	public int getHighestOrder() {
		int highestOrder = 0;
		for (Entry<Multiset<UnexpandableExpression>, AbstractExpression> e : this
				.getRepresentation().entrySet()) {
			if (e.getKey().size() >= highestOrder) {
				highestOrder = e.getKey().size();
			}
		}
		return highestOrder;
	}

	public List<CoefficientTerm> getHighestTerms() {
		if (this.equals(Polynomial.getEmptyPolynomial(this
				.getCoefficientSpecification()))) {
			return new LinkedList<CoefficientTerm>();
		}
		int highestOrder = getHighestOrder();
		List<CoefficientTerm> ret = new LinkedList<CoefficientTerm>();
		for (Entry<Multiset<UnexpandableExpression>, AbstractExpression> e : this
				.getRepresentation().entrySet()) {
			if (e.getKey().size() == highestOrder) {
				ret.add(new CoefficientTerm(e.getValue(), e.getKey()));
			}
		}
		return ret;
	}

	private static void insertIntoMap(Multiset<UnexpandableExpression> term,
			AbstractExpression coefficient,
			Map<Multiset<UnexpandableExpression>, AbstractExpression> map) {
		if (map.containsKey(term)) {
			map.put(term, SumExpression.create(map.get(term), coefficient));
		} else {
			map.put(term, coefficient);
		}
	}

	public static DivisionResult divide(Polynomial a, Polynomial b) {
		return divide(a, b, new HashSet<Polynomial>());
	}

	public static DivisionResult divide(Polynomial a, Polynomial b,
			Set<Polynomial> seenA) {
		// Divide a by the highest term
		if (a.equals(Polynomial.getEmptyPolynomial(a
				.getCoefficientSpecification()))) {
			return new DivisionResult(Polynomial.getEmptyPolynomial(a
					.getCoefficientSpecification()), Polynomial
					.getEmptyPolynomial(a.getCoefficientSpecification()));
		}
		if (b.equals(Polynomial.getEmptyPolynomial(b
				.getCoefficientSpecification()))) {
			throw new AssertionError("Division by zero!");
		}
		if (b.isNumber()) {
			return new DivisionResult(scalarProduct(a, DivExpression.create(
					DoubleExpression.ONE, b.numericalValue())), Polynomial
					.getEmptyPolynomial(a.getCoefficientSpecification()));
		}
		seenA = new HashSet<Polynomial>(seenA);
		seenA.add(a);
		List<CoefficientTerm> highestBterms = b.getHighestTerms();
		List<CoefficientTerm> highestAterms = a.getHighestTerms();
		// Finds any highest A term that is divisible by a highest B term
		for (CoefficientTerm hA : highestAterms) {
			for (CoefficientTerm hB : highestBterms) {
				if (Multisets.containsOccurrences(hA.term, hB.term)) {
					Multiset<UnexpandableExpression> factorTerm = HashMultiset
							.<UnexpandableExpression> create(hA.term);
					Multisets.removeOccurrences(factorTerm, hB.term);
					Polynomial factorPolynomial = new Polynomial(b
							.getCoefficientSpecification(), factorTerm);
					factorPolynomial = Polynomial.scalarProduct(
							factorPolynomial, DivExpression.create(
									hA.coefficient, hB.coefficient));
					Polynomial newA = Polynomial.minus(a, Polynomial.product(b,
							factorPolynomial));
					// To avoid cycling, don't repeat the same division
					if (!seenA.contains(newA)) {
						DivisionResult tmp = divide(newA, b, seenA);
						return new DivisionResult(Polynomial.plus(
								factorPolynomial, tmp.getResult()), tmp
								.getRemainder());
					}
				}
			}
		}
		// If none were found than a is definitely not divisible by b
		return new DivisionResult(Polynomial.getEmptyPolynomial(a
				.getCoefficientSpecification()), a);
	}

	public static Multiset<UnexpandableExpression> getGreatestCommonFactor(
			Polynomial a, Polynomial b) {
		if (a.equals(getEmptyPolynomial(a.getCoefficientSpecification())))
			return b.getCommonFactor();
		if (b.equals(getEmptyPolynomial(b.getCoefficientSpecification())))
			return a.getCommonFactor();
		Multiset<UnexpandableExpression> fA = a.getCommonFactor();
		Multiset<UnexpandableExpression> fB = b.getCommonFactor();
		Multiset<UnexpandableExpression> commonFactor = Multisets.intersection(
				fA, fB);
		return commonFactor;
	}

	public Multiset<UnexpandableExpression> getCommonFactor() {
		Multiset<UnexpandableExpression> factor = null;
		for (Multiset<UnexpandableExpression> term : this.getRepresentation()
				.keySet()) {
			if (factor == null) {
				factor = term;
			} else {
				factor = Multisets.intersection(factor, term);
			}
		}
		if (factor == null) {
			factor = getOneTerm(this.getCoefficientSpecification());
		}
		return factor;
	}

	public static Polynomial greatestCommonDivisor(Polynomial a, Polynomial b) {
		return greatestCommonDivisor(a, b, new HashSet<Polynomial>());
	}

	private static Polynomial greatestCommonDivisor(Polynomial a, Polynomial b,
			Set<Polynomial> seenRemainders) {
		if (a.getHighestOrder() < b.getHighestOrder()) {
			return greatestCommonDivisor(b, a, seenRemainders);
		}
		if (b.equals(Polynomial.getEmptyPolynomial(b
				.getCoefficientSpecification()))) {
			return a;
		}
		
		Multiset<UnexpandableExpression> commonFactor = getGreatestCommonFactor(a, b);
		a = divide(a, commonFactor);
		b = divide(b, commonFactor);
		
		
		DivisionResult tmp = Polynomial.divide(a, b);
		Polynomial newDivisor = tmp.getRemainder();
		Multiset<UnexpandableExpression> newDivisorFactor = newDivisor
				.getCommonFactor();
		newDivisor = Polynomial.divide(newDivisor, newDivisorFactor);
		if (!newDivisor.equals(Polynomial.getEmptyPolynomial(a
				.getCoefficientSpecification()))) {
			newDivisor = Polynomial.scalarProduct(newDivisor, DivExpression
					.create(DoubleExpression.ONE, newDivisor.getHighestTerms()
							.iterator().next().coefficient));
		}
		if (seenRemainders.contains(newDivisor)) {
			if (!commonFactor.isEmpty()) {
				return new Polynomial(a.getCoefficientSpecification(), commonFactor);
			} else {
				return Polynomial
						.getUnitPolynomial(a.getCoefficientSpecification());
			}
		}
		Set<Polynomial> newSeenRemainders = new HashSet<Polynomial>(
				seenRemainders);
		newSeenRemainders.add(newDivisor);
		Polynomial greatestCommonDivisor = greatestCommonDivisor(b, newDivisor,
				newSeenRemainders);
		if (Polynomial.divide(b, newDivisorFactor) != null) {
			greatestCommonDivisor = Polynomial.product(greatestCommonDivisor,
					new Polynomial(a.getCoefficientSpecification(),
							newDivisorFactor));
		}
		if (!commonFactor.isEmpty()) {
			greatestCommonDivisor = Polynomial.product(greatestCommonDivisor, new Polynomial(a.getCoefficientSpecification(), commonFactor));
		}
		List<CoefficientTerm> highestTermsOfGCD = greatestCommonDivisor
				.getHighestTerms();
		greatestCommonDivisor = Polynomial.scalarProduct(greatestCommonDivisor,
				DivExpression.create(DoubleExpression.ONE, highestTermsOfGCD
						.iterator().next().coefficient));
		return greatestCommonDivisor;
	}

	public static Polynomial product(Polynomial a, Polynomial b) {
		if (a.equals(getEmptyPolynomial(a.getCoefficientSpecification())))
			return getEmptyPolynomial(a.getCoefficientSpecification());
		if (b.equals(getEmptyPolynomial(b.getCoefficientSpecification())))
			return getEmptyPolynomial(b.getCoefficientSpecification());
		Map<Multiset<UnexpandableExpression>, AbstractExpression> ret = new HashMap<Multiset<UnexpandableExpression>, AbstractExpression>();
		for (Map.Entry<Multiset<UnexpandableExpression>, AbstractExpression> eA : a
				.getRepresentation().entrySet()) {
			for (Map.Entry<Multiset<UnexpandableExpression>, AbstractExpression> eB : b
					.getRepresentation().entrySet()) {
				AbstractExpression coefficient = ProductExpression.create(eA
						.getValue(), eB.getValue());
				// Product of the two terms
				Multiset<UnexpandableExpression> newTerm = HashMultiset
						.<UnexpandableExpression> create(eA.getKey());
				newTerm.addAll(eB.getKey());
				insertIntoMap(newTerm, coefficient, ret);
			}
		}
		return new Polynomial(ret, a.getCoefficientSpecification());
	}

	public static Polynomial scalarProduct(Polynomial p,
			AbstractExpression scalar) {
		if (p.getCoefficientSpecification().isCoefficient(scalar)) {
			Map<Multiset<UnexpandableExpression>, AbstractExpression> ret = new HashMap<Multiset<UnexpandableExpression>, AbstractExpression>();
			for (Map.Entry<Multiset<UnexpandableExpression>, AbstractExpression> e : p
					.getRepresentation().entrySet()) {
				ret.put(e.getKey(), ProductExpression.create(e.getValue(),
						scalar));
			}
			return new Polynomial(ret, p.getCoefficientSpecification());
		} else {
			throw new AssertionError("The expression " + scalar
					+ " is not a scalar for the polynomial " + p);
		}
	}

	// Divides a by b if there is no remainder, otherwise returns null
	// TODO Implement proper polynomial division
	public static Polynomial divide(Polynomial p,
			Multiset<UnexpandableExpression> dividingTerm) {
		// First, moves all the numerical coefficients from the dividingTerm to
		// the dividingCoefficients
		CoefficientTerm tmp = p.separateNumericalValuesFromTerm(dividingTerm);
		AbstractExpression dividingCoefficient = tmp.coefficient;
		dividingTerm = tmp.term;
		Map<Multiset<UnexpandableExpression>, AbstractExpression> ret = new HashMap<Multiset<UnexpandableExpression>, AbstractExpression>();
		for (Entry<Multiset<UnexpandableExpression>, AbstractExpression> e : p
				.getRepresentation().entrySet()) {
			if (!Multisets.containsOccurrences(e.getKey(), dividingTerm)) {
				return null;
			} else {
				Multiset<UnexpandableExpression> newTerm = HashMultiset
						.<UnexpandableExpression> create(e.getKey());
				Multisets.removeOccurrences(newTerm, dividingTerm);
				ret.put(newTerm, DivExpression.create(e.getValue(),
						dividingCoefficient));
			}
		}
		return new Polynomial(ret, p.getCoefficientSpecification());
	}

	public static Polynomial minus(Polynomial a, Polynomial b) {
		return Polynomial.plus(a, Polynomial.product(Polynomial
				.getMinusUnitPolynomial(b.getCoefficientSpecification()), b));
	}

	// Adds one polynomial to another
	public static Polynomial plus(Polynomial a, Polynomial b) {
		Map<Multiset<UnexpandableExpression>, AbstractExpression> ret = new HashMap<Multiset<UnexpandableExpression>, AbstractExpression>(
				a.getRepresentation());
		for (Entry<Multiset<UnexpandableExpression>, AbstractExpression> e : b
				.getRepresentation().entrySet()) {
			insertIntoMap(e.getKey(), e.getValue(), ret);
		}
		return new Polynomial(ret, a.getCoefficientSpecification());
	}

	private static class CoefficientTerm {
		AbstractExpression coefficient;
		Multiset<UnexpandableExpression> term;

		public CoefficientTerm(AbstractExpression coefficient,
				Multiset<UnexpandableExpression> term) {
			super();
			this.coefficient = coefficient;
			this.term = term;
		}

		@Override
		public String toString() {
			return coefficient.toString() + "*" + term.toString();
		}
	}

	protected CoefficientTerm separateNumericalValuesFromTerm(
			Multiset<UnexpandableExpression> term) {
		Multiset<UnexpandableExpression> ret = HashMultiset
				.<UnexpandableExpression> create();
		AbstractExpression numericalCoefficient = new DoubleExpression(1.0);
		for (Multiset.Entry<UnexpandableExpression> f : term.entrySet()) {
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
	protected Map<Multiset<UnexpandableExpression>, AbstractExpression> normalise(
			Map<Multiset<UnexpandableExpression>, AbstractExpression> terms) {
		Map<Multiset<UnexpandableExpression>, AbstractExpression> ret = new HashMap<Multiset<UnexpandableExpression>, AbstractExpression>();
		AbstractExpression numericalSummands = DoubleExpression.ZERO;
		for (Map.Entry<Multiset<UnexpandableExpression>, AbstractExpression> e : terms
				.entrySet()) {
			CoefficientTerm tmp = separateNumericalValuesFromTerm(e.getKey());
			Multiset<UnexpandableExpression> term = tmp.term;
			AbstractExpression value = e.getValue();

			AbstractExpression numericalCoefficient = ProductExpression.create(
					tmp.coefficient, value);
			numericalCoefficient = ContractingExpressionTransformer
					.contractExpression(numericalCoefficient);
			if (!term.isEmpty()) {
				if (!coefficientSpecification.isZero(numericalCoefficient)) {
					ret.put(term, normaliseCoefficient(numericalCoefficient));
				}
			} else {
				numericalSummands = SumExpression.create(numericalSummands,
						normaliseCoefficient(numericalCoefficient));
			}
		}
		numericalSummands = ContractingExpressionTransformer
				.contractExpression(numericalSummands);
		if (!coefficientSpecification.isZero(numericalSummands)) {
			ret.put(getOneTerm(coefficientSpecification),
					normaliseCoefficient(numericalSummands));
		}
		return ret;
	}

	private AbstractExpression normaliseCoefficient(
			AbstractExpression coefficient) {
		coefficient = ContractingExpressionTransformer
				.contractExpression(coefficient);
		return coefficientSpecification.normaliseCoefficient(coefficient);
	}

	public static Multiset<UnexpandableExpression> getOneTerm(
			ICoefficientSpecification coefficientSpecification) {
		Multiset<UnexpandableExpression> ret = HashMultiset
				.<UnexpandableExpression> create();
		ret.add(new UnexpandableExpression(new DoubleExpression(1.0),
				coefficientSpecification));
		return ret;
	}

	public static Multiset<UnexpandableExpression> getMinusOneTerm(
			ICoefficientSpecification coefficientSpecification) {
		Multiset<UnexpandableExpression> ret = HashMultiset
				.<UnexpandableExpression> create();
		ret.add(new UnexpandableExpression(new DoubleExpression(-1.0),
				coefficientSpecification));
		return ret;
	}

	public static Polynomial getUnitPolynomial(
			ICoefficientSpecification coefficientSpecification) {
		return new Polynomial(coefficientSpecification,
				getOneTerm(coefficientSpecification));
	}

	public static Polynomial getMinusUnitPolynomial(
			ICoefficientSpecification coefficientSpecification) {
		return new Polynomial(coefficientSpecification,
				getMinusOneTerm(coefficientSpecification));
	}

	public static Polynomial getEmptyPolynomial(
			ICoefficientSpecification coefficientSpecification) {
		Map<Multiset<UnexpandableExpression>, AbstractExpression> ret = new HashMap<Multiset<UnexpandableExpression>, AbstractExpression>();
		return new Polynomial(ret, coefficientSpecification);
	}

	public boolean isNumber() {
		return representation.size() == 0 || 
		        (representation.size() == 1
				&& representation.keySet().iterator().next().size() == 1
				&& representation.keySet().iterator().next().iterator().next()
						.isNumber());
	}

	public AbstractExpression numericalValue() {
		if (isNumber()) {
			if (representation.size()==0) {
				return DoubleExpression.ZERO;
			}
			Entry<Multiset<UnexpandableExpression>, AbstractExpression> entry = representation
					.entrySet().iterator().next();
			return ProductExpression.create(entry.getKey().iterator().next()
					.numericalValue(), entry.getValue());
		} else {
			return null;
		}
	}

	public Map<Multiset<UnexpandableExpression>, AbstractExpression> getRepresentation() {
		return representation;
	}

	@Override
	public String toString() {
		String ret = "";
		boolean first = true;
		for (Map.Entry<Multiset<UnexpandableExpression>, AbstractExpression> e : representation
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

	public ICoefficientSpecification getCoefficientSpecification() {
		return coefficientSpecification;
	}
}