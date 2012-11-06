package uk.ac.imperial.doc.pctmc.condor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import uk.ac.imperial.doc.pctmc.experiments.iterate.PlotAtDescription;
import uk.ac.imperial.doc.pctmc.experiments.iterate.RangeSpecification;
import uk.ac.imperial.doc.pctmc.interpreter.PCTMCFileRepresentation;
import uk.ac.imperial.doc.pctmc.utils.FileUtils;
import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;
import uk.ac.imperial.doc.pctmc.utils.PCTMCOptions;

public class CondorMerger extends CondorBase {

	public CondorMerger(PCTMCFileRepresentation fileRepresentation,
			String file, String options) {
		super(fileRepresentation, file, options);
	}
	
	public void merge() {
		PCTMCLogging.info("Merging output from condor jobs.");
		RangeSpecification range1 = iterate.getRanges().get(0);
		int dim1 = range1.getSteps();
		int dim2 = 1;
		RangeSpecification range2 = null;
		if (iterate.getRanges().size() == 2) {
			range2 = iterate.getRanges().get(1);
			dim2 = range2.getSteps();
		}
		
		for (PlotAtDescription p : iterate.getPlots()) {
			if (p.getFilename() != null && !p.getFilename().isEmpty()) {
				double[][] data = new double[dim1][dim2];
				for (int i = 0; i < PCTMCOptions.condor_parts; i++) {
					double[][] tmp = read3DFile(p.getFilename()+i);
					for (int j = 0; j < tmp.length; j++) {
						int xi = range1.getIndex(tmp[j][0]);
						int yi = 0;
						if (range2 != null) {
							yi = range2.getIndex(tmp[j][1]);
						}
						data[xi][yi] = tmp[j][2];
					}
				}
				if (range2 != null) {
					FileUtils.write3Dfile(p.getFilename(), data, range1.getFrom(), range1.getDc(), range2.getFrom(), range2.getDc());
				}
			}
		}
	}
	
	protected double[][] read3DFile(String file) {
		try {
			Scanner in = new Scanner(new FileInputStream(file));
			List<double[]> tmp = new LinkedList<double[]>();
			while (in.hasNext()) {
				double[] row = new double[3];
				String line = in.nextLine();
				if (line.length() == 0) continue;
				String[] numbers = line.split(" ");
				for (int i = 0; i < 3; i++){
					row[i] = Double.parseDouble(numbers[i]);
					tmp.add(row);
				}
			}			
			return tmp.toArray(new double[1][1]);                            			
		} catch (FileNotFoundException e) {
			throw new AssertionError("File " + file + " not found!");
		}
		
		
	}
	
	
}
