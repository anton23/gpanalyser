package uk.ac.imperial.doc.pctmc.representation.accumulations;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;

public class AccumulationVariable {
	protected AbstractExpression ddt;

	public AccumulationVariable(AbstractExpression ddt) {
		super();
		this.ddt = ddt;
	}

	public AbstractExpression getDdt() {
		return ddt;
	}
	
	// TODO fix
	public int getOrder() {
		return 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ddt == null) ? 0 : ddt.hashCode());
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
		AccumulationVariable other = (AccumulationVariable) obj;
		if (ddt == null) {
			if (other.ddt != null)
				return false;
		} else if (!ddt.equals(other.ddt))
			return false;
		return true;
	}
	
}
