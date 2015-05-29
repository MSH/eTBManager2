package org.msh.tb.bd;

import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.entities.enums.CaseDefinition;
import org.msh.tb.entities.enums.DiagnosisType;
import org.msh.tb.entities.enums.InfectionSite;


/**
 * Generate indicator TB 11 form (Second table clinically diagnosed pulmonary TB cases)
 * @author Mauricio Santos
 *
 */
@Name("tBForm11v2015Table2")
public class TBForm11v2015Table2 extends TBForm11v2015 {

    protected String getHQLWhereForQuery(){
        String result = super.getHQLWhere();

        result += " and (c.patientType in ("+PATIENT_TYPES_ORDINAL_ROWS+") and (c.previouslyTreatedType is null or c.previouslyTreatedType in ("+PATIENT_TYPES_PRE_TREAT_ORDINAL_ROWS+"))) ";
        result += " and p.gender is not null ";
        result += " and c.state is not null ";
        result += " and c.classification = " + CaseClassification.TB.ordinal();
        result += " and c.diagnosisType = " + DiagnosisType.CONFIRMED.ordinal();
        result += " and c.infectionSite = " + InfectionSite.PULMONARY.ordinal();
        result += " and c.caseDefinition = " + CaseDefinition.CLINICALLY_DIAGNOSED.ordinal();

        result = result + " ";
        return result;
    }
}
