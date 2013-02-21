package uk.ac.imperial.doc.pctmc.representation.accumulations;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;

/**
 * @author as1005
 *
 */
public class NamedAccumulation extends AccumulationVariable {
	
	protected String name;

	public NamedAccumulation(String name) {
		super(new DoubleExpression(1.0));
		this.name = name;
	}
	
	
	public void setDDt (AbstractExpression ddt) {
		this.ddt = ddt;
		calculateOrder();
	}
	
	@Override
	public String toString() {
		return "~" + name;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 0;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		NamedAccumulation other = (NamedAccumulation) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	
	
}
