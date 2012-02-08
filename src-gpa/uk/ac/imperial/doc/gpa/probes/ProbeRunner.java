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
             ComponentId accepting, Constants constants,
             AbstractExpression stopTime, AbstractExpression stepSize,
             int parameter, Class<A> AClass, Class<NP> NPClass,
             Collection<ITransition> alphabet, Collection<ITransition> excluded,
             int mode, double modePar, boolean plot)
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
                constants, stopTimeVal, stepSizeVal, parameter,
                AClass, NPClass, 0, mode, modePar);

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
             double stopTime, double stepSize, int parameter,
             Class<A> AClass, Class<NP> NPClass,
             int start_time, int mode, double modePar)
    {
        switch (mode)
        {
            case 1:
                return steadyIndividual
                    (statesCountExpressions, mapping,
                        countActionStrings, model, stateObservers,
                        mainDef, altDef, definitionsMap, accepting,
                        constants, stopTime, stepSize, parameter,
                        AClass, NPClass, modePar);
            case 2:
                return transientIndividual
                    (statesCountExpressions, mapping,
                        countActionStrings, model, stateObservers,
                        mainDef, definitionsMap, accepting,
                        constants, stopTime, stepSize, parameter,
                        AClass, NPClass);
            case 3:
                return globalPassages
                    (gprobe, statesCountExpressions, mapping,
                        countActionStrings, model, stateObservers,
                        mainDef, definitionsMap,
                        constants, stopTime, stepSize, parameter,
                        AClass, NPClass, start_time);
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
             double stopTime, double stepSize, int parameter,
             Class<A> AClass, Class<NP> NPClass, double steadyStateTime)
    {
        NumericalPostprocessor postprocessor = runTheProbedSystem
            (model, countActionStrings, false, stateObservers,
                statesCountExpressions, mapping, mainDef, constants,
                steadyStateTime, stepSize, parameter, AClass, NPClass);
        LinkedHashMap<GroupComponentPair, AbstractExpression> crates
            = new LinkedHashMap<GroupComponentPair, AbstractExpression> ();
        double maxTime = steadyStateTime * stepSize - stepSize;
        double[] val = getStartingStates
            (model,  mainDef, constants, postprocessor, maxTime, crates);
        double[][] steadyval = postprocessor.evaluateExpressions
            (statesCountExpressions, constants);

        assignNewCounts (crates, definitionsMap, mainDef, model,
                statesCountExpressions, mapping, val, steadyval[(int) maxTime]);
        statesCountExpressions = new LinkedList<AbstractExpression> ();
        mapping = new HashMap<String, AbstractExpression> ();
        stateObservers = new HashSet<GPEPAState> ();
        Set<GroupComponentPair> pairs = model.getGroupComponentPairs (altDef);
        for (GroupComponentPair g : pairs)
        {
            stateObservers.add (new GPEPAState (g));
        }
        postprocessor = runTheProbedSystem (model, countActionStrings, false,
                stateObservers, statesCountExpressions, mapping, altDef,
                constants, stopTime, stepSize, parameter,
                AClass, NPClass);

        double[][] obtainedMeasurements = postprocessor.evaluateExpressions
            (statesCountExpressions, constants);
        double[] cdf = new double[obtainedMeasurements.length];

        passageTimeCDF (obtainedMeasurements, pairs, accepting,
                cdf, statesCountExpressions, mapping);

        return new CDF (cdf);
    }

    private
    <A extends AbstractPCTMCAnalysis, NP extends NumericalPostprocessor>
    CDF transientIndividual
            (List<AbstractExpression> statesCountExpressions,
             Map<String, AbstractExpression> mapping,
             Set<String> countActionStrings,
             GroupedModel model, Set<GPEPAState> stateObservers,
             PEPAComponentDefinitions mainDef,
             Map<PEPAComponentDefinitions, Set<ComponentId>> definitionsMap,
             ComponentId accepting, Constants constants,
             double stopTime, double stepSize, int parameter,
             Class<A> AClass, Class<NP> NPClass)
    {
        NumericalPostprocessor postprocessor = runTheProbedSystem
            (model, countActionStrings, false, stateObservers,
                    statesCountExpressions, mapping, mainDef, constants,
                    stopTime, stepSize, parameter, AClass, NPClass);
        Set<GroupComponentPair> pairs = model.getGroupComponentPairs(mainDef);

        Set<AbstractExpression> afterBegins
                = new HashSet<AbstractExpression> ();
        double[][] K = getProbabilitiesComponentStateAfterBegin
                (pairs, mainDef, postprocessor, constants, afterBegins);

        LinkedHashMap<GroupComponentPair, AbstractExpression> crates
                = new LinkedHashMap<GroupComponentPair, AbstractExpression> ();
        AbstractExpressionEvaluator eval = postprocessor.getExpressionEvaluator
                (statesCountExpressions, constants);
        int indices = (int) Math.ceil (stopTime / stepSize);
        int i = 0;
        double[][] cdf = new double[indices][];

        for (double s = 0; s < stopTime; s += stepSize)
        {
            double[] matchval = getStartingStates (model,  mainDef, constants,
                    postprocessor, s, crates);
            double[] times = new double[statesCountExpressions.size ()];
            Arrays.fill (times, s);
            double[] val = postprocessor.evaluateExpressionsAtTimes
                    (eval, times, constants);

            assignNewCounts (crates, definitionsMap, mainDef, model,
                    statesCountExpressions, mapping, matchval, val);
            postprocessor = runTheProbedSystem (model, countActionStrings,
                    false, stateObservers, statesCountExpressions, mapping,
                    mainDef, constants, stopTime, stepSize, parameter,
                    AClass, NPClass);
            double[][] obtainedMeasurements = postprocessor.evaluateExpressions
                    (statesCountExpressions, constants);
            cdf[i] = new double[obtainedMeasurements.length];

            passageTimeCDF (obtainedMeasurements, pairs, accepting,
                    cdf[i], statesCountExpressions, mapping);
            ++i;
        }

        double[] uncCdf = new double[indices];
        // now integration, possible directly on obtained values
        for (int s = 1; s < indices; ++s)
        {
            double derivK = (K[s][0] - K[s - 1][0])/stepSize;
            System.out.println(derivK);
            for (int t = 0; t < indices; ++t)
            {
                uncCdf[t] += cdf[s][t] * derivK;
            }
        }

        for (int t = 0; t < indices; ++t)
        {
            uncCdf[t] *= stepSize;
        }

        return new CDF (uncCdf);
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
             double stopTime, double stepSize, int parameter,
             Class<A> AClass, Class<NP> NPClass,
             int start_time)
    {
        NumericalPostprocessor postprocessor = runTheProbedSystem
            (model, countActionStrings, false, stateObservers,
                    statesCountExpressions, mapping, mainDef, constants,
                    stopTime, stepSize, parameter, AClass, NPClass);

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
    private double[] getStartingStates
        (GroupedModel model, PEPAComponentDefinitions definitions,
         Constants constants, NumericalPostprocessor postprocessor, double time,
         LinkedHashMap<GroupComponentPair, AbstractExpression> crates)
    {
        // obtaining ratios for steady state component distribution
        getProbabilitiesAfterBegin (model, definitions, crates);

        // obtaining steady-state probabilities (user set stop-time)
        List<AbstractExpression> expressions
                = new LinkedList<AbstractExpression> (crates.values ());

        AbstractExpressionEvaluator eval
                = postprocessor.getExpressionEvaluator (expressions, constants);
        double[] times = new double[expressions.size ()];
        for (int i = 0; i < times.length; ++i)
        {
            times[i] = time;
        }
        return postprocessor.evaluateExpressionsAtTimes
            (eval, times, constants);
    }

    private void assignNewCounts
        (LinkedHashMap<GroupComponentPair, AbstractExpression> crates,
         Map<PEPAComponentDefinitions, Set<ComponentId>> definitionsMap,
         PEPAComponentDefinitions definitions, GroupedModel model,
         List<AbstractExpression> statesCountExpressions,
         Map<String, AbstractExpression> mapping,
         double[] matchval, double[] val)
    {
        int i = 0;
        for (GroupComponentPair gc : crates.keySet ())
        {
            boolean containsComp = false;
            for (ComponentId comp : definitionsMap.get (definitions))
            {
                if (gc.getComponent ().containsComponent (comp))
                {
                    containsComp = true;
                    break;
                }
            }

            if (containsComp)
            {
                crates.put (gc, new DoubleExpression (matchval[i]));
            }
            else
            {
                crates.put (gc, new DoubleExpression
                        (val[statesCountExpressions.indexOf
                                (mapping.get (gc.toString ()))]));
            }
            ++i;
        }

        // setting initial number of components for next analysis
        Map<String, LabelledComponentGroup> lgs = model.getComponentGroups ();
        for (LabelledComponentGroup lg : lgs.values ())
        {
            Group g = lg.getGroup ();
            for (PEPAComponent c : g.getComponentDerivatives (definitions))
            {
                g.setCountExpression (c, crates.get
                        (new GroupComponentPair (lg.getLabel (), c)));
            }
        }
    }

    private void passageTimeCDF
        (double[][] obtainedMeasurements, Set<GroupComponentPair> pairs,
         ComponentId accepting, double[] cdf,
         List<AbstractExpression> statesCountExpressions,
         Map<String, AbstractExpression> mapping)
    {

        for (GroupComponentPair gp : pairs)
        {
            if (gp.getComponent ().containsComponent (accepting))
            {
                 for (int i = 0; i < obtainedMeasurements.length; i++)
                  {
                    cdf[i] += obtainedMeasurements[i]
                            [statesCountExpressions.indexOf
                            (mapping.get (gp.toString ()))];
                }
            }
        }
    }

    private double[][] getProbabilitiesComponentStateAfterBegin
        (Set<GroupComponentPair> pairs, PEPAComponentDefinitions definitions,
         NumericalPostprocessor postprocessor, Constants constants,
         Set<AbstractExpression> afterBegins)
    {
        afterBegins = new HashSet<AbstractExpression> ();
        Set<PEPAComponent> afterBeginsC = new HashSet<PEPAComponent> ();
        for (GroupComponentPair q : pairs)
        {
            for (GroupComponentPair hq : pairs)
            {
                Collection<AbstractPrefix> prefices
                        = hq.getComponent ().getPrefixes (definitions);
                for (AbstractPrefix prefix : prefices)
                {
                    if (prefix.getImmediates ().contains ("begin")
                            && prefix.getContinuation ()
                            .equals(q.getComponent()))
                    {
                        afterBegins.add
                                (CombinedProductExpression.createMeanExpression
                                        (new GPEPAState(q)));
                        afterBeginsC.add (q.getComponent ());
                    }
                }
            }
        }

        findClosureOnAnyActions
            (afterBeginsC, definitions, new HashSet<PEPAComponent> ());
        for (GroupComponentPair hq : pairs)
        {
            if (afterBeginsC.contains(hq.getComponent()))
            {
                afterBegins.add
                        (CombinedProductExpression.createMeanExpression
                                (new GPEPAState (hq)));
            }
        }

        List<AbstractExpression> sum = new ArrayList<AbstractExpression> ();
        sum.add (SumExpression.create (afterBegins));
        return postprocessor.evaluateExpressions (sum, constants);
    }

    private void getProbabilitiesAfterBegin
            (GroupedModel model, PEPAComponentDefinitions definitions,
             LinkedHashMap<GroupComponentPair, AbstractExpression> result)
    {
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
                = hc.getComponent ().getPrefixes(definitions);
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
                                    (action, hc.getComponent());
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
    }

    private void findClosureOnAnyActions
        (Set<PEPAComponent> found, PEPAComponentDefinitions definitions,
            Set<PEPAComponent> visited)
    {
        Set<PEPAComponent> newFound = new HashSet<PEPAComponent> ();
        for (PEPAComponent c : found)
        {
            if (!visited.contains(c))
            {
                newFound.addAll
                    (findClosureOnAnyActionsI (c, found, definitions, visited));
            }
        }
        found.addAll (newFound);
        if (newFound.size () > 0)
        {
            findClosureOnAnyActions (found, definitions, visited);
        }
    }

    private Set<PEPAComponent> findClosureOnAnyActionsI
        (PEPAComponent c, Set<PEPAComponent> found,
             PEPAComponentDefinitions definitions, Set<PEPAComponent> visited)
    {
        Set<PEPAComponent> newFound = new HashSet<PEPAComponent> ();
        visited.add (c);
        List<AbstractPrefix> prefices = c.getPrefixes (definitions);
        for (AbstractPrefix p : prefices)
        {
            newFound.add (p.getContinuation());
        }
        return newFound;
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
            (GroupedModel model, Set<String> countActionsSet,
             boolean countActions, Collection<GPEPAState> stateObservers,
             List<AbstractExpression> statesCountExpressions,
             Map<String, AbstractExpression> stateCombPopMapping,
             PEPAComponentDefinitions definitions, Constants constants,
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

        Set<String> initActions = countActionsSet;
        if (countActions)
        {
            for (String action : countActionsSet)
            {
                GPEPAActionCount gpepaAction = new GPEPAActionCount (action);
                Multiset<State> cooperationActions = HashMultiset.create ();
                setStateObserver (cooperationActions, gpepaAction, action, moments,
                        statesCountExpressions, stateCombPopMapping);
            }
        }
        else
        {
            initActions = new HashSet<String> ();
        }
        List<PlotDescription> plotDescriptions
            = new LinkedList<PlotDescription> ();

        PCTMC pctmc = GPEPAToPCTMC.getPCTMC (definitions, model, initActions);
        System.out.println (pctmc);

        AbstractPCTMCAnalysis analysis = getAnalysis (pctmc, AClass);
        analysis.setUsedMoments (moments);
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
