package uk.ac.imperial.doc.gpa.probes.GlobalProbeExpressions;

public class SequenceUExpression extends AbstractUExpression
{
    private AbstractUExpression R1;
    private AbstractUExpression R2;

    public SequenceUExpression (AbstractUExpression r1, AbstractUExpression r2)
    {
        R1 = r1;
        R2 = r2;
    }

    public AbstractUExpression getR1 ()
    {
        return R1;
    }

    public AbstractUExpression getR2 ()
    {
        return R2;
    }

    @Override
    public boolean equals (Object o)
    {
        if (this == o) return true;
        if (o == null || getClass () != o.getClass ()) return false;

        SequenceUExpression that = (SequenceUExpression) o;

        return R1.equals (that.R1) && R2.equals (that.R2);
    }

    @Override
    public int hashCode ()
    {
        int result = R1.hashCode ();
        result = 2 + 31 * result + R2.hashCode ();
        return result;
    }

    @Override
    public void accept (UExpressionVisitor v, double time)
    {
        v.visit (this, time);
    }
}
