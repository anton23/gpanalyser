package uk.ac.imperial.doc.igpepa.representation.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import uk.ac.imperial.doc.gpepa.representation.components.AbstractPrefix;
import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponent;
import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponentDefinitions;
import uk.ac.imperial.doc.gpepa.representation.components.Stop;
import uk.ac.imperial.doc.gpepa.representation.group.Group;
import uk.ac.imperial.doc.gpepa.representation.group.GroupComponentPair;
import uk.ac.imperial.doc.gpepa.representation.model.LabelledComponentGroup;
import uk.ac.imperial.doc.gpepa.representation.model.PEPAEvolutionEvent;
import uk.ac.imperial.doc.gpepa.states.GPEPAState;
import uk.ac.imperial.doc.igpepa.representation.components.iPEPAPrefix;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;

public class iPEPALabelledComponentGroup extends LabelledComponentGroup
{
    public iPEPALabelledComponentGroup(String label, Group group)
    {
        super (label, group);
    }

	@Override
	public List<PEPAEvolutionEvent> getEvolutionEvents
        (final PEPAComponentDefinitions definitions,
         Set<String> restrictedActions)
    {
		List<PEPAEvolutionEvent> events = new LinkedList<PEPAEvolutionEvent> ();
		for (final PEPAComponent derivative : group
				.getComponentDerivatives (definitions))
        {
			for (final AbstractPrefix prefix
                    : derivative.getPrefixes (definitions))
            {
                boolean restricted = false;
                for (String immediateAction
                        : ((iPEPAPrefix)prefix).getImmediates ())
                {
                    restricted = restrictedActions.contains (immediateAction);
                    if (restricted)
                    {
                        break;
                    }
                }
				if (!restricted && !restrictedActions
                        .contains(prefix.getAction ()))
                {
					AbstractExpression rate
                        = ProductExpression.create (CombinedProductExpression
                            .createMeanExpression (new GPEPAState
                                (new GroupComponentPair (label, derivative))),
                            prefix.getRate ());

					List<GroupComponentPair> increases
                        = new LinkedList<GroupComponentPair> ();
					PEPAComponent continuation = prefix.getContinuation ();
					if (!(continuation instanceof Stop))
						increases.add(new GroupComponentPair (label,
								continuation));
					List<GroupComponentPair> decreases
                        = new LinkedList<GroupComponentPair> ();
					decreases.add(new GroupComponentPair (label, derivative));
					events.add (new iPEPAEvolutionEvent
                            (prefix.getAction (),
                             ((iPEPAPrefix) prefix).getImmediates (),
                             rate, increases, decreases));
				}
			}
		}
		return events;
	}
}
