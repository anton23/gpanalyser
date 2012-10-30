package uk.ac.imperial.doc.gpa.plain.representation.timed;


import uk.ac.imperial.doc.jexpressions.constants.Constants;

public class DiscreteRateTimedEventUpdater extends DiscreteTimedEventUpdater {

	protected String mName;
	DiscreteRateTimedEventUpdater(double[][] values, String name) {
		super(values);
		mName = name;
	}
	
	@Override
	public void update(Constants constants, double[] popVector, double time) {
		constants.setConstantValue(mName, getValue(time));
	}
}
