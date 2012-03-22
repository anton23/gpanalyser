package uk.ac.imperial.doc.pctmc.testing.iterate;

import java.util.List;

import org.junit.Test;

import uk.ac.imperial.doc.pctmc.experiments.iterate.RangeSpecification;

import static org.junit.Assert.assertEquals;

public class TestIterateSplitting {
	
	@Test
	public void testRangeSplitting() {
		RangeSpecification rs = new RangeSpecification("a", 5.0, 13.0, 1.0);
		List<RangeSpecification> threeParts = rs.split(3);
		assertEquals(3, threeParts.size());
		assertEquals(new RangeSpecification("a", 5.0, 7.0, 1.0), threeParts.get(0));
		assertEquals(new RangeSpecification("a", 8.0, 10.0, 1.0), threeParts.get(1));
		assertEquals(new RangeSpecification("a", 11.0, 13.0, 1.0), threeParts.get(2));
		
		List<RangeSpecification> fourParts = rs.split(4);
		assertEquals(new RangeSpecification("a", 5.0, 7.0, 1.0), fourParts.get(0));
		assertEquals(new RangeSpecification("a", 8.0, 9.0, 1.0), fourParts.get(1));
		assertEquals(new RangeSpecification("a", 10.0, 11.0, 1.0), fourParts.get(2));
		assertEquals(new RangeSpecification("a", 12.0, 13.0, 1.0), fourParts.get(3));
		
		List<RangeSpecification> fiveParts = rs.split(5);
		assertEquals(new RangeSpecification("a", 5.0, 6.0, 1.0), fiveParts.get(0));
		assertEquals(new RangeSpecification("a", 7.0, 8.0, 1.0), fiveParts.get(1));
		assertEquals(new RangeSpecification("a", 9.0, 10.0, 1.0), fiveParts.get(2));
		assertEquals(new RangeSpecification("a", 11.0, 12.0, 1.0), fiveParts.get(3));
		assertEquals(1, fiveParts.get(4).getSteps());
		assertEquals(0.0, fiveParts.get(4).getDc(), 0.0);
		assertEquals(new RangeSpecification("a", 13.0, 13.0, 0.0), fiveParts.get(4));
	}
}
