package org.msh.tb.cases.exams;

import org.jboss.seam.annotations.Name;
import org.msh.etbm.commons.transactionlog.mapping.LogInfo;
import org.msh.tb.entities.ExamMicroscopy;
import org.msh.validators.FacesMessagesBinder;

/**
 * Default action for microscopy exams, to be used by the JSF components
 *
 * Created by rmemoria on 6/4/15.
 */
@Name("microscopyActions")
@LogInfo(roleName="EXAM_MICROSC", entityClass=ExamMicroscopy.class)
public class MicroscopyActions extends LabExamActions<ExamMicroscopy> {

    @Override
    public String getControlPrefix() {
        return "mic";
    }


    /**
     * Bind fields to UI components
     * @return
     */
    public FacesMessagesBinder bindFields() {
        return super.bindFields()
                .bind("micres", "result")
                .bind("micafb", "numberOfAFB");
    }


}
