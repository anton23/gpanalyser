package uk.ac.imperial.doc.pctmc.interpreter;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.Parser;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.antlr.runtime.tree.TreeNodeStream;
import org.antlr.runtime.tree.TreeParser;

import uk.ac.imperial.doc.gpa.plain.syntax.PlainParser;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.javaoutput.utils.JExpressionsJavaUtils;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.analysis.PCTMCAnalysisPostprocessor;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.PlotDescription;
import uk.ac.imperial.doc.pctmc.charts.PCTMCChartUtilities;
import uk.ac.imperial.doc.pctmc.condor.CondorGenerator;
import uk.ac.imperial.doc.pctmc.condor.CondorMerger;
import uk.ac.imperial.doc.pctmc.experiments.iterate.PCTMCExperiment;
import uk.ac.imperial.doc.pctmc.expressions.patterns.PatternMatcher;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.syntax.ErrorReporter;
import uk.ac.imperial.doc.pctmc.syntax.ParsingData;
import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;
import uk.ac.imperial.doc.pctmc.utils.PCTMCOptions;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

/**
 * Class with methods for parsing GPEPA files and executing analyses.
 * 
 * @author tonkos
 */
public class PCTMCInterpreter {
	
	private Class<? extends Lexer> lexerClass;
	private Class<? extends Parser> parserClass;
	private Class<? extends TreeParser> compilerClass;
	private Class<? extends PatternMatcher> patternMatcherClass;
	
	private Collection<PCTMCAnalysisPostprocessor> globalPostprocessors;
	
	/**
	 * Creates a PCTMCInterpreter using the given lexer, parser and compiler
	 * classes.
	 * 
	 * @param lexerClass
	 * @param parserClass
	 * @param compilerClass
	 */
	public PCTMCInterpreter(Class<? extends Lexer> lexerClass,
			Class<? extends Parser> parserClass,
			Class<? extends TreeParser> compilerClass) {
		this.lexerClass = lexerClass;
		this.parserClass = parserClass;
		this.compilerClass = compilerClass;

		globalPostprocessors = new LinkedList<PCTMCAnalysisPostprocessor>();
	}
	
	/**
	 * Creates a PCTMCInterpreter using the given lexer, parser, compiler and
	 * pattern matcher classes.
	 * 
	 * @param lexerClass
	 * @param parserClass
	 * @param compilerClass
	 * @param patternMatcherClass
	 */
	public PCTMCInterpreter(Class<? extends Lexer> lexerClass,
			Class<? extends Parser> parserClass,
			Class<? extends TreeParser> compilerClass,
			Class<? extends PatternMatcher> patternMatcherClass) {
		this(lexerClass, parserClass, compilerClass);
		this.patternMatcherClass = patternMatcherClass;
	}

	@SuppressWarnings("unchecked")
	public List<AbstractExpression> parseExpressionList(String string)
			throws ParseException {
		return (List<AbstractExpression>) parseGenericRule(new ANTLRStringStream(string), "expressionList", false);
	}
	
	public void addGlobalPostprocessor(PCTMCAnalysisPostprocessor postprocessor) {
		globalPostprocessors.add(postprocessor);
	}
	
	boolean twoPass = true;
	
	public Object parseGenericRule(String string, String rule, boolean twoPass) throws ParseException {
		return parseGenericRule(new ANTLRStringStream(string), rule, twoPass);
	}
	
	public Object parseGenericRule(ANTLRStringStream stream, String rule, boolean twoPass) throws ParseException {
		Parser parser = null;
		ErrorReporter parserReporter = new ErrorReporter();
		try {			
			// First pass 			
			Lexer lexer = lexerClass.getConstructor(CharStream.class)
					.newInstance(stream);
			CommonTokenStream tokens = new CommonTokenStream(lexer);

			parser = parserClass.getConstructor(TokenStream.class)
				.newInstance(tokens);
			
			Object[] tmp = new Object[]{parserReporter};

			Class<?>[] pTypes = new Class<?>[]{ErrorReporter.class};
			
			if (twoPass) {
				try {
					parser.getClass().getMethod(
							"setErrorReporter", pTypes).invoke(parser,
							new Object[]{null});
					parserClass.getMethod(rule,
						(Class<?>[]) null).invoke(parser, (Object[]) null);
				} catch (Exception e) {
					// Ignores all errors during first parsing
					
				}
				ParsingData data = (ParsingData)parserClass.getMethod("getParsingData", (Class<?>[]) null).invoke(parser, (Object[]) null);
			
				// Second proper pass
				stream.reset();
				
				lexer = lexerClass.getConstructor(CharStream.class)
				.newInstance(stream);
				
				tokens = new CommonTokenStream(lexer);
				
				parser = parserClass.getConstructor(TokenStream.class)
				.newInstance(tokens);
				
				parserClass.getMethod("setParsingData", new Class<?>[]{ParsingData.class}).invoke(parser, new Object[]{data});
			}
		

			parser.getClass().getMethod(
					"setErrorReporter", pTypes).invoke(parser,
					tmp);
			
			Object systemReturn = null;
			
							
			systemReturn = parserClass.getMethod(rule,
						(Class<?>[]) null).invoke(parser, (Object[]) null);

			if (!parserReporter.errors.isEmpty()) {
				throw new ParseException(parserReporter.errors);
			}
		
			CommonTree systemTree = (CommonTree) systemReturn.getClass()
					.getMethod("getTree", (Class<?>[]) null).invoke(
							systemReturn, (Object[]) null);
			CommonTreeNodeStream nodes = new CommonTreeNodeStream(systemTree);
			TreeParser compiler = compilerClass.getConstructor(
					TreeNodeStream.class).newInstance(nodes);
			
			compiler.getClass().getMethod(
					"setErrorReporter", pTypes).invoke(compiler,
					tmp);
			
			return compiler.getClass().getMethod(rule, (Class<?>[]) null)
			.invoke(compiler, (Object[]) null);
		
		} catch (Exception e) {			
			if (e instanceof ParseException) {
				throw (ParseException)e;
			}
			if (!parserReporter.errors.isEmpty()) {
				throw new ParseException(parserReporter.errors);
			}
			if (e instanceof InvocationTargetException) {
				throw new ParseException(Lists
						.newArrayList("Unexpected internal error: " + ((InvocationTargetException)e).getTargetException()));
			}
			throw new ParseException(Lists
					.newArrayList("Unexpected internal error: " + e));		
		}		
	}
		
	protected PCTMCFileRepresentation parseStream(ANTLRStringStream stream)
			throws ParseException {
		try {
			PCTMCFileRepresentation pctmcFileRepresentation = new PCTMCFileRepresentation(
					parseGenericRule(stream, "completeSystem", true));
			if (patternMatcherClass != null) {
				PatternMatcher patternMatcher = patternMatcherClass
						.getConstructor(PCTMC.class).newInstance(
								pctmcFileRepresentation.getPctmc());
				pctmcFileRepresentation.unfoldPatterns(patternMatcher);
			}
			return pctmcFileRepresentation;
		} catch (Exception e) {
			if (e instanceof ParseException) {
				throw (ParseException) e;
			}
			throw new ParseException(Lists
					.newArrayList("Unexpected internal error: " + e));
		}
	}

	/**
	 * Parses an input file and returns a file representation object.
	 * 
	 * @param file
	 * @return
	 * @throws ParseException
	 */
	public PCTMCFileRepresentation parsePCTMCFile(String file)
			throws ParseException {
		try {
			return parseStream(new ANTLRFileStream(file));
		} catch (IOException e) {
			throw new ParseException(Lists
					.newArrayList("The given file doesn't exist!"));
		}
	}

	public PCTMCFileRepresentation parsePCTMCFileInString(String string)
			throws ParseException {
		return parseStream(new ANTLRStringStream(string));
	}

	public void processFileRepresentation(
			PCTMCFileRepresentation fileRepresentation) {
		Constants constants = fileRepresentation.getConstants();
		if (constants.getFiles() != null) {
			JExpressionsJavaUtils.loadFiles(constants.getFiles());
		}
		Multimap<AbstractPCTMCAnalysis, PlotDescription> plots = fileRepresentation
				.getPlots();
		List<PCTMCExperiment> experiments = fileRepresentation.getExperiments();
		PCTMC pctmc = fileRepresentation.getPctmc();

		if (PCTMCOptions.condor) {
			String options = "";
			// A not so nice hack
			if (PlainParser.class.equals(parserClass)) {
				options = "-plain";
			}

			new CondorGenerator(fileRepresentation, file, options).generate();
			return;
		}
		
		if (PCTMCOptions.condor_merge) {
			new CondorMerger(fileRepresentation, file, "").merge();
			return;
		}
		
		fileRepresentation.unfoldVariablesAndSetUsedProducts();

		PCTMCLogging.info("Read a PCTMC with " + pctmc.getStateIndex().size()
				+ " states and " + pctmc.getEvolutionEvents().size()
				+ " transition classes");
		for (AbstractPCTMCAnalysis analysis : plots.keySet()) {
			List<PlotDescription> aplots = new LinkedList<PlotDescription>(
					plots.get(analysis));
			processPlots(analysis, aplots, constants);
			PCTMCChartUtilities.nextBatch();
		}
		for (PCTMCExperiment iterate : experiments) {
			iterate.prepare(constants);
			if (PCTMCOptions.matlab) {
				/*
				 * MatlabIteratePostprocessor matlabImplementer = new
				 * MatlabIteratePostprocessor();
				 * matlabImplementer.writePCTMCIterateFile(iterate, constants);
				 */
			}
			iterate.run(constants);
		}
		for (IExtension e: fileRepresentation.getExtensions()) {
			e.execute();
		}
	}

	protected String file = "";
	
	public void processFile(String file) {
		PCTMCLogging.info("Opening file " + file);
		this.file = file;
		PCTMCLogging.increaseIndent();

		try {
			PCTMCFileRepresentation fileRepresentation = parsePCTMCFile(file);
			processFileRepresentation(fileRepresentation);

		} catch (ParseException e) {
			StringBuilder errors = new StringBuilder();
			boolean first = true;
			for (String error:e.errors) {
				if (first) first = false; 
				else errors.append("\n");
				errors.append(error.replaceAll("\n", "\n   "));
			}
			PCTMCLogging.error("Error when parsing the input file!\nFound "
					+ e.errors.size() + " errors:\n"
					+ errors);
		}
		PCTMCLogging.decreaseIndent();

	}

	public void run(List<String> files) {
		for (String file : files) {
			processFile(file);
		}
	}

	private void processPlots(AbstractPCTMCAnalysis analysis,
			List<PlotDescription> plotDescriptions, Constants constants) {
		PCTMCLogging.info("Running analysis " + analysis.toString());
		PCTMCLogging.increaseIndent();

		analysis.prepare(constants);
		analysis.notifyPostprocessors(constants, plotDescriptions);

		for (PCTMCAnalysisPostprocessor postProcessor : globalPostprocessors) {
			postProcessor.postprocessAnalysis(constants, analysis,
					plotDescriptions);
		}

		PCTMCLogging.decreaseIndent();
	}
	
	public Class<? extends Lexer> getLexerClass() {
		return lexerClass;
	}

	public Class<? extends Parser> getParserClass() {
		return parserClass;
	}

	public Class<? extends TreeParser> getCompilerClass() {
		return compilerClass;
	}

	public Class<? extends PatternMatcher> getPatternMatcherClass() {
		return patternMatcherClass;
	}
}
