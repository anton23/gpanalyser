package uk.ac.imperial.doc.igpepa.representation.components;

import java.util.List;

public interface iPEPA
{
    public List<ImmediatePrefix> getImmediatesRaw ();

    public List<ImmediatePrefix> getImmediatesRawCopy ();

    public List<String> getImmediates ();

    public void addImmediate (ImmediatePrefix imm);

    public void addImmediates (List<ImmediatePrefix> immediates);
}
