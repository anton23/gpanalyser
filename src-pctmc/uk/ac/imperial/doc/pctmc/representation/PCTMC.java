package uk.ac.imperial.doc.pctmc.representation;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.utils.ToStringUtils;
import uk.ac.imperial.doc.pctmc.expressions.ExpressionWalkerForStates;

public class PCTMC {
	private Map<State,Integer> stateIndex; 
	private State[] inverseStateIndex; 
	private AbstractExpression[] initCounts; 
	private Map<State,AbstractExpression> initMap; 
	protected Collection<EvolutionEvent> evolutionEvents; 
	
	public Map<State, AbstractExpression> getInitMap() {
		return initMap;
	}

	public PCTMC(Map<State,AbstractExpression> initMap, Collection<EvolutionEvent> evolutionEvents){		 
		this.evolutionEvents = evolutionEvents;
		this.initMap = initMap; 
		prepareStateIndexes(initMap);		
	}
	
	private void prepareStateIndexes(Map<State,AbstractExpression> initMap){
		stateIndex = new HashMap<State,Integer>(); 
		inverseStateIndex = new State[initMap.size()];
		initCounts = new AbstractExpression[initMap.size()];
		
		int i = 0;
		for (Map.Entry<State, AbstractExpression> entry:initMap.entrySet()){
			stateIndex.put(entry.getKey(),i);
			inverseStateIndex[i] = entry.getKey(); 
			initCounts[i] = entry.getValue(); 
			i++;
		}
	}

	public Map<State, Integer> getStateIndex() {
		return stateIndex;
	}

	public State[] getInverseStateIndex() {
		return inverseStateIndex;
	}

	public AbstractExpression[] getInitCounts() {
		return initCounts;
	}

	public Collection<EvolutionEvent> getEvolutionEvents() {
		return evolutionEvents;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((evolutionEvents == null) ? 0 : evolutionEvents.hashCode());
		result = prime * result + Arrays.hashCode(initCounts);
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
		PCTMC other = (PCTMC) obj;
		if (evolutionEvents == null) {
			if (other.evolutionEvents != null)
				return false;
		} else if (!evolutionEvents.equals(other.evolutionEvents))
			return false;
		if (!Arrays.equals(initCounts, other.initCounts))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		Map<State,AbstractExpression> initMap = new HashMap<State, AbstractExpression>(); 
		//BiMap<Integer, State> inverseIndex = stateIndex.inverse();
		Map<Integer, State> inverseIndex = new HashMap<Integer, State>();		
		for (Entry<State, Integer> e : stateIndex.entrySet()) {
			inverseIndex.put(e.getValue(), e.getKey());
		}
		for (int i = 0; i<initCounts.length; i++){
			initMap.put(inverseIndex.get(i),initCounts[i]); 
		}
		return ToStringUtils.mapToDefinitionList(initMap, "=", ";\n") + 
			   ToStringUtils.iterableToSSV(evolutionEvents, "\n");
	}

	public Map<State,Map<State, Integer>> getDistanceBetweenPopulations(int minDist) {
		Map<State,Map<State, Integer>> distMap = new HashMap<State,Map<State, Integer>>();
		
		for (int i=0; i<inverseStateIndex.length; i++) {
			State a = inverseStateIndex[i];
			for (int j=i; j<inverseStateIndex.length; j++) {
				State b = inverseStateIndex[j];
				int dist = 0;
				if (!a.equals(b)) {
					dist = 1000000;
					HashSet<State> fringe = new HashSet<State>();
					HashSet<State> explored = new HashSet<State>();
					fringe.add(a);
					fringe.add(b);
					int curDist = 1;
					while (!fringe.isEmpty()) {
						HashSet<State> newFringe = new HashSet<State>();
						for (EvolutionEvent e : this.evolutionEvents) {
							ExpressionWalkerForStates efs = new ExpressionWalkerForStates();
							e.getRate().accept(efs);
							for (State s : fringe) {
								if (!e.getIncreasing().contains(s)) {
									continue;
								}
								newFringe.addAll(efs.getStates());
								if (s != a && efs.getStates().contains(a) ||
									s != b && efs.getStates().contains(b)) {
									dist = curDist;
									break;
								}
							}
						}
						
						if (dist == curDist) {
							break;
						}

						++curDist;
						explored.addAll(fringe);
						newFringe.removeAll(fringe);
						fringe = newFringe;
					}
				}

				if (dist >= minDist) {
					if (distMap.get(a) == null) {
						distMap.put(a,new HashMap<State, Integer>());
					}
					if (distMap.get(b) == null) {
						distMap.put(b,new HashMap<State, Integer>());
					}
					distMap.get(a).put(b,dist);
					distMap.get(b).put(a,dist);
					System.out.println("Dist("+a+","+b+")="+dist);
				}
			}
		}
		
		return distMap;
	}
}
