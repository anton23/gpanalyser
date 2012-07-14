package uk.ac.imperial.doc.pctmc.representation;

//clock for generalised transitions in PGSMPs

public abstract class Clock {
	
	protected String clockName;
	
	//set of states in which clock remains active 
	protected State[] activeStates;
	
	//transition probabilities to determine next state when clock expires
	protected double[] transitionProbs;
	
	//current clock time
	protected double remainingTime;
	
	protected Clock(String name, State[] states, double[] probs) {
		clockName = name;
		activeStates = states;
		transitionProbs = probs;
	};
	
	//sample remaining time from particular CDF to start clock
	//(specific to type of transition, e.g. gamma/beta/etc)
	protected abstract double sampleClock();
	
	public void setClock() {
		remainingTime = sampleClock();
	}
	
	public double getClock() {
		return remainingTime;
	}

	public void advanceClock(double time) {
		remainingTime -= time;
	}
	
	//need transition method for when clock reaches zero
	//should return state randomly using transition probs
}