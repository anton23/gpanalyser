package uk.ac.imperial.doc.gpa.probes.GlobalProbeExpressions;

import uk.ac.imperial.doc.gpepa.states.GPEPAActionCount;

import java.util.Set;

public class UPrimeExpression extends AbstractUExpression
{
    Set<GPEPAActionCount> actions;

    public UPrimeExpression (Set<GPEPAActionCount> actions)
    {
        this.actions = actions;
    }

    public Set<GPEPAActionCount> getActions ()
    {
        return actions;
    }

    @Override
    public boolean equals (Object o)
    {
        if (this == o) return true;
        if (o == null || getClass () != o.getClass ()) return false;

        UPrimeExpression that = (UPrimeExpression) o;

        return actions.equals (that.actions);
    }

    @Override
    public int hashCode ()
    {
        return actions.hashCode ();
    }

    @Override
    public void accept (UExpressionVisitor v, double time)
    {
        accept (v, time, 1);
    }

    public void accept (UExpressionVisitor v, double time, int times)
    {
        v.visit (this, time, times);
    }
}
