package org.msh.tb.ng;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.msh.tb.SubstancesQuery;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.MedicineComponent;
import org.msh.tb.entities.PrescribedMedicine;
import org.msh.tb.entities.Substance;
import org.msh.tb.entities.enums.PrevTBTreatmentOutcome;
import org.msh.tb.ng.entities.PrevTBTreatmentNG;
import org.msh.tb.ng.entities.TbCaseNG;
import org.msh.utils.ItemSelect;
import org.msh.utils.date.DateUtils;
import org.msh.utils.date.Period;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;



@Name("prevTBTreatmentNGHome")
@Scope(ScopeType.CONVERSATION)
public class PrevTBTreatmentNGHome {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5809537463336717963L;
	
	@In CaseHome caseHome;
	@In EntityManager entityManager;

	// number of previous TB Treatments
	private List<Item> treatments;
	private TbCaseNG previousCase;
	private boolean editing;
	private List<Substance> substances;
	private List<SelectItem> numTreatmentsOptions;
	private List<PrevTBTreatmentNG> removedTreatments;


	/**
	 * Save the changes made to the previous TB treatment of the case specified in {@link CaseHome} component
	 * @return
	 */
	@Transactional
	public String persist() {
		if (treatments == null) 
			return "error";

		TbCaseNG tbcase = (TbCaseNG) caseHome.getInstance();
		for (Item item: treatments) {
			updateSubstances(item);
			PrevTBTreatmentNG prev = item.getPrevTBTreatmentNG();
			prev.setTbcase(tbcase);
			entityManager.persist(prev);
		}

		// remove previous treatments
		if (removedTreatments != null)
			for (PrevTBTreatmentNG aux: removedTreatments)
				entityManager.remove(aux);

		entityManager.flush();
		
		treatments = null;
		substances = null;
		editing = false;
		
		return "persisted";
	}


	/** 
	 * Remove or include substances in the previous TB treatment according to the user selection
	 * @param item
	 */
	protected void updateSubstances(Item item) {
		PrevTBTreatmentNG prev = item.getPrevTBTreatmentNG();
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
	 * Create list of previous treatments of the patient
	 */
	protected void createTreatments() {
		List<PrevTBTreatmentNG> lst;

		// is existing case?
		if (caseHome.isManaged()) {
			lst = entityManager
				.createQuery("from PrevTBTreatment t where t.tbcase.id = " + caseHome.getId().toString() + " order by t.year, t.month")
				.getResultList();
		}
		else {
			// it's a new case
			List<PrevTBTreatmentNG> prevttlist = loadPreviousTreatPrevCase();
			lst = new ArrayList<PrevTBTreatmentNG>();
			
			// include previous TB treatments registered in the previous case
			if (prevttlist != null) {
				for (PrevTBTreatmentNG prevtt: prevttlist) {
					PrevTBTreatmentNG p = new PrevTBTreatmentNG();
					p.setMonth(prevtt.getMonth());
					p.setOutcome(prevtt.getOutcome());
					p.setTbcase(prevtt.getTbcase());
					p.setYear(prevtt.getYear());
					List<Substance> subs = new ArrayList<Substance>();
					for (Substance sub: prevtt.getSubstances())
						subs.add(sub);
					p.setSubstances(subs);
					lst.add(p);
				}
			}

			// include previous case
			TbCaseNG prevCase = getPreviousCase();
			if (prevCase != null) {
				Period period = prevCase.getTreatmentPeriod();
				if ((period != null) && (period.getIniDate() != null)) {
					PrevTBTreatmentOutcome ttoOutcome = PrevTBTreatmentOutcome.convertFromCaseState(prevCase.getState());
					PrevTBTreatmentNG p = new PrevTBTreatmentNG();
					p.setOutcome(ttoOutcome);
					p.setMonth(DateUtils.monthOf(period.getIniDate()) + 1);
					p.setYear(DateUtils.yearOf(period.getIniDate()));
					List<Substance> subs = new ArrayList<Substance>();

					for (PrescribedMedicine pm: prevCase.getPrescribedMedicines())
						for (MedicineComponent mc: pm.getMedicine().getComponents()) {
							if (!subs.contains(mc.getSubstance()))
								subs.add(mc.getSubstance());
						}
					p.setSubstances(subs);
					lst.add(p);
				}
			}
		}

		createSubstanceList(lst);
		
		treatments = new ArrayList<PrevTBTreatmentNGHome.Item>();
		
		for (PrevTBTreatmentNG prevtto: lst) {
			addItem(prevtto);
		}
	}
	
	
	/**
	 * Create the list of substance based on previous treatments. If the treatments are in editing mode, all substances will be loaded
	 * @param lst
	 */
	private void createSubstanceList(List<PrevTBTreatmentNG> lst) {
		if (editing)
			substances = ((SubstancesQuery)Component.getInstance("substances", true)).getPrevTBsubstances();
		else {
			substances = new ArrayList<Substance>();
			for (PrevTBTreatmentNG prevtto: lst) {
				for (Substance sub: prevtto.getSubstances())
					if (!substances.contains(sub))
						substances.add(sub);
			}
			
			Collections.sort(substances, new Comparator<Substance>() {

				@Override
				public int compare(Substance sub1, Substance sub2) {
					return sub1.compare(sub2);
				}
			});
		}
	}
	
	
	/**
	 * Add an item to the previous tb treatment list 
	 * @param prev
	 * @return
	 */
	protected Item addItem(PrevTBTreatmentNG prev) {
		Item item = new Item();
		item.setPrevTBTreatmentNG(prev);
		for (Substance sub: getSubstances()) {
			ItemSelect is = new ItemSelect();
			is.setItem(sub);
			if (prev.getSubstances().contains(sub))
				is.setSelected(true);
			item.getItems().add(is);
		}
		treatments.add(item);
		item.setIndex(treatments.size());
		
		return item;
	}
	
	
	/**
	 * Return the previous TB case of the patient
	 * @return
	 */
	protected List<PrevTBTreatmentNG> loadPreviousTreatPrevCase() {
		String hql = "from PrevTBTreatment p where p.tbcase.id in " +
					"(select c.id from TbCase c where c.patient.id = :id and c.treatmentPeriod.iniDate = " +
					"(select max(c2.treatmentPeriod.iniDate) from TbCase c2 where c2.patient.id = c.patient.id))";

		List<PrevTBTreatmentNG> previousTreatsPrevCase = entityManager
			.createQuery(hql)
			.setParameter("id", caseHome.getInstance().getPatient().getId())
			.getResultList();
		return previousTreatsPrevCase;
	}
	

	/**
	 * Return the previous TB case of the patient
	 * @return
	 */
	protected TbCaseNG getPreviousCase() {
		if (previousCase == null) {			
			try {
				String hql = "from TbCase c where c.patient.id = :id and c.treatmentPeriod.iniDate = " +
					"(select max(c2.treatmentPeriod.iniDate) from TbCase c2 where c2.patient.id = c.patient.id)";

				previousCase = (TbCaseNG)entityManager
					.createQuery(hql)
					.setParameter("id", caseHome.getInstance().getPatient().getId())
					.getSingleResult();
			} catch (NoResultException e) {
				return null;
			}
		}
		
		return previousCase;
	}
	

	/**
	 * Return list of substances in use in the previous treatments
	 * @return
	 */
	public List<Substance> getSubstances() {
		if (substances == null||substances.size()==0)
			createTreatments();
		return substances;
	}
	
	/**
	 * @return the treatments
	 */
	public List<Item> getTreatments() {
		if (treatments == null)
			createTreatments();
		return treatments;
	}

	/**
	 * @return the numTreatments
	 */
	public int getNumTreatments() {
		return getTreatments().size();
	}


	/**
	 * @param numTreatments the numTreatments to set
	 */
	public void setNumTreatments(int numTreatments) {
		while (getTreatments().size() < numTreatments)
			addItem(new PrevTBTreatmentNG());
		while (getTreatments().size() > numTreatments) {
			PrevTBTreatmentNG aux = treatments.get(treatments.size()-1).getPrevTBTreatmentNG();
			if (entityManager.contains(aux)) {
				if (removedTreatments == null)
					removedTreatments = new ArrayList<PrevTBTreatmentNG>();
				removedTreatments.add(aux);
			}
			treatments.remove(treatments.size() - 1);
		}
	}


	/**
	 * Represents a treatment to be edited or displayed
	 * @author Ricardo Memoria
	 *
	 */
	public class Item {
		private int index;
		private PrevTBTreatmentNG prevTBTreatmentNG;
		private List<ItemSelect> items = new ArrayList<ItemSelect>();
		
		public PrevTBTreatmentNG getPrevTBTreatmentNG() {
			return prevTBTreatmentNG;
		}
		
		public void setPrevTBTreatmentNG(PrevTBTreatmentNG prevTBTreatmentNG) {
			this.prevTBTreatmentNG = prevTBTreatmentNG;
		}

		/**
		 * @return the items
		 */
		public List<ItemSelect> getItems() {
			return items;
		}
		/**
		 * @param items the items to set
		 */
		public void setItems(List<ItemSelect> items) {
			this.items = items;
		}
		/**
		 * @return the index
		 */
		public int getIndex() {
			return index;
		}
		/**
		 * @param index the index to set
		 */
		public void setIndex(int index) {
			this.index = index;
		}
		
	}


	/**
	 * @return the editing
	 */
	public boolean isEditing() {
		return editing;
	}


	/**
	 * @param editing the editing to set
	 */
	public void setEditing(boolean editing) {
		treatments = null;
		substances = null;
		this.editing = editing;
	}


	/**
	 * Return options for the number of previous treatments drop down control
	 * @return
	 */
	public List<SelectItem> getNumTreatmentsOptions() {
		if (numTreatmentsOptions == null) {
			numTreatmentsOptions = new ArrayList<SelectItem>();
			for (int i = 0; i<=10; i++) {
				SelectItem item = new SelectItem();
				item.setLabel(Integer.toString(i));
				item.setValue((Integer)i);
				numTreatmentsOptions.add(item);
			}
		}
		return numTreatmentsOptions;
	}
}


	