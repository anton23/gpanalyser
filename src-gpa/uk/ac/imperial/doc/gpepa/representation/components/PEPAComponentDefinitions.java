package uk.ac.imperial.doc.gpepa.representation.components;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;
import uk.ac.imperial.doc.jexpressions.utils.ToStringUtils;

import java.util.*;

public class PEPAComponentDefinitions {
	private Map<String, PEPAComponent> definitions;

    public Map<PEPAComponent, String> getInverseDefinitionsRaw() {
        return inverseDefinitions;
    }

    protected Map<PEPAComponent, String> inverseDefinitions;

	public PEPAComponent getShorthand(PEPAComponent c) {
		if (inverseDefinitions.containsKey(c)) {
			return new ComponentId(inverseDefinitions.get(c));
		}
		return c;
	}

    // assumption: action with same name not used both as timed and immediate
    public AbstractExpression getApparentRateExpression
            (final String action, PEPAComponent from) {
        List<AbstractExpression> summands = new LinkedList<AbstractExpression>();
        for (AbstractPrefix p : from.getPrefixes(this)) {
            if (p.getAction().equals(action)) {
                summands.add(p.getRate());
            }
        }

        if (summands.size() == 0) {
            return DoubleExpression.ZERO;
        }
        return SumExpression.create(summands);
    }

    // assumption: action with same name not used both as timed and immediate
    public RateWeightPair getApparentRateWeightExpressions
            (final String action, PEPAComponent from) {
		List<AbstractExpression> summandsRates = new LinkedList<AbstractExpression>();
        List<AbstractExpression> summandsWeights = new LinkedList<AbstractExpression>();
		for (AbstractPrefix p : from.getPrefixes(this)) {
            if (p.getAction().equals(action)) {
				summandsRates.add(p.getRate());
                summandsWeights.add(p.getWeight());
			}
		}

		if (summandsRates.size() == 0) {
			return new RateWeightPair
                    (DoubleExpression.ZERO, DoubleExpression.ZERO);
		}
		return new RateWeightPair
                (SumExpression.create(summandsRates),
                        SumExpression.create(summandsWeights));
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

	public Map<String, PEPAComponent> getDefinitions() {
		return definitions;
	}

    public class RateWeightPair {

        public AbstractExpression getRate() {
            return rate;
        }

        public AbstractExpression getWeight() {
            return weight;
        }

        private AbstractExpression rate, weight;

        public RateWeightPair(
                AbstractExpression rate, AbstractExpression weight) {
            this.rate = rate;
            this.weight = weight;
        }
    }
}

