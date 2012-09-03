package uk.ac.imperial.doc.gpa.fsm;

import java.util.Map;

public class NFADummyPredicate extends NFAPredicate
{
    public boolean eval(
            Map<String, Integer> mapping, double[] data
    )
    {
        return true;
    }
}
