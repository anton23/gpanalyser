package uk.ac.imperial.doc.pctmc.expressions;

import uk.ac.imperial.doc.pctmc.representation.State;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;

public class PopulationProduct {
	private Multiset<State> representation;

	public PopulationProduct(Multiset<State> mset) {
		representation = HashMultiset.create();
		for (Multiset.Entry<State> e : mset.entrySet()) {
			representation.add(e.getElement(), e.getCount());
		}
	}

	public static PopulationProduct getProduct(PopulationProduct a,
			PopulationProduct b) {
		if (a == null)
			return b;
		if (b == null)
			return a;
		Multiset<State> tmp = HashMultiset.<State> create();
		tmp.addAll(a.getRepresentation());
		tmp.addAll(b.getRepresentation());
		return new PopulationProduct(tmp);
	}

	public int getPowerOf(State state) {
		return representation.count(state);
	}

	public PopulationProduct getV(State state) {
		Multiset<State> tmp = HashMultiset.create(representation);
		tmp.add(state);
		return new PopulationProduct(tmp);
	}

	public static PopulationProduct getMeanProduct(State s) {
		Multiset<State> product = HashMultiset.create();
		product.add(s);
		return new PopulationProduct(product);
	}

	public int getOrder() {
		return representation.size();
	}

	public Multiset<State> asMultiset() {
		return HashMultiset.create(representation);
	}

	public PopulationProduct toThePower(int p) {
		Multiset<State> newProduct = HashMultiset.create();
		for (Entry<State> e : representation.entrySet()) {
			newProduct.add(e.getElement(), e.getCount() * p);
		}
		return new PopulationProduct(newProduct);
	}

	@Override
	public String toString() {
		String ret = "";
		boolean first = true;
		for (Entry<State> e : representation.entrySet()) {
			if (e.getCount() > 0) {
				if (first)
					first = false;
				else
					ret += " ";
				ret += e.getElement();
				if (e.getCount() > 1)
					ret += "^" + e.getCount();
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

	public Multiset<State> getRepresentation() {
		return representation;
	}
}
