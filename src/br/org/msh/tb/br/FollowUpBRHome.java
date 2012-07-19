package org.msh.tb.br;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.br.entities.MolecularBiology;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.CaseMoveHome;
import org.msh.tb.cases.exams.ExamCultureHome;
import org.msh.tb.cases.exams.ExamDSTHome;
import org.msh.tb.cases.exams.ExamHIVHome;
import org.msh.tb.cases.exams.ExamMicroscopyHome;
import org.msh.tb.cases.exams.ExamXRayHome;
import org.msh.tb.entities.CaseSideEffect;
import org.msh.tb.entities.ExamCulture;
import org.msh.tb.entities.ExamDST;
import org.msh.tb.entities.ExamDSTResult;
import org.msh.tb.entities.ExamHIV;
import org.msh.tb.entities.ExamMicroscopy;
import org.msh.tb.entities.ExamXRay;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.TreatmentHealthUnit;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.DstResult;
import org.msh.utils.ItemSelectHelper;

@Name("followUpBRHome")
@Scope(ScopeType.CONVERSATION)
public class FollowUpBRHome {
	
	@In(create=true) FacesMessages facesMessages;
	@In(create=true) CaseDataBRHome caseDataBRHome;
	@In(create=true) CaseHome caseHome;
	@In(create=true) ExamMicroscopyHome examMicroscopyHome;
	@In(create=true) ExamCultureHome examCultureHome;
	@In(create=true) MolecularBiologyHome molecularBiologyHome;
	@In(create=true) ExamDSTHome examDSTHome;
	@In(create=true) ExamHIVHome examHIVHome;
	@In(create=true) ExamXRayHome examXRayHome;
	@In(create=true) SideEffectHomeBR sideEffectBRHome;
	@In(create=true) CaseCloseHomeBR caseCloseHomeBR;
	@In(create=true) CaseMoveHome caseMoveHome;
	
	private boolean examMicroscopy;
	private boolean examCulture;
	private boolean molecularBiology;
	private boolean examDst;
	private boolean examHiv;
	private boolean examXRay;
	private boolean sideEffects;
	private boolean moveCase;
	
	private CaseState state;
	private static final CaseState[] caseStates = {
		CaseState.ONTREATMENT,
		CaseState.CURED, 
		CaseState.TREATMENT_COMPLETED,
		CaseState.DEFAULTED, 
		CaseState.FAILED,
		CaseState.DIED, 
		CaseState.DIED_NOTTB, 
		CaseState.TRANSFERRED_OUT,
		CaseState.REGIMEN_CHANGED,
		CaseState.MDR_CASE,
		CaseState.DIAGNOSTIC_CHANGED,
		CaseState.OTHER};
	
	public boolean isMoveCase() {
		return moveCase;
	}
	public void setMoveCase(boolean moveCase) {
		this.moveCase = moveCase;
	}
	public CaseState getState() {
		return state;
	}
	public void setState(CaseState state) {
		this.state = state;
	}
	public boolean isSideEffects() {
		return sideEffects;
	}
	public void setSideEffects(boolean sideEffects) {
		this.sideEffects = sideEffects;
	}
	public boolean isExamXRay() {
		return examXRay;
	}
	public void setExamXRay(boolean examXRay) {
		this.examXRay = examXRay;
	}
	public boolean isExamHiv() {
		return examHiv;
	}
	public void setExamHiv(boolean examHiv) {
		this.examHiv = examHiv;
	}
	public boolean isExamDst() {
		return examDst;
	}
	public void setExamDst(boolean examDst) {
		this.examDst = examDst;
	}
	public boolean isExamCulture() {
		return examCulture;
	}
	public void setExamCulture(boolean examCulture) {
		this.examCulture = examCulture;
	}
	public boolean isExamMicroscopy() {
		return examMicroscopy;
	}
	public void setExamMicroscopy(boolean examMicroscopy) {
		this.examMicroscopy = examMicroscopy;
	}
	public boolean isMolecularBiology() {
		return molecularBiology;
	}
	public void setMolecularBiology(boolean molecularBiology) {
		this.molecularBiology = molecularBiology;
	}
	
	public CaseState[] getCaseStates() {
		return caseStates;
	}
	
	public String save(){
		boolean validationError = false;
		
		if(!examMicroscopy){
			if(!validExamMicroscopyForm()){
				ExamMicroscopy examMicroscopy = examMicroscopyHome.getInstance();
				examMicroscopy.setComments("Exame adicionado pelo formulário de Boletim de Acompanhamento." );
			}else{
				validationError = true;
			}
		}
		
		if(!examCulture){
			if(!validExamCultureForm()){
				ExamCulture examCulture = examCultureHome.getInstance();
				examCulture.setComments("Exame adicionado pelo formulário de Boletim de Acompanhamento." );
			}else{
				validationError = true;
			}
		}
		
		if(!molecularBiology){
			if(!validMolecularBiologyForm()){
				MolecularBiology molecularBiology = molecularBiologyHome.getInstance();
				molecularBiology.setComments("Exame adicionado pelo formulário de Boletim de Acompanhamento." );
			}else{
				validationError = true;
			}
		}
		
		if(!examDst){
			if(!validExamDstForm() && examDSTHome.validateAndPrepareFields()){
				ExamDST examDst = examDSTHome.getInstance();
				examDst.setComments("Exame adicionado pelo formulário de Boletim de Acompanhamento." );
			}else{
				validationError = true;
			}
		}
		
		if(!examHiv){
			if(!validExamHivForm() && examHIVHome.validateForm()){
				ExamHIV examHiv = examHIVHome.getInstance();
				examHiv.setComments("Exame adicionado pelo formulário de Boletim de Acompanhamento." );
			}else{
				validationError = true;
			}
		}
		
		if(!examXRay){
			if(!validExamXRayForm()){
				ExamXRay examXRay = examXRayHome.getInstance();
				examXRay.setComments("Exame adicionado pelo formulário de Boletim de Acompanhamento." );
			}else{
				validationError = true;
			}
		}
		
		if(!sideEffects){
			if(!validSideEffectForm()){
				List<CaseSideEffect> lst = ItemSelectHelper.getSelectedItems(sideEffectBRHome.getItems(), true);
				
				for(CaseSideEffect c : lst){
					c.setComment("Adicionado pelo formulário de Boletim de Acompanhamento.");
				}
			}else{
				validationError = true;
			}
		}
		
		//CaseState - BEGIN
		if(state.equals(CaseState.ONTREATMENT)){
			caseHome.getTbCase().setState(state);
		}else{
			caseCloseHomeBR.setState(state);
		}
		if(!caseCloseHomeBR.validateClose())
			validationError = true;
		//CaseState - END
		
		if(!moveCase){
			if(!validCaseMoveForm()){
				//Some code if needs, just going on in the pattern used before
			}else{
				validationError = true;
			}
		}
		
		if(!caseDataBRHome.validateAndPrepare())
			validationError = true;
			
		if(validationError){
			return "validationError";
		}else{
			if(!examMicroscopy)
				examMicroscopyHome.persist();

			if(!examCulture)
				examCultureHome.persist();
			
			if(!molecularBiology)
				molecularBiologyHome.persist();
			
			if(!examDst)
				examDSTHome.persist();
			
			if(!examHiv)
				examHIVHome.persist();
			
			if(!examXRay)
				examXRayHome.persist();
			
			if(!sideEffects)
				sideEffectBRHome.saveSideEffectList();
			
			if(!state.equals(CaseState.ONTREATMENT)){
				caseCloseHomeBR.closeCase();
			}
			
			if(!moveCase){
				caseMoveHome.transferOut();
			}
			
			caseDataBRHome.saveMedicalExamination();
			
			facesMessages.clear();
			
			return caseHome.persist();
		}
	}
	
	private boolean validExamMicroscopyForm(){
		boolean validationError = false;
		
		if (examMicroscopyHome.getInstance().getResult() == null) {
			validationError = true;
			facesMessages.addToControlFromResourceBundle("exammicroscpycbres", "javax.faces.component.UIInput.REQUIRED");
		}
		
		if (examMicroscopyHome.getInstance().getDateCollected() == null) {
			validationError = true;
			facesMessages.addToControlFromResourceBundle("exammicroscpydatecollected", "javax.faces.component.UIInput.REQUIRED");
		}

		return validationError;
	}
	
	private boolean validExamCultureForm(){
		boolean validationError = false;
		
		if (examCultureHome.getInstance().getResult() == null) {
			facesMessages.addToControlFromResourceBundle("examculturecbres", "javax.faces.component.UIInput.REQUIRED");
            validationError = true;
		}
		
		if (examCultureHome.getInstance().getDateCollected() == null) {
			facesMessages.addToControlFromResourceBundle("examculturedatecollected", "javax.faces.component.UIInput.REQUIRED");
            validationError = true;
		}
		
		if (examCultureHome.getLabselection().getLaboratory() == null) {
			facesMessages.addToControlFromResourceBundle("examculturelabselection", "javax.faces.component.UIInput.REQUIRED");
            validationError = true;
		}

		return validationError;
	}
	
	private boolean validMolecularBiologyForm(){
		boolean validationError = false;
		
		if (molecularBiologyHome.getInstance().getResult() == null) {
			facesMessages.addToControlFromResourceBundle("molecularbiologyresult", "javax.faces.component.UIInput.REQUIRED");
            validationError = true;
		}
		
		if (molecularBiologyHome.getInstance().getDate() == null) {
			facesMessages.addToControlFromResourceBundle("molecularbiologydatecollected", "javax.faces.component.UIInput.REQUIRED");
            validationError = true;
		}
		
		if (molecularBiologyHome.getInstance().getDateRelease() == null) {
			facesMessages.addToControlFromResourceBundle("molecularbiologydaterelease", "javax.faces.component.UIInput.REQUIRED");
            validationError = true;
		}
		
		if (molecularBiologyHome.getInstance().getMethod() == null) {
			facesMessages.addToControlFromResourceBundle("molecularbiologymethod", "javax.faces.component.UIInput.REQUIRED");
            validationError = true;
		}
		
		if (molecularBiologyHome.getLabselection().getLaboratory() == null) {
			facesMessages.addToControlFromResourceBundle("molecularbiologylabselection", "javax.faces.component.UIInput.REQUIRED");
            validationError = true;
		}

		return validationError;
	}
	
	private boolean validExamDstForm(){
		boolean validationError = false;
		boolean atLeastOneResult = false;
		
		for(ExamDSTResult r : examDSTHome.getInstance().getResults()){
			if(!r.getResult().equals(DstResult.NOTDONE)){
				atLeastOneResult = true;
			}
		}
		if (!atLeastOneResult) {
			facesMessages.addToControlFromResourceBundle("examdstmedicinelist", "javax.faces.component.UIInput.REQUIRED");
            validationError = true;
		}
			
		if (examDSTHome.getInstance().getDateCollected() == null) {
			facesMessages.addToControlFromResourceBundle("examdstdatecollected", "javax.faces.component.UIInput.REQUIRED");
            validationError = true;
		}

		if (examDSTHome.getLabselection().getLaboratory() == null) {
			facesMessages.addToControlFromResourceBundle("examdstlabselection", "javax.faces.component.UIInput.REQUIRED");
            validationError = true;
		}

		return validationError;
	}
	
	private boolean validExamHivForm(){
		boolean validationError = false;
		
		if (examHIVHome.getInstance().getDate() == null) {
			facesMessages.addToControlFromResourceBundle("examhivdaterelease", "javax.faces.component.UIInput.REQUIRED");
            validationError = true;
		}
		
		if (examHIVHome.getInstance().getResult() == null) {
			facesMessages.addToControlFromResourceBundle("examhivresult", "javax.faces.component.UIInput.REQUIRED");
            validationError = true;
		}

		return validationError;
	}
	
	private boolean validExamXRayForm(){
		boolean validationError = false;
		
		if (examXRayHome.getInstance().getDate() == null) {
			 validationError = true;
			facesMessages.addToControlFromResourceBundle("examxraydate", "javax.faces.component.UIInput.REQUIRED");
		}
		
		if (examXRayHome.getInstance().getPresentation() == null) {
			validationError = true;
			facesMessages.addToControlFromResourceBundle("examxraypresentation", "javax.faces.component.UIInput.REQUIRED");
		}

		return validationError;
	}
	
	private boolean validSideEffectForm(){
		boolean validationError = false;
		List<CaseSideEffect> lst = ItemSelectHelper.getSelectedItems(sideEffectBRHome.getItems(), true);
		
		if(lst.size() < 1){
			validationError = true;
			facesMessages.addToControlFromResourceBundle("sideeffects", "Selecione pelo menos um efeito adverso ou marque a opção 'Nenhum'.");
		}
		
		return validationError;
	}
	
	private boolean validCaseMoveForm(){
		boolean validationError = false;
		
		if (!caseHome.isCanTransferOut())
			validationError = true;
		
		//Has to be the same validation of CaseMoveHome.validateTransferOut()
		Tbunit tbunit = caseMoveHome.getTbunitselection().getTbunit();
		
		// search for previous treatment health unit
		TreatmentHealthUnit prev = caseMoveHome.findTransferOutHealthUnit();		
		if (prev == null) 
			validationError = true;
		
		if (caseMoveHome.getMoveDate() == null) {
			 validationError = true;
			facesMessages.addToControlFromResourceBundle("transferdate", "javax.faces.component.UIInput.REQUIRED");
		}else if (!prev.getPeriod().isDateInside(caseMoveHome.getMoveDate())) {
			// checks if date is before beginning treatment date
			facesMessages.addFromResourceBundle("transferdate", "cases.move.errortreatdate");
			validationError = true;
		}
		
		if (tbunit == null){
			facesMessages.addToControlFromResourceBundle("cbselau", "javax.faces.component.UIInput.REQUIRED");
			validationError = true;
		}else if (prev.getTbunit().equals(tbunit)) {
			facesMessages.addFromResourceBundle("cbselau", "cases.move.errorunit");
			validationError = true;
		}
		
		return validationError;
	}
	

}
