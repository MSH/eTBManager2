package org.msh.tb.ng;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.application.App;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.treatment.StartTreatmentHome;
import org.msh.tb.entities.Regimen;
import org.msh.tb.entities.Tbunit;

import java.util.Date;

/**
 * Simple class to start the treatment from the treatment tab in the detail page of a TB case.
 *
 * Created by rmemoria on 23/4/15.
 */
@Name("startTreatmentNG")
@BypassInterceptors
public class StartTreatmentNG {

    private Date startDate;
    private Regimen regimen;

    /**
     * Start the treatment based on the start date and selected regimen
     * @return "treatment-started" string, if successfully started, otherwise, an exception is thrown
     */
    public String startTreatment() {
        if ((startDate == null) || (regimen == null)) {
            throw new RuntimeException("Start date and regimen must be informed");
        }

        CaseHome caseHome = (CaseHome) App.getComponent("caseHome");

        if ((caseHome == null) || (!caseHome.isManaged())) {
            throw new RuntimeException("No case informed");
        }

        Tbunit unit = caseHome.getInstance().getOwnerUnit();

        StartTreatmentHome serv = (StartTreatmentHome) App.getComponent("startTreatmentHome");
        serv.setIniTreatmentDate(startDate);
        serv.setRegimen(regimen);
        serv.getTbunitselection().setSelected(unit);
        return serv.startStandardRegimen();
    }

    /**
     * Return the start date of the treatment
     * @return
     */
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Regimen getRegimen() {
        return regimen;
    }

    public void setRegimen(Regimen regimen) {
        this.regimen = regimen;
    }
}
