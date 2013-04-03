package org.msh.tb.reports2.variables;

import org.msh.tb.reports2.VariableImpl;


public class EnumFieldVariable extends VariableImpl {

	public EnumFieldVariable(String id, String keylabel, String fieldName, Class<? extends Enum> enumClass) {
		super(id, keylabel, fieldName, null);
	}
	

}
