package uk.ac.imperial.doc.igpepa.representation.components;

import com.rits.cloning.Cloner;
import uk.ac.imperial.doc.gpepa.representation.components.*;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;

import java.util.*;

public class iPEPAComponentDefinitions extends PEPAComponentDefinitions
{
    private static Cloner deepcloner = new Cloner ();

    public iPEPAComponentDefinitions (Map<String, PEPAComponent> definitions)
    {
		super (definitions);
	}

    // assumption: action with same name not used both as timed and immediate
    public AbstractExpression getApparentRateExpression
        (final String action, PEPAComponent from)
    {
        List<AbstractExpression> summands = new LinkedList<AbstractExpression> ();
        for (AbstractPrefix p : from.getPrefixes (this))
        {
            if (p.getAction ().equals (action)
                || ((iPEPAPrefix) p).getImmediates ().contains (action))
            {
                summands.add (p.getRate ());
            }
        }

        if (summands.size () == 0)
        {
            return DoubleExpression.ZERO;
        }
        return SumExpression.create (summands);
    }

    // assumption: action with same name not used both as timed and immediate
    public RateWeightPair getApparentRateWeightExpressions
        (final String action, PEPAComponent from)
    {
		List<AbstractExpression> summandsRates
            = new LinkedList<AbstractExpression> ();
        List<AbstractExpression> summandsWeights
            = new LinkedList<AbstractExpression>();
		for (AbstractPrefix p : from.getPrefixes (this))
        {
            if (p.getAction().equals(action)
                || ((iPEPAPrefix) p).getImmediates().contains(action))
            {
				summandsRates.add (p.getRate ());
                summandsWeights.add (p.getWeight ());
			}
		}

		if (summandsRates.size() == 0) {
			return new RateWeightPair
                    (DoubleExpression.ZERO, DoubleExpression.ZERO);
		}
		return new RateWeightPair
                (SumExpression.create(summandsRates),
                        SumExpression.create(summandsWeights));
	}

    // supports only one immediate action per choice
	public iPEPAComponentDefinitions removeVanishingStates
            (Set<PEPAComponent> initialComponent)
    {
        iPEPAComponentDefinitions newDefinitions = deepcloner.deepClone (this);
        Map<iChoice, String> choices = getChoiceComponents (newDefinitions);
        Map<PEPAComponent, ImmediatePrefix> imms
            = getImmediatesMap (newDefinitions);
        List<String> choicesToRemove = new LinkedList<String> ();

        // lets check all choices for immediate actions
        for (iChoice choice : choices.keySet ())
        {
            ImmediatePrefix imm = choice.getImmediate ();
            if (imm != null)
            {
                PEPAComponent shorthand = newDefinitions.getShorthand (choice);
                choicesToRemove.add (shorthand.toString ());
                boolean predecessorExists = false;

                if (!initialComponent.contains (shorthand))
                {
                    // we need to find all predecessors of this choice
                    for (Choice otherChoice : choices.keySet ())
                    {
                        List<AbstractPrefix> prefixes
                            = otherChoice.getChoices ();

                        for (AbstractPrefix prefix : prefixes)
                        {
                            if (prefix.getContinuation ().equals (shorthand))
                            {
                                predecessorExists = predecessorExists ||
                                        !(shorthand.equals (newDefinitions
                                                .getShorthand (otherChoice)));

                                // refreshing hash - 1
                                String name = newDefinitions
                                    .getInverseDefinitionsRaw ()
                                    .remove(otherChoice);

                                List<ImmediatePrefix> immediates
                                    = new LinkedList<ImmediatePrefix> ();
                                PEPAComponent newCont = getImmediatesList
                                    (prefix.getContinuation (),
                                        imms, immediates);
                                ((iPEPAPrefix) prefix).addImmediates
                                    (immediates);
                                prefix.setContinuation (newCont);

                                // refreshing hash - 2
                                newDefinitions.getInverseDefinitionsRaw ()
                                        .put(otherChoice, name);
                            }
                        }
                    }
                }

                // if this components has no predecessor,
                // it might be the first one and we will cheat - make its
                // signal go immediately
                if (!predecessorExists && initialComponent.contains (shorthand))
                {
                    choicesToRemove.remove (shorthand.toString ());
                    choice.getChoices ().remove (imm);
                    choice.getChoices ().add (new iPrefix (imm.getAction (),
                        new DoubleExpression (25.0), null,
                        imm.getContinuation (), imm.getImmediatesRawCopy ()));
                }
            }
        }

        Map<String, PEPAComponent> defMap = newDefinitions.getDefinitions ();
        for (String name : choicesToRemove)
        {
            defMap.remove (name);
        }

        return new iPEPAComponentDefinitions (newDefinitions.getDefinitions ());
    }

    // mapping between choices and their components names
    private static Map<iChoice, String>
        getChoiceComponents(PEPAComponentDefinitions definitions)
    {
        Map<iChoice, String> choices = new HashMap<iChoice, String> ();

        for (String name : definitions.getDefinitions ().keySet ())
        {
            PEPAComponent comp = definitions.getComponentDefinition (name);
            if (comp instanceof iChoice)
            {
                choices.put ((iChoice) comp, name);
            }
            else if (comp instanceof Choice)
            {
                throw new Error ("Ordinary Choice components in"
                    + " iPEPAComponentDefinitions.");
            }
        }

        return choices;
    }

    // mapping between choices and their immediate actions (only one)
    private static Map<PEPAComponent, ImmediatePrefix>
        getImmediatesMap (PEPAComponentDefinitions definitions)
    {
        Map<iChoice, String> choices = getChoiceComponents (definitions);
        Map<PEPAComponent, ImmediatePrefix> result
            = new HashMap<PEPAComponent, ImmediatePrefix> ();

        for (iChoice choice : choices.keySet ())
        {
            ImmediatePrefix imm = choice.getImmediate ();
            if (imm != null)
            {
                result.put (new ComponentId (choices.get(choice)), imm);
            }
        }

        return result;
    }

    // recursively generates the list of ensuing immediate actions
    private static PEPAComponent getImmediatesList (PEPAComponent cont,
            Map<PEPAComponent, ImmediatePrefix> imms,
            List<ImmediatePrefix> immediates)
    {
        ImmediatePrefix prefix = imms.get (cont);
        if (prefix == null || immediates.contains (prefix))
        {
            return cont;
        }
        PEPAComponent newCont = prefix.getContinuation ();
        immediates.add (prefix);
        immediates.addAll (prefix.getImmediatesRaw ());
        newCont = getImmediatesList (newCont, imms, immediates);

        return newCont;
    }
}
