package org.msh.tb.bd;

import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.enums.Gender;
import org.msh.tb.entities.enums.HIVResult;
import org.msh.tb.entities.enums.PatientType;

import java.util.List;

@Name("TBForm10Block5v2015")
public class TBForm10v2015Block5v2015 extends TBForm10v2015 {

	@Override
	protected void createIndicators() {
        List<Object[]> result = null;
        String query = "";

        initializeInterfaceTableRows();

        // Pulmonary TB cases WITH treatment registered - Bacteriologically confirmed
        query = "select c.patientType, p.gender, count(*), hiv.result, "
                + getHQLSelectSubQBacteriologicallyConfirmedWithTreat("ExamMicroscopy", "exm") + " as micresult, "
                + getHQLSelectSubQBacteriologicallyConfirmedWithTreat("ExamCulture", "exc") + " as culresult, "
                + getHQLSelectSubQBacteriologicallyConfirmedWithTreat("ExamXpert", "exe") + " as expresult "
                + " from TbCaseBD c join c.patient p join c.resHIV hiv "
                + getHQLWhereBlock5() + " and c.infectionSite = 0 and c.treatmentPeriod.iniDate is not null and c.patientType is not null and p.gender is not null "
                + getHQLWhereExamClause("ExamHIV", "hiv", "date") + " and hiv.result is not null "
                + " group by c.patientType, p.gender, hiv.result "
                + " having col_4_0_ in (1,2,3,4,5) or col_5_0_ in (1,2,3,4,5) or col_6_0_ = 5 ";
        result = entityManager.createQuery(query).getResultList();
        allocateValuesOnFields(result, "1");

        // Pulmonary TB cases WITHOUT treatment registered - Bacteriologically confirmed
        query = "select c.patientType, p.gender, count(*), hiv.result, "
                + getHQLSelectSubQBacteriologicallyConfirmedNTR("ExamMicroscopy", "exm") + " as micresult, "
                + getHQLSelectSubQBacteriologicallyConfirmedNTR("ExamCulture", "exc") + " as culresult, "
                + getHQLSelectSubQBacteriologicallyConfirmedNTR("ExamXpert", "exe") + " as expresult "
                + " from TbCaseBD c join c.patient p join c.resHIV hiv "
                + getHQLWhereBlock5() + " and c.infectionSite = 0 and c.treatmentPeriod.iniDate is null and c.patientType is not null and p.gender is not null "
                + getHQLWhereExamClause("ExamHIV", "hiv", "date") + " and hiv.result is not null "
                + " group by c.patientType, p.gender, hiv.result "
                + " having col_4_0_ in (1,2,3,4,5) or col_5_0_ in (1,2,3,4,5) or col_6_0_ = 5 ";
        result = entityManager.createQuery(query).getResultList();
        allocateValuesOnFields(result, "1");

        // Pulmonary TB cases WITH treatment registered - Clinically confirmed
        query = "select c.patientType, p.gender, count(*), hiv.result, "
                + getHQLSelectSubQBacteriologicallyConfirmedWithTreat("ExamMicroscopy", "exm") + " as micresult, "
                + getHQLSelectSubQBacteriologicallyConfirmedWithTreat("ExamCulture", "exc") + " as culresult, "
                + getHQLSelectSubQBacteriologicallyConfirmedWithTreat("ExamXpert", "exe") + " as expresult, "
                + getHQLSelectSubQBacteriologicallyConfirmedWithTreatXray() + " as xrayresult "
                + " from TbCaseBD c join c.patient p join c.resHIV hiv "
                + getHQLWhereBlock5() + " and c.infectionSite = 0 and c.treatmentPeriod.iniDate is not null and c.patientType is not null and p.gender is not null "
                + getHQLWhereExamClause("ExamHIV", "hiv", "date") + " and hiv.result is not null "
                + " group by c.patientType, p.gender, hiv.result "
                + " having (col_4_0_ not in (1,2,3,4,5) or col_4_0_ is null) and (col_5_0_ not in (1,2,3,4,5) or col_5_0_ is null) and (col_6_0_ != 5 or col_6_0_ is null) "
                + " and col_7_0_ like '1' ";
        result = entityManager.createQuery(query).getResultList();
        allocateValuesOnFields(result, "2");

        // Pulmonary TB cases WITHOUT treatment registered - Clinically confirmed
        query = "select c.patientType, p.gender, count(*), hiv.result, "
                + getHQLSelectSubQBacteriologicallyConfirmedNTR("ExamMicroscopy", "exm") + " as micresult, "
                + getHQLSelectSubQBacteriologicallyConfirmedNTR("ExamCulture", "exc") + " as culresult, "
                + getHQLSelectSubQBacteriologicallyConfirmedNTR("ExamXpert", "exe") + " as expresult, "
                + getHQLSelectSubQBacteriologicallyConfirmedNTRXray() + " as xrayresult "
                + " from TbCaseBD c join c.patient p join c.resHIV hiv "
                + getHQLWhereBlock5() + " and c.infectionSite = 0 and c.treatmentPeriod.iniDate is null and c.patientType is not null and p.gender is not null "
                + getHQLWhereExamClause("ExamHIV", "hiv", "date") + " and hiv.result is not null "
                + " group by c.patientType, p.gender, hiv.result "
                + " having (col_4_0_ not in (1,2,3,4,5) or col_4_0_ is null) and (col_5_0_ not in (1,2,3,4,5) or col_5_0_ is null) and (col_6_0_ != 5 or col_6_0_ is null) "
                + " and col_7_0_ like '1' ";
        result = entityManager.createQuery(query).getResultList();
        allocateValuesOnFields(result, "2");

        // Extrapulmonary TB cases WITH treatment registered - Bacteriologically or Clinically confirmed
        query = "select c.patientType, p.gender, count(*), hiv.result, "
                + getHQLSelectSubQBacteriologicallyConfirmedWithTreat("ExamMicroscopy", "exm") + " as micresult, "
                + getHQLSelectSubQBacteriologicallyConfirmedWithTreat("ExamCulture", "exc") + " as culresult, "
                + getHQLSelectSubQBacteriologicallyConfirmedWithTreat("ExamXpert", "exe") + " as expresult, "
                + getHQLSelectSubQBacteriologicallyConfirmedWithTreatXray() + " as xrayresult "
                + " from TbCaseBD c join c.patient p join c.resHIV hiv "
                + getHQLWhereBlock5() + " and c.infectionSite = 1 and c.treatmentPeriod.iniDate is not null and c.patientType is not null and p.gender is not null "
                + getHQLWhereExamClause("ExamHIV", "hiv", "date") + " and hiv.result is not null "
                + " group by c.patientType, p.gender, hiv.result "
                + " having col_4_0_ in (1,2,3,4,5) or col_5_0_ in (1,2,3,4,5) or col_6_0_ = 5 or col_7_0_ like '1'";
        result = entityManager.createQuery(query).getResultList();
        allocateValuesOnFields(result, "3");

        // Extrapulmonary TB cases WITHOUT treatment registered - Bacteriologically or Clinically confirmed
        query = "select c.patientType, p.gender, count(*), hiv.result, "
                + getHQLSelectSubQBacteriologicallyConfirmedNTR("ExamMicroscopy", "exm") + " as micresult, "
                + getHQLSelectSubQBacteriologicallyConfirmedNTR("ExamCulture", "exc") + " as culresult, "
                + getHQLSelectSubQBacteriologicallyConfirmedNTR("ExamXpert", "exe") + " as expresult, "
                + getHQLSelectSubQBacteriologicallyConfirmedNTRXray() + " as xrayresult "
                + " from TbCaseBD c join c.patient p join c.resHIV hiv "
                + getHQLWhereBlock5() + " and c.infectionSite = 1 and c.treatmentPeriod.iniDate is null and c.patientType is not null and p.gender is not null "
                + getHQLWhereExamClause("ExamHIV", "hiv", "date") + " and hiv.result is not null "
                + " group by c.patientType, p.gender, hiv.result "
                + " having col_4_0_ in (1,2,3,4,5) or col_5_0_ in (1,2,3,4,5) or col_6_0_ = 5 or col_7_0_ like '1' ";
        result = entityManager.createQuery(query).getResultList();
        allocateValuesOnFields(result, "3");
	}

    /**
     * Initialize the interface table. All cells should be there also if it's result is zero.
     */
    private void initializeInterfaceTableRows(){
        addValue("M1", "M", messages.get("manag.tbform102015.block5.row1title"), new Float(0).floatValue());
        addValue("F1", "F", messages.get("manag.tbform102015.block5.row1title"), new Float(0).floatValue());
        addValue(messages.get("global.total")+"1", messages.get("global.total"), messages.get("manag.tbform102015.block5.row1title"), new Float(0).floatValue());
        addValue("M2", "M", messages.get("manag.tbform102015.block5.row1title"), new Float(0).floatValue());
        addValue("F2", "F", messages.get("manag.tbform102015.block5.row1title"), new Float(0).floatValue());
        addValue(messages.get("global.total")+"2", messages.get("global.total"), messages.get("manag.tbform102015.block5.row1title"), new Float(0).floatValue());

        addValue("M1", "M", messages.get("manag.tbform102015.block5.row2title"), new Float(0).floatValue());
        addValue("F1", "F", messages.get("manag.tbform102015.block5.row2title"), new Float(0).floatValue());
        addValue(messages.get("global.total")+"1", messages.get("global.total"), messages.get("manag.tbform102015.block5.row2title"), new Float(0).floatValue());
        addValue("M2", "M", messages.get("manag.tbform102015.block5.row2title"), new Float(0).floatValue());
        addValue("F2", "F", messages.get("manag.tbform102015.block5.row2title"), new Float(0).floatValue());
        addValue(messages.get("global.total")+"2", messages.get("global.total"), messages.get("manag.tbform102015.block5.row2title"), new Float(0).floatValue());

        addValue("M1", "M", messages.get("manag.tbform102015.block5.row3title"), new Float(0).floatValue());
        addValue("F1", "F", messages.get("manag.tbform102015.block5.row3title"), new Float(0).floatValue());
        addValue(messages.get("global.total")+"1", messages.get("global.total"), messages.get("manag.tbform102015.block5.row3title"), new Float(0).floatValue());
        addValue("M2", "M", messages.get("manag.tbform102015.block5.row3title"), new Float(0).floatValue());
        addValue("F2", "F", messages.get("manag.tbform102015.block5.row3title"), new Float(0).floatValue());
        addValue(messages.get("global.total")+"2", messages.get("global.total"), messages.get("manag.tbform102015.block5.row3title"), new Float(0).floatValue());

        addValue("M1", "M", messages.get("manag.tbform102015.block5.row4title"), new Float(0).floatValue());
        addValue("F1", "F", messages.get("manag.tbform102015.block5.row4title"), new Float(0).floatValue());
        addValue(messages.get("global.total")+"1", messages.get("global.total"), messages.get("manag.tbform102015.block5.row4title"), new Float(0).floatValue());
        addValue("M2", "M", messages.get("manag.tbform102015.block5.row4title"), new Float(0).floatValue());
        addValue("F2", "F", messages.get("manag.tbform102015.block5.row4title"), new Float(0).floatValue());
        addValue(messages.get("global.total")+"2", messages.get("global.total"), messages.get("manag.tbform102015.block5.row4title"), new Float(0).floatValue());

    }

    /**
     * Include the values from the result object to the interface table object
     * @param result the result to be included on interface table
     * @param row identifies the row that the result will count
     */
    private void allocateValuesOnFields(List<Object[]> result, String row){
        for(Object[] r : result){
            PatientType pt = (PatientType) r[0];
            Gender gender = (Gender) r[1];
            HIVResult hivResult = (HIVResult) r[3];
            Long qtd = (Long) r[2];

            if(pt.equals(PatientType.NEW) || pt.equals(PatientType.UNKNOWN_PREVIOUS_TB_TREAT)){
                if(gender.equals(gender.MALE))
                    addValue("M1", "M", messages.get("manag.tbform102015.block5.row"+row+"title"), qtd.floatValue());
                else if(gender.equals(gender.FEMALE))
                    addValue("F1", "F", messages.get("manag.tbform102015.block5.row"+row+"title"), qtd.floatValue());
                addValue(messages.get("global.total")+"1", messages.get("global.total"), messages.get("manag.tbform102015.block5.row"+row+"title"), qtd.floatValue());

                if(hivResult.equals(HIVResult.POSITIVE)){
                    if(gender.equals(gender.MALE))
                        addValue("M2", "M", messages.get("manag.tbform102015.block5.row"+row+"title"), qtd.floatValue());
                    else if(gender.equals(gender.FEMALE))
                        addValue("F2", "F", messages.get("manag.tbform102015.block5.row"+row+"title"), qtd.floatValue());
                    addValue(messages.get("global.total")+"2", messages.get("global.total"), messages.get("manag.tbform102015.block5.row"+row+"title"), qtd.floatValue());
                }
            }else{
                if(gender.equals(gender.MALE))
                    addValue("M1", "M", messages.get("manag.tbform102015.block5.row4title"), qtd.floatValue());
                else if(gender.equals(gender.FEMALE))
                    addValue("F1", "F", messages.get("manag.tbform102015.block5.row4title"), qtd.floatValue());
                addValue(messages.get("global.total")+"1", messages.get("global.total"), messages.get("manag.tbform102015.block5.row4title"), qtd.floatValue());

                if(hivResult.equals(HIVResult.POSITIVE)){
                    if(gender.equals(gender.MALE))
                        addValue("M2", "M", messages.get("manag.tbform102015.block5.row4title"), qtd.floatValue());
                    else if(gender.equals(gender.FEMALE))
                        addValue("F2", "F", messages.get("manag.tbform102015.block5.row4title"), qtd.floatValue());
                    addValue(messages.get("global.total")+"2", messages.get("global.total"), messages.get("manag.tbform102015.block5.row4title"), qtd.floatValue());
                }
            }
        }
    }

}
