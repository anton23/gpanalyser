package uk.ac.imperial.doc.jexpressions.expressions;

/**
 * An expression for integer valued numerical constants.
 * 
 * @author Anton Stefanek
 * 
 */
public class IntegerExpression extends AbstractExpression {

	private int value;

	public IntegerExpression(int value) {
		super();
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	@Override
	public void accept(IExpressionVisitor v) {
		v.visit(this);
	}

	@Override
	public String toString() {
		return value + "";
	}

	@Override
	public int hashCode() {
		return value;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		IntegerExpression other = (IntegerExpression) obj;
		if (value != other.value)
			return false;
		return true;
	}

}
