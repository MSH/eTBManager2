package org.msh.tb;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.MedicineComponent;
import org.msh.tb.entities.Substance;
import org.msh.tb.entities.enums.MedicineCategory;
import org.msh.tb.transactionlog.LogInfo;
import org.msh.utils.EntityQuery;
import org.msh.utils.ItemSelect;
import org.msh.utils.ItemSelectHelper;



/**
 * Home class to handle medicine CRUD operations
 * @author Ricardo Memoria
 *
 */
@Name("medicineHome")
@LogInfo(roleName="MEDICINES")
public class MedicineHome extends EntityHomeEx<Medicine> {

	private static final long serialVersionUID = 8518405317117822887L;

	@In(create=true) SubstancesQuery substances;
	
	private List<ItemSelect<MedicineComponent>> components;
	
	@Factory("medicine")
	public Medicine getMedicine() {
		return getInstance();
	}


	@Override
	public String persist() {
		// save list of component substances
		List<MedicineComponent> lst = ItemSelectHelper.getSelectedItems(components, true);
		getInstance().setComponents(lst);
		
		return super.persist();
	}


	@Override
	public EntityQuery<Medicine> getEntityQuery() {
		return (MedicinesQuery)Component.getInstance("medicines", false);
	}
	
	@Factory("medicineCategories")
	public MedicineCategory[] getCategories() {
		return MedicineCategory.values();
	}


	/**
	 * Return the list of components to be displayed and selected by the user
	 * @return
	 */
	public List<ItemSelect<MedicineComponent>> getComponents() {
		if (components == null) {
			components = ItemSelectHelper.createList(getInstance().getComponents());
			for (Substance sub: substances.getResultList()) {

				// check if substance is already in the list 
				ItemSelect<MedicineComponent> item = null;
				for (ItemSelect<MedicineComponent> aux: components) {
					if (aux.getItem().getSubstance().equals(sub)) {
						item = aux;
						break;
					}
				}
				
				// include a new substance
				if (item == null) {
					MedicineComponent comp = new MedicineComponent();
					comp.setSubstance(sub);
					comp.setMedicine(getInstance());
					item = new ItemSelect<MedicineComponent>();
					item.setItem(comp);
					item.setSelected(false);
					components.add(item);
				}
				else item.setSelected(true);
			}
			
			Collections.sort(components, new Comparator<ItemSelect<MedicineComponent>>() {
				public int compare(ItemSelect<MedicineComponent> item1,
						ItemSelect<MedicineComponent> item2) {
					return item1.getItem().getSubstance().getName().getDefaultName().compareTo(item2.getItem().getSubstance().getName().getDefaultName());
				}
			});
		}
		return components;
	}
	
	public void setMedicine(Medicine med) {
		if (med == null)
			 setId(null);
		else setId(med.getId());
	}
}
