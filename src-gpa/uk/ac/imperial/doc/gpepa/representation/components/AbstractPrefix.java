package uk.ac.imperial.doc.gpepa.representation.components;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;

import java.util.LinkedList;
import java.util.List;

public abstract class AbstractPrefix
{
    protected String action;
    protected PEPAComponent continuation;
    protected AbstractExpression immediateSum = DoubleExpression.ZERO;
    protected List<ImmediatePrefix> immediates
        = new LinkedList<ImmediatePrefix> ();

    public String getAction ()
    {
        return action;
    }

    public PEPAComponent getContinuation ()
    {
        return continuation;
    }

    public void setContinuation (PEPAComponent continuation)
    {
        this.continuation = continuation;
    }

    public List<ImmediatePrefix> getImmediatesRaw ()
    {
        return immediates;
    }

    public List<String> getImmediates ()
    {
        List<String> actions = new LinkedList<String> ();
        for (ImmediatePrefix immediate : immediates)
        {
            actions.add (immediate.getAction ());
        }
        return actions;
    }

    public void addImmediate (ImmediatePrefix imm)
    {
        immediateSum = SumExpression.create (immediateSum, imm.getWeight ());
        immediates.add (imm);
    }
    
    public void addImmediates (List<ImmediatePrefix> immediates)
    {
        for (ImmediatePrefix immediate : immediates)
        {
            addImmediate (immediate);
        }
    }

    public abstract AbstractExpression getRate ();

    public AbstractPrefix getCooperation
            (AbstractPrefix otherAbstractPrefix,
             AbstractExpression otherApparentRate,
             AbstractExpression thisApparentRate,
             PEPAComponent newContinuation)
    {
        if (!action.equals(otherAbstractPrefix.getAction())) return null;
        return getCooperationImpl(otherAbstractPrefix, otherApparentRate,
            thisApparentRate, newContinuation);
    }

    protected abstract AbstractPrefix getCooperationImpl
            (AbstractPrefix otherAbstractPrefix,
             AbstractExpression otherApparentRate,
             AbstractExpression thisApparentRate,
             PEPAComponent newContinuation);

    public abstract AbstractExpression getCountOrientedRate
        (AbstractExpression expr);
}
