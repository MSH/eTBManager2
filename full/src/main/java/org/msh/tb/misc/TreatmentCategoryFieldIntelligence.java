package org.msh.tb.misc;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.PrescribedMedicine;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.MedicineLine;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.entities.enums.TreatmentCategory;

import javax.persistence.EntityManager;

/**
 * Created by Mauricio on 01/06/2015.
 */
@Name("treatmentCategoryFieldIntelligence")
public class TreatmentCategoryFieldIntelligence {

    @In
    CaseHome caseHome;
    @In
    EntityManager entityManager;

    /**
     * When tbcase is updated the field treatmentCategory is calculated.
     */
    @Observer("entity.TbCase")
    public void tbCaseUpdated(EntityEvent entityEvent){
        if(entityEvent.getType().equals(EntityEvent.EventType.DELETE) || entityEvent.getType().equals(EntityEvent.EventType.NEW))
            return;

        TbCase tbcase = (TbCase)entityEvent.getEntity();

        if(tbcase.isInitialRegimenWithSecondLineDrugs() == null)
            return;

        updateTreatmentCategoryField(tbcase);

        entityManager.persist(tbcase);
        entityManager.flush();
    }

    /**
     * When tbcase in Bangladesh workspace.
     */
    @Observer("entity.TbCaseBD")
    public void tbCaseBDUpdated(EntityEvent entityEvent){
        tbCaseUpdated(entityEvent);
    }

    /**
     * When tbcase in Nigeria workspace.
     */
    @Observer("entity.TbCaseNG")
    public void tbCaseNGUpdated(EntityEvent entityEvent){
        tbCaseUpdated(entityEvent);
    }

    /**
     * When treatment is started the fields treatmentCategory and initialRegimenWithSecondLineDrugs are calculated.
     */
    @Observer("treatment-started")
    public void treatmentStarted(EntityEvent entityEvent){
        updateInitialRegimenWithSecondLineDrugs(entityEvent);
    }

    /**
     * This method identifies if the initial regimen (the regimen registered when starting the treatment) has second
     * line drugs and stores at a tbcase attribute.
     * @param entityEvent
     */
    private void updateInitialRegimenWithSecondLineDrugs(EntityEvent entityEvent){
        if(entityEvent == null)
            return;

        TbCase tbcase = (TbCase)entityEvent.getEntity();

        if(tbcase == null || tbcase.getPrescribedMedicines() == null || tbcase.getPrescribedMedicines().size() < 1)
            throw new RuntimeException("The started treatment must have at least one prescribed medicine.");

        Boolean hasSecondLineDrugs = false;

        for(PrescribedMedicine pm : tbcase.getPrescribedMedicines()){
            if(MedicineLine.SECOND_LINE.equals(pm.getMedicine().getLine())) {
                hasSecondLineDrugs = true;
                break;
            }
        }

        tbcase.setInitialRegimenWithSecondLineDrugs(hasSecondLineDrugs);

        updateTreatmentCategoryField(tbcase);

        entityManager.persist(tbcase);
        entityManager.flush();
    }

    /**
     * This method calculates the treatment category for the case based on the patienttype and the
     * InitialRegimenWithSecondLineDrugs attributes.
     * @param tbcase
     */
    private void updateTreatmentCategoryField(TbCase tbcase){
        TreatmentCategory category;

        if(tbcase.getPatientType() == null)
            return;

        if(tbcase.isInitialRegimenWithSecondLineDrugs())
            category = TreatmentCategory.SECOND_LINE_TREATMENT_REGIMEN;
        else if(PatientType.NEW.equals(tbcase.getPatientType()) || PatientType.UNKNOWN_PREVIOUS_TB_TREAT.equals(tbcase.getPatientType()))
            category = TreatmentCategory.INITIAL_REGIMEN_FIRST_LINE_DRUGS;
        else
            category = TreatmentCategory.RETREATMENT_FIRST_LINE_DRUGS;

        tbcase.setTreatmentCategory(category);
    }
}
