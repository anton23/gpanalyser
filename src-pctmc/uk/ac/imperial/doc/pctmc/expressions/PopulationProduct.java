package uk.ac.imperial.doc.pctmc.expressions;

import java.util.HashMap;
import java.util.Map;

import uk.ac.imperial.doc.pctmc.representation.State;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class PopulationProduct{
	
	public int getPowerOf(State state){
		if (product.containsKey(state)){
			return product.get(state);
		} else {
			return 0; 
		}
	}
	
	public PopulationProduct getV(State state){
		Integer kb = product.get(state);
		if (kb == null)
			kb = 0;
		Map<State, Integer> tmp = new HashMap<State, Integer>(product);
		tmp.put(state, kb + 1);
		return new PopulationProduct(tmp);
	}
	
	private Map<State,Integer> product; 
	
	@Override
	public int hashCode() {
		return product.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj==null) return false;
		if (!(obj instanceof PopulationProduct)) return false;
		PopulationProduct asProduct = (PopulationProduct) obj; 
		return product.equals(asProduct.getProduct());
	}

	public PopulationProduct(Map<State, Integer> moment) {
		super();
		this.product = new HashMap<State, Integer>(); 
		for (Map.Entry<State, Integer> e:moment.entrySet()){
			if (e.getValue()!=0){
				this.product.put(e.getKey(), e.getValue());
			}
		}	
	}
	
	public PopulationProduct(Multiset<State> mset){
		product = new HashMap<State, Integer>(); 
		for (Multiset.Entry<State> e:mset.entrySet()){
			product.put(e.getElement(), e.getCount());
		}
	}
	
	public Map<State, Integer> getProduct() {
		return product;
	}

	
	public static PopulationProduct getMeanProduct(State s){
		Map<State,Integer> product = new HashMap<State, Integer>();
		product.put(s, 1);
		return new PopulationProduct(product);
	}
		
	private int order = -1; 
	public int getOrder(){
		if (order!=-1) return order;
		order = 0; 
		for (Integer v:product.values()){
			order+=v; 
		}
		return order; 
	}
	
	public String toString() {
		String ret = "";
		boolean first = true;
		for (Map.Entry<State, Integer> e : product.entrySet()) {
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
	
	public static PopulationProduct getProduct(PopulationProduct a, PopulationProduct b){
		if (a==null) return b; 
		if (b==null) return a; 
		Multiset<State> tmp = HashMultiset.<State>create(); 
		for (Map.Entry<State, Integer> exp:a.getProduct().entrySet()){
			tmp.add(exp.getKey(),exp.getValue());
		}
		for (Map.Entry<State, Integer> exp:b.getProduct().entrySet()){
			tmp.add(exp.getKey(),exp.getValue());
		}
		Map<State,Integer> map = new HashMap<State, Integer>();
		for (State s:tmp.elementSet()){
			map.put(s,tmp.count(s));
		}
		return new PopulationProduct(map); 
	}
	
	public Multiset<State> asMultiset(){
		Multiset<State> tmp = HashMultiset.<State>create(); 
		for (Map.Entry<State, Integer> e:this.getProduct().entrySet()){
			tmp.add(e.getKey(),e.getValue());
		}
		return tmp;
	}
	
	public PopulationProduct toThePower(int p){
		Map<State,Integer> newProduct = new HashMap<State, Integer>();
		for (Map.Entry<State, Integer> e:product.entrySet()){
			newProduct.put(e.getKey(),e.getValue()*p);
		}
		return new PopulationProduct(newProduct); 
	}
	

}
