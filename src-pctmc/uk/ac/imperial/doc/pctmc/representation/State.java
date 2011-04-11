package uk.ac.imperial.doc.pctmc.representation;

public abstract class State {
	
	@Override 
	public abstract String toString();
	
	@Override
	public abstract int hashCode();
	
	@Override
	public abstract boolean equals(Object o);

}
