package uk.ac.imperial.doc.pctmc.representation;

public class DeterministicClock extends Clock {

	private double delay;
	
	public DeterministicClock(String name, State[] states, double[] probs, double delay) {
		super(name, states, probs);
		this.delay = delay;
	}

	@Override
	protected double sampleClock() {
		return delay;
	}
	
}