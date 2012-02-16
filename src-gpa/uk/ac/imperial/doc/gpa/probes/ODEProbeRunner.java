package uk.ac.imperial.doc.gpa.probes;

import uk.ac.imperial.doc.gpepa.representation.components.ComponentId;
import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponentDefinitions;
import uk.ac.imperial.doc.gpepa.representation.group.GroupComponentPair;
import uk.ac.imperial.doc.gpepa.representation.model.GroupedModel;
import uk.ac.imperial.doc.gpepa.states.GPEPAState;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.javaoutput.statements.AbstractExpressionEvaluator;
import uk.ac.imperial.doc.pctmc.odeanalysis.PCTMCODEAnalysis;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.CPPODEAnalysisNumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.NumericalPostprocessor;

import java.util.*;

public class ODEProbeRunner extends AbstractProbeRunner
{
    public ODEProbeRunner ()
    {
        analysisType = PCTMCODEAnalysis.class;
        postprocType = CPPODEAnalysisNumericalPostprocessor.class;
    }

    @Override
    protected CDF steadyIndividual
        (List<AbstractExpression> statesCountExpressions,
         Map<String, AbstractExpression> mapping,
         Set<String> countActionStrings,
         GroupedModel model, Set<GPEPAState> stateObservers,
         PEPAComponentDefinitions mainDef, PEPAComponentDefinitions altDef,
         Map<PEPAComponentDefinitions, Set<ComponentId>> definitionsMap,
         ComponentId accepting, Constants constants,
         double stopTime, double stepSize, int parameter,
         double steadyStateTime)
    {
        NumericalPostprocessor postprocessor = runTheProbedSystem
            (model, countActionStrings, false, stateObservers,
                statesCountExpressions, mapping, mainDef, constants,
                steadyStateTime, stepSize, parameter);
        LinkedHashMap<GroupComponentPair, AbstractExpression> crates
            = new LinkedHashMap<GroupComponentPair, AbstractExpression> ();

        // obtaining ratios for steady state component distribution
        double[][] cratesVal = getStartingStates
            (model, mainDef, constants, postprocessor, crates);
        double[] times = new double[statesCountExpressions.size ()];
        double maxTime = steadyStateTime - stepSize;
        Arrays.fill (times, maxTime);
        AbstractExpressionEvaluator evaluator = postprocessor
            .getExpressionEvaluator (statesCountExpressions, constants);
        double[] steadyVal = postprocessor.evaluateExpressionsAtTimes
            (evaluator, times, constants);

        int sindex = (int) (maxTime / stepSize) - 1;
        assignNewCounts (crates, definitionsMap, mainDef, model,
                statesCountExpressions, mapping, cratesVal[sindex], steadyVal);
        statesCountExpressions = new LinkedList<AbstractExpression> ();
        mapping = new HashMap<String, AbstractExpression> ();
        Set<GroupComponentPair> pairs = model.getGroupComponentPairs (altDef);
        stateObservers = new HashSet<GPEPAState> ();
        for (GroupComponentPair pair : pairs)
        {
            stateObservers.add (new GPEPAState (pair));
        }
        postprocessor = runTheProbedSystem (model, countActionStrings, false,
                stateObservers, statesCountExpressions, mapping, altDef,
                constants, stopTime, stepSize, parameter);

        double[][] obtainedMeasurements = postprocessor.evaluateExpressions
            (statesCountExpressions, constants);
        double[] cdf = passageTimeCDF (obtainedMeasurements, pairs, accepting,
                statesCountExpressions, mapping);

        return new CDF (cdf);
    }

    @Override
    protected CDF transientIndividual
        (List<AbstractExpression> statesCountExpressions,
         Map<String, AbstractExpression> mapping,
         Set<String> countActionStrings,
         GroupedModel model, Set<GPEPAState> stateObservers,
         PEPAComponentDefinitions mainDef,
         Map<PEPAComponentDefinitions, Set<ComponentId>> definitionsMap,
         ComponentId accepting, Constants constants,
         double stopTime, double stepSize, int parameter,
         double steadyStateTime)
    {
        NumericalPostprocessor postprocessor = runTheProbedSystem
            (model, countActionStrings, false, stateObservers,
                statesCountExpressions, mapping, mainDef, constants,
                steadyStateTime + stepSize, stepSize, parameter);
        Set<GroupComponentPair> pairs = model.getGroupComponentPairs (mainDef);

        double[][] K = getProbabilitiesComponentStateAfterBegin
            (pairs, mainDef, postprocessor, constants);
        double[][] steadyVal = postprocessor.evaluateExpressions
            (statesCountExpressions, constants);

        LinkedHashMap<GroupComponentPair, AbstractExpression> crates
            = new LinkedHashMap<GroupComponentPair, AbstractExpression> ();
        // obtaining ratios for steady state component distribution
        double[][] matchVal = getStartingStates
            (model, mainDef, constants, postprocessor, crates);

        int times = (int) Math.ceil (stopTime / stepSize);
        int indices = (int) Math.ceil (steadyStateTime / stepSize);
        int i = 0;
        double[][] cdf = new double[indices][];

        for (double s = 0; s < steadyStateTime; s += stepSize)
        {
            assignNewCounts (crates, definitionsMap, mainDef, model,
                    statesCountExpressions, mapping, matchVal[i], steadyVal[i]);

            List<AbstractExpression> statesCountExpressionsS
                = new ArrayList<AbstractExpression> ();
            Map<String, AbstractExpression> mappingS
                = new HashMap<String, AbstractExpression> ();
            NumericalPostprocessor postprocessorS = runTheProbedSystem
                (model, countActionStrings, false, stateObservers,
                    statesCountExpressionsS, mappingS,
                    mainDef, constants, stopTime, stepSize, parameter);
            double[][] obtainedMeasurements = postprocessorS.evaluateExpressions
                (statesCountExpressionsS, constants);
            cdf[i] = passageTimeCDF (obtainedMeasurements, pairs, accepting,
                    statesCountExpressionsS, mappingS);
            ++i;
            System.out.println ("Ran transient iteration " + i);
        }

        double[] uncCdf = new double[times];
        // now integration, possible directly on obtained values
        for (int s = 0; s < indices; ++s)
        {
            double derivK = (K[s + 1][0] - K[s][0]) / stepSize;
            for (int t = 0; t < times; ++t)
            {
                uncCdf[t] += (cdf[s][t] * derivK);
            }
        }

        for (int t = 0; t < times; ++t)
        {
            uncCdf[t] *= stepSize;
        }

        return new CDF (uncCdf);
    }
}