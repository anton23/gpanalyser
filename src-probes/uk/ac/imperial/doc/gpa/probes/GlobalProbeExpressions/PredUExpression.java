package uk.ac.imperial.doc.gpa.probes.GlobalProbeExpressions;

import uk.ac.imperial.doc.gpa.fsm.NFAPredicate;

public class PredUExpression extends AbstractUExpression
{
    private final AbstractUExpression R;
    private final NFAPredicate predicate;

    public PredUExpression (AbstractUExpression r, NFAPredicate predicate)
    {
        R = r;
        this.predicate = predicate;
    }

    public AbstractUExpression getR ()
    {
        return R;
    }

    public NFAPredicate getPredicate ()
    {
        return predicate;
    }

    @Override
    public boolean equals (Object o)
    {
        if (this == o) return true;
        if (o == null || getClass () != o.getClass ()) return false;

        PredUExpression that = (PredUExpression) o;

        return R.equals (that.R) && predicate.equals (that.predicate);
    }

    @Override
    public int hashCode ()
    {
        int result = R.hashCode ();
        result = 31 * result + predicate.hashCode ();
        return result;
    }

    @Override
    public void accept (UExpressionVisitor v, double time)
    {
        v.visit (this, time);
    }
}
