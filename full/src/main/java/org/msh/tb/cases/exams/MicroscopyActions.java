package org.msh.tb.cases.exams;

import org.jboss.seam.annotations.Name;
import org.msh.etbm.commons.transactionlog.mapping.LogInfo;
import org.msh.tb.entities.ExamMicroscopy;
import org.msh.validators.FacesMessagesBinder;

import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.List;

/**
 * Default action for microscopy exams, to be used by the JSF components
 *
 * Created by rmemoria on 6/4/15.
 */
@Name("microscopyActions")
@LogInfo(roleName="EXAM_MICROSC", entityClass=ExamMicroscopy.class)
public class MicroscopyActions extends LabExamActions<ExamMicroscopy> {

    private List<SelectItem> optionsNumSamples;

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


    /**
     * Return the options for the number of samples collected
     * @return List of options to be displayed in a drop down box
     */
    public List<SelectItem> getOptionsNumSamples() {
        if (optionsNumSamples == null) {
            optionsNumSamples = new ArrayList<SelectItem>();

            optionsNumSamples.add(new SelectItem(null, "-"));
            optionsNumSamples.add(new SelectItem(1, "1"));
            optionsNumSamples.add(new SelectItem(2, "2"));
        }

        return optionsNumSamples;
    }

}
