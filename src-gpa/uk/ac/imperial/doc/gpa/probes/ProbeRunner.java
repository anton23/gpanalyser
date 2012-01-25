package uk.ac.imperial.doc.gpa.probes;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import uk.ac.imperial.doc.gpa.fsm.ITransition;
import uk.ac.imperial.doc.gpa.fsm.NFADetectors;
import uk.ac.imperial.doc.gpa.pctmc.GPEPAToPCTMC;
import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponent;
import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponentDefinitions;
import uk.ac.imperial.doc.gpepa.representation.model.GroupedModel;
import uk.ac.imperial.doc.gpepa.states.GPEPAActionCount;
import uk.ac.imperial.doc.gpepa.states.GPEPAState;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.constants.visitors.ExpressionEvaluatorWithConstants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.PlotDescription;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;
import uk.ac.imperial.doc.pctmc.odeanalysis.PCTMCODEAnalysis;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.NumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.ODEAnalysisNumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.representation.State;

import java.util.*;

public class ProbeRunner
{
    public static void executeProbedModel
        (GlobalProbe gprobe, GroupedModel model,
         Collection<GPEPAState> stateObservers,
         Map<String, PEPAComponent> newComponents, Constants constants,
         AbstractExpression stopTime, AbstractExpression stepSize, int density,
         Collection<ITransition> alphabet, Collection<ITransition> excluded)
    {
        Set<ITransition> countActions = NFADetectors.detectAlphabet
            (gprobe.getStartingState(), true, excluded);
        countActions.addAll (alphabet);
        Set<String> countActionStrings = convertObjectsToStrings (countActions);
        List<AbstractExpression> statesCountExpressions
            = new LinkedList<AbstractExpression> ();
        Map<String, AbstractExpression> mapping
            = new HashMap<String, AbstractExpression> ();
        NumericalPostprocessor postprocessor = runTheProbedSystem
            (model, countActionStrings, stateObservers, statesCountExpressions,
            mapping, newComponents, constants, stopTime, stepSize, density);
        double[][] data = postprocessor.evaluateExpressions
            (statesCountExpressions, constants);
        double[] actionsExecuted = Arrays.copyOf (data[0], data[0].length);
    
        // observing wih global probe
        int i = 0;
        while (i < data.length)
        {
            Set<ITransition> availableTransitions
                = gprobe.getAvailableTransitions ();
            for (ITransition transition : availableTransitions)
            {
                int index = statesCountExpressions.indexOf
                    (mapping.get (transition.toString ()));
                if (Math.floor (actionsExecuted [index])
                    < Math.floor (data[i][index]))
                {
                    actionsExecuted [index] = data[i][index];
                    System.out.println ("gprobe step " + i + ": \n");
                    gprobe.advanceWithTransition (transition,
                        statesCountExpressions, mapping, data[i]);
                }
            }
            ++i;
        }
    }

    private static Set<String> convertObjectsToStrings (Set<?> objects)
    {
        Set<String> objectStrings = new HashSet<String> ();
        for (Object object : objects)
        {
            objectStrings.add (object.toString ());
        }
        return objectStrings;
    }

    private static NumericalPostprocessor runTheProbedSystem
        (GroupedModel model, Set<String> countActions,
         Collection<GPEPAState> stateObservers,
         List<AbstractExpression> statesCountExpressions,
         Map<String, AbstractExpression> stateCombPopMapping,
         Map<String, PEPAComponent> newComponents, Constants constants,
         AbstractExpression stopTime, AbstractExpression stepSize, int density)
    {
        PCTMC pctmc = GPEPAToPCTMC.getPCTMC
            (new PEPAComponentDefinitions (newComponents)
                .removeVanishingStates (), model, countActions);
        List<CombinedPopulationProduct> moments
                = new ArrayList<CombinedPopulationProduct> ();
        for (GPEPAState state : stateObservers)
        {
            Multiset<State> states = HashMultiset.create ();
            states.add (state);
            CombinedPopulationProduct combinedActions
                = new CombinedPopulationProduct
                    (new PopulationProduct (states));
            moments.add (combinedActions);
            AbstractExpression combPop
                = CombinedProductExpression.create (combinedActions);
            statesCountExpressions.add (combPop);
            stateCombPopMapping.put (state.toString (), combPop);
        }
        System.out.println (pctmc);
        Map<String, Object> parameters = new HashMap<String, Object> ();
        AbstractPCTMCAnalysis analysis
            = new PCTMCODEAnalysis (pctmc, parameters);
        for (String action : countActions)
        {
            Multiset<State> cooperationActions = HashMultiset.create ();
            GPEPAActionCount gpepaAction = new GPEPAActionCount (action);
            cooperationActions.add (gpepaAction);
            CombinedPopulationProduct combinedActions
                = new CombinedPopulationProduct
                    (new PopulationProduct (cooperationActions));
            moments.add (combinedActions);
            AbstractExpression combPop
                = CombinedProductExpression.create (combinedActions);
            statesCountExpressions.add (combPop);
            stateCombPopMapping.put (action, combPop);
        }
        List<PlotDescription> plotDescriptions
            = new LinkedList<PlotDescription> ();
        plotDescriptions.add (new PlotDescription (statesCountExpressions));
        analysis.setUsedMoments (moments);
        analysis.prepare (constants);
        ExpressionEvaluatorWithConstants stopEval
            = new ExpressionEvaluatorWithConstants (constants);
        stopTime.accept (stopEval);
        ExpressionEvaluatorWithConstants stepEval
            = new ExpressionEvaluatorWithConstants (constants);
        stepSize.accept (stepEval);
        NumericalPostprocessor postprocessor
            = new ODEAnalysisNumericalPostprocessor
            (stopEval.getResult (), stepEval.getResult (), density);
        analysis.addPostprocessor (postprocessor);
        analysis.notifyPostprocessors (constants, plotDescriptions);
        return postprocessor;
    }
}
