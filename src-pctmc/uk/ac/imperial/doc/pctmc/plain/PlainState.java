package uk.ac.imperial.doc.pctmc.plain;

import uk.ac.imperial.doc.pctmc.representation.State;


public class PlainState extends State {
	
	private String id; 
	
	public PlainState(String id) {
		super();
		this.id = id;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		PlainState other = (PlainState) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return id;
	}

}
