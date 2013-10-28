package org.msh.tb.ua.cases;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Synchronized;
import org.msh.tb.application.App;
import org.msh.tb.cases.CaseFilters;
import org.msh.tb.entities.FieldValue;
import org.msh.tb.entities.Source;
import org.msh.tb.entities.Tag;
import org.msh.tb.ua.entities.enums.TreatmentType;

@Name("caseFiltersUA")
@Scope(ScopeType.SESSION)
@Synchronized(timeout=10000L)
public class CaseFiltersUA {
	private FieldValue pulmonaryType;
	private FieldValue extrapulmonaryType;
	private FieldValue extrapulmonaryType2;
	private Tag tag;
	private Source source;
	private TreatmentType ttype;

	public void clear(){
		pulmonaryType = null;
		extrapulmonaryType = null;
		extrapulmonaryType2 = null;
		tag = null;
		source = null;
		ttype = null;
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

	public void setTag(Tag tag) {
		this.tag = tag;
	}

	public Tag getTag() {
		CaseFilters cf = (CaseFilters)App.getComponent("caseFilters");
		if (cf!=null)
			if (cf.getTagid()!=null)
				tag = (Tag)App.getEntityManager().find(Tag.class, cf.getTagid());
		return tag;
	}

	public void setSource(Source source) {
		this.source = source;
	}

	public Source getSource() {
		return source;
	}

	public void setTtype(TreatmentType ttype) {
		this.ttype = ttype;
	}

	public TreatmentType getTtype() {
		return ttype;
	}
	
}
