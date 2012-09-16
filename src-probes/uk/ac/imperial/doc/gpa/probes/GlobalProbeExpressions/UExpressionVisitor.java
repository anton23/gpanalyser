package uk.ac.imperial.doc.gpa.probes.GlobalProbeExpressions;

import uk.ac.imperial.doc.gpa.fsm.NFAPredicate;
import uk.ac.imperial.doc.gpepa.states.GPEPAActionCount;

import java.util.Map;
import java.util.Set;

public class UExpressionVisitor
{
    private final double[][] states;
    private final double stopTime;
    private final double stepSize;
    private final Map<String, Integer> mapping;

    public UExpressionVisitor (double[][] states,
        double stopTime, double stepSize, Map<String, Integer> mapping)
    {
        this.states = states;
        this.stopTime = stopTime;
        this.stepSize = stepSize;
        this.mapping = mapping;
    }

    public void visit (SequenceUExpression expression, double time)
    {
        AbstractUExpression R2 = expression.getR2 ();
        R2.accept (this, time);
        AbstractUExpression R1 = expression.getR1 ();
        R1.accept (this, R2.getEvaluatedTime ());
        expression.setEvaluatedTime (R1.getEvaluatedTime ());
    }

    public void visit (BothUExpression expression, double time)
    {
        AbstractUExpression R1 = expression.getR1 ();
        R1.accept (this, time);
        AbstractUExpression R2 = expression.getR2 ();
        R2.accept (this, time);
        expression.setEvaluatedTime
            (Math.max (R1.getEvaluatedTime (), R2.getEvaluatedTime ()));
    }

    public void visit (EitherUExpression expression, double time)
    {
        AbstractUExpression R1 = expression.getR1 ();
        R1.accept (this, time);
        AbstractUExpression R2 = expression.getR2 ();
        R2.accept (this, time);
        expression.setEvaluatedTime
            (Math.min (R1.getEvaluatedTime (), R2.getEvaluatedTime ()));
    }

    public void visit (PredUExpression expression, double time)
    {
        AbstractUExpression R = expression.getR ();
        R.accept (this, time);
        expression.setEvaluatedTime
            (evalPred (expression.getPredicate(), R.getEvaluatedTime()));
    }

    public void visit (ActionsUExpression expression, double time)
    {
        UPrimeExpression uprime = expression.getActions ();
        uprime.accept (this, time, expression.getTimes ());
        time = uprime.getEvaluatedTime ();
        expression.setEvaluatedTime (time);
    }

    public void visit (UPrimeExpression expression, double time, int times)
    {
        Set<GPEPAActionCount> actions = expression.getActions ();
        if (actions.size () == 0)
        {
            expression.setEvaluatedTime (time);
        }
        else
        {
            double newTime = time + stepSize;
            while (!enoughActions (expression.getActions (),
                    newTime, time, times) && newTime <= stopTime)
            {
                newTime += stepSize;
                if (newTime >= stopTime)
                {
                    expression.setEvaluatedTime (stopTime);
                    return;
                }
            }
            expression.setEvaluatedTime (newTime);
        }
    }

    private double evalPred (NFAPredicate predicate, double startingTime)
    {
        while (!predicate.eval (mapping,
            states[getTimeIndex (startingTime)]) && startingTime <= stopTime)
        {
            startingTime += stepSize;
        }
        return startingTime;
    }

    private boolean enoughActions (Set<GPEPAActionCount> actions, double time,
            double startingTime, int n)
    {
        double atCurrent = 0, atStart = 0;
        for (GPEPAActionCount action : actions)
        {
            atCurrent += states[getTimeIndex (time)]
                    [mapping.get (action.getName ())];
            atStart += states[getTimeIndex (startingTime)]
                    [mapping.get (action.getName())];
        }

        return (atCurrent - atStart) >= n;
    }

    private int getTimeIndex (double time)
    {
        return (int) (time / stepSize);
    }
}
