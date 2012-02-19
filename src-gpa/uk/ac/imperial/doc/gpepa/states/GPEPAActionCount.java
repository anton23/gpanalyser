package uk.ac.imperial.doc.gpepa.states;

import uk.ac.imperial.doc.pctmc.representation.State;


public class GPEPAActionCount extends State{

    String name;

	public GPEPAActionCount(String name) {
		super();
		this.name = name;
	}

    public String getName() {
        return name;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 0;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public String toString() {		
		return "#"+name;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		GPEPAActionCount other = (GPEPAActionCount) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	
	
	

}
