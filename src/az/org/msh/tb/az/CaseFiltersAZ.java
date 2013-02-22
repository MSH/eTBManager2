package org.msh.tb.az;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Synchronized;
import org.msh.tb.application.App;
import org.msh.tb.cases.CaseFilters;
import org.msh.tb.entities.Tag;

@Name("caseFiltersAZ")
@Scope(ScopeType.SESSION)
@Synchronized(timeout=10000L)
public class CaseFiltersAZ {
	private Tag tag;
	private String caseNums;

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

	public void setCaseNums(String caseNums) {
		this.caseNums = caseNums;
	}

	public String getCaseNums() {
		return caseNums;
	}
}
