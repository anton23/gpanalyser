package uk.ac.imperial.doc.gpepa.representation.model;

import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponent;
import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponentDefinitions;
import uk.ac.imperial.doc.gpepa.representation.group.Group;
import uk.ac.imperial.doc.gpepa.representation.group.GroupComponentPair;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;

import java.util.*;



/**
 * Class representing a grouped model - either a group cooperation or labelled
 * group.
 * 
 * @author Anton Stefanek
 * 
 */
public abstract class GroupedModel {
	// keeps a map of all leaf labelled component groups
	protected Map<String, LabelledComponentGroup> componentGroups;

	public Map<String, LabelledComponentGroup> getComponentGroups() {
		return componentGroups;
	}

	public abstract Set<String> getActions(PEPAComponentDefinitions definitions);

	// B(G,H)
	/**
	 * Returns all possible derivatives of components within the given group.
	 */
	public Set<PEPAComponent> getComponentDerivatives(String group,
			PEPAComponentDefinitions definitions) {
		Group g = componentGroups.get(group).getGroup();
		return g.getComponentDerivatives(definitions);
	}

	public Set<GroupComponentPair> getGroupComponentPairs(
			PEPAComponentDefinitions system) {
		Set<GroupComponentPair> ret = new HashSet<GroupComponentPair>();
		for (LabelledComponentGroup g : componentGroups.values()) {
			for (PEPAComponent c : getComponentDerivatives(g.getLabel(), system)) {
				ret.add(new GroupComponentPair(g.getLabel(), c));
			}
		}
		return ret;
	}

	public String toGnuplotString() {
		return toString().replace("{", "\\\\{").replace("}", "\\\\}").replace(
				"_", "\\\\_");
	}


	
	
	public AbstractExpression getCountExpression(GroupComponentPair pair) {
		return componentGroups.get(pair.getGroup()).getGroup().getCountExpression(pair.getComponent());
	}
	
	public abstract AbstractExpression getMomentOrientedRateExpression(
			String action, 
			PEPAComponentDefinitions definitions);

    public abstract AbstractExpression getComponentRateExpression(
            String action, PEPAComponentDefinitions definitions,
            GroupComponentPair groupComponentPair);

	public abstract List<PEPAEvolutionEvent> getEvolutionEvents(
			PEPAComponentDefinitions definitions,
			Set<String> restrictedActions);
	
	public List<PEPAEvolutionEvent> getObservableEvolutionEvents(PEPAComponentDefinitions componentDefinitions) {
		List<PEPAEvolutionEvent> allEvents = this.getEvolutionEvents(
				componentDefinitions, new HashSet<String>());
		List<PEPAEvolutionEvent> observableEvents = new LinkedList<PEPAEvolutionEvent>();
		for (PEPAEvolutionEvent e : allEvents) {
			if (!new HashSet<GroupComponentPair>(e.getIncreases())
					.equals(new HashSet<GroupComponentPair>(e.getDecreases()))) {
				observableEvents.add(e);
			}
		}
		return observableEvents;
	}
	
	@Override
	public abstract boolean equals(Object obj);
	
	public abstract void enumerateGroupedModelParents
        (Map<GroupedModel, GroupedModel> groupedModels, GroupedModel owner);
}
