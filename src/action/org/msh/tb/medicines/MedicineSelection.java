package org.msh.tb.medicines;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.framework.EntityQuery;
import org.msh.mdrtb.entities.Medicine;
import org.msh.mdrtb.entities.enums.MedicineCategory;
import org.msh.utils.ItemSelect;
import org.msh.utils.ItemSelectHelper;


/**
 * @author Ricardo
 *
 * Creates a list of medicines to be selected by the user in a list of check boxes.
 * The class also supports medicine filtering 
 *
 */
@Name("medicineSelection")
@Scope(ScopeType.CONVERSATION)
public class MedicineSelection {

	@In(create=true) EntityQuery<Medicine> medicines;
	
	public class MedicinesCategory {
		private MedicineCategory category;
		private List<ItemSelect> medicines = new ArrayList<ItemSelect>();

		public MedicineCategory getCategory() {
			return category;
		}
		public void setCategory(MedicineCategory category) {
			this.category = category;
		}
		public List<ItemSelect> getMedicines() {
			return medicines;
		}
		public void setMedicines(List<ItemSelect> medicines) {
			this.medicines = medicines;
		}
	}
	
	private List<ItemSelect<Medicine>> items;
	private List<MedicinesCategory> categories;
	private List<Medicine> filterList;
	
	/**
	 * Apply filter of medicines, removing the same medicines from the items list
	 * @param aFilterList
	 */
	public void applyFilter(List<Medicine> aFilterList) {
		filterList = aFilterList;
		items = null;
		categories = null;
	}
	
	/**
	 * Returns the list of selected medicines
	 * @return
	 */
	public List<Medicine> getSelectedMedicines() {
		List<Medicine> medicinesSel = new ArrayList<Medicine>();
		
		if (items == null)
			return medicinesSel;

		for (ItemSelect it: items) {
			if (it.isSelected())
				medicinesSel.add((Medicine)it.getItem());
		}
		return medicinesSel;
	}
	
	/**
	 * Creates the list of medicines for selection
	 */
	protected void createItens() {
		items = ItemSelectHelper.createList(medicines.getResultList());
		// filter was defined ?
		if (filterList != null) {
			int i = 0;
			// remove medicines which are in the filter
			while (i < items.size()) {
				Medicine m = (Medicine)items.get(i).getItem();
				if (filterList.contains(m))
					items.remove(i);
				else i++;
			}
		}
	}
	
	protected void createCategories() {
		categories = new ArrayList<MedicinesCategory>();
		for (ItemSelect item: getItems()) {
			Medicine m = (Medicine)item.getItem();
			findCategory(m.getCategory()).getMedicines().add(item);
		}
	}
	
	protected MedicinesCategory findCategory(MedicineCategory cat) {
		for (MedicinesCategory mc: categories) {
			if (mc.getCategory().equals(cat))
				return mc;
		}
		MedicinesCategory mc = new MedicinesCategory();
		mc.setCategory(cat);
		categories.add(mc);
		return mc;
	}
	
	public List<MedicinesCategory> getCategories() {
		if (categories == null)
			createCategories();
		return categories;
	}
	
	public List<ItemSelect<Medicine>> getItems() {
		if (items == null)
			createItens();
		return items;
	}
	
	public void setItens(List<ItemSelect<Medicine>> value) {
		items = value;
	}
}
