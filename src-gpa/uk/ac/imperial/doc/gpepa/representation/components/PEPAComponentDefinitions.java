package uk.ac.imperial.doc.gpepa.representation.components;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;
import uk.ac.imperial.doc.jexpressions.utils.ToStringUtils;


public class PEPAComponentDefinitions {
	private Map<String, PEPAComponent> definitions;

	private Map<PEPAComponent, String> inverseDefinitions;

	public PEPAComponent getShorthand(PEPAComponent c) {
		if (inverseDefinitions.containsKey(c)) {
			return new Constant(inverseDefinitions.get(c));
		}
		return c;
	}

	public AbstractExpression getApparentRateExpression(final String action,
			PEPAComponent from) {
		List<AbstractExpression> summands = new LinkedList<AbstractExpression>();
		for (Prefix p : from.getPrefixes(this)) {
			if (p.getAction().equals(action)) {
				summands.add(p.getRate());
			}
		}

		if (summands.size() == 0) {
			return DoubleExpression.ZERO;
		}
		return SumExpression.create(summands);
	}

	public PEPAComponentDefinitions(Map<String, PEPAComponent> definitions) {
		super();
		this.definitions = definitions;
		inverseDefinitions = new HashMap<PEPAComponent, String>();
		for (Map.Entry<String, PEPAComponent> e : definitions.entrySet()) {
			inverseDefinitions.put(e.getValue(), e.getKey());
		}
	}

	/**
	 * Returns the component defined by the given constant.
	 * 
	 * @param constant
	 * @return
	 */
	public PEPAComponent getComponentDefinition(String constant) {
		return definitions.get(constant);
	}

	/**
	 * Returns textual representation of the definitions.
	 */
	public String toString() {
		return ToStringUtils.mapToDefinitionList(definitions, "=", ";\n");
	}

	/**
	 * Returns all the actions of the model.
	 * 
	 * @return
	 */
	public Set<String> getActions() {
		Set<String> actions = new HashSet<String>();
		for (PEPAComponent c : definitions.values()) {
			actions.addAll(c.getActions());
		}
		return actions;
	}

}
