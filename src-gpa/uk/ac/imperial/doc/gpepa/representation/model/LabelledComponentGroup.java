package uk.ac.imperial.doc.gpepa.representation.model;

import uk.ac.imperial.doc.gpepa.representation.components.AbstractPrefix;
import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponent;
import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponentDefinitions;
import uk.ac.imperial.doc.gpepa.representation.components.Stop;
import uk.ac.imperial.doc.gpepa.representation.group.Group;
import uk.ac.imperial.doc.gpepa.representation.group.GroupComponentPair;
import uk.ac.imperial.doc.gpepa.states.GPEPAState;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;

import java.util.*;

/**
 * Representation of the labelled component group structure of a model.
 * 
 * @author Anton Stefanek
 * 
 */
public class LabelledComponentGroup extends GroupedModel {
	@Override
	public Set<String> getActions(PEPAComponentDefinitions definitions) {
		return group.getActions(definitions);
	}

	

	@Override
	public List<PEPAEvolutionEvent> getEvolutionEvents(

			final PEPAComponentDefinitions definitions,
			Set<String> restrictedActions) {
		List<PEPAEvolutionEvent> events = new LinkedList<PEPAEvolutionEvent>();
		for (final PEPAComponent derivative : group
				.getComponentDerivatives(definitions)) {
			for (final AbstractPrefix prefix : derivative.getPrefixes(definitions)) {
				if (!restrictedActions.contains(prefix.getAction())) {


					AbstractExpression rate
                        = ProductExpression.create(CombinedProductExpression
                            .createMeanExpression (new GPEPAState
                                    (new GroupComponentPair(label, derivative))),
                            prefix.getParameter());

					List<GroupComponentPair> increases = new LinkedList<GroupComponentPair>();
					PEPAComponent continuation = prefix.getContinuation();
					if (!(continuation instanceof Stop))
						increases.add(new GroupComponentPair(label,
								continuation));
					List<GroupComponentPair> decreases = new LinkedList<GroupComponentPair>();
					decreases.add(new GroupComponentPair(label, derivative));
					events.add(new PEPAEvolutionEvent(
                            prefix.getAction(),
                            prefix.getImmediates(),
                            rate, increases, decreases));
				}
			}
		}
		return events;
	}

	@Override
	public AbstractExpression getMomentOrientedRateExpression(String action,
				PEPAComponentDefinitions definitions) {
		Set<PEPAComponent> componentDerivatives = getComponentDerivatives(
				label, definitions);
		List<AbstractExpression> summands = new LinkedList<AbstractExpression>();
		for (PEPAComponent p : componentDerivatives) {
			AbstractExpression rate = definitions.getApparentRateExpression(
					action, p);
			if (!(rate.equals(DoubleExpression.ZERO))) {
				summands.add(ProductExpression.create(CombinedProductExpression
                        .createMeanExpression(new GPEPAState
                                (new GroupComponentPair(label, p))), rate));
			}
		}
		return SumExpression.create(summands);
	}

    @Override
    public AbstractExpression getComponentRateExpression
            (String action, PEPAComponentDefinitions definitions,
             GroupComponentPair groupComponentPair) {
        if (groupComponentPair.getGroup().equals(label)) {
            AbstractExpression rate = definitions.getApparentRateExpression(
                    action, groupComponentPair.getComponent());
            return ProductExpression.create(CombinedProductExpression
                    .createMeanExpression(new GPEPAState(groupComponentPair)),
                    rate);
        }
        else {
            return DoubleExpression.ZERO;
        }
    }

    public LabelledComponentGroup(String label, Group group) {
		super();
		this.label = label;
		this.group = group;
		componentGroups = new HashMap<String, LabelledComponentGroup>();
		componentGroups.put(label, this);
	}

	private String label;

	/**
	 * Returns the label of this labelled component group.
	 * 
	 * @return
	 */
	public String getLabel() {
		return label;
	}

	private Group group;

	public Group getGroup() {
		return group;
	}

	public String toString() {
		return label + "{" + group.toString() + "}";
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		return result;
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		LabelledComponentGroup other = (LabelledComponentGroup) obj;
		if (group == null) {
			if (other.group != null)
				return false;
		} else if (!group.equals(other.group))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		return true;
	}

    @Override
    public void enumerateGroupedModelParents
        (Map<GroupedModel, GroupedModel> groupedModels, GroupedModel owner) {
        groupedModels.put (this, owner);
    }
}
