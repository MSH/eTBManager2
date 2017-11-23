package org.msh.utils;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("validationModel")
@Scope(ScopeType.EVENT)
public class ValidationModel {

	private boolean ok;
	
	/**
	 * If the validation process is executed, the ok variable is set to true
	 */
	public void check() {
		ok = true;
	}
	
	public boolean getOk() {
		return ok;
	}
}
