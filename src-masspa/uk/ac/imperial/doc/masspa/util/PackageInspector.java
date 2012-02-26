package uk.ac.imperial.doc.masspa.util;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * 
 * @see http://snippets.dzone.com/user/vtatai
 */
public class PackageInspector
{
	/**
	 * Find all class names available in {@code _packageName}
	 * @param _packageName The name of the base package
	 * @return list of classes in package
	 */
	public static List<Class<?>> getClasses(String _packageName)
	{
		List<String> classNames = getClassesLocal(_packageName);;
		if (PackageInspector.class.getResource("PackageInspector.class").toString().startsWith("jar"))
		{
			classNames = getClassesJar(_packageName);
		}

		List<Class<?>> classes = new ArrayList<Class<?>>();
		for (String s : classNames)
		{
			try
			{
				classes.add(Class.forName(s));
			} 
			catch (ClassNotFoundException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return classes;
	}
	
    /**
     * Scans all jar classes accessible from the context class loader which belong to the given package and subpackages.
     *
     * @param _packageName The name of the base package
     * @return list of class names in package
     */
	private static List<String> getClassesJar(String _packageName)
	{
		Class<?> context = PackageInspector.class;
		URL location = context.getResource("/" + context.getName().replace(".", "/") + ".class");
		String jarPath = location.getPath();
		jarPath = jarPath.substring("file:".length(), jarPath.lastIndexOf("!"));	
		ArrayList<String> classes = new ArrayList<String> ();
		_packageName = _packageName.replaceAll("\\." , "/");
		try
		{
			JarInputStream jarFile = new JarInputStream(new FileInputStream(jarPath));
			JarEntry jarEntry;

			while(true)
			{
				jarEntry=jarFile.getNextJarEntry();
				if(jarEntry == null)
				{
					break;
				}
				else if((jarEntry.getName().startsWith(_packageName)) && (jarEntry.getName().endsWith(".class")))
				{
					classes.add(jarEntry.getName().replaceAll("/", "\\.").replace(".class", ""));
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace ();
		}
		return classes;
	}

	
    /**
     * Scans all local classes accessible from the context class loader which belong to the given package and subpackages.
     *
     * @param packageName The name of the base package
     * @return list of class names in package
     */
    private static List<String> getClassesLocal(String _packageName)
    {
        ArrayList<String> classes = new ArrayList<String>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = _packageName.replace('.', '/');
        try
        {
	        Enumeration<URL> resources = classLoader.getResources(path);
	        List<File> dirs = new ArrayList<File>();
	        while (resources.hasMoreElements())
	        {
	            URL resource = resources.nextElement();
	            dirs.add(new File(resource.getFile()));
	        }
	        for (File directory : dirs)
	        {
	            classes.addAll(findClassesLocal(directory, _packageName));
	        }
        }
        catch(Exception e)
        {
        	
        }
        return classes;
    }

    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     *
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    private static List<String> findClassesLocal(File directory, String _packageName) throws ClassNotFoundException
    {
        List<String> classes = new ArrayList<String>();
        if (!directory.exists())
        {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files)
        {
            if (file.isDirectory())
            {
                assert !file.getName().contains(".");
                classes.addAll(findClassesLocal(file, _packageName + "." + file.getName()));
            }
            else if (file.getName().endsWith(".class"))
            {
                classes.add(_packageName + '.' + file.getName().substring(0, file.getName().length() - 6));
            }
        }
        return classes;
    }

}
