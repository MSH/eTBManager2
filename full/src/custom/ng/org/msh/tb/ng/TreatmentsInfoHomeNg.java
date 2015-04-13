package org.msh.tb.ng;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.application.App;
import org.msh.tb.cases.treatment.TreatmentInfo;
import org.msh.tb.cases.treatment.TreatmentsInfoHome;
import org.msh.tb.entities.Patient;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.Workspace;
import org.msh.tb.entities.enums.*;
import org.msh.utils.date.Period;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Return information about cases on treatment
 * @author Mauricio Santos
 *
 */
@Name("treatmentsInfoHomeNg")
@BypassInterceptors
public class TreatmentsInfoHomeNg extends TreatmentsInfoHome {

    private static final String HQL_CONFIRMED = "select c.id, p.name, p.middleName, p.lastName, c.treatmentPeriod, " +
            "c.daysTreatPlanned, c.classification, c.patientType, p.gender, c.infectionSite, pt.name.name1, c.registrationDate, " +
            "(select ( sum(day1) + sum(day2) + sum(day3) + sum(day4) + sum(day5) + sum(day6) + sum(day7) + sum(day8) + sum(day9) " +
            "+ sum(day10) + sum(day11) + sum(day12) + sum(day13) + sum(day14) + sum(day15) + sum(day16) + sum(day17) + sum(day18) " +
            "+ sum(day19) + sum(day20) + sum(day21) + sum(day22) + sum(day23) + sum(day24) + sum(day25) + sum(day26) + sum(day27) " +
            "+ sum(day28) + sum(day29) + sum(day30) + sum(day31) ) as total " +
            "from TreatmentMonitoring tm0 where tm0.tbcase.id = c.id) as medicineTakenDays " +
            "from TbCase c " +
            "join c.patient p left join c.pulmonaryType pt " +
            "where c.state = " + CaseState.ONTREATMENT.ordinal() + " and c.diagnosisType = " + DiagnosisType.CONFIRMED.ordinal() +
            " and c.ownerUnit.id = :unitId " +
            "group by c.id, p.name, p.middleName, p.lastName, c.treatmentPeriod, c.daysTreatPlanned, c.classification ";

    private static final String HQL_SUSPECT = "select c.id, p.name, p.middleName, p.lastName, c.treatmentPeriod, " +
            "c.daysTreatPlanned, c.classification, c.patientType, p.gender, c.infectionSite, pt.name.name1, c.registrationDate " +
            "from TbCase c " +
            "join c.patient p left join c.pulmonaryType pt " +
            "where c.state = " + CaseState.ONTREATMENT.ordinal() + " and c.diagnosisType = " + DiagnosisType.SUSPECT.ordinal() +
            " and c.ownerUnit.id = :unitId " +
            "group by c.id, p.name, p.middleName, p.lastName, c.treatmentPeriod, c.daysTreatPlanned, c.classification ";

    protected CaseGroup suspectGroup;

    public List<CaseGroup> getConfirmedGroups() {
        if (super.groups == null || groups.size()==0)
            super.createTreatments(HQL_CONFIRMED);
        return super.groups;
    }

    public CaseGroup getSuspectGroup() {
        if (suspectGroup == null)
            createSuspectTreatments();
        return suspectGroup;
    }

    public CaseGroup getConfirmedTBCasesGroup() {
        if (getConfirmedGroups() != null){
            for (CaseGroup cg : getConfirmedGroups()) {
                if (cg.getClassification() == CaseClassification.TB)
                    return cg;
            }
        }
        return null;
    }

    public CaseGroup getConfirmedDRTBCasesGroup(){
        if (getConfirmedGroups() != null){
            for (CaseGroup cg : getConfirmedGroups()) {
                if (cg.getClassification() == CaseClassification.DRTB)
                    return cg;
            }
        }
        return null;
    }

    /**
     * Create the list of treatments for the health unit TB unit
     */
    protected void createSuspectTreatments() {
        // get the unit id selected
        Tbunit unit = getTbunit();
        if (unit == null)
            return;

        groups = new ArrayList<CaseGroup>();

        List<Object[]> lst = App.getEntityManager().createQuery(HQL_SUSPECT + getHQLOrderBy())
                .setParameter("unitId", unit.getId())
                .getResultList();

        Patient p = new Patient();
        Workspace ws = (Workspace) Component.getInstance("defaultWorkspace", true);
        suspectGroup = new CaseGroup();

        for (Object[] vals: lst) {
            //CaseClassification classification = (CaseClassification)vals[6];
            PatientType pt = (PatientType)vals[7];
            Gender gender = (Gender)vals[8];
            CaseGroup grp = suspectGroup;
            InfectionSite is = (InfectionSite) vals[9];

            TreatmentInfo info = new TreatmentInfoNg();
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

            grp.getTreatments().add(info);
        }

        loadCultureResults();
        loadMicroscopyResults();
        loadXpertResults();
    }


    /**
     * Simple way to return a list of exam results from the suspect cases
     * @param fields the list of fields to be included in the HQL sentence
     * @param entity the name of the entity to be included in the 'from' clause of the HQL sentence
     * @return list of objects, where the last item is the case number
     */
    protected List<Object[]> getExamResults(String fields, String entity) {
        CaseGroup grp = getSuspectGroup();
        if (grp.getTreatments().size() == 0) {
            return null;
        }
        String s = "";
        for (TreatmentInfo info: grp.getTreatments()) {
            TreatmentInfoNg item = (TreatmentInfoNg)info;
            if (!s.isEmpty()) {
                s += ",";
            }
            s += info.getCaseId();
        }
        String hql = "select " + fields + ", tbcase.id " +
                "from " + entity + " where tbcase.id in (" + s + ") " +
                "order by dateCollected";

        return App.getEntityManager().createQuery(hql).getResultList();
    }

    /**
     * Load information about xpert results of suspect cases
     */
    protected void loadXpertResults() {
        List<Object[]> lst = getExamResults("result, rifResult", "ExamXpert");

        if (lst == null) {
            return;
        }

        for (Object[] vals: lst) {
            XpertResult res = (XpertResult)vals[0];
            XpertRifResult rif = (XpertRifResult)vals[1];
            Integer caseId = (Integer)vals[2];

            TreatmentInfoNg info = (TreatmentInfoNg)getSuspectGroup().findByCaseId(caseId);
            if (info != null) {
                info.setXpertResult(res);
                info.setXpertRifResult(rif);
            }
        }
    }

    /**
     * Load information about culture result of suspect cases
     */
    protected void loadCultureResults() {
        List<Object[]> lst = getExamResults("result", "ExamCulture");

        if (lst == null) {
            return;
        }

        for (Object[] vals: lst) {
            CultureResult res = (CultureResult)vals[0];
            Integer caseId = (Integer)vals[1];

            TreatmentInfoNg info = (TreatmentInfoNg)getSuspectGroup().findByCaseId(caseId);
            if (info != null) {
                info.setCultureResult(res);
            }
        }
    }

    /**
     * Load information about microscopy result of suspect cases
     */
    protected void loadMicroscopyResults() {
        List<Object[]> lst = getExamResults("result", "ExamMicroscopy");

        if (lst == null) {
            return;
        }

        for (Object[] vals: lst) {
            MicroscopyResult res = (MicroscopyResult)vals[0];
            Integer caseId = (Integer)vals[1];

            TreatmentInfoNg info = (TreatmentInfoNg)getSuspectGroup().findByCaseId(caseId);
            if (info != null) {
                info.setMicroscopyResult(res);
            }
        }
    }


    @Override
    public Tbunit getTbunit() {
        return ((TreatmentsInfoHome)App.getComponentFromDefaultWorkspaceOrGeneric("treatmentsInfoHome")).getTbunit();
    }
}
