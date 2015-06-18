package org.msh.tb.bd;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.msh.tb.bd.entities.TbCaseBD;
import org.msh.tb.bd.entities.enums.SmearStatus;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.ExamMicroscopy;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.Workspace;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.entities.enums.DiagnosisType;
import org.msh.tb.entities.enums.InfectionSite;
import org.msh.tb.entities.enums.MicroscopyResult;
import org.msh.tb.misc.EntityEvent;
import org.msh.utils.date.DateUtils;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * Created by Mauricio on 29/05/2015.
 */
@Name("followUpSmearStatusFieldIntelligence")
public class FollowUpSmearStatusFieldIntelligence {

    @In
    CaseHome caseHome;
    @In
    EntityManager entityManager;

    @Observer("entity.ExamMicroscopy")
    public void examMicroscopyModifiedIncludedOrRemoved(EntityEvent entityEvent){
        updateFollowUpSmearStatusField(entityEvent);
    }

    @Observer("entity.TbCase")
    public void tbCaseModifiedIncludedOrRemoved(EntityEvent entityEvent){
        updateFollowUpSmearStatusField(entityEvent);
    }

    @Observer("entity.TbCaseBD")
    public void tbCaseBDModifiedIncludedOrRemoved(EntityEvent entityEvent){
        updateFollowUpSmearStatusField(entityEvent);
    }

    /**
     * This method will evaluate if the case is smear positive, negative or not evaluated, based on the follow up microscopy exam.
     * @param entityEvent
     */
    private void updateFollowUpSmearStatusField(EntityEvent entityEvent){
        if(entityEvent.getType().equals(EntityEvent.EventType.DELETE) && entityEvent.getEntity() instanceof TbCase)
            return;

        Workspace ws = caseHome.getWorkspace();
        if(!"bd".equals(ws.getExtension()))
            return;

        TbCaseBD tbcase = (TbCaseBD)caseHome.getInstance();

        SmearStatus value = null;

        //Only evaluate Confirmed TB Pulmonary Cases with treatment registered.
        if ( (!DiagnosisType.CONFIRMED.equals(tbcase.getDiagnosisType())) || (!CaseClassification.TB.equals(tbcase.getClassification()))
                || (!InfectionSite.PULMONARY.equals(tbcase.getInfectionSite())) ){
            tbcase.setFollowUpSmearStatus(value);
            entityManager.persist(tbcase);
            entityManager.flush();
            return;
        }

        ExamMicroscopy followUpExam = getFollowUpExam(tbcase);

        if(followUpExam == null || followUpExam.getResult() == null || MicroscopyResult.NOTDONE.equals(followUpExam.getResult())
                || MicroscopyResult.PENDING.equals(followUpExam.getResult())){
            value = SmearStatus.NOT_EVALUATED;
        }else if(followUpExam.getResult().isPositive()){
            value = SmearStatus.SMEAR_POSITIVE;
        }else if(followUpExam.getResult().isNegative()){
            value = SmearStatus.SMEAR_NEGATIVE;
        }

        if(value == null)
            throw new RuntimeException("Value must not be null.");

        tbcase.setFollowUpSmearStatus(value);
        entityManager.persist(tbcase);
        entityManager.flush();

    }

    /**
     * The rule: The exam microscopy that must be considered is the first exam after the final date of intensive phase
     * until seven days after that date.
     * @param tbcase
     * @return the exam microscopy that has to be considered to evaluate the Smear Status
     */
    private ExamMicroscopy getFollowUpExam(TbCase tbcase){
        List<Object> result;

        if(tbcase.getTreatmentPeriod() == null || tbcase.getIniContinuousPhase() == null)
            return null;

        result = entityManager.createQuery(" from ExamMicroscopy e " +
                                            "where e.tbcase.id = :caseId " +
                                            "and e.dateCollected >= :iniDateLimit " +
                                            "and e.dateCollected <= :finalDateLimit " +
                                            "order by e.dateCollected, e.id ")
                .setParameter("caseId", tbcase.getId())
                .setParameter("iniDateLimit", DateUtils.incDays(tbcase.getIniContinuousPhase(), -8))
                .setParameter("finalDateLimit", DateUtils.incDays(tbcase.getIniContinuousPhase(), 7))
                .getResultList();

        if(result != null && result.size()>0)
            return (ExamMicroscopy) result.get(0);

        return null;
    }

}
