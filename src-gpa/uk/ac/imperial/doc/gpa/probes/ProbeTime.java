package uk.ac.imperial.doc.gpa.probes;

public class ProbeTime
{
    private double startTime, stopTime;

    public ProbeTime (double startTime, double stopTime)
    {
        this.startTime = startTime;
        this.stopTime = stopTime;
    }

    public double getStartTime ()
    {
        return startTime;
    }

    public double getStopTime ()
    {
        return stopTime;
    }
}
