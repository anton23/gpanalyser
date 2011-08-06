package uk.ac.imperial.doc.gpepa.representation.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponent;
import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponentDefinitions;
import uk.ac.imperial.doc.gpepa.representation.components.Prefix;
import uk.ac.imperial.doc.gpepa.representation.components.Stop;
import uk.ac.imperial.doc.gpepa.representation.group.Group;
import uk.ac.imperial.doc.gpepa.representation.group.GroupComponentPair;
import uk.ac.imperial.doc.gpepa.states.GPEPAState;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;
import uk.ac.imperial.doc.pctmc.expressions.PopulationExpression;

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
			/*
			 * PEPAComponent s = definitions
			 * .getComponentDefinition(derivative);
			 */
			for (final Prefix prefix : derivative.getPrefixes(definitions)) {
				if (!restrictedActions.contains(prefix.getAction())) {

					AbstractExpression parameter = prefix.getRate();
					AbstractExpression rate = ProductExpression.create(
							new PopulationExpression(new GPEPAState(new GroupComponentPair(label,
									derivative))), parameter);

					List<GroupComponentPair> increases = new LinkedList<GroupComponentPair>();
					PEPAComponent continuation = prefix.getContinuation();
					if (!(continuation instanceof Stop))
						increases.add(new GroupComponentPair(label,
								continuation));
					List<GroupComponentPair> decreases = new LinkedList<GroupComponentPair>();
					decreases.add(new GroupComponentPair(label, derivative));
					events.add(new PEPAEvolutionEvent(prefix.getAction(), rate,
							increases, decreases));
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
				summands.add(ProductExpression.create(new PopulationExpression(
						new GPEPAState(new GroupComponentPair(label, p))), rate));
			}
		}
		return SumExpression.create(summands);
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

}
