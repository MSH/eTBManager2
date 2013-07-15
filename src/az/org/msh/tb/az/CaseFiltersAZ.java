package org.msh.tb.az;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.msh.tb.application.App;
import org.msh.tb.cases.CaseFilters;
import org.msh.tb.entities.Tag;

@Name("caseFiltersAZ")
@Scope(ScopeType.CONVERSATION)
public class CaseFiltersAZ {
	@In(create=true) CaseFilters caseFilters;
	
	private Tag tag;
	private String caseNums;
	private String caseNumbers;

	public void setTag(Tag tag) {
		this.tag = tag;
		//CaseFilters cf = (CaseFilters)App.getComponent("caseFilters");
		if (tag!=null)
			caseFilters.setTagid(tag.getId());
		else
			caseFilters.setTagid(null);
	}

	public Tag getTag() {
		//CaseFilters cf = (CaseFilters)App.getComponent("caseFilters");
		if (tag==null)
			if (caseFilters.getTagid()!=null)
				tag = (Tag)App.getEntityManager().find(Tag.class, caseFilters.getTagid());
			else 
				tag = null;
		return tag;
	}

	public void setCaseNums(String caseNums) {
		this.caseNums = caseNums;
	}

	public String getCaseNums() {
		return caseNums;
	}
	
	public String getCaseNumbers() {
		if (caseNumbers == null){
			String hql = null;
			if (!"".equals(caseNums) && caseNums != null){
				hql = "";
				String[] nums = caseNums.split(",");
				for (String num:nums){
					if (num.contains("-")){
						String [] range = num.split("-");
						try{
							int beg = Integer.parseInt(range[0].trim());
							int end = Integer.parseInt(range[range.length-1].trim());
							hql += ("".equals(hql)?"":" or ")+"p.recordNumber >= "+beg+" and p.recordNumber <="+end;
							/*for (int i = beg; i <= end; i++) {
								hql += i+",";
							}*/
						} catch (Exception e){
							System.out.println(e.getLocalizedMessage());
						}
					}
					else{
						hql += ("".equals(hql)?"":" or ")+"p.recordNumber = " + Integer.parseInt(num.trim());
					}
				}
				//hql = hql.substring(0, hql.length()-1);
			}
			caseNumbers = hql;
		}
		return caseNumbers;
	}
	/**
	 * Reinitialize the advanced filters
	 */
	public void clear() {
		tag = null;
		caseNumbers = null;
		caseNums = null;
	}
}
