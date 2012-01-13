package uk.ac.imperial.doc.pctmc.javaoutput.odeanalysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.javaoutput.JavaExpressionPrinterWithVariables;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.javaoutput.analysis.JavaPrinterCombinedProductBased;
import uk.ac.imperial.doc.pctmc.javaoutput.analysis.JavaStatementPrinterCombinedProductBased;
import uk.ac.imperial.doc.pctmc.odeanalysis.utils.SystemOfODEs;
import uk.ac.imperial.doc.pctmc.statements.odeanalysis.IODEMethodVisitor;
import uk.ac.imperial.doc.pctmc.statements.odeanalysis.ODEMethod;
import uk.ac.imperial.doc.pctmc.utils.FileUtils;
import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;
import uk.ac.imperial.doc.pctmc.utils.PCTMCOptions;

import com.google.common.collect.BiMap;

/**
 * Java ODE method printer.
 * 
 * @author Anton Stefanek
 * 
 */
public class JavaODEMethodPrinter implements IODEMethodVisitor {

	private Constants constants;
	private BiMap<CombinedPopulationProduct, Integer> combinedMomentsIndex;
	private Map<AbstractExpression, Integer> generalExpectationIndex;

	private int methodCharacters = 6000;
	public static final String GENERATEDCLASSNAME = "GeneratedODEs";
	private static final String OLDY = "y";
	private static final String NEWY = "newy";

	private StringBuilder output;

	public JavaODEMethodPrinter(Constants constants,
			BiMap<CombinedPopulationProduct, Integer> combinedMomentsIndex,
			Map<AbstractExpression, Integer> generalExpectationIndex) {

		this.constants = constants;
		this.combinedMomentsIndex = combinedMomentsIndex;
		this.generalExpectationIndex = generalExpectationIndex;
		output = new StringBuilder();
	}

	@Override
	public void visit(ODEMethod s) {

		String[] javaMomentODEs = new String[s.getBody().length + 1];
		Set<ExpressionVariable> variables = new HashSet<ExpressionVariable>();
		for (int i = 0; i < s.getBody().length; i++) {
			JavaStatementPrinterCombinedProductBased tmp = new JavaStatementPrinterCombinedProductBased(
					constants, combinedMomentsIndex, generalExpectationIndex,
					OLDY, NEWY, false);
			s.getBody()[i].accept(tmp);
			javaMomentODEs[i] = tmp.toString() + "\n";
			variables.addAll(tmp.getRhsVariables());
		}
		JavaStatementPrinterCombinedProductBased tmp = new JavaStatementPrinterCombinedProductBased(
				constants, combinedMomentsIndex, generalExpectationIndex, OLDY,
				NEWY, false);
		javaMomentODEs[s.getBody().length] = tmp.toString();

		StringBuilder header = new StringBuilder();
		StringBuilder code = new StringBuilder();
		header.append("import " + SystemOfODEs.class.getName() + ";\n"
				+ "public class " + GENERATEDCLASSNAME + " extends "
				+ SystemOfODEs.class.getName() + "{\n");
		header.append(javaMomentODEs[javaMomentODEs.length - 1]);
		for (ExpressionVariable v:variables) {
			header.append("double " + v.getName() + ";\n");
		}
		int line = 0;
		int method = 0;
		int nODEs = javaMomentODEs.length;
		int[] remainingCharacters = new int[nODEs + 1];
		remainingCharacters[nODEs - 1] = 0;
		for (int l = nODEs - 2; l >= 0; l--) {
			int lineLength = javaMomentODEs[l].length();
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
				header
						.append("public double[] derivn(double x, double[] y) {\n");
				int nOdes = combinedMomentsIndex.size();
				for (ExpressionVariable v:variables) {
					header.append(v.getName() + " = ");
					JavaPrinterCombinedProductBased printer = new JavaPrinterCombinedProductBased(
							constants, combinedMomentsIndex, generalExpectationIndex,
							OLDY, true);
					v.getUnfolded().accept(printer);
					header.append(printer.toString());
					header.append(";\n");
				}
				header.append("double[] newy = new double[" + nOdes + "];\n");
			} else {
				code.append("private void derivn" + method
						+ "(double[] newy,double x, double[] y) {\n");
			}
			int charactersUsed = 0;
			while (line < nODEs - 1
					&& charactersUsed + javaMomentODEs[line].length() < methodCharacters) {
				charactersUsed += javaMomentODEs[line].length();
				if (method == 0) {
					header.append(javaMomentODEs[line]);
				} else {
					code.append(javaMomentODEs[line]);
				}
				line++;
			}
			if (method != 0) {
				code.append("\n}");
			}
			method++;
		}

		code.append("\n}\n");

		for (int i = 1; i < method; i++) {
			header.append("derivn" + i + "(newy,x,y);\n");
		}
		header.append("return newy;\n}");
		header.append(code);
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

	@Override
	public String toString() {
		return output.toString();
	}
}
