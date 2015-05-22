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

        // Pulmonary and Extra-pulmonary
        query = "select c.patientRefToFv, count(*) "
                + " from TbCaseBD c join c.patient p "
                + getHQLWhereBlock_2_5() + " and c.infectionSite in (0,1) and c.caseDefinition in (0,1) " //Block 1 is considering only cases with caseDefinition defined as 0 or 1. Same for infectionSite
                + " group by c.patientRefToFv ";
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
