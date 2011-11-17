package org.msh.tb.cases;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.msh.tb.SubstancesQuery;
import org.msh.tb.entities.Patient;
import org.msh.tb.entities.PrevTBTreatment;
import org.msh.tb.entities.Substance;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.PrevTBTreatmentOutcome;
import org.msh.utils.ItemSelect;
import org.msh.utils.date.DateUtils;


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
	private List<Item> items;
	private List<PrevTBTreatment> prevTBTreatments;
	private List<SubstanceTreatment> substancesPrevTreatments;
	private List<Item> editingItems;
	private List<Item> viewItems;

	public List<Item> getItems() {
		if (viewItems == null) {
			editing = false;
			viewItems = createItems();
		}
		return viewItems;
	}

	
	public List<Item> getEditingItems() {
		if (editingItems == null) {
			editing = true;
			editingItems = createItems();
		}
		return editingItems;		
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
	 * Return list of previous TB Treatments of cases registered in the database
	 * @return
	 */
	protected List<SubstanceTreatment> getSubstancesPrevTreatments() {
		if (substancesPrevTreatments == null) {
			createSubstancesPrevTreatments();
		}
		return substancesPrevTreatments;
	}


	/**
	 * Create a list of previous TB Treatments of cases registered in the database 
	 */
	private void createSubstancesPrevTreatments() {
		substancesPrevTreatments = new ArrayList<SubstanceTreatment>();

		TbCase tbcase = caseHome.getInstance();
		Patient p = tbcase.getPatient();
		if ((p == null) || (p.getId() == null))
			return;

		String hql = "select c.treatmentPeriod.iniDate, comp.substance.id, c.state " +
			"from PrescribedMedicine pm inner join pm.tbcase c inner join pm.medicine m inner join m.components comp " +
			"where c.patient.id = :id ";
		
		Date dt = tbcase.getTreatmentPeriod().getIniDate(); 
		if (dt != null)
			hql += " and c.treatmentPeriod.iniDate < :dt";
		
		Query qry = entityManager.createQuery(hql)
			.setParameter("id", p.getId());

		if (dt != null)
			qry.setParameter("dt", dt);
		
		List<Object[]> lst = qry.getResultList();
		
		SubstancesQuery subs = (SubstancesQuery)Component.getInstance("substances", true);
		List<Substance> subList = subs.getResultList();
		
		substancesPrevTreatments = new ArrayList<SubstanceTreatment>();
		for (Object[] vals: lst) {
			dt = (Date)vals[0];
			Integer subId = (Integer)vals[1];
			SubstanceTreatment subt = new SubstanceTreatment();
			subt.setMonth(DateUtils.monthOf(dt));
			subt.setYear(DateUtils.yearOf(dt));
			subt.setCaseState((CaseState)vals[2]);
			
			for (Substance sub: subList) {
				if (sub.getId().equals(subId)) {
					subt.setSubstance(sub);
					break;
				}
			}
			
			if (subt.getSubstance() != null)
				substancesPrevTreatments.add(subt);
			else System.out.println("** ERROR: Substance of id " + subId + " was not found.");
		}
	}


	/**
	 * Find an item by its month and year. If the item doesn't exist, create it and fill with the year and month searched 
	 * @param month
	 * @param year
	 * @return
	 */
	protected Item itemByMonthYear(int month, int year) {
		for (Item item: items) {
			PrevTBTreatment prev = item.getPrevTBTreatment();
			if (((Integer)month).equals(prev.getMonth()) && ( ((Integer)year).equals(prev.getYear()) )) {
				return item; 
			}
		}

		PrevTBTreatment prev = new PrevTBTreatment();
		prev.setMonth(month);
		prev.setYear(year);
		return addItem(prev);
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


	/**
	 * Create the items to be edited/displayed
	 */
	public List<Item> createItems() {
		items = new ArrayList<PrevTBTreatmentHome.Item>();
		createListSubstances();

		if (editing)
			 createItemsForEditing();
		else createItemsForViewing();
		
		return items;
	}


	/**
	 * Create list of previous TB Treatment items for viewing. In this situation, just the 
	 * substances that are in the previous TB treatments are displayed, and not all substances available
	 * @param prevs
	 */
	protected void createItemsForViewing() {
		for (PrevTBTreatment prev: getPrevTBTreatments()) {
			addItem(prev);
		}	
	}


	/**
	 * Create list of previous TB Treatment items for editing
	 * @param prevs
	 */
	protected void createItemsForEditing() {		
		for (PrevTBTreatment prev: getPrevTBTreatments())
			addItem(prev);

		// select previous TB treatments from previous cases
		for (SubstanceTreatment st: getSubstancesPrevTreatments()) {
			Item item = itemByMonthYear(st.getMonth(), st.getYear());
			
			if (item.getPrevTBTreatment().getOutcome() == null) {
				item.getPrevTBTreatment().setOutcome( PrevTBTreatmentOutcome.convertFromCaseState(st.getCaseState()) );
			}
			
			for (ItemSelect<Substance> is: item.getItems()) {
				if (is.getItem().equals(st.getSubstance())) {
					is.setSelected(true);
				}
			}
		}

		// check if the number of items is under the number of previous treatment
		if (items.size() < numItems) {
			for (int i = items.size(); i < numItems; i++) {
				PrevTBTreatment prev = new PrevTBTreatment();
				prev.setTbcase(caseHome.getInstance());
				addItem(prev);
			}
		}
		else numItems = items.size();
	}

	
	protected Item addItem(PrevTBTreatment prev) {
		Item item = new Item();
		item.setPrevTBTreatment(prev);
		for (Substance sub: getSubstances()) {
			ItemSelect is = new ItemSelect();
			is.setItem(sub);
			if (prev.getSubstances().contains(sub))
				is.setSelected(true);
			item.getItems().add(is);
		}
		items.add(item);
		item.setIndex(items.size());
		
		return item;
	}

	
	/**
	 * Create the list of substances to be displayed
	 */
	protected void createListSubstances() {
		substances = new ArrayList<Substance>();
		
		if (editing) {
			SubstancesQuery subs = (SubstancesQuery)Component.getInstance("substances", true);
			List<Substance> subList = subs.getPrevTBsubstances();
			for (Substance sub: subList)
				substances.add(sub);
			
			// include substances in treatments of previous cases
			for (SubstanceTreatment subt: getSubstancesPrevTreatments()) {
				if (!substances.contains(subt.getSubstance()))
					substances.add(subt.getSubstance());
			}
			
			// include substances in previous TB treatment registered in this case
			for (PrevTBTreatment prev: getPrevTBTreatments()) {
				for (Substance sub: prev.getSubstances())
					if (!substances.contains(sub))
						substances.add(sub);
			}
		}
		else {
			// it's for viewing, so just uses substances in the previous TB cases
			for (PrevTBTreatment prev: getPrevTBTreatments()) {
				for (Substance sub: prev.getSubstances())
					if (!substances.contains(sub))
						substances.add(sub);
			}
		}
		
		// sort substances
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
	}


	/**
	 * Return list of previous TB treatments
	 * @return
	 */
	protected List<PrevTBTreatment> getPrevTBTreatments() {
		if (prevTBTreatments == null)
			createPrevTBTreatments();
		return prevTBTreatments;
	}


	/**
	 * Create list of previous TB treatments
	 */
	protected void createPrevTBTreatments() {
		if (caseHome.getId() != null)
			prevTBTreatments = entityManager
				.createQuery("from PrevTBTreatment t where t.tbcase.id = " + caseHome.getId().toString() +
						" order by t.year, t.month")
				.getResultList();
		else prevTBTreatments = new ArrayList<PrevTBTreatment>();
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
			editingItems = createItems();
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

	public List<SelectItem> getYears() {
		List<SelectItem> lst = new ArrayList<SelectItem>();
		Calendar c = Calendar.getInstance();
		int ano = c.get(Calendar.YEAR);
		
		SelectItem si = new SelectItem();
		si.setLabel("-");
		lst.add(si);
		
		for (int i = ano + 1; i >= 1970; i--) {
			SelectItem it = new SelectItem();
			it.setLabel(Integer.toString(i));
			it.setValue(i);
			lst.add(it);
		}
		
		return lst;
	}


	/**
	 * Information about a substance used in a previous TB treatment
	 * @author Ricardo Memoria
	 *
	 */
	public class SubstanceTreatment {
		private int year;
		private int month;
		private Substance substance;
		private CaseState caseState;

		/**
		 * @return the year
		 */
		public int getYear() {
			return year;
		}
		/**
		 * @param year the year to set
		 */
		public void setYear(int year) {
			this.year = year;
		}
		/**
		 * @return the month
		 */
		public int getMonth() {
			return month;
		}
		/**
		 * @param month the month to set
		 */
		public void setMonth(int month) {
			this.month = month;
		}
		/**
		 * @return the substance
		 */
		public Substance getSubstance() {
			return substance;
		}
		/**
		 * @param substance the substance to set
		 */
		public void setSubstance(Substance substance) {
			this.substance = substance;
		}
		/**
		 * @return the caseState
		 */
		public CaseState getCaseState() {
			return caseState;
		}
		/**
		 * @param caseState the caseState to set
		 */
		public void setCaseState(CaseState caseState) {
			this.caseState = caseState;
		}
	}

	/**
	 * Row of the table being edited or displayed for a case
	 * @author Ricardo Memoria
	 *
	 */
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
	
}
