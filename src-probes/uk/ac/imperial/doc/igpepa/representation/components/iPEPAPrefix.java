package uk.ac.imperial.doc.igpepa.representation.components;

import java.util.List;

import uk.ac.imperial.doc.gpepa.representation.components.PEPAComponent;

public interface iPEPAPrefix extends iPEPA
{
    public String getAction();

    public List<String> getAllActions ();

    public PEPAComponent getContinuation ();
}
