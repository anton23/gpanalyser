package uk.ac.imperial.doc.jexpressions.expressions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import uk.ac.imperial.doc.jexpressions.utils.ToStringUtils;


/**
 * An expression for a sum of a number of expressions a1+a2+...+an. 
 * @author as1005
 *
 */
public class SumExpression extends AbstractExpression {

	@Override
	public void accept(IExpressionVisitor v) {
		v.visit(this);
	}

	@Override
	public int hashCode() {
		return summands.hashCode();
	}

	@Override
	public String toString() {
		String ret = "";
		ret = ToStringUtils.iterableToSSV(summands, "+");
		if (summands.size() > 1) {
			ret = "(" + ret + ")";
		}
		return ret;
	}

	private List<AbstractExpression> summands;

	public List<AbstractExpression> getSummands() {
		return summands;
	}

	protected SumExpression(Collection<AbstractExpression> s) {
		this.summands = new ArrayList<AbstractExpression>(s);
	}

	public static AbstractExpression create(Collection<AbstractExpression> s) {
		return create(s.toArray(new AbstractExpression[0]));
	}

	public static AbstractExpression create(AbstractExpression... s) {
		if (s.length == 1)
			return s[0];
		List<AbstractExpression> summands = new LinkedList<AbstractExpression>();
		for (AbstractExpression e : s) {
			if (!e.equals(new DoubleExpression(0.0))) {
				if (e instanceof SumExpression){
					summands.addAll(((SumExpression)e).getSummands());
				} else {
					summands.add(e);
				}
			}
		}
		if (summands.isEmpty()) {
			return new DoubleExpression(0.0);
		}
		if (summands.size() == 1) {
			return summands.get(0);
		} else {
			return new SumExpression(summands);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof SumExpression))
			return false;
		SumExpression asSum = (SumExpression) o;
		return this.summands.equals(asSum.getSummands());
	}

}
