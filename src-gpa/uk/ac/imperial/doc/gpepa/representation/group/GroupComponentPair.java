package uk.ac.imperial.doc.gpepa.representation.group;

import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponent;
import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponentDefinitions;

public class GroupComponentPair {
	private String group;
	private PEPAComponent component;

	public GroupComponentPair getShorthandVersion(
			PEPAComponentDefinitions definitions) {
		return new GroupComponentPair(group, definitions
				.getShorthand(component));
	}

	public GroupComponentPair(String group, PEPAComponent component) {
		super();
		this.group = group;
		this.component = component;
	}

	public String getGroup() {
		return group;
	}

	public PEPAComponent getComponent() {
		return component;
	}

	@Override
	public int hashCode() {
		return group.hashCode() * 23 + component.hashCode();
	}

	public boolean equals(Object o) {
		if (!(o instanceof GroupComponentPair))
			return false;
		GroupComponentPair asPair = (GroupComponentPair) o;
		return asPair.getGroup().equals(group)
				&& asPair.getComponent().equals(component);
	}

	@Override
	public String toString() {
		return group + ":" + component;
	}
}
