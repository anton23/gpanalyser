package uk.ac.imperial.doc.pctmc.interpreter;

import java.io.IOException;
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

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.utils.ToStringUtils;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.analysis.PCTMCAnalysisPostprocessor;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.PlotDescription;
import uk.ac.imperial.doc.pctmc.charts.PCTMCChartUtilities;
import uk.ac.imperial.doc.pctmc.experiments.iterate.PCTMCIterate;
import uk.ac.imperial.doc.pctmc.expressions.patterns.PatternMatcher;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
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
	
	@SuppressWarnings("unchecked")
	public List<AbstractExpression> parseExpressionList(String string) throws ParseException{
		Lexer lexer;
		try {
	
			lexer = lexerClass.getConstructor(
					CharStream.class).newInstance(
					new ANTLRStringStream(string));
		
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		Parser parser = parserClass.getConstructor(
				TokenStream.class).newInstance(tokens);
		Object expressionsReturn = parserClass.getMethod("expressionList",
				(Class<?>[]) null).invoke(parser,
				(Object[]) null);
		CommonTree expressionsTree = (CommonTree) expressionsReturn
				.getClass().getMethod("getTree",
						(Class<?>[]) null).invoke(expressionsReturn,
						(Object[]) null);
		CommonTreeNodeStream nodes = new CommonTreeNodeStream(
				expressionsTree);
		TreeParser compiler = compilerClass.getConstructor(
				TreeNodeStream.class).newInstance(nodes);
		Object compilerReturn = compiler.getClass().getMethod(
				"expressionList", (Class<?>[]) null).invoke(compiler,
				(Object[]) null);
		
		return (List<AbstractExpression>) compilerReturn;
		
		} catch (Exception e) {
			if (e instanceof ParseException) throw (ParseException)e;
			throw new ParseException(Lists.newArrayList("Unexpected internal error: " + e));
		} 
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

	/**
	 * Creates a PCTMCInterpreter using the given lexer, parser and compiler classes.
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

	private Collection<PCTMCAnalysisPostprocessor> globalPostprocessors;
	
	public void addGlobalPostprocessor(PCTMCAnalysisPostprocessor postprocessor){
		globalPostprocessors.add(postprocessor);
	}

	
	@SuppressWarnings("unchecked")
	protected PCTMCFileRepresentation parseStream(ANTLRStringStream stream) throws ParseException {
		try {
			Lexer lexer = lexerClass.getConstructor(CharStream.class)
					.newInstance(stream);
			CommonTokenStream tokens = new CommonTokenStream(lexer);

			Parser parser = parserClass.getConstructor(TokenStream.class)
					.newInstance(tokens);
			
			Object systemReturn = parserClass.getMethod("system",
					(Class<?>[]) null).invoke(parser, (Object[]) null);
			
			List<String> errors = (List<String>)parser.getClass()
					.getMethod("getErrors", (Class<?>[]) null)
					.invoke(parser, (Object[]) null);
			if (!errors.isEmpty()){
				throw new ParseException(errors);
			}
					
			CommonTree systemTree = (CommonTree) systemReturn.getClass()
					.getMethod("getTree", (Class<?>[]) null)
					.invoke(systemReturn, (Object[]) null);
			CommonTreeNodeStream nodes = new CommonTreeNodeStream(systemTree);
			TreeParser compiler = compilerClass.getConstructor(
					TreeNodeStream.class).newInstance(nodes);
			PCTMCFileRepresentation pctmcFileRepresentation = new PCTMCFileRepresentation(compiler.getClass().getMethod("system", (Class<?>[]) null)
					.invoke(compiler, (Object[]) null));
			if (patternMatcherClass!=null){
				try {
					PatternMatcher patternMatcher = patternMatcherClass
							.getConstructor(PCTMC.class).newInstance(pctmcFileRepresentation.getPctmc());
					pctmcFileRepresentation.unfoldPatterns(patternMatcher);
				} catch (Exception e){
					PCTMCLogging.error("Unexpected internal error!\n"+e);
				}
				
			}
			return pctmcFileRepresentation;
		} catch (Exception e) {
			if (e instanceof ParseException) throw (ParseException)e;
			throw new ParseException(Lists.newArrayList("Unexpected internal error: " + e));
		}
	}

	
	

	
	/**
	 * Parses an input file and returns a file representation object.
	 * @param file
	 * @return
	 * @throws ParseException
	 */
	public PCTMCFileRepresentation parsePCTMCFile(String file) throws ParseException {
		try {
			return parseStream(new ANTLRFileStream(file));
		} catch (IOException e) {
			throw new ParseException(Lists.newArrayList("The given file doesn't exist!"));
		}
	}
	
	public PCTMCFileRepresentation parsePCTMCFileInString(String string) throws ParseException {
		return parseStream(new ANTLRStringStream(string));
	}
	
	public void processFileRepresentation(PCTMCFileRepresentation fileRepresentation){
		Constants constants = fileRepresentation.getConstants();
		Multimap<AbstractPCTMCAnalysis, PlotDescription> plots = fileRepresentation.getPlots();
		List<PCTMCIterate> experiments = fileRepresentation.getExperiments();
		PCTMC pctmc = fileRepresentation.getPctmc();
		
		fileRepresentation.unfoldVariablesAndSetUsedProducts();

		PCTMCLogging
				.info("Read a PCTMC with " + pctmc.getStateIndex().size()
						+ " states and "
						+ pctmc.getEvolutionEvents().size()
						+ " transition classes");
		for (AbstractPCTMCAnalysis analysis : plots.keySet()) {
			List<PlotDescription> aplots = new LinkedList<PlotDescription>(
					plots.get(analysis));
			processPlots(analysis, aplots,constants);
			PCTMCChartUtilities.nextBatch();
		}
		for (PCTMCIterate iterate : experiments) {
			iterate.prepare(constants);
			if (PCTMCOptions.matlab) {
				/*
				 * MatlabIteratePostprocessor matlabImplementer = new
				 * MatlabIteratePostprocessor();
				 * matlabImplementer.writePCTMCIterateFile(iterate,
				 * constants);
				 */
			}
			iterate.iterate(constants);
		}	
	}
	

	public void processFile(String file) {
		PCTMCLogging.info("Opening file " + file);
		PCTMCLogging.increaseIndent();

		try {
			PCTMCFileRepresentation fileRepresentation = parsePCTMCFile(file);
			processFileRepresentation(fileRepresentation);
	
		} catch (ParseException e) {
			PCTMCLogging
					.error("Error when parsing the input file!\nFound " + e.errors.size() + " errors:\n" +
							ToStringUtils.iterableToSSV(e.errors, "\n")
			);
		} 
		PCTMCLogging.decreaseIndent();

	}

	public void run(List<String> files) {
		for (String file : files) {
			processFile(file);
		}
	
	}

	private void processPlots(AbstractPCTMCAnalysis analysis,
			List<PlotDescription> plotDescriptions,
			Constants constants) {
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
}
