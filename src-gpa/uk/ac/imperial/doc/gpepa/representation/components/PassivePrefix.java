package uk.ac.imperial.doc.gpepa.representation.components;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IntegerExpression;

import java.util.List;

/**
 * Class representing a (action, T).continuation triple.
 *
 * @author Matej Kohut
 *
 */
public class PassivePrefix extends AbstractPrefix {

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PassivePrefix))
            return false;
        PassivePrefix asPrefix = (PassivePrefix) o;
        return action.equals(asPrefix.getAction())
                && continuation.equals(asPrefix.getContinuation());
    }

    @Override
    public int hashCode() {
        return toString().hashCode() + 2;
    }

    public AbstractExpression getRate() {
        return DoubleExpression.ZERO;
    }

    public AbstractPrefix getCooperationImpl(AbstractPrefix otherAbstractPrefix,
                                             AbstractExpression otherApparentRate,
                                             AbstractExpression thisApparentRate,
                                             PEPAComponent newContinuation) {
        if (otherAbstractPrefix instanceof Prefix)
        {
            Prefix nPref = new Prefix(getAction(),
                otherApparentRate, newContinuation, immediates);
            nPref.addImmediates(otherAbstractPrefix.getImmediatesRaw());
            return nPref;
        }
        throw new Error("Unsupported cooperation between PassivePrefix and "
                + otherAbstractPrefix.getClass().getName());
    }

    public AbstractExpression getCountOrientedRate (AbstractExpression expr)
    {
        return new IntegerExpression(Integer.MAX_VALUE);
    }

    // dummy compatible constructor
    public PassivePrefix(String action, AbstractExpression rate,
                         PEPAComponent continuation,
                         List<ImmediatePrefix> immediates)
    {
        this(action, continuation, immediates);
    }

    public PassivePrefix(String action, PEPAComponent continuation,
                         List<ImmediatePrefix> immediates) {
        super();
        this.action = action;
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
        return "(" + action + ", T)." + continuationString;
    }
}
