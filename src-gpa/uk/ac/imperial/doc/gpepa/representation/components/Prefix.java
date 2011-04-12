package uk.ac.imperial.doc.gpepa.representation.components;

import uk.ac.imperial.doc.jexpressions.constants.ConstantExpression;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;

/**
 * Class representing a (action,rate parameter).continuation triple.
 * 
 * @author Anton Stefanek
 * 
 */
public class Prefix {

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Prefix))
			return false;
		Prefix asPrefix = (Prefix) o;
		return action.equals(asPrefix.getAction())
				&& rate.equals(asPrefix.getRate())
				&& continuation.equals(asPrefix.getContinuation());
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	private String action;
	private AbstractExpression rate;
	private PEPAComponent continuation;

	public String getAction() {
		return action;
	}

	public AbstractExpression getRate() {
		return rate;
	}

	public PEPAComponent getContinuation() {
		return continuation;
	}

	public Prefix(String action, String rate, PEPAComponent continuation) {
		super();
		this.action = action;
		this.rate = new ConstantExpression(rate);
		this.continuation = continuation;
	}

	public Prefix(String action, AbstractExpression rate,
			PEPAComponent continuation) {
		super();
		this.action = action;
		this.rate = rate;
		this.continuation = continuation;
	}

	public String toString() {
		String continuationString = continuation.toString();
		if (continuation instanceof Choice
				&& ((Choice) continuation).getChoices().size() == 1
				|| continuation instanceof Constant
				|| continuation instanceof Stop) {
		} else {
			continuationString = "(" + continuationString + ")";
		}
		return "(" + action + "," + rate + ")." + continuationString;
	}
}
