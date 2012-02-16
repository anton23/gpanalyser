package uk.ac.imperial.doc.pctmc.analysis.plotexpressions;

import java.util.List;

import uk.ac.imperial.doc.jexpressions.expressions.AbstractExpression;
import uk.ac.imperial.doc.jexpressions.utils.ToStringUtils;

public class PlotDescription {

	private List<AbstractExpression> expressions;
	private String filename;

	public PlotDescription(List<AbstractExpression> expressions) {
		this(expressions, "");
	}

	public PlotDescription(List<AbstractExpression> expressions, String filename) {
		super();
		this.expressions = expressions;
		this.filename = filename;
	}

	public String[] getLabels() {
		String[] ret = new String[expressions.size()];
		int i = 0;
		for (AbstractExpression e : expressions) {
			ret[i++] = e.toString();
		}
		return ret;
	}

	public List<AbstractExpression> getExpressions() {
		return expressions;
	}

	public String getFilename() {
		return filename;
	}

	@Override
	public String toString() {
		return ToStringUtils.iterableToSSV(expressions, ",")
				+ (filename.equals("") ? "" : ("->\"" + filename + "\""));
	}
}
