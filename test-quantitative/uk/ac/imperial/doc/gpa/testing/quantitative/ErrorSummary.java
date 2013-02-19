package uk.ac.imperial.doc.gpa.testing.quantitative;

public class ErrorSummary {
	protected double relativeAccumulated;
	protected double maxRelative;
	protected double averageRelative;
	
	public ErrorSummary(double relativeAccumulated, double maxRelative,
			double averageRelative) {
		super();
		this.relativeAccumulated = relativeAccumulated;
		this.maxRelative = maxRelative;
		this.averageRelative = averageRelative;
	}

	public double getRelativeAccumulated() {
		return relativeAccumulated;
	}

	public double getMaxRelative() {
		return maxRelative;
	}

	public double getAverageRelative() {
		return averageRelative;
	}

	public void setRelativeAccumulated(double relativeAccumulated) {
		this.relativeAccumulated = relativeAccumulated;
	}

	public void setMaxRelative(double maxRelative) {
		this.maxRelative = maxRelative;
	}

	public void setAverageRelative(double averageRelative) {
		this.averageRelative = averageRelative;
	}
}
