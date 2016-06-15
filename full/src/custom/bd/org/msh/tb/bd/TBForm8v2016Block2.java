package org.msh.tb.bd;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.indicators.core.Indicator2D;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Name("TBForm8v2016Block2")
public class TBForm8v2016Block2 extends Indicator2D {

    @In(create=true)
    EntityManager entityManager;

    private static final PatientType superColumn1[] = {
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

    private static final PatientType otherSuperColumns[] = {
            PatientType.DRTB_OTHER_UNKNOWN_HISTORY,
            PatientType.DRTB_OTHER_NEW,
            PatientType.DRTB_OTHER_PREVIOUSLY_TREATED
    };

    private List<PatientType> allPatientTypes;

    /**
     * To guarantee that the conditions for period will be applied on registration date atributte
     */
    public void initialize(){
        this.getIndicatorFilters().setUseIniTreatmentDate(false);
        this.getIndicatorFilters().setUseRegistrationDate(true);

        allPatientTypes = new ArrayList<PatientType>();
        allPatientTypes.addAll(Arrays.asList(superColumn1));
        allPatientTypes.addAll(Arrays.asList(otherSuperColumns));
    }

	@Override
	protected void createIndicators() {
        initializeInterface();

        List<Object[]> result = null;
        String query = "";

        query = "select c.patientType, count(*) "
                + " from TbCase c join c.patient p "
                + getHQLWhere() + " and c.classification = 1 and c.infectionSite = 0 and c.caseDefinition = 0 " // pulmonary bacteriollogically confirmed DRTB cases
                + " and c.patientType is not null " // prevent null pointer
                + " group by c.patientType ";
        result = entityManager.createQuery(query).getResultList();
        populateInterfaceRows(result, "PULBAC");

        query = "select c.patientType, count(*) "
                + " from TbCase c join c.patient p "
                + getHQLWhere() + " and c.classification = 1 and c.infectionSite = 1 and c.patientType in (64,65,66) " // extrapulmonary bacteriollogically confirmed DRTB cases (others registration group)
                + " group by c.patientType ";
        result = entityManager.createQuery(query).getResultList();
        populateInterfaceRows(result, "EXTRA");

        query = "select c.patientType, count(*) "
                + " from TbCase c join c.patient p "
                + getHQLWhere() + " and c.classification = 1 and c.infectionSite = 0 and c.caseDefinition = 1 and c.patientType in (64,65,66) " // pulmonary clinically diagnosed DRTB cases (others registration group)
                + " group by c.patientType ";
        result = entityManager.createQuery(query).getResultList();
        populateInterfaceRows(result, "PULCLI");

	}

    private void populateInterfaceRows(List<Object[]> data, String superColumn){
        Map<String, String> msgs = Messages.instance();

        for(Object[] o : data){
            PatientType pt = (PatientType)o[0];
            Long qtd = (Long) o[1];

            if(allPatientTypes.contains(pt)) {
                addValue(superColumn + msgs.get(pt.getKey()), msgs.get(pt.getKey()), "singlerow", qtd.floatValue());
                //total column
                addValue("TOTAL", "Total", "singlerow", qtd.floatValue());
            }
        }
    }

    private void initializeInterface(){
        Map<String, String> msgs = Messages.instance();

        for(PatientType pt : superColumn1) {
            addValue("PULBAC" + msgs.get(pt.getKey()), msgs.get(pt.getKey()), "singlerow", new Float(0));
        }

        for(PatientType pt : otherSuperColumns) {
            addValue("PULCLI" + msgs.get(pt.getKey()), msgs.get(pt.getKey()), "singlerow", new Float(0));
        }

        for(PatientType pt : otherSuperColumns) {
            addValue("EXTRA" + msgs.get(pt.getKey()), msgs.get(pt.getKey()), "singlerow", new Float(0));
        }

        for(PatientType pt : otherSuperColumns) {
            addValue("PULBAC" + msgs.get(pt.getKey()), msgs.get(pt.getKey()), "singlerow", new Float(0));
        }

        addValue("TOTAL", "Total", "singlerow", new Float(0));
    }
}
