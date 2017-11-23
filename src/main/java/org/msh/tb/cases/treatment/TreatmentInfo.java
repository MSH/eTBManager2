package org.msh.tb.cases.treatment;

import org.msh.tb.entities.enums.Gender;
import org.msh.tb.entities.enums.InfectionSite;
import org.msh.tb.entities.enums.PatientType;
import org.msh.utils.date.DateUtils;
import org.msh.utils.date.Period;

import java.util.Date;

/**
 * Hold basic information about a case on treatment
 *
 * @author Ricardo Memoria
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
     *
     * @return
     */
    public Double getPlannedProgress() {
        if ((treatmentPeriod == null) || (treatmentPeriod.isEmpty()))
            return null;

        double days = DateUtils.daysBetween(treatmentPeriod.getIniDate(), new Date());
        int daysPlanned = treatmentPeriod.getDays();
        Double max = (daysPlanned == 0 ? null : days / treatmentPeriod.getDays() * 100);
        if (max == null)
            return null;

        return (max > 100) ? 100 : max;
    }

    public Integer getProgressPoints() {
        Double val = getPlannedProgress();
        if ((val == null) || (val == 0))
            return 0;
        return (val > 100 ? 100 : val.intValue());
    }


    /**
     * Return the percentage progress of the treatment considering the medicine that was actually taken
     *
     * @return
     */
    public Double getTakenMedicineProgress() {
        if ((treatmentPeriod == null) || (treatmentPeriod.isEmpty()))
            return null;

        double days = numDaysPlanned;
        int daysDone = getNumDaysDone();

        if (days == 0 || daysDone == 0)
            return 0.0;

        Double result = daysDone / days * 100;
        if (result == null)
            return null;

        return (result > 100) ? 100 : result;
    }

    public Integer getMedicineIntakePoints() {
        Double val = getTakenMedicineProgress();
        if ((val == null) || (val == 0))
            return 0;
        return (val > 100 ? 100 : val.intValue());
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
