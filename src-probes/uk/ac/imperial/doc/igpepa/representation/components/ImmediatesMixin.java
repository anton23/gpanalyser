package uk.ac.imperial.doc.igpepa.representation.components;

import java.util.LinkedList;
import java.util.List;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;

public class ImmediatesMixin implements iPEPA
{
    private AbstractExpression immediateSum = DoubleExpression.ZERO;
    private List<ImmediatePrefix> immediates
        = new LinkedList<ImmediatePrefix>();

    public List<ImmediatePrefix> getImmediatesRaw ()
    {
        return immediates;
    }

    public List<ImmediatePrefix> getImmediatesRawCopy ()
    {
        return new LinkedList<ImmediatePrefix> (immediates);
    }

    public List<String> getImmediates ()
    {
        List<String> actions = new LinkedList<String> ();
        for (ImmediatePrefix immediate : immediates)
        {
            actions.add(immediate.getAction());
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

    public AbstractExpression getImmediatesSum ()
    {
        return immediateSum;
    }

    public String getCooperationAction (String cooperationAction,
            iPEPAPrefix firAbstractPrefix, iPEPAPrefix secAbstractPrefix)
    {
        int thisContains = 0;
        int otherContains = 0;
        if (firAbstractPrefix.getImmediates().contains (cooperationAction))
        {
            thisContains = 2;
        }
        if (cooperationAction.equals (firAbstractPrefix.getAction()))
        {
            thisContains = 1;
        }
        if (secAbstractPrefix.getImmediates ().contains (cooperationAction))
        {
            otherContains = 2;
        }
        if (cooperationAction.equals (secAbstractPrefix.getAction ()))
        {
            otherContains = 1;
        }

        // we disallow the cooperation on action, if it isn't called by one
        // components, or if it is ensuing immediate action on both only
        if ((thisContains == 0 || otherContains == 0)
                || (thisContains == 2 && otherContains == 2)) {
            return null;
        }

        String newAction = cooperationAction;

        // new prefix will have the main action as the new action
        if (thisContains == 2)
        {
            newAction = firAbstractPrefix.getAction();
        }
        if (otherContains == 2)
        {
            newAction = secAbstractPrefix.getAction ();
        }

        return newAction;
    }
}
