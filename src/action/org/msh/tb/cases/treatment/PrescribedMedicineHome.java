package org.msh.tb.cases.treatment;

 import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.model.SelectItem;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.msh.mdrtb.entities.Medicine;
import org.msh.mdrtb.entities.PrescribedMedicine;
import org.msh.mdrtb.entities.TbCase;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.SourcesQuery;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.treatment.TreatmentHome.FormEditing;
import org.msh.utils.date.DateUtils;
import org.msh.utils.date.Period;


/**
 * Handle medicine prescribed to a patient
 * @author Ricardo Memoria
 *
 */
@Name("prescribedMedicineHome")
public class PrescribedMedicineHome extends EntityHomeEx<PrescribedMedicine> {
	private static final long serialVersionUID = 4665192156115818462L;

	@In(required=true) CaseHome caseHome;
	@In(create=true) FacesMessages facesMessages;
	@In(create=true) TreatmentHome treatmentHome;
	@In(required=false) PrescriptionTable prescriptionTable;
	@In(create=true) CaseRegimenHome caseRegimenHome;
	
	private List<SelectItem> months;
	private List<SelectItem> iniMonths;
	private List<SelectItem> doses;

	
	/**
	 * During editing, if true, the system will also update the period of medicines with same dates
	 */
	private boolean updateMedicinesSamePeriod;

	/**
	 * Used by the UI to check if there is a validation error when generating the case
	 */
	private boolean validated;

	@Factory("prescribedMedicine")
	public PrescribedMedicine getPrescribedMedicine() {
		return getInstance();
	}


	/**
	 * Start entering a new prescription period
	 */
	public void startNew() {
		setInstance(null);
		Contexts.getConversationContext().set("prescribedMedicine", getInstance());

		// check the initial treatment date for individualized regimen
		TbCase tbcase = caseHome.getInstance();
		Period p = tbcase.getTreatmentPeriod();
		PrescribedMedicine pm = getInstance();
		pm.setTbcase(tbcase);
		if (p == null) {
			p = new Period();
			caseHome.getInstance().setTreatmentPeriod(p);
		}

		if (p.getIniDate() == null) 
			p.setIniDate(new Date());
		if (p.getEndDate() == null)
			p.setEndDate(new Date());

		treatmentHome.setFormEditing(FormEditing.MEDICINE);
		validated = false;
	}


	/**
	 * Start editing an existing prescription period
	 * @param pm
	 */
	public void startEditing(PrescribedMedicine pm) {
		setInstance(pm);
		Contexts.getConversationContext().set("prescribedMedicine", pm);
		treatmentHome.setFormEditing(FormEditing.MEDICINE);
		validated = false;
	}


	/**
	 * Add a medicine to the prescription of the case
	 */
	public void updateChanges() {
		validated = false;
		
		TbCase tbcase = getTbCase();
		
		PrescribedMedicine pm = getInstance();
		
		if (pm.getSource() == null) {
			SourcesQuery sources = (SourcesQuery)Component.getInstance("sources");
			if (sources.isSingleResult())
				pm.setSource(sources.getResultList().get(0));
			else return;
		}
		
		if (!tbcase.getPrescribedMedicines().contains(pm)) {
			pm.setTbcase(tbcase);
			tbcase.getPrescribedMedicines().add(pm);
		}

		if (!treatmentHome.checkDateBasicRules(pm.getPeriod().getIniDate(), pm.getPeriod().getEndDate()))
			return;
		
		if (pm.getPeriod().getIniDate().before(tbcase.getTreatmentPeriod().getIniDate())) {
			facesMessages.addToControlFromResourceBundle("edtIniDate", "javax.faces.component.UIInput.REQUIRED");
			return;
		}

		updateDates(pm);
		
		validated = true;
		treatmentHome.setFormEditing(FormEditing.NONE);
		caseRegimenHome.updateRegimensByMedicinesPrescribed();

		if (prescriptionTable != null)
			prescriptionTable.refresh();
	}

	
	/**
	 * Remove a prescribed medicine from the treatment
	 * @param pm
	 */
	public void deleteMedicine(PrescribedMedicine pm) {
		TbCase tbcase = getTbCase();
		tbcase.getPrescribedMedicines().remove(pm);

		if (getEntityManager().contains(pm))
			getEntityManager().remove(pm);
		
		caseRegimenHome.updateRegimensByMedicinesPrescribed();

		if (prescriptionTable != null)
			prescriptionTable.refresh();
	}


	/**
	 * Update dates of the medicine prescribed to prevent that the prescription doesn't overlap other periods with the same medicine
	 * @param pm
	 */
	protected void updateDates(PrescribedMedicine pm) {
		TbCase tbcase = caseHome.getInstance();
		Period period = pm.getPeriod();
		
		List<PrescribedMedicine> lst = createPrescribedMedicineList(pm.getMedicine());
		for (PrescribedMedicine aux: lst) {
			if (aux != pm) {
				// prescription is inside new/edited one?
				if (period.contains(aux.getPeriod())) {
//				if (DateUtils.isPeriodInside(dtIni, dtEnd, aux.getIniDate(), aux.getEndDate())) {
					tbcase.getPrescribedMedicines().remove(aux);
					if (getEntityManager().contains(aux))
						getEntityManager().remove(aux);
				}
				else
				// prescription is containing the whole new/edit prescription
				if (aux.getPeriod().contains(period)) {	
//				if ((aux.getIniDate().before(dtIni)) && (aux.getEndDate().after(dtEnd))) {
					PrescribedMedicine aux2 = clonePrescribedMedicine(aux);
					aux.getPeriod().setIniDate(DateUtils.incDays(period.getIniDate(), -1));
					aux2.getPeriod().setEndDate(DateUtils.incDays(period.getEndDate(), 1));
					tbcase.getPrescribedMedicines().add(aux2);
				}
				else 
				if (period.isDateInside(aux.getPeriod().getIniDate())) {
//				if ((!aux.getIniDate().before(dtIni)) && (!aux.getIniDate().after(dtEnd))) {
					aux.getPeriod().cutIni( DateUtils.incDays(period.getEndDate(), 1) );
				}
				else
				if (period.isDateInside(aux.getPeriod().getEndDate())) {
//				if ((!aux.getEndDate().before(dtIni)) && (!aux.getEndDate().after(dtEnd))) {
					aux.getPeriod().cutEnd( DateUtils.incDays(period.getIniDate(), -1) );
				}
			}
		}
		
		if (tbcase.getTreatmentPeriod().getEndDate().before(period.getEndDate()))
			tbcase.getTreatmentPeriod().setEndDate(period.getEndDate());
	}


	
	/**
	 * Create a clone of the prescribed medicine object
	 * @param pm
	 * @return
	 */
	protected PrescribedMedicine clonePrescribedMedicine(PrescribedMedicine pm) {
		PrescribedMedicine aux = new PrescribedMedicine();
		aux.setDoseUnit(pm.getDoseUnit());
		aux.getPeriod().setEndDate(pm.getPeriod().getEndDate());
		aux.getPeriod().setIniDate(pm.getPeriod().getIniDate());
		aux.setFrequency(pm.getFrequency());
		aux.setMedicine(pm.getMedicine());
		aux.setSource(pm.getSource());
		aux.setTbcase(pm.getTbcase());
		return aux;
	}
	
	/**
	 * Generate a list of prescribed medicines of a specific medicine for the current case
	 * @param med
	 * @return
	 */
	protected List<PrescribedMedicine> createPrescribedMedicineList(Medicine med) {
		List<PrescribedMedicine> lst = new ArrayList<PrescribedMedicine>();
		TbCase tbcase = getTbCase();
		
		for (PrescribedMedicine pm: tbcase.getPrescribedMedicines()) {
			if (pm.getMedicine().equals(med))
				lst.add(pm);
		}
		return lst;
	}
	
	
	/**
	 * Return TB case instance in use
	 * @return {@link TbCase} instance
	 */
	protected TbCase getTbCase() {
		return caseHome.getInstance();
	}

	public boolean isValidated() {
		return validated;
	}

	public boolean isUpdateMedicinesSamePeriod() {
		return updateMedicinesSamePeriod;
	}

	public void setUpdateMedicinesSamePeriod(boolean updateMedicinesSamePeriod) {
		this.updateMedicinesSamePeriod = updateMedicinesSamePeriod;
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
}
