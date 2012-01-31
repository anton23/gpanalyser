package uk.ac.imperial.doc.gpepa.representation.components;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.utils.ToStringUtils;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


/**
 * Representation of a choice. Differs from the formal definition in that that
 * it can only contain prefixes and not constants, i.e. (a,r).P1 + P2 is not
 * allowed.
 * 
 * @author Anton Stefanek
 * 
 */
public class Choice extends PEPAComponent {

	@Override
	public boolean matchPattern(PEPAComponent pattern) {
		return (pattern instanceof AnyComponent) || equals(pattern);
	}

    @Override
    public boolean containsComponent(ComponentId component) {
        return false;
    }

    @Override
	public Set<String> getActions(PEPAComponentDefinitions definitions) {
		return getActions();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Choice))
			return false;
		Choice asChoice = (Choice) o;
		return choices.equals(asChoice.getChoices());
	}

	@Override
	public int hashCode() {
		return choices.hashCode();
	}

	@Override
	public List<AbstractPrefix> getPrefixes(PEPAComponentDefinitions definitions) {
		List<AbstractPrefix> ret = new LinkedList<AbstractPrefix>();
		for (AbstractPrefix p : choices) {
			PEPAComponent newContinuation = definitions.getShorthand(p
					.getContinuation());
            try {
			    ret.add(p.getClass().getDeclaredConstructor
                        (String.class, AbstractExpression.class, PEPAComponent.class,
                                List.class)
                        .newInstance(p.getAction(), p.getRate(),
                                newContinuation, p.getImmediatesRaw()));
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
		}
		return ret;
	}

    @Override
	public Set<String> getActions() {
		Set<String> ret = new HashSet<String>();
		for (AbstractPrefix p : choices) {
			ret.add(p.getAction());
		}
		return ret;
	}

	@Override
	public Set<PEPAComponent> getDerivativeStates(Set<PEPAComponent> known,
			PEPAComponentDefinitions definitions) {
		Set<PEPAComponent> ret = new HashSet<PEPAComponent>();
		PEPAComponent shorthand = definitions.getShorthand(this);
		if (known.contains(this))
			return ret;
		ret.add(shorthand);
		known.add(shorthand);
		known.add(this);
		for (AbstractPrefix p : choices) {
			if (!(p.getContinuation() instanceof Stop)) {
				// ret.add(p.getContinuation()); //TODO check
				Set<PEPAComponent> states = p.getContinuation()
						.getDerivativeStates(known, definitions);
				ret.addAll(states);
			}
		}
		return ret;
    }

	public Choice(List<AbstractPrefix> choices) {
		super();
		this.choices = choices;
	}

	private List<AbstractPrefix> choices;

	public List<AbstractPrefix> getChoices() {
		return choices;
	}

	public String toString() {
		return ToStringUtils.iterableToSSV(choices, "+");
	}

    public ImmediatePrefix getImmediate() {
        for (AbstractPrefix choice : choices) {
            if (choice instanceof ImmediatePrefix)
            {
                return (ImmediatePrefix)choice;
            }
        }
        return null;
    }
}
