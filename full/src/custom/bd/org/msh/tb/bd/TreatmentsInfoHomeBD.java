package org.msh.tb.bd;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.application.App;
import org.msh.tb.bd.entities.enums.PulmonaryTypesBD;
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
@Name("treatmentsInfoHome_bd")
@BypassInterceptors
public class TreatmentsInfoHomeBD extends TreatmentsInfoHome {

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
                "c.daysTreatPlanned, c.classification, c.patientType, p.gender, c.infectionSite, pt.name.name1, c.registrationDate, " +
                "(select ( sum(day1) + sum(day2) + sum(day3) + sum(day4) + sum(day5) + sum(day6) + sum(day7) + sum(day8) + sum(day9) " +
                "+ sum(day10) + sum(day11) + sum(day12) + sum(day13) + sum(day14) + sum(day15) + sum(day16) + sum(day17) + sum(day18) " +
                "+ sum(day19) + sum(day20) + sum(day21) + sum(day22) + sum(day23) + sum(day24) + sum(day25) + sum(day26) + sum(day27) " +
                "+ sum(day28) + sum(day29) + sum(day30) + sum(day31) ) as total " +
                    "from TreatmentMonitoring tm0 " +
                    "where tm0.tbcase.id = c.id " +
                    "and PERIOD_DIFF(CONCAT(tm0.year,LPAD(tm0.month,2,'0')), DATE_FORMAT(tm0.tbcase.treatmentPeriod.iniDate, '%Y%m')) >= 0 " +
                    "and PERIOD_DIFF(CONCAT(tm0.year,LPAD(tm0.month,2,'0')), DATE_FORMAT(tm0.tbcase.treatmentPeriod.endDate, '%Y%m')) <= 0 ) as medicineTakenDays, c.pulmonaryTypesBD " +
                "from TbCaseBD c " +
                "join c.patient p left join c.pulmonaryType pt " +
                "where c.state = " + CaseState.ONTREATMENT.ordinal() +
                " and c.ownerUnit.id = " + unit.getId() +
                "group by c.id, p.name, p.middleName, p.lastName, c.treatmentPeriod, c.daysTreatPlanned, c.classification " + getHQLOrderBy())
                .getResultList();

        Patient p = new Patient();
        Workspace ws = (Workspace)Component.getInstance("defaultWorkspace", true);

        for (Object[] vals: lst) {
            CaseClassification classification = (CaseClassification)vals[6];
            PatientType pt = (PatientType)vals[7];
            Gender gender = (Gender)vals[8];
            CaseGroup grp = findGroup(classification);
            InfectionSite is = (InfectionSite) vals[9];
            PulmonaryTypesBD ptBD = (PulmonaryTypesBD) vals[13];

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
            if(ptBD != null)
                info.setPulmonaryTypesBD(ptBD.name());

            grp.getTreatments().add(info);
        }
    }

}
