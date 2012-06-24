package uk.ac.imperial.doc.gpa.probes;

import uk.ac.imperial.doc.gpa.pctmc.GPEPAToPCTMC;
import uk.ac.imperial.doc.gpa.probes.GlobalProbeExpressions.AbstractUExpression;
import uk.ac.imperial.doc.gpa.probes.GlobalProbeExpressions.UExpressionVisitor;
import uk.ac.imperial.doc.gpepa.representation.components.*;
import uk.ac.imperial.doc.gpepa.representation.group.Group;
import uk.ac.imperial.doc.gpepa.representation.group.GroupComponentPair;
import uk.ac.imperial.doc.gpepa.representation.model.GroupedModel;
import uk.ac.imperial.doc.gpepa.representation.model.LabelledComponentGroup;
import uk.ac.imperial.doc.gpepa.states.GPEPAState;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.DoubleExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;
import uk.ac.imperial.doc.jexpressions.javaoutput.statements.AbstractExpressionEvaluator;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.odeanalysis.PCTMCODEAnalysis;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.CPPODEAnalysisNumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.NumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.ODEAnalysisNumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.utils.PCTMCOptions;

import java.util.*;

public class ODEProbeRunner extends AbstractProbeRunner
{
    public ODEProbeRunner ()
    {
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
         ComponentId accepting, Constants constants, double stopTime,
         double stepSize, int parameter, double steadyStateTime, String name)
    {
        // creating and running the steady-state postprocessor and evaluator
        NumericalPostprocessor postprocessor = runTheProbedSystem
            (model, mainDef, constants, null, stateObservers,
             statesCountExpressions, mapping,
             steadyStateTime, stepSize, parameter, new PCTMC[1]);
        LinkedHashMap<GroupComponentPair, AbstractExpression> crates
            = new LinkedHashMap<GroupComponentPair, AbstractExpression> ();

        // obtaining the ratios for steady state component distribution
        double[][] cratesVal = getStartingStates
            (model, mainDef, constants, postprocessor, crates);
        double[] times = new double[statesCountExpressions.size ()];
        double maxTime = steadyStateTime - stepSize;
        Arrays.fill (times, maxTime);
        AbstractExpressionEvaluator evaluator = postprocessor
            .getExpressionEvaluator (statesCountExpressions, constants);
        double[] steadyVal = postprocessor.evaluateExpressionsAtTimes
            (evaluator, times, constants);

        int sindex = (int) (maxTime / stepSize);
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
        postprocessor = runTheProbedSystem
            (model, altDef, constants, null, stateObservers,
             statesCountExpressions, mapping, stopTime, stepSize, parameter,
             new PCTMC[1]);

        double[][] obtainedMeasurements = postprocessor.evaluateExpressions
            (statesCountExpressions, constants);
        double[] cdf = passageTimeCDF (obtainedMeasurements,
                pairs, accepting, mapping);

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
        // the main postprocessor
        NumericalPostprocessor postprocessor = runTheProbedSystem
            (model, mainDef, constants, null, stateObservers,
             statesCountExpressions, mapping, steadyStateTime + stepSize,
             stepSize, parameter, new PCTMC[1]);
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

        final int times = (int) Math.ceil (stopTime / stepSize);
        final int indices = (int) Math.ceil (steadyStateTime / stepSize);
        int i = 0;
        double[][] cdf = new double[indices][];

        // obtaining the system values for various times
        statesCountExpressions.clear ();
        mapping.clear ();
        PCTMC[] pctmcs = new PCTMC[1];
        postprocessor = runTheProbedSystem
            (model, mainDef, constants, null, stateObservers,
             statesCountExpressions, mapping, stopTime,
             stepSize, parameter, pctmcs);
        evaluator = postprocessor
            .getExpressionEvaluator (statesCountExpressions, constants);

        for (double s = 0; s < steadyStateTime; s += stepSize)
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

            cdf[i] = passageTimeCDF (obtainedMeasurements,
                    pairs, accepting, mapping);
            ++i;
            System.out.println ("Ran transient iteration " + i);
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
            uncCdf[t] *= stepSize;
        }

        return new CDF (name, stepSize, uncCdf);
    }

    @Override
    protected CDF globalPassages
        (GlobalProbe gprobe, GroupedModel model, Set<GPEPAState> stateObservers,
         List<AbstractExpression> statesCountExpressions,
         Map<String, Integer> mapping, Set<String> countActions,
         ComponentId accepting, Constants constants,
         PEPAComponentDefinitions mainDef,
         double stopTime, double stepSize, int parameter)
    {
        NumericalPostprocessor postprocessor = runTheProbedSystem
            (model, mainDef, constants, countActions, stateObservers,
             statesCountExpressions, mapping, stopTime, stepSize, parameter,
             new PCTMC[1]);
        double states[][] = postprocessor.evaluateExpressions
            (statesCountExpressions, constants);
        AbstractUExpression u = gprobe.getU ();
        UExpressionVisitor visitor = new UExpressionVisitor
            (states, stopTime, stepSize, mapping);

        u.accept (visitor, 0);

        double pointMass = u.getEvaluatedTime ();
        double[] cdf = new double[(int) (stopTime / stepSize)];
        for (double time = 0; time < stopTime; time += stepSize)
        {
            int i = (int) (time / stepSize);
            cdf[i] = (time >= pointMass) ? 1 : 0;
        }
        return new CDF (gprobe.getName (), stepSize, cdf);
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
                if (prefix.getImmediates ().contains (BEGIN_SIGNAL)
                    || (prefix.getAction().equals(BEGIN_SIGNAL)
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
