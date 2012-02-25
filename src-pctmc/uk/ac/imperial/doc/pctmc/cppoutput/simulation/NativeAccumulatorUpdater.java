package uk.ac.imperial.doc.pctmc.cppoutput.simulation;

import uk.ac.imperial.doc.pctmc.simulation.utils.AccumulatorUpdater;

public abstract class NativeAccumulatorUpdater extends AccumulatorUpdater
{
    public abstract double[] updateI
            (double[] counts, double delta, double[] r);

    @Override
    public double[] update(double[] counts, double delta) {
        return updateI (counts, delta, r);
    }
}
