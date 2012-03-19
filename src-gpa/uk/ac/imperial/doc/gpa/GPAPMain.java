package uk.ac.imperial.doc.gpa;

import java.io.IOException;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import uk.ac.imperial.doc.gpa.patterns.GPEPAPatternMatcher;
import uk.ac.imperial.doc.gpa.plain.expressions.TransactionPatternMatcher;
import uk.ac.imperial.doc.gpa.plain.syntax.PlainCompiler;
import uk.ac.imperial.doc.gpa.plain.syntax.PlainLexer;
import uk.ac.imperial.doc.gpa.plain.syntax.PlainParser;
import uk.ac.imperial.doc.gpa.syntax.GPACompiler;
import uk.ac.imperial.doc.gpa.syntax.GPALexer;
import uk.ac.imperial.doc.gpa.syntax.GPAParser;
import uk.ac.imperial.doc.masspa.patterns.MASSPAPatternMatcher;
import uk.ac.imperial.doc.masspa.syntax.MASSPACompiler;
import uk.ac.imperial.doc.masspa.syntax.MASSPALexer;
import uk.ac.imperial.doc.masspa.syntax.MASSPAParser;
import uk.ac.imperial.doc.pctmc.charts.PCTMCChartUtilities;
import uk.ac.imperial.doc.pctmc.interpreter.PCTMCInterpreter;
import uk.ac.imperial.doc.pctmc.postprocessors.languageoutput.JavaOutputAnalysisPostprocessor;
import uk.ac.imperial.doc.pctmc.postprocessors.languageoutput.MatlabAnalysisPostprocessor;
import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;
import uk.ac.imperial.doc.pctmc.utils.PCTMCOptions;

public class GPAPMain {

	public static OptionParser createOptionParser() {
		return new OptionParser() {
			{
				accepts("debug",
						"generates debug output, including source files")
						.withRequiredArg().ofType(String.class)
						.describedAs("output folder");

				accepts("matlab",
						"generates matlab output, including source files")
						.withRequiredArg().ofType(String.class)
						.describedAs("output folder");
				
				accepts("java",
						"generates java output, including source files")
						.withRequiredArg().ofType(String.class)
						.describedAs("output folder");

				accepts("noGUI", "runs without graphical output");

				accepts("plain",
						"reads model descriptions in plain PCTMC format");
				
				accepts("masspa",
						"reads model descriptions in MASSPA format");

				accepts("3D", "displays 3D plots for iterate experiments");
				
				accepts("nthreads", "number of threads available to analyses")
				.withRequiredArg().ofType(Integer.class)
						.describedAs("number of threads");

				accepts("help", "show help");
			}
		};

	}
	
	public static PCTMCInterpreter processOptions(OptionParser optionParser, OptionSet options) {

		 
		if (options.nonOptionArguments().isEmpty()) {
			try {
				System.out.println("Usage: gpa <options> <model files>");
				optionParser.printHelpOn(System.out);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		} else {
			PCTMCInterpreter interpreter;
			if (options.has("help")) {
				try {
					optionParser.printHelpOn(System.out);
				} catch (IOException e1) {
					PCTMCLogging.error(e1.getMessage());
				}
			}

			if (options.has("noGUI")) {
				PCTMCChartUtilities.setGui(false);
				PCTMCLogging.info("Running without GUI.");
			}

			if (options.has("debug")) {
				PCTMCOptions.debug = true;
				PCTMCOptions.debugFolder = options.valueOf("debug").toString();
				PCTMCLogging.info("Running in debug mode, output folder is "
						+ PCTMCOptions.debugFolder + ".");
			}
			if (options.has("3D")) {
				PCTMCChartUtilities.jogl = true;
			}
			if (options.has("nthreads")) {
				Integer nthreads = (Integer)options.valueOf("nthreads");
				if (nthreads<1) {
					nthreads = 1;
				}
				if (nthreads > Runtime.getRuntime().availableProcessors()) {
					PCTMCLogging.info("Using more threads (" + nthreads + ") than available processors (" + Runtime.getRuntime().availableProcessors() + ")");
				}
				PCTMCOptions.nthreads = nthreads;
			}
			if (options.has("plain")) {
				interpreter = createPlainPCTMCInterpreter();
			}
			else if (options.has("masspa"))
			{
				interpreter = createMASSPAInterpreter();
			}
			else {
				interpreter = createGPEPAInterpreter();
			}

			if (options.has("matlab")) {
				PCTMCOptions.matlab = true;
				PCTMCOptions.matlabFolder = options.valueOf("matlab")
						.toString();
				PCTMCLogging.info("Generating matlab code, output folder is "
						+ PCTMCOptions.matlabFolder + ".");
				interpreter.addGlobalPostprocessor(new MatlabAnalysisPostprocessor());			
			}
			if (options.has("java")){
				PCTMCOptions.javaFolder = options.valueOf("java").toString();
				PCTMCLogging.info("Generating java code, output folder is " + PCTMCOptions.javaFolder);
				interpreter.addGlobalPostprocessor(new JavaOutputAnalysisPostprocessor());
			}
			
			 PCTMCLogging .debug("Creating a PCTMC interpreter with\n lexer: "
			  + interpreter.getLexerClass() + ",\n parser: " + interpreter.getParserClass() + ",\n compiler: " +
			  interpreter.getCompilerClass()); 
			 if (interpreter.getPatternMatcherClass() != null) {
			  PCTMCLogging.debug("Registering pattern matcher " +
			  interpreter.getPatternMatcherClass());
			 }
			return interpreter;
		}

	}

	public static void main(String[] args) {
		OptionParser optionParser = createOptionParser();
		OptionSet options = optionParser.parse(args);
		PCTMCInterpreter interpreter = processOptions(optionParser, options);
		interpreter.run(options.nonOptionArguments());

	}

	public static PCTMCInterpreter createGPEPAInterpreter() {
		return new PCTMCInterpreter(GPALexer.class, GPAParser.class,
				GPACompiler.class, GPEPAPatternMatcher.class);

	}
	
	public static PCTMCInterpreter createMASSPAInterpreter() {
		return new PCTMCInterpreter(MASSPALexer.class, MASSPAParser.class,
				MASSPACompiler.class, MASSPAPatternMatcher.class);
	}

	public static PCTMCInterpreter createPlainPCTMCInterpreter() {
		return new PCTMCInterpreter(PlainLexer.class, PlainParser.class,
				PlainCompiler.class, TransactionPatternMatcher.class);
	}
}
