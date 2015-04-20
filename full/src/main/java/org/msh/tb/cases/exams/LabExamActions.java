package org.msh.tb.cases.exams;

import org.msh.etbm.services.cases.exams.LabExamServices;
import org.msh.tb.application.App;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.LaboratoryExam;
import org.msh.tb.laboratories.LaboratorySelection;
import org.msh.tb.misc.EntityActions;
import org.msh.validators.FacesMessagesBinder;

/**
 * Created by rmemoria on 10/4/15.
 */
public abstract class LabExamActions<E extends LaboratoryExam> extends EntityActions<E> {

    private LaboratorySelection labselection;

    /**
     * Initialize the form. Manually called by the *.page.xhtml
     */
    public void init() {
        LaboratoryExam exam = getInstance();

        if (labselection == null) {
            getLabselection().setSelected(exam.getLaboratory());
        }
    }

    /**
     * Return the prefix used in the JSF controls
     * @return
     */
    public abstract String getControlPrefix();

    @Override
    public boolean validate() {
        if (labselection != null) {
            getInstance().setLaboratory(labselection.getSelected());
        }
        return super.validate();
    }

    /**
     * Save the content of the form
     * @return
     */
    @Override
    public String save() {
        LaboratoryExam exam = getInstance();

        if (exam.getTbcase() == null) {
            CaseHome caseHome = (CaseHome) App.getComponent("caseHome");
            exam.setTbcase(caseHome.getInstance());
        }

        if (labselection != null) {
            exam.setLaboratory(labselection.getLaboratory());
        }

        return super.save();
    }


    /**
     * Bind fields to UI components
     * @return
     */
    public FacesMessagesBinder bindFields() {
        String p = getControlPrefix();

        FacesMessagesBinder binder = new FacesMessagesBinder();
        binder.bind(p + "dtcollected", "dateCollected")
                .bind(p + "lab", "laboratory")
                .bind(p + "sample", "sampleNumber")
                .bind(p + "dtrelease", "dateRelease")
                .bind(p + "met", "method")
                .bind(p + "comments", "comments");
        return binder;
    }


    /**
     * Return object for laboratory selection
     * @return
     */
    public LaboratorySelection getLabselection() {
        if (labselection == null) {
            String pre = getControlPrefix();
            labselection = new LaboratorySelection(pre + "labid");
        }
        return labselection;
    }
}
