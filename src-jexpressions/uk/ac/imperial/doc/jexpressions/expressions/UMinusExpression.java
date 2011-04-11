package uk.ac.imperial.doc.jexpressions.expressions;


/**
 * An expression for the unary minus -a. 
 * @author as1005
 *
 */
public class UMinusExpression extends AbstractExpression {

	protected AbstractExpression e;

	public AbstractExpression getE() {
		return e;
	}

	public UMinusExpression(AbstractExpression e) {
		super();
		this.e = e;
	}

	@Override
	public void accept(IExpressionVisitor v) {
		v.visit(this);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof UMinusExpression))
			return false;
		UMinusExpression asUMinus = (UMinusExpression) o;
		return e.equals(asUMinus.getE());
	}


	@Override
	public int hashCode() {
		return -e.hashCode();
	}

	@Override
	public String toString() {
		return "(-" + e.toString() + ")";
	}

}
