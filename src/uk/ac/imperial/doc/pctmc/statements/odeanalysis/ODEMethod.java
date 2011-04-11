package uk.ac.imperial.doc.pctmc.statements.odeanalysis;

import uk.ac.imperial.doc.jexpressions.statements.AbstractStatement;

public class ODEMethod {

	AbstractStatement[] body;

	public AbstractStatement[] getBody() {
		return body;
	}


	public ODEMethod(AbstractStatement[] body) {
		this.body = body;

	}

	public void accept(IODEMethodVisitor v) {
		if (v instanceof IODEMethodVisitor)
			((IODEMethodVisitor) v).visit(this);
	}

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();
		for (int i = 0; i < body.length; i++) {
			ret.append(body[i]);
			ret.append("\n");
		}
		return ret.toString();
	}

}
