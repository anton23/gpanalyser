package uk.ac.imperial.doc.pctmc.cppoutput.utils;

import uk.ac.imperial.doc.pctmc.cppoutput.odeanalysis.CPPODEMethodPrinter;
import uk.ac.imperial.doc.pctmc.odeanalysis.utils.NativeSystemOfODEs;
import uk.ac.imperial.doc.pctmc.utils.FileUtils;

import javax.naming.spi.DirectoryManager;
import javax.tools.*;
import javax.tools.JavaFileObject.Kind;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.SecureClassLoader;
import java.util.ArrayList;
import java.util.List;

public class CPPClassCompiler {

    private static final String tmp = "tmp";

	public static Object getInstance(String javaCode, String className,
             String nativeCode, String nativeFile) {
		return new CPPClassCompiler().getInstancePrivate
                (javaCode, className, nativeCode, nativeFile);
	}

    private static void winCompile
            (String libName, String nativeFile, String javaInclude) {
        String command = "gcc -D_JNI_IMPLEMENTATION_ -Wl,--kill-at -shared -o "
            + libName + " " + nativeFile + ".cpp "
            + " -I\"" + javaInclude + "include\""
            + " -I\"" + javaInclude + "include/win32\"";
        ExecProcess.main(command, 2);
    }

    private static void linuxCompile
            (String libName, String nativeFile, String javaInclude) {
        String command = "gcc -shared -fPIC -o " + libName
                + " " + nativeFile + ".cpp"
                + " -I\"" + javaInclude + "include\""
                + " -I\"" + javaInclude + "include/linux\"";
        ExecProcess.main(command, 3);
    }

	private Object getInstancePrivate (String javaCode, String className,
             String nativeCode, String nativeFile) {
		
		StringBuilder src = new StringBuilder();
		src.append(javaCode);
		
		// get an instance of Java compiler
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

		List<JavaFileObject> files = new ArrayList<JavaFileObject>(1);
        JavaFileManager fileManager
            = compiler.getStandardFileManager(null, null, null);
        String filePath = CPPODEMethodPrinter.PACKAGE.replace(".", "/") ;
        String file = filePath + "/" + className;
        String fullClassName = CPPODEMethodPrinter.PACKAGE + "." + className;
		files.add(new CharSequenceJavaFileObject("src-pctmc/" + file, src));

        String outputDir = System.getProperty("user.dir") + "/" + tmp;
        File f = new File(outputDir);
        f.mkdir();
        List<String> options = new ArrayList<String>();
        options.add("-d");
        options.add(outputDir);
		compiler.getTask(null, fileManager, null, options, null, files).call();
		try {
            URL url = f.toURI().toURL();
            URLClassLoader loader = new URLClassLoader(new URL[] {url});
			Object c = loader.loadClass(fullClassName).newInstance();

            String javaHome = System.getProperty("java.home");
            int indexJre = javaHome.lastIndexOf("jre");
            String javaInclude = javaHome;
            if (indexJre >= 0)
            {
                javaInclude = javaHome.substring(0, indexJre);
            }

            String command = "javah -jni -classpath " + outputDir + " " + fullClassName;
            ExecProcess.main(command, 1);
            FileUtils.writeGeneralFile(nativeCode, nativeFile + ".cpp");
            String libName = System.mapLibraryName(nativeFile);

            if (System.getProperty("os.name").toLowerCase().contains("win"))
            {
                winCompile(libName, nativeFile, javaInclude);
            }
            else
            {
                linuxCompile(libName, nativeFile, javaInclude);
            }

            ((NativeSystemOfODEs) c).loadLib (nativeFile);
            return c;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return null;
	}

	class CharSequenceJavaFileObject extends SimpleJavaFileObject {		
		private CharSequence content;

		public CharSequenceJavaFileObject(String className, CharSequence content) {
			super(URI.create("string:///" + className.replace('.', '/')
					+ Kind.SOURCE.extension), Kind.SOURCE);
            FileUtils.writeGeneralFile(content.toString(), className + ".java");
			this.content = content;
		}

		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors) {
			return content;
		}
	}

	class JavaClassObject extends SimpleJavaFileObject {

		protected final ByteArrayOutputStream bos = new ByteArrayOutputStream();

		public JavaClassObject(String name, Kind kind) {
			super(URI.create("string:///" + name.replace('.', '/')
					+ kind.extension), kind);
		}


		public byte[] getBytes() {
			return bos.toByteArray();
		}


		@Override
		public OutputStream openOutputStream() throws IOException {
			return bos;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	class ClassFileManager extends ForwardingJavaFileManager {

		private JavaClassObject jclassObject;


		public ClassFileManager(StandardJavaFileManager standardManager) {
			super(standardManager);
		}


		@Override
		public ClassLoader getClassLoader(Location location) {
			return new SecureClassLoader() {
				@Override
				protected Class<?> findClass(String name)
						throws ClassNotFoundException {
					byte[] b = jclassObject.getBytes();
					return super.defineClass(name, jclassObject.getBytes(), 0,
							b.length);
				}
			};
		}

		
		@Override
		public JavaFileObject getJavaFileForOutput(Location location,
				String className, Kind kind, FileObject sibling)
				throws IOException {
			jclassObject = new JavaClassObject(className, kind);
			return jclassObject;
		}
	}
}
