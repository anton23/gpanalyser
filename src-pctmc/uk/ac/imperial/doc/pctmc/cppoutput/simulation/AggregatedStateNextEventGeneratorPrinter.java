package uk.ac.imperial.doc.pctmc.cppoutput.simulation;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.pctmc.representation.EvolutionEvent;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.simulation.PCTMCSimulation;
import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;
import uk.ac.imperial.doc.pctmc.utils.PCTMCOptions;

import java.io.*;
import java.util.Collection;
import java.util.UUID;

public class AggregatedStateNextEventGeneratorPrinter
{
    private Constants variables;
    private PCTMCSimulation simulation;
    private PCTMC pctmc;
    private Collection<EvolutionEvent> observableEvents;
    public static final String GENERATEDCLASSNAME = "GeneratedEventGenerator";
    public final static String PACKAGE = "uk.ac.imperial.doc.pctmc.cppoutput.simulation";
    private String nativeClassName;
    private UUID uuid;

    private StringBuilder output, classOutput;

    public AggregatedStateNextEventGeneratorPrinter
            (Constants variables, PCTMCSimulation simulation,
             PCTMC pctmc, Collection<EvolutionEvent> observableEvents) {

        this.variables = variables;
        this.simulation = simulation;
        this.pctmc = pctmc;
        this.observableEvents = observableEvents;
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
        classOutput.append("package " + PACKAGE + ";\n");
        classOutput.append("public class " + nativeClassName + " extends "
                + NativeAggregatedStateNextEventGenerator.class.getName() + "\n{\n");
        classOutput.append("public " + nativeClassName + "() {}\n");
        classOutput.append("static { System.loadLibrary (\""
                + nativeClassName + "\"); }\n");
        classOutput.append("@Override\n");
        classOutput.append("public native double recalculateWeightsI"
                + "(double[] counts, double[] weights, double[] r);\n");
        classOutput.append("}");
        header.append("#include \"" + PACKAGE.replace(".", "_") + "_"
                + nativeClassName + ".h\"\n");
        header.append("#include <cmath>\n");
        header.append("#include \"src-jexpressions/uk/ac/imperial/doc/"
                + "jexpressions/cppoutput/utils/JExpressionsCPPUtils.h\"\n");

        jniCode.append("JNIEXPORT jdouble JNICALL " +
                "Java_" + PACKAGE.replace(".", "_") + "_"
                + nativeClassName + "_recalculateWeightsI\n"
                + " (JNIEnv *env, jobject,"
                + " jdoubleArray arr_counts, jdoubleArray arr_weights,"
                + " jdoubleArray arr_r) {\n");
        jniCode.append("jdouble *counts = env -> " +
                "GetDoubleArrayElements (arr_counts, 0);\n");
        jniCode.append("jdouble *weights = env -> GetDoubleArrayElements"
                + " (arr_weights, 0);\n");
        jniCode.append("jdouble *r = env -> GetDoubleArrayElements"
                + " (arr_r, 0);\n");
        jniCode.append("double totalRate = recalculateWeights"
                + " (counts, weights, r);\n");
        jniCode.append("env -> ReleaseDoubleArrayElements" +
                " (arr_counts, counts, JNI_ABORT);\n");
        jniCode.append("env -> ReleaseDoubleArrayElements" +
                " (arr_weights, weights, 0);\n");
        jniCode.append("env -> ReleaseDoubleArrayElements" +
                " (arr_r, r, JNI_ABORT);\n");
        jniCode.append("return totalRate;\n");
        jniCode.append("}\n");

        code.append("double recalculateWeights"
                + "(double *counts, double *weights, double *r)\n{\n");
        code.append("double totalRate=0.0;\n");

        int i = 0;
        for (EvolutionEvent e : observableEvents) {
            CPPPrinterPopulationBased ratePrinter = new CPPPrinterPopulationBased(
                    variables, pctmc.getStateIndex(), simulation.getAccumulatedMomentIndex(),
                    "counts");
            e.getRate().accept(ratePrinter);
            String rate = ratePrinter.toString();
            code.append("weights[" + i + "] = " + rate + ";\n");
            code.append("totalRate += weights[" + i + "];\n");

            i++;
        }
        code.append("return totalRate;\n}\n");

        output.append(header);
        output.append(code);
        output.append(jniCode);

        if (PCTMCOptions.debug) {
            File file = new File(PCTMCOptions.debugFolder + "/codeSim");
            try {
                Writer out = new BufferedWriter(new FileWriter(file));
                out.write(code.toString());
                out.close();
            } catch (IOException e) {
                PCTMCLogging.error(e.getStackTrace().toString());
            }
        }
    }
}
