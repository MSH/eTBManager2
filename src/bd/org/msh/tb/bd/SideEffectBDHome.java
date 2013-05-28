package org.msh.tb.bd;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;

import org.hibernate.validator.InvalidStateException;
import org.hibernate.validator.InvalidValue;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.TagsCasesHome;
import org.msh.tb.bd.entities.CaseSideEffectBD;
import org.msh.tb.bd.entities.TbCaseBD;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.CaseSideEffect;
import org.msh.tb.entities.FieldValue;
import org.msh.tb.entities.TbCase;
import org.msh.tb.misc.FieldsQuery;
import org.msh.utils.ItemSelect;
import org.msh.utils.date.Period;


@Name("sideEffectBDHome")
@Scope(ScopeType.CONVERSATION)
public class SideEffectBDHome extends EntityHomeEx<CaseSideEffectBD>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4954787062161078703L;
	@In(required=true) CaseHome caseHome;
	@In(create=true) FieldsQuery fieldsQuery;
	@In(create=true) EntityManager entityManager;
	@In(create=true) FacesMessages facesMessages;
	
	private List<ItemSelect<CaseSideEffectBD>> items;
	
	
	public List<ItemSelect<CaseSideEffectBD>> getItems() {
		if (items == null)
			createItems();
		return items;
	}
	
	@Factory("caseSideEffectBD")
	public CaseSideEffectBD getCaseSideEffectBD() {
		return getInstance();
	}
	

	/**
	 * Create the side effect list to be selected by the user
	 */
	protected void createItems() {
		List<FieldValue> sideEffects = fieldsQuery.getSideEffects();
				
		items = new ArrayList<ItemSelect<CaseSideEffectBD>>();
		
		TbCaseBD casedata = (TbCaseBD)caseHome.getInstance();
		
		for (FieldValue sideEffect: sideEffects) {
			ItemSelect<CaseSideEffectBD> item = new ItemSelect<CaseSideEffectBD>();

			CaseSideEffectBD cse = casedata.findSideEffectData(sideEffect);
			if (cse == null) {
				cse = new CaseSideEffectBD();
				cse.getSideEffect().setValue(sideEffect);
			}
			else item.setSelected(true);

			item.setItem(cse);
			items.add(item);
		}
	}


	/**
	 * Saves the changes made to the side effects of the case
	 * @return - "persisted" if it was successfully saved
	 */
	@Transactional
	public String save() {
//		String result = null;
//		try{
//		TbCase tbcase = caseHome.getInstance();
//		
//		List<CaseSideEffectBD> lst = ItemSelectHelper.getSelectedItems(getItems(), true);
//		for (CaseSideEffectBD it: lst) {
////			it.setCaseData(caseData);
//			it.setTbcase(tbcase);
//			it.setTbcasebd((TbCaseBD)tbcase);
//			if ((it.getSubstance() != null) && (it.getSubstance2() != null) && (it.getSubstance().equals(it.getSubstance2())))
//				it.setSubstance2(null);
//
//			if ((it.getSubstance() == null) && (it.getSubstance2() != null)) {
//				it.setSubstance(it.getSubstance2());
//				it.setSubstance2(null);
//			}
//			String name ="";
//			if(it.getSubstance()!= null)
//				name += it.getSubstance().getAbbrevName().getName1();
//			if(it.getSubstance2()!= null)
//				name += " "+it.getSubstance2().getAbbrevName().getName1();
//			it.setMedicines(name);
//			entityManager.persist(it);
//		}
//		
//		for (CaseSideEffect it:tbcase.getSideEffects()) {
//			if (!lst.contains(it)) {
//				entityManager.remove(it);
//			}
//		}
//		entityManager.flush();
//		entityManager.refresh(tbcase);
//		return "persisted";
//		}catch (InvalidStateException iv){
//			iv.printStackTrace();
//			InvalidValue[] values = iv.getInvalidValues();
//			for (int i = 0; i < values.length; i++) {
//				InvalidValue invalidValue = values[i];
//				System.err.println("getPropertyName === "+invalidValue.getPropertyName());
//				System.err.println("getMessage === "+invalidValue.getValue());
//				System.err.println("getMessage === "+invalidValue.getPropertyPath());
//				System.err.println("getMessage === "+invalidValue.getMessage());
//			}
//		}
//		return result;
		

		if(!validateForm()){
			return "error";
		}
		
		 TbCase tbcase = caseHome.getInstance();

		CaseSideEffectBD it = getInstance();
		
		it.setTbcase(tbcase);
		it.setTbcasebd((TbCaseBD)tbcase);
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
try{
		getEntityManager().persist(it);
		TagsCasesHome.instance().updateTags(tbcase);
}
	catch(InvalidStateException e) {

		
		for (InvalidValue invalidValue : e.getInvalidValues()) {
			System.out.println("Instance of bean class: " + invalidValue.getBeanClass().getSimpleName() +
             " has an invalid property: " + invalidValue.getPropertyName() +
             " with message: " + invalidValue.getMessage());
		}
	}
	 catch(Throwable e){
         System.out.println(e.getMessage());
         System.out.println(e.getCause());
     }
		
		return "persisted";
	
}
	
	/**
	 * Returns the list of months to be selected in the field 'month of treatment' in the side effect data of the case
	 * @return - List of objects<SelectItem> to be used in a selectOneMenu component
	 */
	public List<SelectItem> getMonths() {
		List<SelectItem> lst = new ArrayList<SelectItem>();
		
		TbCase tbcase = caseHome.getInstance();
		Period p = tbcase.getTreatmentPeriod();
		
		int numMonths;
		if ((p == null) || (p.isEmpty()))
			numMonths = 12;
		else numMonths = p.getMonths();
		
		for (int i = 1; i<= numMonths; i++) {
			SelectItem item = new SelectItem();
			item.setLabel(Integer.toString(i));
			item.setValue(i);
			lst.add(item);
		}
		
		return lst;
	}
	
	public boolean validateForm(){
		boolean validationError = false;
		
		if(getCaseSideEffectBD().getMonth() == 0){
			facesMessages.addToControlFromResourceBundle("month", "javax.faces.component.UIInput.REQUIRED");
			validationError = true;
		}else if(getCaseSideEffectBD().getId() == null){ //new sideeffect		
			for(CaseSideEffect c : caseHome.getTbCase().getSideEffects()){
				if(c.getSideEffect().getValue().getId().equals(getCaseSideEffectBD().getSideEffect().getValue().getId()) 
							&& c.getMonth() == getCaseSideEffectBD().getMonth()){
					facesMessages.addToControlFromResourceBundle("month", "cases.sideeffects.samemonth");
					validationError = true;
				}
			}
		}
		
		return !validationError;
	}
}
