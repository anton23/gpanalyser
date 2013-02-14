package uk.ac.imperial.doc.pctmc.cppoutput.utils;

import uk.ac.imperial.doc.pctmc.odeanalysis.utils.SystemOfODEs;

public abstract class NativeSystemOfODEs extends SystemOfODEs
{
    public abstract double[][] solve(double[] initial, double stopTime,
                                 double stepSize, int density, double[] rates);
}
