package org.msh.tb.br;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.msh.mdrtb.entities.FieldValue;

@Name("caseFiltersBR")
@Scope(ScopeType.SESSION)
public class CaseFiltersBR {

	private FieldValue resistanceType;
	
	private FieldValue schemaChanged;


	/**
	 * @return the resistanceType
	 */
	public FieldValue getResistanceType() {
		return resistanceType;
	}

	/**
	 * @param resistanceType the resistanceType to set
	 */
	public void setResistanceType(FieldValue resistanceType) {
		this.resistanceType = resistanceType;
	}

	/**
	 * @return the schemaChanged
	 */
	public FieldValue getSchemaChanged() {
		return schemaChanged;
	}

	/**
	 * @param schemaChanged the schemaChanged to set
	 */
	public void setSchemaChanged(FieldValue schemaChanged) {
		this.schemaChanged = schemaChanged;
	}
}
