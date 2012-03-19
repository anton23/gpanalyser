package examples.odes;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IExpressionVisitor;

public class ODEVariableExpression extends AbstractExpression{
	protected String name;
	
	public ODEVariableExpression(String name) {
		super();
		this.name = name;
	}
	
	

	public String getName() {
		return name;
	}



	@Override
	public String toString() {
		return name;
	}

	@Override
	public void accept(IExpressionVisitor v) {
		if (v instanceof IODEVariableExpressionVisitor){
			((IODEVariableExpressionVisitor)v).visit(this);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		ODEVariableExpression other = (ODEVariableExpression) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	} 
	

}
