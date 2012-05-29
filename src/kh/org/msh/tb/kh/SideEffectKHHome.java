package org.msh.tb.kh;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;

import org.hibernate.validator.InvalidStateException;
import org.hibernate.validator.InvalidValue;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.msh.tb.TagsCasesHome;
import org.msh.tb.cases.CaseHome;

import org.msh.tb.entities.CaseSideEffect;
import org.msh.tb.entities.FieldValue;
import org.msh.tb.entities.TbCase;
import org.msh.tb.kh.entities.CaseSideEffectKH;
import org.msh.tb.kh.entities.TbCaseKH;
import org.msh.tb.misc.FieldsQuery;
import org.msh.utils.ItemSelect;
import org.msh.utils.ItemSelectHelper;
import org.msh.utils.date.Period;

@Name("sideEffectKHHome")
public class SideEffectKHHome {

	@In(required=true) CaseHome caseHome;
	@In(create=true) FieldsQuery fieldsQuery;
	@In(create=true) EntityManager entityManager;
	
	private List<ItemSelect<CaseSideEffectKH>> items;
	
	/**
	 * Returns side effect list of the case to be selected by the user
	 * @return
	 */
	public List<ItemSelect<CaseSideEffectKH>> getItems() {
		if (items == null)
			createItems();
		return items;
	}
	

	/**
	 * Create the side effect list to be selected by the user
	 */
	protected void createItems() {
		List<FieldValue> sideEffects = fieldsQuery.getSideEffects();
		
		TbCaseKH tbcase = (TbCaseKH) caseHome.getInstance();
		
		items = new ArrayList<ItemSelect<CaseSideEffectKH>>();
		
		for (FieldValue sideEffect: sideEffects) {
			ItemSelect item = new ItemSelect();

			CaseSideEffectKH cse = tbcase.findSideEffectData(sideEffect);
			if (cse == null) {
				cse = new CaseSideEffectKH();
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
	public String save() {
		String result = null;
		try{
		TbCase tbcase = caseHome.getInstance();
		
		List<CaseSideEffectKH> lst = ItemSelectHelper.getSelectedItems(getItems(), true);
		for (CaseSideEffectKH it: lst) {
//			it.setCaseData(caseData);
			it.setTbcase(tbcase);
			it.setTbcasekh((TbCaseKH)tbcase);
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
			entityManager.persist(it);
		}
		
		for (CaseSideEffect it:tbcase.getSideEffects()) {
			if (!lst.contains(it)) {
				entityManager.remove(it);
			}
		}
		entityManager.flush();
		entityManager.refresh(tbcase);
		return "persisted";
		}catch (InvalidStateException iv){
			iv.printStackTrace();
			InvalidValue[] values = iv.getInvalidValues();
			for (int i = 0; i < values.length; i++) {
				InvalidValue invalidValue = values[i];
				System.err.println("getPropertyName === "+invalidValue.getPropertyName());
				System.err.println("getMessage === "+invalidValue.getValue());
				System.err.println("getMessage === "+invalidValue.getPropertyPath());
				System.err.println("getMessage === "+invalidValue.getMessage());
			}
		}
		return result;
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
}
