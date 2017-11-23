package org.msh.tb.cases;

import org.jboss.seam.international.Messages;
import org.msh.etbm.commons.transactionlog.ActionTX;
import org.msh.tb.application.App;
import org.msh.tb.entities.*;
import org.msh.tb.entities.enums.RoleAction;

import javax.persistence.EntityManager;

/**
 * This class execute all controls needed round of changing the owner unit, as it is a strong parameter used on
 * Desktop Sync.
 *
 * Created by Mauricio on 04/12/2015.
 */
public class OwnerUnitChecker {

    /**
     * Register a transaction log when the owner unit changes
     * @param tbcase tbcase that the owner unit changed
     * @param oldOwnerUnit the previous owner unit
     * @return the transaction log persisted
     */
    private static TransactionLog registerLog(TbCase tbcase, Tbunit oldOwnerUnit) {
        ActionTX atx = ActionTX.begin("CASE_DATA");

        atx.getDetailWriter().addText(Messages.instance().get("cases.ownerunitchanged"));
        atx.addRow("cases.ownerunitchanged.from", oldOwnerUnit);
        atx.addRow("cases.ownerunitchanged.to", tbcase.getOwnerUnit());

        atx.setEntityClass( TbCase.class.getSimpleName() )
                .setEntityId(tbcase.getId())
                .setEntity(tbcase)
                .setRoleAction(RoleAction.EDIT)
                .setDescription(tbcase.toString())
                .end();

        return atx.getTransactionLog();
    }

    /**
     * This method checks the owner unit and if it has changed, execute many controls needed.
     * @param tbcase the case that will have its owner unit checked
     */
    public static void checkOwnerId(TbCase tbcase){
        Tbunit oldOwnerUnit = tbcase.getOwnerUnit();
        Tbunit newOwnerUnit = selectOwnerUnit(tbcase);

        //check if owner has not changed, in this case, no need to proceed
        if((oldOwnerUnit == null && newOwnerUnit == null) ||
                (oldOwnerUnit != null && newOwnerUnit != null && oldOwnerUnit.getId().equals(newOwnerUnit.getId())) )
            return;

        EntityManager entityManager = App.getEntityManager();

        //If this is a synchronized case it must be deleted from the unit that is synchronizing this case and don't own it anymore.
        App.registerDeletedSyncEntity(tbcase, oldOwnerUnit);

        //Changes owner unit of tbcase and set its client id as null in case it is a synchronized case
        tbcase.setOwnerUnit(newOwnerUnit);
        tbcase.setClientId(null);
        entityManager.persist(tbcase);

        TransactionLog transactionLog = registerLog(tbcase, oldOwnerUnit);

        //Updates related entities
        for(ExamHIV o : tbcase.getResHIV())
            updateRelatedObjects(o, transactionLog);
        for(ExamXRay o : tbcase.getResXRay())
            updateRelatedObjects(o, transactionLog);
        for(TbContact o : tbcase.getContacts())
            updateRelatedObjects(o, transactionLog);
        for(CaseSideEffect o : tbcase.getSideEffects())
            updateRelatedObjects(o, transactionLog);
        for(MedicalExamination o : tbcase.getExaminations())
            updateRelatedObjects(o, transactionLog);

        for(LaboratoryExam o : tbcase.getAllLaboratoryExams())
            updateRelatedObjects(o, transactionLog);
    }

    /**
     * The owner unit of a tbcase is the last healthunit, for the cases that has treatment registered, or the
     * notification unit, for the cases that doesn't have treatment registered.
     * @param tbcase tbcase that will have its owner unit searched.
     * @return the owner unit of the tbcase passed as parameter
     */
    public static Tbunit selectOwnerUnit(TbCase tbcase){
        if(tbcase.getHealthUnits() != null && tbcase.getHealthUnits().size() > 0){
            TreatmentHealthUnit ownerUnit = null;
            for(TreatmentHealthUnit h : tbcase.getHealthUnits()){
                if(ownerUnit == null) {
                    ownerUnit = h;
                }else{
                    if(h != null && ownerUnit != null && ownerUnit.getPeriod().getEndDate().before(h.getPeriod().getEndDate())){
                        ownerUnit = h;
                    }
                }
            }
            return ownerUnit.getTbunit();
        }

        return tbcase.getNotificationUnit();
    }

    /**
     * Updates the last transaction log on the related entities of that case, so, if this case is now owned by a
     * tbunit that uses Desktop module all entities related to that case (exams, aditional information...) will be
     * sent to that desktop instance. Also the clientId has to be cleared.
     * @param o - transactional syncKey object to be updated
     * @param transactionLog - TransactionLog generated when changing the owner unit of a case
     */
    private static void updateRelatedObjects(Object o, TransactionLog transactionLog) {
        if (o == null || transactionLog == null || transactionLog.getId() == null)
            return;

        if (o instanceof Transactional) {
            ((Transactional) o).setLastTransaction(transactionLog);
        }

        if (o instanceof SyncKey) {
            ((SyncKey) o).setClientId(null);
        }

        App.getEntityManager().persist(o);
        App.getEntityManager().flush();
    }
}
