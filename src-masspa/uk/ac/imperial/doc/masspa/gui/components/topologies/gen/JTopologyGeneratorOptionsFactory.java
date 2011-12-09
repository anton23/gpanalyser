package uk.ac.imperial.doc.masspa.gui.components.topologies.gen;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import uk.ac.imperial.doc.masspa.language.Messages;
import uk.ac.imperial.doc.masspa.util.MASSPALogging;
import uk.ac.imperial.doc.masspa.util.PackageInspector;

/**
 * This class scans the package
 * for all available topology
 * generators.
 * 
 * @author Chris Guenther
 */
@SuppressWarnings("unchecked")
public class JTopologyGeneratorOptionsFactory
{
	private static final String[] s_topologyGeneratorNames;
	private static final List<Class<? extends JTopologyGeneratorOptions>> s_topologyGenerators;
	
	/**
	 * Introspect package to find all generators
	 */
	static
	{
		List<String> names = new ArrayList<String>();
		List<Class<?>> classes = PackageInspector.getClasses(JTopologyGeneratorOptionsFactory.class.getPackage().getName());
		s_topologyGenerators = new ArrayList<Class<? extends JTopologyGeneratorOptions>>();
		for (Class<?> c : classes)
		{
			if (JTopologyGeneratorOptions.class.isAssignableFrom(c) &&
				!(Modifier.isAbstract(c.getModifiers())))
			{
				s_topologyGenerators.add((Class<? extends JTopologyGeneratorOptions>)c);
				names.add(c.getSimpleName());
			}
		}
		s_topologyGeneratorNames = names.toArray(new String[names.size()]);
	}
	
	/**
	 * @param _generatorName
	 * @return a new instance of {@code _generatorName}
	 */
	public static JTopologyGeneratorOptions getTopologyGen(String _generatorName)
	{
		JTopologyGeneratorOptions t = null;
		String genName = JTopologyGeneratorOptionsFactory.class.getPackage().getName()+"."+_generatorName;
		try
		{
			Class<? extends JTopologyGeneratorOptions> topologyGen = Class.forName(genName).asSubclass(JTopologyGeneratorOptions.class);
			t = topologyGen.newInstance();
		} 
		catch (ClassNotFoundException e)
		{
			MASSPALogging.error(String.format(Messages.s_LOCATION_EDITOR_TOPOLOGY_GEN_NOT_FOUND, genName));
		} 
		catch (InstantiationException e)
		{
			MASSPALogging.error(String.format(Messages.s_LOCATION_EDITOR_TOPOLOGY_GEN_INSTANTIATION_FAILED, genName));
		}
		catch (IllegalAccessException e)
		{
			MASSPALogging.error(String.format(Messages.s_LOCATION_EDITOR_TOPOLOGY_GEN_ACCESS_FAILED, genName));
		} 
		return t;
	}
	
	/**
	 * @return names of topology generators
	 */
	public static String[] getTopologyGenerators()
	{
		return s_topologyGeneratorNames;
	}
}
