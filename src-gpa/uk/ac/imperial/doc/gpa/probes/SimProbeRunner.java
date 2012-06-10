package uk.ac.imperial.doc.gpa.probes;

import uk.ac.imperial.doc.gpa.pctmc.GPEPAToPCTMC;
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
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.javaoutput.statements.AbstractExpressionEvaluator;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.CPPSimulationAnalysisNumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.NumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.SimulationAnalysisNumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.simulation.PCTMCSimulation;
import uk.ac.imperial.doc.pctmc.utils.PCTMCOptions;

import java.util.*;

public class SimProbeRunner extends AbstractProbeRunner
{
    public SimProbeRunner ()
    {
        analysisType = PCTMCSimulation.class;
        if (PCTMCOptions.cpp)
        {
            postprocessorType
                = CPPSimulationAnalysisNumericalPostprocessor.class;
        }
        else
        {
            postprocessorType = SimulationAnalysisNumericalPostprocessor.class;
        }
    }

    @Override
    protected CDF steadyIndividual
        (List<AbstractExpression> statesCountExpressions,
         Map<String, Integer> mapping, GroupedModel model,
         Set<GPEPAState> stateObservers, PEPAComponentDefinitions mainDef,
         PEPAComponentDefinitions altDef,
         Map<PEPAComponentDefinitions, Set<ComponentId>> definitionsMap,
         ComponentId accepting, Constants constants,
         double stopTime, double stepSize, int parameter,
         double steadyStateTime, String name)
    {
        double[][] overallMeasurements = null;
        Set<GroupComponentPair> pairs = model.getGroupComponentPairs (mainDef);

        //steady-state runner
        List<AbstractExpression> statesCountExpressionsS
                = new ArrayList<AbstractExpression> ();
        Map<String, Integer> mappingS = new HashMap<String, Integer> ();
        NumericalPostprocessor postprocessorS = runTheProbedSystem
            (model, mainDef, constants, null, stateObservers,
             statesCountExpressionsS, mappingS, steadyStateTime,
             stepSize, 1, new PCTMC[1], null);
        AbstractExpressionEvaluator evaluatorS = postprocessorS
            .getExpressionEvaluator(statesCountExpressionsS, constants);

        // we use this to measure when the begin signal fires
        Set<String> beginActionCount = new HashSet<String> ();
        beginActionCount.add (BEGIN_SIGNAL);
        List<AbstractExpression> beginActionExpr
            = new ArrayList<AbstractExpression> ();
        beginActionExpr.add (CombinedProductExpression.createMeanExpression
            (new GPEPAActionCount (BEGIN_SIGNAL)));

        PCTMC[] pctmcs = new PCTMC[1];
        // initial upto begin signal
        NumericalPostprocessor postprocessor = runTheProbedSystem
            (model, mainDef, constants, beginActionCount, stateObservers,
             statesCountExpressions, mapping, steadyStateTime,
             stepSize, 1, pctmcs, null);
        AbstractExpressionEvaluator evaluator = postprocessor
            .getExpressionEvaluator (statesCountExpressions, constants);
        AbstractExpressionEvaluator beginEvaluator = postprocessor
            .getExpressionEvaluator (beginActionExpr, constants);

        // main after begin signal
        List<AbstractExpression> statesCountExpressionsA
            = new ArrayList<AbstractExpression> ();
        Map<String, Integer> mappingA = new HashMap<String, Integer> ();
        Set<GroupComponentPair> pairsA = model.getGroupComponentPairs (altDef);
        stateObservers = new HashSet<GPEPAState> ();
        for (GroupComponentPair pair : pairsA)
        {
            stateObservers.add (new GPEPAState (pair));
        }
        PCTMC[] pctmcsA = new PCTMC[1];
        NumericalPostprocessor postprocessorA = runTheProbedSystem
            (model, altDef, constants, null, stateObservers,
             statesCountExpressionsA, mappingA, stopTime, stepSize,
             1, pctmcsA, pairs);
        AbstractExpressionEvaluator evaluatorA = postprocessorA
            .getExpressionEvaluator (statesCountExpressionsA, constants);

        double[] times = new double[statesCountExpressions.size ()];
        for (int p = 0; p < parameter; ++p)
        {
            double[] steadyVal = postprocessorS.evaluateExpressionsAtTimes
                    (evaluatorS, times, constants);

            assignNewCounts (mainDef, model, mappingS, pairs, steadyVal);
            GPEPAToPCTMC.updatePCTMC (pctmcs[0], mainDef, model);

            // detect when begin signal fired
            double time = 0;
            postprocessor.calculateDataPoints (constants);
            double[][] beginSignalled = postprocessor
                .evaluateExpressions (beginEvaluator, constants);
            int i = 0;
            for (; i < beginSignalled.length; ++i)
            {
                if (beginSignalled[i][0] == 1)
                {
                    time = i * stepSize;
                    Arrays.fill (times, time);
                    break;
                }
            }

            if (i >= beginSignalled.length)
            {
                throw new Error ("No begin action in the given time occurred.");
            }

            // calculate state of art after begin signal and rerun the model
            // with new component counts
            double[] beginVal = postprocessor.evaluateExpressionsAtTimes
                (evaluator, times, constants);

            assignNewCounts (mainDef, model, mapping, pairs, beginVal);
            GPEPAToPCTMC.updatePCTMC (pctmcsA[0], mainDef, model);
            postprocessorA.calculateDataPoints (constants);

            double[][] obtainedMeasurements = postprocessorA
                .evaluateExpressions (evaluatorA, constants);

            if (overallMeasurements == null)
            {
                overallMeasurements = obtainedMeasurements;
            }
            else
            {
                for (int x = 0; x < overallMeasurements.length; ++x)
                {
                    for (int y = 0; y < stateObservers.size (); ++y)
                    {
                        overallMeasurements[x][y] += obtainedMeasurements[x][y];
                    }
                }
            }

            System.out.println ("Ran steady replication " + p);
        }

        // averaging the obtained measurements
        for (int x = 0; x < overallMeasurements.length; ++x)
        {
            for (int y = 0; y < stateObservers.size (); ++y)
            {
                overallMeasurements[x][y] /= parameter;
            }
        }

        double[] cdf = passageTimeCDF (overallMeasurements,
                pairsA, accepting, mappingA);
        return new CDF (name, stepSize, cdf);
    }

    @Override
    protected CDF transientIndividual
        (List<AbstractExpression> statesCountExpressions,
         Map<String, Integer> mapping, GroupedModel model,
         Set<GPEPAState> stateObservers, PEPAComponentDefinitions mainDef,
         Map<PEPAComponentDefinitions, Set<ComponentId>> definitionsMap,
         ComponentId accepting, Constants constants, double stopTime,
         double stepSize, int parameter, double steadyStateTime, String name)
    {
        double[][] overallMeasurements = null;
        Set<GroupComponentPair> pairs = model.getGroupComponentPairs (mainDef);

        // we use this to measure when the begin signal fires
        Set<String> beginActionCount = new HashSet<String> ();
        beginActionCount.add (BEGIN_SIGNAL);
        List<AbstractExpression> beginActionExpr
            = new ArrayList<AbstractExpression> ();
        beginActionExpr.add (CombinedProductExpression.createMeanExpression
            (new GPEPAActionCount (BEGIN_SIGNAL)));

        // initial upto begin signal
        NumericalPostprocessor postprocessor = runTheProbedSystem
            (model, mainDef, constants, beginActionCount, stateObservers,
             statesCountExpressions, mapping, steadyStateTime,
             stepSize, 1, new PCTMC[1], null);
        AbstractExpressionEvaluator evaluator = postprocessor
            .getExpressionEvaluator (statesCountExpressions, constants);
        AbstractExpressionEvaluator beginEvaluator = postprocessor
            .getExpressionEvaluator (beginActionExpr, constants);
        PCTMC[] pctmcs = new PCTMC[1];

        List<AbstractExpression> statesCountExpressionsA
            = new ArrayList<AbstractExpression> ();
        Map<String, Integer> mappingA = new HashMap<String, Integer> ();
        // main after begin signal
        NumericalPostprocessor postprocessorA = runTheProbedSystem
            (model, mainDef, constants, null, stateObservers,
             statesCountExpressionsA, mappingA, stopTime, stepSize,
             1, pctmcs, null);
        AbstractExpressionEvaluator evaluatorA = postprocessor
            .getExpressionEvaluator (statesCountExpressionsA, constants);

        double[] times = new double[statesCountExpressions.size ()];
        for (int p = 0; p < parameter; ++p)
        {
            // detect when begin signal fired
            double time = 0;
            postprocessor.calculateDataPoints (constants);
            double[][] beginSignalled = postprocessor
                .evaluateExpressions (beginEvaluator, constants);
            int i = 0;
            for (; i < beginSignalled.length; ++i)
            {
                if (beginSignalled[i][0] == 1)
                {
                    time = i * stepSize;
                    Arrays.fill (times, time);
                    break;
                }
            }

            if (i >= beginSignalled.length)
            {
                throw new Error ("No begin action in the given time occurred.");
            }

            // calculate state of art after begin signal and rerun the model
            // with new component counts
            double[] transientVal = postprocessor.evaluateExpressionsAtTimes
                (evaluator, times, constants);

            assignNewCounts (mainDef, model, mapping, pairs, transientVal);
            GPEPAToPCTMC.updatePCTMC (pctmcs[0], mainDef, model);
            postprocessorA.calculateDataPoints (constants);

            double[][] obtainedMeasurements = postprocessorA
                .evaluateExpressions (evaluatorA, constants);

            if (overallMeasurements == null)
            {
                overallMeasurements = obtainedMeasurements;
            }
            else
            {
                for (int x = 0; x < overallMeasurements.length; ++x)
                {
                    for (int y = 0; y < stateObservers.size (); ++y)
                    {
                        overallMeasurements[x][y] += obtainedMeasurements[x][y];
                    }
                }
            }

            System.out.println ("Ran transient replication " + p);
        }

        // averaging the obtained measurements
        for (int x = 0; x < overallMeasurements.length; ++x)
        {
            for (int y = 0; y < stateObservers.size (); ++y)
            {
                overallMeasurements[x][y] /= parameter;
            }
        }

        double[] cdf = passageTimeCDF (overallMeasurements,
                pairs, accepting, mappingA);
        return new CDF (name, stepSize, cdf);
    }

    protected CDF globalPassages
        (GlobalProbe gprobe, GroupedModel model,
         Set<GPEPAState> stateObservers,
         List<AbstractExpression> statesCountExpressions,
         Map<String, Integer> mapping, Set<String> countActions,
         ComponentId accepting, Constants constants,
         PEPAComponentDefinitions mainDef,
         double stopTime, double stepSize, int parameter)
    {
        NumericalPostprocessor postprocessor = runTheProbedSystem
            (model, mainDef, constants, null, stateObservers,
             statesCountExpressions, mapping, stopTime, stepSize, parameter,
             new PCTMC[1], null);

        double[][] values = postprocessor
            .evaluateExpressions (statesCountExpressions, constants);

        double[] cdf = passageTimeCDF (values,
                model.getGroupComponentPairs (mainDef), accepting, mapping);
        return new CDF (gprobe.getName (), stepSize, cdf);
    }

    private void assignNewCounts
        (PEPAComponentDefinitions definitions, GroupedModel model,
         Map<String, Integer> mapping, Set<GroupComponentPair> pairs,
         double[] origVal)
    {
        Map<GroupComponentPair, AbstractExpression> newCounts
                = new HashMap<GroupComponentPair, AbstractExpression> ();
        for (GroupComponentPair gc : pairs)
        {
            newCounts.put (gc, new DoubleExpression
                    (origVal[mapping.get(gc.toString ())]));
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
}
