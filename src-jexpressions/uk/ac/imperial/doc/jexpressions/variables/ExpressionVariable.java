package uk.ac.imperial.doc.jexpressions.variables;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IExpressionVisitor;

public class ExpressionVariable extends AbstractExpression{
	protected String name;
	
	protected AbstractExpression unfolded; 
	
	public void setUnfolded(AbstractExpression unfolded){
		this.unfolded = unfolded; 
	}
	
	

	public AbstractExpression getUnfolded() {
		return unfolded;
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
		ExpressionVariable other = (ExpressionVariable) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public void accept(IExpressionVisitor v) {
		if (v instanceof IExpressionVariableVisitor){
			((IExpressionVariableVisitor)v).visit(this);
		} else {
			unfolded.accept(v);
		}	
	}

	@Override
	public String toString() {
		return "$"+name; 
	}

	public ExpressionVariable(String name) {
		super();
		this.name = name;
	} 	
}
