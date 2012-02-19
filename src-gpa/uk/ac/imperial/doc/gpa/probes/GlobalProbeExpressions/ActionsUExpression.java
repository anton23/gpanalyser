package uk.ac.imperial.doc.gpa.probes.GlobalProbeExpressions;

public class ActionsUExpression extends AbstractUExpression
{
    UPrimeExpression actions;
    int times;

    public ActionsUExpression (UPrimeExpression actions, int times)
    {
        this.actions = actions;
        this.times = times;
    }

    public UPrimeExpression getActions ()
    {
        return actions;
    }

    public int getTimes ()
    {
        return times;
    }

    @Override
    public boolean equals (Object o)
    {
        if (this == o) return true;
        if (o == null || getClass () != o.getClass ()) return false;

        ActionsUExpression that = (ActionsUExpression) o;

        return (times == that.times) && actions.equals (that.actions);
    }

    @Override
    public int hashCode ()
    {
        int result = actions.hashCode ();
        result = 31 * result + times;
        return result;
    }

    @Override
    public void accept (UExpressionVisitor v, double time)
    {
        v.visit (this, time);
    }
}
