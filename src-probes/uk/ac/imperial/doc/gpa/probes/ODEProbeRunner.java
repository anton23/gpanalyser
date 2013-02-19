package uk.ac.imperial.doc.gpa.probes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.imperial.doc.gpa.pctmc.GPEPAToPCTMC;
import uk.ac.imperial.doc.gpa.probes.GlobalProbeExpressions.AbstractUExpression;
import uk.ac.imperial.doc.gpa.probes.GlobalProbeExpressions.UExpressionVisitor;
import uk.ac.imperial.doc.gpepa.representation.components.AbstractPrefix;
import uk.ac.imperial.doc.gpepa.representation.components.ComponentId;
import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponent;
import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponentDefinitions;
import uk.ac.imperial.doc.gpepa.representation.components.Prefix;
import uk.ac.imperial.doc.gpepa.representation.group.Group;
import uk.ac.imperial.doc.gpepa.representation.group.GroupComponentPair;
import uk.ac.imperial.doc.gpepa.representation.model.GroupedModel;
import uk.ac.imperial.doc.gpepa.representation.model.LabelledComponentGroup;
import uk.ac.imperial.doc.gpepa.states.GPEPAState;
import uk.ac.imperial.doc.igpepa.representation.components.iPEPAPrefix;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;
import uk.ac.imperial.doc.jexpressions.javaoutput.statements.AbstractExpressionEvaluator;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.odeanalysis.PCTMCODEAnalysis;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.CPPODEAnalysisNumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.NumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.ODEAnalysisNumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;
import uk.ac.imperial.doc.pctmc.utils.PCTMCOptions;

public class ODEProbeRunner extends AbstractProbeRunner
{
    public ODEProbeRunner (Constants constants,
           Map<ExpressionVariable, AbstractExpression> unfoldedVariables)
    {
        super (constants, unfoldedVariables);
        analysisType = PCTMCODEAnalysis.class;
        if (PCTMCOptions.cpp)
        {
            postprocessorType = CPPODEAnalysisNumericalPostprocessor.class;
        }
        else
        {
            postprocessorType = ODEAnalysisNumericalPostprocessor.class;
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
        // creating and running the steady-state postprocessor and evaluator
    	double originalStopTime = templatePostprocessor.getStopTime();
    	templatePostprocessor.setStopTime(steadyStateTime);
    	NumericalPostprocessor postprocessor = runTheProbedSystem
            (model, mainDef, constants, null, stateObservers,
             statesCountExpressions, mapping,
             templateAnalysis,
             templatePostprocessor,
             new PCTMC[1]);
        LinkedHashMap<GroupComponentPair, AbstractExpression> crates
            = new LinkedHashMap<GroupComponentPair, AbstractExpression> ();

        // obtaining the ratios for steady state components distribution
        double[][] cratesVal = getStartingStates
            (model, mainDef, constants, postprocessor, crates);
        double[] times = new double[statesCountExpressions.size ()];
        double maxTime = steadyStateTime - templatePostprocessor.getStepSize();
        Arrays.fill (times, maxTime);
        AbstractExpressionEvaluator evaluator = postprocessor
            .getExpressionEvaluator (statesCountExpressions, constants);
        double[] steadyVal = postprocessor.evaluateExpressionsAtTimes
            (evaluator, times, constants);

        int sindex = (int) (maxTime / templatePostprocessor.getStepSize());
        // setting the absorbing model with new initial values and measuring
        assignNewCounts (crates, definitionsMap, mainDef, model,
                mapping, cratesVal[sindex], steadyVal);
        statesCountExpressions = new LinkedList<AbstractExpression> ();
        mapping = new HashMap<String, Integer> ();
        Set<GroupComponentPair> pairs = model.getGroupComponentPairs (altDef);
        stateObservers = new HashSet<GPEPAState> ();
        for (GroupComponentPair pair : pairs)
        {
            stateObservers.add (new GPEPAState (pair));
        }
        
    	templatePostprocessor.setStopTime(originalStopTime);
    	postprocessor = runTheProbedSystem
            (model, altDef, constants, null, stateObservers,
             statesCountExpressions, mapping, 
             templateAnalysis, templatePostprocessor,
             new PCTMC[1]);

        double[][] obtainedMeasurements = postprocessor.evaluateExpressions
            (statesCountExpressions, constants);
        double[] cdf = passageTimeCDF (obtainedMeasurements,
                pairs, accepting, mapping);

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
        // the main postprocessor
    	double originalStopTime = templatePostprocessor.getStopTime();
    	templatePostprocessor.setStopTime(steadyStateTime + templatePostprocessor.getStepSize());
    	
    	NumericalPostprocessor postprocessor = runTheProbedSystem
            (model, mainDef, constants, null, stateObservers,
             statesCountExpressions, mapping, 
             templateAnalysis, templatePostprocessor,
             new PCTMC[1]);
        AbstractExpressionEvaluator evaluator = postprocessor
            .getExpressionEvaluator(statesCountExpressions, constants);
        double[][] transientVal = postprocessor.evaluateExpressions
                (evaluator, constants);

        Set<GroupComponentPair> pairs = model.getGroupComponentPairs (mainDef);
        double[][] K = getProbabilitiesComponentStateAfterBegin
            (pairs, mainDef, postprocessor, constants);

        // obtaining the ratios for steady state component distribution
        LinkedHashMap<GroupComponentPair, AbstractExpression> crates
            = new LinkedHashMap<GroupComponentPair, AbstractExpression> ();
        double[][] matchVal = getStartingStates
            (model, mainDef, constants, postprocessor, crates);

        final int times = (int) Math.ceil (templatePostprocessor.getStopTime() / templatePostprocessor.getStepSize());
        final int indices = (int) Math.ceil (steadyStateTime / templatePostprocessor.getStepSize());
        int i = 0;
        double[][] cdf = new double[indices][];

        // obtaining the system values for various times
        statesCountExpressions.clear ();
        mapping.clear ();
        PCTMC[] pctmcs = new PCTMC[1];
        templatePostprocessor.setStopTime(originalStopTime);
        runTheProbedSystem
            (model, mainDef, constants, null, stateObservers,
             statesCountExpressions, mapping, 
             templateAnalysis, templatePostprocessor,
             pctmcs);
        evaluator = postprocessor
            .getExpressionEvaluator (statesCountExpressions, constants);

        PCTMCLogging.setVisible (false);
        for (double s = 0; s < steadyStateTime; s += templatePostprocessor.getStepSize())
        {
            if (s > 0)
            {
                assignNewCounts (crates, definitionsMap, mainDef, model,
                        mapping, matchVal[i], transientVal[i]);
            }

            GPEPAToPCTMC.updatePCTMC (pctmcs[0], mainDef, model);
            postprocessor.calculateDataPoints (constants);
            double[][] obtainedMeasurements = postprocessor.evaluateExpressions
                (evaluator, constants);

            outputInfo(i, indices, "transient iterations");

            cdf[i] = passageTimeCDF (obtainedMeasurements,
                    pairs, accepting, mapping);
            ++i;
        }

        double[] uncCdf = new double[times];
        // now integration and truncation, possible directly on obtained values
        for (int s = 0; s < indices; ++s)
        {
            final double derivK = (K[s + 1][0] - K[s][0]) / templatePostprocessor.getStepSize();
            for (int t = 0; t < times; ++t)
            {
                uncCdf[t] += (cdf[s][t] * derivK);
            }
        }

        for (int t = 0; t < times; ++t)
        {
            uncCdf[t] *= templatePostprocessor.getStepSize();
        }

        PCTMCLogging.setVisible (true);
        return new CDF (name, templatePostprocessor.getStepSize(), uncCdf);
    }

    @Override
    protected CDF globalPassages
        (GlobalProbe gprobe, GroupedModel model, Set<GPEPAState> stateObservers,
         List<AbstractExpression> statesCountExpressions,
         Map<String, Integer> mapping, Set<String> countActions,
         Set<ComponentId> accepting,
         PEPAComponentDefinitions mainDef,
         AbstractPCTMCAnalysis templateAnalysis, NumericalPostprocessor templatePostprocessor        
        )
    {
    	NumericalPostprocessor postprocessor = runTheProbedSystem
            (model, mainDef, constants, countActions, stateObservers,
             statesCountExpressions, mapping, 
             templateAnalysis, templatePostprocessor,             
             new PCTMC[1]);
        double states[][] = postprocessor.evaluateExpressions
            (statesCountExpressions, constants);
        AbstractUExpression u = gprobe.getU ();
        UExpressionVisitor visitor = new UExpressionVisitor
            (states, templatePostprocessor.getStopTime(), templatePostprocessor.getStepSize(), mapping);

        u.accept (visitor, 0);

        double pointMass = u.getEvaluatedTime ();
        double[] cdf = new double[(int) (templatePostprocessor.getStopTime() / templatePostprocessor.getStepSize())];

        if (pointMass != -1)
        {
            Arrays.fill(cdf, 0, (int) Math.floor(pointMass / templatePostprocessor.getStepSize()), 0);
            Arrays.fill(cdf, (int) Math.ceil(pointMass / templatePostprocessor.getStepSize()),
                    cdf.length, 1);
        }

        return new CDF (gprobe.getName (), templatePostprocessor.getStepSize(), cdf);
    }

    protected double[][] getStartingStates
            (GroupedModel model, PEPAComponentDefinitions definitions,
             Constants constants, NumericalPostprocessor postprocessor,
             LinkedHashMap<GroupComponentPair, AbstractExpression> crates)
    {
        getProbabilitiesAfterBegin (model, definitions, crates);

        List<AbstractExpression> expressions
                = new LinkedList<AbstractExpression> (crates.values ());

        return postprocessor.evaluateExpressions (expressions, constants);
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
                newCounts.put (gc, new DoubleExpression (matchVal[i]));
            }
            else
            {
                newCounts.put (gc, new DoubleExpression
                        (origVal[mapping.get(gc.toString ())]));
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
        }
    }

    // assumes probes without cycles
    protected double[][] getProbabilitiesComponentStateAfterBegin
    (Set<GroupComponentPair> pairs, PEPAComponentDefinitions definitions,
     NumericalPostprocessor postprocessor, Constants constants)
    {
        Set<PEPAComponent> afterBeginsC = new HashSet<PEPAComponent> ();
        for (GroupComponentPair hq : pairs)
        {
            Collection<AbstractPrefix> prefices
                    = hq.getComponent ().getPrefixes (definitions);
            for (AbstractPrefix prefix : prefices)
            {
                if (!(prefix instanceof iPEPAPrefix))
                {
                    throw new Error ("Fluid flow approximation of probe passage"
                        + " time running ith incompatible type of Prefix.");
                }
                if (((iPEPAPrefix)prefix).getImmediates ()
                    .contains (BEGIN_SIGNAL)
                    || (prefix.getAction().equals (BEGIN_SIGNAL)
                        && prefix instanceof Prefix))
                {
                    afterBeginsC.add (prefix.getContinuation ());
                }
            }
        }

        findClosureOnAnyActions
                (afterBeginsC, definitions, new HashSet<PEPAComponent> ());

        List<AbstractExpression> afterBegins
                = new ArrayList<AbstractExpression> ();
        for (GroupComponentPair hq : pairs)
        {
            if (afterBeginsC.contains (hq.getComponent ()))
            {
                afterBegins.add (CombinedProductExpression
                        .createMeanExpression(new GPEPAState(hq)));
            }
        }

        List<AbstractExpression> sum = new ArrayList<AbstractExpression> ();
        sum.add (SumExpression.create(afterBegins));
        return postprocessor.evaluateExpressions (sum, constants);
    }

    protected void findClosureOnAnyActions
            (Set<PEPAComponent> found, PEPAComponentDefinitions definitions,
             Set<PEPAComponent> visited)
    {
        Set<PEPAComponent> newFound = new HashSet<PEPAComponent> ();
        for (PEPAComponent c : found)
        {
            if (!visited.contains (c))
            {
                newFound.addAll
                        (findClosureOnAnyActionsI (c, definitions, visited));
            }
        }
        found.addAll (newFound);
        if (newFound.size () > 0)
        {
            findClosureOnAnyActions (found, definitions, visited);
        }
    }

    private Set<PEPAComponent> findClosureOnAnyActionsI
            (PEPAComponent c, PEPAComponentDefinitions definitions,
             Set<PEPAComponent> visited)
    {
        visited.add (c);
        Set<PEPAComponent> newFound = new HashSet<PEPAComponent> ();
        List<AbstractPrefix> prefices = c.getPrefixes (definitions);
        for (AbstractPrefix p : prefices)
        {
            newFound.add (p.getContinuation ());
        }
        return newFound;
    }
}
