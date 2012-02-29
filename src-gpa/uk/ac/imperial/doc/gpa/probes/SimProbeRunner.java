package uk.ac.imperial.doc.gpa.probes;

import com.google.common.collect.HashBiMap;
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
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.CPPSimulationAnalysisNumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.NumericalPostprocessor;
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
                (model, mainDef, constants, null, stateObservers,
                 new ArrayList<CombinedPopulationProduct>(),
                 statesCountExpressions, mapping, steadyStateTime, stepSize, 1,
                 HashBiMap.<CombinedPopulationProduct, Integer>create ());
            double[] times = new double[statesCountExpressions.size ()];
            Arrays.fill (times, maxTime);
            AbstractExpressionEvaluator evaluator = postprocessor
                .getExpressionEvaluator (statesCountExpressions, constants);
            double[] steadyVal = postprocessor.evaluateExpressionsAtTimes
                (evaluator, times, constants);

            assignNewCounts
                (mainDef, model, statesCountExpressions, mapping, steadyVal);
            altStatesCountExpressions = new LinkedList<AbstractExpression> ();
            altMapping = new HashMap<String, AbstractExpression> ();
            postprocessor = runTheProbedSystem
                (model, altDef, constants, null, altStateObservers,
                 new ArrayList<CombinedPopulationProduct>(),
                 altStatesCountExpressions, altMapping, stopTime, stepSize, 1,
                 HashBiMap.<CombinedPopulationProduct, Integer>create ());
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
             new ArrayList<CombinedPopulationProduct>(),
             new ArrayList<AbstractExpression> (),
             new HashMap<String, AbstractExpression> (),
             steadyStateTime + stepSize, stepSize, parameter,
             HashBiMap.<CombinedPopulationProduct, Integer>create ());
        double[][] K = getProbabilitiesComponentStateAfterBegin
                (pairs, mainDef, postprocessorK, constants);

        for (int p = 0; p < parameter; ++p)
        {
            NumericalPostprocessor postprocessor = runTheProbedSystem
                (model, mainDef, constants, null, stateObservers,
                 new ArrayList<CombinedPopulationProduct>(),
                 statesCountExpressions, mapping,
                 steadyStateTime + stepSize, stepSize, 1,
                 HashBiMap.<CombinedPopulationProduct, Integer>create ());
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
                     new ArrayList<CombinedPopulationProduct>(),
                     statesCountExpressionsS, mappingS,
                     stopTime, stepSize, 1,
                     HashBiMap.<CombinedPopulationProduct, Integer>create ());
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
                mainDef, constants, countActions, stateObservers,
                new ArrayList<CombinedPopulationProduct>(),
                statesCountExpressions, mapping, stopTime, stepSize, parameter,
                HashBiMap.<CombinedPopulationProduct, Integer>create ());

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
     Map<String, AbstractExpression> mapping, double[] origVal)
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
                        (origVal[statesCountExpressions.indexOf
                                (mapping.get (new GroupComponentPair(label, c)
                                        .toString ()))]));
            }
        }
    }
}
