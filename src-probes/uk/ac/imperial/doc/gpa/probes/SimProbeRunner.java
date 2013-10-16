package uk.ac.imperial.doc.gpa.probes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.CPPSimulationAnalysisNumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.NumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.SimulationAnalysisNumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.simulation.PCTMCSimulation;
import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;
import uk.ac.imperial.doc.pctmc.utils.PCTMCOptions;

public class SimProbeRunner extends AbstractProbeRunner
{
    public SimProbeRunner (Constants constants,
           Map<ExpressionVariable, AbstractExpression> unfoldedVariables)
    {
        super (constants, unfoldedVariables);
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
            Set<ComponentId> accepting,
            AbstractPCTMCAnalysis templateAnalysis, NumericalPostprocessor templatePostprocessor,
            double steadyStateTime, String name)
    {
        double[][] overallMeasurements = null;
        SimulationAnalysisNumericalPostprocessor simulationPostprocessor = 
        	(SimulationAnalysisNumericalPostprocessor) templatePostprocessor;
        int originalReplications = simulationPostprocessor.getReplications();
        simulationPostprocessor.setReplications(1);

        double originalStopTime = templatePostprocessor.getStopTime();
    	templatePostprocessor.setStopTime(steadyStateTime + templatePostprocessor.getStepSize());
    	
        
    	NumericalPostprocessor postprocessor = runTheProbedSystem                
            (model, mainDef, constants, null, stateObservers,
             statesCountExpressions, mapping,
             templateAnalysis, templatePostprocessor,
             new PCTMC[1]);
        AbstractExpressionEvaluator evaluator = postprocessor
            .getExpressionEvaluator (statesCountExpressions, constants);

        LinkedHashMap<GroupComponentPair, AbstractExpression> crates
            = new LinkedHashMap<GroupComponentPair, AbstractExpression> ();
        getProbabilitiesAfterBegin (model, mainDef, crates);
        double[] times = new double[crates.size ()];
        Arrays.fill (times, steadyStateTime);

        mapping.clear ();
        statesCountExpressions.clear ();
        simulationPostprocessor.setReplications(originalReplications);
        NumericalPostprocessor postprocessorC = runTheProbedSystem
            (model, mainDef, constants, null, stateObservers,
             statesCountExpressions, mapping, 
             templateAnalysis, templatePostprocessor
             , new PCTMC[1]);

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
        Map<String, Integer> altMapping = new HashMap<String, Integer> ();
        PCTMC[] pctmc = new PCTMC[1];
        simulationPostprocessor.setReplications(1);
    	templatePostprocessor.setStopTime(originalStopTime);
        NumericalPostprocessor postprocessorA = runTheProbedSystem
            (model, altDef, constants, null, altStateObservers,
             altStatesCountExpressions, altMapping, 
             templateAnalysis, templatePostprocessor,             
             pctmc);
        AbstractExpressionEvaluator altEvaluator = postprocessorA
            .getExpressionEvaluator (altStatesCountExpressions, constants);

        PCTMCLogging.setVisible (false);
        // repeating the experiment
        for (int i = 0; i < originalReplications; ++i)
        {
            // running the steady-state repeating model
            postprocessor.calculateDataPoints (constants);
            double[] steadyVal = postprocessor.evaluateExpressionsAtTimes
                (evaluator, times, constants);

            // setting the absorbing model with new initial values
            assignNewCounts (crates, definitionsMap, mainDef, model,
                   mapping, cratesVal, steadyVal);
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

            outputInfo(i, originalReplications, "steady replications");
        }

        // averaging the obtained measurements
        for (int x = 0; x < overallMeasurements.length; ++x)
        {
            for (int y = 0; y < altStateObservers.size (); ++y)
            {
                overallMeasurements[x][y] /= originalReplications;
            }
        }

        double[] cdf = passageTimeCDF (overallMeasurements, pairs, accepting,
                altMapping);
        PCTMCLogging.setVisible (true);
        return new CDF (name, templatePostprocessor.getStepSize(), cdf);
    }

    @Override
    protected CDF transientIndividual
        (List<AbstractExpression> statesCountExpressions,
         Map<String, Integer> mapping, GroupedModel model,
         Set<GPEPAState> stateObservers, PEPAComponentDefinitions mainDef,
         Map<PEPAComponentDefinitions, Set<ComponentId>> definitionsMap,
         Set<ComponentId> accepting, 
         AbstractPCTMCAnalysis templateAnalysis, NumericalPostprocessor templatePostprocessor,         
         double steadyStateTime, String name)
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
        
        SimulationAnalysisNumericalPostprocessor simulationPostprocessor = 
        	(SimulationAnalysisNumericalPostprocessor) templatePostprocessor;
        int originalReplications = simulationPostprocessor.getReplications();
        simulationPostprocessor.setReplications(1);

        double originalStopTime = templatePostprocessor.getStopTime();
    	templatePostprocessor.setStopTime(steadyStateTime);
        
    	NumericalPostprocessor postprocessor = runTheProbedSystem
            (model, mainDef, constants, beginActionCount, stateObservers,
             statesCountExpressions, mapping, 
             templateAnalysis, templatePostprocessor, new PCTMC[1]);
        AbstractExpressionEvaluator evaluator = postprocessor
            .getExpressionEvaluator (statesCountExpressions, constants);
        AbstractExpressionEvaluator beginEvaluator = postprocessor
            .getExpressionEvaluator (beginActionExpr, constants);
        PCTMC[] pctmcs = new PCTMC[1];

        List<AbstractExpression> statesCountExpressionsA
            = new ArrayList<AbstractExpression> ();
        Map<String, Integer> mappingA = new HashMap<String, Integer> ();
        // main after begin signal
    	templatePostprocessor.setStopTime(originalStopTime);

        NumericalPostprocessor postprocessorA = runTheProbedSystem
            (model, mainDef, constants, null, stateObservers,
             statesCountExpressionsA, mappingA, 
             templateAnalysis, templatePostprocessor,
             pctmcs);
        AbstractExpressionEvaluator evaluatorA = postprocessor
            .getExpressionEvaluator (statesCountExpressionsA, constants);

        double[] times = new double[statesCountExpressions.size ()];
        PCTMCLogging.setVisible (false);

        for (int p = 0; p < originalReplications; ++p)
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
                    time = i * simulationPostprocessor.getStepSize();
                    Arrays.fill (times, time);
                    break;
                }
            }

            if (i >= beginSignalled.length)
            {
                PCTMCLogging.info("No begin action occurred"
                    + " in the given time, repeating simulation " + p + ".");
                --p;
                continue;
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

            outputInfo(p, originalReplications, "transient replications");
        }

        // averaging the obtained measurements
        for (int x = 0; x < overallMeasurements.length; ++x)
        {
            for (int y = 0; y < stateObservers.size (); ++y)
            {
                overallMeasurements[x][y] /= originalReplications;
            }
        }

        double[] cdf = passageTimeCDF (overallMeasurements,
                pairs, accepting, mappingA);
        PCTMCLogging.setVisible (true);
        return new CDF (name, simulationPostprocessor.getStepSize(), cdf);
    }

    protected CDF globalPassages
        (GlobalProbe gprobe, GroupedModel model,
         Set<GPEPAState> stateObservers,
         List<AbstractExpression> statesCountExpressions,
         Map<String, Integer> mapping, Set<String> countActions,
         Set<ComponentId> accepting,
         PEPAComponentDefinitions mainDef,
         AbstractPCTMCAnalysis templateAnalysis, NumericalPostprocessor templatePostprocessor        
        )
    {
        NumericalPostprocessor postprocessor = runTheProbedSystem
            (model, mainDef, constants, null, stateObservers,
             statesCountExpressions, mapping, 
             templateAnalysis, templatePostprocessor,
             new PCTMC[1]);

        double[][] values = postprocessor
            .evaluateExpressions (statesCountExpressions, constants);

        double[] cdf = passageTimeCDF (values,
                model.getGroupComponentPairs (mainDef), accepting, mapping);
        return new CDF (gprobe.getName (), templatePostprocessor.getStepSize(), cdf);
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
                    (origVal[mapping.get (gc.toString ())]));
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

    private void assignNewCounts
            (LinkedHashMap<GroupComponentPair, AbstractExpression> crates,
             Map<PEPAComponentDefinitions, Set<ComponentId>> definitionsMap,
             PEPAComponentDefinitions definitions, GroupedModel model,
             Map<String, Integer> mapping, double[] matchVal, double[] origVal)
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
                        (origVal[mapping.get (gc.toString ())]));
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