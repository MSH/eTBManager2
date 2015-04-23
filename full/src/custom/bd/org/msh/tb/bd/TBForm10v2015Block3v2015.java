package org.msh.tb.bd;

import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.enums.Gender;

import java.util.List;

@Name("TBForm10Block3v2015")
public class TBForm10v2015Block3v2015 extends TBForm10v2015 {

    @Override
    protected void createIndicators() {
        List<Object[]> result = null;
        String query = "";

        initializeInterfaceTableRows();

        // TB suspects with microscopy exams registered - consider dateCollected
        query = "select p.gender, count(*) "
                + " from TbCaseBD c join c.patient p join c.examsMicroscopy exm "
                + getHQLWhereBlock_3_4()
                + getHQLWhereExamClause("ExamMicroscopy", "exm", "dateCollected")
                + " group by p.gender ";
        result = entityManager.createQuery(query).getResultList();

        for(Object[] o : result) {
            Gender gender = (Gender) o[0];
            Long qtd = (Long) o[1];

            if (gender.equals(Gender.MALE)) {
                addValue("M1", "M", "nodisplayname_line1", qtd.floatValue());
                addValue(messages.get("global.total") + "1", messages.get("global.total"), "nodisplayname_line1", qtd.floatValue());

            } else if (gender.equals(Gender.FEMALE)) {
                addValue("F1", "M", "nodisplayname_line1", qtd.floatValue());
                addValue(messages.get("global.total") + "1", messages.get("global.total"), "nodisplayname_line1", qtd.floatValue());

            }
        }

        // TB suspects with POSITIVE microscopy exams registered - consider dateCollected
        query = "select p.gender, count(*) "
                + " from TbCaseBD c join c.patient p join c.examsMicroscopy exm "
                + getHQLWhereBlock_3_4()
                + getHQLWhereExamClause("ExamMicroscopy", "exm", "dateCollected")
                + " and exm.result in (1,2,3,4,5) "
                + " group by p.gender ";
        result = entityManager.createQuery(query).getResultList();

        for(Object[] o : result){
            Gender gender  = (Gender) o[0];
            Long qtd = (Long) o[1];

            if(gender.equals(Gender.MALE)){
                addValue("M2", "M", "nodisplayname_line1", qtd.floatValue());
                addValue(messages.get("global.total")+"2", messages.get("global.total"), "nodisplayname_line1", qtd.floatValue());

            }else if(gender.equals(Gender.FEMALE)) {
                addValue("F2", "M", "nodisplayname_line1", qtd.floatValue());
                addValue(messages.get("global.total")+"2", messages.get("global.total"), "nodisplayname_line1", qtd.floatValue());

            }
        }

    }

    /**
     * Initialize the interface table. All cells should be there also if it's result is zero.
     */
    private void initializeInterfaceTableRows(){
        //first 3 colummns, presumptive who had microscopy exam
        addValue("M1", "M", "nodisplayname_line1", new Float(0).floatValue());
        addValue("F1", "F", "nodisplayname_line1", new Float(0).floatValue());
        addValue(messages.get("global.total")+"1", messages.get("global.total"), "nodisplayname_line1", new Float(0).floatValue());

        //last 3 colummns, presumptive who had POSITIVE microscopy exam
        addValue("M2", "M", "nodisplayname_line1", new Float(0).floatValue());
        addValue("F2", "F", "nodisplayname_line1", new Float(0).floatValue());
        addValue(messages.get("global.total")+"2", messages.get("global.total"), "nodisplayname_line1", new Float(0).floatValue());
    }

}
