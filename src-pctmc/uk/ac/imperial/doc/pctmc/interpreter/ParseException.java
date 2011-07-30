package uk.ac.imperial.doc.pctmc.interpreter;

import java.util.List;

@SuppressWarnings("serial")
public class ParseException  extends Exception {
		protected List<String> errors;

		public ParseException(List<String> errors) {
			super();
			this.errors = errors;
		}

		public List<String> getErrors() {
			return errors;
		} 
		
		

}
