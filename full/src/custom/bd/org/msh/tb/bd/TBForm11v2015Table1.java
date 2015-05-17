package org.msh.tb.bd;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.enums.*;
import org.msh.tb.indicators.core.Indicator2D;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;


/**
 * Generate indicator TB 11 form (first table)
 * @author Mauricio Santos
 *
 */
@Name("tBForm11v2015Table1")
public class TBForm11v2015Table1 extends TBForm11v2015 {

    protected String getHQLWhereForQuery(){
        String result = super.getHQLWhere();

        result += " and (c.patientType in ("+PATIENT_TYPES_ORDINAL_ROWS+") or c.previouslyTreatedType in ("+PATIENT_TYPES_PRE_TREAT_ORDINAL_ROWS+")) ";
        result += " and p.gender is not null ";
        result += " and c.state is not null ";
        result += " and c.classification = " + CaseClassification.TB.ordinal();
        result += " and c.infectionSite = " + InfectionSite.PULMONARY.ordinal();
        result += " and c.caseDefinition = " + CaseDefinition.BACTERIOLOGICALLY_CONFIRMED.ordinal();

        result = result + " ";
        return result;
    }
}
