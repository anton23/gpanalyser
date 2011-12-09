package uk.ac.imperial.doc.masspa.gui.util;

public class MathExtra
{
	public static double round(double _num, int _decimalPlaces)
	{
		double dec = Math.pow(10, _decimalPlaces);
		return ((double)Math.round(_num*dec))/dec;
	}
}
