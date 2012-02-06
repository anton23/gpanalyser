package uk.ac.imperial.doc.pctmc.javaoutput.odeanalysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.SumExpression;
import uk.ac.imperial.doc.jexpressions.javaoutput.JavaExpressionPrinterWithVariables;
import uk.ac.imperial.doc.jexpressions.statements.AbstractStatement;
import uk.ac.imperial.doc.jexpressions.statements.Assignment;
import uk.ac.imperial.doc.jexpressions.statements.Increment;
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

		ArrayList<String> javaMomentODEs = new ArrayList<String>(s.getBody().length + 1);
		javaMomentODEs.ensureCapacity(s.getBody().length + 1);
		Set<ExpressionVariable> variables = new HashSet<ExpressionVariable>();
		
		int additionalLines = 0;
		for (int i = 0; i < s.getBody().length; i++) {
			JavaStatementPrinterCombinedProductBased printer = new JavaStatementPrinterCombinedProductBased(
					constants, combinedMomentsIndex, generalExpectationIndex,
					OLDY, NEWY, false);
			s.getBody()[i].accept(printer);
			
			String nextLine = printer.toString();
			if (nextLine.length() > methodCharacters) {
				if (s.getBody()[i] instanceof Assignment) {
					int statementLength = methodCharacters / 2;
					int nSplit = nextLine.length() / statementLength + 1;
					AbstractExpression rhs = ((Assignment)s.getBody()[i]).getRhs();	
					AbstractExpression lhs = ((Assignment)s.getBody()[i]).getLhs();
					if (rhs instanceof SumExpression) {						
						SumExpression sRhs = (SumExpression) rhs;
						int nSummands = Math.max(sRhs.getSummands().size() / nSplit,1);
						javaMomentODEs.ensureCapacity(s.getBody().length + 2 + additionalLines + nSplit);
						int nPortions = sRhs.getSummands().size()/nSummands;
						for (int j = 0; j < nPortions; j++) {
							int fromIndex = j*nSummands;
							int toIndex = (j == nPortions - 1)?sRhs.getSummands().size():(j+1)*nSummands;
							List<AbstractExpression> portion = ((SumExpression) rhs).getSummands().subList(fromIndex, toIndex);
							AbstractStatement newS;
							if (j==0) {
								newS =  new Assignment(lhs, SumExpression.create(portion));
							} else {
								newS =  new Increment(lhs, SumExpression.create(portion));
								additionalLines++;
							}
							printer = new JavaStatementPrinterCombinedProductBased(
									constants, combinedMomentsIndex, generalExpectationIndex,
									OLDY, NEWY, false);
							newS.accept(printer);
							javaMomentODEs.add(i+additionalLines, printer.toString() + "\n");
						}						
					}
				} else {
					throw new AssertionError("Cannot split the statement " + printer.toString() + " into multiple shorter statements!");
				}
			} else {
				javaMomentODEs.add(i+additionalLines, nextLine + "\n");
			}
			variables.addAll(printer.getRhsVariables());
		}
		JavaStatementPrinterCombinedProductBased tmp = new JavaStatementPrinterCombinedProductBased(
				constants, combinedMomentsIndex, generalExpectationIndex, OLDY,
				NEWY, false);
		javaMomentODEs.add(s.getBody().length+additionalLines, tmp.toString());

		StringBuilder header = new StringBuilder();
		StringBuilder code = new StringBuilder();
		header.append("import " + SystemOfODEs.class.getName() + ";\n"
				+ "public class " + GENERATEDCLASSNAME + " extends "
				+ SystemOfODEs.class.getName() + "{\n");
		header.append(javaMomentODEs.get(javaMomentODEs.size() - 1));
		for (ExpressionVariable v:variables) {
			header.append("double " + JavaExpressionPrinterWithVariables.escapeName(v.getName()) + ";\n");
		}
		int line = 0;
		int method = 0;
		int totalLines = javaMomentODEs.size();
		int[] remainingCharacters = new int[totalLines + 1];
		remainingCharacters[totalLines - 1] = 0;
		for (int l = totalLines - 2; l >= 0; l--) {
			int lineLength = javaMomentODEs.get(l).length();
			/*if (lineLength > methodCharacters) {
				methodCharacters = lineLength + 1;
			}*/
			remainingCharacters[l] = remainingCharacters[l + 1] + lineLength;
		}
		PCTMCLogging.debug("Total characters of the generated code "
				+ remainingCharacters[0]);
		PCTMCLogging.debug("Splitting code into methods.");
		while (line < totalLines - 1) {
			if (method == 0) {
				header
						.append("public double[] derivn(double x, double[] y) {\n");
				int nOdes = combinedMomentsIndex.size();
				for (ExpressionVariable v:variables) {
					header.append(JavaExpressionPrinterWithVariables.escapeName(v.getName()) + " = ");
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
			while (line < totalLines - 1
					&& charactersUsed + javaMomentODEs.get(line).length() < methodCharacters || javaMomentODEs.get(line).length() > methodCharacters) {
				if (javaMomentODEs.get(line).length() > methodCharacters && charactersUsed > 0) break;
				charactersUsed += javaMomentODEs.get(line).length();
				if (method == 0) {
					header.append(javaMomentODEs.get(line));
				} else {
					code.append(javaMomentODEs.get(line));
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
