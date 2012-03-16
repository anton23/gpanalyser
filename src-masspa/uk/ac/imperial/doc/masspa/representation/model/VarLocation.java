package uk.ac.imperial.doc.masspa.representation.model;

public class VarLocation extends Location
{
	private static VarLocation s_instance;
	
	private VarLocation()
	{
		super();
	}

	public static VarLocation getInstance()
	{
		if (s_instance == null)
		{
			s_instance = new VarLocation();
		}
		return s_instance;
	}
	
	//**********************************************
	// Object overwrites
	//**********************************************
	@Override
	public String toString()
	{
		return "@(x)";
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
