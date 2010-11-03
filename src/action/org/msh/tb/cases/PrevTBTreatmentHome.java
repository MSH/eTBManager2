package org.msh.tb.cases;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.msh.mdrtb.entities.PrevTBTreatment;
import org.msh.mdrtb.entities.Substance;
import org.msh.mdrtb.entities.TbCase;
import org.msh.tb.SubstancesQuery;
import org.msh.utils.ItemSelect;


@Name("prevTBTreatmentHome")
@Scope(ScopeType.CONVERSATION)
public class PrevTBTreatmentHome {
	private static final long serialVersionUID = -515608455149323704L;

	@In(required=true) @Out CaseHome caseHome;
	@In(create=true) EntityManager entityManager;

	private int numItems;
	private List<SelectItem> numTreatments;
	private boolean editing;
	private List<Substance> substances;
	
	public class Item {
		private int Index;
		private PrevTBTreatment prevTBTreatment;
		private List<ItemSelect> items = new ArrayList<ItemSelect>();

		public PrevTBTreatment getPrevTBTreatment() {
			return prevTBTreatment;
		}
		public void setPrevTBTreatment(PrevTBTreatment prevTBTreatment) {
			this.prevTBTreatment = prevTBTreatment;
		}
		public List<ItemSelect> getItems() {
			return items;
		}
		public int getIndex() {
			return Index;
		}
		public void setIndex(int index) {
			Index = index;
		}
	}
	
	private List<Item> items;
	
	public List<Item> getItems() {
		if (items == null) {
			editing = false;
			createItems();
		}
		return items;
	}

	
	public List<Item> getEditingItems() {
		if (items == null) {
			editing = true;
			createItems();
		}
		return items;		
	}
	
	/**
	 * Saves the previous TB treatments of the case
	 * @return
	 */
	@Transactional
	public String persist() {
		TbCase tbcase = caseHome.getInstance();
		for (Item item: getItems()) {
			updateSubstances(item);
			PrevTBTreatment prev = item.getPrevTBTreatment();
			prev.setTbcase(tbcase);
			entityManager.persist(prev);
		}

		entityManager.flush();
		substances.clear();
		items.clear();
		
		editing = false;
		createItems();
		
		return "persisted";
	}

	
	/** 
	 * Remove or include substances in the prev. TB treatment according to the user selection
	 * @param item
	 */
	protected void updateSubstances(Item item) {
		PrevTBTreatment prev = item.getPrevTBTreatment();
		for (ItemSelect it: item.getItems()) {
			Substance sub = (Substance)it.getItem();
			if (it.isSelected()) {
				if (!prev.getSubstances().contains(sub))
					prev.getSubstances().add(sub);
			}
			else {
				prev.getSubstances().remove(sub);
			}
		}
	}


	public void createItems() {
		List<PrevTBTreatment> prevs;

		if (caseHome.getId() != null)
			prevs = entityManager
				.createQuery("from PrevTBTreatment t where t.tbcase.id = " + caseHome.getId().toString() +
						" order by t.year, t.month")
				.getResultList();
		else prevs = new ArrayList<PrevTBTreatment>();
		
		items = new ArrayList<Item>();
		
		substances = new ArrayList<Substance>();

		if (editing)
			 createItemsForEditing(prevs);
		else createItemsForViewing(prevs);
	}


	/**
	 * Create list of previous TB Treatment items for viewing
	 * @param prevs
	 */
	protected void createItemsForViewing(List<PrevTBTreatment> prevs) {
		int index = 0;
		
		for (PrevTBTreatment prev: prevs) {
			for (Substance sub: prev.getSubstances())
				if (!substances.contains(sub))
					substances.add(sub);
		}
		
		Collections.sort(substances, new Comparator<Substance>() {

			public int compare(Substance o1, Substance o2) {
				Integer val1 = o1.getPrevTreatmentOrder();
				Integer val2 = o2.getPrevTreatmentOrder();
				if ((val1 == null) && (val2 == null))
					return 0;
				if (val1 == null)
					return -1;
				if (val2 == null)
					return 1;
				return (val1 > val2? 1: (val1 == val2? 0: -1));
			}
			
		});
		
		for (PrevTBTreatment prev: prevs) {
			Item item = new Item();
			index++;
			item.setIndex(index);
			
			item.setPrevTBTreatment(prev);
			for (Substance sub: substances) {
				ItemSelect is = addSubstance(item, sub);
				is.setSelected(prev.getSubstances().contains(sub));
			}
			items.add(item);
		}		
	}


	/**
	 * Create list of previous TB Treatment items for editing
	 * @param prevs
	 */
	protected void createItemsForEditing(List<PrevTBTreatment> prevs) {		
		int index = 0;
		SubstancesQuery substancesList = (SubstancesQuery)Component.getInstance("substances", true);
		
		for (PrevTBTreatment prev: prevs) {
			Item item = new Item();
			index++;
			item.setIndex(index);
			
			item.setPrevTBTreatment(prev);
			for (Substance sub: substancesList.getPrevTBsubstances()) {
				ItemSelect is = addSubstance(item, sub);
				is.setSelected(prev.getSubstances().contains(sub));
			}
			items.add(item);
		}

		// check if the number of items is under the number of previous treatment
		if (items.size() < numItems) {
			for (int i = items.size(); i < numItems; i++) {
				Item item = new Item();
				item.setPrevTBTreatment(new PrevTBTreatment());
				
				index++;
				item.setIndex(index);
				for (Substance sub: substancesList.getPrevTBsubstances()) {
					addSubstance(item, sub);
				}
				items.add(item);
			}
		}		
	}


	/**
	 * Add a substance to a row
	 * @param item
	 * @param sub
	 * @return
	 */
	protected ItemSelect addSubstance(Item item, Substance sub) {
		ItemSelect is = new ItemSelect();
		is.setItem(sub);
		item.getItems().add(is);
		if (!substances.contains(sub))
			substances.add(sub);
		return is;
	}


	public int getNumItems() {
		return numItems;
	}


	public void setNumItems(int numItems) {
		if (numItems == this.numItems)
			return;
		this.numItems = numItems;
		items = null;
		substances = null;
	}


	public List<SelectItem> getNumTreatmentsOptions() {
		if (numTreatments == null) {
			numTreatments = new ArrayList<SelectItem>();
			for (int i = 0; i<=10; i++) {
				SelectItem item = new SelectItem();
				item.setLabel(Integer.toString(i));
				item.setValue((Integer)i);
				numTreatments.add(item);
			}
		}
		return numTreatments; 
	}


	public List<Substance> getEditingSubstances() {
		if (substances == null) {
			editing = true;
			createItems();
		}
		return substances;
	}


	public List<Substance> getSubstances() {
		if (substances == null) {
			editing = false;
			createItems();
		}
		return substances;
	}
}
