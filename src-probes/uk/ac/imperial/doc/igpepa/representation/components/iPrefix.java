package uk.ac.imperial.doc.igpepa.representation.components;

import java.util.List;

import uk.ac.imperial.doc.gpepa.representation.components.AbstractPrefix;
import uk.ac.imperial.doc.gpepa.representation.components.Choice;
import uk.ac.imperial.doc.gpepa.representation.components.ComponentId;
import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponent;
import uk.ac.imperial.doc.gpepa.representation.components.Prefix;
import uk.ac.imperial.doc.gpepa.representation.components.Stop;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivDivMinExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;

public class iPrefix extends Prefix implements iPEPAPrefix
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
        List<ImmediatePrefix> newImmediates
            = immediates.getImmediatesRawCopy ();
        newImmediates.addAll
                (((iPEPAPrefix) otherAbstractPrefix).getImmediatesRawCopy());
        if (otherAbstractPrefix instanceof iPrefix)
        {
            AbstractExpression coopRate = DivDivMinExpression.create (
                    getRate (), otherAbstractPrefix.getRate (),
                    thisApparentRate, otherApparentRate);
            return new iPrefix (newAction, coopRate, null,
                    newContinuation, newImmediates);
        }
        if (otherAbstractPrefix instanceof iPassivePrefix)
        {
            return new iPrefix (newAction,
                    ProductExpression.create
                        (getRate (), DivExpression.create
                                (otherAbstractPrefix.getWeight (),
                                        otherApparentWeight)),
                    null, newContinuation, newImmediates);
        }
        throw new Error("Unsupported cooperation between iPrefix and "
            + otherAbstractPrefix.getClass ().getName ());
    }

	public iPrefix (String action, AbstractExpression rate,
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
        return "(" + action + immediatesString + ", "
                + rate + ")." + continuationString;
    }

	public AbstractExpression getRate ()
    {
        if (immediates.getImmediatesRaw ().size() > 0)
        {
		    return ProductExpression.create
                (rate, immediates.getImmediatesSum ());
        }
        return rate;
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
