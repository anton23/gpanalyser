package uk.ac.imperial.doc.igpepa.representation.components;

import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponent;

import java.util.List;

public interface iPEPAPrefix extends iPEPA
{
    public String getAction();

    public List<String> getAllActions ();

    public PEPAComponent getContinuation ();
}
