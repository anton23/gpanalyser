package uk.ac.imperial.doc.pctmc.cppoutput.simulation;

import uk.ac.imperial.doc.pctmc.simulation.SimulationUpdater;

public abstract class NativeSimulationUpdater extends SimulationUpdater
{
    protected abstract void updateI(double[] values, double[] tmp, double[] r);

    @Override
    public void update(double[] values, double[] tmp) {
        updateI (values, tmp, r);
    }
}
