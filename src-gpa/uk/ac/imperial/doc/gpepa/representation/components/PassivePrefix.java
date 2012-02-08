package uk.ac.imperial.doc.gpepa.representation.components;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;

import java.util.List;

/**
 * Class representing a (action, n x T).continuation triple.
 *
 * @author Matej Kohut
 *
 */
public class PassivePrefix extends AbstractPrefix {

    private AbstractExpression weight;      //T

    // the return value of this function should not matter
    // MAX is used for check, that this function
    // really is unused in the final model. It ould be used,
    // if a Choice offered both a passive and an active prefix
    // for the same action.
    public AbstractExpression getRate() {
        return DoubleExpression.MAX;
    }

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

    public AbstractExpression getWeight() {
        if (immediates.size() > 0) {
            return ProductExpression.create(weight, immediateSum);
        }
        return weight;
    }

    public AbstractPrefix getCooperationImpl
            (String newAction,
             AbstractPrefix otherAbstractPrefix,
             AbstractExpression otherApparentRate,
             AbstractExpression otherApparentWeight,
             AbstractExpression thisApparentRate,
             AbstractExpression thisApparentWeight,
             PEPAComponent newContinuation,
             List<ImmediatePrefix> newImmediates) {
        if (otherAbstractPrefix instanceof Prefix)
        {
            return new Prefix(newAction,
                    ProductExpression.create(otherAbstractPrefix.getRate(),
                        DivExpression.create(weight, thisApparentWeight)),
                    null, newContinuation, newImmediates);
        }
        return null;
    }

    public PassivePrefix(String action, AbstractExpression rate,
                         AbstractExpression weight, PEPAComponent continuation,
                         List<ImmediatePrefix> immediates)
    {
        super();
        this.action = action;
        this.continuation = continuation;
        this.weight = weight;
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
        String immediatesString = "";
        for (ImmediatePrefix imm : immediates)
        {
            immediatesString += ", " + imm.getAction ();
        }
        return "(" + action + immediatesString + ", T, "
            + weight + ")." + continuationString;
    }
}
