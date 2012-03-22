package uk.ac.imperial.doc.pctmc.expressions;

import java.util.HashMap;
import java.util.Map;

import uk.ac.imperial.doc.pctmc.representation.State;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class PopulationProduct {
	private Map<State, Integer> representation;
	private int order = -1;

	public PopulationProduct(Map<State, Integer> moment) {
		super();
		this.representation = new HashMap<State, Integer>();
		for (Map.Entry<State, Integer> e : moment.entrySet()) {
			if (e.getValue() != 0) {
				this.representation.put(e.getKey(), e.getValue());
			}
		}
	}

	public PopulationProduct(Multiset<State> mset) {
		representation = new HashMap<State, Integer>();
		for (Multiset.Entry<State> e : mset.entrySet()) {
			representation.put(e.getElement(), e.getCount());
		}
	}

	public static PopulationProduct getProduct(PopulationProduct a,
			PopulationProduct b) {
		if (a == null)
			return b;
		if (b == null)
			return a;
		Multiset<State> tmp = HashMultiset.<State> create();
		for (Map.Entry<State, Integer> exp : a.getRepresentation().entrySet()) {
			tmp.add(exp.getKey(), exp.getValue());
		}
		for (Map.Entry<State, Integer> exp : b.getRepresentation().entrySet()) {
			tmp.add(exp.getKey(), exp.getValue());
		}
		Map<State, Integer> map = new HashMap<State, Integer>();
		for (State s : tmp.elementSet()) {
			map.put(s, tmp.count(s));
		}
		return new PopulationProduct(map);
	}

	public int getPowerOf(State state) {
		if (representation.containsKey(state)) {
			return representation.get(state);
		} else {
			return 0;
		}
	}

	public PopulationProduct getV(State state) {
		Integer kb = representation.get(state);
		if (kb == null)
			kb = 0;
		Map<State, Integer> tmp = new HashMap<State, Integer>(representation);
		tmp.put(state, kb + 1);
		return new PopulationProduct(tmp);
	}

	public static PopulationProduct getMeanProduct(State s) {
		Map<State, Integer> product = new HashMap<State, Integer>();
		product.put(s, 1);
		return new PopulationProduct(product);
	}

	public int getOrder() {
		if (order != -1)
			return order;
		order = 0;
		for (Integer v : representation.values()) {
			order += v;
		}
		return order;
	}

	public Multiset<State> asMultiset() {
		Multiset<State> tmp = HashMultiset.<State> create();
		for (Map.Entry<State, Integer> e : this.getRepresentation().entrySet()) {
			tmp.add(e.getKey(), e.getValue());
		}
		return tmp;
	}

	public PopulationProduct toThePower(int p) {
		Map<State, Integer> newProduct = new HashMap<State, Integer>();
		for (Map.Entry<State, Integer> e : representation.entrySet()) {
			newProduct.put(e.getKey(), e.getValue() * p);
		}
		return new PopulationProduct(newProduct);
	}

	@Override
	public String toString() {
		String ret = "";
		boolean first = true;
		for (Map.Entry<State, Integer> e : representation.entrySet()) {
			if (e.getValue() > 0) {
				if (first)
					first = false;
				else
					ret += " ";
				ret += e.getKey();
				if (e.getValue() > 1)
					ret += "^" + e.getValue();
			}
		}
		return ret;
	}

	@Override
	public int hashCode() {
		return representation.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof PopulationProduct))
			return false;
		PopulationProduct asProduct = (PopulationProduct) obj;
		return representation.equals(asProduct.getRepresentation());
	}

	public Map<State, Integer> getRepresentation() {
		return representation;
	}
}
