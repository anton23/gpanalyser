package uk.ac.imperial.doc.igpepa.representation.components;

import uk.ac.imperial.doc.gpepa.representation.components.*;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;

import java.util.List;

public class ImmediatePrefix extends AbstractPrefix implements iPEPAPrefix
{
    private AbstractExpression weight;
    private ImmediatesMixin immediates = new ImmediatesMixin ();

    @Override
    public boolean equals (Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof ImmediatePrefix))
            return false;
        ImmediatePrefix asPrefix = (ImmediatePrefix) o;
        return getAllActions().equals(asPrefix.getAllActions())
                && weight.equals (asPrefix.getWeight ())
                && continuation.equals(asPrefix.getContinuation ());
    }

    @Override
    public int hashCode ()
    {
        return toString ().hashCode ();
    }

    public AbstractExpression getRate ()
    {
        return DoubleExpression.ZERO;
    }

    public AbstractExpression getWeight ()
    {
        return weight;
    }

    public AbstractPrefix getCooperationImpl (String newAction,
         AbstractPrefix otherAbstractPrefix,
         AbstractExpression otherApparentRate,
         AbstractExpression otherApparentWeight,
         AbstractExpression thisApparentRate,
         AbstractExpression thisApparentWeight,
         PEPAComponent newContinuation)
    {
        throw new Error("Unsupported cooperation between ImmediatePrefix"
                + " and " + otherAbstractPrefix.getClass().getName());
    }

    public ImmediatePrefix (String action, AbstractExpression weight,
           PEPAComponent continuation)
    {
        super ();
        this.action = action;
        this.continuation = continuation;
        this.weight = weight;
    }

    public String toString()
    {
        String continuationString = continuation.toString ();
        if (continuation instanceof Choice
                && ((Choice) continuation).getChoices ().size () == 1
                || continuation instanceof ComponentId
                || continuation instanceof Stop)
        {
        }
        else
        {
            continuationString = "(" + continuationString + ")";
        }
        return action + "." + continuationString;
    }

    public List<String> getAllActions()
    {
        List<String> actions = super.getAllActions();
        actions.addAll (getImmediates ());
        return actions;
    }

    //ImmediatesMixin

    public List<ImmediatePrefix> getImmediatesRaw ()
    {
        return immediates.getImmediatesRaw ();
    }

    public List<ImmediatePrefix> getImmediatesRawCopy ()
    {
        return immediates.getImmediatesRawCopy ();
    }

    public List<String> getImmediates ()
    {
        return immediates.getImmediates ();
    }

    public void addImmediate (ImmediatePrefix imm)
    {
        immediates.addImmediate (imm);
    }

    public void addImmediates (List<ImmediatePrefix> immediates)
    {
        this.immediates.addImmediates (immediates);
    }
}
