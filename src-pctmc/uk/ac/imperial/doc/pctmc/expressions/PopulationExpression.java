package uk.ac.imperial.doc.pctmc.expressions;


import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IExpressionVisitor;
import uk.ac.imperial.doc.pctmc.representation.State;

/**
 * Representation of a population count expression. 
 * @author Anton Stefanek
 *
 */
public class PopulationExpression extends AbstractExpression {

	@Override
	public void accept(IExpressionVisitor v) {
		if (v instanceof IPopulationVisitor) {
			((IPopulationVisitor) v).visit(this);
		} else {
			throw new AssertionError("Unsupported visit to a population expression!"); 
		}
	}

	private State state;

	@Override
	public String toString() {
		return "E(" + state.toString() + ")";
	}

	public State getState() {
		return state;
	}

	public PopulationExpression(State state) {
		super();
		this.state = state;
	}

	public int hashCode() {
		return state.hashCode();
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof PopulationExpression))
			return false;
		PopulationExpression asCount = (PopulationExpression) o;
		return this.state.equals(asCount.getState());
	}

}
