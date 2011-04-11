package uk.ac.imperial.doc.jexpressions.expressions;


/**
 * The PEPA div(a,b) expression. This is equal to 0 if b=0 and a/b otherwise.
 * @author Anton Stefanek
 *
 */
public class PEPADivExpression extends AbstractExpression {

	public static AbstractExpression create(AbstractExpression numerator,
			AbstractExpression denominator) {
		if (numerator.equals(denominator)) {
			return new DoubleExpression(1.0);
		}
		if (numerator instanceof ZeroExpression) {
			return new ZeroExpression();
		}
		if (denominator instanceof ZeroExpression) {
			return new ZeroExpression();
		}
		return new PEPADivExpression(numerator, denominator);
	}

	private AbstractExpression denominator;
	private AbstractExpression numerator;
	
	protected PEPADivExpression(AbstractExpression numerator,
			AbstractExpression denominator) {
		super();
		this.numerator = numerator;
		this.denominator = denominator;
	}

	@Override
	public void accept(IExpressionVisitor v) {
		v.visit(this);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof PEPADivExpression))
			return false;
		PEPADivExpression asPEPADiv = (PEPADivExpression) o;
		return this.numerator.equals(asPEPADiv.getNumerator())
				&& this.denominator.equals(asPEPADiv.getDenominator());
	}

	public AbstractExpression getDenominator() {
		return denominator;
	}

	public AbstractExpression getNumerator() {
		return numerator;
	}

	@Override
	public int hashCode() {
		return numerator.hashCode() * 23 + denominator.hashCode();
	}

	@Override
	public String toString() {
		return "div(" + numerator.toString() + "," + denominator.toString()
				+ ")";
	}

}
