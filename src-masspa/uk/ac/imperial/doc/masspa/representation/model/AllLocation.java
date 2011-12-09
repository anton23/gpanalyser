package uk.ac.imperial.doc.masspa.representation.model;

public class AllLocation extends Location
{
	private static AllLocation s_instance;
	
	private AllLocation()
	{
		super();
	}

	public static AllLocation getInstance()
	{
		if (s_instance == null)
		{
			s_instance = new AllLocation();
		}
		return s_instance;
	}
	
	//**********************************************
	// Object overwrites
	//**********************************************
	@Override
	public String toString()
	{
		return "@(A)";
	}
	
	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}
	
	@Override
	public boolean equals(Object _obj)
	{
		// Since we have a singleton
		return this == _obj;
	}
}
