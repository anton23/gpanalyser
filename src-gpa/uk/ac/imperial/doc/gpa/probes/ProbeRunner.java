package uk.ac.imperial.doc.gpa.probes;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import uk.ac.imperial.doc.gpa.fsm.ITransition;
import uk.ac.imperial.doc.gpa.fsm.NFADetectors;
import uk.ac.imperial.doc.gpa.pctmc.GPEPAToPCTMC;
import uk.ac.imperial.doc.gpepa.representation.components.AbstractPrefix;
import uk.ac.imperial.doc.gpepa.representation.components.ComponentId;
import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponent;
import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponentDefinitions;
import uk.ac.imperial.doc.gpepa.representation.group.Group;
import uk.ac.imperial.doc.gpepa.representation.group.GroupComponentPair;
import uk.ac.imperial.doc.gpepa.representation.model.GroupedModel;
import uk.ac.imperial.doc.gpepa.representation.model.LabelledComponentGroup;
import uk.ac.imperial.doc.gpepa.states.GPEPAActionCount;
import uk.ac.imperial.doc.gpepa.states.GPEPAState;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.constants.visitors.ExpressionEvaluatorWithConstants;
import uk.ac.imperial.doc.jexpressions.expressions.*;
import uk.ac.imperial.doc.jexpressions.javaoutput.statements.AbstractExpressionEvaluator;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
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
        CDF executeProbedModel
            (GlobalProbe gprobe, GroupedModel model,
             Set<GPEPAState> stateObservers,
             PEPAComponentDefinitions mainDef, PEPAComponentDefinitions altDef,
             Map<PEPAComponentDefinitions, Set<ComponentId>> definitionsMap,
             ComponentId accepting, Constants constants, Map<ExpressionVariable,
             AbstractExpression> unfoldedVariables,
             AbstractExpression stopTime, AbstractExpression stepSize,
             int parameter, Class<A> AClass, Class<NP> NPClass,
             Collection<ITransition> alphabet, Collection<ITransition> excluded,
             int mode, boolean plot)
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
        double stopTimeVal = stopEval.getResult ();
        ExpressionEvaluatorWithConstants stepEval
                = new ExpressionEvaluatorWithConstants (constants);
        stepSize.accept (stepEval);
        double stepSizeVal = stepEval.getResult();

        CDF cdf = dispatchEvaluation (gprobe,
                statesCountExpressions, mapping, countActionStrings, model,
                stateObservers, mainDef, altDef, definitionsMap, accepting,
                constants, unfoldedVariables,
                stopTimeVal, stepSizeVal, parameter,
                AClass, NPClass, 0, mode);

        if (plot)
        {
            plotGraph (gprobe.getName (), cdf, stepSizeVal);
        }

        return cdf;
    }

    private
        <A extends AbstractPCTMCAnalysis, NP extends NumericalPostprocessor>
        CDF dispatchEvaluation
            (GlobalProbe gprobe,
             List<AbstractExpression> statesCountExpressions,
             Map<String, AbstractExpression> mapping,
             Set<String> countActionStrings,
             GroupedModel model, Set<GPEPAState> stateObservers,
             PEPAComponentDefinitions mainDef, PEPAComponentDefinitions altDef,
             Map<PEPAComponentDefinitions, Set<ComponentId>> definitionsMap,
             ComponentId accepting, Constants constants,
             Map<ExpressionVariable, AbstractExpression> unfoldedVariables,
             double stopTime, double stepSize, int parameter,
             Class<A> AClass, Class<NP> NPClass,
             int start_time, int mode)
    {
        switch (mode)
        {
            case 1:
                return steadyIndividual
                    (statesCountExpressions, mapping,
                        countActionStrings, model, stateObservers,
                        mainDef, altDef, definitionsMap, accepting,
                            constants, unfoldedVariables, stopTime, stepSize,
                        parameter, AClass, NPClass);
            case 2:
                return transientIndividual
                    (gprobe, statesCountExpressions, mapping,
                        countActionStrings, model, stateObservers,
                        mainDef, definitionsMap, accepting,
                        constants, unfoldedVariables, stopTime, stepSize,
                        parameter, AClass, NPClass, start_time);
            case 3:
                return globalPassages
                    (gprobe, statesCountExpressions, mapping,
                        countActionStrings, model, stateObservers,
                        mainDef, definitionsMap,
                        constants, unfoldedVariables, stopTime, stepSize,
                        parameter, AClass, NPClass, start_time);
            default:
                return null;
        }
    }

    private
        <A extends AbstractPCTMCAnalysis, NP extends NumericalPostprocessor>
        CDF steadyIndividual
            (List<AbstractExpression> statesCountExpressions,
             Map<String, AbstractExpression> mapping,
             Set<String> countActionStrings,
             GroupedModel model, Set<GPEPAState> stateObservers,
             PEPAComponentDefinitions mainDef, PEPAComponentDefinitions altDef,
             Map<PEPAComponentDefinitions, Set<ComponentId>> definitionsMap,
             ComponentId accepting, Constants constants,
             Map<ExpressionVariable, AbstractExpression> unfoldedVariables,
             double stopTime, double stepSize, int parameter,
             Class<A> AClass, Class<NP> NPClass)
    {
        // obtaining ratios for steady state component distribution
        LinkedHashMap<GroupComponentPair, AbstractExpression> crates
            = getProbabilitiesAfterBegin (model, mainDef);
        Map<String, LabelledComponentGroup> lgs = model.getComponentGroups ();

        // obtaining steady-state probabilities (user set stop-time)
        List<AbstractExpression> expressions
            = new LinkedList<AbstractExpression> (crates.values ());
        NumericalPostprocessor postprocessor = runTheProbedSystem
            (model, countActionStrings, stateObservers, statesCountExpressions,
                mapping, mainDef, constants, unfoldedVariables,
                stopTime, stepSize, parameter, AClass, NPClass);

        AbstractExpressionEvaluator eval
            = postprocessor.getExpressionEvaluator (expressions, constants);
        double[] times = new double[expressions.size ()];
        double maxTime = stopTime * stepSize - stepSize;
        for (int i = 0; i < times.length; ++i)
        {
            times[i] = maxTime;
        }
        double[] val = postprocessor.evaluateExpressionsAtTimes
            (eval, times, constants);
        double[][] steadyval = postprocessor.evaluateExpressions
            (statesCountExpressions, constants);

        int i = 0;
        for (GroupComponentPair gc : crates.keySet ())
        {
            boolean containsComp = false;
            for (ComponentId comp : definitionsMap.get (mainDef))
            {
                if (gc.getComponent ().containsComponent (comp))
                {
                    containsComp = true;
                    break;
                }
            }

            if (containsComp)
            {
                crates.put (gc, new DoubleExpression (val[i]));
            }
            else
            {
                crates.put (gc, new DoubleExpression
                    (steadyval[(int)(stopTime * stepSize - stepSize)]
                        [statesCountExpressions.indexOf
                            (mapping.get (gc.toString ()))]));
            }
            ++i;
        }

        // setting initial number of components for next analysis
        for (LabelledComponentGroup lg : lgs.values ())
        {
            Group g = lg.getGroup ();
            for (PEPAComponent c : g.getComponentDerivatives (mainDef))
            {
                g.setCountExpression (c, crates.get
                        (new GroupComponentPair (lg.getLabel (), c)));
            }
        }
        statesCountExpressions = new LinkedList<AbstractExpression> ();
        mapping = new HashMap<String, AbstractExpression> ();
        stateObservers = new HashSet<GPEPAState> ();
        Set<GroupComponentPair> pairs = model.getGroupComponentPairs (altDef);
        for (GroupComponentPair g : pairs)
        {
            stateObservers.add (new GPEPAState (g));
        }
        postprocessor = runTheProbedSystem (model, countActionStrings,
                stateObservers, statesCountExpressions, mapping, altDef,
                constants, unfoldedVariables, stopTime, stepSize, parameter,
                AClass, NPClass);

        double[][] obtainedMeasurements = postprocessor.evaluateExpressions
            (statesCountExpressions, constants);
        double[] cdf = new double[obtainedMeasurements.length];

        for (int j = 0; j < obtainedMeasurements.length; j++)
        {
            for (GroupComponentPair gp : pairs)
            {
                if (gp.getComponent ().containsComponent (accepting))
                {
                    cdf[j] += obtainedMeasurements[j]
                        [statesCountExpressions.indexOf
                            (mapping.get (gp.toString ()))];
                }
            }
        }

        return new CDF (cdf);
    }

    private
    <A extends AbstractPCTMCAnalysis, NP extends NumericalPostprocessor>
    CDF transientIndividual
            (GlobalProbe gprobe,
             List<AbstractExpression> statesCountExpressions,
             Map<String, AbstractExpression> mapping,
             Set<String> countActionStrings,
             GroupedModel model, Set<GPEPAState> stateObservers,
             PEPAComponentDefinitions mainDef,
             Map<PEPAComponentDefinitions, Set<ComponentId>> definitionsMap,
             ComponentId accepting, Constants constants,
             Map<ExpressionVariable, AbstractExpression> unfoldedVariables,
             double stopTime, double stepSize, int parameter,
             Class<A> AClass, Class<NP> NPClass,
             int start_time)
    {
        return new CDF (null);
    }

    private
    <A extends AbstractPCTMCAnalysis, NP extends NumericalPostprocessor>
    CDF globalPassages
            (GlobalProbe gprobe,
             List<AbstractExpression> statesCountExpressions,
             Map<String, AbstractExpression> mapping,
             Set<String> countActionStrings,
             GroupedModel model, Set<GPEPAState> stateObservers,
             PEPAComponentDefinitions mainDef,
             Map<PEPAComponentDefinitions, Set<ComponentId>> definitionsMap,
             Constants constants,
             Map<ExpressionVariable, AbstractExpression> unfoldedVariables,
             double stopTime, double stepSize, int parameter,
             Class<A> AClass, Class<NP> NPClass,
             int start_time)
    {
        return new CDF (null);
    }
/*
    private int runGlobalProbe
            (double[][] data, GlobalProbe gprobe,
             List<AbstractExpression> statesCountExpressions,
             Map<String, AbstractExpression> mapping, int start_time,
             boolean repeating)
    {
        double[] actionsExecuted = Arrays.copyOf (data[0], data[0].length);
        Collection<ProbeTime> measuredTimes = new ArrayList<ProbeTime> ();
        double tempStart = -1;

        // observing wih global probe
        int i = start_time;
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
                            if (!repeating)
                            {
                                break;
                            }
                        }
                    }
                }
            }
            ++i;
        }

        return i;
    }
*/
    private LinkedHashMap<GroupComponentPair, AbstractExpression>
        getProbabilitiesAfterBegin
            (GroupedModel model, PEPAComponentDefinitions definitions)
    {
        LinkedHashMap<GroupComponentPair, AbstractExpression> result
            = new LinkedHashMap<GroupComponentPair, AbstractExpression> ();
        Set<GroupComponentPair> pairs
            = model.getGroupComponentPairs (definitions);

        // total expected rate of all transitions firing begin signals
        Collection<AbstractExpression> summands
            = new ArrayList<AbstractExpression> ();
        for (GroupComponentPair hc : pairs)
        {
            Collection<String> actions = hc.getComponent ()
                .getActions (definitions);
            Collection<AbstractPrefix> prefices
                = hc.getComponent ().getPrefixes (definitions);
            for (String action : actions)
            {
                for (AbstractPrefix prefix : prefices)
                {
                    if (prefix.getAction ().equals (action)
                        && prefix.getImmediates ().contains ("begin"))
                    {
                        AbstractExpression arate
                            = definitions.getApparentRateExpression
                                (action, hc.getComponent());
                        AbstractExpression crate
                            = model.getComponentRateExpression
                                (action, definitions, hc);
                        summands.add (DivExpression.create (crate, arate));
                    }
                }
            }
        }
        AbstractExpression totalBeginRate = SumExpression.create (summands);

        // probability for each Q
        for (GroupComponentPair q : pairs)
        {
            summands = new ArrayList<AbstractExpression> ();
            for (GroupComponentPair hc : pairs)
            {
                Collection<String> actions
                    = hc.getComponent ().getActions (definitions);
                Collection<AbstractPrefix> prefices
                        = hc.getComponent ().getPrefixes (definitions);
                for (String action : actions)
                {
                    for (AbstractPrefix prefix : prefices)
                    {
                        if (prefix.getAction ().equals (action)
                                && prefix.getContinuation ()
                                    .equals (q.getComponent ())
                                && prefix.getImmediates ().contains ("begin"))
                        {
                            AbstractExpression arate
                                = definitions.getApparentRateExpression
                                    (action, hc.getComponent ());
                            AbstractExpression crate
                                = model.getComponentRateExpression
                                    (action, definitions, hc);
                            summands.add (DivExpression.create (crate, arate));
                        }
                    }
                }
            }
            result.put (q, DivExpression.create
                    (SumExpression.create (summands), totalBeginRate));
        }

        return result;
    }
    private void plotGraph (String name, CDF cdf, double stepSize)
    {
        if (graph == null)
        {
            graph = new ProbeGraph ();
        }
        graph.renderData (cdf, name, stepSize);
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
             PEPAComponentDefinitions definitions, Constants constants,
             Map<ExpressionVariable, AbstractExpression> unfoldedVariables,
             double stopTime, double stepSize, int parameter,
             Class<A> AClass, Class<NP> NPClass)
    {
        List<CombinedPopulationProduct> moments
                = new ArrayList<CombinedPopulationProduct> ();
        for (GPEPAState state : stateObservers)
        {
            Multiset<State> states = HashMultiset.create ();
            setStateObserver (states, state, state.toString (), moments,
                statesCountExpressions, stateCombPopMapping);
        }
        for (String action : countActions)
        {
            GPEPAActionCount gpepaAction = new GPEPAActionCount (action);
            Multiset<State> cooperationActions = HashMultiset.create ();
            setStateObserver (cooperationActions, gpepaAction, action, moments,
                    statesCountExpressions, stateCombPopMapping);
        }

        List<PlotDescription> plotDescriptions
            = new LinkedList<PlotDescription> ();
        plotDescriptions.add (new PlotDescription (new ArrayList<AbstractExpression>(unfoldedVariables.keySet())));

        PCTMC pctmc = GPEPAToPCTMC.getPCTMC (definitions, model, countActions);
        System.out.println (pctmc);

        AbstractPCTMCAnalysis analysis = getAnalysis (pctmc, AClass);
        analysis.setUsedMoments (moments);
        AbstractPCTMCAnalysis.unfoldVariablesAndSetUsedProducts
            (analysis, plotDescriptions, unfoldedVariables);
        NumericalPostprocessor postprocessor
            = getPostprocessor (stopTime, stepSize, parameter, NPClass);
        analysis.addPostprocessor (postprocessor);
        analysis.prepare (constants);
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
        CombinedPopulationProduct combinedPopulation
            = new CombinedPopulationProduct (new PopulationProduct (statesSet));
        moments.add (combinedPopulation);
        AbstractExpression combProd
            = CombinedProductExpression.create (combinedPopulation);
        statesCountExpressions.add (combProd);
        stateCombPopMapping.put (mappingString, combProd);
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
            return AClass.getDeclaredConstructor
                    (PCTMC.class).newInstance (pctmc);
        }
        catch (Exception ex)
        {
            ex.printStackTrace ();
        }
        return null;
    }
}
