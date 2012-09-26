package uk.ac.imperial.doc.gpa.probes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DivExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.ProductExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.NumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.representation.State;
import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;
import uk.ac.imperial.doc.pctmc.utils.PCTMCOptions;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public abstract class AbstractProbeRunner
{
    private static ProbeGraph graph;
    protected Class<? extends AbstractPCTMCAnalysis> analysisType;
    protected Class<? extends NumericalPostprocessor> postprocessorType;
    protected static final String BEGIN_SIGNAL = "begin";

    protected final Constants constants;
    private final Map<ExpressionVariable, AbstractExpression> unfoldedVariables;
    
  

    public AbstractProbeRunner (Constants constants,
           Map<ExpressionVariable, AbstractExpression> unfoldedVariables)
    {
        this.constants = constants;
        this.unfoldedVariables = unfoldedVariables;
    }

    protected abstract CDF steadyIndividual
        (List<AbstractExpression> statesCountExpressions,
         Map<String, Integer> mapping, GroupedModel model,
         Set<GPEPAState> stateObservers, PEPAComponentDefinitions mainDef,
         PEPAComponentDefinitions altDef,
         Map<PEPAComponentDefinitions, Set<ComponentId>> definitionsMap,
         Set<ComponentId> accepting,
         AbstractPCTMCAnalysis analysis, NumericalPostprocessor postprocessor,
         double steadyStateTime, String name);

    protected abstract CDF transientIndividual
        (List<AbstractExpression> statesCountExpressions,
         Map<String, Integer> mapping, GroupedModel model,
         Set<GPEPAState> stateObservers, PEPAComponentDefinitions mainDef,
         Map<PEPAComponentDefinitions, Set<ComponentId>> definitionsMap,
         Set<ComponentId> accepting,
         AbstractPCTMCAnalysis analysis, NumericalPostprocessor postprocessor,
         double steadyStateTime, String name);

    protected abstract CDF globalPassages
        (GlobalProbe gprobe, GroupedModel model, Set<GPEPAState> stateObservers,
         List<AbstractExpression> statesCountExpressions,
         Map<String, Integer> mapping, Set<String> countActions,
         Set<ComponentId> accepting,
         PEPAComponentDefinitions mainDef,
         AbstractPCTMCAnalysis analysis, NumericalPostprocessor postprocessor);

    public CDF executeProbedModel
        (GlobalProbe gprobe, GroupedModel model,
         Set<GPEPAState> stateObservers,
         PEPAComponentDefinitions mainDef, PEPAComponentDefinitions altDef,
         Map<PEPAComponentDefinitions, Set<ComponentId>> definitionsMap,
         Set<ComponentId> accepting, 
         AbstractPCTMCAnalysis analysis,
         NumericalPostprocessor postprocessor,
         Set<String> alphabet,
         int mode, double modePar)
    {
        List<AbstractExpression> statesCountExpressions
            = new LinkedList<AbstractExpression> ();
        Map<String, Integer> mapping
            = new HashMap<String, Integer> ();
  

        CDF cdf = dispatchEvaluation (gprobe, model, stateObservers,
                statesCountExpressions, mapping, alphabet,
                mainDef, altDef, definitionsMap, accepting,
                analysis, postprocessor,                
                mode, modePar);

        if (PCTMCOptions.gui)
        {
            plotGraph (gprobe.getName (), cdf, postprocessor.getStepSize());
        }

        return cdf;
    }

    private CDF dispatchEvaluation
        (GlobalProbe gprobe, GroupedModel model, Set<GPEPAState> stateObservers,
         List<AbstractExpression> statesCountExpressions,
         Map<String, Integer> mapping, Set<String> countActions,
         PEPAComponentDefinitions mainDef, PEPAComponentDefinitions altDef,
         Map<PEPAComponentDefinitions, Set<ComponentId>> definitionsMap,         
         Set<ComponentId> accepting,
         AbstractPCTMCAnalysis analysis, NumericalPostprocessor postprocessor,
         int mode, double modePar)
    {
        switch (mode)
        {
            case 1:
                return steadyIndividual
                    (statesCountExpressions, mapping, model, stateObservers,
                        mainDef, altDef, definitionsMap, accepting,
                            analysis, postprocessor,
                        modePar, gprobe.getName ());
            case 2:
                return transientIndividual
                    (statesCountExpressions, mapping, model, stateObservers,
                        mainDef, definitionsMap, accepting,
                        analysis, postprocessor,
                        modePar, gprobe.getName ());
            case 3:
                return globalPassages
                    (gprobe, model, stateObservers, statesCountExpressions,
                        mapping, countActions, accepting, mainDef,
                        analysis, postprocessor);
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

    protected void outputInfo (int i, int repetitions, String message)
    {
        if (i > 0 && i % (repetitions / 5 > 0 ? repetitions / 5 : 1) == 0)
        {
            PCTMCLogging.setVisible (true);
            PCTMCLogging.info ("Ran " + i + " " + message + ".");
            PCTMCLogging.setVisible (false);
        }
    }

    protected NumericalPostprocessor runTheProbedSystem
            (GroupedModel model, PEPAComponentDefinitions definitions,
             Constants constants, Set<String> countActionsSet,
             Collection<GPEPAState> stateObservers,
             List<AbstractExpression> statesCountExpressions,
             Map<String, Integer> stateCombPopMapping,
             AbstractPCTMCAnalysis templateAnalysis,
             NumericalPostprocessor templatePostprocessor,
             PCTMC[] pctmcs)
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
            (definitions, model, initActions, unfoldedVariables);
        //System.out.println (pctmcs[0]);
        AbstractPCTMCAnalysis analysis
            = getPreparedAnalysis(templateAnalysis, pctmcs[0], moments, constants);

        NumericalPostprocessor postprocessor
            = (NumericalPostprocessor) templatePostprocessor.regenerate();
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

  
    protected AbstractPCTMCAnalysis getPreparedAnalysis
        (AbstractPCTMCAnalysis template, PCTMC pctmc, List<CombinedPopulationProduct> moments,
         Constants constants)
    {
        AbstractPCTMCAnalysis analysis = template.regenerate(pctmc);
        analysis.setUsedMoments (moments);
        analysis.prepare (constants);
        return analysis;
    }
}
