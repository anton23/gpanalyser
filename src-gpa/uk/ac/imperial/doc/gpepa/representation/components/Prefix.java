package uk.ac.imperial.doc.gpepa.representation.components;

import uk.ac.imperial.doc.jexpressions.expressions.*;

/**
 * Class representing a (action,rate parameter).continuation triple.
 * 
 * @author Anton Stefanek
 * 
 */
public class Prefix extends AbstractPrefix {

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Prefix))
			return false;
		Prefix asPrefix = (Prefix) o;
		return getAllActions().equals(asPrefix.getAllActions())
				&& rate.equals(asPrefix.getRate())
				&& continuation.equals(asPrefix.getContinuation());
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	protected final AbstractExpression rate;

	public AbstractExpression getRate() {
        return rate;
	}

    // dummy function
    public AbstractExpression getWeight() {
        return DoubleExpression.ZERO;
    }

    public AbstractPrefix getCooperationImpl
            (String newAction,
             AbstractPrefix otherAbstractPrefix,
             AbstractExpression otherApparentRate,
             AbstractExpression otherApparentWeight,
             AbstractExpression thisApparentRate,
             AbstractExpression thisApparentWeight,
             PEPAComponent newContinuation) {
        if (otherAbstractPrefix instanceof Prefix)
        {
            AbstractExpression coopRate = DivDivMinExpression.create(
                    getRate(), otherAbstractPrefix.getRate(),
                    thisApparentRate, otherApparentRate);
            return new Prefix(newAction, coopRate, null,
                    newContinuation);
        }
        if (otherAbstractPrefix instanceof PassivePrefix)
        {
            return new Prefix(newAction,
                    ProductExpression.create
                        (getRate(), DivExpression.create
                            (otherAbstractPrefix.getWeight(),
                            otherApparentWeight)),
                    null, newContinuation);
        }
        throw new Error("Unsupported cooperation between Prefix and "
            + otherAbstractPrefix.getClass().getName());
    }

	public Prefix(String action, AbstractExpression rate,
            AbstractExpression weight, PEPAComponent continuation) {
		super();
		this.action = action;
		this.rate = rate;
		this.continuation = continuation;
	}

	public String toString() {
		String continuationString = continuation.toString();
		if (continuation instanceof Choice
				&& ((Choice) continuation).getChoices().size() == 1
				|| continuation instanceof ComponentId
				|| continuation instanceof Stop) {
		} else {
			continuationString = "(" + continuationString + ")";
		}
		return "(" + action + "," + rate + ")." + continuationString;
	}
}
