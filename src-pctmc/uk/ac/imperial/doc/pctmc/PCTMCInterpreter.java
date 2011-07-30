package uk.ac.imperial.doc.pctmc;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.Parser;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.antlr.runtime.tree.TreeNodeStream;
import org.antlr.runtime.tree.TreeParser;

import uk.ac.imperial.doc.gpa.plain.expressions.TransactionPatternMatcher;
import uk.ac.imperial.doc.gpa.plain.syntax.PlainCompiler;
import uk.ac.imperial.doc.gpa.plain.syntax.PlainLexer;
import uk.ac.imperial.doc.gpa.plain.syntax.PlainParser;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.utils.ToStringUtils;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.analysis.PCTMCAnalysisPostprocessor;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.CollectUsedMomentsVisitor;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.PlotDescription;
import uk.ac.imperial.doc.pctmc.charts.PCTMCChartUtilities;
import uk.ac.imperial.doc.pctmc.experiments.iterate.PCTMCIterate;
import uk.ac.imperial.doc.pctmc.experiments.iterate.PlotAtDescription;
import uk.ac.imperial.doc.pctmc.experiments.iterate.PlotConstraint;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.ExpressionVariableSetterPCTMC;
import uk.ac.imperial.doc.pctmc.expressions.patterns.PatternMatcher;
import uk.ac.imperial.doc.pctmc.expressions.patterns.PatternSetterVisitor;
import uk.ac.imperial.doc.pctmc.postprocessors.MatlabAnalysisPostprocessor;
import uk.ac.imperial.doc.pctmc.representation.EvolutionEvent;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;
import uk.ac.imperial.doc.pctmc.utils.PCTMCOptions;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

/**
 * Class with methods for parsing GPEPA files and executing the analyses.
 * 
 * @author tonkos
 */
public class PCTMCInterpreter {
	private Class<? extends Lexer> lexerClass;
	private Class<? extends Parser> parserClass;
	private Class<? extends TreeParser> compilerClass;
	private Class<? extends PatternMatcher> patternMatcherClass;

	public PCTMCInterpreter(Class<? extends Lexer> lexerClass,
			Class<? extends Parser> parserClass,
			Class<? extends TreeParser> compilerClass) {
		this.lexerClass = lexerClass;
		this.parserClass = parserClass;
		this.compilerClass = compilerClass;

		globalPostprocessors = new LinkedList<PCTMCAnalysisPostprocessor>();
	}

	public PCTMCInterpreter(Class<? extends Lexer> lexerClass,
			Class<? extends Parser> parserClass,
			Class<? extends TreeParser> compilerClass,
			Class<? extends PatternMatcher> patternMatcherClass) {
		this(lexerClass, parserClass, compilerClass);
		this.patternMatcherClass = patternMatcherClass;
	}

	private Collection<PCTMCAnalysisPostprocessor> globalPostprocessors;

	private OptionParser createOptionParser() {
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

				accepts("noGUI", "runs without graphical output");

				accepts("plain",
						"reads model descriptions in plain PCTMC format");

				accepts("3D", "displays 3D plots for iterate experiments");

				accepts("help", "show help");
			}
		};

	}

	@SuppressWarnings("serial")
	class ParseException extends Exception {
		List<String> errors;

		public ParseException(List<String> errors) {
			super();
			this.errors = errors;
		} 
	}

	@SuppressWarnings("unchecked")
	private Object parseFile(String file) throws ParseException {
		try {
			Lexer lexer = lexerClass.getConstructor(CharStream.class)
					.newInstance(new ANTLRFileStream(file));
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
			return compiler.getClass().getMethod("system", (Class<?>[]) null)
					.invoke(compiler, (Object[]) null);
		} catch (Exception e) {
			if (e instanceof ParseException) throw (ParseException)e;
			e.printStackTrace();
			throw new ParseException(Lists.newArrayList("Unexpected internal error: " + e));
		}
	}

	private void unfoldPatterns(PCTMC pctmc,
			Multimap<AbstractPCTMCAnalysis, PlotDescription> plots,
			Collection<PCTMCIterate> experiments,
			Map<ExpressionVariable, AbstractExpression> unfoldedVariables,
			PatternMatcher patternMatcher) {
		PatternSetterVisitor.unfoldPatterns(unfoldedVariables, patternMatcher);
		for (EvolutionEvent e : pctmc.getEvolutionEvents()) {
			AbstractExpression rate = e.getRate();
			PatternSetterVisitor.unfoldPatterns(rate, patternMatcher);
		}
		for (PlotDescription pd : plots.values()) {
			for (AbstractExpression e : pd.getExpressions()) {
				PatternSetterVisitor.unfoldPatterns(e, patternMatcher);
			}
		}
		for (PCTMCIterate iterate : experiments) {
			for (PlotAtDescription pd : iterate.getPlots()) {
				PatternSetterVisitor.unfoldPatterns(pd.getExpression(),
						patternMatcher);
				for (PlotConstraint c : pd.getConstraints()) {
					PatternSetterVisitor.unfoldPatterns(c.getExpression(),
							patternMatcher);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void processFile(String file) {
		PCTMCLogging.info("Opening file " + file);
		PCTMCLogging.increaseIndent();

		Constants constants;
		try {
			Object compilerReturn = parseFile(file);
			constants = (Constants) compilerReturn.getClass()
					.getField("constants").get(compilerReturn);

			Multimap<AbstractPCTMCAnalysis, PlotDescription> plots = (Multimap<AbstractPCTMCAnalysis, PlotDescription>) compilerReturn
					.getClass().getField("plots").get(compilerReturn);
			List<PCTMCIterate> experiments = (List<PCTMCIterate>) compilerReturn
					.getClass().getField("experiments").get(compilerReturn);
			Map<ExpressionVariable, AbstractExpression> unfoldedVariables = (Map<ExpressionVariable, AbstractExpression>) compilerReturn
					.getClass().getField("unfoldedVariables")
					.get(compilerReturn);
			PCTMC pctmc = (PCTMC) compilerReturn.getClass().getField("pctmc")
					.get(compilerReturn);
			PatternMatcher patternMatcher = null;
			if (patternMatcherClass != null) {
				patternMatcher = patternMatcherClass
						.getConstructor(PCTMC.class).newInstance(pctmc);

			}
			if (patternMatcher != null) {
				unfoldPatterns(pctmc, plots, experiments, unfoldedVariables,
						patternMatcher);
			}

			PCTMCLogging
					.info("Read a PCTMC with " + pctmc.getStateIndex().size()
							+ " states and "
							+ pctmc.getEvolutionEvents().size()
							+ " transition classes");
			for (AbstractPCTMCAnalysis analysis : plots.keySet()) {
				List<PlotDescription> aplots = new LinkedList<PlotDescription>(
						plots.get(analysis));
				processPlots(analysis, aplots, unfoldedVariables, constants);
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
			PCTMCLogging.decreaseIndent();
		} catch (ParseException e) {
			PCTMCLogging
					.error("Error when parsing the input file!\nFound " + e.errors.size() + " errors:\n" +
							ToStringUtils.iterableToSSV(e.errors, "\n")
			);
		} 
		  catch (IllegalArgumentException e1) { e1.printStackTrace(); } catch
		  (SecurityException e1) { e1.printStackTrace(); } catch
		  (IllegalAccessException e1) { e1.printStackTrace(); } catch
		  (NoSuchFieldException e1) { e1.printStackTrace(); } catch
		  (InstantiationException e) { e.printStackTrace(); } catch
		  (InvocationTargetException e) { e.printStackTrace(); } catch
		  (NoSuchMethodException e) { e.printStackTrace(); }
		 

	}

	public void run(String[] args) {
		OptionParser optionParser = createOptionParser();

		try {
			OptionSet options = optionParser.parse(args);

			if (options.nonOptionArguments().isEmpty()) {
				try {
					System.out.println("Usage: gpa <options> <model files>");
					optionParser.printHelpOn(System.out);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {

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
					PCTMCOptions.debugFolder = options.valueOf("debug")
							.toString();
					PCTMCLogging
							.info("Running in debug mode, output folder is "
									+ PCTMCOptions.debugFolder + ".");
				}
				if (options.has("3D")) {
					PCTMCChartUtilities.jogl = true;
				}
				if (options.has("plain")) {
					this.lexerClass = PlainLexer.class;
					this.parserClass = PlainParser.class;
					this.compilerClass = PlainCompiler.class;
					this.patternMatcherClass = TransactionPatternMatcher.class;
				}

				if (options.has("matlab")) {
					PCTMCOptions.matlab = true;
					PCTMCOptions.matlabFolder = options.valueOf("matlab")
							.toString();
					PCTMCLogging
							.info("Generating matlab code, output folder is "
									+ PCTMCOptions.matlabFolder + ".");
					globalPostprocessors.add(new MatlabAnalysisPostprocessor());
				}

				PCTMCLogging
						.debug("Creating a PCTMC interpreter with\n lexer: "
								+ lexerClass + ",\n parser: " + parserClass
								+ ",\n compiler: " + compilerClass);
				if (patternMatcherClass != null) {
					PCTMCLogging.debug("Registering pattern matcher "
							+ patternMatcherClass);
				}

				for (String file : options.nonOptionArguments()) {
					processFile(file);
				}
			}
		} catch (OptionException e) {
			PCTMCLogging.error(e.getMessage());
		}

	}

	private void processPlots(AbstractPCTMCAnalysis analysis,
			List<PlotDescription> plotDescriptions,
			Map<ExpressionVariable, AbstractExpression> unfoldedVariables,
			Constants constants) {
		PCTMCLogging.info("Running analysis " + analysis.toString());
		PCTMCLogging.increaseIndent();
		List<List<AbstractExpression>> plots = new ArrayList<List<AbstractExpression>>(
				plotDescriptions.size());
		List<String> filenames = new ArrayList<String>(plotDescriptions.size());

		List<AbstractExpression> usedExpressions = new LinkedList<AbstractExpression>();
		for (PlotDescription pexp : plotDescriptions) {
			List<AbstractExpression> plotExpressions = new LinkedList<AbstractExpression>();
			for (AbstractExpression e : pexp.getExpressions()) {
				ExpressionVariableSetterPCTMC setter = new ExpressionVariableSetterPCTMC(
						unfoldedVariables);
				e.accept(setter);
				plotExpressions.add(e);
				usedExpressions.add(e);
			}
			plots.add(plotExpressions);
			filenames.add(pexp.getFilename());
		}

		analyse(analysis, constants, usedExpressions);
		analysis.notifyPostprocessors(constants, plotDescriptions);

		for (PCTMCAnalysisPostprocessor postProcessor : globalPostprocessors) {
			postProcessor.postprocessAnalysis(constants, analysis,
					plotDescriptions);
		}

		PCTMCLogging.decreaseIndent();
	}

	private void analyse(AbstractPCTMCAnalysis analysis, Constants variables,
			List<AbstractExpression> usedExpressions) {
		Set<CombinedPopulationProduct> usedProducts = new HashSet<CombinedPopulationProduct>();
		Set<AbstractExpression> usedGeneralExpectations = new HashSet<AbstractExpression>();
		for (AbstractExpression exp : usedExpressions) {
			CollectUsedMomentsVisitor visitor = new CollectUsedMomentsVisitor();
			exp.accept(visitor);
			usedProducts.addAll(visitor.getUsedCombinedMoments());
			usedGeneralExpectations
					.addAll(visitor.getUsedGeneralExpectations());
		}
		analysis.setUsedMoments(usedProducts);
		analysis.setUsedGeneralExpectations(usedGeneralExpectations);
		analysis.prepare(variables);
	}
}
