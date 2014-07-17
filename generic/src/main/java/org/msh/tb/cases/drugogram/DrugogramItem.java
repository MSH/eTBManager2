package org.msh.tb.cases.drugogram;

import org.jboss.seam.international.Messages;
import org.msh.tb.cases.drugogram.Drugogram.MedicineColumn;
import org.msh.tb.entities.ExamXRay;
import org.msh.tb.entities.MedicalExamination;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DrugogramItem {
	private int month;
	private int year;
	private int monthTreatment;

	private String specimenId;
	private MedicalExamination medicalExamination;
	private ExamXRay examXRay;

	private Date dateCollected;
	private List<ExamResult> results = new ArrayList<ExamResult>();
	private Date regimenDate;
	private List<SubstanceItem> substances = new ArrayList<SubstanceItem>();
	private Drugogram drugogram;
	private List<SubstanceItem> substancesDisplay; 
	

	public ExamResult getFreeResultMicroscopy() {
		for (ExamResult res: results) {
			if (res.getExamMicroscopy() == null)
				return res;
		}
		ExamResult res = new ExamResult();
		results.add(res);
		return res;
	}
	
	
	public ExamResult newResult() {
		ExamResult res = new ExamResult();
		results.add(res);
		return res;
	}
	
	public SubstanceItem findSubstance(String medName) {
		for (SubstanceItem sub: substances) {
			if (sub.getMedicine().equals(medName)) {
				return sub;
			}
		}
		
		SubstanceItem sub = new SubstanceItem();
		sub.setMedicine(medName);
		substances.add(sub);
		return sub;
	}

	
	/**
	 * Check if the date informed is before the current date collected. If so, the date collected is replaced by the dt param
	 * @param dt Date to compare with dateCollected
	 * @return true if the date collected was replaced by the param dt 
	 */
	public boolean checkDateCollected(Date dt) {
		if ((dateCollected == null) || (dt.before(dateCollected))) {
			dateCollected = dt;
			return true;
		}
		return false;
	}
	
	public ExamResult getFreeResultCulture() {
		for (ExamResult res: results) {
			if (res.getExamCulture() == null)
				return res;
		}
		ExamResult res = new ExamResult();
		results.add(res);
		return res;		
	}

	
	/**
	 * @return the specimenId
	 */
	public String getSpecimenId() {
		return specimenId;
	}
	/**
	 * @param specimenId the specimenId to set
	 */
	public void setSpecimenId(String specimenId) {
		this.specimenId = specimenId;
	}
	/**
	 * @return the dateCollected
	 */
	public Date getDateCollected() {
		return dateCollected;
	}
	/**
	 * @param dateCollected the dateCollected to set
	 */
	public void setDateCollected(Date dateCollected) {
		this.dateCollected = dateCollected;
	}
	/**
	 * @return the results
	 */
	public List<ExamResult> getResults() {
		return results;
	}
	/**
	 * @param results the results to set
	 */
	public void setResults(List<ExamResult> results) {
		this.results = results;
	}
	/**
	 * @return the regimenDate
	 */
	public Date getRegimenDate() {
		return regimenDate;
	}
	/**
	 * @param regimenDate the regimenDate to set
	 */
	public void setRegimenDate(Date regimenDate) {
		this.regimenDate = regimenDate;
	}
	/**
	 * @return the substances
	 */
	public List<SubstanceItem> getSubstances() {
		return substances;
	}
	/**
	 * @param substances the substances to set
	 */
	public void setSubstances(List<SubstanceItem> substances) {
		this.substances = substances;
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
	 * @return the monthTreatment
	 */
	public int getMonthTreatment() {
		return monthTreatment;
	}
	/**
	 * @param monthTreatment the monthTreatment to set
	 */
	public void setMonthTreatment(int monthTreatment) {
		this.monthTreatment = monthTreatment;
	}


	/**
	 * @param drugogram the drugogram to set
	 */
	public void setDrugogram(Drugogram drugogram) {
		this.drugogram = drugogram;
	}


	/**
	 * @return the drugogram
	 */
	public Drugogram getDrugogram() {
		return drugogram;
	}

	public List<SubstanceItem> getSubstancesDisplay() {
		if (substancesDisplay == null) {
			createListSubstancesDisplay();
		}
		
		return substancesDisplay;
	}
	
	protected void createListSubstancesDisplay() {
		substancesDisplay = new ArrayList<SubstanceItem>();
		
		for (MedicineColumn med: drugogram.getMedicines()) {
			SubstanceItem item = findSubstance(med.getMedicine());
			substancesDisplay.add(item);
		}
	}


	/**
	 * Returns a key related to the system messages to display the month
	 * @return
	 */
	public String getMonthDisplay() {
		if (monthTreatment == -1)
			return Messages.instance().get("cases.exams.prevdt");
		
		if (monthTreatment == 0)
			 return Messages.instance().get("cases.exams.zero");
		else return Integer.toString(monthTreatment);
	}

	public MedicalExamination getMedicalExamination() {
		return medicalExamination;
	}


	public void setMedicalExamination(MedicalExamination medicalExamination) {
		this.medicalExamination = medicalExamination;
	}


	public ExamXRay getExamXRay() {
		return examXRay;
	}


	public void setExamXRay(ExamXRay examXRay) {
		this.examXRay = examXRay;
	}

}
