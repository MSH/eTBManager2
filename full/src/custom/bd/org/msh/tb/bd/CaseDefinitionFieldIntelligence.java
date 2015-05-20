package org.msh.tb.bd;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.*;
import org.msh.tb.entities.enums.CaseDefinition;
import org.msh.tb.entities.enums.DiagnosisType;
import org.msh.tb.entities.enums.XpertResult;
import org.msh.tb.misc.EntityEvent;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Date;
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
    public void examMicroscopyModifiedIncludedOrRemoved(EntityEvent entityEvent){
        updateCaseDefinitionField(entityEvent);
    }

    @Observer("entity.ExamCulture")
    public void examCultureModifiedIncludedOrRemoved(EntityEvent entityEvent){
        updateCaseDefinitionField(entityEvent);
    }

    @Observer("entity.ExamXpert")
    public void examXpertModifiedIncludedOrRemoved(EntityEvent entityEvent){
        updateCaseDefinitionField(entityEvent);
    }

    /**
     * This method will get the case on caseHome and check its parameter to define if the case definiciton
     * for this field is Bacteriologically Confirmed or Clinically Diagnosed.
     * The details about the logic used are on the comments inside the method.
     */
    private void updateCaseDefinitionField(EntityEvent entityEvent){
        LaboratoryExam exam = (LaboratoryExam) entityEvent.getEntity();
        TbCase tbcase = caseHome.getInstance();
        Workspace ws = tbcase.getPatient().getWorkspace();

        CaseDefinition value = null;

        //If it is a suspect (presumptive), the case is not confirmed as a TB or DRTB or NMT case, so, no case definition for  this case.
        if (tbcase.getDiagnosisType().equals(DiagnosisType.SUSPECT)) {
            tbcase.setCaseDefinition(value);
            entityManager.persist(tbcase);
            entityManager.flush();
            return;
        }

        value = CaseDefinition.CLINICALLY_DIAGNOSED;

        //Check if positive on microscopy exam
        ExamMicroscopy examMic = (ExamMicroscopy) getLastExam(tbcase, "ExamMicroscopy");
        if (examMic != null && examMic.getResult() != null && examMic.getResult().isPositive())
            value = CaseDefinition.BACTERIOLOGICALLY_CONFIRMED;

        if (value.equals(CaseDefinition.CLINICALLY_DIAGNOSED)) {
            //Check if positive on culture exam
            ExamCulture examCul = (ExamCulture) getLastExam(tbcase, "ExamCulture");
            if (examCul != null && examCul.getResult() != null && examCul.getResult().isPositive())
                value = CaseDefinition.BACTERIOLOGICALLY_CONFIRMED;
        }

        if (value.equals(CaseDefinition.CLINICALLY_DIAGNOSED)) {
            //Check if positive on culture exam
            ExamXpert examX = (ExamXpert) getLastExam(tbcase, "ExamXpert");
            if (examX != null && examX.getResult() != null && examX.getResult().equals(XpertResult.TB_DETECTED))
                value = CaseDefinition.BACTERIOLOGICALLY_CONFIRMED;
        }

        if(!value.equals(tbcase.getCaseDefinition())) {
            tbcase.setCaseDefinition(value);
            entityManager.persist(tbcase);
            entityManager.flush();
            System.out.println("heyhey");
        }

    }

    /**
     * This method will return the last exam according to the following condition:
     * 1- If the case has treatment registered it will return the last exam before the treatment considering the date collected.
     * 2- If the case doesn't have treatment registered it will return the last exam registered considering the date collected.
     * @param tbcase - The Tbcase that is beeing verificated
     * @param examEntityName - the name of the Exam Entity that will be searched.
     * @return The last exam according to the conditions on the description of that method.
     */
    private LaboratoryExam getLastExam(TbCase tbcase, String examEntityName){
        LaboratoryExam result = null;
        List<LaboratoryExam> list = null;

        if(tbcase.getTreatmentPeriod()==null || tbcase.getTreatmentPeriod().getIniDate() == null){
            list = (List<LaboratoryExam>) entityManager.createQuery(" from "+examEntityName+" e " +
                    " where e.tbcase.id = :caseId and e.result is not null " +
                    " order by e.dateCollected desc, e.id desc ")
                    .setParameter("caseId", tbcase.getId())
                    .getResultList();

            if(list != null && list.size() > 0)
                result = list.get(0);
        }else{
            list = (List<LaboratoryExam>) entityManager.createQuery(" from " + examEntityName + " e " +
                    " where e.tbcase.id = :caseId and e.dateCollected < :iniTreatDate and e.result is not null  " +
                    " order by e.dateCollected desc, e.id desc ")
                    .setParameter("caseId", tbcase.getId())
                    .setParameter("iniTreatDate", tbcase.getTreatmentPeriod().getIniDate())
                    .getResultList();

            if(list != null && list.size() > 0)
                result = list.get(0);
        }

        return result;
    }

}
