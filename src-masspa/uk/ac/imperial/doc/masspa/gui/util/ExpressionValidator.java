package uk.ac.imperial.doc.masspa.gui.util;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTreeNodeStream;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.masspa.syntax.MASSPACompiler;
import uk.ac.imperial.doc.masspa.syntax.MASSPALexer;
import uk.ac.imperial.doc.masspa.syntax.MASSPAParser;

/**
 * Validates expressions
 * @author Chris Guenther
 */
public class ExpressionValidator
{
	public static AbstractExpression validate(String _expr)
	{
		_expr = (_expr.endsWith(";")) ? _expr : _expr + ";";
		CharStream stream = new ANTLRStringStream(_expr);
		MASSPALexer lexer = new MASSPALexer(stream);
		TokenStream tokenStream = new CommonTokenStream(lexer);
		MASSPAParser parser = new MASSPAParser(tokenStream);
		try
		{
			MASSPACompiler compiler = new MASSPACompiler(new CommonTreeNodeStream(parser.expression().getTree()));
			return compiler.expression();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
}
