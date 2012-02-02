package uk.ac.imperial.doc.pctmc.cppoutput.analysis;

import com.google.common.collect.BiMap;
import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.expressions.IExpressionPrinterFactory;
import uk.ac.imperial.doc.jexpressions.expressions.IExpressionVisitor;
import uk.ac.imperial.doc.jexpressions.statements.Assignment;
import uk.ac.imperial.doc.jexpressions.statements.IAssignmentVisitor;
import uk.ac.imperial.doc.jexpressions.statements.IIncrementVisitor;
import uk.ac.imperial.doc.jexpressions.statements.Increment;
import uk.ac.imperial.doc.pctmc.cppoutput.statements.CPPStatementPrinter;
import uk.ac.imperial.doc.pctmc.expressions.CombinedPopulationProduct;

import java.util.Map;

/**
 * Java statement printer.
 * 
 * @author Anton Stefanek
 * 
 */
public class CPPStatementPrinterCombinedProductBased extends
        CPPStatementPrinter implements IIncrementVisitor, IAssignmentVisitor {

	protected IExpressionPrinterFactory lhsFactory;

	public CPPStatementPrinterCombinedProductBased(Constants parameters,
           BiMap<CombinedPopulationProduct, Integer> combinedMomentsIndex,
           Map<AbstractExpression, Integer> generalExpectationIndex,
           String oldY, String newY) {
		super(new CPPCombinedProductBasedExpressionPrinterFactory(parameters,
				combinedMomentsIndex, generalExpectationIndex, oldY));
		lhsFactory = new CPPCombinedProductBasedExpressionPrinterFactory(
				parameters, combinedMomentsIndex, generalExpectationIndex, newY);
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
	}
}
