package org.msh.tb.bd;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.msh.tb.AgeRangeHome;
import org.msh.tb.entities.AgeRange;
import org.msh.tb.entities.enums.CaseDefinition;
import org.msh.tb.entities.enums.Gender;
import org.msh.tb.entities.enums.PatientType;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

@Name("TBForm10Block1v2015")
public class TBForm10v2015Block1v2015 extends TBForm10v2015 {

    private TB10v2015Block1Info block1Info;

    /**
     * To guarantee that the conditions for period will be applied on registration date atributte
     */
    public void initialize(){
        this.getIndicatorFilters().setUseIniTreatmentDate(false);
        this.getIndicatorFilters().setUseRegistrationDate(true);
        this.block1Info = new TB10v2015Block1Info();
    }

	@Override
	protected void createIndicators() {
        List<Object[]> result = null;
        String query = "";

        // Pulmonary TB cases - Bacteriologically confirmed
        query = "select ar, c.patientType, c.previouslyTreatedType, p.gender, count(*) "
                + " from TbCase c, AgeRange ar join c.patient p "
                + getHQLWhereBlock1() + " and c.infectionSite = 0 and c.caseDefinition = " + CaseDefinition.BACTERIOLOGICALLY_CONFIRMED.ordinal()
                + " group by ar, c.patientType, c.previouslyTreatedType, p.gender ";
        result = entityManager.createQuery(query).getResultList();
        allocateValuesOnFields(result, 0);

         // Pulmonary TB cases - Clinically confirmed
        query = "select ar, c.patientType, c.previouslyTreatedType, p.gender, count(*) "
                + " from TbCase c, AgeRange ar join c.patient p "
                + getHQLWhereBlock1() + " and c.infectionSite = 0 and c.caseDefinition = " + CaseDefinition.CLINICALLY_DIAGNOSED.ordinal()
                + " group by ar, c.patientType, c.previouslyTreatedType, p.gender ";
        result = entityManager.createQuery(query).getResultList();
        allocateValuesOnFields(result, 1);

        // Extrapulmonary TB cases
        query = "select ar, c.patientType, c.previouslyTreatedType, p.gender, count(*) "
                + " from TbCase c, AgeRange ar join c.patient p "
                + getHQLWhereBlock1() + " and c.infectionSite = 1 and c.caseDefinition in (0,1) "
                + " group by ar, c.patientType, c.previouslyTreatedType, p.gender ";
        result = entityManager.createQuery(query).getResultList();
        allocateValuesOnFields(result, 2);

        populateInterfaceTableRows();
	}

    /**
     * The logic for allocating the values from the result of the queries on the object that stored this results was encapsulated on this method.
     * @param result the result returned from the query
     * @param subgroup identifies in witch subgroup the results ill be counted. the subgroup is defined on the conditions of the query.
     */
    public void allocateValuesOnFields(List<Object[]> result, int subgroup){
        for(Object[] o : result){
            AgeRange ar = (AgeRange)o[0];
            PatientType pt1 = (PatientType) o[1];
            PatientType prevpt = (PatientType) o[2];
            Gender g = (Gender) o[3];
            Long qtd = (Long) o[4];

            PatientType pt = (pt1.equals(PatientType.PREVIOUSLY_TREATED) ? prevpt : pt1);

            if(pt != null && (pt.equals(PatientType.UNKNOWN_PREVIOUS_TB_TREAT) || pt.equals(PatientType.NEW) || pt.equals(PatientType.RELAPSE) || pt.equals(PatientType.TREATMENT_AFTER_FAILURE) ||
                    pt.equals(PatientType.TREATMENT_AFTER_LOSS_FOLLOW_UP) || pt.equals(PatientType.OTHER)))
            this.block1Info.setValue(ar, subgroup, g, pt, qtd);
        }
    }

    /**
     * Write the results allocated on the block1info object to the interface table object
     */
    public void populateInterfaceTableRows(){

        // Populates the age ranges row
        for(AgeRange key : block1Info.getAgeGroupsRows().keySet()){
            //Pulmonary - Bacteriologically Confirmed
            for(PatientType pt : patientTypesReport){
                addValue("M"+pt.ordinal()+"bc", messages.get("manag.gender.male"), key.toString(), block1Info.getAgeGroupsRows().get(key).getPulmonaryBacteriologicallyConf().getMaleValues().get(pt).floatValue());
                addValue("F"+pt.ordinal()+"bc", messages.get("manag.gender.female"), key.toString(), block1Info.getAgeGroupsRows().get(key).getPulmonaryBacteriologicallyConf().getFemaleValues().get(pt).floatValue());
            }

            //Pulmonary - Clinically Confirmed
            for(PatientType pt : patientTypesReport){
                addValue("M" + pt.ordinal() + "cc", messages.get("manag.gender.male"), key.toString(), block1Info.getAgeGroupsRows().get(key).getPulmonaryClinicallyConf().getMaleValues().get(pt).floatValue());
                addValue("F"+pt.ordinal()+"cc", messages.get("manag.gender.female"), key.toString(), block1Info.getAgeGroupsRows().get(key).getPulmonaryClinicallyConf().getFemaleValues().get(pt).floatValue());
            }

            //Extrapulmonary
            for(PatientType pt : patientTypesReport){
                addValue("M" + pt.ordinal() + "ex", messages.get("manag.gender.male"), key.toString(), block1Info.getAgeGroupsRows().get(key).getExtrapulmonary().getMaleValues().get(pt).floatValue());
                addValue("F"+pt.ordinal()+"ex", messages.get("manag.gender.female"), key.toString(), block1Info.getAgeGroupsRows().get(key).getExtrapulmonary().getFemaleValues().get(pt).floatValue());
            }
            addValue("MT", messages.get("manag.gender.male"), key.toString(), block1Info.getAgeGroupsRows().get(key).getTotalByGender(Gender.MALE).floatValue());
            addValue("FT", messages.get("manag.gender.female"), key.toString(), block1Info.getAgeGroupsRows().get(key).getTotalByGender(Gender.FEMALE).floatValue());
            addValue("GT", messages.get("manag.pulmonary.sum"), key.toString(), block1Info.getAgeGroupsRows().get(key).getTotal().floatValue());
        }

        //Total Row
        for(PatientType pt : patientTypesReport){
            addValue("M"+pt.ordinal()+"bc", messages.get("manag.gender.male"), messages.get("global.total"), block1Info.getTotalByGenderAndPatientType(0, pt, Gender.MALE).floatValue());
            addValue("F"+pt.ordinal()+"bc", messages.get("manag.gender.female"),  messages.get("global.total"), block1Info.getTotalByGenderAndPatientType(0, pt, Gender.FEMALE).floatValue());
        }

        //Pulmonary - Clinically Confirmed
        for(PatientType pt : patientTypesReport){
            addValue("M" + pt.ordinal() + "cc", messages.get("manag.gender.male"),  messages.get("global.total"), block1Info.getTotalByGenderAndPatientType(1, pt, Gender.MALE).floatValue());
            addValue("F"+pt.ordinal()+"cc", messages.get("manag.gender.female"),  messages.get("global.total"), block1Info.getTotalByGenderAndPatientType(1, pt, Gender.MALE).floatValue());
        }

        //Extrapulmonary
        for(PatientType pt : patientTypesReport){
            addValue("M" + pt.ordinal() + "ex", messages.get("manag.gender.male"),  messages.get("global.total"), block1Info.getTotalByGenderAndPatientType(2, pt, Gender.MALE).floatValue());
            addValue("F"+pt.ordinal()+"ex", messages.get("manag.gender.female"),  messages.get("global.total"), block1Info.getTotalByGenderAndPatientType(2, pt, Gender.MALE).floatValue());
        }
        addValue("MT", messages.get("manag.gender.male"),  messages.get("global.total"), block1Info.getGrandTotalByGender(Gender.MALE).floatValue());
        addValue("FT", messages.get("manag.gender.female"), messages.get("global.total"), block1Info.getGrandTotalByGender(Gender.FEMALE).floatValue());
        addValue("GT", messages.get("manag.pulmonary.sum"), messages.get("global.total"), block1Info.getGrantTotal().floatValue());
    }

    public static PatientType[] getPatientTypesReport() {
        return patientTypesReport;
    }

    /**
     * This class stores the values
     */
    public class TB10v2015Block1Info{
        private LinkedHashMap<AgeRange, TB10v2015Block1RowInfo> ageGroupsRows;

        /**
         * Initializes objects that will store the information of the block 1 report
         */
        public TB10v2015Block1Info(){
            this.ageGroupsRows = new LinkedHashMap<AgeRange, TB10v2015Block1RowInfo>();
            List<AgeRange> ageRangeList = ((AgeRangeHome)Component.getInstance("ageRangeHome")).getItems();

            for(AgeRange item : ageRangeList){
                this.ageGroupsRows.put(item, new TB10v2015Block1RowInfo());
            }
        }

        /**
         * Method used to set a value to a certain "cell" of the interface table
         * @param ageRange - Identifies the age range of the quantity
         * @param subgroup - Identifies the subgroup of the quantity. 0 for pulmonaryBacteriologicallyConf, 1 for pulmonaryClinicallyConf, 2 for extrapulmonary.
         * @param gender - Identifies the gender of the quantity
         * @param pt - Identifies the patient type of the quantity
         * @param quantity - the quantity
         */
        public void setValue(AgeRange ageRange, int subgroup, Gender gender, PatientType pt, Long quantity){
            TB10v2015Block1RowInfo rowInfo = ageGroupsRows.get(ageRange);

            if(subgroup == 0 && gender.equals(Gender.MALE))
                rowInfo.getPulmonaryBacteriologicallyConf().getMaleValues().put(pt, (rowInfo.getPulmonaryBacteriologicallyConf().getMaleValues().get(pt).longValue()) + quantity.longValue());
            else if(subgroup == 0 && gender.equals(Gender.FEMALE))
                rowInfo.getPulmonaryBacteriologicallyConf().getFemaleValues().put(pt, (rowInfo.getPulmonaryBacteriologicallyConf().getFemaleValues().get(pt).longValue()) + quantity.longValue());
            if(subgroup == 1 && gender.equals(Gender.MALE))
                rowInfo.getPulmonaryClinicallyConf().getMaleValues().put(pt, (rowInfo.getPulmonaryClinicallyConf().getMaleValues().get(pt).longValue()) + quantity.longValue());
            else if(subgroup == 1 && gender.equals(Gender.FEMALE))
                rowInfo.getPulmonaryClinicallyConf().getFemaleValues().put(pt, (rowInfo.getPulmonaryClinicallyConf().getFemaleValues().get(pt).longValue()) + quantity.longValue());
            if(subgroup == 2 && gender.equals(Gender.MALE))
                rowInfo.getExtrapulmonary().getMaleValues().put(pt, (rowInfo.getExtrapulmonary().getMaleValues().get(pt).longValue()) + quantity.longValue());
            else if(subgroup == 2 && gender.equals(Gender.FEMALE))
                rowInfo.getExtrapulmonary().getFemaleValues().put(pt, (rowInfo.getExtrapulmonary().getFemaleValues().get(pt).longValue()) + quantity.longValue());
        }

        /**
         * @param subgroup - select the subgroup to totalize. 0 for pulmonaryBacteriologicallyConf, 1 for pulmonaryClinicallyConf, 2 for extrapulmonary.
         * @param patienttype - set the patient type to totalize.
         * @param gender - set the gender to totalize.
         * @return The total for the column selected according to the params.
         */
        public Long getTotalByGenderAndPatientType(int subgroup, PatientType patienttype, Gender gender){
            Long result = new Long(0);
            for(AgeRange key : ageGroupsRows.keySet()){
                if(subgroup == 0 && gender.equals(gender.MALE))
                    result = result.longValue() + ageGroupsRows.get(key).getPulmonaryBacteriologicallyConf().getMaleValues().get(patienttype).longValue();
                else if(subgroup == 0 && gender.equals(gender.FEMALE))
                    result = result.longValue() + ageGroupsRows.get(key).getPulmonaryBacteriologicallyConf().getFemaleValues().get(patienttype).longValue();
                else if(subgroup == 1 && gender.equals(gender.MALE))
                    result = result.longValue() + ageGroupsRows.get(key).getPulmonaryClinicallyConf().getMaleValues().get(patienttype).longValue();
                else if(subgroup == 1 && gender.equals(gender.FEMALE))
                    result = result.longValue() + ageGroupsRows.get(key).getPulmonaryClinicallyConf().getFemaleValues().get(patienttype).longValue();
                else if(subgroup == 2 && gender.equals(gender.MALE))
                    result = result.longValue() + ageGroupsRows.get(key).getExtrapulmonary().getMaleValues().get(patienttype).longValue();
                else if(subgroup == 2 && gender.equals(gender.FEMALE))
                    result = result.longValue() + ageGroupsRows.get(key).getExtrapulmonary().getFemaleValues().get(patienttype).longValue();

            }
            return result;
        }

        /**
         *
         * @param gender identify the gender that will return the grand total
         * @return the very grand total of cases counted by gender
         */
        public Long getGrandTotalByGender(Gender gender){
            List<AgeRange> ageRangeList = ((AgeRangeHome)Component.getInstance("ageRangeHome")).getItems();
            Long result = new Long(0);

            for(AgeRange key : getAgeGroupsRows().keySet()){
                result = result.longValue() + ageGroupsRows.get(key).getTotalByGender(gender).longValue();
            }

            return result;
        }

        /**
         *
         * @return the very grant total of the cases counted
         */
        public Long getGrantTotal(){
            return getGrandTotalByGender(Gender.MALE).longValue() + getGrandTotalByGender(Gender.FEMALE).longValue();
        }

        public HashMap<AgeRange, TB10v2015Block1RowInfo> getAgeGroupsRows() {
            return ageGroupsRows;
        }
    }

    public class TB10v2015Block1RowInfo{
        TB10v2015Block1SubGroupInfo pulmonaryBacteriologicallyConf;
        TB10v2015Block1SubGroupInfo pulmonaryClinicallyConf;
        TB10v2015Block1SubGroupInfo extrapulmonary;

        /**
         * Initializes each sub group object that will store information of block 1
         */
        public TB10v2015Block1RowInfo(){
            this.pulmonaryBacteriologicallyConf = new TB10v2015Block1SubGroupInfo();
            this.pulmonaryClinicallyConf = new TB10v2015Block1SubGroupInfo();
            this.extrapulmonary = new TB10v2015Block1SubGroupInfo();
        }

        public Long getTotalByGender(Gender gender){
            return pulmonaryBacteriologicallyConf.getTotalByGender(gender).longValue() + pulmonaryClinicallyConf.getTotalByGender(gender).longValue() + extrapulmonary.getTotalByGender(gender).longValue();
        }

        public Long getTotal(){
            return getTotalByGender(Gender.MALE).longValue() + getTotalByGender(Gender.FEMALE).longValue();
        }

        public TB10v2015Block1SubGroupInfo getPulmonaryBacteriologicallyConf() {
            return pulmonaryBacteriologicallyConf;
        }

        public TB10v2015Block1SubGroupInfo getPulmonaryClinicallyConf() {
            return pulmonaryClinicallyConf;
        }

        public TB10v2015Block1SubGroupInfo getExtrapulmonary() {
            return extrapulmonary;
        }
    }

    public class TB10v2015Block1SubGroupInfo{
        private HashMap<PatientType, Long> maleValues;
        private HashMap<PatientType, Long> femaleValues;

        /**
         * Initializes each patient type column and its sub columns male and female
         */
        public TB10v2015Block1SubGroupInfo(){
            maleValues = new HashMap<PatientType, Long>();
            for(PatientType type : patientTypesReport){
                this.maleValues.put(type, new Long(0));
            }

            femaleValues = new HashMap<PatientType, Long>();
            for(PatientType type : patientTypesReport){
                this.femaleValues.put(type, new Long(0));
            }
        }

        public HashMap<PatientType, Long> getMaleValues() {
            return maleValues;
        }

        public HashMap<PatientType, Long> getFemaleValues() {
            return femaleValues;
        }

        /**
         *
         * @param gender identify the gender asked
         * @return the total by gender for each column of the table.
         */
        public Long getTotalByGender(Gender gender){
            HashMap<PatientType, Long> values;
            Long result = new Long(0);

            if(gender == null)
                return result;
            else if(gender.equals(gender.MALE))
                values = maleValues;
            else
                values = femaleValues;

            for(PatientType key : values.keySet()){
                result = result.longValue() + values.get(key).longValue();
            }
            return result;
        }
    }
}
