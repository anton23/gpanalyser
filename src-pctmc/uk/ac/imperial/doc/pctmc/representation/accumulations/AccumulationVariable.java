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
	
}
