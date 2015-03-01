package org.msh.tb.cases.treatment;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.application.App;
import org.msh.tb.entities.Patient;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.Workspace;
import org.msh.tb.entities.enums.*;
import org.msh.tb.login.SessionData;
import org.msh.tb.login.UserSession;
import org.msh.utils.date.DateUtils;
import org.msh.utils.date.Period;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Return information about cases on treatment
 * @author Ricardo Memoria
 *
 */
@Name("treatmentsInfoHome")
@BypassInterceptors
public class TreatmentsInfoHome {

    /*HQl was is a paramneter to make easier customizations for others workspaces*/
    private static final String HQL = "select c.id, p.name, p.middleName, p.lastName, c.treatmentPeriod, " +
            "c.daysTreatPlanned, c.classification, c.patientType, p.gender, c.infectionSite, pt.name.name1, c.registrationDate, " +
            "(select ( sum(day1) + sum(day2) + sum(day3) + sum(day4) + sum(day5) + sum(day6) + sum(day7) + sum(day8) + sum(day9) " +
            "+ sum(day10) + sum(day11) + sum(day12) + sum(day13) + sum(day14) + sum(day15) + sum(day16) + sum(day17) + sum(day18) " +
            "+ sum(day19) + sum(day20) + sum(day21) + sum(day22) + sum(day23) + sum(day24) + sum(day25) + sum(day26) + sum(day27) " +
            "+ sum(day28) + sum(day29) + sum(day30) + sum(day31) ) as total " +
            "from TreatmentMonitoring tm0 where tm0.tbcase.id = c.id) as medicineTakenDays " +
            "from TbCase c " +
            "join c.patient p left join c.pulmonaryType pt " +
            "where c.state = " + CaseState.ONTREATMENT.ordinal() +
            " and c.ownerUnit.id = :unitId " +
            "group by c.id, p.name, p.middleName, p.lastName, c.treatmentPeriod, c.daysTreatPlanned, c.classification ";

	protected List<CaseGroup> groups;
    protected Tbunit tbunit;

    protected Integer orderby;

	/**
	 * Return the list of treatments for the health unit tbunit
	 * @return
	 */
	public List<CaseGroup> getGroups() {
		if (groups == null)
			createTreatments(HQL);
		return groups;
	}


	/**
	 * Create the list of treatments for the health unit TB unit
	 */
    protected void createTreatments(String hql) {
		// get the unit id selected
		Tbunit unit = getTbunit();
		if (unit == null)
			return;

		groups = new ArrayList<CaseGroup>();

		List<Object[]> lst = App.getEntityManager().createQuery(hql + getHQLOrderBy())
                                                    .setParameter("unitId", unit.getId())
			                                        .getResultList();

		Patient p = new Patient();
		Workspace ws = (Workspace)Component.getInstance("defaultWorkspace", true);

		for (Object[] vals: lst) {
			CaseClassification classification = (CaseClassification)vals[6];
			PatientType pt = (PatientType)vals[7];
			Gender gender = (Gender)vals[8];
			CaseGroup grp = findGroup(classification);
            InfectionSite is = (InfectionSite) vals[9];
			
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
			info.setInfectionSite(is);
            info.setPulmonaryType((String) vals[10]);
            info.setRegistrationDate((Date) vals[11]);
            if(vals[12] != null)
                info.setNumDaysDone(((Long) vals[12]).intValue());
            else
                info.setNumDaysDone(0);

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
        private InfectionSite infectionSite;
		private int numDaysPlanned;
		private int numDaysDone;
		private PatientType patientType;
        private String pulmonaryType;
        private Date registrationDate;
        private String pulmonaryTypesBD;
		
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

        public String getPulmonaryTypesBD() {
            return pulmonaryTypesBD;
        }

        public void setPulmonaryTypesBD(String pulmonaryTypesBD) {
            this.pulmonaryTypesBD = pulmonaryTypesBD;
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
         * Return the percentage progress of the treatment considering the medicine that was actually taken
         * @return
         */
        public Double getTakenMedicineProgress() {
            if ((treatmentPeriod == null) || (treatmentPeriod.isEmpty()))
                return null;

            double days = numDaysPlanned;
            int daysDone = getNumDaysDone();

            if(days == 0 || daysDone == 0)
                return 0.0;

            Double result = daysDone / days * 100;
            if (result == null)
                return null;

            return (result > 100)? 100: result;
        }

        public Integer getMedicineIntakePoints() {
            Double val = getTakenMedicineProgress();
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

        public InfectionSite getInfectionSite() {
            return infectionSite;
        }

        public void setInfectionSite(InfectionSite infectionSite) {
            this.infectionSite = infectionSite;
        }

        public String getPulmonaryType() {
            return pulmonaryType;
        }

        public void setPulmonaryType(String pulmonaryType) {
            this.pulmonaryType = pulmonaryType;
        }

        public Date getRegistrationDate() {
            return registrationDate;
        }

        public void setRegistrationDate(Date registrationDate) {
            this.registrationDate = registrationDate;
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

    protected String getHQLOrderBy(){
        String hql = " order by ";

        Boolean inverseOrderBy = (Boolean) SessionData.instance().getValue("treatmentsInfoHome_inverseOrderBy");
        if(inverseOrderBy == null)
            inverseOrderBy = new Boolean(false);
        if(this.orderby == null)
            this.orderby = new Integer(0);

        switch (orderby.intValue()){
            case 0:
                hql += " c.classification, upper(p.lastName), upper(p.name), upper(p.middleName) ";
                break;
            case 1:
                if(inverseOrderBy.booleanValue())
                    hql += " upper(p.lastName) desc, upper(p.name) desc, upper(p.middleName) desc ";
                else
                    hql += " upper(p.lastName), upper(p.name), upper(p.middleName) ";
                break;
            case 2:
                hql += " c.patientType ";
                break;
            case 3:
                hql += " c.infectionSite ";
                break;
            case 4:
                hql += " c.registrationDate ";
                break;
            case 5:
                hql += " c.treatmentPeriod.iniDate ";
                break;
            /*case 6:
                hql += " c.classification, p.name, p.lastName, p.middleName ";
                break;
            case 7:
                hql += " c.classification, p.name, p.lastName, p.middleName ";
                break;*/
        }


        if((orderby.intValue()  != 0 && orderby.intValue()  != 1) && inverseOrderBy.booleanValue())
            hql += " desc ";

        return hql;
    }

    public Integer getOrderby() {
        return orderby;
    }

    public void setOrderby(Integer orderby) {
        if(SessionData.instance().getValue("treatmentsInfoHome_inverseOrderBy") == null)
            SessionData.instance().setValue("treatmentsInfoHome_inverseOrderBy", new Boolean(false));

        if(SessionData.instance().getValue("treatmentsInfoHome_orderBy") == null)
            SessionData.instance().setValue("treatmentsInfoHome_orderBy", orderby);

        /*Check if the user clicked twice on the same link*/
        SessionData.instance().setValue("treatmentsInfoHome_orderBy", orderby);
        this.orderby = (Integer) SessionData.instance().getValue("treatmentsInfoHome_orderBy");
        Boolean tempInverse = (Boolean) SessionData.instance().getValue("treatmentsInfoHome_inverseOrderBy");

        if (orderby.equals(this.orderby)) {
            tempInverse = new Boolean(!tempInverse.booleanValue());
            SessionData.instance().setValue("treatmentsInfoHome_inverseOrderBy", tempInverse);
        }else {
            SessionData.instance().setValue("treatmentsInfoHome_inverseOrderBy", new Boolean(false));
        }
    }
}
