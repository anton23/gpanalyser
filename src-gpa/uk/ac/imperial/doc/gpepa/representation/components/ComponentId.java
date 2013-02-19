package uk.ac.imperial.doc.gpepa.representation.components;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Class representing a constant defining a component.
 * 
 * @author Anton Stefanek
 * 
 */
public class ComponentId extends PEPAComponent {
	@Override
	public boolean matchPattern(PEPAComponent pattern) {
		return (pattern instanceof AnyComponent) || equals(pattern);
	}

    @Override
    public boolean containsComponent(ComponentId component) {
        return name.equals (component.getName ());
    }

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof ComponentId))
			return false;
		ComponentId asConstant = (ComponentId) o;
		return name.equals(asConstant.getName());
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

    @Override
    public void unfoldImplicitCooperations (PEPAComponentDefinitions definitions,
                                            Set<PEPAComponent> visited) {
        if (visited.contains(this)) {
            return;
        }
        visited.add(this);
        definitions.getComponentDefinition(name).unfoldImplicitCooperations(definitions, visited);
    }

    @Override
    public Set<String> getActions(PEPAComponentDefinitions definitions) {
        return definitions.getComponentDefinition(name).getActions();
    }

    @Override
	public void getActionsRecursively(PEPAComponentDefinitions definitions,
                                      Set<String> actions, Set<PEPAComponent> visited) {
        if (visited.contains(this)) {
            return;
        }
        visited.add(this);
		definitions.getComponentDefinition(name).getActionsRecursively(definitions, actions, visited);
	}

	@Override
	// TODO Does not allow redefinitions of constants, i.e. A = B
	public List<AbstractPrefix> getPrefixes(PEPAComponentDefinitions definitions) {
        PEPAComponent component = definitions.getComponentDefinition(name);
        if (component != null)
        {
            return component.getPrefixes(definitions);
        }
        return new LinkedList<AbstractPrefix>();
	}

    @Override
	public Set<String> getActions() {
		return new HashSet<String>();
	}

	@Override
	public Set<PEPAComponent> getDerivativeStates(Set<PEPAComponent> known,
			PEPAComponentDefinitions system) {
		Set<PEPAComponent> ret = new HashSet<PEPAComponent>();
		ret.add(this);
		if (!known.contains(this)) {
			known.add(this);
			PEPAComponent componentDefinition = system.getComponentDefinition(name);
			if (componentDefinition==null){
				throw new AssertionError("Component with name " + name + " unknown!");
			}
			ret.addAll(componentDefinition.getDerivativeStates(
					known, system));
		}
		return ret;
	}

	public ComponentId(String id) {
		super();
		this.name = id;
	}

	private String name;

	public String getName() {
		return name;
	}

	public String toString() {
		return name;
	}

}
