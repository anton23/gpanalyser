package uk.ac.imperial.doc.gpa;

import uk.ac.imperial.doc.gpa.patterns.GPEPAPatternMatcher;
import uk.ac.imperial.doc.gpa.syntax.GPACompiler;
import uk.ac.imperial.doc.gpa.syntax.GPALexer;
import uk.ac.imperial.doc.gpa.syntax.GPAParser;
import uk.ac.imperial.doc.pctmc.PCTMCInterpreter;

public class GPAPMain {
	
	public static void main(String[] args){ 
		new PCTMCInterpreter(GPALexer.class, GPAParser.class, GPACompiler.class,GPEPAPatternMatcher.class).run(args);
	}	

}
