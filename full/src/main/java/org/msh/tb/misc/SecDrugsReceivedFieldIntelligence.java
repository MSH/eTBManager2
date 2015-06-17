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
 * Created by Mauricio on 16/06/2015.
 */
@Name("secDrugsReceivedFieldIntelligence")
public class SecDrugsReceivedFieldIntelligence {

    @In
    EntityManager entityManager;

    @Observer("entity.TbCase")
    public void tbCaseModifiedIncludedOrRemoved(EntityEvent entityEvent){
        updateCaseDefinitionField(entityEvent);
    }

    /**
     * This method will get the case passed as parameter and check its previous treatments to define id this
     * case have ever used second line drugs.
     * It should be calculated only for DRTB cases.
     * Rule:
     * If at least one substance is second line the answer is yes.
     * If there is prev treat registered but no substances selected the answer is Unknown.
     * If there is no prev treat the answer is no.
     * If there is prev treats with only first line substances, the answer is no.
     */
    private void updateCaseDefinitionField(EntityEvent entityEvent){
        if(entityEvent.getType().equals(EntityEvent.EventType.DELETE) && entityEvent.getEntity() instanceof TbCase)
            return;

        TbCase tbcase = (TbCase) entityEvent.getEntity();

        if(tbcase == null)
            return;

        //Should not be calculated for not DRTB cases.
        if(tbcase.getClassification() == null || !tbcase.getClassification().equals(CaseClassification.DRTB)){
            if(tbcase.getSecDrugsReceived() != null){
                tbcase.setSecDrugsReceived(null);
                entityManager.persist(tbcase);
                entityManager.flush();
            }
            return;
        }

        SecDrugsReceived value = null;

        List<PrevTBTreatment> prevTreats = entityManager.createQuery("from PrevTBTreatment p where p.tbcase.id = :caseId")
                                            .setParameter("caseId", tbcase.getId())
                                            .getResultList();

        // If the case doesn't have prev treat, the answer is no.
        if(prevTreats == null || prevTreats.size() <= 0)
            value = SecDrugsReceived.NO;

        // If the condition before was not attempted test the substances registered. Test for YES or NO.
        if(value == null) {
            for (PrevTBTreatment treat : prevTreats) {
                for (Substance s : treat.getSubstances()) {
                    if (MedicineLine.SECOND_LINE.equals(s.getLine())) {
                        value = SecDrugsReceived.YES;
                        break;
                    }else if(MedicineLine.FIRST_LINE.equals(s.getLine())){
                        value = SecDrugsReceived.NO;
                    }
                }
                if(SecDrugsReceived.YES.equals(value))
                    break;
            }
        }

        //if no condition before was attempted must be UNKNOWN
        if(value == null)
            value = SecDrugsReceived.UNKNOWN;

        if(!value.equals(tbcase.getSecDrugsReceived())) {
            tbcase.setSecDrugsReceived(value);
            entityManager.persist(tbcase);
            entityManager.flush();
        }
    }

}
