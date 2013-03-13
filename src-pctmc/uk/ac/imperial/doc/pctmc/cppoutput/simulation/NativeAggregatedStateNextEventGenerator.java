package uk.ac.imperial.doc.pctmc.cppoutput.simulation;

import java.util.ArrayList;
import java.util.Collection;

import uk.ac.imperial.doc.pctmc.representation.EvolutionEvent;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.representation.State;
import uk.ac.imperial.doc.pctmc.simulation.utils.AggregatedStateNextEventGenerator;

public abstract class NativeAggregatedStateNextEventGenerator
        extends AggregatedStateNextEventGenerator
{
    public void initCoefficients
            (PCTMC pctmc, Collection<EvolutionEvent> observableEvents)
    {
        int nevents = observableEvents.size();
        increasing = (ArrayList<Integer>[]) new ArrayList[nevents];
        decreasing = (ArrayList<Integer>[]) new ArrayList[nevents];
        weights = new double[nevents];
        int i = 0;

        for (EvolutionEvent e : observableEvents) {
            increasing[i] = new ArrayList<Integer>(e.getIncreasing().size());
            decreasing[i] = new ArrayList<Integer>(e.getDecreasing().size());
            for (State increasingState : e.getIncreasing()) {
                increasing[i].add(pctmc.getStateIndex().get(increasingState));
            }

            for (State decreasingState : e.getDecreasing()) {
                decreasing[i].add(pctmc.getStateIndex().get(decreasingState));
            }
            ++i;
        }
    }

    @Override
    public void initCoefficients() {
        weights = new double[weights.length];
    }

    public abstract double recalculateWeightsI
        (double[] counts, double[] weights, double[] r);

    @Override
    public void recalculateWeights(double[] counts, double t) {
        totalRate = recalculateWeightsI(counts, weights, r);
    }
}
