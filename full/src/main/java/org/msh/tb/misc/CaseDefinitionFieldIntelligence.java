package org.msh.tb.misc;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.*;
import org.msh.tb.entities.enums.CaseDefinition;
import org.msh.tb.entities.enums.DiagnosisType;
import org.msh.tb.entities.enums.XpertResult;
import org.msh.utils.date.DateUtils;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * Created by Mauricio on 08/05/2015.
 */
@Name("caseDefinitionFieldIntelligence")
public class CaseDefinitionFieldIntelligence {

    @In
    CaseHome caseHome;
    @In
    EntityManager entityManager;

    @Observer("entity.ExamMicroscopy")
    public void examMicroscopyModifiedIncludedOrRemoved(EntityEvent entityEvent){ updateCaseDefinitionField(entityEvent);}

    @Observer("entity.ExamCulture")
    public void examCultureModifiedIncludedOrRemoved(EntityEvent entityEvent){ updateCaseDefinitionField(entityEvent);}

    @Observer("entity.ExamXpert")
    public void examXpertModifiedIncludedOrRemoved(EntityEvent entityEvent){
        updateCaseDefinitionField(entityEvent);
    }

    @Observer("entity.TbCase")
    public void tbCaseModifiedIncludedOrRemoved(EntityEvent entityEvent){
        updateCaseDefinitionField(entityEvent);
    }

    @Observer("entity.TbCaseBD")
    public void tbCaseBDModifiedIncludedOrRemoved(EntityEvent entityEvent){
        updateCaseDefinitionField(entityEvent);
    }

    @Observer("entity.TbCaseNG")
    public void tbCaseNGModifiedIncludedOrRemoved(EntityEvent entityEvent){
        updateCaseDefinitionField(entityEvent);
    }

    /**
     * This method will get the case on caseHome and check its parameter to define if the case definiciton
     * for this field is Bacteriologically Confirmed or Clinically Diagnosed.
     * The details about the logic used are on the comments inside the method.
     */
    private void updateCaseDefinitionField(EntityEvent entityEvent){
        if(entityEvent.getType().equals(EntityEvent.EventType.DELETE) && entityEvent.getEntity() instanceof TbCase)
            return;

        // if there is no case, so just return
        if (!caseHome.isManaged()) {
            return;
        }

        TbCase tbcase = caseHome.getInstance();
        if(tbcase == null)
            return;

        CaseDefinition value = null;

        //If it is a suspect (presumptive), the case is not a confirmed case, so, no case definition for  this case.
        if (tbcase.getDiagnosisType() == null || tbcase.getDiagnosisType().equals(DiagnosisType.SUSPECT)) {
            tbcase.setCaseDefinition(value);
            entityManager.persist(tbcase);
            entityManager.flush();
            return;
        }

        value = CaseDefinition.CLINICALLY_DIAGNOSED;

        //Check if positive on microscopy exams
        if (isAnyExamPositive(getExams(tbcase, "ExamMicroscopy")))
            value = CaseDefinition.BACTERIOLOGICALLY_CONFIRMED;

        if (value.equals(CaseDefinition.CLINICALLY_DIAGNOSED)) {
            //Check if positive on culture exams
            if (isAnyExamPositive(getExams(tbcase, "ExamCulture")))
                value = CaseDefinition.BACTERIOLOGICALLY_CONFIRMED;
        }

        if (value.equals(CaseDefinition.CLINICALLY_DIAGNOSED)) {
            //Check if positive on xpert exams
            if (isAnyExamPositive(getExams(tbcase, "ExamXpert")))
                value = CaseDefinition.BACTERIOLOGICALLY_CONFIRMED;
        }

        if(!value.equals(tbcase.getCaseDefinition())) {
            tbcase.setCaseDefinition(value);
            entityManager.persist(tbcase);
            entityManager.flush();
        }

    }

    /**
     * This method will return exams according to the following condition:
     * 1- If the case has treatment registered it will return all exams until 8 days before the end start of continuous phase of treatment.
     * 2- If the case doesn't have treatment registered it will return all exams registered.
     * @param tbcase - The Tbcase that is beeing verificated
     * @param examEntityName - the name of the Exam Entity that will be searched.
     * @return The all exams of a type according to the conditions on the description of that method.
     */
    private List<LaboratoryExam> getExams(TbCase tbcase, String examEntityName){
        List<LaboratoryExam> list = null;

        if(tbcase.getTreatmentPeriod()==null || tbcase.getTreatmentPeriod().getIniDate() == null){
            list = (List<LaboratoryExam>) entityManager.createQuery(" from "+examEntityName+" e " +
                    " where e.tbcase.id = :caseId and e.result is not null " +
                    " order by e.dateCollected desc, e.id desc ")
                    .setParameter("caseId", tbcase.getId())
                    .getResultList();
        }else{
            list = (List<LaboratoryExam>) entityManager.createQuery(" from " + examEntityName + " e " +
                    " where e.tbcase.id = :caseId and e.dateCollected <= :date and e.result is not null  " +
                    " order by e.dateCollected desc, e.id desc ")
                    .setParameter("caseId", tbcase.getId())
                    .setParameter("date", DateUtils.incDays(tbcase.getIniContinuousPhase(),-9))
                    .getResultList();
        }

        return list;
    }

    /**
     *
     * @param list list of exams to be checked
     * @return true if at least one exam on list is positive
     */
    private boolean isAnyExamPositive(List<LaboratoryExam> list){
        if(list == null)
            return false;

        for(LaboratoryExam exam : list){
            if(exam instanceof ExamMicroscopy){
                ExamMicroscopy examMic = (ExamMicroscopy) exam;
                if(examMic.getResult() != null && examMic.getResult().isPositive())
                    return true;
            }else if(exam instanceof ExamCulture){
                ExamCulture examCul = (ExamCulture) exam;
                if(examCul.getResult() != null && examCul.getResult().isPositive())
                    return true;
            }else if(exam instanceof ExamXpert){
                ExamXpert examX = (ExamXpert) exam;
                if(examX.getResult() != null && examX.getResult().equals(XpertResult.TB_DETECTED))
                    return true;
            }
        }

        return false;
    }

}
