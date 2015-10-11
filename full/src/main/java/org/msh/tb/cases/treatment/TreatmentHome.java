package org.msh.tb.cases.treatment;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.SourcesQuery;
import org.msh.tb.TagsCasesHome;
import org.msh.tb.application.App;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.*;
import org.msh.tb.entities.enums.*;
import org.msh.tb.login.UserSession;
import org.msh.tb.tbunits.TBUnitSelection;
import org.msh.tb.tbunits.TBUnitType;
import org.msh.utils.date.DateUtils;
import org.msh.utils.date.Period;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Case treatment editing functions
 * 
 * @author Ricardo Memoria
 *
 */
@Name("treatmentHome")
@Scope(ScopeType.CONVERSATION)
public class TreatmentHome {
	// forms displayed by the page
	public enum FormEditing {	NONE, TREATMENT, REGIMEN, MEDICINE, REMOVE; 	}

	@In(required=true) CaseHome caseHome;
	@In(create=true) CaseRegimenHome caseRegimenHome;
	@In(create=true) PrescribedMedicineHome prescribedMedicineHome;
	@In(create=true) FacesMessages facesMessages;
	@In EntityManager entityManager;
	@In (required=true) UserSession userSession;
	
	private Date iniDate;
	private Date endDate;
	private Date iniContinuousPhase;

	private boolean preservePreviousPeriod;
	
	// used for validation. Initial date of treatment cannot be before this date
	private Date minIniDate;
	
	private List<SelectItem> months;
	private List<SelectItem> iniMonths;
	private List<SelectItem> doses;
	
	private TBUnitSelection tbunitselection;
	private PrescribedMedicine pmcopy;
	
	private boolean regimenMovedToIndivid;
	
	
	/**
	 * Return to the UI if there is any validation problem during ajax requests
	 */
	private boolean validated;

	private TreatmentHealthUnit healthUnit;
	private FormEditing formEditing;
	private boolean initialized;
	private TbCase tbcase;
	

	/**
	 * Initialize editing of the treatment
	 */
	public void initializeEditing() {
		if (initialized)
			return;

		tbcase = caseHome.getInstance();
		List<TreatmentHealthUnit> lst = tbcase.getSortedTreatmentHealthUnits();
		if (lst.size() == 0)
			return;
		
		PrescriptionTable tbl = (PrescriptionTable)Component.getInstance("prescriptionTable", true);
		tbl.setEditing(true);
		tbl.refresh();
		updateTreatmentPeriod();

		if (tbcase.getIniContinuousPhase() != null)
			prescribedMedicineHome.splitPeriod(tbcase.getIniContinuousPhase());

		healthUnit = lst.get( lst.size() - 1 );
		if (lst.indexOf(healthUnit) > 0)
			 minIniDate = healthUnit.getPeriod().getIniDate();
		else minIniDate = null;
		
		getTbunitselection().setSelected(healthUnit.getTbunit());
		initialized = true;
		regimenMovedToIndivid = false;
		
		Events.instance().raiseEvent("treatment-init-editing");
	}


	/**
	 * Save changes made to the treatment and finish editing
	 * @return
	 */
	public String saveChanges() {
		Tbunit unit = getTbunitselection().getSelected();
		
		healthUnit.setTbunit( unit );
		TbCase tbcase = caseHome.getInstance();

		tbcase.setOwnerUnit(unit);
		caseHome.getInstance().updateDaysTreatPlanned();

		refreshPrescriptionTable();

		// check if regimen moved to an individualized regimen
		if (checkregimenMovedToIndivid()) {
			tbcase.setRegimen(null);
		}
		
		Events.instance().raiseEvent("treatment-persist");
		
		String s = caseHome.persist();

		TagsCasesHome.instance().updateTags(tbcase);
		
		PrescriptionTable tbl = (PrescriptionTable)Component.getInstance("prescriptionTable");
		tbl.setEditing(false);
		
		return s;
	}
	

	/**
	 * Start editing in the treatment period
	 */
	public void startTreatmentPeriodEditing() {
		TbCase tbcase = caseHome.getInstance();

		iniDate = tbcase.getTreatmentPeriod().getIniDate();
		endDate = healthUnit.getPeriod().getEndDate();
		iniContinuousPhase = tbcase.getIniContinuousPhase();
		
		formEditing = FormEditing.TREATMENT;
	}
	

	/**
	 * Confirm editing of the treatment period
	 */
	public void endTreatmentPeriodEditing() {
		validated = false;
		
		if (!checkDateBasicRules(iniDate, endDate))
			return;
/*		if (caseHome.getInstance().getDiagnosisDate()!=null)
			if (iniDate.before(caseHome.getInstance().getDiagnosisDate())) {
				facesMessages.addToControlFromResourceBundle("edtIniContPhase", "cases.treat.inidatemsg");
				return;
			}
*/		
		if ((!iniDate.before(iniContinuousPhase)) && (!endDate.after(iniContinuousPhase))) {
			facesMessages.addToControlFromResourceBundle("edtIniContPhase", "cases.treat.contdateerror");
			return;
		}

		TbCase tbcase = caseHome.getInstance();
		
		Period p = new Period(iniDate, endDate);
		if ((p.equals(tbcase.getTreatmentPeriod())) && (iniContinuousPhase.equals(tbcase.getIniContinuousPhase()))) {
			validated = true;
			return;
		}
		
		Events.instance().raiseEvent("treatment-period-change", p, iniContinuousPhase);
		
		// case has continuous phase ?
		if (tbcase.getIniContinuousPhase() != null) {
			Period intPeriod = new Period(iniDate, DateUtils.incDays(iniContinuousPhase, -1));
			Period conPeriod = new Period(iniContinuousPhase, endDate);

			prescribedMedicineHome.splitPeriod(tbcase.getIniContinuousPhase());
			prescribedMedicineHome.adjustPhasePeriod(RegimenPhase.INTENSIVE, intPeriod);
			prescribedMedicineHome.adjustPhasePeriod(RegimenPhase.CONTINUOUS, conPeriod);
		}
		else {
			prescribedMedicineHome.adjustPhasePeriod(RegimenPhase.INTENSIVE, new Period(iniDate, endDate));
			prescribedMedicineHome.splitPeriod(iniContinuousPhase);
		}
		
		if(tbcase.getHealthUnits().size() > 1){
			healthUnit.getPeriod().setEndDate(endDate);
		}else{
			healthUnit.setPeriod(p);
		}
		
		tbcase.setTreatmentPeriod(p);
		tbcase.setIniContinuousPhase(iniContinuousPhase);

		formEditing = FormEditing.NONE;
		validated = true;
		refreshPrescriptionTable();
		caseHome.updateCaseTags();
		facesMessages.addFromResourceBundle("form.clickon.save");
	}



	/**
	 * Initialize regimen change 
	 */
	public void startRegimenChange() {
		formEditing = FormEditing.REGIMEN;
		validated = false;
		caseRegimenHome.setRegimen(null);
	}


	/**
	 * Change the treatment regimen
	 */
	public void endRegimenChange() {
		TbCase tbcase = caseHome.getInstance();
		Date dt = tbcase.getTreatmentPeriod().getIniDate();
		caseRegimenHome.setUseDefaultDoseUnit(false);
		caseRegimenHome.applyRegimen(dt, null);
		tbcase.setRegimen(caseRegimenHome.getRegimen());
		tbcase.setRegimenIni(caseRegimenHome.getRegimen());
		//caseHome.persist();
		
		formEditing = FormEditing.NONE;
		validated = true;
		
		refreshPrescriptionTable();
		
		regimenMovedToIndivid = true;
		
		Events.instance().raiseEvent("treatment-regimen-changed");
	}


	/**
	 * Start entering a new prescription period
	 */
	public void startAddMedicine() {
		PrescribedMedicine pm = new PrescribedMedicine();
		Contexts.getConversationContext().set("prescribedMedicine", pm);

		// check the initial treatment date for individualized regimen
		TbCase tbcase = caseHome.getInstance();
		Period p = tbcase.getTreatmentPeriod();
		pm.setTbcase(tbcase);
		if (p == null) {
			p = new Period();
			caseHome.getInstance().setTreatmentPeriod(p);
		}

		if (p.getIniDate() == null) 
			p.setIniDate(new Date());
		if (p.getEndDate() == null)
			p.setEndDate(new Date());
		
		pmcopy = null;

		formEditing = FormEditing.MEDICINE;
		validated = false;
	}


	/**
	 * Start editing an existing prescription period
	 * @param pm
	 */
	public void startEditingMedicine(PrescribedMedicine pm) {
/*		if (prescribedMedicineId == null)
			throw new RuntimeException("Medicine not define for editing");
		
		PrescribedMedicine pm = null;
		TbCase tbcase = caseHome.getInstance();
		for (PrescribedMedicine aux: tbcase.getPrescribedMedicines()) {
			if (aux.getId().equals(prescribedMedicineId)) {
				pm = aux;
				break;
			}
		}

		if (pm == null)
			throw new RuntimeException("Medicine not define for editing");
		
*/		Contexts.getConversationContext().set("prescribedMedicine", pm);
		pmcopy = prescribedMedicineHome.clonePrescribedMedicine(pm);
		preservePreviousPeriod = true;
		formEditing = FormEditing.MEDICINE;
		validated = false;
	}


	/**
	 * Add a medicine to the prescription of the case
	 */
	public void endMedicineEditing() {
		validated = false;
		
		TbCase tbcase = caseHome.getInstance();
		
		PrescribedMedicine pm = (PrescribedMedicine)Contexts.getConversationContext().get("prescribedMedicine");
//		boolean bNew = tbcase.getPrescribedMedicines().contains(pm);
		boolean bNew = pmcopy == null;
		
		if (pm.getSource() == null) {
			SourcesQuery sources = (SourcesQuery)Component.getInstance("sources");
			if (sources.isSingleResult())
				pm.setSource(sources.getResultList().get(0));
			else return;
		}

		if (!checkDateBasicRules(pm.getPeriod().getIniDate(), pm.getPeriod().getEndDate()))
			return;
		
		
/*		if (pm.getPeriod().getIniDate().before(tbcase.getTreatmentPeriod().getIniDate())) {
			facesMessages.addToControlFromResourceBundle("edtIniDate", "javax.faces.component.UIInput.REQUIRED");
			return;
		}
*/
		boolean periodChanged = (pmcopy != null) && (!pmcopy.getPeriod().equals(pm.getPeriod()));

		// there is a clone and the period was changed ?
		if ((periodChanged) && (preservePreviousPeriod)) {
			tbcase.getPrescribedMedicines().remove(pm);
			tbcase.getPrescribedMedicines().add(pmcopy);
			prescribedMedicineHome.removePeriod(pm.getPeriod(), pm.getMedicine(), null);
			tbcase.getPrescribedMedicines().add(pm);
		}
		else {
			prescribedMedicineHome.removePeriod(pm.getPeriod(), pm.getMedicine(), pm);
			if (periodChanged)
				tbcase.setRegimen(null);
		}

		if (!tbcase.getPrescribedMedicines().contains(pm)) {
			pm.setTbcase(tbcase);
			tbcase.getPrescribedMedicines().add(pm);
		}
		
//		if (bNew) 
		regimenMovedToIndivid = checkregimenMovedToIndivid();
			
		updateTreatmentPeriod();
		
		validated = true;
		formEditing = FormEditing.NONE;
		
		if (bNew)
		 	 Events.instance().raiseEvent("treatment-new-medicine", pm);
		else Events.instance().raiseEvent("treatment-edit-medicine", pm);

		refreshPrescriptionTable();
	}


	/**
	 * @return
	 */
	public boolean isNewPrescribedMedicine() {
		return pmcopy == null;
	}


	/**
	 * Start removing a specific period
	 * @param pm
	 */
	public void startRemovePeriod(PrescribedMedicine pm) {
		iniDate = pm.getPeriod().getIniDate();
		endDate = pm.getPeriod().getEndDate();
		Contexts.getConversationContext().set("prescribedMedicine", pm);
		validated = false;
		formEditing = FormEditing.REMOVE;
	}

	
	/**
	 * Confirm operation to remove a period 
	 */
	public void endRemovePeriod() {
		if (!checkDateBasicRules(iniDate, endDate))
			return;
		
		PrescribedMedicine pm = (PrescribedMedicine)Contexts.getConversationContext().get("prescribedMedicine");

		Period p = new Period(iniDate, endDate);
		if (prescribedMedicineHome.removePeriod(p, pm.getMedicine(), null)) {
			regimenMovedToIndivid = checkregimenMovedToIndivid();
		}
		updateTreatmentPeriod();
		refreshPrescriptionTable();
		validated = true;
		formEditing = FormEditing.NONE;
		
		Events.instance().raiseEvent("treatment-remove-period", pm.getMedicine(), p);
	}

	
	/**
	 * Remove a prescribed medicine from the treatment
	 * @param pm
	 */
	public void removePrescribedMedicine(PrescribedMedicine pm) {
		TbCase tbcase = caseHome.getInstance();
		
		tbcase.getPrescribedMedicines().remove(pm);

		if (entityManager.contains(pm))
			entityManager.remove(pm);

		tbcase.setRegimen(null);
		updateTreatmentPeriod();
	}


	/**
	 * Update treatment period based on the periods of the prescribed medicines
	 */
	private void updateTreatmentPeriod() {
		TbCase tbcase = caseHome.getInstance();
		
		Date dtini = null;
		Date dtend = null;
		for (PrescribedMedicine pm: tbcase.getPrescribedMedicines()) {
			Period p = pm.getPeriod();
			
			if ((dtini == null) || (p.getIniDate().before(dtini)))
				dtini = p.getIniDate();
			if ((dtend == null) || (p.getEndDate().after(dtend)))
				dtend = p.getEndDate();
		}
		
		// update treatment health unit periods
		if ((dtini != null) && (dtend != null)) {
			tbcase.setTreatmentPeriod( new Period(dtini, dtend) );

			List<TreatmentHealthUnit> lst = tbcase.getSortedTreatmentHealthUnits();
			TreatmentHealthUnit firstHU = lst.get(0);
			TreatmentHealthUnit lastHU = lst.get( lst.size() - 1 );
			
			firstHU.getPeriod().setIniDate(dtini);
			lastHU.getPeriod().setEndDate(dtend);
		}
	}
	


	/** 
	 * Crop the treatment period according to the new period
	 * @param iniDate
	 * @param endDate
	 */
	public void cropTreatmentPeriod(Period p) {
		TbCase tbcase = caseHome.getInstance();
		int index = 0;
	
		// crop treatment health units
		while (index < tbcase.getHealthUnits().size()) {
			TreatmentHealthUnit hu = tbcase.getHealthUnits().get(index);
			// if doesn't intersect, so it's out of the period
			if (!hu.getPeriod().intersect(p)) {
				tbcase.getHealthUnits().remove(hu);
				if (entityManager.contains(hu))
					entityManager.remove(hu);
			}
			else index++;
		}
		
		// crop prescribed medicines
		index = 0;
		while (index < tbcase.getPrescribedMedicines().size()) {
			PrescribedMedicine pm = tbcase.getPrescribedMedicines().get(index);
			if (!pm.getPeriod().intersect(p)) {
				tbcase.getPrescribedMedicines().remove(pm);
				if (entityManager.contains(pm))
					entityManager.remove(pm);
			}
			else index++;
		}

		// set the new treatment period, if necessary
		if (tbcase.getTreatmentPeriod().getIniDate().before(p.getIniDate()) || tbcase.getTreatmentPeriod().getEndDate().after(p.getEndDate())) 
			tbcase.setTreatmentPeriod(p);
	}
	
	
	/**
	 * Check basic constraints for date fields
	 * @return
	 */
	protected boolean checkDateBasicRules(Date iniDate, Date endDate) {
		if ((iniDate == null) || (endDate == null)) {
			facesMessages.add("Start and finish dates are required");
			return false;
		}

		// check if end date is before the initial date
		if (endDate.before(iniDate)) {
			facesMessages.addToControlFromResourceBundle("edtIniDate", "form.inienddate");
			return false;
		}
		
		if(endDate.before(tbcase.getSortedTreatmentHealthUnits().get(tbcase.getSortedTreatmentHealthUnits().size()-1).getPeriod().getIniDate())){
			facesMessages.addToControlFromResourceBundle("edtEndContPhase", "tbcase.treat.dates.msg01");
			return false;
		}
		
		return true;
	}
	
	
	/**
	 * Undo the treatment, turning the case to the 'waiting for treatment' state again
	 */
	@Transactional
	public String undoTreatment() {
		TbCase tbcase = caseHome.getInstance();

		Events.instance().raiseEvent("treatment-undone");
		
		tbcase.setState(CaseState.WAITING_TREATMENT);
		tbcase.setTreatmentPeriod(null);
		tbcase.setOwnerUnit(null);
		tbcase.setIniContinuousPhase(null);
		tbcase.setRegimen(null);
		tbcase.setRegimenIni(null);
		tbcase.setInitialRegimenWithSecondLineDrugs(null);
		tbcase.setTreatmentCategory(null);

		Integer id = caseHome.getInstance().getId();

		//Register removal of PrescribedMedicine for Desktop Sync
		List<PrescribedMedicine> pms = entityManager.createQuery("from PrescribedMedicine where tbcase.id = " + id.toString()).getResultList();
		for(PrescribedMedicine pm : pms)
			App.registerDeletedSyncEntity(pm);
		entityManager.createQuery("delete from PrescribedMedicine where tbcase.id = " + id.toString()).executeUpdate();

		//Register removal of TreatmentHealthUnit for Desktop Sync
		List<TreatmentHealthUnit> thus = entityManager.createQuery("from TreatmentHealthUnit where tbcase.id = " + id.toString()).getResultList();
		for(TreatmentHealthUnit thu : thus)
			App.registerDeletedSyncEntity(thu);
		entityManager.createQuery("delete from TreatmentHealthUnit where tbcase.id = " + id.toString()).executeUpdate();
		
//		tbcase.getTreatmentPeriod().set(null, null);
		tbcase.getHealthUnits().clear();
		tbcase.getPrescribedMedicines().clear();

		tbcase.setOwnerUnit(tbcase.getNotificationUnit());
		
		caseHome.setDisplayMessage(false);
		caseHome.persist();

		//Register removal of TreatmentMonitoring for Desktop Sync
		List<TreatmentMonitoring> tms = entityManager.createQuery("from TreatmentMonitoring tm where tm.tbcase.id = :caseId")
				.setParameter("caseId", tbcase.getId())
				.getResultList();
		for(TreatmentMonitoring tm : tms)
			App.registerDeletedSyncEntity(tm);
		entityManager.createQuery("delete from TreatmentMonitoring tm where tm.tbcase.id = :caseId")
				.setParameter("caseId", tbcase.getId())
				.executeUpdate();

		facesMessages.addFromResourceBundle("cases.treat.undo.executed");
		
		return "treatment-undone";
	}


	/**
	 * Check if initial treatment date can be changed
	 * @return
	 */
	public boolean isCanChangeIniTreatmentDate() {
		return minIniDate == null;
	}

	
	/**
	 * Check if initial of continuous phase can be changed 
	 * @return
	 */
	public boolean isCanChangeIniContinuousPhase() {
		Date iniContPhase = caseHome.getInstance().getIniContinuousPhase();
		return ((iniContPhase == null) || (minIniDate == null) || (iniContPhase.after(minIniDate)));
	}
	
	
	public Date getIniDate() {
		return iniDate;
	}

	public void setIniDate(Date iniDate) {
		this.iniDate = iniDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}


	public boolean isValidated() {
		return validated;
	}


	public FormEditing getFormEditing() {
		return formEditing;
	}


	public void setFormEditing(FormEditing formEditing) {
		this.formEditing = formEditing;
	}
	


	/**
	 * @return the iniContinuousPhase
	 */
	public Date getIniContinuousPhase() {
		return iniContinuousPhase;
	}


	/**
	 * @param iniContinuousPhase the iniContinuousPhase to set
	 */
	public void setIniContinuousPhase(Date iniContinuousPhase) {
		this.iniContinuousPhase = iniContinuousPhase;
	}

	/**
	 * Return the list of months as options for the user
	 * @return
	 */
	public List<SelectItem> getMonths() {
		if (months == null) {
			months = new ArrayList<SelectItem>();
			for (int i = 1; i <= 24; i++) {
				months.add(new SelectItem(i, Integer.toString(i)));
			}
		}
		return months;
	}


	/**
	 * Return the list of months as options for the user
	 * @return
	 */
	public List<SelectItem> getIniMonths() {
		if (iniMonths == null) {
			iniMonths = new ArrayList<SelectItem>();
			for (int i = 0; i < 24; i++) {
				iniMonths.add(new SelectItem(i, Integer.toString(i + 1)));
			}
		}
		return iniMonths;
	}


	/**
	 * Return the options for the dose unit during prescription
	 * @return
	 */
	public List<SelectItem> getDoses() {
		if (doses == null) {
			doses = new ArrayList<SelectItem>();
			doses.add(new SelectItem(null, "-"));
			for (int i = 1; i <= 10; i++) {
				doses.add(new SelectItem(i, Integer.toString(i)));
			}
		}

		return doses;
	}

	
	/**
	 * Return a reference to a prescription table component
	 * @return
	 */
	protected void refreshPrescriptionTable() {
		PrescriptionTable tbl = (PrescriptionTable)Component.getInstance("prescriptionTable");
		if (tbl != null)
			tbl.refresh();
	}



	/**
	 * @return the tbunitselection
	 */
	public TBUnitSelection getTbunitselection() {
		if (tbunitselection == null) {
			TbCase tbcase = caseHome.getInstance();
			TBUnitType filter;
			if (tbcase.getClassification() == CaseClassification.DRTB)
				 filter = TBUnitType.MDRHEALTH_UNITS;
			else filter = TBUnitType.TBHEALTH_UNITS;
			tbunitselection= new TBUnitSelection("unitid", true, filter);
		}
		return tbunitselection;
	}


	/**
	 * @return the preservePreviousPeriod
	 */
	public boolean isPreservePreviousPeriod() {
		return preservePreviousPeriod;
	}


	/**
	 * @param preservePreviousPeriod the preservePreviousPeriod to set
	 */
	public void setPreservePreviousPeriod(boolean preservePreviousPeriod) {
		this.preservePreviousPeriod = preservePreviousPeriod;
	}


	/**
	 * Check if changes in the medicine turned the regimen to an individualized
	 */
	public boolean checkregimenMovedToIndivid() {
		TbCase tbcase = caseHome.getTbCase();
		Regimen reg = tbcase.getRegimen();
		
		if (reg == null)
			return true;

		// get list of medicines in the regimen
		List<Integer> lst = entityManager.createQuery("select s.substance.id from Regimen r, in(r.medicines) med, in(med.medicine.components) s " +
				"where r.id = :id and med.medicine.line <> :line")
				.setParameter("id", reg.getId())
				.setParameter("line", MedicineLine.OTHER)
				.getResultList();

		// get list of medicines prescribed
		List<Medicine> meds = new ArrayList<Medicine>();
		for (PrescribedMedicine pm: tbcase.getPrescribedMedicines()) {
			if ((pm.getMedicine().getLine() != MedicineLine.OTHER) && (!meds.contains(pm.getMedicine()))) {
				meds.add(pm.getMedicine());
			}
		}

		if (meds.size() == 0)
			return false;
		
		// create query to select substances of the regimen
		String s = "";
		for (Medicine med: meds) {
			String sid = med.getId().toString();
			if (!s.contains(sid)) {
				if (!s.isEmpty())
					s += ",";
				s += med.getId().toString();
			}
		}
		s = "(" + s + ")";
		List<Integer> subs = entityManager.createQuery("select comp.substance.id from MedicineComponent comp where comp.medicine.id in " + s)
			.getResultList();

		for (Integer id: subs) {
			if (!lst.contains(id))
				return true;
		}
		
		for (Integer id: lst) {
			if (!subs.contains(id))
				return true;
		}
		
		return false;
	}


	/**
	 * @return the regimenMovedToIndivid
	 */
	public boolean isRegimenMovedToIndivid() {
		return regimenMovedToIndivid;
	}
}
