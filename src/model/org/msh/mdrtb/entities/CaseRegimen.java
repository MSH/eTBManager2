package org.msh.mdrtb.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.validator.NotNull;
import org.jboss.seam.international.Messages;
import org.msh.utils.date.DateUtils;
import org.msh.utils.date.Period;


/**
 * Store information about the regimen of a case
 * @author Ricardo Memoria
 *
 */
@Entity
public class CaseRegimen implements Serializable {
	private static final long serialVersionUID = -5681061096315046547L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="REGIMEN_ID")
	private Regimen regimen;
	
	@Embedded
	private Period period = new Period();
	
	@Temporal(TemporalType.DATE)
	private Date iniContPhase;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="CASE_ID", nullable=false)
	@NotNull
	private TbCase tbCase;

	@Column(length=200)
	private String comments;

	
	/**
	 * Check if a regimen is individualized
	 * @return
	 */
	public boolean isIndividualized() {
		return (regimen == null);
	}
	
	/**
	 * Check if regimen has intensive phase
	 * @return true if regimen has intensive phase, otherwise returns false
	 */
	public boolean isHasIntensivePhase() {
		return (iniContPhase == null) || ((iniContPhase != null) && (!iniContPhase.equals(period.getIniDate())));
	}

	
	/**
	 * Check if regimen has continuous phase
	 * @return
	 */
	public boolean isHasContinuousPhase() {
		return (iniContPhase != null);
	}

	
	/**
	 * Return intensive phase period
	 * @return {@link Period} instance containing the intensive phase period, or null if there is no intensive phase
	 */
	public Period getPeriodIntPhase() {
		if (!isHasIntensivePhase())
			return null;
		if (iniContPhase != null)
			 return new Period(period.getIniDate(), DateUtils.incDays(iniContPhase, -1));
		else return period;
	}
	

	/**
	 * Return continuous phase period
	 * @return {@link Period} instance containing the continuous phase period, or null if there is no continuous phase
	 */
	public Period getPeriodContPhase() {
		if (!isHasContinuousPhase())
			return null;
		if (iniContPhase != null)
			 return new Period(iniContPhase, period.getEndDate());
		else return period;
	}


	@Override
	public String toString() {
		if (regimen == null)
			 return Messages.instance().get("regimens.individualized");
		else return regimen.getName();
	}
	
	/**
	 * Returns if the date dt is a day of medicine prescription
	 * @param dt - Date
	 * @return true - if it's a day of medicine prescription
	 */
	public boolean isDayPrescription(Date dt) {
		if (!period.isDateInside(dt))
			return false;

		for (PrescribedMedicine pm: tbCase.getPrescribedMedicines()) {
			if (pm.getPeriod().isDateInside(dt)) {
				WeeklyFrequency wf = pm.getWeeklyFrequency();
				if (wf.isDateSet(dt))
					return true;
			}
		}
		
		return false;
	}


	/**
	 * Calculate number of prescribed days for the period of the regimen
	 * @return
	 */
/*	public int getNumPrescribedDays() {
		int numdays = 0;
		for (PrescribedMedicine pm: getMedicines()) {
			int num = pm.calcNumDaysDispensing(iniDate, endDate);
			if (num > numdays)
				numdays = num;
		}
		return numdays;
	}
*/	
	
	/**
	 * Returns number of days of the case regimen period
	 * @return number of days
	 */
	public Integer getNumDays() {
		return period.getDays();
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Regimen getRegimen() {
		return regimen;
	}

	public void setRegimen(Regimen regimen) {
		this.regimen = regimen;
	}


	public TbCase getTbCase() {
		return tbCase;
	}

	public void setTbCase(TbCase tbCase) {
		this.tbCase = tbCase;
	}


	public Date getIniContPhase() {
		return iniContPhase;
	}

	public void setIniContPhase(Date iniContPhase) {
		this.iniContPhase = iniContPhase;
	}


	public void setComments(String comments) {
		this.comments = comments;
	}


	public String getComments() {
		return comments;
	}

	public Period getPeriod() {
		return period;
	}

	public void setPeriod(Period period) {
		this.period = period;
	}
}
