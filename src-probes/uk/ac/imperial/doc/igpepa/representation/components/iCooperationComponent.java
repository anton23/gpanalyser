package uk.ac.imperial.doc.igpepa.representation.components;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import uk.ac.imperial.doc.gpepa.representation.components.AbstractPrefix;
import uk.ac.imperial.doc.gpepa.representation.components.CooperationComponent;
import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponent;
import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponentDefinitions;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class iCooperationComponent extends CooperationComponent
{
    public iCooperationComponent (PEPAComponent left, PEPAComponent right,
                                  Set<String> cooperationSet)
    {
        super (left, right, cooperationSet);
    }

    public iCooperationComponent (PEPAComponent left, PEPAComponent right)
    {
        super (left, right);
    }

    @Override
	public List<AbstractPrefix> getPrefixes
        (PEPAComponentDefinitions definitions)
    {
        if (!(definitions instanceof iPEPAComponentDefinitions))
        {
            throw new Error ("iCooperationComponent used with"
                + " incompatible definitions.");
        }
        unfoldImplicitCooperations(definitions, new HashSet<PEPAComponent> ());
        List<AbstractPrefix> leftPrefixes = left.getPrefixes (definitions);
		List<AbstractPrefix> rightPrefixes = right.getPrefixes (definitions);
		List<AbstractPrefix> ret = new LinkedList<AbstractPrefix> ();

		Multimap<String, AbstractPrefix> leftActionmap = LinkedHashMultimap
				.create ();

		Multimap<String, AbstractPrefix> rightActionmap = LinkedHashMultimap
				.create ();

		// only left evolves
		for (AbstractPrefix leftPrefix : leftPrefixes)
        {
            boolean immediatesCleared = true;
            for (String action : ((iPEPAPrefix)leftPrefix).getImmediates ())
            {
                if (cooperationSet.contains (action))
                {
                    leftActionmap.put (action, leftPrefix);
                    immediatesCleared = false;
                }
            }
            if (immediatesCleared &&
                    !cooperationSet.contains (leftPrefix.getAction ()))
            {
                PEPAComponent newContinuation = definitions
                    .getShorthand(new iCooperationComponent (leftPrefix
                            .getContinuation (), right, cooperationSet));
                try
                {
                    ret.add (leftPrefix.getClass ().getDeclaredConstructor
                        (String.class, AbstractExpression.class,
                                AbstractExpression.class,
                                PEPAComponent.class, List.class)
                        .newInstance(leftPrefix.getAction (),
                                leftPrefix.getRate (),
                                leftPrefix.getWeight (), newContinuation,
                                ((iPEPAPrefix) leftPrefix)
                                    .getImmediatesRawCopy ()));
                }
                catch (Exception e)
                {
                    e.printStackTrace ();
                }
            }
            else
            {
                leftActionmap.put (leftPrefix.getAction (), leftPrefix);
            }
		}
		// only right evolves
		for (AbstractPrefix rightPrefix : rightPrefixes)
        {
            boolean immediatesCleared = true;
            for (String action : ((iPEPAPrefix) rightPrefix).getImmediates ())
            {
                if (cooperationSet.contains (action))
                {
                    rightActionmap.put (action, rightPrefix);
                    immediatesCleared = false;
                }
            }
            if (immediatesCleared
                    && !cooperationSet.contains (rightPrefix.getAction ()))
            {
                PEPAComponent newContinuation = definitions
                    .getShorthand (new iCooperationComponent (left,
                            rightPrefix.getContinuation (), cooperationSet));
                try
                {
                    ret.add (rightPrefix.getClass ().getDeclaredConstructor
                            (String.class, AbstractExpression.class,
                                    AbstractExpression.class,
                                    PEPAComponent.class, List.class)
                            .newInstance (rightPrefix.getAction (),
                                    rightPrefix.getRate (),
                                    rightPrefix.getWeight (), newContinuation,
                                    ((iPEPAPrefix) rightPrefix)
                                        .getImmediatesRawCopy ()));
                }
                catch (Exception e)
                {
                    e.printStackTrace ();
                }
            }
            else
            {
                rightActionmap.put (rightPrefix.getAction (), rightPrefix);
            }
		}

        Set<String> cooperationActions = new HashSet<String> ();
        cooperationActions.addAll(leftActionmap.keySet ());
        cooperationActions.addAll(rightActionmap.keySet ());

		// both evolve
		for (String action : cooperationActions)
        {
			for (AbstractPrefix leftPrefix : leftActionmap.get (action))
            {
				for (AbstractPrefix rightPrefix : rightActionmap.get (action))
                {
					PEPAComponent newContinuation = definitions
                        .getShorthand (new iCooperationComponent (leftPrefix
                                .getContinuation (), rightPrefix
                                .getContinuation (), cooperationSet));
                    PEPAComponentDefinitions.RateWeightPair leftRateWeight
                        = definitions.getApparentRateWeightExpressions
                            (leftPrefix.getAction (), left);
                    PEPAComponentDefinitions.RateWeightPair rightRateWeight
                        = definitions.getApparentRateWeightExpressions
                            (rightPrefix.getAction (), right);
                    AbstractPrefix newPrefix = leftPrefix.getCooperation
                        (action, rightPrefix, rightRateWeight.getRate (),
                            rightRateWeight.getWeight (),
                            leftRateWeight.getRate (),
                            leftRateWeight.getWeight (), newContinuation);
                    if (newPrefix != null)
                    {
                        ret.add (newPrefix);
                    }
				}
			}
		}

		return ret;
	}
}
