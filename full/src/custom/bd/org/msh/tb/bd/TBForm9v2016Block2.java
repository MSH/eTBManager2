package org.msh.tb.bd;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.tb.AgeRangeHome;
import org.msh.tb.entities.enums.*;
import org.msh.tb.indicators.core.Indicator2D;

import javax.persistence.EntityManager;
import java.util.*;

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

    private final String GRAND_TOTAL_ROW = "Grand Total";
    private final String TOTAL_COLUMN = "Total";
    private final String TOTAL_REGISTERED_COLUMN = "Total number of DR TB patients registered during the quarter";

    private List<CaseState> suportedStates;
    private List<PatientType> notDetailedPatientTypes;

    private static final CaseState[] reportStates = {
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
                + " and c.patientType is not null and c.infectionSite is not null and c.caseDefinition is not null and c.state is not null "// prevent null pointer
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
            addValue(TOTAL_REGISTERED_COLUMN, GRAND_TOTAL_ROW, qtd.floatValue());

            if(suportedStates.contains(cs)) {
                if (notDetailedPatientTypes.contains(pt)) {
                    addValue(msgs.get(cs.getKey()), msgs.get(pt.getKey()), qtd.floatValue());
                    addValue(msgs.get(cs.getKey()), GRAND_TOTAL_ROW, qtd.floatValue());

                    addValue(TOTAL_COLUMN, msgs.get(pt.getKey()), qtd.floatValue());
                    addValue(TOTAL_COLUMN, GRAND_TOTAL_ROW, qtd.floatValue());
                }else{
                    String row = msgs.get(pt.getKey()) + " - " + msgs.get(is.getKey()) + (InfectionSite.EXTRAPULMONARY.equals(is) ? "" : " - " + msgs.get(cd.getKey()));
                    addValue(msgs.get(cs.getKey()), row, qtd.floatValue());
                    addValue(msgs.get(cs.getKey()), GRAND_TOTAL_ROW, qtd.floatValue());

                    addValue(TOTAL_COLUMN, row, qtd.floatValue());
                    addValue(TOTAL_COLUMN, GRAND_TOTAL_ROW, qtd.floatValue());
                }
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

            //Just to simplify code understanding
            ArrayList<String> otherRows = new ArrayList<String>();
            otherRows.add(msgs.get(PatientType.DRTB_OTHER_UNKNOWN_HISTORY.getKey()) + " - " + msgs.get(InfectionSite.PULMONARY.getKey()) + " - " + msgs.get(CaseDefinition.CLINICALLY_DIAGNOSED.getKey()));
            otherRows.add(msgs.get(PatientType.DRTB_OTHER_NEW.getKey()) + " - " + msgs.get(InfectionSite.PULMONARY.getKey()) + " - " + msgs.get(CaseDefinition.CLINICALLY_DIAGNOSED.getKey()));
            otherRows.add(msgs.get(PatientType.DRTB_OTHER_PREVIOUSLY_TREATED.getKey()) + " - " + msgs.get(InfectionSite.PULMONARY.getKey()) + " - " + msgs.get(CaseDefinition.CLINICALLY_DIAGNOSED.getKey()));
            otherRows.add(msgs.get(PatientType.DRTB_OTHER_UNKNOWN_HISTORY.getKey()) + " - " + msgs.get(InfectionSite.EXTRAPULMONARY.getKey()));
            otherRows.add(msgs.get(PatientType.DRTB_OTHER_NEW.getKey()) + " - " + msgs.get(InfectionSite.EXTRAPULMONARY.getKey()));
            otherRows.add(msgs.get(PatientType.DRTB_OTHER_PREVIOUSLY_TREATED.getKey()) + " - " + msgs.get(InfectionSite.EXTRAPULMONARY.getKey()));
            otherRows.add(msgs.get(PatientType.DRTB_OTHER_UNKNOWN_HISTORY.getKey()) + " - " + msgs.get(InfectionSite.PULMONARY.getKey()) + " - " + msgs.get(CaseDefinition.BACTERIOLOGICALLY_CONFIRMED.getKey()));
            otherRows.add(msgs.get(PatientType.DRTB_OTHER_NEW.getKey()) + " - " + msgs.get(InfectionSite.PULMONARY.getKey()) + " - " + msgs.get(CaseDefinition.BACTERIOLOGICALLY_CONFIRMED.getKey()));
            otherRows.add(msgs.get(PatientType.DRTB_OTHER_PREVIOUSLY_TREATED.getKey()) + " - " + msgs.get(InfectionSite.PULMONARY.getKey()) + " - " + msgs.get(CaseDefinition.BACTERIOLOGICALLY_CONFIRMED.getKey()));

            for(String row : otherRows){
                addValue(column, row, new Float(0));
            }

            addValue(column, GRAND_TOTAL_ROW, new Float(0));

            position++;
        }
    }
}
