package org.msh.tb.entities;

 import java.io.Serializable;
import java.util.Date;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.validator.NotNull;
import org.msh.utils.date.DateUtils;
import org.msh.utils.date.Period;


/**
 * Store information about a medicine prescribed to a case
 * @author Ricardo Memoria
 *
 */
@Entity
@Table(name="prescribedmedicine")
public class PrescribedMedicine implements Serializable {
	private static final long serialVersionUID = 7239969189199419487L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

	/**
	 * Medicine prescribed
	 */
	@ManyToOne
	@JoinColumn(name="MEDICINE_ID")
	@NotNull
	private Medicine medicine;

	/**
	 * Period of administration of the medicine prescribed
	 */
	@Embedded
	private Period period = new Period();
	
	/**
	 * Dose unit prescribed for the period
	 */
	private int doseUnit;
	
	/**
	 * Weekly frequency of administration of the medicine
	 */
	private int frequency;
	
	/**
	 * Medicine source
	 */
	@ManyToOne
	@JoinColumn(name="SOURCE_ID")
	@NotNull
	private Source source;

	/**
	 * Optional comments entered by the user
	 */
	@Lob
	private String comments;
	
	/**
	 * Case related to the medicine prescribed
	 */
	@ManyToOne
	@JoinColumn(name="CASE_ID")
	@NotNull
	private TbCase tbcase;


	/**
	 * Check if has any comment to the prescribed medicine
	 * @return true if has comments
	 */
	public boolean isHasComments() {
		return (comments != null) && (!comments.isEmpty());
	}

	
	/**
	 * Returns the weekly frequency object related to the prescription
	 * @return WeeklyFrequency object instance
	 */
	public WeeklyFrequency getWeeklyFrequency() {
		Workspace ws = getMedicine().getWorkspace();
		return ws.getWeeklyFrequency(getFrequency());
	}

	/**
	 * Calculates the estimated consumption for the period
	 * @return
	 */
	public int calcEstimatedDispensing(Period p) {
		int doseUnit = getDoseUnit();
		
		int qtd = calcNumDaysDispensing(p) * doseUnit;
		
		return qtd;
	}


	/**
	 * Initialize the attributes from a {@link MedicineRegimen} object
	 * @param medReg
	 * @param p
	 */
	public void initializeFromRegimen(MedicineRegimen medReg, Date iniDate) {
		Period p = new Period();
		p.setIniDate(iniDate);
		Date dtend = DateUtils.incDays( DateUtils.incMonths(iniDate, medReg.getMonthsTreatment()), -1);
		p.setEndDate(dtend);

		doseUnit = medReg.getDefaultDoseUnit();
		frequency = medReg.getDefaultFrequency();
		medicine = medReg.getMedicine();
		source = medReg.getDefaultSource();
		period = p;
	}


	/**
	 * Return the number of prescribed days for the period
	 * @return
	 */
	public int getNumPrescribedDays() {
		return getWeeklyFrequency().calcNumDays(period);
	}
	
	/**
	 * Calculates the number of days of dispensing in the period
	 * @param dtIni
	 * @param dtFim
	 * @return
	 */
	public int calcNumDaysDispensing(Period p) {
		Period aux = new Period(p);
		aux.intersect(getPeriod());
		int numDays = getWeeklyFrequency().calcNumDays(aux);
		
		return numDays;
	}

	
	/**
	 * Return the month of treatment according to the beginning of the treatment and the initial date of the period
	 * @return
	 */
	public Integer getIniMonth() {
		if ((period == null) || (period.isEmpty()) || (tbcase == null))
			return null;
		
		return tbcase.getMonthTreatment(period.getIniDate()) - 1;
	}
	
	
	/**
	 * Return number of months in the period
	 * @return
	 */
	public Integer getMonths() {
		return (period != null? period.getMonths(): null);
	}


	/**
	 * Change the initial month of the period
	 * @param month
	 */
	public void setIniMonth(Integer month) {
		if (period.isEmpty()) {
			if (tbcase != null)
				period = new Period(tbcase.getTreatmentPeriod());
			
			if (period.getIniDate() == null)
				period.set(new Date(), new Date());
		}
		
		Date dt = DateUtils.incMonths(period.getIniDate(), month-1);
		period.movePeriod(dt);
	}


	/**
	 * Specify the number of months of the period
	 * @param months
	 */
	public void setMonths(Integer months) {
		if (period == null)
			return;
		if (period.getIniDate() == null) {
			if (tbcase != null)
				 period.setIniDate(tbcase.getTreatmentPeriod().getIniDate());
			else period.setIniDate(new Date());
		}
		Date dt = DateUtils.incMonths(period.getIniDate(), months);
		dt = DateUtils.incDays(dt, -1);
		period.setEndDate(dt);
	}
	

	@Override
	public String toString() {
		return ((medicine != null) && (period != null)? period.toString() + " - " + medicine.toString(): null);
	}
	
	public Medicine getMedicine() {
		return medicine;
	}

	public void setMedicine(Medicine medicine) {
		this.medicine = medicine;
	}

	public int getDoseUnit() {
		return doseUnit;
	}

	public void setDoseUnit(int doseUnit) {
		this.doseUnit = doseUnit;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public Source getSource() {
		return source;
	}

	public void setSource(Source source) {
		this.source = source;
	}


	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public TbCase getTbcase() {
		return tbcase;
	}

	public void setTbcase(TbCase tbcase) {
		this.tbcase = tbcase;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}


	public Period getPeriod() {
		return period;
	}


	public void setPeriod(Period period) {
		this.period = period;
	}

}
