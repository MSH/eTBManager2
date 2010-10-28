package org.msh.tb.cases.treatment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.msh.mdrtb.entities.CaseRegimen;
import org.msh.mdrtb.entities.PrescribedMedicine;
import org.msh.mdrtb.entities.TbCase;
import org.msh.mdrtb.entities.TreatmentHealthUnit;
import org.msh.mdrtb.entities.enums.CaseState;
import org.msh.tb.cases.CaseHome;
import org.msh.utils.date.DateUtils;
import org.msh.utils.date.Period;


@Name("treatmentHome")
@Scope(ScopeType.CONVERSATION)
public class TreatmentHome {
	// forms displayed by the page
	public enum FormEditing {	NONE, TREATMENT, HEALTHUNIT, REGIMEN, MEDICINE; 	}

	@In(required=true) CaseHome caseHome;
	@In(create=true) FacesMessages facesMessages;
	@In(required=false) PrescriptionTable prescriptionTable;
	@In EntityManager entityManager;
	
	private Date iniDate;
	private Date endDate;
	
	private List<Date> prescriptionChanges;

	/**
	 * Period been edited
	 */
	private Period period;
	
	/**
	 * Check if initial date can be changed 
	 */
	private boolean canChangeIniDate;

	/**
	 * Return to the UI if there is any validation problem during ajax requests
	 */
	private boolean validated;

	private TreatmentHealthUnit healthUnit;
	private FormEditing formEditing;
	

	/**
	 * Save changes to the treatment
	 * @return
	 */
	public String saveChanges() {
		if (prescriptionTable != null)
			prescriptionTable.refresh();

		caseHome.getInstance().updateDaysTreatPlanned();
		return caseHome.persist();
	}
	

	/**
	 * Start editing in the treatment period
	 */
	public void startTreatmentPeriodEditing() {
		TbCase tbcase = caseHome.getInstance();

		List<TreatmentHealthUnit> lst = tbcase.getSortedTreatmentHealthUnits();

		healthUnit = lst.get(lst.size() - 1);
		
		// ini treatment date can only be changed if there is only one treatment health unit
		canChangeIniDate = lst.size() == 1;
		
		iniDate = tbcase.getTreatmentPeriod().getIniDate();
		endDate = healthUnit.getPeriod().getEndDate();
		
		formEditing = FormEditing.TREATMENT;
	}
	
	
	/**
	 * Start editing of health unit properties
	 * @param hu
	 */
	public void startHealthUnitEditing(TreatmentHealthUnit hu) {
		healthUnit = hu;
		TbCase tbcase = caseHome.getInstance();

		List<TreatmentHealthUnit> lst = tbcase.getSortedTreatmentHealthUnits();
		canChangeIniDate = lst.indexOf(hu) == 0;
		
		iniDate = hu.getPeriod().getIniDate();
		endDate = hu.getPeriod().getEndDate();
		
		formEditing = FormEditing.HEALTHUNIT;
	}


	/**
	 * Confirm editing of the treatment period
	 */
	public void endTreatmentPeriodEditing() {
		validated = checkDateBasicRules(iniDate, endDate);
		
		if (!validated)
			return;

		if (canChangeIniDate) {
			healthUnit.getPeriod().setIniDate(iniDate);
			TbCase tbcase = caseHome.getInstance();
			tbcase.getTreatmentPeriod().setIniDate(iniDate);
		}
		
		healthUnit.getPeriod().setEndDate(endDate);
		formEditing = FormEditing.NONE;

		if (prescriptionTable != null)
			prescriptionTable.refresh();
		
		cropTreatmentPeriod(caseHome.getInstance().getTreatmentPeriod());
	}
	
	
	/**
	 * Create list of dates indicating the changes in the medicine prescription
	 */
	private void createListMedicineChanges() {
		prescriptionChanges = new ArrayList<Date>();
		
		TbCase tbcase = caseHome.getInstance();
		
		// create period list
		prescriptionChanges.add(  tbcase.getTreatmentPeriod().getIniDate());
		prescriptionChanges.add(tbcase.getTreatmentPeriod().getEndDate());
		for (PrescribedMedicine pm: tbcase.getPrescribedMedicines()) {
			Date dt = pm.getPeriod().getIniDate();
			if (!prescriptionChanges.contains(dt))
				prescriptionChanges.add(dt);
			dt = DateUtils.incDays( pm.getPeriod().getEndDate(), 1 );
			if (!prescriptionChanges.contains(dt))
				prescriptionChanges.add(dt);
		}
		Collections.sort(prescriptionChanges);
	}
	

	/**
	 * Initialize the period been edited
	 */
	public void initializePeriod() {
		TbCase tbcase = caseHome.getInstance();
		
		if (tbcase.getHealthUnits().size() == 0)
			return;
		
		if (tbcase.getHealthUnits().size() > 1)
			 period = tbcase.getHealthUnits().get(tbcase.getHealthUnits().size() - 1).getPeriod();
		else period = tbcase.getTreatmentPeriod();
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
		
		// crop case regimens
		index = 0;
		while (index < tbcase.getRegimens().size()) {
			CaseRegimen cr = tbcase.getRegimens().get(index);
			if (!cr.getPeriod().intersect(p)) {
				tbcase.getRegimens().remove(cr);
				if (entityManager.contains(cr))
					entityManager.remove(cr);
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
		
		// set the new treatment period
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
		
		return true;
	}
	
	
	/**
	 * Undo the treatment, turning the case to the 'waiting for treatment' state again
	 */
	public String undoTreatment() {
		TbCase tbcase = caseHome.getInstance();
		tbcase.setState(CaseState.WAITING_TREATMENT);
		tbcase.setTreatmentPeriod(null);

		Integer id = caseHome.getInstance().getId();
		entityManager.createQuery("delete from PrescribedMedicine where tbcase.id = " + id.toString()).executeUpdate();
		entityManager.createQuery("delete from TreatmentHealthUnit where tbCase.id = " + id.toString()).executeUpdate();
		entityManager.createQuery("delete from CaseRegimen where tbCase.id = " + id.toString()).executeUpdate();
		
//		tbcase.getTreatmentPeriod().set(null, null);
		tbcase.getHealthUnits().clear();
		tbcase.getRegimens().clear();
		tbcase.getPrescribedMedicines().clear();

		caseHome.setDisplayMessage(false);
		caseHome.persist();

		facesMessages.addFromResourceBundle("cases.treat.undo.executed");
		
		return "treatment-undone";
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


	public boolean isCanChangeIniDate() {
		return canChangeIniDate;
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
	 * Return list of changes in the medicine prescription 
	 * @return
	 */
	public List<Date> getPrescriptionChanges() {
		if (prescriptionChanges == null)
			createListMedicineChanges();
		return prescriptionChanges;
	}


	public Period getPeriod() {
		if (period == null)
			initializePeriod();
		return period;
	}
}
