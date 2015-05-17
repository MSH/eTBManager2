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
public abstract class TBForm11v2015 extends Indicator2D{

    @In(create=true) EntityManager entityManager;
    @In(create=true) Map<String, String> messages;

    /**
     * ENUM that defines 3 super-columns that exists in each table
     */
    protected enum Column {
        TOTAL_REGISTERED("manag.pulmonary.totpatients"),
        CASESTATES_NOT_INCLUDED("Column.CASESTATES_NOT_INCLUDED"),
        GRAND_TOTAL("manag.pulmonary.grandtotal");

        String key;

        private Column(String k){
            key = k;
        }

        public String getKey(){
            return key;
        }
    }

    /**
     * List of outcome columns that has to appear on the report
     */
    protected static final CaseState[] outcomesColumns = {
            CaseState.CURED,
            CaseState.TREATMENT_COMPLETED,
            CaseState.DIED,
            CaseState.FAILED,
            CaseState.DEFAULTED,
            CaseState.NOT_EVALUATED,
            CaseState.OTHER
    };

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

        private Row(String k){
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

    /**
     * Method that creates all cells on the report with the start value as 0
     */
    protected void createInterfaceFields(){
        for(Row r : Row.values()){
            addValue(Column.TOTAL_REGISTERED.name()+"M", messages.get("manag.gender.male"), messages.get(r.getKey()), new Float(0));
            addValue(Column.TOTAL_REGISTERED.name()+"F", messages.get("manag.gender.female"), messages.get(r.getKey()), new Float(0));
            addValue(Column.TOTAL_REGISTERED.name()+"T", messages.get("manag.pulmonary.sum"), messages.get(r.getKey()), new Float(0));

            for(CaseState cs : outcomesColumns){
                addValue(cs.name()+"M", messages.get("manag.gender.male"), messages.get(r.getKey()), new Float(0));
                addValue(cs.name()+"F", messages.get("manag.gender.female"), messages.get(r.getKey()), new Float(0));
            }

            addValue(Column.CASESTATES_NOT_INCLUDED.name()+"M", messages.get("manag.gender.male"), messages.get(r.getKey()), new Float(0));
            addValue(Column.CASESTATES_NOT_INCLUDED.name()+"F", messages.get("manag.gender.female"), messages.get(r.getKey()), new Float(0));

            addValue(Column.GRAND_TOTAL.name()+"M", messages.get("manag.gender.male"), messages.get(r.getKey()), new Float(0));
            addValue(Column.GRAND_TOTAL.name()+"F", messages.get("manag.gender.female"), messages.get(r.getKey()), new Float(0));
            addValue(Column.GRAND_TOTAL.name()+"T", messages.get("manag.pulmonary.sum"), messages.get(r.getKey()), new Float(0));
        }
    }

    /**
     * Method that insert the values on the tables based on the types defined to rows and columns.
     * This method also calculates the total row and the grand-total column.
     * @param cs Case state super-column that will receive the value
     * @param row Row that will receive the value
     * @param gender Gender column that will receive the value
     * @param qtd The quantity that will be set on the cell specified by the other parameters
     */
    protected void setValue(CaseState cs, Row row, Gender gender, Float qtd){
        if(gender == null || row == null || cs == null || qtd == null)
            return;

        String g = (gender.equals(Gender.MALE) ? messages.get("manag.gender.male") : messages.get("manag.gender.female"));

        if(isCaseStateAColumn(cs)) {
            addValue(cs.name() + g, g, messages.get(row.getKey()), qtd);
            addValue(cs.name() + g, g, messages.get(Row.TOTAL.getKey()), qtd);

            addValue(Column.GRAND_TOTAL.name() + g, g, messages.get(row.getKey()), qtd);
            addValue(Column.GRAND_TOTAL.name() + g, g, messages.get(Row.TOTAL.getKey()), qtd);
            addValue(Column.GRAND_TOTAL.name() + "T", messages.get("manag.pulmonary.sum"), messages.get(row.getKey()), qtd);
            addValue(Column.GRAND_TOTAL.name() + "T", messages.get("manag.pulmonary.sum"), messages.get(Row.TOTAL.getKey()), qtd);
        }else{
            addValue(Column.CASESTATES_NOT_INCLUDED.name() + g, g, messages.get(row.getKey()), qtd);
            addValue(Column.CASESTATES_NOT_INCLUDED.name() + g, g, messages.get(Row.TOTAL.getKey()), qtd);

            addValue(Column.GRAND_TOTAL.name() + g, g, messages.get(row.getKey()), qtd);
            addValue(Column.GRAND_TOTAL.name() + g, g, messages.get(Row.TOTAL.getKey()), qtd);
            addValue(Column.GRAND_TOTAL.name() + "T", messages.get("manag.pulmonary.sum"), messages.get(row.getKey()), qtd);
            addValue(Column.GRAND_TOTAL.name() + "T", messages.get("manag.pulmonary.sum"), messages.get(Row.TOTAL.getKey()), qtd);
        }
    }

    /**
     * Method that insert the values on the total reported super-column based on the types defined to rows and columns.
     * This method also calculates the total row.
     * @param row Row that will receive the value
     * @param gender Gender column that will receive the value
     * @param qtd The quantity that will be set on the cell specified by the other parameters
     */
    protected void setTotalReportedColumnValue(Row row, Gender gender, Float qtd){
        if(gender == null || row == null || qtd == null)
            return;

        String g = (gender.equals(Gender.MALE) ? messages.get("manag.gender.male") : messages.get("manag.gender.female"));

        addValue(Column.TOTAL_REGISTERED.name()+g, g, messages.get(row.getKey()), qtd);
        addValue(Column.TOTAL_REGISTERED.name()+"T", messages.get("manag.pulmonary.sum"), messages.get(row.getKey()), qtd);
        addValue(Column.TOTAL_REGISTERED.name()+g, g, messages.get(Row.TOTAL.getKey()), qtd);
        addValue(Column.TOTAL_REGISTERED.name()+"T", messages.get("manag.pulmonary.sum"), messages.get(Row.TOTAL.getKey()), qtd);
    }

    /**
     * This method will check if the case state parameter (cs) has its own super-column
     * @param cs Case state tested.
     * @return true if the case state passed on parameter cs has its own super-column on the table.
     */
    protected boolean isCaseStateAColumn(CaseState cs){
        if(cs==null)
            return false;

        for(CaseState c : outcomesColumns){
            if(c.equals(cs))
                return true;
        }

        return false;
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
     * Execute the needed queries to generate the table
     */
    protected void createIndicators() {
        createInterfaceFields();
        List<Object[]> queryResult;

        //Outcomes section
        queryResult = entityManager.createQuery(" select c.patientType, c.previouslyTreatedType, c.state, p.gender, count (*) " +
                " from TbCase c join c.patient p "
                + getHQLWhereForQuery()
                + "group by c.patientType, c.previouslyTreatedType, c.state, p.gender ")
                .getResultList();

        for(Object[] o : queryResult){
            PatientType pt = (PatientType) o[0];
            PatientType prevPt = (PatientType) o[1];
            CaseState cs = (CaseState) o[2];
            Gender g = (Gender) o[3];
            Long qtd = (Long) o[4];

            setValue(cs, getPatientTypeAsRow(pt.equals(PatientType.PREVIOUSLY_TREATED) ? prevPt : pt) , g, qtd.floatValue());
        }

        //Total registered section
        queryResult = entityManager.createQuery(" select c.patientType, c.previouslyTreatedType, p.gender, count (*) " +
                " from TbCase c join c.patient p "
                + getHQLWhereForQuery()
                + "group by c.patientType, c.previouslyTreatedType, p.gender ")
                .getResultList();

        for(Object[] o : queryResult){
            PatientType pt = (PatientType) o[0];
            PatientType prevPt = (PatientType) o[1];
            Gender g = (Gender) o[2];
            Long qtd = (Long) o[3];

            setTotalReportedColumnValue(getPatientTypeAsRow(pt.equals(PatientType.PREVIOUSLY_TREATED) ? prevPt : pt) , g, qtd.floatValue());
        }

    }

    protected abstract String getHQLWhereForQuery();
}
