package org.msh.tb.cases.treatment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.application.App;
import org.msh.tb.entities.Patient;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.Workspace;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.Gender;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.entities.enums.UserView;
import org.msh.tb.login.UserSession;
import org.msh.utils.date.DateUtils;
import org.msh.utils.date.Period;

/**
 * Return information about cases on treatment
 * @author Ricardo Memoria
 *
 */
@Name("treatmentsInfoHome")
@BypassInterceptors
public class TreatmentsInfoHome {

	private List<CaseGroup> groups;
	
	private Tbunit tbunit;

	
	/**
	 * Return the list of treatments for the health unit tbunit
	 * @return
	 */
	public List<CaseGroup> getGroups() {
		if (groups == null)
			createTreatments();
		return groups;
	}


	/**
	 * Create the list of treatments for the health unit TB unit
	 */
	private void createTreatments() {
		// get the unit id selected 
		Tbunit unit = getTbunit();
		if (unit == null)
			return;
		
		groups = new ArrayList<CaseGroup>();

		List<Object[]> lst = App.getEntityManager().createQuery("select c.id, p.name, p.middleName, p.lastName, c.treatmentPeriod, " +
				"c.daysTreatPlanned, sum(disp.totalDays), c.classification, c.patientType, p.gender " +
				"from TbCase c " + 
				"join c.patient p left join c.dispensing disp " + 
				"where c.state = " + CaseState.ONTREATMENT.ordinal() +
				" and c.ownerUnit.id = " + unit.getId() + 
/*				" and exists (select aux.id from TreatmentHealthUnit aux where aux.tbunit.id = " + unitid + 
				" and aux.tbcase.id = c.id and aux.period.endDate = c.treatmentPeriod.endDate) " +
*/				"group by c.id, p.name, p.middleName, p.lastName, c.treatmentPeriod, c.daysTreatPlanned, c.classification " +
				"order by p.name, p.lastName, p.middleName")
			.getResultList();

		Patient p = new Patient();
		Workspace ws = (Workspace)Component.getInstance("defaultWorkspace", true);

		for (Object[] vals: lst) {
			CaseClassification classification = (CaseClassification)vals[7];
			PatientType pt = (PatientType)vals[8];
			Gender gender = (Gender)vals[9];
			CaseGroup grp = findGroup(classification);
			
			TreatmentInfo info = new TreatmentInfo();
			info.setCaseId((Integer)vals[0]);
			
			p.setName((String)vals[1]);
			p.setMiddleName((String)vals[2]);
			p.setLastName((String)vals[3]);
			p.setGender(gender);
			info.setPatientName(p.compoundName(ws));
			info.setPatientType(pt);
			info.setTreatmentPeriod((Period)vals[4]);
			if (vals[5] != null)
				info.setNumDaysPlanned((Integer)vals[5]);
			if (vals[6] != null)
				info.setNumDaysDone(((Long)vals[6]).intValue());

			grp.getTreatments().add(info);
		}
	}

	
	protected CaseGroup findGroup(CaseClassification classification) {
		for (CaseGroup grp: groups) {
			if (grp.getClassification() == classification) {
				return grp;
			}
		}
		
		CaseGroup grp = new CaseGroup();
		grp.setClassification(classification);
		groups.add(grp);
		return grp;
	}



	/**
	 * Hold basic information about a case on treatment
	 * @author Ricardo Memoria
	 *
	 */
	public class TreatmentInfo {
		private int caseId;
		private Gender gender;
		private String patientName;
		private Period treatmentPeriod;
		private int numDaysPlanned;
		private int numDaysDone;
		private PatientType patientType;
		
		/**
		 * @return the caseId
		 */
		public int getCaseId() {
			return caseId;
		}
		/**
		 * @param caseId the caseId to set
		 */
		public void setCaseId(int caseId) {
			this.caseId = caseId;
		}
		/**
		 * @return the patientName
		 */
		public String getPatientName() {
			return patientName;
		}
		/**
		 * @param patientName the patientName to set
		 */
		public void setPatientName(String patientName) {
			this.patientName = patientName;
		}
		/**
		 * @return the numDaysPlanned
		 */
		public int getNumDaysPlanned() {
			return numDaysPlanned;
		}
		/**
		 * @param numDaysPlanned the numDaysPlanned to set
		 */
		public void setNumDaysPlanned(int numDaysPlanned) {
			this.numDaysPlanned = numDaysPlanned;
		}
		/**
		 * @return the numDaysDone
		 */
		public int getNumDaysDone() {
			return numDaysDone;
		}
		/**
		 * @param numDaysDone the numDaysDone to set
		 */
		public void setNumDaysDone(int numDaysDone) {
			this.numDaysDone = numDaysDone;
		}
		
		/**
		 * Return the percentage progress of the treatment
		 * @return
		 */
		public Double getPlannedProgress() {
			if ((treatmentPeriod == null) || (treatmentPeriod.isEmpty()))
				return null;

			double days = DateUtils.daysBetween(treatmentPeriod.getIniDate(), new Date());
			int daysPlanned = treatmentPeriod.getDays();
			Double max = (daysPlanned == 0? null: days / treatmentPeriod.getDays() * 100);
			if (max == null)
				return null;
			
			return (max > 100)? 100: max;
		}
		
		public Integer getProgressPoints() {
			Double val = getPlannedProgress();
			if ((val == null) || (val == 0))
				return 0;
			return (val > 100? 100: val.intValue());
		}
		/**
		 * @return the patientType
		 */
		public PatientType getPatientType() {
			return patientType;
		}
		/**
		 * @param patientType the patientType to set
		 */
		public void setPatientType(PatientType patientType) {
			this.patientType = patientType;
		}
		/**
		 * @return the gender
		 */
		public Gender getGender() {
			return gender;
		}
		/**
		 * @param gender the gender to set
		 */
		public void setGender(Gender gender) {
			this.gender = gender;
		}
		/**
		 * @return the treatmentPeriod
		 */
		public Period getTreatmentPeriod() {
			return treatmentPeriod;
		}
		/**
		 * @param treatmentPeriod the treatmentPeriod to set
		 */
		public void setTreatmentPeriod(Period treatmentPeriod) {
			this.treatmentPeriod = treatmentPeriod;
		}
	}



	/**
	 * Keep information by case classification, i.e, MDR-TB and TB cases
	 * @author Ricardo Memoria
	 *
	 */
	public class CaseGroup {
		private CaseClassification classification;
		private List<TreatmentInfo> treatments = new ArrayList<TreatmentInfo>();
		/**
		 * @return the classification
		 */
		public CaseClassification getClassification() {
			return classification;
		}
		/**
		 * @param classification the classification to set
		 */
		public void setClassification(CaseClassification classification) {
			this.classification = classification;
		}
		/**
		 * @return the treatments
		 */
		public List<TreatmentInfo> getTreatments() {
			return treatments;
		}
		/**
		 * @param treatments the treatments to set
		 */
		public void setTreatments(List<TreatmentInfo> treatments) {
			this.treatments = treatments;
		}
	}


	/**
	 * @return the tbunit
	 */
	public Tbunit getTbunit() {
		UserWorkspace uw = UserSession.getUserWorkspace();
		if (uw.getView() == UserView.TBUNIT)
			 return uw.getTbunit();
		else return tbunit;
	}


	/**
	 * @param tbunit the tbunit to set
	 */
	public void setTbunit(Tbunit tbunit) {
		this.tbunit = tbunit;
	}
	
	
	public Integer getTbunitId() {
		Tbunit unit = getTbunit();
		return unit != null? unit.getId(): null;
	}
	
	public void setTbunitId(Integer id) {
		if (id == null)
			 tbunit = null;
		else tbunit = App.getEntityManager().find(Tbunit.class, id);
	}
	
	
}
