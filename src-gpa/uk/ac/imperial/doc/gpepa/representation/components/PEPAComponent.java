package uk.ac.imperial.doc.gpepa.representation.components;

import java.util.List;
import java.util.Set;

/**
 * Representation of PEPA components.
 * 
 * @author Anton Stefanek
 * 
 */
public abstract class PEPAComponent {

	public abstract boolean matchPattern(PEPAComponent pattern);

	/**
	 * Returns the derivative states of this component. Given a set of known
	 * states to avoid infinite recursion.
	 *
	 * @param known
	 *            A set of already seen states.
	 * @param definitions
	 * @return
	 */
	public abstract Set<PEPAComponent> getDerivativeStates(
			Set<PEPAComponent> known, PEPAComponentDefinitions definitions);

	/**
	 * Returns all the possible actions from this component.
	 * 
	 * @return
	 */
	public abstract Set<String> getActions();

	public abstract Set<String> getActions(PEPAComponentDefinitions definitions);

	public abstract List<AbstractPrefix> getPrefixes(
			PEPAComponentDefinitions definitions);

    @Override
	public abstract int hashCode();

	@Override
	public abstract boolean equals(Object o);

}
