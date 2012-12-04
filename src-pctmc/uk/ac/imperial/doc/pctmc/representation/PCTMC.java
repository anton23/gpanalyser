package uk.ac.imperial.doc.pctmc.representation;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.utils.ToStringUtils;

public class PCTMC {
	private Map<State,Integer> stateIndex; 
	private State[] inverseStateIndex; 
	private AbstractExpression[] initCounts; 
	private Map<State,AbstractExpression> initMap; 
	private Collection<EvolutionEvent> evolutionEvents; 
	
	
	
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
		StringBuilder ret = new StringBuilder();
		Map<State,AbstractExpression> initMap = new HashMap<State, AbstractExpression>(); 
		//BiMap<Integer, State> inverseIndex = stateIndex.inverse();
		Map<Integer, State> inverseIndex = new HashMap<Integer, State>();		
		for (Entry<State, Integer> e : stateIndex.entrySet()) {
			inverseIndex.put(e.getValue(), e.getKey());
		}
		for (int i = 0; i<initCounts.length; i++){
			initMap.put(inverseIndex.get(i),initCounts[i]); 
		}		
		ret.append(ToStringUtils.iterableToSSV(evolutionEvents, "\n"));
		ret.append(ToStringUtils.mapToDefinitionList(initMap, "=", ";\n"));
		return ret.toString();
	}
}
