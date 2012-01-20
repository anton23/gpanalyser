package uk.ac.imperial.doc.gpepa.representation.components;

import com.rits.cloning.Cloner;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;
import uk.ac.imperial.doc.jexpressions.utils.ToStringUtils;

import java.util.*;

public class PEPAComponentDefinitions {
	private Map<String, PEPAComponent> definitions;

	private Map<PEPAComponent, String> inverseDefinitions;

	public PEPAComponent getShorthand(PEPAComponent c) {
		if (inverseDefinitions.containsKey(c)) {
			return new ComponentId(inverseDefinitions.get(c));
		}
		return c;
	}

	public AbstractExpression getApparentRateExpression(final String action,
			PEPAComponent from) {
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
	
	public PEPAComponentDefinitions removeVanishingStates() {
        Cloner deepcloner = new Cloner();
        PEPAComponentDefinitions newDefinitions = deepcloner.deepClone(this);
        Map<Choice, String> choices = getChoiceComponents(newDefinitions);
        Map<PEPAComponent, ImmediatePrefix> imms
            = getImmediatesMap(newDefinitions);
        List<String> choicesToRemove = new LinkedList<String>();

        for (Choice choice : choices.keySet()) {
            ImmediatePrefix imm = choice.getImmediate();
            if (imm != null) {
                choicesToRemove.add
                    (newDefinitions.inverseDefinitions.get(choice));

                boolean predecessorExists = false;
                for (Choice otherChoice : choices.keySet()) {
                    List<AbstractPrefix> prefixes = otherChoice.getChoices();
                    for (AbstractPrefix prefix : prefixes) {
                        PEPAComponent shorthand = newDefinitions.getShorthand(choice);
                        if (prefix.getContinuation().equals(shorthand)) {
                            predecessorExists = shorthand.equals
                                    (newDefinitions.getShorthand(otherChoice));
                            String name = newDefinitions.inverseDefinitions
                                    .remove(otherChoice);
                            List<ImmediatePrefix> immediates
                                = new LinkedList<ImmediatePrefix>();
                            PEPAComponent newCont = getImmediatesList
                                (prefix.getContinuation(), imms, immediates);
                            prefix.addImmediates(immediates);
                            prefix.setContinuation(newCont);
                            // refreshing hash
                            newDefinitions.inverseDefinitions
                                    .put(otherChoice, name);
                        }
                    }
                }

                if (!predecessorExists) {
                    choicesToRemove.remove
                        (newDefinitions.inverseDefinitions.get(choice));
                    choice.getChoices().remove(imm);
                    choice.getChoices().add(new Prefix(imm.getAction(),
                        new DoubleExpression(250.0),
                        imm.getContinuation(), imm.getImmediatesRaw()));
                }
            }
        }
        
        Map<String, PEPAComponent> defMap = newDefinitions.getDefinitions();
        for (String name : choicesToRemove) {
            defMap.remove(name);
        }

        return new PEPAComponentDefinitions(newDefinitions.getDefinitions());
    }

    private static Map<Choice, String>
        getChoiceComponents(PEPAComponentDefinitions definitions) {
        Map<Choice, String> choices = new HashMap<Choice, String>();

        for (String name : definitions.getDefinitions().keySet()) {
            PEPAComponent comp = definitions.getComponentDefinition(name);
            if (comp instanceof Choice) {
                choices.put((Choice) comp, name);
            }
        }

        return choices;
    }
    
    private static Map<PEPAComponent, ImmediatePrefix>
        getImmediatesMap(PEPAComponentDefinitions definitions) {
        Map<Choice, String> choices = getChoiceComponents(definitions);
        Map<PEPAComponent, ImmediatePrefix> result
            = new HashMap<PEPAComponent, ImmediatePrefix>();

        for (Choice choice : choices.keySet()) {
            ImmediatePrefix imm = choice.getImmediate();
            if (imm != null) {
                result.put(new ComponentId(choices.get(choice)), imm);
            }
        }

        return result;
    }

    private PEPAComponent getImmediatesList(PEPAComponent cont,
            Map<PEPAComponent, ImmediatePrefix> imms,
            List<ImmediatePrefix> immediates) {
        ImmediatePrefix prefix = imms.get(cont);
        if (prefix == null || immediates.contains(prefix)) {
            return cont;
        }
        PEPAComponent newCont = prefix.getContinuation();
        immediates.add(prefix);
        immediates.addAll(prefix.getImmediatesRaw());
        newCont = getImmediatesList(newCont, imms, immediates);

        return newCont;
    }
}

