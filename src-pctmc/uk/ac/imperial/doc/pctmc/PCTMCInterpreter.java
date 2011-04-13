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
import org.jfree.data.xy.XYSeriesCollection;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.analysis.AnalysisUtils;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.CollectUsedMomentsVisitor;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.PlotDescription;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.PlotExpression;
import uk.ac.imperial.doc.pctmc.charts.PCTMCChartUtilities;
import uk.ac.imperial.doc.pctmc.experiments.iterate.PCTMCIterate;
import uk.ac.imperial.doc.pctmc.experiments.iterate.PlotAtDescription;
import uk.ac.imperial.doc.pctmc.experiments.iterate.PlotConstraint;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.ExpressionVariableSetterPCTMC;
import uk.ac.imperial.doc.pctmc.expressions.patterns.PatternMatcher;
import uk.ac.imperial.doc.pctmc.expressions.patterns.PatternSetterVisitor;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;
import uk.ac.imperial.doc.pctmc.utils.FileUtils;
import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;
import uk.ac.imperial.doc.pctmc.utils.PCTMCOptions;

import com.google.common.collect.Multimap;
 
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
	}

	
	
	public PCTMCInterpreter(Class<? extends Lexer> lexerClass,
			Class<? extends Parser> parserClass,
			Class<? extends TreeParser> compilerClass,
			Class<? extends PatternMatcher> patternMatcherClass) {
		this(lexerClass, parserClass, compilerClass);
		this.patternMatcherClass = patternMatcherClass;
	}

	@SuppressWarnings("unchecked")
	public void run(String[] args) {

		OptionParser optionParser = new OptionParser() {
			{
				accepts("debug",
						"generates debug output, including source files")
						.withRequiredArg().ofType(String.class).describedAs(
								"output folder");
				/*
				 * accepts("matlab",
				 * "generates matlab output, including source files")
				 * .withRequiredArg().ofType(String.class).describedAs(
				 * "output folder");
				 */

				accepts("noGUI", "runs without graphical output");
				
				accepts("3D","displays 3D plots for iterate experiments");

				accepts("help", "show help");
			}
		};

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
				if (options.has("3D")){
					PCTMCChartUtilities.jogl = true; 
				}

				/*
				 * if (options.has("matlab")) { PCTMCOptions.matlab = true;
				 * PCTMCOptions.matlabFolder =
				 * options.valueOf("matlab").toString();
				 * PCTMCLogging.info("Generating matlab code, output folder is "
				 * + PCTMCLogging.matlabFolder + "."); }
				 */
				PCTMCLogging.debug("Creating a PCTMC interpreter with\n lexer: "
						+ lexerClass + ",\n parser: " + parserClass + ",\n compiler: "
						+ compilerClass);
				if (patternMatcherClass!=null) {
					PCTMCLogging.debug("Registering pattern matcher " + patternMatcherClass);
				}
				
				for (String file : options.nonOptionArguments()) {
					PCTMCLogging.info("Opening file " + file);
					try {
						PCTMCLogging.increaseIndent();
						Lexer lexer = lexerClass.getConstructor(
								CharStream.class).newInstance(
								new ANTLRFileStream(file));
						CommonTokenStream tokens = new CommonTokenStream(lexer);
						Parser parser = parserClass.getConstructor(
								TokenStream.class).newInstance(tokens);
						Object systemReturn = parserClass.getMethod("system",
								(Class<?>[]) null).invoke(parser,
								(Object[]) null);
						CommonTree systemTree = (CommonTree) systemReturn
								.getClass().getMethod("getTree",
										(Class<?>[]) null).invoke(systemReturn,
										(Object[]) null);
						CommonTreeNodeStream nodes = new CommonTreeNodeStream(
								systemTree);
						TreeParser compiler = compilerClass.getConstructor(
								TreeNodeStream.class).newInstance(nodes);
						Object compilerReturn = compiler.getClass().getMethod(
								"system", (Class<?>[]) null).invoke(compiler,
								(Object[]) null);
						Constants constants = (Constants) compilerReturn
								.getClass().getField("constants").get(
										compilerReturn);
						Multimap<AbstractPCTMCAnalysis, PlotDescription> plots = (Multimap<AbstractPCTMCAnalysis, PlotDescription>) compilerReturn
								.getClass().getField("plots").get(
										compilerReturn);
						List<PCTMCIterate> experiments = (List<PCTMCIterate>) compilerReturn
								.getClass().getField("experiments").get(
										compilerReturn);
						Map<ExpressionVariable, AbstractExpression> unfoldedVariables = (Map<ExpressionVariable, AbstractExpression>) compilerReturn
								.getClass().getField("unfoldedVariables").get(
										compilerReturn);
						PCTMC pctmc = (PCTMC) compilerReturn.getClass()
								.getField("pctmc").get(compilerReturn);
						PatternMatcher patternMatcher = null;
						if (patternMatcherClass != null) {
							patternMatcher = patternMatcherClass
									.getConstructor(PCTMC.class).newInstance(
											pctmc);
						}
						if (patternMatcher != null) {
							// PCTMCLogging.debug("Registering a state pattern matcher.");
							PatternSetterVisitor.unfoldPatterns(
									unfoldedVariables, patternMatcher);
						}

						PCTMCLogging.info("Read a PCTMC with "
								+ pctmc.getStateIndex().size() + " states and "
								+ pctmc.getEvolutionEvents().size()
								+ " transition classes");
						for (AbstractPCTMCAnalysis analysis : plots.keySet()) {
							Collection<PlotDescription> aplots = plots
									.get(analysis);
							if (patternMatcher != null) {
								for (PlotDescription pd : aplots) {
									for (AbstractExpression e : pd
											.getExpressions()) {
										PatternSetterVisitor.unfoldPatterns(e,
												patternMatcher);
									}
								}
							}
							processPlots(analysis, aplots, unfoldedVariables,
									constants);
							PCTMCChartUtilities.nextBatch();
						}
						for (PCTMCIterate iterate : experiments) {
							if (patternMatcher != null) {
								for (PlotAtDescription pd : iterate.getPlots()) {
									PatternSetterVisitor.unfoldPatterns(pd
											.getExpression(), patternMatcher);
									for (PlotConstraint c : pd.getConstraints()) {
										PatternSetterVisitor.unfoldPatterns(c
												.getExpression(),
												patternMatcher);
									}
								}
							}
							iterate.iterate(constants);
						}
						PCTMCLogging.decreaseIndent();

					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (SecurityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InstantiationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NoSuchMethodException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NoSuchFieldException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		} catch (OptionException e) {
			PCTMCLogging.error(e.getMessage());
		}

	}

	private void processPlots(AbstractPCTMCAnalysis analysis,
			Collection<PlotDescription> plotDescriptions,
			Map<ExpressionVariable, AbstractExpression> unfoldedVariables,
			Constants constants) {
		PCTMCLogging.info("Running analysis " + analysis.toString());
		PCTMCLogging.increaseIndent();
		List<List<PlotExpression>> plots = new ArrayList<List<PlotExpression>>(
				plotDescriptions.size());
		List<String> filenames = new ArrayList<String>(plotDescriptions.size());

		List<PlotExpression> usedExpressions = new LinkedList<PlotExpression>();
		for (PlotDescription pexp : plotDescriptions) {
			List<PlotExpression> plotExpressions = new LinkedList<PlotExpression>();
			for (AbstractExpression e : pexp.getExpressions()) {
				ExpressionVariableSetterPCTMC setter = new ExpressionVariableSetterPCTMC(
						unfoldedVariables);
				e.accept(setter);
				PlotExpression pe = new PlotExpression(e);
				plotExpressions.add(pe);
				usedExpressions.add(pe);
			}
			plots.add(plotExpressions);
			filenames.add(pexp.getFilename());
		}

		analyse(analysis, constants, usedExpressions, analysis.getStepSize());

		for (int i = 0; i < plots.size(); i++) {
			plotData(analysis, constants, plots.get(i), filenames.get(i));
		}

		PCTMCLogging.decreaseIndent();
	}

	private void analyse(AbstractPCTMCAnalysis analysis, Constants variables,
			List<PlotExpression> expressions, double timeStep) {
		Set<CombinedPopulationProduct> usedProducts = new HashSet<CombinedPopulationProduct>();
		for (PlotExpression exp : expressions) {
			CollectUsedMomentsVisitor visitor = new CollectUsedMomentsVisitor();
			exp.getExpression().accept(visitor);
			usedProducts.addAll(visitor.getUsedCombinedMoments());
		}
		analysis.setUsedMoments(usedProducts);
		analysis.analyse(variables);
	}

	public static void plotData(AbstractPCTMCAnalysis analysis,
			Constants variables, List<PlotExpression> expressions,
			String filename) {
		String[] names = new String[expressions.size()];
		for (int i = 0; i < expressions.size(); i++) {
			names[i] = expressions.get(i).toString();
		}
		double[][] data = analysis.evaluateExpressions(expressions, variables);
		XYSeriesCollection dataset = AnalysisUtils.getDataset(data, analysis
				.getStepSize(), names);
		PCTMCChartUtilities.drawChart(dataset, "time", "count", "", analysis
				.toString());
		if (!filename.equals("")) {
			List<String> labels = new LinkedList<String>();
			for (PlotExpression e : expressions) {
				labels.add(e.toString());
			}
			FileUtils.writeGnuplotFile(filename, "", labels, "time", "count");
			FileUtils.writeCSVfile(filename, dataset);
		}

	}
}
