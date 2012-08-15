package uk.ac.imperial.doc.gpa.probes.GlobalProbeExpressions;

public abstract class AbstractUExpression
{
    private double evaluatedTime;

    void setEvaluatedTime (double evaluatedTime)
    {
        this.evaluatedTime = evaluatedTime;
    }

    public double getEvaluatedTime ()
    {
        return evaluatedTime;
    }

    public abstract void accept (UExpressionVisitor v, double time);

    @Override
    public abstract boolean equals (Object o);

    @Override
    public abstract int hashCode ();
}
