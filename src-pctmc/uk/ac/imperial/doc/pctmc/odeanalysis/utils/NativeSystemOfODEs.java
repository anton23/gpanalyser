package uk.ac.imperial.doc.pctmc.odeanalysis.utils;

public abstract class NativeSystemOfODEs extends SystemOfODEs {

    public abstract double[] derivnI(double x, double[] y, double[] r);

    @Override
    public double[] derivn(double x, double[] y) {
        return derivnI(x, y, r);
    }

    // Load the library
    public void loadLib(String libName) {
        try
        {
            System.loadLibrary(libName);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
