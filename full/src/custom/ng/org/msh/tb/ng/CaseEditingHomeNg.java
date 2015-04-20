package org.msh.tb.ng;


import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.msh.tb.application.App;
import org.msh.tb.cases.CaseEditingHome;
import org.msh.tb.cases.exams.*;
import org.msh.tb.entities.ExamCulture;
import org.msh.tb.entities.ExamMicroscopy;
import org.msh.tb.entities.ExamXpert;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.*;
import org.msh.tb.ng.entities.TbCaseNG;
import org.msh.tb.ng.entities.enums.HIVPosition;
import org.msh.validators.BeanValidator;
import org.msh.validators.FacesMessagesBinder;
import org.msh.validators.MessagesList;


/**
 * Handle TB and DR-TB cases editing and new notification
 *
 * @author Mauricio Santos
 */
@Name("caseEditingHomeNg")
@Scope(ScopeType.CONVERSATION)
public class CaseEditingHomeNg extends CaseEditingHome {

    @In(create = true) MicroscopyActions microscopyActions;
    @In(create = true) XpertActions xpertActions;
    @In(create = true) CultureActions cultureActions;


    /**
     * Initialize the caseHome object for editing
     */
    public void initializeEditing() {
        TbCase tbcase = caseHome.getInstance();

        if (getTbunitselection().getSelected() == null)
            getTbunitselection().setSelected(tbcase.getNotificationUnit());

        if (getNotifAdminUnit().getSelectedUnit() == null)
            getNotifAdminUnit().setSelectedUnit(tbcase.getNotifAddress().getAdminUnit());

        if (getCurrentAdminUnit().getSelectedUnit() == null)
            getCurrentAdminUnit().setSelectedUnit(tbcase.getCurrentAddress().getAdminUnit());

        updatePatientAge();

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
     * Register the notification of a new DR-TB case
     * @return
     */
/*
    private String saveNewDRTBcase() {


        if(examMicroscopyHome.getInstance() != null && examMicroscopyHome.getInstance().getResult() != null
                && !examMicroscopyHome.persist().equals("persisted"))
            return "error";

        if(examXpertHome.getInstance() != null && examXpertHome.getInstance().getResult() != null
                && !examXpertHome.persist().equals("persisted"))
            return "error";

        if(examCultureHome.getInstance() != null && examCultureHome.getInstance().getResult() != null
                && !examCultureHome.persist().equals("persisted"))
            return "error";

        facesMessages.clear();
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "default.entity_created");

        return "persisted";
    }
*/

    /**
     * Prepare a new case (confirmed or suspect) to be saved
     */
    private void prepareNew() {
        TbCaseNG tbcase = (TbCaseNG)caseHome.getInstance();

        tbcase.setNotificationUnit(getTbunitselection().getSelected());
        tbcase.getNotifAddress().setAdminUnit(getTbunitselection().getAuselection().getSelectedUnit());
        tbcase.setOwnerUnit(tbcase.getNotificationUnit());

        if (tbcase.getValidationState() == null)
            tbcase.setValidationState(ValidationState.WAITING_VALIDATION);

        if (tbcase.getState() == null)
            tbcase.setState(CaseState.WAITING_TREATMENT);

        tbcase.getPatient().setWorkspace(caseHome.getWorkspace());
        updatePatientAge();

        // set the initial classification of the suspect (won't change in case of confirmed cases that were not suspects)
        tbcase.setSuspectClassification(tbcase.getClassification());
    }

    /**
     * Save the notification of a new suspect case
     * @return
     */
/*
    private String saveNewSuspect(){
        prepareNew();

        // validate data
        if(!validateSuspectData())
            return "error";

        // save the patient's data
        patientHome.setTransactionLogActive(false);
        patientHome.persist();

        // treatment was defined ?
        caseHome.setTransactionLogActive(true);
        if (!caseHome.persist().equals("persisted"))
            return "error";

        // save exams results
        if (cultureActions.getInstance().getResult() != null) {
            cultureActions.save();
        }
        if (microscopyActions.getInstance().getResult() != null) {
            microscopyActions.save();
        }

        if (xpertActions.getInstance().getResult() != null) {
            xpertActions.save();
        }

        caseHome.setTransactionLogActive(false);

        caseHome.updateCaseTags();

        return "persisted";
    }
*/

    /**
     * Save changes made to a case
     * @return
     */
    public String saveEditingSuspect() {
        if (!validate())
            return "error";

        TbCaseNG tbcase = (TbCaseNG) caseHome.getInstance();

        tbcase.setNotificationUnit(getTbunitselection().getSelected());
        tbcase.getNotifAddress().setAdminUnit(getTbunitselection().getAuselection().getSelectedUnit());

        //fix the inconsistence when owner unit is null.
        if(tbcase.getOwnerUnit() == null){
            updateOwnerUnit(tbcase);
        }

        updatePatientAge();

        String s = caseHome.persist();

        if ("persisted".equals(s))
            caseHome.updateCaseTags();

        return s;
    }

/*
    private String saveNewTBcase(){
        if(!validateTbCaseData())
            return "error";
        //TODO
        TbCaseNG tbcase = (TbCaseNG)caseHome.getInstance();

        tbcase.setSuspectClassification(tbcase.getClassification());

        // save the patient's data
        patientHome.persist();

        // get notification unit
        tbcase.setNotificationUnit(getTbunitselection().getSelected());
        tbcase.getNotifAddress().setAdminUnit(getTbunitselection().getAuselection().getSelectedUnit());

        tbcase.setOwnerUnit(tbcase.getNotificationUnit());

        if (tbcase.getValidationState() == null)
            tbcase.setValidationState(ValidationState.WAITING_VALIDATION);

        if (tbcase.getState() == null)
            tbcase.setState(CaseState.WAITING_TREATMENT);

        updatePatientAge();

        // treatment was defined ?
        caseHome.setTransactionLogActive(true);
        if (!caseHome.persist().equals("persisted"))
            return "error";

        caseHome.setTransactionLogActive(false);

        caseHome.updateCaseTags();

        return "persisted";
    }
*/

    /**
     * Save changes made to a case
     * @return
     */
    public String saveEditingTbCase() {
        if (!validate())
            return "error";
        //TODO
        TbCaseNG tbcase = (TbCaseNG) caseHome.getInstance();

        tbcase.setNotificationUnit(getTbunitselection().getSelected());
        tbcase.getNotifAddress().setAdminUnit(getTbunitselection().getAuselection().getSelectedUnit());

        //fix the inconsistence when owner unit is null.
        if(tbcase.getOwnerUnit() == null){
            updateOwnerUnit(tbcase);
        }

        updatePatientAge();

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

        // save the patient's data
        patientHome.setTransactionLogActive(false);
        patientHome.persist();

        // treatment was defined ?
        caseHome.setTransactionLogActive(true);
        if (!caseHome.persist().equals("persisted"))
            return "error";

        // save exams results
        saveExams();

        caseHome.setTransactionLogActive(false);

        caseHome.updateCaseTags();

        return "persisted";
    }

    /**
     * Overrides parent method to apply validation manually
     */
    @Override
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

}
