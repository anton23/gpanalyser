package uk.ac.imperial.doc.masspa.gui.util;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTreeNodeStream;

import uk.ac.imperial.doc.masspa.language.Messages;
import uk.ac.imperial.doc.masspa.pctmc.MASSPAPCTMC;
import uk.ac.imperial.doc.masspa.representation.components.MASSPAAgents;
import uk.ac.imperial.doc.masspa.syntax.MASSPACompiler;
import uk.ac.imperial.doc.masspa.syntax.MASSPALexer;
import uk.ac.imperial.doc.masspa.syntax.MASSPAParser;
import uk.ac.imperial.doc.masspa.syntax.MASSPACompiler_PCTMCCompilerPrototype.system_return;
import uk.ac.imperial.doc.masspa.util.MASSPALogging;

/**
 * Validates and compiles
 * MASSPA-Agent definitions
 * @author Chris Guenther
 */
public class MASSPAAgentsValidator
{	
	public static MASSPAAgents validate(String _agentDef)
	{
		String agentDef = _agentDef + "\n Locations = {(0)};"; // Dummy location
		
		// Compile agents
		CharStream stream = new ANTLRStringStream(agentDef);
		MASSPALexer lexer = new MASSPALexer(stream);
		TokenStream tokenStream = new CommonTokenStream(lexer);
		MASSPAParser parser = new MASSPAParser(tokenStream);
		system_return ret = null;
		try
		{
			MASSPACompiler compiler = new MASSPACompiler(new CommonTreeNodeStream(parser.system().getTree()));
			ret = compiler.system();
			return ((MASSPAPCTMC)ret.pctmc).getModel().getMASSPAAgents();
		}
		catch (Exception e)
		{
			MASSPALogging.error(Messages.s_AGENT_EDITOR_COMPILER_ERROR);
		}
		return null;
	}
}
