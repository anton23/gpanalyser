package uk.ac.imperial.doc.gpa.probes;

import uk.ac.imperial.doc.gpa.pctmc.GPEPAToPCTMC;
import uk.ac.imperial.doc.gpepa.representation.components.ComponentId;
import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponent;
import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponentDefinitions;
import uk.ac.imperial.doc.gpepa.representation.group.Group;
import uk.ac.imperial.doc.gpepa.representation.group.GroupComponentPair;
import uk.ac.imperial.doc.gpepa.representation.model.GroupedModel;
import uk.ac.imperial.doc.gpepa.representation.model.LabelledComponentGroup;
import uk.ac.imperial.doc.gpepa.states.GPEPAState;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.javaoutput.statements.AbstractExpressionEvaluator;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.CPPSimulationAnalysisNumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.NumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.simulation.PCTMCSimulation;

import java.util.*;

public class SimProbeRunner extends AbstractProbeRunner
{
    public SimProbeRunner ()
    {
        analysisType = PCTMCSimulation.class;
        postprocessorType = CPPSimulationAnalysisNumericalPostprocessor.class;
    }

    @Override
    protected CDF steadyIndividual
        (List<AbstractExpression> statesCountExpressions,
         Map<String, AbstractExpression> mapping,
         GroupedModel model, Set<GPEPAState> stateObservers,
         PEPAComponentDefinitions mainDef, PEPAComponentDefinitions altDef,
         Map<PEPAComponentDefinitions, Set<ComponentId>> definitionsMap,
         ComponentId accepting, Constants constants,
         double stopTime, double stepSize, int parameter,
         double steadyStateTime, String name)
    {
        double[][] overallMeasurements = null;

        // creating the steady-state postprocessor and evaluator
        NumericalPostprocessor postprocessor = runTheProbedSystem
            (model, mainDef, constants, null, stateObservers,
             statesCountExpressions, mapping, steadyStateTime + stepSize,
             stepSize, 1, new PCTMC[1]);
        double[] times = new double[statesCountExpressions.size ()];
        Arrays.fill (times, steadyStateTime);
        AbstractExpressionEvaluator evaluator = postprocessor
            .getExpressionEvaluator (statesCountExpressions, constants);

        LinkedHashMap<GroupComponentPair, AbstractExpression> crates
            = new LinkedHashMap<GroupComponentPair, AbstractExpression> ();
        getProbabilitiesAfterBegin (model, mainDef, crates);
        NumericalPostprocessor postprocessorC = runTheProbedSystem
            (model, mainDef, constants, null, stateObservers,
             statesCountExpressions, mapping, steadyStateTime + stepSize,
             stepSize, parameter, new PCTMC[1]);
        List<AbstractExpression> cratesExpr
            = new LinkedList<AbstractExpression> (crates.values ());
        AbstractExpressionEvaluator cratesEval = postprocessorC
            .getExpressionEvaluator (cratesExpr, constants);
        // obtaining the ratios for steady state component distribution
        double[] cratesVal = postprocessorC
            .evaluateExpressionsAtTimes (cratesEval, times, constants);

        // creating the absorbing postprocessor and evaluator
        Set<GroupComponentPair> pairs = model.getGroupComponentPairs (altDef);
        Set<GPEPAState> altStateObservers = new HashSet<GPEPAState> ();
        for (GroupComponentPair pair : pairs)
        {
            altStateObservers.add (new GPEPAState (pair));
        }

        List<AbstractExpression> altStatesCountExpressions
            = new ArrayList<AbstractExpression> ();
        Map<String, AbstractExpression> altMapping
            = new HashMap<String, AbstractExpression> ();
        PCTMC[] pctmc = new PCTMC[1];
        NumericalPostprocessor postprocessorA = runTheProbedSystem
            (model, altDef, constants, null, altStateObservers,
             altStatesCountExpressions, altMapping, stopTime, stepSize, 1,
             pctmc);
        AbstractExpressionEvaluator altEvaluator = postprocessorA
            .getExpressionEvaluator (altStatesCountExpressions, constants);

        // repeating the experiment
        for (int i = 0; i < parameter; ++i)
        {
            // running the steady-state repeating model
            postprocessor.calculateDataPoints (constants);
            double[] steadyVal = postprocessor.evaluateExpressionsAtTimes
                (evaluator, times, constants);

            // setting the absorbing model with new initial values
            assignNewCounts (crates, definitionsMap, mainDef, model,
                    statesCountExpressions, mapping, cratesVal, steadyVal);
            GPEPAToPCTMC.updatePCTMC (pctmc[0], altDef, model);
            postprocessorA.calculateDataPoints (constants);
            double[][] obtainedMeasurements = postprocessorA.evaluateExpressions
                (altEvaluator, constants);

            if (overallMeasurements == null)
            {
                overallMeasurements = obtainedMeasurements;
            }
            else
            {
                for (int x = 0; x < overallMeasurements.length; ++x)
                {
                    for (int y = 0; y < altStateObservers.size (); ++y)
                    {
                        overallMeasurements[x][y] += obtainedMeasurements[x][y];
                    }
                }
            }
        }

        // averaging the obtained measurements
        for (int x = 0; x < overallMeasurements.length; ++x)
        {
            for (int y = 0; y < altStateObservers.size (); ++y)
            {
                overallMeasurements[x][y] /= parameter;
            }
        }

        double[] cdf = passageTimeCDF (overallMeasurements, pairs, accepting,
                altStatesCountExpressions, altMapping);
        return new CDF (name, stepSize, cdf);
    }

    @Override
    protected CDF transientIndividual
        (List<AbstractExpression> statesCountExpressions,
         Map<String, AbstractExpression> mapping, GroupedModel model,
         Set<GPEPAState> stateObservers, PEPAComponentDefinitions mainDef,
         Map<PEPAComponentDefinitions, Set<ComponentId>> definitionsMap,
         ComponentId accepting, Constants constants, double stopTime,
         double stepSize, int parameter, double steadyStateTime, String name)
    {
        // obtaining the system values for various times
        NumericalPostprocessor postprocessor = runTheProbedSystem
            (model, mainDef, constants, null, stateObservers,
             statesCountExpressions, mapping, steadyStateTime + stepSize,
             stepSize, parameter, new PCTMC[1]);

        Set<GroupComponentPair> pairs = model.getGroupComponentPairs (mainDef);
        double[][] K = getProbabilitiesComponentStateAfterBegin
            (pairs, mainDef, postprocessor, constants);

        // obtaining the ratios for steady state component distribution
        LinkedHashMap<GroupComponentPair, AbstractExpression> crates
            = new LinkedHashMap<GroupComponentPair, AbstractExpression> ();
        double[][] matchVal = getStartingStates
            (model, mainDef, constants, postprocessor, crates);

        PCTMC[] pctmcs = new PCTMC[1];
        postprocessor = runTheProbedSystem
            (model, mainDef, constants, null, stateObservers,
             statesCountExpressions, mapping, steadyStateTime + stepSize,
             stepSize, 1, pctmcs);
        AbstractExpressionEvaluator evaluator = postprocessor
            .getExpressionEvaluator(statesCountExpressions, constants);

        final int times = (int) Math.ceil (stopTime / stepSize);
        final int indices = (int) Math.ceil (steadyStateTime / stepSize);

        double[] overallUncCdf = new double[times];

        for (int p = 0; p < parameter; ++p)
        {
            int i = 0;
            double[][] cdf = new double[indices][];

            for (double s = 0; s < steadyStateTime; s += stepSize)
            {
                postprocessor.calculateDataPoints (constants);
                double[][] transientVal = postprocessor.evaluateExpressions
                        (evaluator, constants);

                if (s > 0)
                {
                    assignNewCounts (crates, definitionsMap, mainDef, model,
                            statesCountExpressions, mapping,
                            matchVal[i], transientVal[i]);
                }

                GPEPAToPCTMC.updatePCTMC (pctmcs[0], mainDef, model);
                postprocessor.calculateDataPoints (constants);
                double[][] obtainedMeasurements = postprocessor.evaluateExpressions
                    (evaluator, constants);

                cdf[i] = passageTimeCDF (obtainedMeasurements, pairs, accepting,
                        statesCountExpressions, mapping);
                ++i;
            }

            double[] uncCdf = new double[times];
            // now integration and truncation, possible directly on obtained values
            for (int s = 0; s < indices; ++s)
            {
                final double derivK = (K[s + 1][0] - K[s][0]) / stepSize;
                for (int t = 0; t < times; ++t)
                {
                    uncCdf[t] += (cdf[s][t] * derivK);
                }
            }

            for (int t = 0; t < times; ++t)
            {
                overallUncCdf[t] += uncCdf[t] * stepSize;
            }

            System.out.println ("Ran transient replication " + p);
        }

        for (int t = 0; t < times; ++t)
        {
            overallUncCdf[t] /= parameter;
        }

        return new CDF (name, stepSize, overallUncCdf);
    }

    protected CDF globalPassages
        (GlobalProbe gprobe, GroupedModel model,
         Set<GPEPAState> stateObservers,
         List<AbstractExpression> statesCountExpressions,
         Map<String, AbstractExpression> mapping, Set<String> countActions,
         ComponentId accepting, Constants constants,
         PEPAComponentDefinitions mainDef,
         double stopTime, double stepSize, int parameter)
    {
        NumericalPostprocessor postprocessor = runTheProbedSystem (model,
                mainDef, constants, null, stateObservers,
                statesCountExpressions, mapping, stopTime, stepSize, parameter,
                new PCTMC[1]);

        double[][] values = postprocessor
            .evaluateExpressions (statesCountExpressions, constants);

        double[] cdf = passageTimeCDF (values,
                model.getGroupComponentPairs (mainDef),
                accepting, statesCountExpressions, mapping);
        return new CDF (gprobe.getName (), stepSize, cdf);
    }

    @Override
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
        double totalWeight = 0;
        List<Double> weights = new ArrayList<Double> ();
        List<GroupComponentPair> gcs = new ArrayList<GroupComponentPair> ();
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
                weights.add (matchVal[i]);
                totalWeight += matchVal[i];
                gcs.add (gc);
                newCounts.put (gc, DoubleExpression.ZERO);
            }
            else
            {
                newCounts.put (gc, new DoubleExpression
                    (origVal[statesCountExpressions.indexOf
                            (mapping.get (gc.toString ()))]));
            }
            ++i;
        }

        double pick = totalWeight * Math.random ();
        double currentWeight = 0;
        GroupComponentPair chosen = null;
        i = 0;
        for (double d : weights)
        {
            currentWeight += d;
            if (currentWeight >= pick)
            {
                chosen = gcs.get (i);
                break;
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
            if (label.equals (chosen.getGroup ()))
            {
                g.setCountExpression (chosen.getComponent (),
                        DoubleExpression.ONE);
            }
        }
    }
}
