package uk.ac.imperial.doc.gpepa.representation.components;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivDivMinExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;

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

    public AbstractPrefix getCooperationImpl(AbstractPrefix otherAbstractPrefix,
                                             AbstractExpression otherApparentRate,
                                             AbstractExpression thisApparentRate,
                                             PEPAComponent newContinuation) {
        if (otherAbstractPrefix instanceof Prefix)
        {
            AbstractExpression coopRate = DivDivMinExpression.create(
                    getRate(), otherAbstractPrefix.getRate(),
                    thisApparentRate, otherApparentRate);
            Prefix nPref = new Prefix(getAction(), coopRate,
                    newContinuation, immediates);
            nPref.addImmediates(otherAbstractPrefix.getImmediatesRaw());
            return nPref;
        }
        if (otherAbstractPrefix instanceof PassivePrefix)
        {
            Prefix nPref = new Prefix(getAction(),
                thisApparentRate, newContinuation, immediates);
            nPref.addImmediates(otherAbstractPrefix.getImmediatesRaw());
            return nPref;
        }
        throw new Error("Unsupported cooperation between Prefix and "
            + otherAbstractPrefix.getClass().getName());
    }

    public AbstractExpression getCountOrientedRate (AbstractExpression expr)
    {
        return ProductExpression.create(expr, getRate());
    }

	public Prefix(String action, AbstractExpression rate,
			PEPAComponent continuation, List<ImmediatePrefix> immediates) {
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
