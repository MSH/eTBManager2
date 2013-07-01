package org.msh.tb.cases;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.TagsCasesHome;
import org.msh.tb.entities.CaseSideEffect;
import org.msh.tb.entities.TbCase;
import org.msh.tb.misc.FieldsQuery;


/**
 * Manage the adverse reactions of a case, with the possibility to read an existing
 * adverse reaction record, including a new one, editing an existing one or deleting one
 *  
 * @author Ricardo Memoria
 *
 */
@Name("sideEffectHome")
public class SideEffectHome extends WsEntityHome<CaseSideEffect>{
	private static final long serialVersionUID = 4590228131339634325L;

	@In(required=true) CaseHome caseHome;
	@In(create=true) FieldsQuery fieldsQuery;
	@In(create=true) FacesMessages facesMessages;
	private List<CaseSideEffect> results;

	
	/**
	 * Return the list of adverse reactions of a case
	 * @return List of objects of {@link CaseSideEffect} type 
	 */
	public List<CaseSideEffect> getResults(){
		if (results == null)
			results = createResults();
		return results;
	}

	/**
	 * Factory method to return an instance of {@link CaseSideEffect}
	 * @return instance of {@link CaseSideEffect} class
	 */
	@Factory("caseSideEffect")
	public CaseSideEffect getCaseSideEffect() {
		return getInstance();
	}

	/**
	 * Saves the changes made to the side effects of the case
	 * @return - "persisted" if it was successfully saved
	 */
	public String save() {
		if(!validateForm()){
			return "error";
		}
		
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
		String[] strMonths = {"1","2","3","4","5","6","7","8","9","10","12", "13", "14","15","16","17","18","19","20","21","22","23","24",">24"};
//		SelectItem item1 = new SelectItem();
//		item1.setLabel("-");
//		item1.setValue(0);
//		lst.add(item1);
		
		for (int i = 0; i< strMonths.length; i++) {
			SelectItem item = new SelectItem();
			item.setLabel(strMonths[i]);
			item.setValue(strMonths[i]);
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


	/** {@inheritDoc}
	 */
	@Override
	public String remove(){
		String s = super.remove();
		results = createResults();
		if(s.equals("removed"))
			return "sideeffectremoved";
		else
			return s;
	}
	
	public boolean validateForm(){
		boolean validationError = false;
		
		if(getCaseSideEffect().getMonth() == 0){
			facesMessages.addToControlFromResourceBundle("month", "javax.faces.component.UIInput.REQUIRED");
			validationError = true;
		}else if(getCaseSideEffect().getId() == null){ //new sideeffect		
			for(CaseSideEffect c : caseHome.getTbCase().getSideEffects()){
				if(c.getSideEffect().getValue().getId().equals(getCaseSideEffect().getSideEffect().getValue().getId()) 
							&& c.getMonth() == getCaseSideEffect().getMonth()){
					facesMessages.addToControlFromResourceBundle("month", "cases.sideeffects.samemonth");
					validationError = true;
				}
			}
		}
		
		return !validationError;
	}
}
