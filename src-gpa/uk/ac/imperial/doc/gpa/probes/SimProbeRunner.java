package uk.ac.imperial.doc.gpa.probes;

import uk.ac.imperial.doc.gpepa.representation.components.ComponentId;
import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponentDefinitions;
import uk.ac.imperial.doc.gpepa.representation.group.GroupComponentPair;
import uk.ac.imperial.doc.gpepa.representation.model.GroupedModel;
import uk.ac.imperial.doc.gpepa.states.GPEPAState;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.javaoutput.statements.AbstractExpressionEvaluator;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.NumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.SimulationAnalysisNumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.simulation.PCTMCSimulation;

import java.util.*;

public class SimProbeRunner extends AbstractProbeRunner
{
    public SimProbeRunner ()
    {
        analysisType = PCTMCSimulation.class;
        postprocType = SimulationAnalysisNumericalPostprocessor.class;
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
        double maxTime = steadyStateTime - stepSize;
        double altMaxTime = stopTime - stepSize;
        int altMaxTimeIndex = (int) (altMaxTime / stepSize);
        double[][] overallMeasurements
            = new double[altMaxTimeIndex][];

        Set<GroupComponentPair> pairs = model.getGroupComponentPairs (altDef);
        Set<GPEPAState> altStateObservers = new HashSet<GPEPAState> ();
        for (GroupComponentPair pair : pairs)
        {
            altStateObservers.add (new GPEPAState (pair));
        }
        List<AbstractExpression> altStatesCountExpressions = null;
        Map<String, AbstractExpression> altMapping = null;

        for (int i = 0; i < parameter; ++i)
        {
            statesCountExpressions = new LinkedList<AbstractExpression> ();
            mapping = new HashMap<String, AbstractExpression> ();
            NumericalPostprocessor postprocessor = runTheProbedSystem
                (model, countActionStrings, false, stateObservers,
                        statesCountExpressions, mapping, mainDef, constants,
                        steadyStateTime, stepSize, 1);
            LinkedHashMap<GroupComponentPair, AbstractExpression> crates
                = new LinkedHashMap<GroupComponentPair, AbstractExpression> ();
            double[] cratesVal = getStartingStates (model,  mainDef, constants,
                    postprocessor, maxTime, crates);
            double[] times = new double[statesCountExpressions.size ()];
            Arrays.fill (times, maxTime);
            AbstractExpressionEvaluator evaluator = postprocessor
                .getExpressionEvaluator (statesCountExpressions, constants);
            double[] steadyVal = postprocessor.evaluateExpressionsAtTimes
                (evaluator, times, constants);

            assignNewCounts (crates, definitionsMap, mainDef, model,
                statesCountExpressions, mapping, cratesVal, steadyVal);
            altStatesCountExpressions = new LinkedList<AbstractExpression> ();
            altMapping = new HashMap<String, AbstractExpression> ();
            postprocessor = runTheProbedSystem (model, countActionStrings,
                    false, altStateObservers, altStatesCountExpressions,
                    altMapping, altDef, constants, stopTime, stepSize, 1);
            double[][] obtainedMeasurements = postprocessor.evaluateExpressions
                    (altStatesCountExpressions, constants);
            for (int x = 0; x < altMaxTimeIndex; ++x)
            {
                if (overallMeasurements[x] == null)
                {
                    overallMeasurements[x] = obtainedMeasurements[x];
                }
                else
                {
                    for (int y = 0; y < altStateObservers.size (); ++y)
                    {
                        overallMeasurements[x][y] += obtainedMeasurements[x][y];
                    }
                }
            }
        }

        for (int x = 0; x < overallMeasurements.length; ++x)
        {
            for (int y = 0; y < altStateObservers.size (); ++y)
            {
                overallMeasurements[x][y] /= parameter;
            }
        }

        double[] cdf = new double[overallMeasurements.length];

        passageTimeCDF (overallMeasurements, pairs, accepting,
                cdf, altStatesCountExpressions, altMapping);

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
        int indices = (int) Math.ceil (stopTime / stepSize);
        double[] mainCdf = new double[indices];
        for (int p = 0; p < parameter; ++p)
        {
            NumericalPostprocessor postprocessor = runTheProbedSystem
                    (model, countActionStrings, false, stateObservers,
                            statesCountExpressions, mapping, mainDef, constants,
                            stopTime, stepSize, 1);
            Set<GroupComponentPair> pairs
                = model.getGroupComponentPairs (mainDef);
    
            Set<AbstractExpression> afterBegins
                    = new HashSet<AbstractExpression> ();
            double[][] K = getProbabilitiesComponentStateAfterBegin
                    (pairs, mainDef, postprocessor, constants, afterBegins);
    
            LinkedHashMap<GroupComponentPair, AbstractExpression> crates = new
                    LinkedHashMap<GroupComponentPair, AbstractExpression> ();
            AbstractExpressionEvaluator eval
                = postprocessor.getExpressionEvaluator
                    (statesCountExpressions, constants);
            int i = 0;
            double[][] cdf = new double[indices][];
    
            for (double s = 0; s < stopTime; s += stepSize)
            {
                double[] matchval = getStartingStates (model,  mainDef,
                        constants, postprocessor, s, crates);
                double[] times = new double[statesCountExpressions.size ()];
                Arrays.fill (times, s);
                double[] val = postprocessor.evaluateExpressionsAtTimes
                        (eval, times, constants);
    
                assignNewCounts (crates, definitionsMap, mainDef, model,
                        statesCountExpressions, mapping, matchval, val);
                postprocessor = runTheProbedSystem (model, countActionStrings,
                        false, stateObservers, statesCountExpressions, mapping,
                        mainDef, constants, stopTime, stepSize, 1);
                double[][] obtainedMeasurements
                    = postprocessor.evaluateExpressions
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
                mainCdf[t] += uncCdf[t] * stepSize;
            }
        }

        for (int t = 0; t < indices; ++t)
        {
            mainCdf[t] /= parameter;
        }

        return new CDF (mainCdf);
    }
}
