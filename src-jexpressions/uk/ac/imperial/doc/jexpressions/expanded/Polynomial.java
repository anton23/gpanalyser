package uk.ac.imperial.doc.jexpressions.expanded;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

public class Polynomial {
	
	private final Map<Multiset<ExpandedExpression>, Double> representation;

	public Polynomial(Map<Multiset<ExpandedExpression>, Double> representation) {
		this.representation = normalise(representation);
	}
	
	public Polynomial(Multiset<ExpandedExpression> ...terms){
		this(getRepresentation(terms));
	}
	
	protected static Map<Multiset<ExpandedExpression>, Double> getRepresentation(Multiset<ExpandedExpression> ...terms){
		Map<Multiset<ExpandedExpression>, Double> ret = new HashMap<Multiset<ExpandedExpression>, Double>();
		for (Multiset<ExpandedExpression> t:terms){
			if (ret.containsKey(t)){
				ret.put(t, ret.get(t)+1.0);
			} else {
				ret.put(t, 1.0);
			}
		}
		return ret;
	}
	
	public static Multiset<ExpandedExpression> getCommonFactor(Polynomial p){
		Multiset<ExpandedExpression> factor = null;
		for (Multiset<ExpandedExpression> term:p.getRepresentation().keySet()){
			if (factor == null){
				factor = term;
			} else {
				factor = Multisets.intersection(factor, term);
			}
		}
		if (factor == null){
			factor = getOneTerm();
		}
		return factor;
	}
	
	public static Polynomial product(Polynomial a, Polynomial b){
		if (a.equals(getEmptyPolynomial())) return getEmptyPolynomial();
		if (b.equals(getEmptyPolynomial())) return getEmptyPolynomial();
		Map<Multiset<ExpandedExpression>, Double> ret = new HashMap<Multiset<ExpandedExpression>, Double>();
		for (Map.Entry<Multiset<ExpandedExpression>, Double> eA:a.getRepresentation().entrySet()){
			for (Map.Entry<Multiset<ExpandedExpression>, Double> eB:b.getRepresentation().entrySet()){
				double coefficient = eA.getValue()*eB.getValue();
				// Product of the two terms
				Multiset<ExpandedExpression> newTerm = HashMultiset.<ExpandedExpression>create(eA.getKey());
				newTerm.addAll(eB.getKey());
				if (ret.containsKey(newTerm)){
					coefficient += ret.get(newTerm);
				}
				ret.put(newTerm, coefficient);
			}
		}
		return new Polynomial(ret);
	}
	
	public static Polynomial times(Polynomial p, Double coefficient){
		Map<Multiset<ExpandedExpression>, Double> ret = new HashMap<Multiset<ExpandedExpression>, Double>();
		for (Entry<Multiset<ExpandedExpression>, Double> e:p.getRepresentation().entrySet()){
			ret.put(e.getKey(), e.getValue()*coefficient);
		}
		return new Polynomial(ret);
	}
	
	// Divides a by b if there is no remainder, otherwise returns null
	// TODO Implement proper polynomial division
	public static Polynomial divide(Polynomial p, Multiset<ExpandedExpression> dividingTerm, Double dividingCoefficient){
		// First, moves all the numerical coefficients from the dividingTerm to the dividingCoefficients
		CoefficientTerm tmp = separateNumericalValuesFromTerm(dividingTerm);
		dividingCoefficient *= tmp.coefficient;
		dividingTerm = tmp.term;
		Map<Multiset<ExpandedExpression>, Double> ret = new HashMap<Multiset<ExpandedExpression>, Double>();
		for (Entry<Multiset<ExpandedExpression>, Double> e:p.getRepresentation().entrySet()){
			if (!Multisets.containsOccurrences(e.getKey(), dividingTerm)){
				return null;
			} else {
				Multiset<ExpandedExpression> newTerm = HashMultiset.<ExpandedExpression>create(e.getKey());
				Multisets.removeOccurrences(newTerm, dividingTerm);
				ret.put(newTerm, e.getValue()/dividingCoefficient);
			}
		}		
		return new Polynomial(ret);
	}
     
	// Adds one polynomial to another
	public static Polynomial plus(Polynomial a, Polynomial b){
		Map<Multiset<ExpandedExpression>, Double> ret = new HashMap<Multiset<ExpandedExpression>, Double>(a.getRepresentation());
		for (Entry<Multiset<ExpandedExpression>, Double> e:b.getRepresentation().entrySet()){
			if (ret.containsKey(e.getKey())){
				ret.put(e.getKey(), e.getValue()+ret.get(e.getKey()));
			} else {
				ret.put(e.getKey(), e.getValue());
			}
		}
		return new Polynomial(ret);
	}
	
	private static class CoefficientTerm{
		Double coefficient;
		Multiset<ExpandedExpression> term;
		public CoefficientTerm(Double coefficient,
				Multiset<ExpandedExpression> term) {
			super();
			this.coefficient = coefficient;
			this.term = term;
		}
	}
	
	protected static CoefficientTerm separateNumericalValuesFromTerm(Multiset<ExpandedExpression> term){
		Multiset<ExpandedExpression> ret = HashMultiset.<ExpandedExpression>create();
		double numericalCoefficient = 1.0;
		for (Multiset.Entry<ExpandedExpression> f:term.entrySet()){
			if (f.getElement().isNumber()){
				numericalCoefficient*=Math.pow(f.getElement().numericalValue(),f.getCount());
			} else {
				ret.add(f.getElement(), f.getCount());
			}
		}
		return new CoefficientTerm(numericalCoefficient, ret); 
	}

	// Makes sure that the terms cannot be simplified further
	protected static Map<Multiset<ExpandedExpression>, Double> normalise(Map<Multiset<ExpandedExpression>, Double>  terms){
		Map<Multiset<ExpandedExpression>, Double> ret = new HashMap<Multiset<ExpandedExpression>, Double>();
		double numericalSummands = 0.0;
		for (Map.Entry<Multiset<ExpandedExpression>, Double> e:terms.entrySet()){
			CoefficientTerm tmp = separateNumericalValuesFromTerm(e.getKey());
			Multiset<ExpandedExpression> term = tmp.term;
			double numericalCoefficient = tmp.coefficient*e.getValue();
			if (!term.isEmpty()){ 
				if (numericalCoefficient!=0.0){
					ret.put(term, numericalCoefficient);
				}
			} else {
				numericalSummands+=numericalCoefficient;
			}
		}
		if (numericalSummands!=0.0){
			ret.put(getOneTerm(), numericalSummands);
		}
		return ret;
	}
	
	
	public static Multiset<ExpandedExpression> getOneTerm(){
		Multiset<ExpandedExpression> ret = HashMultiset.<ExpandedExpression>create();
		ret.add(new UnexpandableExpression(new DoubleExpression(1.0)));
		return ret;
	}
	
	public static Multiset<ExpandedExpression> getMinusOneTerm(){
		Multiset<ExpandedExpression> ret = HashMultiset.<ExpandedExpression>create();
		ret.add(new UnexpandableExpression(new DoubleExpression(-1.0)));
		return ret;
	}
	
	public static Polynomial getUnitPolynomial(){
		return new Polynomial(getOneTerm());		
	}
	
	public static Polynomial getMinusUnitPolynomial(){
		return new Polynomial(getMinusOneTerm());		
	}
	
	public static Polynomial getEmptyPolynomial(){
		Map<Multiset<ExpandedExpression>, Double> ret = new HashMap<Multiset<ExpandedExpression>, Double>();		
		return new Polynomial(ret);
	}
	
	public boolean isNumber(){
		return 
		    representation.size()==1 && 
			representation.keySet().iterator().next().size()==1 &&
			representation.keySet().iterator().next().iterator().next().isNumber();
	}
	
	public Double numericalValue(){
		if (isNumber()){
			Entry<Multiset<ExpandedExpression>, Double> entry = representation.entrySet().iterator().next();
			return entry.getKey().iterator().next().numericalValue()*entry.getValue();
		} else {
			return null;
		}
	}

	public Map<Multiset<ExpandedExpression>, Double> getRepresentation() {
		return representation;
	}
	
	@Override
	public String toString() {
		String ret = "";
		boolean first = true;
		for (Map.Entry<Multiset<ExpandedExpression>, Double> e:representation.entrySet()){
			if (first){
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
}
