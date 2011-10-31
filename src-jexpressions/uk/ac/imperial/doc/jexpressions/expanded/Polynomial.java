package uk.ac.imperial.doc.jexpressions.expanded;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
	
	public static Polynomial product(Polynomial a, Polynomial b){
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
	
	// Divides a by b if there is no remainder, otherwise returns null
	public static Polynomial divide(Polynomial p, Multiset<ExpandedExpression> dividingTerm, Double dividingCoefficient){
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
			}
		}
		return new Polynomial(ret);
	}

	// Makes sure that the terms cannot be simplified further
	protected static Map<Multiset<ExpandedExpression>, Double> normalise(Map<Multiset<ExpandedExpression>, Double>  terms){
		Map<Multiset<ExpandedExpression>, Double> ret = new HashMap<Multiset<ExpandedExpression>, Double>();
		double numericalSummands = 0.0;
		for (Map.Entry<Multiset<ExpandedExpression>, Double> e:terms.entrySet()){
			Multiset<ExpandedExpression> term = HashMultiset.<ExpandedExpression>create();
			double numericalCoefficient = 1.0;
			for (Multiset.Entry<ExpandedExpression> f:e.getKey().entrySet()){
				if (f.getElement().isNumber()){
					numericalCoefficient*=Math.pow(f.getElement().numericalValue(),f.getCount());
				} else {
					term.add(f.getElement(), f.getCount());
				}
			}
			if (!term.isEmpty()){ 
				if (numericalCoefficient!=0.0){
					ret.put(term, numericalCoefficient);
				}
			} else {
				numericalSummands+=numericalCoefficient;
			}
		}
		if (numericalSummands!=0.0){
			ret.put(getOne(), numericalSummands);
		}
		return ret;
	}
	
	
	protected static Multiset<ExpandedExpression> getOne(){
		Multiset<ExpandedExpression> ret = HashMultiset.<ExpandedExpression>create();
		ret.add(new UnexpandableExpression(new DoubleExpression(1.0)));
		return ret;
	}

	public Map<Multiset<ExpandedExpression>, Double> getRepresentation() {
		return representation;
	}
}
