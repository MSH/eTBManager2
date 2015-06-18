package org.msh.tb.ng.cases;


import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.msh.tb.application.App;
import org.msh.tb.cases.CaseEditingHome;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.PatientHome;
import org.msh.tb.cases.exams.CultureActions;
import org.msh.tb.cases.exams.MicroscopyActions;
import org.msh.tb.cases.exams.XpertActions;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.*;
import org.msh.tb.ng.entities.TbCaseNG;
import org.msh.tb.ng.entities.enums.HIVPosition;
import org.msh.validators.BeanValidator;
import org.msh.validators.FacesMessagesBinder;
import org.msh.validators.MessagesList;

import java.util.Date;


/**
 * Handle TB and DR-TB cases editing and new notification
 *
 * @author Mauricio Santos
 */
@Name("caseEditingHomeNg")
@Scope(ScopeType.CONVERSATION)
public class CaseEditingHomeNg {

    @In(create = true) MicroscopyActions microscopyActions;
    @In(create = true) XpertActions xpertActions;
    @In(create = true) CultureActions cultureActions;
    @In(create = true) CaseEditingHome caseEditingHome;
    @In CaseHome caseHome;
    @In FacesMessages facesMessages;

    private boolean initialized;


    /**
     * Initialize the caseHome object for editing
     */
    public void initializeEditing() {
        if (initialized) {
            return;
        }

        TbCase tbcase = caseHome.getInstance();

        if (caseEditingHome.getTbunitselection().getSelected() == null)
            caseEditingHome.getTbunitselection().setSelected(tbcase.getNotificationUnit());

        if (caseEditingHome.getNotifAdminUnit().getSelectedUnit() == null)
            caseEditingHome.getNotifAdminUnit().setSelectedUnit(tbcase.getNotifAddress().getAdminUnit());

        if (caseEditingHome.getCurrentAdminUnit().getSelectedUnit() == null)
            caseEditingHome.getCurrentAdminUnit().setSelectedUnit(tbcase.getCurrentAddress().getAdminUnit());

        caseEditingHome.updatePatientAge();

        initialized = true;
    }


    /*
    * Validates data, checking required fields and other rules
    */
    protected boolean validate(){
        TbCaseNG tbcase = (TbCaseNG)caseHome.getInstance();

        boolean valid = validateExams();

        MessagesList lst = BeanValidator.validate(tbcase);

        // check age manually
        if (tbcase.getAge() == null) {
            lst.addRequired("age");
        }

        // check HIV position only if case is a suspect or a TB case
        if ((tbcase.getDiagnosisType() == DiagnosisType.SUSPECT) || (tbcase.getClassification() == CaseClassification.TB)) {
            // check HIV result
            if (tbcase.getHivPosition() == null) {
                lst.addRequired("hivPosition");
            }
            else {
                if ((tbcase.getHivPosition() != HIVPosition.UNKNOWN) && (tbcase.getHivPositionDetail() == null)) {
                    lst.addRequired("hivPositionDetail");
                }
            }
        }

        if (tbcase.getNotificationUnit() == null) {
            lst.addRequired("notificationUnit");
        }

        // is TB Confirmed ?
        if ((tbcase.getClassification() == CaseClassification.TB) && (tbcase.getDiagnosisType() == DiagnosisType.CONFIRMED)) {
            validateTBspecific(tbcase, lst);
        }

        valid = valid && lst.size() == 0;

        // publish validation errors on the form
        if (lst.size() > 0) {
            createBinder().publish(lst);
        }

        return valid;
    }

    /**
     * Validate the address
     * @param tbcase
     * @param msgs
     */
    protected void validateAddr(TbCaseNG tbcase, MessagesList msgs) {
        if (tbcase.getNotifAddress() == null) {
            msgs.addRequired("notifAddress");
            return;
        }

        String s = tbcase.getNotifAddress().getAddress();
        if ((s == null) || (s.trim().isEmpty())) {
            msgs.addRequired("notifAddress.address");
        }
/*
        s = tbcase.getNotifAddress().getZipCode();
        if ((s == null) || (s.trim().isEmpty())) {
            msgs.addRequired("zipCode");
        }
*/

        if (tbcase.getNotifAddress().getAdminUnit() == null) {
            msgs.addRequired("notifAddress.adminUnit");
        }
    }

    /**
     * Validate specific fields for TB case
     * @param tbcase
     * @param msgs
     */
    protected void validateTBspecific(TbCaseNG tbcase, MessagesList msgs) {
        if (tbcase.getIntakeAntiTBDrugs() == null) {
            msgs.addRequired("intakeAntiTBDrugs");
        }
        else {
            if ((tbcase.getIntakeAntiTBDrugs() == YesNoType.YES) && (tbcase.getIntakeAntiTBDrugsDuration() == null)) {
                msgs.addRequired("intakeAntiTBDrugsDuration");
            }
        }
        validateAddr(tbcase, msgs);
    }

    /**
     * Create object that will bind field messages to controls
     * @return
     */
    protected FacesMessagesBinder createBinder() {
        FacesMessagesBinder caseBinder = new FacesMessagesBinder();
        caseBinder.bind("pacname", "patient.name")
                .bind("edtsecnumber", "patient.securityNumber")
                .bind("edtage", "age")
                .bind("edtgender", "patient.gender")
                .bind("edtregdate", "registrationDate")
                .bind("cbnotifunit", "notificationUnit")
                .bind("edtaddr", "notifAddress")
                .bind("edtaddr", "notifAddress.address")
                .bind("edtzip", "notifAddress.zipCode")
                .bind("edtphone", "edtmob")
                .bind("edtoccupation", "occupation")
                .bind("edtsr", "sourceReferral")
                .bind("edtmaritalstatus", "maritalStatus")
                .bind("hivresult", "hivPosition")
                .bind("hivdetail", "hivPositionDetail")
                .bind("edtint", "intakeAntiTBDrugs")
                .bind("edtintduration", "intakeAntiTBDrugsDuration");

        return caseBinder;
    }



    /**
     * Prepare a new case (confirmed or suspect) to be saved
     */
    private void prepareNew() {
        TbCaseNG tbcase = (TbCaseNG)caseHome.getInstance();

        tbcase.setNotificationUnit(caseEditingHome.getTbunitselection().getSelected());
        tbcase.getNotifAddress().setAdminUnit(caseEditingHome.getTbunitselection().getAuselection().getSelectedUnit());
        tbcase.setOwnerUnit(tbcase.getNotificationUnit());

        if (tbcase.getRegistrationDate() == null) {
            tbcase.setRegistrationDate( new Date() );
        }

        if (tbcase.getValidationState() == null)
            tbcase.setValidationState(ValidationState.WAITING_VALIDATION);

        if (tbcase.getState() == null)
            tbcase.setState(CaseState.WAITING_TREATMENT);

        tbcase.getPatient().setWorkspace(caseHome.getWorkspace());
        caseEditingHome.updatePatientAge();

        // set the initial classification of the suspect (won't change in case of confirmed cases that were not suspects)
        tbcase.setSuspectClassification(tbcase.getClassification());
    }


    /**
     * Save changes made to a case
     * @return
     */
    public String saveEditingSuspect() {
        if (!validate())
            return "error";

        TbCaseNG tbcase = (TbCaseNG) caseHome.getInstance();

        tbcase.setNotificationUnit(caseEditingHome.getTbunitselection().getSelected());
        tbcase.getNotifAddress().setAdminUnit(caseEditingHome.getTbunitselection().getAuselection().getSelectedUnit());

        //fix the inconsistence when owner unit is null.
        if(tbcase.getOwnerUnit() == null){
            caseEditingHome.updateOwnerUnit(tbcase);
        }

        caseEditingHome.updatePatientAge();

        String s = caseHome.persist();

        if ("persisted".equals(s))
            caseHome.updateCaseTags();

        return s;
    }


    /**
     * Save changes made to a case
     * @return
     */
    public String saveEditingTbCase() {
        if (!validate())
            return "error";
        //TODO
        TbCaseNG tbcase = (TbCaseNG) caseHome.getInstance();

        tbcase.setNotificationUnit(caseEditingHome.getTbunitselection().getSelected());
        tbcase.getNotifAddress().setAdminUnit(caseEditingHome.getTbunitselection().getAuselection().getSelectedUnit());

        //fix the inconsistence when owner unit is null.
        if(tbcase.getOwnerUnit() == null){
            caseEditingHome.updateOwnerUnit(tbcase);
        }

        caseEditingHome.updatePatientAge();

        String s = caseHome.persist();

        if ("persisted".equals(s))
            caseHome.updateCaseTags();

        return s;
    }



    /**
     * Overrides parent method to apply validation manually
     */
    @Transactional
    public String saveNew() {
        prepareNew();

        // information is valid ?
        if (!validate()) {
            return "validation-error";
        }

        PatientHome patientHome = caseEditingHome.getPatientHome();

        // save the patient's data
        patientHome.setTransactionLogActive(false);
        patientHome.setDisplayMessage(false);
        patientHome.persist();

        // treatment was defined ?
        caseHome.setTransactionLogActive(true);
        if (!caseHome.persist().equals("persisted"))
            return "error";

        // save exams results
        saveExams();

        caseHome.setTransactionLogActive(false);

        caseHome.updateCaseTags();

        saveContacts();

        return "persisted";
    }

    /**
     * Overrides parent method to apply validation manually
     */
    public String saveEditing() {
        String result;

        if(caseHome.getInstance().getDiagnosisType().equals(DiagnosisType.SUSPECT))
            result = saveEditingSuspect();
        else if(caseHome.getInstance().getDiagnosisType().equals(DiagnosisType.CONFIRMED) && caseHome.getInstance().getClassification().equals(CaseClassification.TB))
            result = saveEditingTbCase();
        else if(caseHome.getInstance().getDiagnosisType().equals(DiagnosisType.CONFIRMED) && caseHome.getInstance().getClassification().equals(CaseClassification.DRTB))
            result = "TODO MY FRIEND";
        else
            result = "error";

        if(!result.equals("persisted"))
            return "error";

        facesMessages.clear();
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "default.entity_created" , null);

        return result;
    }


    /**
     * Validate the exams declared in the form
     * @return
     */
    private boolean validateExams() {
        TbCaseNG tbcase = (TbCaseNG)caseHome.getInstance();

        boolean valid = true;

        if ((microscopyActions != null) && (microscopyActions.getInstance().getResult() != null)) {
            microscopyActions.getInstance().setTbcase(caseHome.getInstance());
            valid = microscopyActions.validate() && valid;
        }

        if ((cultureActions != null) && (cultureActions.getInstance().getResult() != null)) {
            cultureActions.getInstance().setTbcase(caseHome.getInstance());
            valid = cultureActions.validate() && valid;
        }

        if ((xpertActions != null) && (xpertActions.getInstance().getResult() != null)) {
            xpertActions.getInstance().setTbcase(caseHome.getInstance());
            valid = xpertActions.validate() && valid;
        }

        return valid;
    }


    /**
     * Save exams, if result available. Before saving, the exams must be validated,
     * otherwise an exception will be thrown
     */
    private void saveExams() {
        if ((microscopyActions != null) && microscopyActions.getInstance().getResult() != null) {
            microscopyActions.getInstance().setTbcase(caseHome.getInstance());
            microscopyActions.setShowMessages(false);
            microscopyActions.save();
        }

        if ((cultureActions != null) && cultureActions.getInstance().getResult() != null) {
            cultureActions.getInstance().setTbcase(caseHome.getInstance());
            cultureActions.setShowMessages(false);
            cultureActions.save();
        }

        if ((xpertActions != null) && xpertActions.getInstance().getResult() != null) {
            xpertActions.getInstance().setTbcase(caseHome.getInstance());
            xpertActions.setShowMessages(false);
            xpertActions.save();
        }
    }


    /**
     * Save the list of contacts
     */
    private void saveContacts() {
        ContactsActionNG act = (ContactsActionNG)App.getComponent("contactsActionNG");

        if (act != null) {
            act.save();
        }
    }
}
