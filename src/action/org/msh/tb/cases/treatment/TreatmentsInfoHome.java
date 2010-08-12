package org.msh.tb.cases.treatment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.Patient;
import org.msh.mdrtb.entities.Tbunit;
import org.msh.mdrtb.entities.Workspace;
import org.msh.mdrtb.entities.enums.CaseClassification;
import org.msh.mdrtb.entities.enums.CaseState;
import org.msh.mdrtb.entities.enums.PatientType;

/**
 * Return information about cases on treatment
 * @author Ricardo Memoria
 *
 */
@Name("treatmentsInfoHome")
public class TreatmentsInfoHome {

	@In EntityManager entityManager;

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
	 * Create the list of treatments for the health unit tbunit
	 */
	private void createTreatments() {
		if (tbunit == null)
			return;
		
		groups = new ArrayList<CaseGroup>();
		
		List<Object[]> lst = entityManager.createQuery("select c.id, p.name, p.middleName, p.lastName, c.treatmentPeriod.iniDate, " +
				"c.daysTreatPlanned, sum(disp.totalDays), c.classification, c.patientType " +
				"from TbCase c " + 
				"join c.patient p left join c.dispensing disp " + 
				"where c.state = " + CaseState.ONTREATMENT.ordinal() +
				" and exists (select aux.id from TreatmentHealthUnit aux where aux.tbunit.id = " + tbunit.getId().toString() + 
				" and aux.tbCase.id = c.id and aux.period.endDate = c.treatmentPeriod.endDate) " +
				"group by c.id, p.name, p.middleName, p.lastName, c.treatmentPeriod.iniDate, c.daysTreatPlanned, c.classification " +
				"order by p.name, p.lastName, p.middleName")
			.getResultList();

		Patient p = new Patient();
		Workspace ws = (Workspace)Component.getInstance("defaultWorkspace", true);

		for (Object[] vals: lst) {
			CaseClassification classification = (CaseClassification)vals[7];
			PatientType pt = (PatientType)vals[8];
			CaseGroup grp = findGroup(classification);
			
			TreatmentInfo info = new TreatmentInfo();
			info.setCaseId((Integer)vals[0]);
			
			p.setName((String)vals[1]);
			p.setMiddleName((String)vals[2]);
			p.setLastName((String)vals[3]);
			info.setPatientName(p.compoundName(ws));
			info.setPatientType(pt);
			info.setIniTreatmentDate((Date)vals[4]);
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
		private String patientName;
		private Date iniTreatmentDate;
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
		 * @return the iniTreatmentDate
		 */
		public Date getIniTreatmentDate() {
			return iniTreatmentDate;
		}
		/**
		 * @param iniTreatmentDate the iniTreatmentDate to set
		 */
		public void setIniTreatmentDate(Date iniTreatmentDate) {
			this.iniTreatmentDate = iniTreatmentDate;
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
		public Double getProgress() {
			return (numDaysPlanned == 0? null: (double)numDaysDone / numDaysPlanned * 100);
		}
		
		public Integer getProgressPoints() {
			Double val = getProgress();
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
		return tbunit;
	}

	/**
	 * @param tbunit the tbunit to set
	 */
	public void setTbunit(Tbunit tbunit) {
		this.tbunit = tbunit;
	}
	

	public Integer getTbunitId() {
		return (tbunit != null? tbunit.getId(): null);
	}
	
	public void setTbunitId(Integer id) {
		if (id == null)
			 tbunit = null;
		else tbunit = entityManager.find(Tbunit.class, id);
	}
}
