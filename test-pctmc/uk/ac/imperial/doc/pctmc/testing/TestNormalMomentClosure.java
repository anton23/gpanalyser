package uk.ac.imperial.doc.pctmc.testing;


import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Set;

import org.junit.Test;

import uk.ac.imperial.doc.pctmc.odeanalysis.GetVVersionVisitorMomentClosure;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class TestNormalMomentClosure {
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	@Test
	public void testPartitions() {
		Set<Set<List<Integer>>> expected = (Set)Sets.newHashSet(
			(Set)Sets.newHashSet((List)Lists.newArrayList(1,2),(List)Lists.newArrayList(3,4)),
			(Set)Sets.newHashSet((List)Lists.newArrayList(1,3),(List)Lists.newArrayList(2,4)),		
			(Set)Sets.newHashSet((List)Lists.newArrayList(1,4),(List)Lists.newArrayList(2,3))		
		);
		assertEquals(expected, GetVVersionVisitorMomentClosure.getAllPartitionsIntoPairs(Lists.newArrayList(1,2,3,4)));
		
		Set<Set<List<Integer>>> expected2 = (Set)Sets.newHashSet(
				(Set)Sets.newHashSet((List)Lists.newArrayList(1,2),(List)Lists.newArrayList(3,4),(List)Lists.newArrayList(5,6)),
				(Set)Sets.newHashSet((List)Lists.newArrayList(1,3),(List)Lists.newArrayList(2,4),(List)Lists.newArrayList(5,6)),		
				(Set)Sets.newHashSet((List)Lists.newArrayList(1,4),(List)Lists.newArrayList(2,3),(List)Lists.newArrayList(5,6)),
				
				(Set)Sets.newHashSet((List)Lists.newArrayList(1,2),(List)Lists.newArrayList(3,5),(List)Lists.newArrayList(4,6)),
				(Set)Sets.newHashSet((List)Lists.newArrayList(1,3),(List)Lists.newArrayList(2,5),(List)Lists.newArrayList(4,6)),		
				(Set)Sets.newHashSet((List)Lists.newArrayList(1,5),(List)Lists.newArrayList(2,3),(List)Lists.newArrayList(4,6)),
				
				(Set)Sets.newHashSet((List)Lists.newArrayList(1,2),(List)Lists.newArrayList(4,5),(List)Lists.newArrayList(3,6)),
				(Set)Sets.newHashSet((List)Lists.newArrayList(1,4),(List)Lists.newArrayList(2,5),(List)Lists.newArrayList(3,6)),		
				(Set)Sets.newHashSet((List)Lists.newArrayList(1,5),(List)Lists.newArrayList(2,4),(List)Lists.newArrayList(3,6)),
				
				(Set)Sets.newHashSet((List)Lists.newArrayList(1,3),(List)Lists.newArrayList(4,5),(List)Lists.newArrayList(2,6)),
				(Set)Sets.newHashSet((List)Lists.newArrayList(1,4),(List)Lists.newArrayList(3,5),(List)Lists.newArrayList(2,6)),		
				(Set)Sets.newHashSet((List)Lists.newArrayList(1,5),(List)Lists.newArrayList(3,4),(List)Lists.newArrayList(2,6)),
				
				(Set)Sets.newHashSet((List)Lists.newArrayList(2,3),(List)Lists.newArrayList(4,5),(List)Lists.newArrayList(1,6)),
				(Set)Sets.newHashSet((List)Lists.newArrayList(2,4),(List)Lists.newArrayList(3,5),(List)Lists.newArrayList(1,6)),		
				(Set)Sets.newHashSet((List)Lists.newArrayList(2,5),(List)Lists.newArrayList(3,4),(List)Lists.newArrayList(1,6))
			);
		assertEquals(expected2, GetVVersionVisitorMomentClosure.getAllPartitionsIntoPairs(Lists.newArrayList(1,2,3,4,5,6)));
	}

}
