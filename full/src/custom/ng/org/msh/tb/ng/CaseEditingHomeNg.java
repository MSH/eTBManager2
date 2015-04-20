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
    * Validates suspect data, checking required fields and other rules
    */
    private boolean validateSuspectData(){
        TbCaseNG tbcase = (TbCaseNG)caseHome.getInstance();

        boolean valid = validateExams();

        MessagesList lst = BeanValidator.validate(tbcase);

        // check age manually
        if (tbcase.getAge() == null) {
            lst.addRequired("age");
        }

        // check HIV result
        if (tbcase.getHivPosition() == null) {
            lst.addRequired("hivPosition");
        }
        else {
            if ((tbcase.getHivPosition() != HIVPosition.UNKNOWN) && (tbcase.getHivPositionDetail() == null)) {
                lst.addRequired("hivPositionDetail");
            }
        }

        valid = valid && lst.size() == 0;

        if (lst.size() > 0) {
            createBinder().publish(lst);
        }

        //Validates Required fields - begin
        //patient and case data
/*
        if(!validateRequired(tbcase.getPatient().getName(), "name")) suspectValidationError = false;
        if(!validateRequired(tbcase.getAge(), "edtage")) suspectValidationError = false;
        if(!validateRequired(tbcase.getPatient().getGender(), "gender")) suspectValidationError = false;
        if(!validateRequired(tbcase.getRegistrationDate(), "edtregdate")) suspectValidationError = false;
        if(!validateRequired(super.getTbunitselection().getAuselection().getSelectedUnit(), "cbselau")) suspectValidationError = false;
        if(!validateRequired(super.getTbunitselection().getSelected(), "cbunits")) suspectValidationError = false;
*/

        //microscopy data
/*
        if(examMicroscopy != null && examMicroscopy.getResult() != null) {
            if(!validateRequired(examMicroscopy.getDateCollected(), "datecollectedmic")) suspectValidationError = false;
            if(!validateRequired(examMicroscopy.getSampleType(), "sampletypemic")) suspectValidationError = false;
            if(!validateRequired(examMicroscopy.getDateRelease(), "datereleasemic")) suspectValidationError = false;
        }
*/

        //expert data
/*
        if(examXpert != null && examXpert.getResult() != null) {
            if(examXpert.getResult().equals(XpertResult.TB_DETECTED))
                if(!validateRequired(examXpert.getRifResult(), "cbres2")) suspectValidationError = false;

            if(!validateRequired(examXpert.getDateCollected(), "datecollectedxpert")) suspectValidationError = false;
        }
*/

        //culture data
/*
        if(examCulture != null && examCulture.getResult() != null) {
            if(!validateRequired(examCulture.getDateCollected(), "datecollectedculture")) suspectValidationError = false;
            if(!validateRequired(examCulture.getSampleType(), "sampletypeculture")) suspectValidationError = false;
            if(!validateRequired(examCulture.getDateRelease(), "datereleaseculture")) suspectValidationError = false;
        }
*/

        //HIV
/*
        if(!validateRequired(tbcase.getHivPosition(), "hivresult")) suspectValidationError = false;
        if(tbcase.getHivPosition() != null && tbcase.getHivPosition().equals(HIVPosition.POSITIVE))
            if(!validateRequired(tbcase.getHivPositionDetail(), "hivdetail")) valid = false;
        //Validates Required fields - end

        //Protect hiv data
        if(tbcase.getHivPosition() != null && (!tbcase.getHivPosition().equals(HIVPosition.POSITIVE)))
            tbcase.setHivPositionDetail(null);
*/

        return valid;
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
                .bind("edtaddr", "notifAddress.address")
                .bind("edtzip", "notifAddress.zipCode")
                .bind("edtphone", "edtmob")
                .bind("edtoccupation", "occupation")
                .bind("edtsr", "sourceReferral")
                .bind("edtmaritalstatus", "maritalStatus")
                .bind("hivresult", "hivPosition")
                .bind("hivdetail", "hivPositionDetail");

        return caseBinder;
    }


    /*
    * Validates TB case data, checking required fields and other rules
    */
    private boolean validateTbCaseData(){
        boolean valid = validateExams();

        TbCase tbcase = caseHome.getInstance();

        MessagesList lst = BeanValidator.validate(tbcase);
        valid = valid && lst.size() == 0;
        if (lst.size() > 0) {
            createBinder().publish(lst);
            return false;
        }

        return valid;
    }


    /**
     * Register the notification of a new DR-TB case
     * @return
     */
    private String saveNewDRTBcase() {


/*
        if(examMicroscopyHome.getInstance() != null && examMicroscopyHome.getInstance().getResult() != null
                && !examMicroscopyHome.persist().equals("persisted"))
            return "error";

        if(examXpertHome.getInstance() != null && examXpertHome.getInstance().getResult() != null
                && !examXpertHome.persist().equals("persisted"))
            return "error";

        if(examCultureHome.getInstance() != null && examCultureHome.getInstance().getResult() != null
                && !examCultureHome.persist().equals("persisted"))
            return "error";
*/

        facesMessages.clear();
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "default.entity_created");

        return "persisted";
    }

    /**
     * Save the notification of a new suspect case
     * @return
     */
    private String saveNewSuspect(){

        TbCaseNG tbcase = (TbCaseNG)caseHome.getInstance();
        tbcase.setSuspectClassification(tbcase.getClassification());

        // get notification unit
        tbcase.setNotificationUnit(getTbunitselection().getSelected());
        tbcase.getNotifAddress().setAdminUnit(getTbunitselection().getAuselection().getSelectedUnit());

        tbcase.setOwnerUnit(tbcase.getNotificationUnit());

        if (tbcase.getValidationState() == null)
            tbcase.setValidationState(ValidationState.WAITING_VALIDATION);

        if (tbcase.getState() == null)
            tbcase.setState(CaseState.WAITING_TREATMENT);

        tbcase.getPatient().setWorkspace(caseHome.getWorkspace());

        updatePatientAge();

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

    /**
     * Save changes made to a case
     * @return
     */
    public String saveEditingSuspect() {
        if (!validateSuspectData())
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

    /**
     * Save changes made to a case
     * @return
     */
    public String saveEditingTbCase() {
        if (!validateTbCaseData())
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
        DiagnosisType diagType = caseHome.getInstance().getDiagnosisType();
        CaseClassification cla = caseHome.getInstance().getClassification();

        // is a suspect case ?
        if (diagType == DiagnosisType.SUSPECT) {
            return saveNewSuspect();
        }

        // is a TB case ?
        if (cla == CaseClassification.TB) {
            return saveNewTBcase();
        }

        // is a DR-TB case ?
        if (cla == CaseClassification.DRTB) {
            return saveNewDRTBcase();
        }

        // when the situation is not handled
        return "undefined";
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

/*
        if(examMicroscopyHome.getInstance() != null && examMicroscopyHome.getInstance().getResult() != null
                && !examMicroscopyHome.persist().equals("persisted"))
            return "error";

        if(examXpertHome.getInstance() != null && examXpertHome.getInstance().getResult() != null
                && !examXpertHome.persist().equals("persisted"))
            return "error";

        if(examCultureHome.getInstance() != null && examCultureHome.getInstance().getResult() != null
                && !examCultureHome.persist().equals("persisted"))
            return "error";
*/

        facesMessages.clear();
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "default.entity_created" , null);

        return result;
    }

    /**
     * set error to false if a validation error occours
     */
/*
    public boolean validateRequired(Object o, String fieldid){
        if(o == null){
            facesMessages.addToControlFromResourceBundle(fieldid, "javax.faces.component.UIInput.REQUIRED");
            return false;
        }else if(o instanceof String && ((String)o).trim().isEmpty()){
            facesMessages.addToControlFromResourceBundle(fieldid, "javax.faces.component.UIInput.REQUIRED");
            return false;
        }else if(o instanceof Number && ((Number)o).intValue() == 0){
            facesMessages.addToControlFromResourceBundle(fieldid, "javax.faces.component.UIInput.REQUIRED");
            return false;
        }
        return true;
    }
*/

    /**
     * Validate the exams declared in the form
     * @return
     */
    private boolean validateExams() {
        TbCaseNG tbcase = (TbCaseNG)caseHome.getInstance();

        boolean valid = true;

        if (microscopyActions.getInstance().getResult() != null) {
            microscopyActions.getInstance().setTbcase(caseHome.getInstance());
            valid = microscopyActions.validate() && valid;
        }

        if (cultureActions.getInstance().getResult() != null) {
            cultureActions.getInstance().setTbcase(caseHome.getInstance());
            valid = cultureActions.validate() && valid;
        }

        if (xpertActions.getInstance().getResult() != null) {
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
        if (microscopyActions.getInstance().getResult() != null) {
            microscopyActions.getInstance().setTbcase(caseHome.getInstance());
            microscopyActions.setShowMessages(false);
            microscopyActions.save();
        }

        if (cultureActions.getInstance().getResult() != null) {
            cultureActions.getInstance().setTbcase(caseHome.getInstance());
            cultureActions.setShowMessages(false);
            cultureActions.save();
        }

        if (xpertActions.getInstance().getResult() != null) {
            xpertActions.getInstance().setTbcase(caseHome.getInstance());
            xpertActions.setShowMessages(false);
            xpertActions.save();
        }
    }

}
