package uk.ac.imperial.doc.igpepa.representation.components;

import java.util.List;

import uk.ac.imperial.doc.gpepa.representation.components.AbstractPrefix;
import uk.ac.imperial.doc.gpepa.representation.components.Choice;
import uk.ac.imperial.doc.gpepa.representation.components.ComponentId;
import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponent;
import uk.ac.imperial.doc.gpepa.representation.components.PassivePrefix;
import uk.ac.imperial.doc.gpepa.representation.components.Stop;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;

public class iPassivePrefix extends PassivePrefix implements iPEPAPrefix
{
    private ImmediatesMixin immediates = new ImmediatesMixin ();

    public AbstractPrefix getCooperationImpl (String newAction,
         AbstractPrefix otherAbstractPrefix,
         AbstractExpression otherApparentRate,
         AbstractExpression otherApparentWeight,
         AbstractExpression thisApparentRate,
         AbstractExpression thisApparentWeight,
         PEPAComponent newContinuation)
    {
        if (otherAbstractPrefix instanceof iPrefix)
        {
            List<ImmediatePrefix> newImmediates
                = immediates.getImmediatesRawCopy ();
            newImmediates.addAll
                    (((iPrefix) otherAbstractPrefix).getImmediatesRawCopy ());
            return new iPrefix (newAction,
                    ProductExpression.create (otherAbstractPrefix.getRate (),
                            DivExpression.create (weight, thisApparentWeight)),
                    null, newContinuation, newImmediates);
        }
        return null;
    }

    public iPassivePrefix (String action, AbstractExpression rate,
        AbstractExpression weight, PEPAComponent continuation,
        List<ImmediatePrefix> immediateActions)
    {
        super (action, rate, weight, continuation);
        immediates.addImmediates (immediateActions);
    }

    public String toString ()
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
        String immediatesString = "";
        for (String imm : immediates.getImmediates ())
        {
            immediatesString += ", " + imm;
        }
        return "(" + action + immediatesString + ", T, "
                + weight + ")." + continuationString;
    }

    public AbstractExpression getWeight()
    {
        if (immediates.getImmediatesRaw().size() > 0)
        {
            return ProductExpression.create
                (weight, immediates.getImmediatesSum ());
        }
        return weight;
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

    public AbstractPrefix getCooperation
        (String cooperationAction, AbstractPrefix otherAbstractPrefix,
         AbstractExpression otherApparentRate,
         AbstractExpression otherApparentWeight,
         AbstractExpression thisApparentRate,
         AbstractExpression thisApparentWeight,
         PEPAComponent newContinuation)
    {
        if (!(otherAbstractPrefix instanceof iPEPAPrefix))
        {
            throw new Error ("Cooperation of " + this.getClass ().getName ()
                + " and " + otherAbstractPrefix.getClass ().getName ());
        }

        String newAction = immediates.getCooperationAction
            (cooperationAction, this,  (iPEPAPrefix)otherAbstractPrefix);
        if (newAction == null)
        {
            return null;
        }
        return getCooperationImpl (cooperationAction, otherAbstractPrefix,
            otherApparentRate, otherApparentWeight, thisApparentRate,
            thisApparentWeight, newContinuation);
    }
}
