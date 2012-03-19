package uk.ac.imperial.doc.gpepa.states;

import uk.ac.imperial.doc.gpepa.representation.group.GroupComponentPair;
import uk.ac.imperial.doc.pctmc.representation.State;

public class GPEPAState extends State{
	
	private GroupComponentPair pair; 

	public GroupComponentPair getPair() {
		return pair;
	}

	@Override
	public boolean equals(Object o) {
		if (o==null) return false; 
		if (!(o instanceof GPEPAState)) return false; 
		GPEPAState asS = (GPEPAState) o; 
		
		return pair.equals(asS.getPair());
	}

	@Override
	public int hashCode() {
		return pair.hashCode();
	}

	@Override
	public String toString() {
		return pair.toString();
	}

	public GPEPAState(GroupComponentPair pair) {
		super();
		this.pair = pair;
	}

}
