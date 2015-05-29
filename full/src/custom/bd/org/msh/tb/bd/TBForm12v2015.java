package org.msh.tb.bd;

import org.jboss.seam.annotations.In;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.Gender;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.indicators.core.Indicator2D;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;

/**
 * @author MSANTOS
 * Parent abstract class that contain all logic needed to generates the 3 tables for TB 11 Form
 */
 public abstract class TBForm12v2015 extends Indicator2D{

    @In(create=true) EntityManager entityManager;
    @In(create=true) Map<String, String> messages;

    /**
     * ENUM that defines the columns that exists in each table.
     */
    protected enum Column {
        TOTAL_REGISTERED("manag.pulmonary.totpatients"),
        SMEAR_NEGATIVE("manag.pulmonary.microscopy.negative"),
        SMEAR_POSITIVE("manag.pulmonary.microscopy.positive"),
        OUTCOME_DIED("CaseState.DIED"),
        OUTCOME_FAILURE("CaseState.FAILED"),
        OUTCOME_LOST_TO_FOLLOW_UP("CaseState.DEFAULTED"),
        OUTCOME_NOT_EVALUATED("CaseState.NOT_EVALUATED"),
        OUTCOME_OTHER("CaseState.OTHER"),
        NOT_EVALUATED_CASES("CaseState.NOT_EVALUATED"),
        GRAND_TOTAL("manag.pulmonary.grandtotal");

        String key;

        Column(String k){
            key = k;
        }

        public String getKey(){
            return key;
        }
    }

    /**
     * ENUM that defines the rows that exists in each table.
     */
    protected enum Row {
        NEW_UNKNOWN("manag.tbform112015.row1"),
        RELAPSES("PatientType.RELAPSE"),
        AFTER_FAILURE("PatientType.TREATMENT_AFTER_FAILURE"),
        AFTER_LOSS_FOLLOWUP("PatientType.TREATMENT_AFTER_LOSS_FOLLOW_UP"),
        OTHERS_PREV_TREAT("PatientType.OTHER_PREVIOUSLY_TREATED"),
        TOTAL("global.total");

        String key;

        Row(String k){
            key = k;
        }

        public String getKey(){
            return key;
        }
    }

    /**
     * Static string with the ordinal of the patient types counted on the report
     */
    protected static final String PATIENT_TYPES_ORDINAL_ROWS = PatientType.NEW.ordinal() + ", " + PatientType.PREVIOUSLY_TREATED.ordinal() + ", " + PatientType.UNKNOWN_PREVIOUS_TB_TREAT.ordinal();

    /**
     * Static string with the ordinal of the previously treated patient types counted on the report
     */
    protected static final String PATIENT_TYPES_PRE_TREAT_ORDINAL_ROWS = PatientType.RELAPSE.ordinal() + ", " + PatientType.TREATMENT_AFTER_FAILURE.ordinal() + ", "
            + PatientType.TREATMENT_AFTER_LOSS_FOLLOW_UP.ordinal() + ", " + PatientType.OTHER_PREVIOUSLY_TREATED.ordinal();

    protected void setValue(Column c, Row row, Gender gender, Float qtd){
        if(gender == null || row == null || c == null || qtd == null)
            return;

        String g = (gender.equals(Gender.MALE) ? messages.get("manag.gender.male") : messages.get("manag.gender.female"));

        addValue(c.name() + g, g, messages.get(row.getKey()), qtd);
        addValue(c.name() + g, g, messages.get(Row.TOTAL.getKey()), qtd);

        if(c.equals(Column.TOTAL_REGISTERED)){
            addValue(Column.TOTAL_REGISTERED.name()+"T", messages.get("manag.pulmonary.sum"), messages.get(row.getKey()), qtd);
            addValue(Column.TOTAL_REGISTERED.name()+"T", messages.get("manag.pulmonary.sum"), messages.get(Row.TOTAL.getKey()), qtd);
        }else{
            addValue(Column.GRAND_TOTAL.name() + g, g, messages.get(row.getKey()), qtd);
            addValue(Column.GRAND_TOTAL.name() + g, g, messages.get(Row.TOTAL.getKey()), qtd);
            addValue(Column.GRAND_TOTAL.name() + "T", messages.get("manag.pulmonary.sum"), messages.get(row.getKey()), qtd);
            addValue(Column.GRAND_TOTAL.name() + "T", messages.get("manag.pulmonary.sum"), messages.get(Row.TOTAL.getKey()), qtd);
        }
    }

    /**
     * This method will return the Row enum equivalent to the Patient Type passed as parameter
     * @param patientType Patient Type tested
     * @return the Row correspondent to the patient type passed as parameter.
     */
    protected Row getPatientTypeAsRow(PatientType patientType){
        if(patientType == null)
            return null;
        else if(PatientType.UNKNOWN_PREVIOUS_TB_TREAT.equals(patientType))
            return Row.NEW_UNKNOWN;
        else if(PatientType.NEW.equals(patientType))
            return Row.NEW_UNKNOWN;
        else if(PatientType.RELAPSE.equals(patientType))
            return Row.RELAPSES;
        else if(PatientType.TREATMENT_AFTER_FAILURE.equals(patientType))
            return Row.AFTER_FAILURE;
        else if(PatientType.TREATMENT_AFTER_LOSS_FOLLOW_UP.equals(patientType))
            return Row.AFTER_LOSS_FOLLOWUP;
        else if(PatientType.OTHER_PREVIOUSLY_TREATED.equals(patientType))
            return Row.OTHERS_PREV_TREAT;
        else
            return null;
    }

    /**
     * Method that creates all cells on the report with the start value as 0
     */
    protected void createInterfaceFields(){
        for(Row r : Row.values()){
            for(Column c : Column.values()){
                addValue(c.name()+"M", messages.get("manag.gender.male"), messages.get(r.getKey()), new Float(0));
                addValue(c.name()+"F", messages.get("manag.gender.female"), messages.get(r.getKey()), new Float(0));

                if(c.equals(Column.TOTAL_REGISTERED) || c.equals(Column.GRAND_TOTAL))
                    addValue(c.name()+"T", messages.get("manag.pulmonary.sum"), messages.get(r.getKey()), new Float(0));
            }
        }
    }

    /**
     * Execute the needed queries to generate the table
     */
    protected void createIndicators() {
        createInterfaceFields();
        List<Object[]> queryResult;

        //TODO: smear section


        //TODO: case states section


        //Total registered section
        queryResult = entityManager.createQuery(" select c.patientType, c.previouslyTreatedType, p.gender, count (*) " +
                " from TbCase c join c.patient p "
                + getHQLWhereForQuery()
                + "group by c.patientType, c.previouslyTreatedType, p.gender ")
                .getResultList();

        for (Object[] o : queryResult) {
            PatientType pt = (PatientType) o[0];
            PatientType prevPt = (PatientType) o[1];
            Gender g = (Gender) o[2];
            Long qtd = (Long) o[3];

            setValue(Column.TOTAL_REGISTERED, getPatientTypeAsRow(pt.equals(PatientType.PREVIOUSLY_TREATED) ? prevPt : pt), g, qtd.floatValue());
        }
    }

    protected abstract String getHQLWhereForQuery();
}
