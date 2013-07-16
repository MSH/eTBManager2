package org.msh.tb.az;


import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.msh.tb.application.App;
import org.msh.tb.az.entities.TbCaseAZ;
import org.msh.tb.az.entities.enums.LastAction;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.ComorbidityHome;
import org.msh.tb.cases.SideEffectHome;
import org.msh.tb.cases.TbContactHome;
import org.msh.tb.cases.dispensing.CaseDispensingHome;
import org.msh.tb.cases.exams.ExamHIVHome;
import org.msh.tb.cases.exams.MedicalExaminationHome;
import org.msh.tb.cases.treatment.StartTreatmentHome;
import org.msh.tb.cases.treatment.StartTreatmentIndivHome;
import org.msh.tb.entities.CaseSideEffect;
import org.msh.tb.entities.ExamCulture;
import org.msh.tb.entities.ExamDST;
import org.msh.tb.entities.ExamHIV;
import org.msh.tb.entities.ExamMicroscopy;
import org.msh.tb.entities.ExamXRay;
import org.msh.tb.entities.MedicalExamination;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.TbContact;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.enums.ValidationState;
import org.msh.tb.tbunits.TBUnitSelection;

/**
 * Class-controller for cases-unit
 * @author A.M.
 */

@Name("casesControlAZ")
public class CasesControlAZ {
	
	//==========================CONFIRM FOR START TREATMENT==================================
	/**
	 * Return true, if select tb-unit not where user from
	 */
	public boolean notOwnTBUnit(){
		TBUnitSelection nTbUSel;
		StartTreatmentHome th = (StartTreatmentHome)App.getComponent("startTreatmentHome",false);
		if (th!=null)
			nTbUSel = th.getTbunitselection();
		else{
			StartTreatmentIndivHome tih = (StartTreatmentIndivHome)App.getComponent("startTreatmentIndivHome",false);
			nTbUSel = tih.getTbunitselection();
		}
		Tbunit nTbU = nTbUSel.getTbunit();
		Tbunit nTbUUser = ((UserWorkspace)App.getComponent("userWorkspace")).getTbunit();
		if (nTbU==null || nTbUUser==null) return false;
		if (nTbU.getId().intValue() != nTbUUser.getId().intValue())
			return true;
		return false;
	}
	/**
	 * Set tb-unit from database
	 */
	public void setUserTBUnitDefault(){
		//Tbunit nTbUUser = ((UserWorkspace)App.getComponent("userWorkspace")).getTbunit();
		TBUnitSelection nTbUSel;
		CaseHome caseHome = (CaseHome)App.getComponent("caseHome");
		Tbunit nTbUUser = caseHome.getTbCase().getOwnerUnit();
		StartTreatmentHome th = (StartTreatmentHome)App.getComponent("startTreatmentHome",false);
		if (th!=null)
			nTbUSel = th.getTbunitselection();
		else{
			StartTreatmentIndivHome tih = (StartTreatmentIndivHome)App.getComponent("startTreatmentIndivHome",false);
			nTbUSel = tih.getTbunitselection();
		}
		nTbUSel.setTbunitWithOptions(nTbUUser);
	}
	
	
	//==========================SAVE THE LAST ACTION WITH CASE==================================
	
	/**
	 * Save this last action to current instance of case
	 */
	public static boolean saveLastAction(LastAction la){
		if (la == null) return false;
		CaseHome caseHome = (CaseHome)App.getComponent("caseHome");
		TbCase cur = caseHome.getInstance();
		if (cur!=null){
			TbCaseAZ tc = (TbCaseAZ) App.getEntityManager().find(TbCaseAZ.class, cur.getId());
			tc.setLastAction(la);
			App.getEntityManager().persist(tc);
			App.getEntityManager().flush();
			return true;
		}
		return false;
	}
	
	/*
	 * Next 10 methods - observers of general operations with tbcase  
	 */
	
	@Observer("case.close")
	public void closeCase(){
		saveLastAction(LastAction.CLOSE_CASE);
	}
	
	@Observer("case.reopen")
	public void reOpenCase(){
		saveLastAction(LastAction.RE_OPEN_CASE);
	}
	
	@Observer("case.validate")
	public void validateCase(){
		saveLastAction(LastAction.VALIDATE_CASE);
	}
	
	@Observer("pending-registered-answered")
	public void postPendingCase(){
		CaseHome caseHome = (CaseHome)App.getComponent("caseHome");
		TbCase cur = caseHome.getInstance();
// Ricardo Memoria - There is no pending status anymore
/*		if (ValidationState.PENDING.equals(cur.getValidationState()))
			saveLastAction(LastAction.POST_PENDING_CASE);
		if (ValidationState.PENDING_ANSWERED.equals(cur.getValidationState()))
			saveLastAction(LastAction.POST_ANSWER_CASE);
*/	}
	
	@Observer("case.transferout")
	public void transferOutCase(){
		saveLastAction(LastAction.TRANSFER_PATIENT_TO);
	}
	
	@Observer("case.transferin")
	public void transferInCase(){
		saveLastAction(LastAction.TRANSFER_PATIENT_IN);
	}
	
	@Observer("case.transferout.rollback")
	public void transferRollbackCase(){
		saveLastAction(LastAction.TRANSFER_PATIENT_CANCEL);
	}
	
	@Observer("treatment-started")
	public void treatStart(){
		saveLastAction(LastAction.START_TREAT);
	}
	
	@Observer("treatment-persist")
	public void treatEdit(){
		saveLastAction(LastAction.EDIT_TREAT);
	}
	
	@Observer("treatment-undone")
	public void treatCancel(){
		saveLastAction(LastAction.CANCEL_TREAT);
	}
	
	/**
	 * Rewrite {@link CaseDispensingHome.saveDispensing()} with setting last action
	 * @return result of {@link CaseDispensingHome.saveDispensing()}
	 */
	public String saveCaseDispensing(){
		CaseDispensingHome disp = (CaseDispensingHome) App.getComponent("caseDispensingHome", false);
		String res = disp.saveDispensing();
		if ("dispensing-saved".equals(res))
			saveLastAction(LastAction.EDIT_CASE_DISPENSING);
		return res;
	}
	
	/**
	 * Rewrite persist or save of active Home-object of current form with setting proper last action
	 * @return result of persist
	 */
	public String persistActiveHomeObject(){
		LastAction la = null;
		String res="";
		
		//get instance WITHOUT CREATE
		ExamHIVHome hiv = (ExamHIVHome) App.getComponent("examHIVHome", false);
		ExamMicroscopyAZHome mic = (ExamMicroscopyAZHome) App.getComponent("examMicroscopyAZHome", false);
		ExamCultureAZHome cul = (ExamCultureAZHome) App.getComponent("examCultureAZHome", false);
		ExamDSTAZHome dst = (ExamDSTAZHome) App.getComponent("examDSTAZHome", false);
		ExamXRayAZHome xray = (ExamXRayAZHome) App.getComponent("examXRayAZHome", false);
		MedicalExaminationHome meh = (MedicalExaminationHome) App.getComponent("medicalExaminationHome", false);
		ComorbidityHome ch = (ComorbidityHome) App.getComponent("comorbidityHome", false);
		TbContactHome cont = (TbContactHome) App.getComponent("tbContactHome", false);
		SideEffectHome side = (SideEffectHome) App.getComponent("sideEffectHome", false);

		//because of we must recognize home-object is active.
		//Set proper action
		if (hiv!=null){
			ExamHIV ex = hiv.getInstance();
			if (ex.getId()==null)
				la = LastAction.NEW_EXAM_HIV;
			else
				la = LastAction.EDIT_EXAM_HIV;
			res = hiv.persist();
		}
		else
		if (mic!=null){
			ExamMicroscopy ex = mic.getInstance();
			if (ex.getId()==null)
				la = LastAction.NEW_EXAM_MICROSCOPY;
			else
				la = LastAction.EDIT_EXAM_MICROSCOPY;
			res = mic.persist();
		}
		else
		if (cul!=null){
			ExamCulture ex = cul.getInstance();
			if (ex.getId()==null)
				la = LastAction.NEW_EXAM_CULTURE;
			else
				la = LastAction.EDIT_EXAM_CULTURE;
			res = cul.persist();
		}
		else
		if (dst!=null){
			ExamDST ex = dst.getInstance();
			if (ex.getId()==null)
				la = LastAction.NEW_EXAM_DST;
			else
				la = LastAction.EDIT_EXAM_DST;
			res = dst.persist();
		}
		else
		if (xray!=null){
			ExamXRay ex = xray.getInstance();
			if (ex.getId()==null)
				la = LastAction.NEW_EXAM_XRAY;
			else
				la = LastAction.EDIT_EXAM_XRAY;
			res = xray.persist();
		}
		else
		if (meh!=null){
			MedicalExamination ex = meh.getInstance();
			if (ex.getId()==null)
				la = LastAction.NEW_MED_EXAM;
			else
				la = LastAction.EDIT_MED_EXAM;
			res = meh.persist();
		}
		else
		if (ch!=null){
			la = LastAction.EDIT_COMORB;
			res = ch.save();
		}
		else
		if (cont!=null){
			TbContact ex = cont.getInstance();
			if (ex.getId()==null)
				la = LastAction.NEW_CONTACT;
			else
				la = LastAction.EDIT_CONTACT;
			res = cont.persist();
		}
		else
		if (side!=null){
			CaseSideEffect ex = side.getInstance();
			if (ex.getId()==null)
				la = LastAction.NEW_SIDE_EFFECT;
			else
				la = LastAction.EDIT_SIDE_EFFECT;
			res = side.save();
		}
		//verify result of persist
		if ("persisted".equals(res))
			saveLastAction(la);
		return res;
	}
	
}
