package uk.ac.imperial.doc.gpa.probes;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import uk.ac.imperial.doc.gpa.fsm.ITransition;
import uk.ac.imperial.doc.gpa.pctmc.GPEPAToPCTMC;
import uk.ac.imperial.doc.gpepa.representation.components.*;
import uk.ac.imperial.doc.gpepa.representation.group.Group;
import uk.ac.imperial.doc.gpepa.representation.group.GroupComponentPair;
import uk.ac.imperial.doc.gpepa.representation.model.GroupedModel;
import uk.ac.imperial.doc.gpepa.representation.model.LabelledComponentGroup;
import uk.ac.imperial.doc.gpepa.states.GPEPAActionCount;
import uk.ac.imperial.doc.gpepa.states.GPEPAState;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.constants.visitors.ExpressionEvaluatorWithConstants;
import uk.ac.imperial.doc.jexpressions.expressions.*;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.expressions.*;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.NumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.representation.State;

import java.util.*;

public abstract class AbstractProbeRunner
{
    private static ProbeGraph graph;
    protected Class<? extends AbstractPCTMCAnalysis> analysisType;
    protected Class<? extends NumericalPostprocessor> postprocessorType;
    private static final String BEGIN_SIGNAL = "begin";

    protected abstract CDF steadyIndividual
        (List<AbstractExpression> statesCountExpressions,
         Map<String, AbstractExpression> mapping,
         GroupedModel model, Set<GPEPAState> stateObservers,
         PEPAComponentDefinitions mainDef, PEPAComponentDefinitions altDef,
         Map<PEPAComponentDefinitions, Set<ComponentId>> definitionsMap,
         ComponentId accepting, Constants constants,
         double stopTime, double stepSize, int parameter,
         double steadyStateTime, String name);

    protected abstract CDF transientIndividual
        (List<AbstractExpression> statesCountExpressions,
         Map<String, AbstractExpression> mapping,
         GroupedModel model, Set<GPEPAState> stateObservers,
         PEPAComponentDefinitions mainDef,
         Map<PEPAComponentDefinitions, Set<ComponentId>> definitionsMap,
         ComponentId accepting, Constants constants,
         double stopTime, double stepSize, int parameter,
         double steadyStateTime, String name);

    protected abstract CDF globalPassages
        (GlobalProbe gprobe, GroupedModel model, Set<GPEPAState> stateObservers,
         List<AbstractExpression> statesCountExpressions,
         Map<String, AbstractExpression> mapping, Set<String> countActions,
         ComponentId accepting, Constants constants,
         PEPAComponentDefinitions mainDef,
         double stopTime, double stepSize, int parameter);

    public CDF executeProbedModel
        (GlobalProbe gprobe, GroupedModel model,
         Set<GPEPAState> stateObservers,
         PEPAComponentDefinitions mainDef, PEPAComponentDefinitions altDef,
         Map<PEPAComponentDefinitions, Set<ComponentId>> definitionsMap,
         ComponentId accepting, Constants constants,
         AbstractExpression stopTime, AbstractExpression stepSize,
         int parameter, Set<ITransition> alphabet,
         int mode, double modePar, boolean plot)
    {
        Set<String> countActionStrings = convertObjectsToStrings (alphabet);
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
        double stepSizeVal = stepEval.getResult ();

        CDF cdf = dispatchEvaluation (gprobe, model, stateObservers,
                statesCountExpressions, mapping, countActionStrings,
                mainDef, altDef, definitionsMap, accepting, constants,
                stopTimeVal, stepSizeVal, parameter, mode, modePar);

        if (plot)
        {
            plotGraph (gprobe.getName (), cdf, stepSizeVal);
        }

        return cdf;
    }

    private CDF dispatchEvaluation
        (GlobalProbe gprobe, GroupedModel model, Set<GPEPAState> stateObservers,
         List<AbstractExpression> statesCountExpressions,
         Map<String, AbstractExpression> mapping, Set<String> countActions,
         PEPAComponentDefinitions mainDef, PEPAComponentDefinitions altDef,
         Map<PEPAComponentDefinitions, Set<ComponentId>> definitionsMap,
         ComponentId accepting, Constants constants,
         double stopTime, double stepSize, int parameter,
         int mode, double modePar)
    {
        switch (mode)
        {
            case 1:
                return steadyIndividual
                    (statesCountExpressions, mapping, model, stateObservers,
                        mainDef, altDef, definitionsMap, accepting,
                        constants, stopTime, stepSize, parameter,
                        modePar, gprobe.getName ());
            case 2:
                return transientIndividual
                    (statesCountExpressions, mapping, model, stateObservers,
                        mainDef, definitionsMap, accepting,
                        constants, stopTime, stepSize, parameter,
                        modePar, gprobe.getName ());
            case 3:
                return globalPassages
                    (gprobe, model, stateObservers, statesCountExpressions,
                        mapping, countActions, accepting, constants, mainDef,
                        stopTime, stepSize, parameter);
            default:
                return null;
        }
    }

    protected double[][] getStartingStates
        (GroupedModel model, PEPAComponentDefinitions definitions,
         Constants constants, NumericalPostprocessor postprocessor,
         LinkedHashMap<GroupComponentPair, AbstractExpression> crates)
    {
        getProbabilitiesAfterBegin (model, definitions, crates);

        List<AbstractExpression> expressions
            = new LinkedList<AbstractExpression> (crates.values ());

        return postprocessor.evaluateExpressions (expressions, constants);
    }

    protected void assignNewCounts
        (LinkedHashMap<GroupComponentPair, AbstractExpression> crates,
         Map<PEPAComponentDefinitions, Set<ComponentId>> definitionsMap,
         PEPAComponentDefinitions definitions, GroupedModel model,
         List<AbstractExpression> statesCountExpressions,
         Map<String, AbstractExpression> mapping,
         double[] matchVal, double[] origVal)
    {
        Map<GroupComponentPair, AbstractExpression> newCounts
            = new HashMap<GroupComponentPair, AbstractExpression> ();
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
                newCounts.put (gc, new DoubleExpression (matchVal[i]));
            }
            else
            {
                newCounts.put (gc, new DoubleExpression
                    (origVal[statesCountExpressions.indexOf
                            (mapping.get (gc.toString ()))]));
            }
            ++i;
        }

        // setting initial number of components for the next analysis
        Map<String, LabelledComponentGroup> lgs = model.getComponentGroups ();
        for (LabelledComponentGroup lg : lgs.values ())
        {
            Group g = lg.getGroup ();
            String label = lg.getLabel ();
            for (PEPAComponent c : g.getComponentDerivatives (definitions))
            {
                g.setCountExpression (c, newCounts.get
                        (new GroupComponentPair (label, c)));
            }
        }
    }

    protected double[] passageTimeCDF
        (double[][] obtainedMeasurements, Set<GroupComponentPair> pairs,
         ComponentId accepting, List<AbstractExpression> statesCountExpressions,
         Map<String, AbstractExpression> mapping)
    {
        double[] cdf = new double[obtainedMeasurements.length];
        for (GroupComponentPair gp : pairs)
        {
            if (gp.getComponent ().containsComponent (accepting))
            {
                for (int i = 0; i < obtainedMeasurements.length; ++i)
                {
                    cdf[i] += obtainedMeasurements[i]
                            [statesCountExpressions.indexOf
                            (mapping.get (gp.toString ()))];
                }
            }
        }
        return cdf;
    }

    // assumes probes without cycles
    protected double[][] getProbabilitiesComponentStateAfterBegin
        (Set<GroupComponentPair> pairs, PEPAComponentDefinitions definitions,
         NumericalPostprocessor postprocessor, Constants constants)
    {
        Set<PEPAComponent> afterBeginsC = new HashSet<PEPAComponent> ();
        for (GroupComponentPair hq : pairs)
        {
            Collection<AbstractPrefix> prefices
                = hq.getComponent ().getPrefixes (definitions);
            for (AbstractPrefix prefix : prefices)
            {
                if (prefix.getImmediates ().contains (BEGIN_SIGNAL))
                {
                    afterBeginsC.add (prefix.getContinuation ());
                }
            }
        }

        findClosureOnAnyActions
            (afterBeginsC, definitions, new HashSet<PEPAComponent> ());

        List<AbstractExpression> afterBegins
                = new ArrayList<AbstractExpression> ();
        for (GroupComponentPair hq : pairs)
        {
            if (afterBeginsC.contains (hq.getComponent ()))
            {
                afterBegins.add (CombinedProductExpression
                        .createMeanExpression (new GPEPAState (hq)));
            }
        }

        List<AbstractExpression> sum = new ArrayList<AbstractExpression> ();
        sum.add (SumExpression.create (afterBegins));
        return postprocessor.evaluateExpressions (sum, constants);
    }

    protected void getProbabilitiesAfterBegin
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
                = hc.getComponent ().getPrefixes (definitions);
            for (String action : actions)
            {
                for (AbstractPrefix prefix : prefices)
                {
                    if (prefix.getAction ().equals (action)
                        && prefix.getImmediates ().contains (BEGIN_SIGNAL))
                    {
                        AbstractExpression arate
                            = definitions.getApparentRateExpression
                                (action, hc.getComponent ());
                        AbstractExpression crate
                            = model.getComponentRateExpression
                                (action, definitions, hc);
                        summands.add (ProductExpression.create
                                (prefix.getRate (),
                                 DivExpression.create (crate, arate)));
                    }
                }
            }
        }
        AbstractExpression totalBeginRate = SumExpression.create (summands);

        // probability for each Q
        for (GroupComponentPair q : pairs)
        {
            if (totalBeginRate.equals (DoubleExpression.ZERO))
            {
                result.put (q, DoubleExpression.ZERO);
            }
            else
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
                                    && prefix.getImmediates ()
                                        .contains (BEGIN_SIGNAL))
                            {
                                AbstractExpression arate
                                    = definitions.getApparentRateExpression
                                        (action, hc.getComponent ());
                                AbstractExpression crate
                                    = model.getComponentRateExpression
                                        (action, definitions, hc);
                                summands.add (ProductExpression.create
                                        (prefix.getRate (),
                                         DivExpression.create (crate, arate)));
                            }
                        }
                    }
                }
                result.put (q, DivExpression.create
                        (SumExpression.create (summands), totalBeginRate));
            }
        }
    }

    protected void findClosureOnAnyActions
        (Set<PEPAComponent> found, PEPAComponentDefinitions definitions,
         Set<PEPAComponent> visited)
    {
        Set<PEPAComponent> newFound = new HashSet<PEPAComponent> ();
        for (PEPAComponent c : found)
        {
            if (!visited.contains (c))
            {
                newFound.addAll
                    (findClosureOnAnyActionsI (c, definitions, visited));
            }
        }
        found.addAll (newFound);
        if (newFound.size () > 0)
        {
            findClosureOnAnyActions (found, definitions, visited);
        }
    }

    private Set<PEPAComponent> findClosureOnAnyActionsI
        (PEPAComponent c, PEPAComponentDefinitions definitions,
         Set<PEPAComponent> visited)
    {
        visited.add (c);
        Set<PEPAComponent> newFound = new HashSet<PEPAComponent> ();
        List<AbstractPrefix> prefices = c.getPrefixes (definitions);
        for (AbstractPrefix p : prefices)
        {
            newFound.add (p.getContinuation ());
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

    protected NumericalPostprocessor runTheProbedSystem
            (GroupedModel model, PEPAComponentDefinitions definitions,
             Constants constants, Set<String> countActionsSet,
             Collection<GPEPAState> stateObservers,
             List<AbstractExpression> statesCountExpressions,
             Map<String, AbstractExpression> stateCombPopMapping,
             double stopTime, double stepSize, int parameter, PCTMC[] pctmcs)
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
        if (countActionsSet != null)
        {
            for (String action : countActionsSet)
            {
                GPEPAActionCount gpepaAction = new GPEPAActionCount (action);
                Multiset<State> cooperationActions = HashMultiset.create ();
                setStateObserver (cooperationActions, gpepaAction, action,
                        moments, statesCountExpressions, stateCombPopMapping);
            }
        }
        else
        {
            initActions = new HashSet<String> ();
        }

        pctmcs[0] = GPEPAToPCTMC.getPCTMC (definitions, model, initActions);
        System.out.println (pctmcs[0]);
        AbstractPCTMCAnalysis analysis
            = getPreparedAnalysis (pctmcs[0], moments, constants);

        /*
            Set<String> cooperation = new HashSet<String> ();
            cooperation.add ("cont_tfr");
            cooperation.add ("data_tfr");
            cooperation.add ("clt_shutdown");
            AbstractExpression ge = new PatternPopulationExpression (new GPEPAState (new GroupComponentPair ("Clients", new CooperationComponent (new AnyComponent (), new AnyComponent (), cooperation))));
            List<AbstractExpression> listGE = new ArrayList<AbstractExpression> ();
            plotDescriptions.add (new PlotDescription (listGE));

            PatternSetterVisitor.unfoldPatterns (ge, new GPEPAPatternMatcher (pctmc));
            listGE.add (ge);
            AbstractPCTMCAnalysis.unfoldVariablesAndSetUsedProducts (analysis, plotDescriptions, new HashMap<ExpressionVariable, AbstractExpression> ());
        */

        NumericalPostprocessor postprocessor
            = getPostprocessor (stopTime, stepSize, parameter);
        return runPostProcessor (analysis, postprocessor, constants);
    }

    protected NumericalPostprocessor runPostProcessor
        (AbstractPCTMCAnalysis analysis, NumericalPostprocessor postprocessor,
         Constants constants)
    {
        postprocessor.prepare (analysis, constants);
        postprocessor.calculateDataPoints (constants);
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

    private NumericalPostprocessor getPostprocessor
        (double stopTime, double stepSize, int parameter)
    {
        try
        {
            return postprocessorType.getDeclaredConstructor
                (double.class, double.class, int.class).newInstance
                (stopTime, stepSize, parameter);
        }
        catch (Exception e)
        {
            e.printStackTrace ();
        }
        return null;
    }

    protected AbstractPCTMCAnalysis getPreparedAnalysis
        (PCTMC pctmc, List<CombinedPopulationProduct> moments,
         Constants constants)
    {
        AbstractPCTMCAnalysis analysis = getAnalysis (pctmc);
        analysis.setUsedMoments (moments);
        analysis.prepare (constants);
        return analysis;
    }

    protected AbstractPCTMCAnalysis getAnalysis (PCTMC pctmc)
    {
        try
        {
            return analysisType.getDeclaredConstructor
                (PCTMC.class).newInstance (pctmc);
        }
        catch (Exception ex)
        {
            ex.printStackTrace ();
        }
        return null;
    }
}
