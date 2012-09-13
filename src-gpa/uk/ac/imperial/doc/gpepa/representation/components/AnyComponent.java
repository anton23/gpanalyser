package uk.ac.imperial.doc.gpepa.representation.components;

import java.util.List;
import java.util.Set;

public class AnyComponent extends PEPAComponent {

	@Override
	public boolean equals(Object o) {
		return (o instanceof AnyComponent);
	}

	@Override
	public Set<String> getActions() {
		return null;
	}

    @Override
    public Set<String> getActions(PEPAComponentDefinitions definitions) {
        return null;
    }

    @Override
    public void getActionsRecursively(PEPAComponentDefinitions definitions,
                                      Set<String> actions, Set<PEPAComponent> visited) {}

    @Override
	public Set<PEPAComponent> getDerivativeStates(Set<PEPAComponent> known,
			PEPAComponentDefinitions definitions) {
		return null;
	}

	@Override
	public List<AbstractPrefix> getPrefixes(PEPAComponentDefinitions definitions) {
		return null;
	}

    @Override
	public int hashCode() {
		return 0;
	}

	@Override
	public boolean matchPattern(PEPAComponent pattern) {
		return true;
	}

    @Override
    public boolean containsComponent(ComponentId component) {
        return true;
    }

    @Override
	public String toString() {
		return "_";
	}

}
