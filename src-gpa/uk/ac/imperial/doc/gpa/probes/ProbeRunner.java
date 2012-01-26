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
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.NumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.representation.State;

import java.util.*;

public class ProbeRunner
{
    private ProbeGraph graph;

    public
        <A extends AbstractPCTMCAnalysis, NP extends NumericalPostprocessor>
        Collection<ProbeTime> executeProbedModel
        (GlobalProbe gprobe, GroupedModel model,
         Collection<GPEPAState> stateObservers,
         Map<String, PEPAComponent> newComponents, Constants constants,
         AbstractExpression stopTime, AbstractExpression stepSize,
         int parameter, Class<A> AClass, Class<NP> NPClass,
         Collection<ITransition> alphabet, Collection<ITransition> excluded,
         boolean plot)
    {
        Set<ITransition> countActions = NFADetectors.detectAlphabet
            (gprobe.getStartingState(), true, excluded);
        countActions.addAll (alphabet);
        Set<String> countActionStrings = convertObjectsToStrings (countActions);
        List<AbstractExpression> statesCountExpressions
            = new LinkedList<AbstractExpression> ();
        Map<String, AbstractExpression> mapping
            = new HashMap<String, AbstractExpression> ();
        ExpressionEvaluatorWithConstants stopEval
                = new ExpressionEvaluatorWithConstants (constants);
        stopTime.accept (stopEval);
        ExpressionEvaluatorWithConstants stepEval
                = new ExpressionEvaluatorWithConstants (constants);
        stepSize.accept (stepEval);
        double stepSizeVal = stepEval.getResult ();
        NumericalPostprocessor postprocessor = runTheProbedSystem
            (model, countActionStrings, stateObservers, statesCountExpressions,
            mapping, newComponents, constants,
            stopEval.getResult (), stepSizeVal, parameter, AClass, NPClass);
        double[][] data = postprocessor.evaluateExpressions
            (statesCountExpressions, constants);
        double[] actionsExecuted = Arrays.copyOf (data[0], data[0].length);
        Collection<ProbeTime> measuredTimes = new ArrayList<ProbeTime> ();
        double tempStart = -1;

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
                    ITransition lastExecuted =
                        gprobe.advanceWithTransition (transition,
                            statesCountExpressions, mapping, data[i]);
                    if (lastExecuted != null)
                    {
                        if (lastExecuted.toString ().equals ("start"))
                        {
                            tempStart = i;
                        }
                        else if (lastExecuted.toString ().equals ("stop"))
                        {
                            measuredTimes.add (new ProbeTime (tempStart, i));
                        }
                    }
                }
            }
            ++i;
        }

        if (plot)
        {
            stopTime.accept (stopEval);
            plotGraph (gprobe.getName (), measuredTimes,
                    stopEval.getResult () / stepSizeVal);
        }

        return measuredTimes;
    }

    private void plotGraph
            (String name, Collection<ProbeTime> measuredTimes, double end)
    {
        if (graph == null)
        {
            graph = new ProbeGraph ();
        }
        graph.renderData (measuredTimes, name, end);
    }

    private Set<String> convertObjectsToStrings (Set<?> objects)
    {
        Set<String> objectStrings = new HashSet<String> ();
        for (Object object : objects)
        {
            objectStrings.add (object.toString ());
        }
        return objectStrings;
    }

    private
        <A extends AbstractPCTMCAnalysis, NP extends NumericalPostprocessor>
        NumericalPostprocessor runTheProbedSystem
        (GroupedModel model, Set<String> countActions,
         Collection<GPEPAState> stateObservers,
         List<AbstractExpression> statesCountExpressions,
         Map<String, AbstractExpression> stateCombPopMapping,
         Map<String, PEPAComponent> newComponents, Constants constants,
         double stopTime, double stepSize, int parameter,
         Class<A> AClass, Class<NP> NPClass)
    {
        PCTMC pctmc = GPEPAToPCTMC.getPCTMC
            (new PEPAComponentDefinitions (newComponents)
                .removeVanishingStates (), model, countActions);
        List<CombinedPopulationProduct> moments
                = new ArrayList<CombinedPopulationProduct> ();
        for (GPEPAState state : stateObservers)
        {
            Multiset<State> states = HashMultiset.create ();
            setStateObserver (states, state, state.toString (), moments,
                statesCountExpressions, stateCombPopMapping);
        }
        //System.out.println (pctmc);
        AbstractPCTMCAnalysis analysis
            = getAnalysis (pctmc, AClass);
        for (String action : countActions)
        {
            GPEPAActionCount gpepaAction = new GPEPAActionCount (action);
            Multiset<State> cooperationActions = HashMultiset.create ();
            setStateObserver (cooperationActions, gpepaAction, action, moments,
                    statesCountExpressions, stateCombPopMapping);
        }
        List<PlotDescription> plotDescriptions
            = new LinkedList<PlotDescription> ();
        plotDescriptions.add (new PlotDescription (statesCountExpressions));
        analysis.setUsedMoments (moments);
        analysis.prepare (constants);
        NumericalPostprocessor postprocessor
            = getPostprocessor (stopTime, stepSize, parameter, NPClass);
        analysis.addPostprocessor (postprocessor);
        analysis.notifyPostprocessors (constants, plotDescriptions);
        return postprocessor;
    }

    private void setStateObserver
        (Multiset<State> statesSet, State state, String mappingString,
         List<CombinedPopulationProduct> moments,
         List<AbstractExpression> statesCountExpressions,
         Map<String, AbstractExpression> stateCombPopMapping)
    {
        statesSet.add (state);
        CombinedPopulationProduct combinedActions
            = new CombinedPopulationProduct (new PopulationProduct (statesSet));
        moments.add (combinedActions);
        AbstractExpression combPop
            = CombinedProductExpression.create (combinedActions);
        statesCountExpressions.add (combPop);
        stateCombPopMapping.put (mappingString, combPop);
    }

    private <NP extends NumericalPostprocessor>
        NumericalPostprocessor getPostprocessor
        (double stopTime, double stepSize, int parameter, Class<NP> NPClass)
    {
        try
        {
            return NPClass.getDeclaredConstructor
                (double.class, double.class, int.class).newInstance
                (stopTime, stepSize, parameter);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private <A extends AbstractPCTMCAnalysis>
        AbstractPCTMCAnalysis getAnalysis (PCTMC pctmc, Class<A> AClass)
    {
        try
        {
            // Constructor for ODE
            Map<String, Object> parameters = new HashMap<String, Object> ();
            return AClass.getDeclaredConstructor
                    (PCTMC.class, Map.class).newInstance (pctmc, parameters);
        }
        catch (NoSuchMethodException e)
        {
            try
            {
                // Constructor for Simulation
                return AClass.getDeclaredConstructor
                    (PCTMC.class).newInstance (pctmc);
            }
            catch (Exception ex)
            {
                ex.printStackTrace ();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace ();
        }
        return null;
    }
}
