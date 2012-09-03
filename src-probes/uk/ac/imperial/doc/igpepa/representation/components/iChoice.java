package uk.ac.imperial.doc.igpepa.representation.components;

import uk.ac.imperial.doc.gpepa.representation.components.AbstractPrefix;
import uk.ac.imperial.doc.gpepa.representation.components.Choice;
import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponent;
import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponentDefinitions;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;

import java.util.LinkedList;
import java.util.List;

public class iChoice extends Choice
{
	@Override
	public List<AbstractPrefix> getPrefixes
        (PEPAComponentDefinitions definitions)
    {
        if (!(definitions instanceof iPEPAComponentDefinitions))
        {
            throw new Error ("iChoice used with incompatible definitions.");
        }
		List<AbstractPrefix> ret = new LinkedList<AbstractPrefix> ();
		for (AbstractPrefix p : choices)
        {
			PEPAComponent newContinuation = definitions.getShorthand
                (p.getContinuation());
            try
            {
			    ret.add (p.getClass().getDeclaredConstructor
                        (String.class, AbstractExpression.class,
                                AbstractExpression.class, PEPAComponent.class,
                                List.class)
                    .newInstance(p.getAction(), p.getRate(), p.getWeight(),
                            newContinuation,
                            ((iPEPAPrefix) p).getImmediatesRawCopy()));
            }
            catch (Exception ex)
            {
                ex.printStackTrace ();
            }
		}
		return ret;
	}

    public iChoice (List<AbstractPrefix> choices)
    {
        super (choices);
    }

    public ImmediatePrefix getImmediate ()
    {
        for (AbstractPrefix choice : choices)
        {
            if (choice instanceof ImmediatePrefix)
            {
                return (ImmediatePrefix)choice;
            }
        }
        return null;
    }
}
