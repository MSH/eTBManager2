package org.msh.tb.cases;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.TagsCasesHome;
import org.msh.tb.entities.CaseSideEffect;
import org.msh.tb.entities.TbCase;
import org.msh.tb.misc.FieldsQuery;


@Name("sideEffectHome")
public class SideEffectHome extends EntityHomeEx<CaseSideEffect>{

	@In(required=true) CaseHome caseHome;
	@In(create=true) FieldsQuery fieldsQuery;
	private List<CaseSideEffect> results;

	public void setResults(List<CaseSideEffect> results) {
		this.results = results;
	}
	
	public List<CaseSideEffect> getResults(){
		if (results == null)
			results = createResults();
		return results;
	}

	@Factory("caseSideEffect")
	public CaseSideEffect getCaseSideEffect() {
		return getInstance();
	}

	/**
	 * Saves the changes made to the side effects of the case
	 * @return - "persisted" if it was successfully saved
	 */
	public String save() {
		TbCase tbcase = caseHome.getInstance();

		CaseSideEffect it = getInstance();
		
		it.setTbcase(tbcase);
		if ((it.getSubstance() != null) && (it.getSubstance2() != null) && (it.getSubstance().equals(it.getSubstance2())))
			it.setSubstance2(null);

		if ((it.getSubstance() == null) && (it.getSubstance2() != null)) {
			it.setSubstance(it.getSubstance2());
			it.setSubstance2(null);
		}
		String name ="";
		if(it.getSubstance()!= null)
			name += it.getSubstance().getAbbrevName().getName1();
		if(it.getSubstance2()!= null)
			name += " "+it.getSubstance2().getAbbrevName().getName1();
		it.setMedicines(name);

		getEntityManager().persist(it);

		TagsCasesHome.instance().updateTags(tbcase);
		
		return "persisted";
	}
	
	/**
	 * Returns the list of months to be selected in the field 'month of treatment' in the side effect data of the case
	 * @return - List of objects<SelectItem> to be used in a selectOneMenu component
	 */
	public List<SelectItem> getMonths() {
		List<SelectItem> lst = new ArrayList<SelectItem>();
		
		SelectItem item1 = new SelectItem();
		item1.setLabel("-");
		item1.setValue(0);
		lst.add(item1);
		
		for (int i = 1; i<= 24; i++) {
			SelectItem item = new SelectItem();
			item.setLabel(Integer.toString(i));
			item.setValue(i);
			lst.add(item);
		}
		
		return lst;
	}
	
	public List<SelectItem> getYears(){
		List<SelectItem> lst = new ArrayList<SelectItem>();
		String[] strYears = {"1","2","3","4","5","6","7","8","9","10",">10"};
		for (int i = 0; i< strYears.length; i++) {
			SelectItem item = new SelectItem();
			item.setLabel(strYears[i]);
			item.setValue(strYears[i]);
			lst.add(item);
		}
		return lst;
	}
	
	public String getResultsHQL() {
		String hql = "from CaseSideEffect c where c.tbcase.id = #{tbcase.id}";

		return hql.concat(" order by c.month desc");
	}
	
	/**
	 * Creates the list of exam results
	 * @param lastRes
	 * @return
	 */
	protected List<CaseSideEffect> createResults() {
		return getEntityManager()
			.createQuery(getResultsHQL())
			.getResultList();
	}
	
	public String remove(){
		String s = super.remove();
		results = createResults();
		if(s.equals("removed"))
			return "sideeffectremoved";
		else
			return s;
	}
}
