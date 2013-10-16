package uk.ac.imperial.doc.gpa.probes.GlobalProbeExpressions;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.pctmc.javaoutput.utils.ClassCompiler;

public abstract class Predicate
{
    public abstract boolean eval (Map<String, Integer> mapping, double[] data);

    public static Predicate create (String newPredicateString)
    {
        if (newPredicateString.equals (""))
        {
            return new DummyPredicate();
        }
        
        StringBuilder code = new StringBuilder ();
        code.append ("import " + List.class.getName () + ";\n");
        code.append ("import " + Map.class.getName () + ";\n");
        code.append ("import " + Predicate.class.getName () + ";\n");
        code.append ("import " + AbstractExpression.class.getName () + ";\n");
        String stripped = UUID.randomUUID ().toString ().replace ("-", "");
        code.append ("public class Predicate" + stripped
            + " extends Predicate\n");
        code.append ("{\n");
        code.append ("public Predicate" + stripped + " ()\n");
        code.append ("{\n");
        //code.append ("predicateString = \""
        //    + newPredicateString.replace ("\"", "\\\"") + "\";\n");
        code.append ("}\n");
        code.append ("public boolean eval (\n" +
            "Map<String, Integer> mapping, double[] data\n" +
            ")\n");
        code.append ("{\n");
        code.append ("return " + newPredicateString + ";\n");
        code.append ("}\n");
        code.append ("}\n");

        return (Predicate) ClassCompiler.getInstance
            (code.toString (), "Predicate" + stripped);
    }
}
