package uk.ac.imperial.doc.jexpressions.utils;

import java.util.Iterator;
import java.util.Map;

public class ToStringUtils {
	public static String mapToDefinitionList(Map<?, ?> map, String def,
			String semi) {
		String ret = "";
		for (Map.Entry<?, ?> e : map.entrySet()) {
			ret += e.getKey().toString() + def + e.getValue().toString() + semi;
		}
		return ret;
	}

	@SuppressWarnings("all")
	public static String iterableToSSV(Iterable list, String sep) {
		String ret = "";
		for (Iterator it = list.iterator(); it.hasNext();) {
			ret += it.next().toString();
			if (it.hasNext())
				ret += sep;
		}
		return ret;
	}

}
