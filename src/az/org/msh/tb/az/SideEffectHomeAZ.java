package org.msh.tb.az;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.TagsCasesHome;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.CaseSideEffect;
import org.msh.tb.entities.FieldValue;
import org.msh.tb.entities.FieldValueComponent;
import org.msh.tb.entities.TbCase;
import org.msh.tb.misc.FieldsQuery;
import org.msh.utils.ItemSelect;
import org.msh.utils.ItemSelectHelper;
import org.msh.utils.date.Period;


@Name("sideEffectHomeAZ")
public class SideEffectHomeAZ {

	@In(required=true) CaseHome caseHome;
	@In(create=true) FieldsQuery fieldsQuery;
	
	private List<ItemSelect<CaseSideEffect>> items;
	
	/**
	 * Returns side effect list of the case to be selected by the user
	 * @return
	 */
	public List<ItemSelect<CaseSideEffect>> getItems() {
		if (items == null)
			createItems();
		return items;
	}
	

	/**
	 * Create the side effect list to be selected by the user
	 */
	protected void createItems() {
		List<FieldValue> sideEffects = fieldsQuery.getSideEffects();
		
		TbCase tbcase = caseHome.getInstance();
		
		items = new ArrayList<ItemSelect<CaseSideEffect>>();
		
		for (FieldValue sideEffect: sideEffects) {
			ItemSelect item = new ItemSelect();

			CaseSideEffect cse = tbcase.findSideEffectData(sideEffect);
			if (cse == null) {
				cse = new CaseSideEffect();
				FieldValueComponent fvc = new FieldValueComponent();
				fvc.setValue(sideEffect);
				cse.setSideEffect(fvc);
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
		TbCase tbcase = caseHome.getInstance();
		
		List<CaseSideEffect> lst = ItemSelectHelper.getSelectedItems(getItems(), true);
		for (CaseSideEffect it: lst) {
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
		
		for (CaseSideEffect it: tbcase.getSideEffects()) {
			if (!lst.contains(it)) {
				caseHome.getEntityManager().remove(it);
			}
		}
		
		tbcase.setSideEffects(lst);
		
		String s = caseHome.persist();
		if ("persisted".equals(s))
			TagsCasesHome.instance().updateTags(tbcase);
		
		return s;
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
