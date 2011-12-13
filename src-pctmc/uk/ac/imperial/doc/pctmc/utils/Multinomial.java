package uk.ac.imperial.doc.pctmc.utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class Multinomial {
	
	protected static class GetPartitionsArgument {
		Integer order;
		Integer components;

		public GetPartitionsArgument(int order, int components) {
			this.order = order;
			this.components = components;
		}

		@Override
		public int hashCode() {
			return (components + 1) * 23 + (order + 1);
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof GetPartitionsArgument))
				return false;
			GetPartitionsArgument asGMP = (GetPartitionsArgument) o;
			return this.order.equals(asGMP.order)
					&& this.components.equals(asGMP.components);
		}

		@Override
		public String toString() {
			return "(" + order + "," + components + ")";
		}
	}
	
	
	protected static Map<GetPartitionsArgument, List<Multiset<Integer>>> memMoments = new HashMap<GetPartitionsArgument,List<Multiset<Integer>>>()
	;
	
	public static List<Multiset<Integer>> getPartitions(Integer order, Integer components
			) {
		List<Multiset<Integer>> moments = new LinkedList<Multiset<Integer>>();
		if (components==0){
			Multiset<Integer> moment = HashMultiset.<Integer>create();
			moments.add(moment); 
			return moments; 
		}
		if (components == 1) {
			Multiset<Integer> moment = HashMultiset.<Integer>create();
			moment.add(0, order);
			moments.add(moment);
			return moments;
		}
		if (order == 0) {
			moments.add(HashMultiset.<Integer>create());
			return moments;
		}
		for (int i = 0; i <= order; i++) {
			List<Multiset<Integer>> tmpMoments = getPartitions(order - i,
					components - 1);
			for (Multiset<Integer> moment : tmpMoments) {
				moment.add(components - 1, i);
				Multiset<Integer> copyOfMoment = HashMultiset.<Integer>create();
				for (Multiset.Entry<Integer> entry:moment.entrySet()){
					copyOfMoment.add(entry.getElement(), entry.getCount());
				}
				moments.add(copyOfMoment);
			}
		}
		return moments;
	}
	
	
	protected static List<Multiset<Integer>> getMomentsMem(Integer order, Integer components) { 
		GetPartitionsArgument entry = new GetPartitionsArgument(order, components);
		if (memMoments.get(entry) != null) {
			return memMoments.get(entry);
		}
		List<Multiset<Integer>> moments = getPartitions(order, components);
		memMoments.put(entry, moments);
		return moments;
	}
	
	
	public static int getMultinomialCoefficient(int n,Multiset<Integer> r){
		int ret = 1; 
		int sum = 0; 
		for (Multiset.Entry<Integer> entry:r.entrySet()){
			sum+=entry.getCount(); 
			ret*=Binomial.choose(sum, entry.getCount());
		}
		return ret; 
	}
	
	


}
