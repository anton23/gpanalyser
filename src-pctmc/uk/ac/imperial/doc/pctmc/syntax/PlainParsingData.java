package uk.ac.imperial.doc.pctmc.syntax;

import java.util.Set;

import com.google.common.collect.Multiset;

public class PlainParsingData extends ParsingData {
	protected Set<Multiset<String>> states;

	public PlainParsingData(Set<Multiset<String>> states) {
		super();
		this.states = states;
	}

	public Set<Multiset<String>> getStates() {
		return states;
	}
}
