package org.msh.tb.cases.exams;

import org.msh.tb.entities.Laboratory;
import org.msh.tb.laboratories.LaboratorySelection;

/**
 * Created by rmemoria on 20/8/15.
 */
public class ExamsRequestItem {

    private LaboratorySelection labSelection;

    /**
     * Exams to request
     */
    private boolean reqMicroscopy;
    private boolean reqCulture;
    private boolean reqXpert;
    private boolean reqDst;

    public ExamsRequestItem(int index) {
        super();
        labSelection = new LaboratorySelection("lab" + index);
    }

    public LaboratorySelection getLabSelection() {
        return labSelection;
    }

    public void setLabSelection(LaboratorySelection labSelection) {
        this.labSelection = labSelection;
    }

    public boolean isReqMicroscopy() {
        return reqMicroscopy;
    }

    public void setReqMicroscopy(boolean reqMicroscopy) {
        this.reqMicroscopy = reqMicroscopy;
    }

    public boolean isReqCulture() {
        return reqCulture;
    }

    public void setReqCulture(boolean reqCulture) {
        this.reqCulture = reqCulture;
    }

    public boolean isReqXpert() {
        return reqXpert;
    }

    public void setReqXpert(boolean reqXpert) {
        this.reqXpert = reqXpert;
    }

    public boolean isReqDst() {
        return reqDst;
    }

    public void setReqDst(boolean reqDst) {
        this.reqDst = reqDst;
    }
}
