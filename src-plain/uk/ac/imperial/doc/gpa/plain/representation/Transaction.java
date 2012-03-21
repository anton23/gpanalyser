package uk.ac.imperial.doc.gpa.plain.representation;

import java.util.List;

import uk.ac.imperial.doc.jexpressions.utils.ToStringUtils;
import uk.ac.imperial.doc.pctmc.representation.State;

public class Transaction extends State{
	List<String> components;
	
	

	public List<String> getComponents() {
		return components;
	}

	@Override
	public String toString() {
		return "{" + ToStringUtils.iterableToSSV(components, ",")+"}";
	}

	public Transaction(List<String> components) {
		super();
		this.components = components;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((components == null) ? 0 : components.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Transaction other = (Transaction) obj;
		if (components == null) {
			if (other.components != null)
				return false;
		} else if (!components.equals(other.components))
			return false;
		return true;
	} 
}
