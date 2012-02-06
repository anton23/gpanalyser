package uk.ac.imperial.doc.pctmc.cppoutput.utils;

import uk.ac.imperial.doc.pctmc.utils.FileUtils;

import javax.tools.*;
import javax.tools.JavaFileObject.Kind;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.security.SecureClassLoader;
import java.util.ArrayList;
import java.util.List;

public class CPPClassCompiler {

    private static final String srcpctmc = "src-pctmc";

	public static Object getInstance(String javaCode, String className,
             String nativeCode, String nativeFile, String packageName) {
		return new CPPClassCompiler().getInstancePrivate
                (javaCode, className, nativeCode, nativeFile, packageName);
	}

    private static void winCompile
            (String libName, String nativeFile, String javaInclude) {
        String command = "g++ -D_JNI_IMPLEMENTATION_ "
            + "-Wl,--kill-at -shared -Wall -o "
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
        String command = "gcc -Wall -shared -fPIC -o " + libName
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
        JavaFileManager fileManager = new ClassFileManager
                (compiler.getStandardFileManager(null, null, null));
        String filePath = packageName.replace(".", "/") ;
        String file = filePath + "/" + className;
        String fullClassName = packageName + "." + className;
		files.add(new CharSequenceJavaFileObject(srcpctmc + "/" + file, src));

		compiler.getTask(null, fileManager, null, null, null, files).call();
		try {
            String javaHome = System.getProperty("java.home");
            int indexJre = javaHome.lastIndexOf("jre");
            String javaInclude = javaHome;
            if (indexJre >= 0)
            {
                javaInclude = javaHome.substring(0, indexJre);
            }

            String command = "javah -jni -classpath " + srcpctmc + " " + fullClassName;
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

            return fileManager.getClassLoader(null)
                    .loadClass(fullClassName).newInstance();
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
