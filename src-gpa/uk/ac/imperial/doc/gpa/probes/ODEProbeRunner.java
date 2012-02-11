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
        double maxTime = steadyStateTime - stepSize;
        double[] cratesVal = getStartingStates
            (model,  mainDef, constants, postprocessor, maxTime, crates);
        double[] times = new double[statesCountExpressions.size ()];
        Arrays.fill (times, maxTime);
        AbstractExpressionEvaluator evaluator = postprocessor
            .getExpressionEvaluator (statesCountExpressions, constants);
        double[] steadyVal = postprocessor.evaluateExpressionsAtTimes
            (evaluator, times, constants);

        assignNewCounts (crates, definitionsMap, mainDef, model,
                statesCountExpressions, mapping, cratesVal, steadyVal);
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
        double[] cdf = new double[obtainedMeasurements.length];

        passageTimeCDF (obtainedMeasurements, pairs, accepting,
                cdf, statesCountExpressions, mapping);

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
         double stopTime, double stepSize, int parameter)
    {
        NumericalPostprocessor postprocessor = runTheProbedSystem
            (model, countActionStrings, false, stateObservers,
                    statesCountExpressions, mapping, mainDef, constants,
                    stopTime, stepSize, parameter);
        Set<GroupComponentPair> pairs = model.getGroupComponentPairs (mainDef);

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
                    mainDef, constants, stopTime, stepSize, parameter);
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
}
