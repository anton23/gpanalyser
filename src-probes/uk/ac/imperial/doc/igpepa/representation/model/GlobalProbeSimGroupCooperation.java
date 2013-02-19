package uk.ac.imperial.doc.igpepa.representation.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponentDefinitions;
import uk.ac.imperial.doc.gpepa.representation.group.GroupComponentPair;
import uk.ac.imperial.doc.gpepa.representation.model.GroupCooperation;
import uk.ac.imperial.doc.gpepa.representation.model.GroupedModel;
import uk.ac.imperial.doc.gpepa.representation.model.PEPAEvolutionEvent;
import uk.ac.imperial.doc.gpepa.states.GPEPAState;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;


public class GlobalProbeSimGroupCooperation extends GroupCooperation
{
    public GlobalProbeSimGroupCooperation
        (GroupedModel left, GroupedModel right,
         Set<String> actions)
    {
        super (left, right, actions);
    }

    @Override
    public List<PEPAEvolutionEvent> getEvolutionEvents
        (final PEPAComponentDefinitions definitions,
         Set<String> restrictedActions)
    {
        List<PEPAEvolutionEvent> events = new LinkedList<PEPAEvolutionEvent> ();
        Set<String> newRestrictedActions = new HashSet<String> ();
        newRestrictedActions.addAll (restrictedActions);
        newRestrictedActions.addAll (actions);
        // events without cooperation between left and right
        events.addAll (left.getEvolutionEvents
                (definitions, newRestrictedActions));
        events.addAll (right.getEvolutionEvents
                (definitions, newRestrictedActions));
        // events from cooperation between left and right
        List<PEPAEvolutionEvent> leftEvents = left.getEvolutionEvents
                (definitions, restrictedActions);
        List<PEPAEvolutionEvent> rightEvents = right.getEvolutionEvents
                (definitions, restrictedActions);
        Multimap<String, PEPAEvolutionEvent> leftActionmap = LinkedHashMultimap
                . create ();
        Multimap<String, PEPAEvolutionEvent> rightActionmap = LinkedHashMultimap
                . create ();

        for (PEPAEvolutionEvent le : leftEvents)
        {
            if (actions.contains (le.getAction ()))
            {
                leftActionmap.put (le.getAction (), le);
            }
            for (String immediateAction
                    : ((iPEPAEvolutionEvent) le).getImmediateActions ())
            {
                if (actions.contains (immediateAction))
                {
                    leftActionmap.put (immediateAction, le);
                }
            }
        }

        for (PEPAEvolutionEvent re : rightEvents)
        {
            rightActionmap.put (re.getAction (), re);
        }

        for (final String action : actions)
        {
            for (final PEPAEvolutionEvent le : leftActionmap.get (action))
            {
                for (final PEPAEvolutionEvent re : rightActionmap.get (action))
                {
                    List<GroupComponentPair> increases
                        = new LinkedList<GroupComponentPair> ();
                    List<GroupComponentPair> decreases
                        = new LinkedList<GroupComponentPair> ();
                    increases.addAll (le.getIncreases ());
                    increases.addAll (re.getIncreases ());
                    decreases.addAll (le.getDecreases ());
                    decreases.addAll (re.getDecreases ());

                    List<String> immediateActions
                        = new LinkedList<String> () ;
                    immediateActions.addAll (((iPEPAEvolutionEvent) le)
                            .getImmediateActions ());
                    if (!le.getAction ().equals (action))
                    {
                        immediateActions.remove (action);
                    }

                    List<AbstractExpression> decreasingStates
                        = new ArrayList<AbstractExpression> ();
                    for (GroupComponentPair gp : re.getDecreases ())
                    {
                        decreasingStates.add (CombinedProductExpression
                                .createMeanExpression (new GPEPAState (gp)));
                    }
                    decreasingStates.add (le.getRate ());
                    AbstractExpression rate
                        = ProductExpression.create (decreasingStates);

                    events.add (new iPEPAEvolutionEvent
                            (action, immediateActions, rate,
                                increases, decreases));
                }
            }
        }

        return events;
    }
}
