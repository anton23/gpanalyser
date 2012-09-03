package uk.ac.imperial.doc.gpa.probes;

public class CDF
{
    private double[] values;
    private String name;
    private double stepSize;

    public double[] getValues ()
    {
        return values;
    }

    public CDF (String name, double stepSize, double[] values)
    {
        this.name = name;
        this.stepSize = stepSize;
        this.values = values;
    }

    @Override
    public String toString ()
    {
        StringBuilder output = new StringBuilder ();
        for (int i = 0; i < values.length; ++i)
        {
            output.append (i * stepSize);
            output.append ('\t');
            output.append (values[i]);
            output.append ('\n');
        }
        return output.toString ();
    }
}
