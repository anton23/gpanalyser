package uk.ac.imperial.doc.gpepa.representation.components;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class representing a constant defining a component.
 * 
 * @author Anton Stefanek
 * 
 */
public class Constant extends PEPAComponent {
	@Override
	public boolean matchPattern(PEPAComponent pattern) {
		if (pattern instanceof AnyComponent)
			return true;
		return this.equals(pattern);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Constant))
			return false;
		Constant asConstant = (Constant) o;
		return name.equals(asConstant.getName());
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public Set<String> getActions(PEPAComponentDefinitions definitions) {
		return definitions.getComponentDefinition(name).getActions();
	}

	@Override
	// TODO Does not allow redefinitions of constants, i.e. A = B
	public List<Prefix> getPrefixes(PEPAComponentDefinitions definitions) {
		return definitions.getComponentDefinition(name)
				.getPrefixes(definitions);
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

	public Constant(String id) {
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
