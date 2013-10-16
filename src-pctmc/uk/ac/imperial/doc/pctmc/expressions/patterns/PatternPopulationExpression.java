package uk.ac.imperial.doc.pctmc.expressions.patterns;


import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IExpressionVisitor;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.representation.State;

public class PatternPopulationExpression extends ExpressionVariable{
	
	private State state;

	public PatternPopulationExpression(State state) {
		super(state.toString());
		this.state = state;
	}

	public void setUnfolded(AbstractExpression unfolded){
		this.unfolded = unfolded; 
	}

	@Override
	public void accept(IExpressionVisitor v) {
		if (v instanceof IPatternVisitor){
			((IPatternVisitor)v).visit(this);
		} else {
			super.accept(v); 
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (getClass() != obj.getClass())
			return false;
		PatternPopulationExpression other = (PatternPopulationExpression) obj;
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result=0;
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "%" + state.toString(); 
	}
	
	public State getState() {
		return state;
	}

}
