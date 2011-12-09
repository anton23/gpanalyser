package uk.ac.imperial.doc.pctmc.representation;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.utils.ToStringUtils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class PCTMC {
	private BiMap<State,Integer> stateIndex; 
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
		stateIndex = HashBiMap.<State,Integer>create(); 
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

	public BiMap<State, Integer> getStateIndex() {
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
		BiMap<Integer, State> inverseIndex = stateIndex.inverse();
		for (int i = 0; i<initCounts.length; i++){
			initMap.put(inverseIndex.get(i),initCounts[i]); 
		}
		return ToStringUtils.mapToDefinitionList(initMap, "=", ";\n") + 
			   ToStringUtils.iterableToSSV(evolutionEvents, "\n");
	}
}
