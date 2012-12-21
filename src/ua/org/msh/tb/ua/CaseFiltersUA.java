package org.msh.tb.ua;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Synchronized;
import org.msh.tb.entities.FieldValue;

@Name("caseFiltersUA")
@Scope(ScopeType.SESSION)
@Synchronized(timeout=10000L)
public class CaseFiltersUA {
	private FieldValue pulmonaryType;
	private FieldValue extrapulmonaryType;
	private FieldValue extrapulmonaryType2;
	
	public void clear(){
		pulmonaryType = null;
		extrapulmonaryType = null;
		extrapulmonaryType2 = null;
	}
	
	//=========GETTERS & SETTERS=========
	public FieldValue getPulmonaryType() {
		return pulmonaryType;
	}
	public void setPulmonaryType(FieldValue pulmonaryType) {
		this.pulmonaryType = pulmonaryType;
	}
	public FieldValue getExtrapulmonaryType() {
		return extrapulmonaryType;
	}
	public void setExtrapulmonaryType(FieldValue extrapulmonaryType) {
		this.extrapulmonaryType = extrapulmonaryType;
	}
	public FieldValue getExtrapulmonaryType2() {
		return extrapulmonaryType2;
	}
	public void setExtrapulmonaryType2(FieldValue extrapulmonaryType2) {
		this.extrapulmonaryType2 = extrapulmonaryType2;
	}

	
}
