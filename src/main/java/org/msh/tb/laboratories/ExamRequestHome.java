package org.msh.tb.laboratories;

import javassist.tools.reflect.Sample;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.cases.exams.ExamCultureHome;
import org.msh.tb.cases.exams.ExamDSTHome;
import org.msh.tb.cases.exams.ExamMicroscopyHome;
import org.msh.tb.cases.exams.ExamXpertHome;
import org.msh.tb.entities.*;

/**
 * Support CRUD operations with the {@link org.msh.tb.entities.ExamRequest} entity
 *
 * Created by ricardo on 18/08/14.
 */
@Name("examRequestHome")
public class ExamRequestHome extends EntityHomeEx<ExamRequest>{

    /**
     * The exam ID in use
     */
    private Integer examId;

    /**
     * Remove the microscopy referred by the exam ID property
     */
    @Transactional
    public void removeMicroscopy() {
        if (examId == null) {
            return;
        }

        ExamMicroscopyHome home = (ExamMicroscopyHome) Component.getInstance("examMicroscopyHome");
        for (ExamMicroscopy exam: getInstance().getExamsMicroscopy()) {
            if (exam.getId().equals(examId)) {
                home.setId(examId);
                home.remove();
                updateSampleRequestList();
            }
        }
    }

    /**
     * Remove a culture exam from the request
     */
    @Transactional
    public void removeCulture() {
        if (examId == null) {
            return;
        }

        ExamCultureHome home = (ExamCultureHome) Component.getInstance("examCultureHome");
        for (ExamCulture exam: getInstance().getExamsCulture()) {
            if (exam.getId().equals(examId)) {
                home.setId(examId);
                home.remove();
                updateSampleRequestList();
            }
        }
    }

    /**
     * Remove a DST exam from the request
     */
    @Transactional
    public void removeDst() {
        if (examId == null) {
            return;
        }

        ExamDSTHome home = (ExamDSTHome) Component.getInstance("examDSTHome");
        for (ExamDST exam: getInstance().getExamsDST()) {
            if (exam.getId().equals(examId)) {
                home.setId(examId);
                home.remove();
                updateSampleRequestList();
            }
        }
    }

    /**
     * Remove an Xpert exam from the request
     */
    @Transactional
    public void removeXpert() {
        if (examId == null) {
            return;
        }

        ExamXpertHome home = (ExamXpertHome) Component.getInstance("examXpertHome");
        for (ExamXpert exam: getInstance().getExamsXpert()) {
            if (exam.getId().equals(examId)) {
                home.setId(examId);
                home.remove();
                updateSampleRequestList();
            }
        }
    }


    private void updateSampleRequestList() {
        SamplesRequestList lst = (SamplesRequestList)Component.getInstance("samplesRequestList");
        if (lst != null) {
            lst.refresh();
        }
    }


    public Integer getExamId() {
        return examId;
    }

    public void setExamId(Integer examId) {
        this.examId = examId;
    }
}
