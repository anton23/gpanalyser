package uk.ac.imperial.doc.jexpressions.expanded;

import java.util.Set;

import com.google.common.collect.Multiset;

public class ExpandedExpression {
	
	private Set<Multiset<ExpandedExpression>> representation;

	public ExpandedExpression(Set<Multiset<ExpandedExpression>> representation) {
		this.representation = representation;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((representation == null) ? 0 : representation.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof ExpandedExpression)){
			return false;
		}
		ExpandedExpression other = (ExpandedExpression) obj;
        return representation.equals(other.representation);
	}

	public Set<Multiset<ExpandedExpression>> getRepresentation() {
		return representation;
	}
}