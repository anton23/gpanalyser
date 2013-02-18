package uk.ac.imperial.doc.igpepa.representation.model;

import uk.ac.imperial.doc.gpepa.representation.group.GroupComponentPair;
import uk.ac.imperial.doc.gpepa.representation.model.PEPAEvolutionEvent;
import uk.ac.imperial.doc.gpepa.states.GPEPAActionCount;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.representation.EvolutionEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class iPEPAEvolutionEvent extends PEPAEvolutionEvent
{
    private final List<String> immediateActions;

	public iPEPAEvolutionEvent (String action, List<String> immediateActions,
           AbstractExpression rate, List<GroupComponentPair> increases,
           List<GroupComponentPair> decreases)
    {
		super (action, rate, increases, decreases);
		this.immediateActions = immediateActions;
	}

    @Override
	public EvolutionEvent getEvolutionEvent
        (Set<String> countActions,
         Map<ExpressionVariable,AbstractExpression> unfoldedVariables)
    {
        EvolutionEvent event
            = super.getEvolutionEvent (countActions, unfoldedVariables);
        List<String> immActions = new ArrayList<String> (immediateActions);
        // assumption - immediate actions do not contain the timed action
        immActions.remove (action);
        for (String immAction : immActions)
        {
            if (countActions.contains (immAction))
            {
                event.getIncreasing ().add (new GPEPAActionCount (immAction));
            }
        }
        return event;
    }

    public List<String> getImmediateActions ()
    {
        return immediateActions;
    }
}
