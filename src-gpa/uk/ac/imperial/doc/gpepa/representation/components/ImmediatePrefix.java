package uk.ac.imperial.doc.gpepa.representation.components;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;

public class ImmediatePrefix extends AbstractPrefix {

    private AbstractExpression weight;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ImmediatePrefix))
            return false;
        ImmediatePrefix asPrefix = (ImmediatePrefix) o;
        return action.equals(asPrefix.getAction())
                && weight.equals(asPrefix.getWeight())
                && continuation.equals(asPrefix.getContinuation());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public AbstractExpression getRate() {
        return DoubleExpression.ZERO;
    }

    public AbstractExpression getWeight() {
        return weight;
    }

    public AbstractPrefix getCooperationImpl(AbstractPrefix otherAbstractPrefix,
                                             AbstractExpression otherApparentRate,
                                             AbstractExpression thisApparentRate,
                                             PEPAComponent newContinuation) {
        throw new Error("Unsupported cooperation between ImmediatePrefix" +
                " and " + otherAbstractPrefix.getClass().getName());
    }

    public AbstractExpression getCountOrientedRate (AbstractExpression expr)
    {
        throw new Error("Cannot get count oriented rate for " +
                "ImmediatePrefix, must do vanishing state removal.");
    }

    public ImmediatePrefix(String action, AbstractExpression weight,
                           PEPAComponent continuation) {
        super();
        this.action = action;
        this.continuation = continuation;
        this.weight = weight;
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
        return action + "." + continuationString;
    }
}
