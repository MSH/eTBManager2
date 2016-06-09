package org.msh.tb.bd;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.tb.entities.enums.CaseDefinition;
import org.msh.tb.entities.enums.Gender;
import org.msh.tb.entities.enums.InfectionSite;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.indicators.core.Indicator2D;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;

@Name("TBForm8v2016Block3")
public class TBForm8v2016Block3 extends Indicator2D {

    @In(create=true)
    EntityManager entityManager;

    private final String UNDER_15_ROW = "< 15 years";
    private final String ABOVE_AND_15 = "15 years and above";

    private final String columns[] = {
        Gender.MALE.name(),
        Gender.FEMALE.name(),
        "TOTAL",
        InfectionSite.PULMONARY.name() + CaseDefinition.BACTERIOLOGICALLY_CONFIRMED.name() + PatientType.HIV_INFECTED_WITHSS_UNKNOWN_HISTORY.name(),
        InfectionSite.PULMONARY.name() + CaseDefinition.BACTERIOLOGICALLY_CONFIRMED.name() + PatientType.HIV_INFECTED_WITHSS_NEW.name(),
        InfectionSite.PULMONARY.name() + CaseDefinition.BACTERIOLOGICALLY_CONFIRMED.name() + PatientType.HIV_INFECTED_WITHSS_PREVIOUSLY_TREATED.name(),
        InfectionSite.PULMONARY.name() + CaseDefinition.CLINICALLY_DIAGNOSED.name() + PatientType.HIV_INFECTED_WITHSS_UNKNOWN_HISTORY.name(),
        InfectionSite.PULMONARY.name() + CaseDefinition.CLINICALLY_DIAGNOSED.name() + PatientType.HIV_INFECTED_WITHSS_NEW.name(),
        InfectionSite.PULMONARY.name() + CaseDefinition.CLINICALLY_DIAGNOSED.name() + PatientType.HIV_INFECTED_WITHSS_PREVIOUSLY_TREATED.name(),
        InfectionSite.EXTRAPULMONARY.name() + CaseDefinition.BACTERIOLOGICALLY_CONFIRMED.name() + PatientType.HIV_INFECTED_WITHSS_UNKNOWN_HISTORY.name(),
        InfectionSite.EXTRAPULMONARY.name() + CaseDefinition.BACTERIOLOGICALLY_CONFIRMED.name() + PatientType.HIV_INFECTED_WITHSS_NEW.name(),
        InfectionSite.EXTRAPULMONARY.name() + CaseDefinition.BACTERIOLOGICALLY_CONFIRMED.name() + PatientType.HIV_INFECTED_WITHSS_PREVIOUSLY_TREATED.name(),
        InfectionSite.EXTRAPULMONARY.name() + CaseDefinition.CLINICALLY_DIAGNOSED.name() + PatientType.HIV_INFECTED_WITHSS_UNKNOWN_HISTORY.name(),
        InfectionSite.EXTRAPULMONARY.name() + CaseDefinition.CLINICALLY_DIAGNOSED.name() + PatientType.HIV_INFECTED_WITHSS_NEW.name(),
        InfectionSite.EXTRAPULMONARY.name() + CaseDefinition.CLINICALLY_DIAGNOSED.name() + PatientType.HIV_INFECTED_WITHSS_PREVIOUSLY_TREATED.name(),
        "ART",
        "CPT"
    };

    /**
     * To guarantee that the conditions for period will be applied on registration date atributte
     */
    public void initialize(){
        this.getIndicatorFilters().setUseIniTreatmentDate(false);
        this.getIndicatorFilters().setUseRegistrationDate(true);
    }

    protected String getHQLWhere(){
        // HIV registration groups pulmonary or extrapulmonary
        return super.getHQLWhere() + " and c.patientType in (61, 62, 63) and c.infectionSite in (0,1) ";
    }

	@Override
	protected void createIndicators() {
        initializeInterface();

        List<Object[]> result = null;

        //Generate Male/Female/Total numbers
        result = entityManager.createQuery("select p.gender, c.age, count(*) from TbCase c join c.patient p " + getHQLWhere() + " group by p.gender, c.age ")
                .getResultList();
        for(Object[] o : result){
            Gender g = (Gender) o[0];
            Integer age = (Integer) o[1];
            Long qtd = (Long) o[2];

            addValue(g.name(), (age < 15 ? UNDER_15_ROW : ABOVE_AND_15), qtd.floatValue());
            addValue("TOTAL", (age < 15 ? UNDER_15_ROW : ABOVE_AND_15), qtd.floatValue());
        }

        //Generate Registration Group numbers
        result = entityManager.createQuery("select c.infectionSite, c.caseDefinition, c.patientType, c.age, count(*) from TbCase c join c.patient p "
                + getHQLWhere() + " group by c.infectionSite, c.caseDefinition, c.patientType, c.age ")
                .getResultList();
        for(Object[] o : result){
            InfectionSite is = (InfectionSite) o[0];
            CaseDefinition cd = (CaseDefinition) o[1];
            PatientType pt = (PatientType) o[2];
            Integer age = (Integer) o[3];
            Long qtd = (Long) o[4];

            addValue(is.name() + cd.name() + pt.name(), (age < 15 ? UNDER_15_ROW : ABOVE_AND_15), qtd.floatValue());
        }

        //Generate ART numbers under 15
        result = entityManager.createQuery("select c.age, count(*) from ExamHIV e join e.tbcase c join c.patient p "
                + getHQLWhere() + " and e.startedARTdate <= :endPeriodDate"
                + " group by c.age ")
                .setParameter("endPeriodDate", getIndicatorFilters().getEndDate())
                .getResultList();
        for(Object[] o : result){
            Integer age = (Integer) o[3];
            Long qtd = (Long) o[4];

            addValue("ART", (age < 15 ? UNDER_15_ROW : ABOVE_AND_15), qtd.floatValue());
        }

        //Generate CPT numbers under 15
        result = entityManager.createQuery("select c.age, count(*) from ExamHIV e join e.tbcase c join c.patient p "
                + getHQLWhere() + " and e.startedCPTdate <= :endPeriodDate"
                + " group by c.age ")
                .setParameter("endPeriodDate", getIndicatorFilters().getEndDate())
                .getResultList();
        for(Object[] o : result){
            Integer age = (Integer) o[3];
            Long qtd = (Long) o[4];

            addValue("CPT", (age < 15 ? UNDER_15_ROW : ABOVE_AND_15), qtd.floatValue());
        }

	}


    private void initializeInterface(){
        String row = UNDER_15_ROW;

        while(row != null){
            for(String col : columns){
                addValue(col, row, new Float(0));
            }

            row = (row.equals(UNDER_15_ROW) ? ABOVE_AND_15 : null);
        }
    }
}
