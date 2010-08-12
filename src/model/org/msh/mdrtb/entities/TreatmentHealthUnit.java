package org.msh.mdrtb.entities;

 import java.io.Serializable;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.validator.NotNull;
import org.msh.utils.date.Period;


@Entity
public class TreatmentHealthUnit implements Serializable {
	private static final long serialVersionUID = -7878679577509206518L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="CASE_ID")
	@NotNull
	private TbCase tbCase;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="UNIT_ID")
	@NotNull
	private Tbunit tbunit;

	@Embedded
	private Period period = new Period();
	
	
	/**
	 * Indicate if case is being transferred to this health unit
	 */
	private boolean transferring;

	
	public boolean isTransferring() {
		return transferring;
	}


	public void setTransferring(boolean transferring) {
		this.transferring = transferring;
	}


/*	public Date addRegimenPhase(Regimen regimen, RegimenPhase phase, Date dateIni, Date dateEnd, boolean fillDoses) {
		List<Integer> months = regimen.groupMonthsTreatment(phase);
		TbCase tbCase = getTbCase();
		
		Date dt = dateIni;
		for (Integer num: months) {
			CaseRegimen cr = new CaseRegimen();
			cr.setRegimen(regimen);
			cr.setIniDate(dt);
			dt = DateUtils.incMonths(dt, num);
			cr.setEndDate(DateUtils.incDays(dt, -1));
			if ((dateEnd != null) && (cr.getEndDate().after(dateEnd)))
				cr.setEndDate(dateEnd);
			cr.setPhase(phase);
			cr.setTbCase(tbCase);
			cr.setTreatmentHealthUnit(this);
			tbCase.getRegimens().add(cr);
			this.getRegimens().add(cr);

			List<MedicineRegimen> meds = regimen.groupMedicinesByMonthTreatment(phase, num);
			for (MedicineRegimen medreg: meds) {
				PrescribedMedicine pm = new PrescribedMedicine();
				if (fillDoses) {
					pm.setDoseUnit(medreg.getDefaultDoseUnit());
					pm.setFrequency(medreg.getDefaultFrequency());
				}
				pm.setMedicine(medreg.getMedicine());
				pm.setSource(medreg.getDefaultSource());
				pm.setCaseRegimen(cr);
				cr.getMedicines().add(pm);
			}
			
			if ((endDate != null) && (cr.getEndDate().equals(dateEnd))) 
				return DateUtils.incDays(cr.getEndDate(), 1);
		}
		return dt;
	}
*/
	
/*	public List<CaseRegimen> getRegimens() {
		return regimens;
	}

	public List<CaseRegimen> getSortedRegimens() {
		if (sortedregs == null) {
			Collections.sort(getRegimens(), new CaseRegimenComparator());
			sortedregs = regimens;
		}
		return sortedregs;
	}
	
	public void setRegimens(List<CaseRegimen> regimens) {
		this.regimens = regimens;
	}
*/
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public TbCase getTbCase() {
		return tbCase;
	}

	public void setTbCase(TbCase tbCase) {
		this.tbCase = tbCase;
	}

	public Tbunit getTbunit() {
		return tbunit;
	}

	public void setTbunit(Tbunit tbunit) {
		this.tbunit = tbunit;
	}


	public Period getPeriod() {
		return period;
	}


	public void setPeriod(Period period) {
		this.period = period;
	}
}
