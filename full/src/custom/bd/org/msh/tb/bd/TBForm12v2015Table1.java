package org.msh.tb.bd;

import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.enums.*;


/**
 * Generate indicator TB 11 form (first table - clinically diagnosed pulmonary TB cases)
 * @author Mauricio Santos
 *
 */
@Name("tBForm12v2015Table1")
public class TBForm12v2015Table1 extends TBForm12v2015 {

    /**
     * To guarantee that the conditions for period will be applied on registration date atributte
     */
    public void initialize(){
        this.getIndicatorFilters().setUseIniTreatmentDate(false);
        this.getIndicatorFilters().setUseRegistrationDate(true);
    }

    protected String getHQLWhereForQuery(){
        String result = super.getHQLWhere();

        result += " and (c.patientType in ("+PATIENT_TYPES_ORDINAL_ROWS+") and (c.previouslyTreatedType is null or c.previouslyTreatedType in ("+PATIENT_TYPES_PRE_TREAT_ORDINAL_ROWS+"))) ";
        result += " and p.gender is not null ";
        result += " and c.state is not null ";
        result += " and c.classification = " + CaseClassification.TB.ordinal();
        result += " and c.diagnosisType = " + DiagnosisType.CONFIRMED.ordinal();
        result += " and c.infectionSite = " + InfectionSite.PULMONARY.ordinal();
        result += " and c.caseDefinition = " + CaseDefinition.BACTERIOLOGICALLY_CONFIRMED.ordinal();

        result = result + " ";
        return result;
    }
}
