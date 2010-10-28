package org.msh.tb.cases.treatment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.msh.mdrtb.entities.CaseRegimen;
import org.msh.mdrtb.entities.Medicine;
import org.msh.mdrtb.entities.PrescribedMedicine;
import org.msh.mdrtb.entities.Regimen;
import org.msh.mdrtb.entities.TbCase;
import org.msh.mdrtb.entities.enums.RegimenPhase;
import org.msh.tb.RegimensQuery;
import org.msh.tb.cases.CaseHome;
import org.msh.utils.date.DateUtils;
import org.msh.utils.date.Period;

@Name("caseRegimenHome")
@Scope(ScopeType.CONVERSATION)
public class CaseRegimenHome {

	@In(required=true) CaseHome caseHome;
	@In(create=true) TreatmentHome treatmentHome;
	@In EntityManager entityManager;
	@In(create=true) RegimensQuery regimens;

	private Regimen regimen;
	
	/**
	 * Indicate if, when changing the regimen, if the periods of intensive and continuous phases should be preserved 
	 */
	private boolean preservePeriods;

	
	/**
	 * Change the current regimen
	 */
	public void changeRegimen() {
		
	}

	/**
	 * Update treatment regimens according to the medicines prescribed 
	 */
	public void updateRegimensByMedicinesPrescribed() {
		List<Date> dates = treatmentHome.getPrescriptionChanges();
		
		TbCase tbcase = caseHome.getTbCase();
		
		// create list of regimens by period
		List<CaseRegimen> regimens = new ArrayList<CaseRegimen>();
		CaseRegimen cr = null;

		// evaluate the periods
		for (int i = 1; i < dates.size(); i++) {
			Date dtIni = dates.get(i - 1);
			Date dtEnd = dates.get(i);
			// for sibling periods, the end date is 1 day before the initial date of the next period
			// so the system checks if there is more than 1 day between dates to consider it a period
			if (DateUtils.daysBetween(dtIni, dtEnd) > 1) {
				RegimenInfo  ri = findRegimenByMedicinesInPeriod( new Period(dtIni, DateUtils.incDays(dtEnd, -1)) );
				
				if (ri == null)
					ri = new RegimenInfo(null, null, dtIni, dtEnd);
				if ((cr == null) || (!mergeRegimen(cr, ri))) {
					cr = new CaseRegimen();
					cr.getPeriod().setIniDate(dtIni);
					cr.getPeriod().setEndDate(dtEnd);
					cr.setRegimen(ri.getRegimen());
					if (RegimenPhase.CONTINUOUS.equals(ri.getPhase())) {
						cr.setIniContPhase(dtIni);
					}
					regimens.add(cr);
				}
			}
		}

		// update regimens in the system
		int index = 0;
		for (CaseRegimen aux: regimens) {
			if (index < tbcase.getRegimens().size())
				cr = tbcase.getRegimens().get(index);
			else {
				cr = new CaseRegimen();
				cr.setTbCase(tbcase);
				tbcase.getRegimens().add(cr);
			}
			cr.setPeriod(aux.getPeriod());
			cr.setIniContPhase(aux.getIniContPhase());
			cr.setRegimen(aux.getRegimen());
			index++;
		}

		while (index < tbcase.getRegimens().size()) {
			cr = tbcase.getRegimens().get(index);
			if (entityManager.contains(cr))
				entityManager.remove(cr);
			tbcase.getRegimens().remove(index);
		}
	}



	/**
	 * Check if the regimen information is compatible with the case regimen, if so,
	 * it's merged into the {@link CaseRegimen} object
	 * @param cr
	 * @param info
	 * @return
	 */
	private boolean mergeRegimen(CaseRegimen cr, RegimenInfo info) {
		// is individualized ?
		if ((cr.getRegimen() == null) && (info.getRegimen() == null)) {
			cr.getPeriod().setEndDate(info.getEndDate());
			cr.setIniContPhase(null);
			return true;
		}
		
		// regimens are different
		if (cr.getRegimen() != info.getRegimen())
			return false;
		
		if (RegimenPhase.INTENSIVE.equals(info.getPhase())) {
			if (cr.isHasContinuousPhase())
				return false;
			cr.getPeriod().setEndDate(info.getEndDate());
		}
		else {
			if (cr.getIniContPhase() == null)
				cr.setIniContPhase(info.getIniDate());
			cr.getPeriod().setEndDate(info.getEndDate());
		}
		
		return true;
	}
	
	

	
	/**
	 * Find regimen according to medicines prescribed in the period
	 * @param dtIni
	 * @param dtEnd
	 * @return
	 */
	protected RegimenInfo findRegimenByMedicinesInPeriod(Period period) {
		List<Medicine> meds = new ArrayList<Medicine>();
		TbCase tbcase = caseHome.getInstance();
		
		// separate drugs in the period 
		for (PrescribedMedicine pm: tbcase.getPrescribedMedicines()) {
			if (pm.getPeriod().contains(period)) {
				if (!meds.contains(pm.getMedicine()))
					meds.add(pm.getMedicine());
			}
		}
		
		if (meds.size() == 0)
			return null;

		RegimenPhase p = null;
		List<RegimenInfo> lst = new ArrayList<RegimenInfo>();

		for (Regimen reg: regimens.getResultList()) {
			if (reg.compareMedicinesInPhase(RegimenPhase.INTENSIVE, meds))
				p = RegimenPhase.INTENSIVE;
			else
			if (reg.compareMedicinesInPhase(RegimenPhase.CONTINUOUS, meds))
				p = RegimenPhase.CONTINUOUS;
			
			if (p != null) {
				RegimenInfo ri = new RegimenInfo(reg, p, period.getIniDate(), period.getEndDate());
				lst.add(ri);
			}
		}

		if (lst.size() == 0) 
			return null;

		// return first regimen
		return lst.get(0);
	}



	/**
	 * Store temporary information about a regimen
	 * @author Ricardo Memoria
	 *
	 */
	public class RegimenInfo {
		private Regimen regimen;
		private RegimenPhase phase;
		private Date iniDate;
		private Date endDate;

		public RegimenInfo(Regimen regimen, RegimenPhase phase, Date iniDate,
				Date endDate) {
			super();
			this.regimen = regimen;
			this.phase = phase;
			this.iniDate = iniDate;
			this.endDate = endDate;
		}
		public Regimen getRegimen() {
			return regimen;
		}
		public RegimenPhase getPhase() {
			return phase;
		}
		public Date getIniDate() {
			return iniDate;
		}
		public Date getEndDate() {
			return endDate;
		}
	}



	/**
	 * @return the regimen
	 */
	public Regimen getRegimen() {
		return regimen;
	}

	/**
	 * @param regimen the regimen to set
	 */
	public void setRegimen(Regimen regimen) {
		this.regimen = regimen;
	}

	/**
	 * @return the preservePeriods
	 */
	public boolean isPreservePeriods() {
		return preservePeriods;
	}

	/**
	 * @param preservePeriods the preservePeriods to set
	 */
	public void setPreservePeriods(boolean preservePeriods) {
		this.preservePeriods = preservePeriods;
	}

}
