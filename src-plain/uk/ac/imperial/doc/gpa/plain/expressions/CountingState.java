package uk.ac.imperial.doc.gpa.plain.expressions;

import uk.ac.imperial.doc.pctmc.representation.State;

public class CountingState extends State {
	private String component;

	public CountingState(String component) {
		super();
		this.component = component;
	}

	public String getComponent() {
		return component;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((component == null) ? 0 : component.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "#" + component; 
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (getClass() != obj.getClass())
			return false;
		CountingState other = (CountingState) obj;
		if (component == null) {
			if (other.component != null)
				return false;
		} else if (!component.equals(other.component))
			return false;
		return true;
	} 
	
	
}