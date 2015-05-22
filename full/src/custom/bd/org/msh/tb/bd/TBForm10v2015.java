package org.msh.tb.bd;

import org.jboss.seam.annotations.In;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.indicators.core.Indicator2D;

import javax.persistence.EntityManager;
import java.util.Map;

/**
 * @author MSANTOS
 * Parent abstract class for each class that genertes each bolck information of TB 10 v2015
 */
public abstract class TBForm10v2015 extends Indicator2D{

	@In(create=true) protected EntityManager entityManager;
    @In(create=true) protected Map<String, String> messages;

    /**
     * Those are the tbcase.patientTypes and tbcase.previouslyTreatedType counted on TB 10 Block 1, 2 and 5
     */
    protected static final PatientType patientTypesReport[] = {
            PatientType.UNKNOWN_PREVIOUS_TB_TREAT,
            PatientType.NEW,
            PatientType.RELAPSE,
            PatientType.TREATMENT_AFTER_FAILURE,
            PatientType.TREATMENT_AFTER_LOSS_FOLLOW_UP,
            PatientType.OTHER,
    };

    /**
     * This HQL where clause is used in many queries in block 1, so it was encapsulated on a method.
     * @return HQL where clause for block 1
     */
    protected String getHQLWhereBlock1(){
        // cases has to be Tb and confirmed and has to be one of type of patients considered on TB 10
        return super.getHQLWhere() + getWhereClausePatientTypes() + " and ar.workspace.id = " + getWorkspace().getId().toString() +
                " and (c.age >= ar.iniAge and c.age <= ar.endAge) and c.classification = 0 and c.diagnosisType = 1 and c.patientType is not null and p.gender is not null ";
    }

    /**
     * This HQL where clause is used in many queries in blocks 3 and 4, so it was encapsulated on a method.
     * @return HQL where clause for block 3 and 4
     */
    protected String getHQLWhereBlock_3_4(){
        // cases has to be Tb and suspect - do not consider filters and has to be one of type of patients considered on TB 10
        return " where c.classification = 0 and c.diagnosisType = 0 and p.workspace.id = " + getWorkspace().getId();
    }

    /**
     * This HQL where clause is used in many queries in blocks 1 and 2, so it was encapsulated on a method.
     * @return HQL where clause for block 2 and 5
     */
    protected String getHQLWhereBlock_2_5(){
        // cases has to be Tb and confirmed and has to be one of type of patients considered on TB 10
        return super.getHQLWhere() + " and c.patientType is not null and p.gender is not null and c.classification = 0 and c.diagnosisType = 1 " + getWhereClausePatientTypes();
    }

    /**
     * This HQL where conditions is used in many queries in block 3 and 4, so it was encapsulated on a method.
     * @param examEntity identify the entity that is being checked on the conditions
     * @param alias identify the alias used on the join clause for the examEntity
     * @param dateParameter identify witch date parameter from the examEntity will be tested
     * @return where conditions selecting just the last exam inside the period selected from examEntity for each case 
     */
    protected String getHQLWhereExamClause(String examEntity, String alias, String dateParameter){
        return " and "+alias+".id = (select max ("+alias+"2.id) from "+examEntity+" "+alias+"2 where "+alias+"2.tbcase.id = "+alias+".tbcase.id and "+alias+"2."+dateParameter+" = " +
                "(select max("+alias+"3."+dateParameter+") from "+examEntity+" "+alias+"3 where "+alias+"3.tbcase.id = "+alias+"2.tbcase.id " +
                "and ("+alias+"3."+dateParameter+" between #{indicatorFilters.iniDate} and #{indicatorFilters.endDate} ) ) ) ";
    }

    /**
     * This conditions are used in many queries so it was encapsulated on this method
     * @return the where conditions to guarantee that on cases with patient types counted on block 1 will be counted on the block 2 and 5
     */
    protected String getWhereClausePatientTypes(){
        String pTypesAcceptedGroup = "";
        pTypesAcceptedGroup = "" + PatientType.NEW.ordinal();
        pTypesAcceptedGroup = pTypesAcceptedGroup + "," + PatientType.PREVIOUSLY_TREATED.ordinal();
        pTypesAcceptedGroup = pTypesAcceptedGroup + "," + PatientType.UNKNOWN_PREVIOUS_TB_TREAT.ordinal();

        String pTypesAcceptedSubGroup = "";
        pTypesAcceptedSubGroup = "" + PatientType.RELAPSE.ordinal();
        pTypesAcceptedSubGroup = pTypesAcceptedSubGroup + "," + PatientType.TREATMENT_AFTER_FAILURE.ordinal();
        pTypesAcceptedSubGroup = pTypesAcceptedSubGroup + "," + PatientType.TREATMENT_AFTER_LOSS_FOLLOW_UP.ordinal();
        pTypesAcceptedSubGroup = pTypesAcceptedSubGroup + "," + PatientType.OTHER.ordinal();

        return " and (c.patientType in (" + pTypesAcceptedGroup + ") or c.previouslyTreatedType in (" + pTypesAcceptedSubGroup + ") )";
    }
}
