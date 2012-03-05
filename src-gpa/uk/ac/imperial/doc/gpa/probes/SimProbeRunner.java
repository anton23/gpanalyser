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
        List<AbstractExpression> cratesExpr
            = new LinkedList<AbstractExpression> (crates.values ());
        AbstractExpressionEvaluator cratesEval = postprocessor
            .getExpressionEvaluator(cratesExpr, constants);

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
            // obtaining the ratios for steady state component distribution
            double[] cratesVal = postprocessor
                .evaluateExpressionsAtTimes(cratesEval, times, constants);

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
         Map<String, AbstractExpression> mapping,
         GroupedModel model, Set<GPEPAState> stateObservers,
         PEPAComponentDefinitions mainDef,
         Map<PEPAComponentDefinitions, Set<ComponentId>> definitionsMap,
         ComponentId accepting, Constants constants,
         double stopTime, double stepSize, int parameter,
         double steadyStateTime, String name)
    {
        int indices = (int) Math.ceil (stopTime / stepSize);
        double[] mainCdf = new double[indices];
        Set<GroupComponentPair> pairs
            = model.getGroupComponentPairs (mainDef);

        NumericalPostprocessor postprocessorK = runTheProbedSystem
            (model, mainDef, constants, null, stateObservers,
                    new ArrayList<AbstractExpression> (),
             new HashMap<String, AbstractExpression> (),
             steadyStateTime + stepSize, stepSize, parameter,
                    new PCTMC[1]);
        double[][] K = getProbabilitiesComponentStateAfterBegin
                (pairs, mainDef, postprocessorK, constants);

        for (int p = 0; p < parameter; ++p)
        {
            NumericalPostprocessor postprocessor = runTheProbedSystem
                (model, mainDef, constants, null, stateObservers,
                        statesCountExpressions, mapping,
                 steadyStateTime + stepSize, stepSize, 1,
                        new PCTMC[1]);
            double[][] steadyVal = postprocessor.evaluateExpressions
                (statesCountExpressions, constants);

            int i = 0;
            double[][] cdf = new double[indices][];
    
            for (double s = 0; s < stopTime; s += stepSize)
            {
                assignNewCounts (mainDef, model,
                        statesCountExpressions, mapping, steadyVal[i]);
                List<AbstractExpression> statesCountExpressionsS
                    = new ArrayList<AbstractExpression> ();
                Map<String, AbstractExpression> mappingS
                    = new HashMap<String, AbstractExpression> ();
                NumericalPostprocessor postprocessorS = runTheProbedSystem
                    (model, mainDef, constants, null, stateObservers,
                            statesCountExpressionsS, mappingS,
                     stopTime, stepSize, 1,
                            new PCTMC[1]);
                double[][] obtainedMeasurements = postprocessorS
                    .evaluateExpressions (statesCountExpressionsS, constants);
                cdf[i] = passageTimeCDF (obtainedMeasurements, pairs, accepting,
                        statesCountExpressions, mapping);
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

        return new CDF (name, stepSize, mainCdf);
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

    protected void assignNewCounts
        (PEPAComponentDefinitions definitions, GroupedModel model,
         List<AbstractExpression> statesCountExpressions,
         Map<String, AbstractExpression> mapping, double[] newVal)
    {
        // setting initial number of components for the next analysis
        Map<String, LabelledComponentGroup> lgs = model.getComponentGroups ();
        for (LabelledComponentGroup lg : lgs.values ())
        {
            Group g = lg.getGroup ();
            String label = lg.getLabel ();
            for (PEPAComponent c : g.getComponentDerivatives (definitions))
            {
                g.setCountExpression (c, new DoubleExpression
                        (newVal[statesCountExpressions.indexOf
                            (mapping.get (new GroupComponentPair(label, c)
                             .toString ()))]));
            }
        }
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

        double pick = Math.random ();
        double totalWeight = 0;
        GroupComponentPair chosen = null;
        i = 0;
        for (double d : weights)
        {
            totalWeight += d;
            if (totalWeight >= pick)
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
