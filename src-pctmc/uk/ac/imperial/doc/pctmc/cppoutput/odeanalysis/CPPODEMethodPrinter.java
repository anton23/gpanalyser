package uk.ac.imperial.doc.pctmc.cppoutput.odeanalysis;

import com.google.common.collect.BiMap;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.pctmc.cppoutput.analysis.CPPStatementPrinterCombinedProductBased;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.odeanalysis.utils.NativeSystemOfODEs;
import uk.ac.imperial.doc.pctmc.statements.odeanalysis.IODEMethodVisitor;
import uk.ac.imperial.doc.pctmc.statements.odeanalysis.ODEMethod;
import uk.ac.imperial.doc.pctmc.utils.FileUtils;
import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;
import uk.ac.imperial.doc.pctmc.utils.PCTMCOptions;

import java.io.*;
import java.util.Map;
import java.util.UUID;

/**
 * CPP ODE method printer.
 * @author Anton Stefanek
 *
 */
public class CPPODEMethodPrinter implements IODEMethodVisitor {

    private Constants constants;
    private BiMap<CombinedPopulationProduct, Integer> combinedMomentsIndex;
    private Map<AbstractExpression, Integer> generalExpectationIndex;

    private int methodCharacters = 6000;
    public static final String GENERATEDCLASSNAME = "GeneratedODEs";
    public static final String PACKAGE = "uk.ac.imperial.doc.pctmc.odeanalysis.utils";
    private static final String OLDY = "y";
    private static final String NEWY = "newy";
    private String nativeClassName;
    private UUID uuid;

    private StringBuilder output, classOutput;

    public CPPODEMethodPrinter(Constants constants,
            BiMap<CombinedPopulationProduct, Integer> combinedMomentsIndex,
            Map<AbstractExpression, Integer> generalExpectationIndex) {

        this.constants = constants;
        this.combinedMomentsIndex = combinedMomentsIndex;
        this.generalExpectationIndex = generalExpectationIndex;
        output = new StringBuilder();
        classOutput = new StringBuilder();
        uuid = UUID.randomUUID();
        nativeClassName = GENERATEDCLASSNAME + uuid.toString().replace("-","");
    }

    public String getNativeClassName() {
        return nativeClassName;
    }

    public String toClassString() {
        return classOutput.toString();
    }

	@Override
	public String toString() {
		return output.toString();
	}

    @Override
    public void visit(ODEMethod s) {

        String[] cppMomentODEs = new String[s.getBody().length + 1];
        for (int i = 0; i < s.getBody().length; i++) {
            CPPStatementPrinterCombinedProductBased tmp
                    = new CPPStatementPrinterCombinedProductBased(
                    constants, combinedMomentsIndex, generalExpectationIndex,
                    OLDY, NEWY);
            s.getBody()[i].accept(tmp);
            cppMomentODEs[i] = tmp.toString() + "\n";
        }
        CPPStatementPrinterCombinedProductBased tmp
                = new CPPStatementPrinterCombinedProductBased(
                constants, combinedMomentsIndex, generalExpectationIndex, OLDY,
                NEWY);
        cppMomentODEs[s.getBody().length] = tmp.toString();

        StringBuilder header = new StringBuilder();
        StringBuilder code = new StringBuilder();
        StringBuilder jniCode = new StringBuilder();
        StringBuilder main = new StringBuilder();
        classOutput.append("package uk.ac.imperial.doc.pctmc.odeanalysis.utils;\n");
        classOutput.append("public class " + nativeClassName + " extends "
                + NativeSystemOfODEs.class.getSimpleName() + "\n{\n");
        classOutput.append("public " + nativeClassName + "() {}\n");
        classOutput.append("private static String libName = \""
                + nativeClassName + "\";\n");
        classOutput.append("static { System.loadLibrary (libName); }\n");
        classOutput.append("@Override\n");
        classOutput.append("public native double[] derivnI" +
                "(double x, double[] y, double[] r);\n");
        classOutput.append("}");
        header.append("#include \"" + PACKAGE.replace(".", "_") + "_"
                + nativeClassName + ".h\"\n");
        header.append("#include <cmath>\n");
        header.append("#include \"src-jexpressions/uk/ac/imperial/doc/" +
                "jexpressions/cppoutput/utils/JExpressionsCPPUtils.h\"\n");
        header.append(cppMomentODEs[cppMomentODEs.length - 1]);
        int line = 0;
        int method = 0;
        int nODEs = cppMomentODEs.length;
        int[] remainingCharacters = new int[nODEs + 1];
        remainingCharacters[nODEs - 1] = 0;
        for (int l = nODEs - 2; l >= 0; l--) {
            int lineLength = cppMomentODEs[l].length();
            if (lineLength > methodCharacters) {
                methodCharacters = lineLength + 1;
            }
            remainingCharacters[l] = remainingCharacters[l + 1] + lineLength;
        }
        PCTMCLogging.debug("Total characters of the generated code "
                + remainingCharacters[0]);
        PCTMCLogging.debug("Splitting code into methods.");
        while (line < nODEs - 1) {
            if (method == 0) {
                jniCode.append("JNIEXPORT jdoubleArray JNICALL " +
                        "Java_" + PACKAGE.replace(".", "_") + "_"
                        + nativeClassName + "_derivnI\n"
                        + " (JNIEnv *env, jobject, jdouble x, "
                        + "jdoubleArray arr_y, jdoubleArray arr_r) {\n");
                jniCode.append("jboolean isCopy = false;\n");
                jniCode.append("jdouble *" + OLDY + " = env -> " +
                        "GetDoubleArrayElements (arr_y, 0);\n");
                jniCode.append("jint n = env -> GetArrayLength (arr_y);\n");
                jniCode.append("jdouble *r = env -> GetDoubleArrayElements " +
                        "(arr_r, 0);\n");
                jniCode.append("jdoubleArray result = env -> NewDoubleArray (n);\n");
                jniCode.append("jdouble *" + NEWY +
                        " = env -> GetDoubleArrayElements (result, &isCopy);\n");
                jniCode.append("derivn (" + OLDY + ", x, " + NEWY + ", r);\n");
                jniCode.append("env -> ReleaseDoubleArrayElements" +
                        " (arr_y, " + OLDY + ", JNI_ABORT);\n");
                jniCode.append("env -> ReleaseDoubleArrayElements" +
                        " (arr_r, r, JNI_ABORT);\n");
                jniCode.append("if (isCopy == JNI_TRUE) {\n");
                jniCode.append("env -> ReleaseDoubleArrayElements " +
                        "(result, " + NEWY + ", 0);\n");
                jniCode.append("}\n");
                jniCode.append("return result;\n");
                jniCode.append("}\n");
                main.append("void derivn (double *" + OLDY
                        + ", double x, double *" + NEWY + ", double *r) {\n");
                //int nOdes = combinedMomentsIndex.size();
            } else {
                code.append("void derivn" + method
                        + " (double *" + OLDY + ", double x, double *"
                        + NEWY + ", double *r) {\n");
            }
            int charactersUsed = 0;
            while (line < nODEs - 1
                    && charactersUsed + cppMomentODEs[line].length()
                    < methodCharacters) {
                charactersUsed += cppMomentODEs[line].length();
                if (method == 0) {
                    main.append(cppMomentODEs[line]);
                } else {
                    code.append(cppMomentODEs[line]);
                }
                line++;
            }
            if (method != 0) {
                code.append("\n}\n");
            }
            method++;
        }

        for (int i = 1; i < method; i++) {
            main.append("derivn" + i + " (" + OLDY + ", x, " + NEWY + ", r);\n");
        }
        main.append("}\n\n");

        header.append(code);
        header.append(main);
        header.append(jniCode);
        output = header;
        PCTMCLogging.debug("The number of methods is " + method);
        if (PCTMCOptions.debug) {
            String codeFile = PCTMCOptions.debugFolder + "/odesCode";
            PCTMCLogging.debug("Writing odes code into a temporary file "
                    + codeFile);
            FileUtils.createNeededDirectories(codeFile);
            File file = new File(codeFile);
            try {
                Writer out = new BufferedWriter(new FileWriter(file));
                out.write(output.toString());
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            file = new File(PCTMCOptions.debugFolder + "/" + nativeClassName);
            try {
                Writer out = new BufferedWriter(new FileWriter(file));
                out.write(output.toString());
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String friendlyFile = PCTMCOptions.debugFolder + "/odesFriendly";
            PCTMCLogging.debug("Writing odes into a temporary file "
                    + friendlyFile);
            file = new File(friendlyFile);
            try {
                Writer out = new BufferedWriter(new FileWriter(file));
                out.write(s.toString());
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
