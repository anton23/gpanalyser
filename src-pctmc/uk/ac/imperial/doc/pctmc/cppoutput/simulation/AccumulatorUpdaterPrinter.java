package uk.ac.imperial.doc.pctmc.cppoutput.simulation;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.PopulationProductExpression;
import uk.ac.imperial.doc.pctmc.simulation.PCTMCSimulation;
import uk.ac.imperial.doc.pctmc.utils.PCTMCOptions;

import java.io.File;
import java.util.Map;
import java.util.UUID;

public class AccumulatorUpdaterPrinter
{
    private Constants variables;
    private PCTMCSimulation simulation;
    public static final String GENERATEDCLASSNAME = "GeneratedAccumulator";
    public final static String PACKAGE = "uk.ac.imperial.doc.pctmc.cppoutput.simulation";
    private String nativeClassName;
    private UUID uuid;

    private StringBuilder output, classOutput;

    public AccumulatorUpdaterPrinter
            (Constants variables, PCTMCSimulation simulation) {

        this.variables = variables;
        this.simulation = simulation;
        output = new StringBuilder();
        classOutput = new StringBuilder();
        uuid = UUID.randomUUID();
        nativeClassName = GENERATEDCLASSNAME + uuid.toString().replace("-","");
        visit();
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

    private void visit() {
        StringBuilder header = new StringBuilder();
        StringBuilder jniCode = new StringBuilder();
        StringBuilder code = new StringBuilder();
        int n = simulation.getAccumulatedMomentIndex().size();
        classOutput.append("package " + PACKAGE + ";\n");
        classOutput.append("public class " + nativeClassName + " extends "
                + NativeAccumulatorUpdater.class.getSimpleName() + "\n{\n");
        classOutput.append("public " + nativeClassName + "() {}\n");
        classOutput.append("static { System.loadLibrary (\""
                + PCTMCOptions.cppFolder + "/" + nativeClassName + "\"); }\n");
        classOutput.append("@Override\n");
        classOutput.append("public native double[] updateI(double[] counts,"
                + " double delta, double[] r);\n");
        classOutput.append("}");
        header.append("#include \"" + PACKAGE.replace(".", "_") + "_"
                + nativeClassName + ".h\"\n");
        header.append("#include <cmath>\n");
        header.append("#include \"" + new File("").getAbsolutePath()
                + "/src-jexpressions/uk/ac/imperial/doc/"
                + "jexpressions/cppoutput/utils/JExpressionsCPPUtils.h\"\n");

        jniCode.append("JNIEXPORT jdoubleArray JNICALL " +
                "Java_" + PACKAGE.replace(".", "_") + "_"
                + nativeClassName + "_updateI\n"
                + " (JNIEnv *env, jobject,"
                + " jdoubleArray arr_counts, jdouble delta,"
                + " jdoubleArray arr_r) {\n");
        if (n > 0)
        {
            getCodeNonZero(jniCode, code, n);
        }
        else
        {
            jniCode.append("jdoubleArray result = env ->"
                    + " NewDoubleArray (0);\n");
            jniCode.append("return result;\n");
            jniCode.append("}\n");
        }

        output.append(header);
        output.append(code);
        output.append(jniCode);
    }

    private void getCodeNonZero(StringBuilder jniCode, StringBuilder code, int n)
    {
        jniCode.append("jdouble *counts = env -> " +
                "GetDoubleArrayElements (arr_counts, 0);\n");
        jniCode.append("jdouble *r = env -> GetDoubleArrayElements"
                + " (arr_r, 0);\n");
        jniCode.append("double *newValues = update (counts, delta, r);\n");
        jniCode.append("jdoubleArray result = env ->"
                + " NewDoubleArray (" + n + ");\n");
        jniCode.append("env -> SetDoubleArrayRegion"
                + " (result, (jsize) 0, " + n + ", (jdouble*) newValues[i]);\n");
        jniCode.append("delete [] newValues;\n");
        jniCode.append("env -> ReleaseDoubleArrayElements" +
                " (arr_counts, counts, JNI_ABORT);\n");
        jniCode.append("env -> ReleaseDoubleArrayElements" +
                " (arr_r, r, JNI_ABORT);\n");
        jniCode.append("return result;\n");
        jniCode.append("}\n");

        code.append("double* update (double *counts, double delta, double *r)\n{\n");
        code.append("double *values = new double[" + n + "];\n");
        for (Map.Entry<PopulationProduct, Integer> entry
                : simulation.getAccumulatedMomentIndex().entrySet()) {
            code.append("values[" + entry.getValue() + "] = delta * (");
            CPPPrinterPopulationBased printer = new CPPPrinterPopulationBased
                    (variables, simulation.getPCTMC().getStateIndex(),
                            simulation.getAccumulatedMomentIndex(), "counts");
            PopulationProductExpression tmp
                    = new PopulationProductExpression(entry.getKey());
            tmp.accept(printer);
            code.append(printer.toString());
            code.append(");\n");
        }

        code.append("return values;\n");
        code.append("}\n");
    }
}
