package uk.ac.imperial.doc.gpa.probes.GlobalProbeExpressions;

public class BasicUExpression extends AbstractUExpression
{
    @Override
    public boolean equals (Object o)
    {
        return (o instanceof BasicUExpression);
    }

    @Override
    public int hashCode ()
    {
        return 75465;
    }

    @Override
    public void accept (UExpressionVisitor v, double time)
    {
        v.visit (this, time);
    }
}
