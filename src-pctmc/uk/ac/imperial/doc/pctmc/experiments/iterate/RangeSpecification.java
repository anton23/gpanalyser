package uk.ac.imperial.doc.pctmc.experiments.iterate;

import java.util.ArrayList;
import java.util.List;

public class RangeSpecification {
	private final String constant;
	private final double from;
	private final double to;
	private final int steps;
	private final double dc;

	public List<RangeSpecification> split(int n) {
		List<RangeSpecification> ret = new ArrayList<RangeSpecification>(n);
		if (n == 1) {
			ret.add(0, this);
		} else {
			int nPart = steps / n;
			int remainder = steps % n;
			double f = from;
			for (int i = 0; i<n; i++) {
				double t = f + (nPart-1)*dc;
				if (remainder>0) {
					remainder--;
					t += dc;
				}
				ret.add(i, new RangeSpecification(constant, f, t,dc));
				f = t + dc;
			}
		}
		return ret;
	}
	
	public RangeSpecification(String constant, double from, double to, int steps) {
		super();
		this.constant = constant;
		this.from = from;
		this.to = to;
		this.steps = steps;
		this.dc = (steps == 0 || from == to) ? 0.0 : ((to - from) / (steps - 1));
	}

	public RangeSpecification(String constant, double from, double to,
			double step) {
		super();
		this.constant = constant;
		this.from = from;
		this.dc = from==to ? 0.0 : step;
		this.steps = (int) Math.floor((to - from) / step) + 1;
		this.to = from + dc * (steps-1);
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
	
	public int getIndex(double value) {
		return (int)Math.floor((value - from)/dc);
	}

	public double getDc() {
		return dc;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((constant == null) ? 0 : constant.hashCode());
		long temp;
		temp = Double.doubleToLongBits(dc);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(from);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + steps;
		temp = Double.doubleToLongBits(to);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RangeSpecification other = (RangeSpecification) obj;
		if (constant == null) {
			if (other.constant != null)
				return false;
		} else if (!constant.equals(other.constant))
			return false;
		if (Double.doubleToLongBits(dc) != Double.doubleToLongBits(other.dc))
			return false;
		if (Double.doubleToLongBits(from) != Double
				.doubleToLongBits(other.from))
			return false;
		if (steps != other.steps)
			return false;
		if (Double.doubleToLongBits(to) != Double.doubleToLongBits(other.to))
			return false;
		return true;
	}
}