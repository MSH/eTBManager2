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

    /*
    * TODO: a hack needed to be done on block 1, 2 and 5 so the query should work, check HAVING clause on queries on the respective classes.
    * About the TO DO above: In SQL I used left join with conditions on the ON clause to select the last exam for cases without treatment and
    * the last exam before treatment to cases with treatment, but in HQL ON clause doesn't exists. There is the WITH clause but in the version of
    * Hibernate that is used in ETBMANAGER it has some bugs.
    * The solution was writing sub queries on the SELECT clause (so the cases that doesn't have a certain exam should not be excluded from the result)
    * and test it on HAVING clause, but another bug of Hibernate happened... Hibernate doesn't recognizes the alias for the subquery result in HQL,
    * so I checked the alias generated on SQL log and putted it on HQL.
    * Summarizing, the alias on HQL is hardcoded to the alias generated in SQL by Hibernate.
    * */

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
     * This HQL where clause is used in many queries in blocks 1 and 2, so it was encapsulated on a method.
     * @return HQL where clause for block 1 and 2
     */
    protected String getHQLWhereBlock_1_2(){
        // cases has to be Tb and confirmed and has to be one of type of patients considered on TB 10
        return super.getHQLWhere() + getWhereClausePatientTypes() + " and ar.workspace.id = " + getWorkspace().getId().toString() +
                " and (c.age >= ar.iniAge and c.age <= ar.endAge) and c.classification = 0 and c.diagnosisType = 1 ";
    }

    /**
     * This HQL where clause is used in many queries in blocks 3 and 4, so it was encapsulated on a method.
     * @return HQL where clause for block 3 and 4
     */
    protected String getHQLWhereBlock_3_4(){
        // cases has to be Tb and suspect - do not consider filters and has to be one of type of patients considered on TB 10
        return " where c.classification = 0 and c.diagnosisType = 0 ";
    }

    /**
     * This HQL where clause is used in many queries in block 5, so it was encapsulated on a method.
     * @return HQL where clause for block 5
     */
    protected String getHQLWhereBlock5(){
        // cases has to be Tb and confirmed and has to be one of type of patients considered on TB 10
        return super.getHQLWhere() + getWhereClausePatientTypes() + " and c.classification = 0 and c.diagnosisType = 1 ";
    }

    /**
     * This HQL subquery is used in many queries in block 1, 2 and 5, so it was encapsulated on a method.
     * @return HQL subquery to be appended on the select clause of an HQL. This subquery selects the last exam before the start treatment date.
     * This subquery is for cases that has treatment registered.
     * @param examEntity The exam that will be selected on subquery
     * @param alias The alias of this exam on the main HQL
     */
    protected String getHQLSelectSubQBacteriologicallyConfirmedWithTreat(String examEntity, String alias){
        return " (select "+alias+".result from "+examEntity+" "+alias+" where "+alias+".tbcase.id = c.id "+
                " and "+alias+".id = (select max ("+alias+"2.id) from "+examEntity+" "+alias+"2 where "+alias+"2.tbcase.id = "+alias+".tbcase.id and "+alias+"2.dateRelease = " +
                "(select max("+alias+"3.dateRelease) from "+examEntity+" "+alias+"3 where "+alias+"3.tbcase.id = "+alias+"2.tbcase.id and "+alias+"3.dateRelease < c.treatmentPeriod.iniDate))) ";
    }

    /**
     * NTR = No treatment registered
     * This HQL subquery is used in many queries in block 1, 2 and 5, so it was encapsulated on a method.
     * @return HQL subquery to be appended on the select clause of an HQL. This subquery selects the last exam registered.
     * This subquery is for cases that has no treatment registred.
     * @param examEntity The exam that will be selected on subquery
     * @param alias The alias of this exam on the main HQL
     */
    protected String getHQLSelectSubQBacteriologicallyConfirmedNTR(String examEntity, String alias){
        return " (select "+alias+".result from "+examEntity+" "+alias+" where "+alias+".tbcase.id = c.id "+
                " and "+alias+".id = (select max ("+alias+"2.id) from "+examEntity+" "+alias+"2 where "+alias+"2.tbcase.id = "+alias+".tbcase.id and "+alias+"2.dateRelease = " +
                "(select max("+alias+"3.dateRelease) from "+examEntity+" "+alias+"3 where "+alias+"3.tbcase.id = "+alias+"2.tbcase.id))) ";
    }

    /**
     * This HQL subquery is used in many queries in block 1, 2 and 5, so it was encapsulated on a method.
     * @return HQL subquery to be appended on the select clause of an HQL. This subquery selects the last Xray exam registered before the start treatment date.
     * This subquery is for cases that has treatment registered.
     */
    protected String getHQLSelectSubQBacteriologicallyConfirmedWithTreatXray(){
        return " (select exxray.presentation.customId from ExamXRay exxray where exxray.tbcase.id = c.id "+
                " and exxray.id = (select max (exxray2.id) from ExamXRay exxray2 where exxray2.tbcase.id = exxray.tbcase.id and exxray2.date = " +
                "(select max(exxray3.date) from ExamXRay exxray3 where exxray3.tbcase.id = exxray2.tbcase.id and exxray3.date < c.treatmentPeriod.iniDate))) ";
    }

    /**
     * NTR = No treatment registered
     * This HQL subquery is used in many queries in block 1, 2 and 5, so it was encapsulated on a method.
     * @return HQL subquery to be appended on the select clause of an HQL. This subquery selects the last Xray exam registered.
     * This subquery is for cases that has no treatment registered.
     */
    protected String getHQLSelectSubQBacteriologicallyConfirmedNTRXray(){
        return " (select exxray.presentation.customId from ExamXRay exxray where exxray.tbcase.id = c.id "+
                " and exxray.id = (select max (exxray2.id) from ExamXRay exxray2 where exxray2.tbcase.id = exxray.tbcase.id and exxray2.date = " +
                "(select max(exxray3.date) from ExamXRay exxray3 where exxray3.tbcase.id = exxray2.tbcase.id))) ";
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
     * This HQL where conditions is used in many queries in block 3 and 4, so it was encapsulated on a method.
     * @return HQL sub query to be included on HQL select clause to select the very last HIV exam for each case
     */
    protected String getHQLSelectSubQLastHivResult(){
        return " (select hiv.result from ExamHIV hiv where hiv.tbcase.id = c.id "+
                " and hiv.id = (select max (hiv2.id) from ExamHIV hiv2 where hiv2.tbcase.id = hiv.tbcase.id and hiv2.date = " +
                "(select max(hiv3.date) from ExamHIV hiv3 where hiv3.tbcase.id = hiv2.tbcase.id))) ";
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
