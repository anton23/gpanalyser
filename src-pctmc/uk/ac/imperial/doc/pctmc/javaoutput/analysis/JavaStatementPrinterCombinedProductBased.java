package uk.ac.imperial.doc.pctmc.javaoutput.analysis;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IExpressionPrinterFactory;
import uk.ac.imperial.doc.jexpressions.expressions.IExpressionVisitor;
import uk.ac.imperial.doc.jexpressions.javaoutput.statements.JavaStatementPrinter;
import uk.ac.imperial.doc.jexpressions.statements.Assignment;
import uk.ac.imperial.doc.jexpressions.statements.IAssignmentVisitor;
import uk.ac.imperial.doc.jexpressions.statements.IIncrementVisitor;
import uk.ac.imperial.doc.jexpressions.statements.Increment;
import uk.ac.imperial.doc.jexpressions.variables.ExpressionVariable;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;

import com.google.common.collect.BiMap;

/**
 * Java statement printer.
 * 
 * @author Anton Stefanek
 * 
 */
public class JavaStatementPrinterCombinedProductBased extends
		JavaStatementPrinter implements IIncrementVisitor, IAssignmentVisitor {

	protected IExpressionPrinterFactory lhsFactory;
	
	protected Set<ExpressionVariable> rhsVariables;

	public JavaStatementPrinterCombinedProductBased(Constants parameters,
			Map<CombinedPopulationProduct, Integer> combinedMomentsIndex,
			Map<AbstractExpression, Integer> generalExpectationIndex,
			String oldY, String newY, boolean expandVariables) {
		super(new JavaCombinedProductBasedExpressionPrinterFactory(parameters,
				combinedMomentsIndex, generalExpectationIndex, oldY, expandVariables));
		lhsFactory = new JavaCombinedProductBasedExpressionPrinterFactory(
				parameters, combinedMomentsIndex, generalExpectationIndex, newY, expandVariables);
		rhsVariables = new HashSet<ExpressionVariable>();
	}

	@Override
	public void visit(Increment s) {
		IExpressionVisitor lhsPrinter = lhsFactory.createPrinter();
		s.getLhs().accept(lhsPrinter);
		String lhsString = lhsPrinter.toString();

		IExpressionVisitor rhsPrinter = expressionPrinterFactory
				.createPrinter();
		s.getRhs().accept(rhsPrinter);
		String rhsString = rhsPrinter.toString();
		output.append(lhsString);
		output.append("+=");
		output.append(rhsString);
		output.append(";");
		rhsVariables.addAll(((JavaPrinterCombinedProductBased)rhsPrinter).getVariables());
	}

	@Override
	public void visit(Assignment s) {
		IExpressionVisitor lhsPrinter = lhsFactory.createPrinter();
		s.getLhs().accept(lhsPrinter);
		String lhsString = lhsPrinter.toString();

		IExpressionVisitor rhsPrinter = expressionPrinterFactory
				.createPrinter();
		s.getRhs().accept(rhsPrinter);
		String rhsString = rhsPrinter.toString();
		output.append(lhsString);
		output.append("=");
		output.append(rhsString);
		output.append(";");
		rhsVariables.addAll(((JavaPrinterCombinedProductBased)rhsPrinter).getVariables());

	}

	public Set<ExpressionVariable> getRhsVariables() {
		return rhsVariables;
	}
	
	
}
