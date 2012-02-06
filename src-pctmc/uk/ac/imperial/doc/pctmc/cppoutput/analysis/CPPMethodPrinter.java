package uk.ac.imperial.doc.pctmc.cppoutput.analysis;

import java.util.Map;
import java.util.UUID;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.javaoutput.statements.AbstractExpressionEvaluator;
import uk.ac.imperial.doc.jexpressions.statements.AbstractStatement;
import uk.ac.imperial.doc.pctmc.cppoutput.statements.CPPStatementPrinter;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.statements.odeanalysis.EvaluatorMethod;

import com.google.common.collect.BiMap;

/**
 * Java printer for expression evaluator methods.
 *
 * @author Anton Stefanek
 *
 */
public class CPPMethodPrinter {

    private Constants constants;
    private BiMap<CombinedPopulationProduct, Integer> combinedMomentsIndex;
    private Map<AbstractExpression, Integer> generalExpectationIndex;
    public static final String PACKAGE
            = "uk.ac.imperial.doc.jexpressions.cppoutput.statements";
    private StringBuilder jniCode, classOutput;

    public CPPMethodPrinter(Constants constants,
                             BiMap<CombinedPopulationProduct, Integer> combinedMomentsIndex,
                             Map<AbstractExpression, Integer> generalExpectationIndex) {
        this.constants = constants;
        this.combinedMomentsIndex = combinedMomentsIndex;
        this.generalExpectationIndex = generalExpectationIndex;
        jniCode = new StringBuilder();
        classOutput = new StringBuilder();
    }

    public String flushClassCode() {
        String nativeClass = classOutput.toString();
        classOutput = new StringBuilder();
        return nativeClass;
    }

    public String flushNativeCode() {
        String nativeCode = jniCode.toString();
        jniCode = new StringBuilder();
        return nativeCode;
    }

    public String printEvaluatorMethod(EvaluatorMethod method, String className) {
        String nativeClassName = className + UUID.randomUUID().toString().replace("-", "");
        classOutput.append("package " + CPPMethodPrinter.PACKAGE + ";\n");
        classOutput.append("public class " + nativeClassName + " extends "
                + AbstractExpressionEvaluator.class.getName() + "{\n");
        classOutput.append("    public int getNumberOfExpressions(){\n");
        classOutput.append("      return " + method.getNumberOfExpressions() + ";\n");
        classOutput.append("    }\n");

        classOutput.append("    public native double[] update(double[] r, " +
                "double[] values, double t);\n");
        classOutput.append("private static String libName = \""
                + nativeClassName + "\";\n");
        classOutput.append("static { System.loadLibrary (libName); }\n");
        classOutput.append("}\n");

        jniCode.append("#include \"" + PACKAGE.replace(".", "_") + "_"
                + nativeClassName + ".h\"\n");
        jniCode.append("#include <cmath>\n");
        jniCode.append("#include \"src-jexpressions/uk/ac/imperial/doc/" +
                "jexpressions/cppoutput/utils/JExpressionsCPPUtils.h\"\n");
        jniCode.append("double* updateI (double *r, double *values, double t)\n{\n");
        for (AbstractStatement s : method.getBody()) {
            CPPStatementPrinter printer = new CPPStatementPrinter(
                    new CPPCombinedProductBasedExpressionPrinterFactory(
                            constants, combinedMomentsIndex,
                            generalExpectationIndex, "values"));
            s.accept(printer);
            jniCode.append("    " + printer + "\n");
        }
        jniCode.append("    return " + method.getReturnArray() + ";\n}\n");
        jniCode.append("JNIEXPORT jdoubleArray JNICALL Java_"
                + PACKAGE.replace(".", "_") + "_" + nativeClassName
                + "_update (JNIEnv *env, jobject, " +
                "jdoubleArray arr_r, jdoubleArray arr_val, jdouble t)\n{\n");
        jniCode.append("jboolean isCopy = false;\n");
        jniCode.append("jdouble * r = env -> GetDoubleArrayElements (arr_r, 0);\n");
        jniCode.append("jdouble *val = env -> GetDoubleArrayElements (arr_val, 0);\n");
        jniCode.append("jdoubleArray result = env -> NewDoubleArray ("
                + method.getNumberOfExpressions() + ");\n");
        jniCode.append("jdouble *resultRaw"
                + " = env -> GetDoubleArrayElements (result, &isCopy);\n");
        jniCode.append("double *newResult = updateI (r, val, t);\n");
        jniCode.append("for (int i = 0; i < "
                + method.getNumberOfExpressions() + "; ++i) {\n");
        jniCode.append("resultRaw[i] = newResult[i];\n");
        jniCode.append("}\n");
        jniCode.append("delete [] newResult;\n");
        jniCode.append("env -> ReleaseDoubleArrayElements" +
                " (arr_r, r, JNI_ABORT);\n");
        jniCode.append("env -> ReleaseDoubleArrayElements" +
                " (arr_val, val, JNI_ABORT);\n");
        jniCode.append("if (isCopy == JNI_TRUE) {\n");
        jniCode.append("env -> ReleaseDoubleArrayElements (result, resultRaw, 0);\n");
        jniCode.append("}\n");
        jniCode.append("return result;\n");

        jniCode.append("}\n");

        return nativeClassName;
    }
}
