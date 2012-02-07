package uk.ac.imperial.doc.gpepa.representation.components;

import uk.ac.imperial.doc.jexpressions.expressions.*;

import java.util.List;

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
		return action.equals(asPrefix.getAction())
				&& rate.equals(asPrefix.getRate())
				&& continuation.equals(asPrefix.getContinuation());
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	private AbstractExpression rate;

	public AbstractExpression getRate() {
        if (immediates.size() > 0)
        {
		    return ProductExpression.create(rate, immediateSum);
        }
        return rate;
	}

    // dummy function
    public AbstractExpression getWeight() {
        return DoubleExpression.ZERO;
    }

    public AbstractPrefix getCooperationImpl(String newAction,
                                             AbstractPrefix otherAbstractPrefix,
                                             AbstractExpression otherApparentRate,
                                             AbstractExpression otherApparentWeight,
                                             AbstractExpression thisApparentRate,
                                             AbstractExpression thisApparentWeight,
                                             PEPAComponent newContinuation,
                                             List<ImmediatePrefix> newImmediates) {
        if (otherAbstractPrefix instanceof Prefix)
        {
            AbstractExpression coopRate = DivDivMinExpression.create(
                    getRate(), otherAbstractPrefix.getRate(),
                    thisApparentRate, otherApparentRate);
            return new Prefix(newAction, coopRate, null,
                    newContinuation, newImmediates);
        }
        if (otherAbstractPrefix instanceof PassivePrefix)
        {
            return new Prefix(newAction,
                    ProductExpression.create
                        (getRate(), DivExpression.create
                            (otherAbstractPrefix.getWeight(),
                            otherApparentWeight)),
                    null, newContinuation, newImmediates);
        }
        throw new Error("Unsupported cooperation between Prefix and "
            + otherAbstractPrefix.getClass().getName());
    }

	public Prefix(String action, AbstractExpression rate,
            AbstractExpression weight, PEPAComponent continuation,
            List<ImmediatePrefix> immediates) {
		super();
		this.action = action;
		this.rate = rate;
		this.continuation = continuation;
        addImmediates(immediates);
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
