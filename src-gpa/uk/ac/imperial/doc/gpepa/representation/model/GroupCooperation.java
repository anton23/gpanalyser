package uk.ac.imperial.doc.gpepa.representation.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponentDefinitions;
import uk.ac.imperial.doc.gpepa.representation.group.GroupComponentPair;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivDivMinExpression;
import uk.ac.imperial.doc.jexpressions.expressions.MinExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ZeroExpression;
import uk.ac.imperial.doc.jexpressions.utils.ToStringUtils;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

/**
 * Representation of the group cooperation structure of a model.
 * 
 * @author Anton Stefanek
 * 
 */
public class GroupCooperation extends GroupedModel {

	@Override
	public Set<String> getActions(PEPAComponentDefinitions definitions) {
		Set<String> ret = new HashSet<String>();
		ret.addAll(left.getActions(definitions));
		ret.addAll(right.getActions(definitions));
		return ret;
	}


	@Override
	public List<PEPAEvolutionEvent> getEvolutionEvents(
			
			final PEPAComponentDefinitions definitions,
			Set<String> restrictedActions) {
		List<PEPAEvolutionEvent> events = new LinkedList<PEPAEvolutionEvent>();
		Set<String> newRestrictedActions = new HashSet<String>();
		newRestrictedActions.addAll(restrictedActions);
		newRestrictedActions.addAll(actions);
		// events without cooperation between left and right
		events.addAll(left.getEvolutionEvents(definitions,
				newRestrictedActions));
		events.addAll(right.getEvolutionEvents(definitions,
				newRestrictedActions));
		// events from cooperation between left and right
		List<PEPAEvolutionEvent> leftEvents = left.getEvolutionEvents(
				 definitions, restrictedActions);
		List<PEPAEvolutionEvent> rightEvents = right.getEvolutionEvents(
				definitions, restrictedActions);
		Multimap<String, PEPAEvolutionEvent> leftActionmap = LinkedHashMultimap
				.<String, PEPAEvolutionEvent> create();
		Multimap<String, PEPAEvolutionEvent> rightActionmap = LinkedHashMultimap
				.<String, PEPAEvolutionEvent> create();

		for (PEPAEvolutionEvent le : leftEvents) {
			if (actions.contains(le.getAction())) {
				leftActionmap.put(le.getAction(), le);
			}
		}

		for (PEPAEvolutionEvent re : rightEvents) {
			if (actions.contains(re.getAction())) {
				rightActionmap.put(re.getAction(), re);
			}
		}

		for (final String action : leftActionmap.keySet()) {
			for (final PEPAEvolutionEvent le : leftActionmap.get(action)) {
				for (final PEPAEvolutionEvent re : rightActionmap.get(action)) {
					List<GroupComponentPair> increases = new LinkedList<GroupComponentPair>();
					List<GroupComponentPair> decreases = new LinkedList<GroupComponentPair>();
					increases.addAll(le.getIncreases());
					increases.addAll(re.getIncreases());
					decreases.addAll(le.getDecreases());
					decreases.addAll(re.getDecreases());

					AbstractExpression leftApparentRate = left
							.getMomentOrientedRateExpression(action,
									 definitions);

					AbstractExpression rightApparentRate = right
							.getMomentOrientedRateExpression(action,
									definitions);
					AbstractExpression leftRate = le.getRate();
					AbstractExpression rightRate = re.getRate();

					AbstractExpression rate = DivDivMinExpression.create(
							leftRate, rightRate, leftApparentRate,
							rightApparentRate);
					events.add(new PEPAEvolutionEvent(action, rate, increases,
							decreases));
				}
			}
		}

		return events;
	}

	@Override
	public AbstractExpression getMomentOrientedRateExpression(String action,
			PEPAComponentDefinitions definitions) {
		AbstractExpression leftRate = left.getMomentOrientedRateExpression(
				action, definitions);
		AbstractExpression rightRate = right.getMomentOrientedRateExpression(
				action, definitions);

		if (actions.contains(action)) {
			if (leftRate instanceof ZeroExpression
					|| rightRate instanceof ZeroExpression) {
				return new ZeroExpression();
			}
			return MinExpression.create(leftRate, rightRate);
		} else {
			if (leftRate instanceof ZeroExpression
					&& rightRate instanceof ZeroExpression) {
				return new ZeroExpression();
			} else if (leftRate instanceof ZeroExpression) {
				return rightRate;
			} else if (rightRate instanceof ZeroExpression) {
				return leftRate;
			} else
				return SumExpression.create(leftRate, rightRate);
		}
	}

	public GroupCooperation(GroupedModel left, GroupedModel right,
			Set<String> actions) {
		super();
		this.left = left;
		this.right = right;
		this.actions = actions;
		componentGroups = new HashMap<String, LabelledComponentGroup>();
		componentGroups.putAll(left.getComponentGroups());
		componentGroups.putAll(right.getComponentGroups());
	}

	private GroupedModel left;
	private GroupedModel right;
	private Set<String> actions;

	public String toString() {
		String leftString = left.toString();
		if (left instanceof GroupCooperation) {
			leftString = "(" + leftString + ")";
		}
		String rightString = right.toString();
		if (right instanceof GroupCooperation) {
			rightString = "(" + rightString + ")";
		}
		return leftString + "<" + ToStringUtils.iterableToSSV(actions, ",")
				+ ">" + rightString;
	}
}
