package uk.ac.imperial.doc.igpepa.representation.model;

import uk.ac.imperial.doc.gpepa.representation.group.GroupComponentPair;
import uk.ac.imperial.doc.gpepa.representation.model.PEPAEvolutionEvent;
import uk.ac.imperial.doc.gpepa.states.GPEPAActionCount;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.pctmc.representation.EvolutionEvent;

import java.util.List;
import java.util.Set;

public class iPEPAEvolutionEvent extends PEPAEvolutionEvent
{
    private List<String> immediateActions;

	public iPEPAEvolutionEvent (String action, List<String> immediateActions,
           AbstractExpression rate, List<GroupComponentPair> increases,
           List<GroupComponentPair> decreases)
    {
		super (action, rate, increases, decreases);
		this.immediateActions = immediateActions;
	}

    @Override
	public EvolutionEvent getEvolutionEvent (Set<String> countActions)
    {
        EvolutionEvent event = super.getEvolutionEvent (countActions);
        for (String action : immediateActions)
        {
            if (countActions.contains (action))
            {
                event.getIncreasing ().add (new GPEPAActionCount (action));
            }
        }
        return event;
    }

    public List<String> getImmediateActions ()
    {
        return immediateActions;
    }
}