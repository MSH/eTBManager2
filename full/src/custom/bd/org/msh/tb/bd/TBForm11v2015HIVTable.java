package org.msh.tb.bd;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.entities.enums.DiagnosisType;
import org.msh.tb.entities.enums.Gender;
import org.msh.tb.entities.enums.InfectionSite;
import org.msh.tb.indicators.core.Indicator2D;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;


/**
 * Generate indicator TB 11 form (HIV CPT and ART table)
 * @author Mauricio Santos
 *
 */
@Name("tBForm11v2015HIVTable")
public class TBForm11v2015HIVTable extends Indicator2D {

    @In(create=true)
    EntityManager entityManager;
    @In(create=true)
    Map<String, String> messages;

    @Override
    protected String getHQLWhere(){
        String result = super.getHQLWhere();

        result += " and c.patientType is not null ";
        result += " and p.gender is not null ";
        result += " and c.state is not null ";
        result += " and c.classification = " + CaseClassification.TB.ordinal();
        result += " and c.diagnosisType = " + DiagnosisType.CONFIRMED.ordinal();

        result = result + " ";
        return result;
    }

    @Override
    protected void createIndicators() {
        createInterfaceFields();

        List<Object[]> result;

        result = entityManager.createQuery(getQuery("startedARTdate")).getResultList();
        for(Object[] o : result){
            Gender g = (Gender) o[0];
            Long qtd = (Long) o[1];

            if(g.equals(Gender.MALE))
                addValue("ART-M", messages.get("manag.gender.male"), messages.get("manag.tbform112015.allTbCases"), qtd.floatValue());
            else if(g.equals(Gender.FEMALE))
                addValue("ART-F", messages.get("manag.gender.female"), messages.get("manag.tbform112015.allTbCases"), qtd.floatValue());
        }

        result = entityManager.createQuery(getQuery("startedCPTdate")).getResultList();
        for(Object[] o : result){
            Gender g = (Gender) o[0];
            Long qtd = (Long) o[1];

            if(g.equals(Gender.MALE))
                addValue("CPT-M", messages.get("manag.gender.male"), messages.get("manag.tbform112015.allTbCases"), qtd.floatValue());
            else if(g.equals(Gender.FEMALE))
                addValue("CPT-F", messages.get("manag.gender.female"), messages.get("manag.tbform112015.allTbCases"), qtd.floatValue());
        }

    }

    private String getQuery(String dateField){
        String query = "select p.gender, count(distinct p.id) " +
                       "from TbCase c join c.patient p join c.resHIV hiv " +
                        getHQLWhere() +
                       "and hiv."+dateField+" is not null " +
                       "group by p.gender";

        return query;
    }

    private void createInterfaceFields(){
        addValue("ART-M", messages.get("manag.gender.male"), messages.get("manag.tbform112015.allTbCases"), new Float(0));
        addValue("ART-F", messages.get("manag.gender.female"), messages.get("manag.tbform112015.allTbCases"), new Float(0));
        addValue("CPT-M", messages.get("manag.gender.male"), messages.get("manag.tbform112015.allTbCases"), new Float(0));
        addValue("CPT-F", messages.get("manag.gender.female"), messages.get("manag.tbform112015.allTbCases"), new Float(0));
    }
}
