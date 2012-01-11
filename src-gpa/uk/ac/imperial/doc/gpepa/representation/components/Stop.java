package uk.ac.imperial.doc.gpepa.representation.components;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Representation of a process not capable of any action.
 * 
 * @author Anton Stefanek
 * 
 */
public class Stop extends PEPAComponent {

	@Override
	public boolean matchPattern(PEPAComponent pattern) {
		if (pattern instanceof AnyComponent)
			return true;
		return this.equals(pattern);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		return (o instanceof Stop);
	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public Set<String> getActions(PEPAComponentDefinitions definitions) {
		return getActions();
	}

	@Override
	public List<Prefix> getPrefixes(PEPAComponentDefinitions definitions) {
		return new LinkedList<Prefix>();
	}

	@Override
	public Set<String> getActions() {
		return new HashSet<String>();
	}

	@Override
	public Set<PEPAComponent> getDerivativeStates(Set<PEPAComponent> known,
			PEPAComponentDefinitions system) {
		return new HashSet<PEPAComponent>();
	}

	public Stop() {
		super();
	}

	public String toString() {
		return "stop";
	}

}
