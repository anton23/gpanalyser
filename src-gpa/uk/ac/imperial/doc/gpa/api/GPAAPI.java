package uk.ac.imperial.doc.gpa.api;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

import uk.ac.imperial.doc.gpa.patterns.GPEPAPatternMatcher;
import uk.ac.imperial.doc.gpa.syntax.GPACompiler;
import uk.ac.imperial.doc.gpa.syntax.GPALexer;
import uk.ac.imperial.doc.gpa.syntax.GPAParser;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.javaoutput.statements.AbstractExpressionEvaluator;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.CollectUsedMomentsVisitor;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;
import uk.ac.imperial.doc.pctmc.expressions.patterns.PatternMatcher;
import uk.ac.imperial.doc.pctmc.odeanalysis.PCTMCODEAnalysis;
import uk.ac.imperial.doc.pctmc.odeanalysis.utils.SystemOfODEs;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.ODEAnalysisNumericalPostprocessor;
import uk.ac.imperial.doc.pctmc.representation.PCTMC;

import com.google.common.collect.BiMap;

public class GPAAPI {
	private Class<? extends Lexer> lexerClass;
	private Class<? extends Parser> parserClass;
	private Class<? extends TreeParser> compilerClass;
	private Class<? extends PatternMatcher> patternMatcherClass;
	
	private GPAAPI(Class<? extends Lexer> lexerClass,
			Class<? extends Parser> parserClass,
			Class<? extends TreeParser> compilerClass,
			Class<? extends PatternMatcher> patternMatcherClass) {
		super();
		this.lexerClass = lexerClass;
		this.parserClass = parserClass;
		this.compilerClass = compilerClass;
		this.patternMatcherClass = patternMatcherClass;
	}

	public static GPAAPI getGPEPAAPI(){
		return new GPAAPI(GPALexer.class, GPAParser.class, GPACompiler.class,GPEPAPatternMatcher.class);
	}
	
	private Constants constants;
	private PCTMC pctmc;
	
	public Constants getConstants() {
		return constants;
	}
	
	Set<AbstractExpression> usedExpressions = new HashSet<AbstractExpression>();
	
	public void addUsedExpressions(Collection<AbstractExpression> toAdd){
		usedExpressions.addAll(toAdd);
	}

	public void parsePCTMC(String src){
		try {
			Lexer lexer = lexerClass.getConstructor(
					CharStream.class).newInstance(
					new ANTLRStringStream(src));
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
			constants = (Constants) compilerReturn
					.getClass().getField("constants").get(
							compilerReturn);
			pctmc = (PCTMC) compilerReturn.getClass()
			.getField("pctmc").get(compilerReturn);
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
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
    ODEAnalysisNumericalPostprocessor numericalPostprocessor; 
	
	PCTMCODEAnalysis odeAnalysis;
	public SystemOfODEs getODEs(int order){
		Set<CombinedPopulationProduct> combinedProducts = new HashSet<CombinedPopulationProduct>();
		for (AbstractExpression e:usedExpressions){
			CollectUsedMomentsVisitor visitor = new CollectUsedMomentsVisitor();
			e.accept(visitor);
			combinedProducts.addAll(visitor.getUsedCombinedMoments());
		}
		
		odeAnalysis = new PCTMCODEAnalysis(pctmc,order);
		odeAnalysis.setUsedMoments(combinedProducts);
		
		odeAnalysis.prepare(constants);		
		numericalPostprocessor = new ODEAnalysisNumericalPostprocessor(1.0,0.1,1); 
		numericalPostprocessor.prepare(odeAnalysis, constants);
		
		SystemOfODEs odes = numericalPostprocessor.getPreprocessedImplementation().getOdes();
		odes.setRates(constants.getFlatConstants());
		return odes;
	}
		
	public double[] getInitialValues(){
		return numericalPostprocessor.getInitialValues(constants);
	}
	
	public List<AbstractExpression> parseExpressionList(String string){
		List<AbstractExpression> ret = null;

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
		
		ret = (List<AbstractExpression>) compilerReturn;
		
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
		} 
		return ret; 
	}
	
	
	public AbstractExpressionEvaluator getExpressionEvaluator(List<AbstractExpression> expressions){
		return numericalPostprocessor.getExpressionEvaluator(expressions, constants); 		
	}
	
	
	public BiMap<CombinedPopulationProduct, Integer> getMomentIndex(){
		BiMap<CombinedPopulationProduct, Integer> momentIndex = odeAnalysis.getMomentIndex();
		return momentIndex;
	}
}
