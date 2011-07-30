package uk.ac.imperial.doc.gpa.pctmc;

import java.util.Collection;
import java.util.Map;

import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponentDefinitions;
import uk.ac.imperial.doc.gpepa.representation.model.GroupedModel;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.pctmc.representation.EvolutionEvent;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.representation.State;

public class GPEPAPCTMC extends PCTMC{
	private PEPAComponentDefinitions componentDefinitions;
	private GroupedModel model;
	
	
	public PEPAComponentDefinitions getComponentDefinitions() {
		return componentDefinitions;
	}


	public GroupedModel getModel() {
		return model;
	}


	public GPEPAPCTMC(Map<State,AbstractExpression> initMap, Collection<EvolutionEvent> evolutionEvents,PEPAComponentDefinitions componentDefinitions, GroupedModel model){
		super(initMap,evolutionEvents);
		this.componentDefinitions = componentDefinitions; 
		this.model = model;
	}
	
	@Override
	public String toString() {
		String ret = "";
		ret+=componentDefinitions.toString();
		ret+="\n";
		ret+=model.toString();
		return ret; 
	}
}
