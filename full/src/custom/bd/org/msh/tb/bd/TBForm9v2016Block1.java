package org.msh.tb.bd;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.tb.AgeRangeHome;
import org.msh.tb.entities.AgeRange;
import org.msh.tb.entities.enums.*;
import org.msh.tb.indicators.core.Indicator2D;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Name("TBForm9v2016Block1")
public class TBForm9v2016Block1 extends Indicator2D {

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

    private final String TOTAL_COLUMN = "Total";
    private final String TOTAL_REGISTERED_COLUMN = "Total number of DR TB patients registered during the quarter";

    private List<CaseState> suportedStates;

    protected static final CaseState[] reportStates = {
            CaseState.CURED,
            CaseState.TREATMENT_COMPLETED,
            CaseState.FAILED,
            CaseState.DIED,
            CaseState.DEFAULTED,
            CaseState.NOT_EVALUATED,
            CaseState.ONTREATMENT,
            CaseState.WAITING_TREATMENT
    };

    /**
     * To guarantee that the conditions for period will be applied on registration date atributte
     */
    public void initialize(){
        this.getIndicatorFilters().setUseIniTreatmentDate(false);
        this.getIndicatorFilters().setUseRegistrationDate(true);

        suportedStates = Arrays.asList(reportStates);

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

        List<Object[]> result;
        String query;

        query = "select c.diagnosisType, c.state, c.drugResistanceType, count(*) "
                + " from TbCase c join c.patient p "
                + getHQLWhere() + " and c.classification = 1 " // DRTB cases
                + " and c.diagnosisType is not null and c.state is not null " // prevent null pointer
                + " group by c.diagnosisType, c.state, c.drugResistanceType ";
        result = entityManager.createQuery(query).getResultList();
        populateInterfaceRowsCases(result);
    }

    private void populateInterfaceRowsCases(List<Object[]> data){
        Map<String, String> msgs = Messages.instance();

        for(Object[] o : data){
            DiagnosisType dt = (DiagnosisType) o[0];
            CaseState cs = (CaseState)o[1];
            DrugResistanceType dr = (DrugResistanceType) o[2];
            Long qtd = (Long) o[3];

            String csColumn = msgs.get(cs.getKey());

            if(DiagnosisType.SUSPECT.equals(dt)){
                if(isSupportedState(cs)) {
                    //enrroled as presumptive row
                    addValue(csColumn, ENRR_PRESUMPTIVE_ROW, qtd.floatValue());
                    addValue(TOTAL_COLUMN, ENRR_PRESUMPTIVE_ROW, qtd.floatValue());
                }

                // Total registered column
                addValue(TOTAL_REGISTERED_COLUMN, ENRR_PRESUMPTIVE_ROW, qtd.floatValue());
            }else {
                if(isSupportedState(cs)) {
                    addValue(csColumn, rowKeys.get(dr), qtd.floatValue());

                    //total column
                    addValue(TOTAL_COLUMN, rowKeys.get(dr), qtd.floatValue());

                    //sub total row
                    addValue(csColumn, SUBTOTAL_ROW, qtd.floatValue());
                    addValue(TOTAL_COLUMN, SUBTOTAL_ROW, qtd.floatValue());

                    //enrroled as confirmed row
                    addValue(csColumn, ENRR_CONFIRMED_ROW, qtd.floatValue());
                    addValue(TOTAL_COLUMN, ENRR_CONFIRMED_ROW, qtd.floatValue());

                    //grand total row
                    addValue(csColumn, GRANT_TOTAL_ROW, qtd.floatValue());
                    addValue(TOTAL_COLUMN, GRANT_TOTAL_ROW, qtd.floatValue());
                }

                // Total registered column
                addValue(TOTAL_REGISTERED_COLUMN, ENRR_CONFIRMED_ROW, qtd.floatValue());
                addValue(TOTAL_REGISTERED_COLUMN, rowKeys.get(dr), qtd.floatValue());
                addValue(TOTAL_REGISTERED_COLUMN, SUBTOTAL_ROW, qtd.floatValue());
                addValue(TOTAL_REGISTERED_COLUMN, GRANT_TOTAL_ROW, qtd.floatValue());
            }
        }
    }

    private void initializeInterface(){
        Map<String, String> msgs = Messages.instance();
        int position = -1;
        List<CaseState> states = Arrays.asList(this.reportStates);

        while(position <= states.size()) {
            String column = position < 0 ? TOTAL_REGISTERED_COLUMN : position == states.size() ? TOTAL_COLUMN : msgs.get(states.get(position).getKey());

            addValue(column, ENRR_CONFIRMED_ROW, new Float(0));
            addValue(column, MDR_ROW, new Float(0));
            addValue(column, XDR_ROW, new Float(0));
            addValue(column, RR_ROW, new Float(0));
            addValue(column, OTHER_ROW, new Float(0));
            addValue(column, SUBTOTAL_ROW, new Float(0));
            addValue(column, ENRR_PRESUMPTIVE_ROW, new Float(0));
            addValue(column, GRANT_TOTAL_ROW, new Float(0));

            position++;
        }
    }

    private boolean isSupportedState(CaseState state){
        return suportedStates.contains(state);
    }
}
