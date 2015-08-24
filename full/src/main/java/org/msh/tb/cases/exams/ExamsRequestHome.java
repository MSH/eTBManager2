package org.msh.tb.cases.exams;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.Transactional;
import org.msh.tb.ETB;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.*;
import org.msh.tb.entities.enums.ExamStatus;
import org.msh.tb.laboratories.ExamRequestHome;
import org.msh.tb.login.UserSession;

import javax.persistence.EntityManager;
import java.util.Date;

/**
 * Handle laboratory exam requests from the case module
 * Created by rmemoria on 20/8/15.
 */
@Name("examsRequestHome")
public class ExamsRequestHome {

    @In
    EntityManager entityManager;

    @In(create=true)
    ExamRequestHome examRequestHome;

    @In
    CaseHome caseHome;


    /**
     * Number of samples being requested
     */
    private int numSamples;

    /**
     * Date when the sample was collected
     */
    private Date dateCollected;

    /**
     * Requests to be done
     */
    // for the future
    //private List<ExamsRequestItem> requests = new ArrayList<ExamsRequestItem>();
    private ExamsRequestItem item;

    private int counter;


    /**
     * Request exams
     * @return
     */
    @Transactional
    public String request() {
        TbCase tbcase = caseHome.getInstance();

        Laboratory lab = getItem().getLabSelection().getLaboratory();
        ExamRequest req = examRequestHome.getInstance();
        req.setTbunit(tbcase.getOwnerUnit());
        req.setLaboratory(lab);
        req.setRegisteredBy(ExamRequest.RegisteredBy.HEALTH_FACILITY);
        req.setRequestDate(new Date());
        req.setTbcase(tbcase);
        req.setUser(UserSession.getUser());

        createRequest(tbcase, item);

        entityManager.persist(tbcase);

        return "exams-requested";
    }


    protected void createRequest(TbCase tbcase, ExamsRequestItem item) {
        ExamRequest req = examRequestHome.getInstance();

        req.setTbunit(tbcase.getOwnerUnit());

        // add microscopy?
        if (item.isReqMicroscopy()) {
            ExamMicroscopy exam = ETB.newWorkspaceObject(ExamMicroscopy.class);
            prepareExam(exam, req, item.getLabSelection().getLaboratory());
            tbcase.getExamsMicroscopy().add(exam);
            entityManager.persist(exam);
            req.getExamsMicroscopy().add(exam);
        }

        // add culture?
        if (item.isReqCulture()) {
            ExamCulture cult = ETB.newWorkspaceObject(ExamCulture.class);
            prepareExam(cult, req, item.getLabSelection().getLaboratory());
            tbcase.getExamsCulture().add(cult);
            entityManager.persist(cult);
            req.getExamsCulture().add(cult);
        }

        // add DST?
        if (item.isReqDst()) {
            ExamDST exam = ETB.newWorkspaceObject(ExamDST.class);
            prepareExam(exam, req, item.getLabSelection().getLaboratory());
            tbcase.getExamsDST().add(exam);
            entityManager.persist(exam);
            req.getExamsDST().add(exam);
        }

        // add xpert?
        if (item.isReqXpert()) {
            ExamXpert exam = ETB.newWorkspaceObject(ExamXpert.class);
            prepareExam(exam, req, item.getLabSelection().getLaboratory());
            tbcase.getExamsXpert().add(exam);
            entityManager.persist(exam);
            req.getExamsXpert().add(exam);
        }

        examRequestHome.setDisplayMessage(false);
        examRequestHome.persist();
    }

    /**
     * Initialize variables of an exam
     * @param exam
     */
    protected void prepareExam(LaboratoryExam exam, ExamRequest request, Laboratory laboratory) {
        exam.setTbcase(caseHome.getTbCase());
        exam.setLaboratory(laboratory);
        exam.setDateCollected(dateCollected);
        exam.setRequest(request);
        exam.setStatus(ExamStatus.REQUESTED);
    }


    @Transactional
    public void cancelrequest() {
        //
    }

    public int getNumSamples() {
        return numSamples;
    }

    public void setNumSamples(int numSamples) {
        this.numSamples = numSamples;
    }

    public Date getDateCollected() {
        return dateCollected;
    }

    public void setDateCollected(Date dateCollected) {
        this.dateCollected = dateCollected;
    }

    public ExamsRequestItem getItem() {
        if (item == null) {
            item = new ExamsRequestItem(1);
        }
        return item;
    }
}
