package uk.ac.imperial.doc.pctmc.simulation;

public abstract class SimulationUpdater {
	protected double[] r;

	public void setRates(double[] r) {
		this.r = r;
	}

	public abstract void update(double[] values, double[] tmp);
}
