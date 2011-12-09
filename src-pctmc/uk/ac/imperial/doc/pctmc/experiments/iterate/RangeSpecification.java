package uk.ac.imperial.doc.pctmc.experiments.iterate;

public class RangeSpecification {
	private String constant;
	private double from;
	private double to;
	private int steps;
	private double dc;

	public RangeSpecification(String constant, double from, double to, int steps) {
		super();
		this.constant = constant;
		this.from = from;
		this.to = to;
		this.steps = steps;
		dc = (to - from) / (steps - 1);
	}

	public RangeSpecification(String constant, double from, double to,
			double step) {
		super();
		this.constant = constant;
		this.from = from;
		this.dc = step;
		this.steps = (int) Math.ceil((to - from) / step);
		this.to = from + dc * steps;
	}

	@Override
	public String toString() {
		return constant + " from " + from + " to " + to + " in " + steps
				+ " steps";
	}

	public String getConstant() {
		return constant;
	}

	public double getFrom() {
		return from;
	}

	public double getTo() {
		return to;
	}

	public int getSteps() {
		return steps;
	}

	public double getStep(int step) {
		return from + step * dc;
	}

	public double getDc() {
		return dc;
	}
}
