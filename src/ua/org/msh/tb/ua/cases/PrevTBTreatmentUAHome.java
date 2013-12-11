package org.msh.tb.ua.cases;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.msh.tb.SubstancesQuery;
import org.msh.tb.application.App;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.PrevTBTreatmentHome;
import org.msh.tb.entities.MedicineComponent;
import org.msh.tb.entities.PrescribedMedicine;
import org.msh.tb.entities.PrevTBTreatment;
import org.msh.tb.entities.Substance;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.PrevTBTreatmentOutcome;
import org.msh.tb.ua.cases.PrevTBTreatmentUAHome.Item;
import org.msh.tb.ua.entities.CaseDataUA;
import org.msh.tb.ua.entities.PrevTBTreatmentUA;
import org.msh.utils.ItemSelect;
import org.msh.utils.date.DateUtils;
import org.msh.utils.date.Period;


@Name("prevTBTreatmentUAHome")
@Scope(ScopeType.CONVERSATION)
public class PrevTBTreatmentUAHome {

	@In CaseHome caseHome;
	@In EntityManager entityManager;

	// number of previous TB Treatments
	private List<Item> treatments;
	private TbCase previousCase;
	private List<Substance> substances;
	private List<SelectItem> numTreatmentsOptions;
	private List<PrevTBTreatmentUA> removedTreatments;


	/**
	 * Save the changes made to the previous TB treatment of the case specified in {@link CaseHome} component
	 * @return
	 */
	@Transactional
	public String persist() {
		if (treatments == null) 
			return "error";

		TbCase tbcase = caseHome.getInstance();
		for (Item item: treatments) {
			updateSubstances(item);
			PrevTBTreatmentUA prev = item.getPrevTBTreatment();
			prev.setTbcase(tbcase);
			entityManager.persist(prev);
		}

		// remove previous treatments
		if (removedTreatments != null)
			for (PrevTBTreatmentUA aux: removedTreatments)
				entityManager.remove(aux);

		entityManager.flush();
		
		treatments = null;
		substances = null;
		PrevTBTreatmentHome prev = (PrevTBTreatmentHome) App.getComponent(PrevTBTreatmentHome.class);
		prev.setEditing(false);
		
		return "persisted";
	}


	/** 
	 * Remove or include substances in the previous TB treatment according to the user selection
	 * @param item
	 */
	protected void updateSubstances(Item item) {
		PrevTBTreatmentUA prev = item.getPrevTBTreatment();
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
		List<PrevTBTreatmentUA> lst;

		// is existing case?
		if (caseHome.isManaged() && caseHome.getId() != null) {  // AK caseHome.getId() == null perfectly possible 26/05/2012
			lst = entityManager
				.createQuery("from PrevTBTreatment t where t.tbcase.id = " + caseHome.getId().toString() + " order by t.year, t.month")
				.getResultList();
		}
		else {
			lst = new ArrayList<PrevTBTreatmentUA>();
			// it's a new case
			List<PrevTBTreatmentUA> prevttlist = loadPreviousTreatPrevCase();
			
			// include previous TB treatments registered in the previous case
			if (prevttlist != null) {
				for (PrevTBTreatmentUA prevtt: prevttlist) {
					PrevTBTreatmentUA p = new PrevTBTreatmentUA();
					p.setMonth(prevtt.getMonth());
					p.setOutcome(prevtt.getOutcome());
					p.setTbcase(prevtt.getTbcase());
					p.setYear(prevtt.getYear());
					p.setPatientType(prevtt.getPatientType());
					p.setRefuse2line(prevtt.isRefuse2line());
					p.setRegistrationCode(prevtt.getRegistrationCode());
					p.setRegistrationDate(prevtt.getRegistrationDate());
					p.setPrevTreatCase(prevtt.getPrevTreatCase());
					List<Substance> subs = new ArrayList<Substance>();
					for (Substance sub: prevtt.getSubstances())
						subs.add(sub);
					p.setSubstances(subs);
					lst.add(p);
				}
			}

			// include previous case
			TbCase prevCase = getPreviousCase();
			if (prevCase != null) {
				Period period = prevCase.getTreatmentPeriod();
				if ((period != null) && (period.getIniDate() != null)) {
					PrevTBTreatmentUA p = new PrevTBTreatmentUA();
					copyPrevCase(prevCase, p);
					lst.add(p);
				}
			}
		}

		createSubstanceList(lst);
		
		treatments = new ArrayList<Item>();
		
		for (PrevTBTreatmentUA prevtto: lst) {
			addItem(prevtto);
		}
	}


	/**
	 * Copy necessary fields from case to case previous treatment
	 * @param prevCase
	 * @param newPrevCase
	 */
	private void copyPrevCase(TbCase prevCase, PrevTBTreatmentUA newPrevCase) {
		PrevTBTreatmentOutcome ttoOutcome = convertFromCaseState(prevCase.getState());
		Period period = prevCase.getTreatmentPeriod();
		newPrevCase.setOutcome(ttoOutcome);
		if (period!=null){
			newPrevCase.setMonth(DateUtils.monthOf(period.getIniDate()));
			newPrevCase.setYear(DateUtils.yearOf(period.getIniDate()));
		}
		newPrevCase.setPatientType(prevCase.getPatientType());
		CaseDataUA data = (CaseDataUA)App.getEntityManager().find(CaseDataUA.class, prevCase.getId());
		newPrevCase.setRefuse2line(data.isRefuse2line());
		newPrevCase.setRegistrationCode(prevCase.getRegistrationCode());
		newPrevCase.setRegistrationDate(prevCase.getRegistrationDate());
		newPrevCase.setPrevTreatCase(prevCase);
		List<Substance> subs = new ArrayList<Substance>();

		for (PrescribedMedicine pm: prevCase.getPrescribedMedicines())
			for (MedicineComponent mc: pm.getMedicine().getComponents()) {
				if (!subs.contains(mc.getSubstance()))
					subs.add(mc.getSubstance());
			}
		newPrevCase.setSubstances(subs);
	}
	
	
	/**
	 * Create the list of substance based on previous treatments. If the treatments are in editing mode, all substances will be loaded
	 * @param lst
	 */
	private void createSubstanceList(List<PrevTBTreatmentUA> lst) {
		if ((isEditing()) || (!caseHome.isManaged()))
			substances = ((SubstancesQuery)Component.getInstance("substances", true)).getPrevTBsubstances();
		else {
			substances = new ArrayList<Substance>();
			for (PrevTBTreatmentUA prevtto: lst) {
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
	protected Item addItem(PrevTBTreatmentUA prev) {
		Item item = new Item();
		item.setPrevTBTreatment(prev);
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
	protected List<PrevTBTreatmentUA> loadPreviousTreatPrevCase() {
		String hql = "from PrevTBTreatmentUA p where p.tbcase.id in " +
					"(select c.id from TbCase c where c.patient.id = :id and c.treatmentPeriod.iniDate = " +
					"(select max(c2.treatmentPeriod.iniDate) from TbCase c2 where c2.patient.id = c.patient.id))";

		List<PrevTBTreatmentUA> previousTreatsPrevCase = entityManager
			.createQuery(hql)
			.setParameter("id", caseHome.getInstance().getPatient().getId())
			.getResultList();
		return previousTreatsPrevCase;
	}
	
	/**
	 * Convert from case outcome to previous TB case outcome
	 * @param state
	 * @return
	 */
	public PrevTBTreatmentOutcome convertFromCaseState(CaseState state) {
		switch (state) {
		case CURED: 
			return PrevTBTreatmentOutcome.CURED;
		case DEFAULTED: case TREATMENT_INTERRUPTION: 
			return PrevTBTreatmentOutcome.DEFAULTED;
		case DIAGNOSTIC_CHANGED:
			return PrevTBTreatmentOutcome.DIAGNOSTIC_CHANGED;
		case FAILED:
			return PrevTBTreatmentOutcome.FAILURE;
		case MDR_CASE:
			return PrevTBTreatmentOutcome.SHIFT_CATIV;
		case NOT_CONFIRMED:
			return PrevTBTreatmentOutcome.UNKNOWN;
		case TREATMENT_COMPLETED:
			return PrevTBTreatmentOutcome.COMPLETED;
		case TRANSFERRED_OUT:
			return PrevTBTreatmentOutcome.TRANSFERRED_OUT;
		default:
			return PrevTBTreatmentOutcome.UNKNOWN;
		}
	}

	/**
	 * Return the previous TB case of the patient
	 * @return
	 */
	protected TbCase getPreviousCase() {
		if (previousCase == null) {			
			try {
				String hql = "from TbCase c where c.patient.id = :id and c.treatmentPeriod.iniDate = " +
					"(select max(c2.treatmentPeriod.iniDate) from TbCase c2 where c2.patient.id = c.patient.id)";

				Query q = entityManager
					.createQuery(hql)
					.setParameter("id", caseHome.getInstance().getPatient().getId());
				if (q.getResultList().size()!=0)
					previousCase = (TbCase)q.getResultList().get(0);
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
		if (substances == null)
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
			addItem(new PrevTBTreatmentUA());
		while (getTreatments().size() > numTreatments) {
			PrevTBTreatmentUA aux = treatments.get(treatments.size()-1).getPrevTBTreatment();
			if (entityManager.contains(aux)) {
				if (removedTreatments == null)
					removedTreatments = new ArrayList<PrevTBTreatmentUA>();
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
		private PrevTBTreatmentUA prevTBTreatment;
		private List<ItemSelect> items = new ArrayList<ItemSelect>();

		/**
		 * @return the prevTBTreatment
		 */
		public PrevTBTreatmentUA getPrevTBTreatment() {
			return prevTBTreatment;
		}
		/**
		 * @param prevTBTreatment the prevTBTreatment to set
		 */
		public void setPrevTBTreatment(PrevTBTreatmentUA prevTBTreatment) {
			this.prevTBTreatment = prevTBTreatment;
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
		PrevTBTreatmentHome prev = (PrevTBTreatmentHome) App.getComponent(PrevTBTreatmentHome.class);
		return prev.isEditing();
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
	
	/**
	 * Associated cases previous treatments with cases exist in database and update fields 
	 */
	public void refresh() {
		Iterator<Item> it = getTreatments().iterator();
		while (it.hasNext()){
			PrevTBTreatmentUA pt = it.next().getPrevTBTreatment();
			if (pt.getPrevTreatCase()!=null){
				TbCase pr = App.getEntityManager().find(TbCase.class, pt.getPrevTreatCase().getId());
				copyPrevCase(pr, pt);
				App.getEntityManager().persist(pt);
			}
			else{
				String hql = "from TbCase c " +
						"where month(c.treatmentPeriod.iniDate) = "+(pt.getMonth()+1) +
						  " and year(c.treatmentPeriod.iniDate) = "+pt.getYear() +
						  " and c.patient.id = "+caseHome.getInstance().getPatient().getId();

				List<TbCase> lst = App.getEntityManager().createQuery(hql)
					.getResultList();
				for (TbCase c:lst){
					if (pt.getOutcome().equals(convertFromCaseState(c.getState()))){
						copyPrevCase(c, pt);
						App.getEntityManager().persist(pt);
					}
				}
				
			}
		}
		App.getEntityManager().flush();
		/*removedTreatments = new ArrayList<PrevTBTreatmentUA>();
		
		Iterator<Item> it = getTreatments().iterator();
		while (it.hasNext()){
			PrevTBTreatmentUA pt = it.next().getPrevTBTreatment();
			if (pt.getPrevTreatCase()!=null)
				removedTreatments.add(pt);
		}
		
		List<PrevTBTreatmentUA> lst = generatePrevTreats();
		for (PrevTBTreatmentUA pb:lst){
			for (Item item:getTreatments()){
				PrevTBTreatmentUA tbold = item.getPrevTBTreatment();
				if (tbold.getPrevTreatCase()==null && tbold.getMonth().equals(pb.getMonth()) && (tbold.getYear()==pb.getYear()))
					removedTreatments.add(tbold);
			}
		}
		for (PrevTBTreatmentUA prevtto: lst) {
			addItem(prevtto);
		}
		persist();*/
	}
}
