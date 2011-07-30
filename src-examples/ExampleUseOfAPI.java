import uk.ac.imperial.doc.gpa.GPAPMain;
import uk.ac.imperial.doc.pctmc.analysis.AbstractPCTMCAnalysis;
import uk.ac.imperial.doc.pctmc.analysis.plotexpressions.PlotDescription;
import uk.ac.imperial.doc.pctmc.interpreter.PCTMCFileRepresentation;
import uk.ac.imperial.doc.pctmc.interpreter.PCTMCInterpreter;
import uk.ac.imperial.doc.pctmc.interpreter.ParseException;
import uk.ac.imperial.doc.pctmc.postprocessors.numerical.NumericalPostprocessor;


public class ExampleUseOfAPI {
	public static void main(String[] args){
		PCTMCInterpreter gpepaInterpreter = GPAPMain.createGPEPAInterpreter();
		try {
			PCTMCFileRepresentation fileRepresentation = gpepaInterpreter.parseFile("src-examples/clientServer.gpepa");
			System.out.println("Parsed a GPEPA file with following conents:");
			System.out.println("Constants:\n"+fileRepresentation.getConstants().toString());
			System.out.println("PCTMC:\n" + fileRepresentation.getPctmc());
			System.out.println("Analyses:\n" + fileRepresentation.getPlots());
			
			AbstractPCTMCAnalysis analysis = fileRepresentation.getPlots().keySet().iterator().next();
			System.out.println("The first analysis has these postprocessors:\n"+analysis.getPostprocessors());
			((NumericalPostprocessor)analysis.getPostprocessors().iterator().next()).setStopTime(50.0);
			
			fileRepresentation.getPlots().put(analysis,new PlotDescription(gpepaInterpreter.parseExpressionList("Var[Clients:Client]^0.5, Var[Servers:Server]^0.5")));
			
			gpepaInterpreter.processFileRepresentation(fileRepresentation);
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}
