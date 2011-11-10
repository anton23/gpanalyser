package uk.ac.imperial.doc.pctmc.syntax;

import java.util.Set;

public class GPAParsingData extends ParsingData {
	 protected Set<String> componentNames;

	public GPAParsingData(Set<String> componentNames) {
		super();
		this.componentNames = componentNames;
	}

	public Set<String> getComponentNames() {
		return componentNames;
	}
}
