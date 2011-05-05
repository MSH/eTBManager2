package org.msh.tb.na;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.hibernate.validator.InvalidStateException;
import org.hibernate.validator.InvalidValue;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.FieldValue;
import org.msh.tb.entities.TbCase;
import org.msh.tb.misc.FieldsQuery;
import org.msh.tb.na.entities.CaseDataNA;
import org.msh.tb.na.entities.CaseSideEffectNA;
import org.msh.utils.ItemSelect;
import org.msh.utils.ItemSelectHelper;
import org.msh.utils.date.Period;


@Name("sideEffectNAHome")
public class SideEffectNAHome{

	@In(required=true) CaseHome caseHome;
	@In(required=true, create=true) CaseDataNAHome caseDataNAHome;
	@In(create=true) FieldsQuery fieldsQuery;
	
	private List<ItemSelect<CaseSideEffectNA>> items;
	
	
	public List<ItemSelect<CaseSideEffectNA>> getItems() {
		if (items == null)
			createItems();
		return items;
	}
	

	/**
	 * Create the side effect list to be selected by the user
	 */
	protected void createItems() {
		List<FieldValue> sideEffects = fieldsQuery.getSideEffects();
				
		items = new ArrayList<ItemSelect<CaseSideEffectNA>>();
		
		CaseDataNA casedata = caseDataNAHome.getCaseDataNA();
		
		for (FieldValue sideEffect: sideEffects) {
			ItemSelect item = new ItemSelect();

			CaseSideEffectNA cse = casedata.findSideEffectData(sideEffect);
			if (cse == null) {
				cse = new CaseSideEffectNA();
				cse.setSideEffect(sideEffect);
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
		CaseDataNA caseData = caseDataNAHome.getCaseDataNA();
		TbCase tbcase = caseHome.getInstance();
		
		List<CaseSideEffectNA> lst = ItemSelectHelper.getSelectedItems(getItems(), true);
		for (CaseSideEffectNA it: lst) {
//			it.setCaseData(caseData);
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
		}
		
		for (CaseSideEffectNA it:caseData.getSideEffects()) {
			if (!lst.contains(it)) {
				caseHome.getEntityManager().remove(it);
			}
		}
//		caseData.setTbcase(caseHome.getInstance());
		caseData.setSideEffects(lst);
		result = caseDataNAHome.persist();
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
}
