package scripts.closurecomparison;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import uk.ac.imperial.doc.jexpressions.constants.Constants;
import uk.ac.imperial.doc.pctmc.experiments.iterate.RangeSpecification;
import uk.ac.imperial.doc.pctmc.utils.PCTMCLogging;

public abstract class RangeRunner {
	protected List<RangeSpecification> ranges;
	protected List<RangeRunner> parts;
	protected boolean toplevel;
	
	protected List<RangeRunner> split(int n) {
		List<RangeRunner> ret = new ArrayList<RangeRunner>(n);
		if (n==1) {
			ret.add(this);
			return ret;
		}
		List<RangeSpecification> firstRangeParts = ranges.get(0).split(n);
		List<RangeSpecification> restOfRanges = ranges.subList(1, ranges.size());
		for (RangeSpecification r:firstRangeParts) {
			List<RangeSpecification> tmpRanges = new ArrayList<RangeSpecification>();
			tmpRanges.add(r);
			tmpRanges.addAll(restOfRanges);
			ret.add(createSlave(tmpRanges, 1));
		}
		return ret;		
	}
	
	
	public RangeRunner(List<RangeSpecification> ranges, int nParts, boolean toplevel) {
		this.ranges = ranges;		
		this.parts = split(nParts);		
		this.toplevel = toplevel;
	}
	
	protected RangeRunner(List<RangeSpecification> ranges, boolean toplevel) {
		this.ranges = ranges;
		this.toplevel = toplevel;
	}

	public void run(final Constants constants) {	
		if (parts.size() > 1) {
			PCTMCLogging.setVisible(false);
			ExecutorService es = Executors.newFixedThreadPool(parts.size());

			for (final RangeRunner part:parts) {				
				Runnable r = new Runnable() {
					@Override
					public void run() {
						part.run(constants.getCopyOf());						
					}
				};
				es.submit(r);								
			}
		
			try {
				es.shutdown();
				es.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			PCTMCLogging.setVisible(true);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			join(constants);
		} else {
			runSerial(constants);
		}		
		if (toplevel) {
			processData(constants);
		}
	}
	
	protected abstract void join(Constants constants);
	protected abstract RangeRunner createSlave(List<RangeSpecification> ranges, int nParts);
	protected abstract void runSingle(Constants constants);
	protected abstract void processData(Constants constants);
	
	protected void runSerial(Constants originalConstants) {
		Constants constants = originalConstants.getCopyOf();
		int steps[] = new int[ranges.size()];
		int step[] = new int[ranges.size()];
		RangeSpecification[] rangesArray = new RangeSpecification[ranges.size()];
		int r = 0;
		int totalSteps = 1;
		for (RangeSpecification ra : ranges) {
			steps[r] = ra.getSteps();
			totalSteps *= steps[r];
			rangesArray[r] = ra;
			r++;
		}
		int show = Math.max(totalSteps / 5, 1);
		int iterations = 0;
		do {
			for (int s = 0; s < step.length; s++) {
				constants.setConstantValue(rangesArray[s].getConstant(),
						rangesArray[s].getStep(step[s]));
			}
			runSingle(constants);
			iterations++;
			if ((iterations) % show == 0) {
				PCTMCLogging.infoForce(iterations + " out of " + totalSteps + " iterations finished.");
			}
		} while (next(step, steps));
	}
	
	
	public List<RangeSpecification> getRanges() {
		return ranges;
	}

	public static boolean next(int[] is, int[] steps) {
		int i = 0;

		while (i < steps.length && ++is[i] == steps[i]) {
			is[i] = 0;
			i++;
		}
		return i < steps.length;
	}
	
}
