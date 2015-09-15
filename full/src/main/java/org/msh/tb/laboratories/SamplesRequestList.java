package org.msh.tb.laboratories;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.msh.tb.entities.*;
import org.msh.tb.misc.EntityEvent;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

@Name("samplesRequestList")
public class SamplesRequestList {

	@In ExamRequestHome examRequestHome;
	@In EntityManager entityManager;
	
	List<SampleRequest> samples;
	
	/**
	 * Create the list of samples of the selected case
	 */
	protected void createSampleList() {
        if (!examRequestHome.isManaged()) {
            return;
        }

        samples = new ArrayList<SampleRequest>();

        ExamRequest request = examRequestHome.getInstance();

        fillExams(request.getExamsMicroscopy());
        fillExams(request.getExamsCulture());
        fillExams(request.getExamsDST());
        fillExams(request.getExamsXpert());
	}

    /**
     * Refresh the list
     */
    public void refresh() {
        samples = null;
    }


    /**
     * Fill the list of laboratory exams to the corresponding sample request
     * @param lst list of {@link org.msh.tb.entities.LaboratoryExam} objects
     */
    private void fillExams(List lst) {
        for (Object obj: lst) {
            LaboratoryExam exam = (LaboratoryExam)obj;
            // exam may be just removed
            if (entityManager.contains(exam)) {
                SampleRequest req = findSampleByExam(exam);
                if (exam instanceof ExamMicroscopy) {
                    req.getExamsMicroscopy().add((ExamMicroscopy)exam);
                    continue;
                }

                if (exam instanceof ExamCulture) {
                    req.getExamsCulture().add((ExamCulture)exam);
                    continue;
                }

                if (exam instanceof ExamDST) {
                    req.getExamsDST().add((ExamDST)exam);
                    continue;
                }

                if (exam instanceof ExamXpert) {
                    req.getExamsXpert().add((ExamXpert)exam);
                }
            }
        }
    }

    /**
     * Find sample by laboratory exam using the date collected and the sample number
     * @param exam the laboratory exam to search for the sample request
     * @return instance of {@link org.msh.tb.laboratories.SampleRequest}
     */
    private SampleRequest findSampleByExam(LaboratoryExam exam) {
        for (SampleRequest req: samples) {
            if (exam.isSameSample(req.getDateCollected(), req.getSampleNumber())) {
                return req;
            }
        }
        SampleRequest req = new SampleRequest();
        req.setDateCollected(exam.getDateCollected());
        req.setSampleNumber(exam.getSampleNumber());
        samples.add(req);
        return req;
    }

	/**
	 * Return the list of samples from the selected case
	 * @return
	 */
	public List<SampleRequest> getSamples() {
		if (samples == null)
			createSampleList();
		return samples;
	}

}
