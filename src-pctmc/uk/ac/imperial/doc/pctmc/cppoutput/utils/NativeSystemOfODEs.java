package uk.ac.imperial.doc.pctmc.cppoutput.utils;

import uk.ac.imperial.doc.pctmc.odeanalysis.utils.ISystemOfODEs;

public abstract class NativeSystemOfODEs implements ISystemOfODEs
{
    public abstract double[][] solve(double[] initial, double stopTime,
                                 double stepSize, int density, double[] rates);
}
