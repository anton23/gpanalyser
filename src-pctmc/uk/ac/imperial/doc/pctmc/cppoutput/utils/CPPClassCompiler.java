package uk.ac.imperial.doc.pctmc.cppoutput.utils;

import uk.ac.imperial.doc.pctmc.utils.FileUtils;

import javax.tools.*;
import javax.tools.JavaFileObject.Kind;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.SecureClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CPPClassCompiler {

    private static final String tmp = "tmp";
    private static URLClassLoader urlLoader;

    static {
        try {
            File currentDir = new File("./" + tmp);
            urlLoader = new URLClassLoader
                    (new URL[] {currentDir.toURI().toURL()});
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

	public static Object getInstance(String javaCode, String className,
             String nativeCode, String nativeFile, String packageName) {
		return new CPPClassCompiler().getInstancePrivate
                (javaCode, className, nativeCode, nativeFile, packageName);
	}

    private static void winCompile
            (String libName, String nativeFile, String javaInclude) {
        String command = "g++ -D_JNI_IMPLEMENTATION_ "
            + "-Wl,--kill-at -shared -Wall -O1 -o "
            + libName + " " + nativeFile + ".cpp "
            + " -I\"" + javaInclude + "include\""
            + " -I\"" + javaInclude + "include/win32\"";
/*
        String command = "cmd /K \"c:\\Program Files (x86)\\Microsoft Visual "
            + "Studio 10.0\\VC\\bin\\vcvars32.bat\" & cl.exe /Fo "
            + libName + " " + nativeFile + ".cpp "
            + " /I\"" + javaInclude + "include\""
            + " /I\"" + javaInclude + "include/win32\"";
*/
        ExecProcess.main(command, 2);
    }

    private static void linuxCompile
            (String libName, String nativeFile, String javaInclude) {
        String command = "g++ -Wall -shared -fPIC -o " + libName
            + " " + nativeFile + ".cpp"
            + " -I\"" + javaInclude + "include\""
            + " -I\"" + javaInclude + "include/linux\"";
        ExecProcess.main(command, 3);
    }

	private Object getInstancePrivate (String javaCode, String className,
             String nativeCode, String nativeFile, String packageName) {
		
		StringBuilder src = new StringBuilder();
		src.append(javaCode);
		
		// get an instance of Java compiler
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

		List<JavaFileObject> files = new ArrayList<JavaFileObject>(1);
        JavaFileManager fileManager
                = compiler.getStandardFileManager(null, null, null);
        String filePath = packageName.replace(".", "/") ;
        String file = filePath + "/" + className;
        String fullClassName = packageName + "." + className;
		files.add(new CharSequenceJavaFileObject(tmp + "/" + file, src));

        String[] options = new String[] {"-d", tmp};
		compiler.getTask(null, fileManager, null,
                Arrays.asList(options), null, files).call();
       
		try {
            String javaHome = System.getProperty("java.home");
            int indexJre = javaHome.lastIndexOf("jre");
            String javaInclude = javaHome;
            if (indexJre >= 0)
            {
                javaInclude = javaHome.substring(0, indexJre);
            }

            String command = "javah -jni -d . -classpath " + tmp + " " + fullClassName;
            ExecProcess.main(command, 1);

            File nativeFileObj =  new File(nativeFile + ".cpp");
            
            FileUtils.writeGeneralFile(nativeCode, nativeFileObj.getAbsolutePath());
            String libName = System.mapLibraryName(nativeFile);
            if (System.getProperty("os.name").toLowerCase().contains("win"))
            {
                winCompile(libName, nativeFile, javaInclude);
            }
            else
            {
                linuxCompile(libName, nativeFile, javaInclude);
            }

            // cleanup
            nativeFileObj.delete();
            File headerFileObj = new File(fullClassName.replace(".","_") + ".h");
            headerFileObj.delete();

            return urlLoader.loadClass(fullClassName).newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
        } catch (ClassNotFoundException e) {
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
}
