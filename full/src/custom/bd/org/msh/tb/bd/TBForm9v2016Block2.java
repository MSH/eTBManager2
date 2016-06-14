package org.msh.tb.bd;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.tb.AgeRangeHome;
import org.msh.tb.entities.enums.*;
import org.msh.tb.indicators.core.Indicator2D;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Name("TBForm9v2016Block2")
public class TBForm9v2016Block2 extends Indicator2D {

    @In(create=true) protected AgeRangeHome ageRangeHome;
    @In(create=true)
    EntityManager entityManager;

    private static final PatientType rowsRegistrationGroup[] = {
            PatientType.CATI_NON_CONVERTER,
            PatientType.CATI_FAILURE,
            PatientType.CATI_TREATMENT_AFTER_LOSS_FOLLOW_UP,
            PatientType.CATI_RELAPSE,
            PatientType.CATII_NON_CONVERTER,
            PatientType.CATII_FAILURE,
            PatientType.CATII_TREATMENT_AFTER_LOSS_FOLLOW_UP,
            PatientType.CATII_RELAPSE,
            PatientType.DRTB_TRANSFER_IN,
            PatientType.DRTB_CLOSE_CONTACT_WITHSS_UNKNOWN_HISTORY,
            PatientType.DRTB_CLOSE_CONTACT_WITHSS_NEW,
            PatientType.DRTB_CLOSE_CONTACT_WITHSS_PREVIOUSLY_TREATED,
            PatientType.HIV_INFECTED_WITHSS_UNKNOWN_HISTORY,
            PatientType.HIV_INFECTED_WITHSS_NEW,
            PatientType.HIV_INFECTED_WITHSS_PREVIOUSLY_TREATED
    };

    private final String TOTAL_COLUMN = "Total";
    private final String TOTAL_REGISTERED_COLUMN = "Total number of DR TB patients registered during the quarter";

    private List<CaseState> suportedStates;
    private List<PatientType> notDetailedPatientTypes;

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
        notDetailedPatientTypes = Arrays.asList(rowsRegistrationGroup);
    }

    @Override
    protected void createIndicators() {
        initializeInterface();

        List<Object[]> result;
        String query;

        query = "select c.patientType, c.infectionSite, c.caseDefinition, c.state, count(*) "
                + " from TbCase c join c.patient p "
                + getHQLWhere() + " and c.classification = 1 and c.diagnosisType = 1 " // confirmed DRTB cases
                + " group by c.patientType, c.infectionSite, c.caseDefinition, c.state ";
        result = entityManager.createQuery(query).getResultList();
        populateInterfaceRowsCases(result);
    }

    private void populateInterfaceRowsCases(List<Object[]> data){
        Map<String, String> msgs = Messages.instance();

        for(Object[] o: data){
            PatientType pt = (PatientType) o[0];
            InfectionSite is = (InfectionSite) o[1];
            CaseDefinition cd = (CaseDefinition) o[2];
            CaseState cs = (CaseState) o[3];
            Long qtd = (Long) o[4];

            addValue(TOTAL_REGISTERED_COLUMN, msgs.get(pt.getKey()), qtd.floatValue());

            if(notDetailedPatientTypes.contains(pt)) {
                addValue(msgs.get(cs.getKey()), msgs.get(pt.getKey()), qtd.floatValue());
                //TODO: add to grandtotal row
                addValue(TOTAL_COLUMN, msgs.get(pt.getKey()), qtd.floatValue());
            }
        }

    }

    private void initializeInterface(){
        Map<String, String> msgs = Messages.instance();
        int position = -1;
        List<CaseState> states = Arrays.asList(this.reportStates);

        while(position <= states.size()) {
            String column = position < 0 ? TOTAL_REGISTERED_COLUMN : position == states.size() ? TOTAL_COLUMN : msgs.get(states.get(position).getKey());

            for(PatientType pt: rowsRegistrationGroup){
                addValue(column, msgs.get(pt.getKey()), new Float(0));
            }

            position++;
        }
    }

    private boolean isSupportedState(CaseState state){
        return suportedStates.contains(state);
    }
}
