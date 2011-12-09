package uk.ac.imperial.doc.masspa.gui.components.channels.gen;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import uk.ac.imperial.doc.masspa.gui.models.ObservableAgents;
import uk.ac.imperial.doc.masspa.gui.models.topologies.Topology;
import uk.ac.imperial.doc.masspa.language.Messages;
import uk.ac.imperial.doc.masspa.util.MASSPALogging;
import uk.ac.imperial.doc.masspa.util.PackageInspector;

/**
 * This class dynamically generates option bars
 * for specific types of channel generators.
 *
 * @author Chris Guenther
 */
@SuppressWarnings("unchecked")
public class JChannelGeneratorOptionsFactory
{
	private static final List<Class<? extends JChannelGeneratorOptions>> s_channelGenerators;
	
	/**
	 * Introspect package to find all generators
	 */
	static
	{
		List<Class<?>> classes = PackageInspector.getClasses(JChannelGeneratorOptionsFactory.class.getPackage().getName());
		s_channelGenerators = new ArrayList<Class<? extends JChannelGeneratorOptions>>();
		for (Class<?> c : classes)
		{
			if (JChannelGeneratorOptions.class.isAssignableFrom(c) &&
				!(Modifier.isAbstract(c.getModifiers())))
			{
				s_channelGenerators.add((Class<? extends JChannelGeneratorOptions>)c);
			}
		}
	}

	/**
	 * @param _generatorName
	 * @return a new instance of {@code _generatorName}
	 */
	public static JChannelGeneratorOptions getChannelGen(String _generatorName, ObservableAgents _agents)
	{
		JChannelGeneratorOptions t = null;
		String genName = JChannelGeneratorOptionsFactory.class.getPackage().getName()+"."+_generatorName;
		try
		{
			Class<? extends JChannelGeneratorOptions> channelGen = Class.forName(genName).asSubclass(JChannelGeneratorOptions.class);
			Constructor<? extends JChannelGeneratorOptions> con = channelGen.getConstructor(new Class[]{ObservableAgents.class});
			t = con.newInstance(_agents);
		} 
		catch (ClassNotFoundException e)
		{
			MASSPALogging.error(String.format(Messages.s_CHANNEL_EDITOR_CHANNEL_GEN_NOT_FOUND, genName));
		} 
		catch (IllegalAccessException e)
		{
			MASSPALogging.error(String.format(Messages.s_CHANNEL_EDITOR_CHANNEL_GEN_ACCESS_FAILED, genName));
		}
		catch (Exception e)
		{
			MASSPALogging.error(String.format(Messages.s_CHANNEL_EDITOR_CHANNEL_GEN_INSTANTIATION_FAILED, genName));
		}
		return t;
	}
	
	/**
	 * @param _topo Topology to be used for channel generation
	 * @return names of available topology generators
	 */
	public static String[] getChannelGenerators(Topology _topo)
	{
		if (_topo==null) {return new String[0];}

		// Filter out generators that are not applicable for the current topology _type
		List<String> names = new ArrayList<String>();
		for (Class<? extends JChannelGeneratorOptions> c : s_channelGenerators)
		{
			String name = c.getSimpleName();
			if (getChannelGen(name,null).acceptsTopology(_topo))
			{
				names.add(name);
			}
		}
		
		return names.toArray(new String[names.size()]);
	}
}
