package uk.ac.imperial.doc.pctmc.cppoutput.utils;

import uk.ac.imperial.doc.pctmc.odeanalysis.utils.NativeSystemOfODEs;
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

	public static Object getInstance(String javaCode, String className,
             String nativeCode, String nativeFile) {
		return new CPPClassCompiler().getInstancePrivate
                (javaCode, className, nativeCode, nativeFile);
	}

	private Object getInstancePrivate (String javaCode, String className,
             String nativeCode, String nativeFile) {
		
		StringBuilder src = new StringBuilder();
		src.append(javaCode);
		
		// get an instance of Java compiler
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		JavaFileManager fileManager = new ClassFileManager(compiler
				.getStandardFileManager(null, null, null));

		List<JavaFileObject> files = new ArrayList<JavaFileObject>(1);
        String file = "uk/ac/imperial/doc/pctmc/odeanalysis/utils/" + className;
		files.add(new CharSequenceJavaFileObject("src-pctmc/" + file, src));

		compiler.getTask(null, fileManager, null, null, null, files).call();
		try {
			Object c = fileManager.getClassLoader(null).loadClass(file.replaceAll("/", "."))
					.newInstance();
            NativeSystemOfODEs n = (NativeSystemOfODEs) c;
            FileUtils.writeGeneralFile(nativeCode, nativeFile + ".cpp");
            String command = "javah -jni -classpath src-pctmc " + file.replaceAll("/", ".");
            ExecProcess.main(command);
            String libName = System.mapLibraryName(nativeFile);
            String javaHome = System.getenv().get("JAVA_HOME");
            String apo = "\"";

            command = "gcc -D_JNI_IMPLEMENTATION_ -Wl,--kill-at -shared -o "
                    + libName + " " + nativeFile + ".cpp "
                    + "-I" + apo + javaHome + "include" + apo + " "
                    + "-I" + apo + javaHome + "include\\win32" + apo + " ";
            ExecProcess.main(command);
            n.loadLib (nativeFile);
            return c;
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
