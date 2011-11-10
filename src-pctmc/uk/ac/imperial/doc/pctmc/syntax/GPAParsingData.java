package uk.ac.imperial.doc.pctmc.syntax;

import java.util.Set;

public class GPAParsingData extends ParsingData {
	protected Set<String> componentNames;
	protected Set<String> groupLabels;

	public GPAParsingData(Set<String> componentNames, Set<String> groupLabels) {
		super();
		this.componentNames = componentNames;
		this.groupLabels = groupLabels;
	}

	public Set<String> getComponentNames() {
		return componentNames;
	}

	public Set<String> getGroupNames() {
		return groupLabels;
	}
	
	
}
