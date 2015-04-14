package org.msh.tb.bd;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.FieldValue;
import org.msh.tb.misc.FieldsQuery;

import java.util.List;

@Name("TBForm10Block2v2015")
public class TBForm10v2015Block2v2015 extends TBForm10v2015 {

    private Long total;

    @Override
    protected void createIndicators() {
        List<Object[]> result = null;
        String query = "";
        total = new Long(0);

        includeAllRefByOnInterfaceTable();

        // TB cases WITH treatment registered
        query = "select c.patientRefToFv, count(*), "
                + getHQLSelectSubQBacteriologicallyConfirmedWithTreat("ExamMicroscopy", "exm") + " as micresult, "
                + getHQLSelectSubQBacteriologicallyConfirmedWithTreat("ExamCulture", "exc") + " as culresult, "
                + getHQLSelectSubQBacteriologicallyConfirmedWithTreat("ExamXpert", "exe") + " as expresult, "
                + getHQLSelectSubQBacteriologicallyConfirmedWithTreatXray() + " as xrayresult "
                + " from TbCaseBD c, AgeRange ar join c.patient p "
                + getHQLWhereBlock_1_2() + " and c.treatmentPeriod.iniDate is not null "
                + " and c.patientType is not null and p.gender is not null " //Block 1 is considering only cases with this fields not null. Need to maintain this condition to the total values be the same
                + " group by c.patientRefToFv "
                + " having col_2_0_ in (1,2,3,4,5) or col_3_0_ in (1,2,3,4,5) or col_4_0_ = 5 or col_5_0_ like '1' ";
        result = entityManager.createQuery(query).getResultList();
        populateInfacetableRows(result);

        // TB cases WITHOUT treatment registered
        query = "select c.patientRefToFv, count(*), "
                + getHQLSelectSubQBacteriologicallyConfirmedNTR("ExamMicroscopy", "exm") + " as micresult, "
                + getHQLSelectSubQBacteriologicallyConfirmedNTR("ExamCulture", "exc") + " as culresult, "
                + getHQLSelectSubQBacteriologicallyConfirmedNTR("ExamXpert", "exe") + " as expresult, "
                + getHQLSelectSubQBacteriologicallyConfirmedNTRXray() + " as xrayresult "
                + " from TbCaseBD c, AgeRange ar join c.patient p "
                + getHQLWhereBlock_1_2() + " and c.treatmentPeriod.iniDate is null "
                + " and c.patientType is not null and p.gender is not null " //Block 1 is considering only cases with this fields not null. Need to maintain this condition to the total values be the same
                + " group by c.patientRefToFv "
                + " having col_2_0_ in (1,2,3,4,5) or col_3_0_ in (1,2,3,4,5) or col_4_0_ = 5 or col_5_0_ like '1' ";
        result = entityManager.createQuery(query).getResultList();
        populateInfacetableRows(result);

        addValue(messages.get("global.total"),"nodisplayname_line1", total.floatValue());

        total = new Long(0);
    }

    /**
     * Includes the result on the interface table
     * @param result the result of the query used to get the number by patient referred by
     */
    private void populateInfacetableRows(List<Object[]> result){
        for(Object[] r : result){
            FieldValue refByType = (FieldValue) r[0];
            Long qtd = (Long) r[1];

            total = total.longValue() + qtd.longValue();

            addValue(refByType.getName().getName1(),"nodisplayname_line1", qtd.floatValue());
        }
    }

    /**
     * Initialize the interface table. All referred by types should be there also if it's result is zero.
     */
    private void includeAllRefByOnInterfaceTable(){
        List<FieldValue> refBys = ((FieldsQuery)Component.getInstance(FieldsQuery.class)).getRefToTypes();

        for(FieldValue f : refBys){
            addValue(f.getName().getName1(),"nodisplayname_line1", new Float(0));
        }
    }
}
