package uk.ac.imperial.doc.gpa.probes;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import uk.ac.imperial.doc.gpa.fsm.ITransition;
import uk.ac.imperial.doc.gpa.pctmc.GPEPAToPCTMC;
import uk.ac.imperial.doc.gpepa.representation.components.AbstractPrefix;
import uk.ac.imperial.doc.gpepa.representation.components.ComponentId;
import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponentDefinitions;
import uk.ac.imperial.doc.gpepa.representation.components.Prefix;
import uk.ac.imperial.doc.gpepa.representation.group.GroupComponentPair;
import uk.ac.imperial.doc.gpepa.representation.model.GroupedModel;
import uk.ac.imperial.doc.gpepa.states.GPEPAActionCount;
import uk.ac.imperial.doc.gpepa.states.GPEPAState;
import uk.ac.imperial.doc.igpepa.representation.components.iPEPAPrefix;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.constants.visitors.ExpressionEvaluatorWithConstants;
import uk.ac.imperial.doc.jexpressions.expressions.*;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.NumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.representation.State;
import uk.ac.imperial.doc.pctmc.utils.PCTMCOptions;

import java.util.*;

public abstract class AbstractProbeRunner
{
    private static ProbeGraph graph;
    protected Class<? extends AbstractPCTMCAnalysis> analysisType;
    protected Class<? extends NumericalPostprocessor> postprocessorType;
    protected static final String BEGIN_SIGNAL = "begin";

    protected abstract CDF steadyIndividual
        (List<AbstractExpression> statesCountExpressions,
         Map<String, Integer> mapping, GroupedModel model,
         Set<GPEPAState> stateObservers, PEPAComponentDefinitions mainDef,
         PEPAComponentDefinitions altDef,
         Map<PEPAComponentDefinitions, Set<ComponentId>> definitionsMap,
         Set<ComponentId> accepting, Constants constants,
         double stopTime, double stepSize, int parameter,
         double steadyStateTime, String name);

    protected abstract CDF transientIndividual
        (List<AbstractExpression> statesCountExpressions,
         Map<String, Integer> mapping, GroupedModel model,
         Set<GPEPAState> stateObservers, PEPAComponentDefinitions mainDef,
         Map<PEPAComponentDefinitions, Set<ComponentId>> definitionsMap,
         Set<ComponentId> accepting, Constants constants,
         double stopTime, double stepSize, int parameter,
         double steadyStateTime, String name);

    protected abstract CDF globalPassages
        (GlobalProbe gprobe, GroupedModel model, Set<GPEPAState> stateObservers,
         List<AbstractExpression> statesCountExpressions,
         Map<String, Integer> mapping, Set<String> countActions,
         Set<ComponentId> accepting, Constants constants,
         PEPAComponentDefinitions mainDef,
         double stopTime, double stepSize, int parameter);

    public CDF executeProbedModel
        (GlobalProbe gprobe, GroupedModel model,
         Set<GPEPAState> stateObservers,
         PEPAComponentDefinitions mainDef, PEPAComponentDefinitions altDef,
         Map<PEPAComponentDefinitions, Set<ComponentId>> definitionsMap,
         Set<ComponentId> accepting, Constants constants,
         AbstractExpression stopTime, AbstractExpression stepSize,
         int parameter, Set<ITransition> alphabet, int mode, double modePar)
    {
        Set<String> countActionStrings = convertObjectsToStrings (alphabet);
        List<AbstractExpression> statesCountExpressions
            = new LinkedList<AbstractExpression> ();
        Map<String, Integer> mapping
            = new HashMap<String, Integer> ();
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

        if (PCTMCOptions.gui)
        {
            plotGraph (gprobe.getName (), cdf, stepSizeVal);
        }

        return cdf;
    }

    private CDF dispatchEvaluation
        (GlobalProbe gprobe, GroupedModel model, Set<GPEPAState> stateObservers,
         List<AbstractExpression> statesCountExpressions,
         Map<String, Integer> mapping, Set<String> countActions,
         PEPAComponentDefinitions mainDef, PEPAComponentDefinitions altDef,
         Map<PEPAComponentDefinitions, Set<ComponentId>> definitionsMap,
         Set<ComponentId> accepting, Constants constants,
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


    protected double[] passageTimeCDF
        (double[][] obtainedMeasurements, Set<GroupComponentPair> pairs,
         Set<ComponentId> accepting, Map<String, Integer> mapping)
    {
        double[] cdf = new double[obtainedMeasurements.length];
        for (ComponentId absorbingState : accepting)
        {
            for (GroupComponentPair gp : pairs)
            {
                if (gp.getComponent ().containsComponent (absorbingState))
                {
                    for (int i = 0; i < obtainedMeasurements.length; ++i)
                    {
                        cdf[i] += obtainedMeasurements[i]
                            [mapping.get (gp.toString ())];
                    }
                }
            }
        }
        return cdf;
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
                .getActions(definitions);
            Collection<AbstractPrefix> prefices
                = hc.getComponent ().getPrefixes (definitions);
            for (String action : actions)
            {
                for (AbstractPrefix prefix : prefices)
                {
                    if (prefix.getAction ().equals (action)
                            && (((iPEPAPrefix) prefix).getImmediates ()
                                .contains (BEGIN_SIGNAL)
                            || (prefix.getAction ().equals (BEGIN_SIGNAL)
                                && prefix instanceof Prefix)))
                    {
                        AbstractExpression arate
                            = definitions.getApparentRateExpression
                                (action, hc.getComponent ());
                        AbstractExpression crate
                            = model.getComponentRateExpression
                                (action, definitions, hc);
                        summands.add (ProductExpression.create
                                (prefix.getRate(),
                                        DivExpression.create(crate, arate)));
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
                                    && (((iPEPAPrefix) prefix).getImmediates ()
                                    .contains (BEGIN_SIGNAL)
                                    || (action.equals (BEGIN_SIGNAL)
                                        && prefix instanceof Prefix)))
                            {
                                AbstractExpression arate
                                    = definitions.getApparentRateExpression
                                        (action, hc.getComponent ());
                                AbstractExpression crate
                                    = model.getComponentRateExpression
                                        (action, definitions, hc);
                                summands.add (ProductExpression.create
                                        (prefix.getRate (), DivExpression
                                                .create (crate, arate)));
                            }
                        }
                    }
                }
                result.put (q, DivExpression.create
                        (SumExpression.create(summands), totalBeginRate));
            }
        }
    }

    protected NumericalPostprocessor runTheProbedSystem
            (GroupedModel model, PEPAComponentDefinitions definitions,
             Constants constants, Set<String> countActionsSet,
             Collection<GPEPAState> stateObservers,
             List<AbstractExpression> statesCountExpressions,
             Map<String, Integer> stateCombPopMapping,
             double stopTime, double stepSize, int parameter, PCTMC[] pctmcs)
    {
        List<CombinedPopulationProduct> moments
            = new ArrayList<CombinedPopulationProduct> ();
        int index = 0;
        for (GPEPAState state : stateObservers)
        {
            Multiset<State> states = HashMultiset.create ();
            setStateObserver (states, state, state.toString (), moments,
                    statesCountExpressions, stateCombPopMapping, index);
            ++index;
        }

        Set<String> initActions = countActionsSet;
        if (countActionsSet != null)
        {
            for (String action : countActionsSet)
            {
                GPEPAActionCount gpepaAction = new GPEPAActionCount (action);
                Multiset<State> cooperationActions = HashMultiset.create ();
                setStateObserver (cooperationActions, gpepaAction, action,
                        moments, statesCountExpressions,
                        stateCombPopMapping, index);
                ++index;
            }
        }
        else
        {
            initActions = new HashSet<String> ();
        }

        pctmcs[0] = GPEPAToPCTMC.getPCTMC
            (definitions, model, initActions);
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
         Map<String, Integer> stateCombPopMapping, int index)
    {
        statesSet.add (state);
        CombinedPopulationProduct combinedPopulation
            = new CombinedPopulationProduct (new PopulationProduct (statesSet));
        moments.add (combinedPopulation);
        AbstractExpression combProd
            = CombinedProductExpression.create (combinedPopulation);
        statesCountExpressions.add (combProd);
        stateCombPopMapping.put (mappingString, index);
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
