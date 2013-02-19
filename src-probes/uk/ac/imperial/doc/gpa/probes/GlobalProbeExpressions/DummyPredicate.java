package uk.ac.imperial.doc.gpa.probes.GlobalProbeExpressions;

import java.util.Map;

public class DummyPredicate extends Predicate
{
    public boolean eval(
            Map<String, Integer> mapping, double[] data
    )
    {
        return true;
    }
}
