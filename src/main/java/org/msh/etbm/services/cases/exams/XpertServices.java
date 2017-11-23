package org.msh.etbm.services.cases.exams;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.ExamXpert;
import org.msh.tb.entities.enums.XpertResult;
import org.msh.tb.resistpattern.ResistancePatternService;
import org.msh.validators.MessagesList;

/**
 * Expose services to handle xpert exams
 *
 * Created by rmemoria on 10/4/15.
 */
@Name("xpertServices")
public class XpertServices extends LabExamServices<ExamXpert> {

    @Override
    public MessagesList validate(ExamXpert exam) {
        MessagesList lst = super.validate(exam);

        // rif result is required if the result was TB DETECTED
        if ((exam.getResult() == XpertResult.TB_DETECTED) && (exam.getRifResult() == null)) {
            lst.addRequired("rifResult");
        }

        return lst;
    }


    @Override
    public MessagesList save(ExamXpert exam) {
        // if value is different from TB DETECTED, the rif result must be null
        if (exam.getResult() != XpertResult.TB_DETECTED) {
            exam.setRifResult(null);
        }

        MessagesList lst = super.save(exam);

        if (lst == null) {
            // update resistance pattern
            ResistancePatternService srv = (ResistancePatternService) Component.getInstance("resistancePatternService");
            srv.updateCase(exam.getTbcase());
        }

        return lst;
    }


    @Override
    protected void afterSaved(ExamXpert exam) {
        super.afterSaved(exam);
        updateResistancePattern(exam);
    }


    @Override
    protected void afterDeleted(ExamXpert exam) {
        super.afterDeleted(exam);
        updateResistancePattern(exam);
    }


    /**
     * Update information about resistance patterns of the case
     */
    protected void updateResistancePattern(ExamXpert exam) {
        ResistancePatternService srv = (ResistancePatternService) Component.getInstance("resistancePatternService");
        srv.updateCase(exam.getTbcase());
    }
}
