package org.msh.tb.bd;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.AgeRangeHome;
import org.msh.tb.entities.AgeRange;
import org.msh.tb.entities.enums.*;
import org.msh.tb.indicators.core.Indicator2D;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.List;

@Name("TBForm8v2016Block1")
public class TBForm8v2016Block1 extends Indicator2D {

    @In(create=true) protected AgeRangeHome ageRangeHome;
    @In(create=true)
    EntityManager entityManager;

    private HashMap<DrugResistanceType, String> rowKeys = new HashMap<DrugResistanceType, String>();
    private final String ENRR_CONFIRMED_ROW = "Enrroled as Confirm DR TB";
    private final String MDR_ROW = "MDR";
    private final String XDR_ROW = "XDR";
    private final String RR_ROW = "RR";
    private final String OTHER_ROW = "Other DR";
    private final String SUBTOTAL_ROW = "Sub Total";
    private final String ENRR_PRESUMPTIVE_ROW = "Enrroled as Presumptive DR TB";
    private final String GRANT_TOTAL_ROW = "Grant Total";
    private final String TOTAL_COLUMN = "totalColumn";

    /**
     * To guarantee that the conditions for period will be applied on registration date atributte
     */
    public void initialize(){
        this.getIndicatorFilters().setUseIniTreatmentDate(false);
        this.getIndicatorFilters().setUseRegistrationDate(true);

        if(rowKeys == null || rowKeys.size() == 0){
            rowKeys.put(DrugResistanceType.MONO_RESISTANCE, OTHER_ROW);
            rowKeys.put(DrugResistanceType.MONO_RESISTANCE_RIF, RR_ROW);
            rowKeys.put(DrugResistanceType.POLY_RESISTANCE, OTHER_ROW);
            rowKeys.put(DrugResistanceType.POLY_RESISTANCE_RIF, RR_ROW);
            rowKeys.put(DrugResistanceType.MULTIDRUG_RESISTANCE, MDR_ROW);
            rowKeys.put(DrugResistanceType.EXTENSIVEDRUG_RESISTANCE, XDR_ROW);
        }
    }

	@Override
	protected void createIndicators() {
        initializeInterface();

        List<Object[]> result = null;
        String query = "";

        query = "select ar, p.gender, c.drugResistanceType, count(*) "
                + " from TbCase c, AgeRange ar join c.patient p "
                + getHQLWhere() + " and ar.workspace.id = " + getWorkspace().getId().toString() + " and (c.age >= ar.iniAge and c.age <= ar.endAge) "
                + " and c.diagnosisType = 1 and c.classification = 1 and c.treatmentPeriod.iniDate is not null " // confirmed DRTB cases not waiting treatment
                + " group by ar, p.gender, c.drugResistanceType ";
        result = entityManager.createQuery(query).getResultList();
        populateInterfaceRowsConfCases(result);

        query = "select ar, p.gender, count(*) "
                + " from TbCase c, AgeRange ar join c.patient p "
                + getHQLWhere() + " and ar.workspace.id = " + getWorkspace().getId().toString() + " and (c.age >= ar.iniAge and c.age <= ar.endAge) "
                + " and c.diagnosisType = 0 and c.classification = 1 and c.treatmentPeriod.iniDate is not null " // presumptive DRTB cases not waiting treatment
                + " group by ar, p.gender ";
        result = entityManager.createQuery(query).getResultList();
        populateInterfaceRowsPresCases(result);
	}

    private void populateInterfaceRowsConfCases(List<Object[]> data){
        for(Object[] o : data){
            AgeRange ar = (AgeRange)o[0];
            Gender g = (Gender)o[1];
            DrugResistanceType dr = (DrugResistanceType) o[2];
            Long qtd = (Long) o[3];

            addValue(g.name() + ar.toString(), g.getAbbrev(), rowKeys.get(dr), qtd.floatValue());

            //total column
            addValue(g.name() + TOTAL_COLUMN, g.getAbbrev(), rowKeys.get(dr), qtd.floatValue());

            //sub total row
            addValue(g.name() + ar.toString(), g.getAbbrev(), SUBTOTAL_ROW, qtd.floatValue());
            addValue(g.name() + TOTAL_COLUMN, g.getAbbrev(), SUBTOTAL_ROW, qtd.floatValue());

            //enrroled as confirmed row
            addValue(g.name() + ar.toString(), g.getAbbrev(), ENRR_CONFIRMED_ROW, qtd.floatValue());
            addValue(g.name() + TOTAL_COLUMN, g.getAbbrev(), ENRR_CONFIRMED_ROW, qtd.floatValue());

            //grand total row
            addValue(g.name() + ar.toString(), g.getAbbrev(), GRANT_TOTAL_ROW, qtd.floatValue());
            addValue(g.name() + TOTAL_COLUMN, g.getAbbrev(), GRANT_TOTAL_ROW, qtd.floatValue());
        }
    }

    private void populateInterfaceRowsPresCases(List<Object[]> data){
        for(Object[] o : data){
            AgeRange ar = (AgeRange)o[0];
            Gender g = (Gender)o[1];
            Long qtd = (Long) o[2];

            //enrroled as presumptive row
            addValue(g.name() + ar.toString(), g.getAbbrev(), ENRR_PRESUMPTIVE_ROW, qtd.floatValue());
            addValue(g.name() + TOTAL_COLUMN, g.getAbbrev(), ENRR_PRESUMPTIVE_ROW, qtd.floatValue());
        }
    }

    private void initializeInterface(){
        int position = 0;
        List<AgeRange> ars = ageRangeHome.getItems();

        while(position <= ars.size()) {
            String superColumn = position == ars.size() ? TOTAL_COLUMN : ars.get(position).toString();

            addValue(Gender.MALE.name() + superColumn, "M", ENRR_CONFIRMED_ROW, new Float(0));
            addValue(Gender.FEMALE.name() + superColumn, "F", ENRR_CONFIRMED_ROW, new Float(0));

            addValue(Gender.MALE.name() + superColumn, "M", MDR_ROW, new Float(0));
            addValue(Gender.FEMALE.name() + superColumn, "F", MDR_ROW, new Float(0));

            addValue(Gender.MALE.name() + superColumn, "M", XDR_ROW, new Float(0));
            addValue(Gender.FEMALE.name() + superColumn, "F", XDR_ROW, new Float(0));

            addValue(Gender.MALE.name() + superColumn, "M", RR_ROW, new Float(0));
            addValue(Gender.FEMALE.name() + superColumn, "F", RR_ROW, new Float(0));

            addValue(Gender.MALE.name() + superColumn, "M", OTHER_ROW, new Float(0));
            addValue(Gender.FEMALE.name() + superColumn, "F", OTHER_ROW, new Float(0));

            addValue(Gender.MALE.name() + superColumn, "M", SUBTOTAL_ROW, new Float(0));
            addValue(Gender.FEMALE.name() + superColumn, "F", SUBTOTAL_ROW, new Float(0));

            addValue(Gender.MALE.name() + superColumn, "M", ENRR_PRESUMPTIVE_ROW, new Float(0));
            addValue(Gender.FEMALE.name() + superColumn, "F", ENRR_PRESUMPTIVE_ROW, new Float(0));

            addValue(Gender.MALE.name() + superColumn, "M", GRANT_TOTAL_ROW, new Float(0));
            addValue(Gender.FEMALE.name() + superColumn, "F", GRANT_TOTAL_ROW, new Float(0));

            position++;
        }
    }
}
