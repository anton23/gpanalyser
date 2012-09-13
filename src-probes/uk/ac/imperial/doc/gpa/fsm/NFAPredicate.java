package uk.ac.imperial.doc.gpa.fsm;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.pctmc.javaoutput.utils.ClassCompiler;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class NFAPredicate
{
    protected final String predicateString = "";

    public abstract boolean eval (Map<String, Integer> mapping, double[] data);

    public String getPredicateString ()
    {
        return predicateString;
    }

    public static NFAPredicate create (String newPredicateString)
    {
        if (newPredicateString.equals (""))
        {
            return new NFADummyPredicate ();
        }
        
        StringBuilder code = new StringBuilder ();
        code.append ("import " + List.class.getName () + ";\n");
        code.append ("import " + Map.class.getName () + ";\n");
        code.append ("import " + NFAPredicate.class.getName () + ";\n");
        code.append ("import " + AbstractExpression.class.getName () + ";\n");
        String stripped = UUID.randomUUID().toString().replace ("-", "");
        code.append ("public class NFAPredicate" + stripped
            + " extends NFAPredicate\n");
        code.append ("{\n");
        code.append ("public NFAPredicate" + stripped + " ()\n");
        code.append ("{\n");
        code.append ("predicateString = \""
            + newPredicateString.replace ("\"", "\\\"") + "\";\n");
        code.append ("}\n");
        code.append ("public boolean eval (\n" +
            "List<AbstractExpression> statesCountExpressions,\n" +
            "Map<String, AbstractExpression> mapping, double[] data\n" +
            ")\n");
        code.append ("{\n");
        code.append ("return " + newPredicateString + ";\n");
        code.append ("}\n");
        code.append ("}\n");

        return (NFAPredicate) ClassCompiler.getInstance
            (code.toString (), "NFAPredicate" + stripped);
    }

    @Override
    public boolean equals (Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass () != o.getClass ())
        {
            return false;
        }

        NFAPredicate that = (NFAPredicate) o;
        return predicateString.equals (that.predicateString);
    }

    @Override
    public int hashCode ()
    {
        return predicateString.hashCode ();
    }
}
