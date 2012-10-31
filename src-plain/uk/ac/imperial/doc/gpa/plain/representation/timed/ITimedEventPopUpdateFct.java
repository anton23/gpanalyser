package uk.ac.imperial.doc.gpa.plain.representation.timed;

/**
 * This interface can be used to auto-generate
 * code for modifying an array of populations
 * or population moments.
 * 
 * @author Chris Guenther
 */
public interface ITimedEventPopUpdateFct {
	void update(double[] popVector, double value);
}
