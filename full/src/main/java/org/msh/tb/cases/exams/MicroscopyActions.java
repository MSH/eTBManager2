package org.msh.tb.cases.exams;

import org.jboss.seam.annotations.Name;
import org.msh.etbm.services.commons.DAOServices;
import org.msh.tb.application.App;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.ExamMicroscopy;
import org.msh.tb.laboratories.LaboratorySelection;
import org.msh.tb.misc.EntityActions;
import org.msh.validators.FacesMessagesBinder;
import org.msh.validators.MessagesList;

/**
 * Default action for microscopy exams, to be used by the JSF components
 *
 * Created by rmemoria on 6/4/15.
 */
@Name("microscopyActions")
public class MicroscopyActions extends EntityActions<ExamMicroscopy> {

    private LaboratorySelection labselection;


    /**
     * Save the content of the form
     * @return
     */
    public String save() {
        ExamMicroscopy exam = getInstance();

        if (exam.getTbcase() == null) {
            CaseHome caseHome = (CaseHome) App.getComponent("caseHome");
            exam.setTbcase(caseHome.getInstance());
        }

        if (labselection != null) {
            exam.setLaboratory(labselection.getLaboratory());
        }

        DAOServices examsrv = getServices();

        // save the data
        MessagesList lst = examsrv.save(getInstance());

        // check if there is  any validation message
        if (lst != null) {
            bindFields().publish(lst.getMessages());
        }

        return "persisted";
    }


    /**
     * Bind fields to UI components
     * @return
     */
    public FacesMessagesBinder bindFields() {
        FacesMessagesBinder binder = new FacesMessagesBinder();
        binder.bind("micdtcollected", "dateCollected")
                .bind("midlab", "laboratory")
                .bind("micsample", "sampleNumber")
                .bind("micdtrelease", "dateRelease")
                .bind("micva", "visualAppearance")
                .bind("micafb", "numberOfAFB")
                .bind("miccomments", "comments");
        return binder;
    }


    /**
     * Return object for laboratory selection
     * @return
     */
    public LaboratorySelection getLabselection() {
        if (labselection == null) {
            labselection = new LaboratorySelection("miclabid");
        }
        return labselection;
    }

}
