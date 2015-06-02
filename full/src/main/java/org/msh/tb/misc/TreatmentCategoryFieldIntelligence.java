package org.msh.tb.misc;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.*;
import org.msh.tb.entities.enums.*;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * Created by Mauricio on 01/06/2015.
 */
@Name("treatmentCategoryFieldIntelligence")
public class TreatmentCategoryFieldIntelligence {

    @In
    CaseHome caseHome;
    @In
    EntityManager entityManager;

    @Observer("treatment-started")
    public void treatmentStarted(EntityEvent entityEvent){
        updateTreatmentCategoryField(entityEvent);
    }

    /**
     * This method identifies the treatment category and updates it on the tbcase passed as a parameter on the EntityEvent object
     * @param entityEvent
     */
    private void updateTreatmentCategoryField(EntityEvent entityEvent){
        if(entityEvent == null || (!EntityEvent.EventType.NEW.equals(entityEvent.getType())))
            return;

        TbCase tbcase = (TbCase)entityEvent.getEntity();

        if(tbcase == null || tbcase.getPrescribedMedicines() == null || tbcase.getPrescribedMedicines().size() < 1)
            throw new RuntimeException("The started treatment must have at least one prescribed medicine.");

        if(tbcase.getPatientType() == null)
            return;

        tbcase.setTreatmentCategory(checkTreatmentCategory(tbcase));
        entityManager.persist(tbcase);
        entityManager.flush();
    }

    /**
     * This method calculates the treatment category for the tbcase passed as parameter.
     * @param tbcase The tbcase to have the treatment category calculated.
     * @return the Treatment Category of the tbcase passaed as parameter
     */
    private TreatmentCategory checkTreatmentCategory(TbCase tbcase){
        for(PrescribedMedicine pm : tbcase.getPrescribedMedicines()){
            if(MedicineLine.SECOND_LINE.equals(pm.getMedicine().getLine()))
                return TreatmentCategory.SECOND_LINE_TREATMENT_REGIMEN;
        }

        if(PatientType.NEW.equals(tbcase.getPatientType()) || PatientType.UNKNOWN_PREVIOUS_TB_TREAT.equals(tbcase.getPatientType()))
            return TreatmentCategory.INITIAL_REGIMEN_FIRST_LINE_DRUGS;

        return TreatmentCategory.RETREATMENT_FIRST_LINE_DRUGS;
    }
}
