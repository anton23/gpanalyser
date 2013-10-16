package uk.ac.imperial.doc.pctmc.representation.accumulations;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.CollectUsedMomentsVisitor;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;

public class AccumulationVariable {
	protected AbstractExpression ddt;
	protected int order;
	
	public AccumulationVariable(AbstractExpression ddt) {
		super();
		this.ddt = ddt;
		calculateOrder();
	}

	public AbstractExpression getDdt() {
		return ddt;
	}
	
	protected void calculateOrder() {
		CollectUsedMomentsVisitor visitor = new CollectUsedMomentsVisitor();
		ddt.accept(visitor);
		order = 0;
		for (CombinedPopulationProduct m : visitor.getUsedCombinedMoments()) {
			order = Math.max(order, m.getOrder());
		}	
	}
	
	public int getOrder() {
		return order;
	}
	
	

	@Override
	public String toString() {
		return "acc(" + ddt.toString() + ")";
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
