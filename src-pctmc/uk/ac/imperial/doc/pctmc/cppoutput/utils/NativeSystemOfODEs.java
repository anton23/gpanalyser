package uk.ac.imperial.doc.pctmc.cppoutput.utils;

public abstract class NativeSystemOfODEs
{
    public abstract double[][] solve(double[] initial, double stopTime,
                                 double stepSize, int density, double[] rates);
}
