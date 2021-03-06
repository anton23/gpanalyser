package uk.ac.imperial.doc.pctmc.cppoutput.simulation;

import java.io.File;
import java.util.Map;
import java.util.UUID;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.pctmc.cppoutput.utils.CPPClassCompiler;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.CombinedProductExpression;
import uk.ac.imperial.doc.pctmc.simulation.PCTMCSimulation;

public class SimulationUpdaterPrinter
{
        private final Constants variables;
        private final Map<CombinedPopulationProduct, Integer> momentIndex;
        private final PCTMCSimulation simulation;
        private final Map<AbstractExpression, Integer> generalExpectationIndex;
        private static final String GENERATEDCLASSNAME = "GeneratedSimulation";
        public final static String PACKAGE = "uk.ac.imperial.doc.pctmc.cppoutput.simulation";
        private final String nativeClassName;

    private StringBuilder output, classOutput;

        public SimulationUpdaterPrinter(Constants variables,
                Map<CombinedPopulationProduct, Integer> momentIndex,
                PCTMCSimulation simulation,
                Map<AbstractExpression, Integer> generalExpectationIndex) {

            this.variables = variables;
            this.momentIndex = momentIndex;
            this.simulation = simulation;
            this.generalExpectationIndex = generalExpectationIndex;
            output = new StringBuilder();
            classOutput = new StringBuilder();
            UUID uuid = UUID.randomUUID();
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
                    + NativeSimulationUpdater.class.getSimpleName() + "\n{\n");
            classOutput.append("public " + nativeClassName + "() {}\n");
            classOutput.append("static { System.load (\""
                    + CPPClassCompiler.getNativeLibraryPath(nativeClassName) + "\"); }\n");
            classOutput.append("@Override\n");
            classOutput.append("protected native void updateI"
                    + " (double[] values, double[] tmp, double[] r);\n");
            classOutput.append("}");
            header.append("#include \"" + PACKAGE.replace(".", "_") + "_"
                    + nativeClassName + ".h\"\n");
            header.append("#include <cmath>\n");
            header.append("#include \"" + new File("").getAbsolutePath()
                    + "/src-jexpressions/uk/ac/imperial/doc/"
                    + "jexpressions/cppoutput/utils/JExpressionsCPPUtils.h\"\n");

            jniCode.append("JNIEXPORT void JNICALL " +
                    "Java_" + PACKAGE.replace(".", "_") + "_"
                    + nativeClassName + "_updateI\n"
                    + " (JNIEnv *env, jobject,"
                    + " jdoubleArray arr_values, jdoubleArray arr_tmp,"
                    + " jdoubleArray arr_r) {\n");
            jniCode.append("jdouble *values = env ->"
                    + " GetDoubleArrayElements (arr_values, 0);\n");
            jniCode.append("jdouble *tmp = env -> GetDoubleArrayElements"
                    + " (arr_tmp, 0);\n");
            jniCode.append("jdouble *r = env -> GetDoubleArrayElements"
                    + " (arr_r, 0);\n");
            jniCode.append("update (values, tmp, r);\n");
            jniCode.append("env -> ReleaseDoubleArrayElements"
                    + " (arr_values, values, 0);\n");
            jniCode.append("env -> ReleaseDoubleArrayElements"
                    + " (arr_tmp, tmp, JNI_ABORT);\n");
            jniCode.append("env -> ReleaseDoubleArrayElements"
                    + " (arr_r, r, JNI_ABORT);\n");
            jniCode.append("}\n");

            code.append("void update (double *values, double *oldValues,"
                    + " double *r)\n{\n");
            code.append("double newValues[" +
                    (momentIndex.size() + simulation.getAccumulatedMomentIndex().size()
                            + generalExpectationIndex.size()) + "];\n");

            for (Map.Entry<CombinedPopulationProduct, Integer> entry : momentIndex.entrySet()){
                code.append("newValues[" + entry.getValue() + "]=(");

                AbstractExpression tmp = CombinedProductExpression.create(entry.getKey());
                CPPPrinterPopulationBased printer = new CPPPrinterPopulationBased
                        (variables, simulation.getPCTMC().getStateIndex(),
                         simulation.getAccumulatedMomentIndex(), "oldValues");
                tmp.accept(printer);
                code.append(printer.toString());
                code.append(");\n");
                code.append("values[" + entry.getValue() + "]+= newValues[" + entry.getValue() + "];\n");

            }

            for (Map.Entry<AbstractExpression, Integer> entry : generalExpectationIndex.entrySet()){
                code.append("values[" + (momentIndex.size() + entry.getValue()) + "]+=");
                CPPPrinterPopulationBased printer =
                        new CPPPrinterPopulationBased(variables,
                                simulation.getPCTMC().getStateIndex(),
                                simulation.getAccumulatedMomentIndex(), "oldValues");
                entry.getKey().accept(printer);
                code.append(printer.toString());
                code.append(";\n");
            }
            code.append("}\n");

            output.append(header);
            output.append(code);
            output.append(jniCode);
        }
    }
