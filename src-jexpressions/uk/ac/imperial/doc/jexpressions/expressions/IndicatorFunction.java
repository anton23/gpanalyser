package uk.ac.imperial.doc.jexpressions.expressions;


public class IndicatorFunction extends AbstractExpression {
	
	protected ExpressionCondition condition;
	
	

	public IndicatorFunction(ExpressionCondition condition) {
		super();
		this.condition = condition;
	}

	@Override
	public void accept(IExpressionVisitor v) {
		v.visit(this);
	}

	@Override
	public String toString() {
		return "[" + condition.toString() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 0;
		result = prime * result
				+ ((condition == null) ? 0 : condition.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
	
		if (getClass() != obj.getClass())
			return false;
		IndicatorFunction other = (IndicatorFunction) obj;
		if (condition == null) {
			if (other.condition != null)
				return false;
		} else if (!condition.equals(other.condition))
			return false;
		return true;
	}

	public ExpressionCondition getCondition() {
		return condition;
	}
}
