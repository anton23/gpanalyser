package uk.ac.imperial.doc.gpepa.representation.components;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;

/**
 * Class representing a (action, n x T).continuation triple.
 *
 * @author Matej Kohut
 *
 */
public class PassivePrefix extends AbstractPrefix {

    protected AbstractExpression weight;      //T

     public AbstractExpression getRate() {
        return DoubleExpression.ZERO;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PassivePrefix))
            return false;
        PassivePrefix asPrefix = (PassivePrefix) o;
        return getAllActions().equals(asPrefix.getAllActions())
                && continuation.equals(asPrefix.getContinuation());
    }

    @Override
    public int hashCode() {
        return toString().hashCode() + 2;
    }

    public AbstractExpression getWeight() {
        return weight;
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
            return new Prefix(newAction,
                    ProductExpression.create(otherAbstractPrefix.getRate(),
                        DivExpression.create(weight, thisApparentWeight)),
                    null, newContinuation);
        }
        return null;
    }

    public PassivePrefix(String action, AbstractExpression rate,
                         AbstractExpression weight, PEPAComponent continuation)
    {
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

        return "(" + action + ", T, "
            + weight + ")." + continuationString;
    }
}
