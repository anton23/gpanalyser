package uk.ac.imperial.doc.gpepa.representation.components;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import uk.ac.imperial.doc.jexpressions.utils.ToStringUtils;


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
		if (pattern instanceof AnyComponent)
			return true;
		return this.equals(pattern);
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
	public List<Prefix> getPrefixes(PEPAComponentDefinitions definitions) {
		List<Prefix> ret = new LinkedList<Prefix>();
		for (Prefix p : choices) {
			PEPAComponent newConinuation = definitions.getShorthand(p
					.getContinuation());
			ret.add(new Prefix(p.getAction(), p.getRate(), newConinuation));
		}
		return ret;
	}

	@Override
	public Set<String> getActions() {
		Set<String> ret = new HashSet<String>();
		for (Prefix p : choices) {
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
		for (Prefix p : choices) {
			if (!(p.getContinuation() instanceof Stop)) {
				// ret.add(p.getContinuation()); //TODO check
				Set<PEPAComponent> states = p.getContinuation()
						.getDerivativeStates(known, definitions);
				ret.addAll(states);
			}
		}
		return ret;
	}

	public Choice(List<Prefix> choices) {
		super();
		this.choices = choices;
	}

	private List<Prefix> choices;

	public List<Prefix> getChoices() {
		return choices;
	}

	public String toString() {
		return ToStringUtils.iterableToSSV(choices, "+");
	}

}
