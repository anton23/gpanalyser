package uk.ac.imperial.doc.gpa.probes.GlobalProbeExpressions;

public class BothUExpression extends AbstractUExpression
{
    private final AbstractUExpression R1;
    private final AbstractUExpression R2;

    public BothUExpression (AbstractUExpression r1, AbstractUExpression r2)
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

        BothUExpression that = (BothUExpression) o;

        return R1.equals (that.R1) && R2.equals (that.R2);
    }

    @Override
    public int hashCode ()
    {
        int result = R1.hashCode ();
        result = 3 + 31 * result + R2.hashCode ();
        return result;
    }

    @Override
    public void accept (UExpressionVisitor v, double time)
    {
        v.visit (this, time);
    }
}
