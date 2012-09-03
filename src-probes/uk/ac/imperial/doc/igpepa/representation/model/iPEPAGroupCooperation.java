package uk.ac.imperial.doc.igpepa.representation.model;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponentDefinitions;
import uk.ac.imperial.doc.gpepa.representation.group.GroupComponentPair;
import uk.ac.imperial.doc.gpepa.representation.model.GroupCooperation;
import uk.ac.imperial.doc.gpepa.representation.model.GroupedModel;
import uk.ac.imperial.doc.gpepa.representation.model.PEPAEvolutionEvent;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivDivMinExpression;

import java.util.*;

public class iPEPAGroupCooperation extends GroupCooperation
{
    public iPEPAGroupCooperation
        (GroupedModel left, GroupedModel right, Set<String> actions)
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
        events.addAll (left.getEvolutionEvents (definitions,
                newRestrictedActions));
        events.addAll (right.getEvolutionEvents (definitions,
                newRestrictedActions));
        // events from cooperation between left and right
        List<PEPAEvolutionEvent> leftEvents = left.getEvolutionEvents
                (definitions, restrictedActions);
        List<PEPAEvolutionEvent> rightEvents = right.getEvolutionEvents
                (definitions, restrictedActions);
        Multimap<String, PEPAEvolutionEvent> leftActionmap = LinkedHashMultimap
                .create ();
        Multimap<String, PEPAEvolutionEvent> rightActionmap = LinkedHashMultimap
                .create ();

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
            if (actions.contains (re.getAction ()))
            {
                rightActionmap.put (re.getAction (), re);
            }
            for (String immediateAction
                    : ((iPEPAEvolutionEvent) re).getImmediateActions ())
            {
                if (actions.contains (immediateAction))
                {
                    rightActionmap.put (immediateAction, re);
                }
            }
        }

        Collection<String> allActions
                = new HashSet<String> (leftActionmap.keySet ());
        allActions.addAll (rightActionmap.keySet ());
        for (final String action : allActions)
        {
            for (final PEPAEvolutionEvent le : leftActionmap.get (action))
            {
                for (final PEPAEvolutionEvent re : rightActionmap.get (action))
                {
                    List<GroupComponentPair> increases = new LinkedList<GroupComponentPair> ();
                    List<GroupComponentPair> decreases = new LinkedList<GroupComponentPair> ();
                    increases.addAll (le.getIncreases ());
                    increases.addAll (re.getIncreases ());
                    decreases.addAll (le.getDecreases ());
                    decreases.addAll (re.getDecreases ());

                    List<String> immediates
                        = new LinkedList<String> ();
                    immediates.addAll (((iPEPAEvolutionEvent) le)
                        .getImmediateActions ());
                    if (!le.getAction ().equals (action))
                    {
                        immediates.remove (action);
                    }
                    immediates.addAll (((iPEPAEvolutionEvent) re)
                        .getImmediateActions ());
                    if (!re.getAction ().equals (action))
                    {
                        immediates.remove (action);
                    }

                    AbstractExpression leftApparentRate = left
                        .getMomentOrientedRateExpression (action,
                            definitions);

                    AbstractExpression rightApparentRate = right
                        .getMomentOrientedRateExpression (action,
                            definitions);
                    AbstractExpression leftRate = le.getRate ();
                    AbstractExpression rightRate = re.getRate ();

                    AbstractExpression rate = DivDivMinExpression.create
                        (leftRate, rightRate, leftApparentRate,
                         rightApparentRate);
                    events.add (new iPEPAEvolutionEvent
                            (action, immediates, rate, increases, decreases));
                }
            }
        }

        return events;
    }
}
