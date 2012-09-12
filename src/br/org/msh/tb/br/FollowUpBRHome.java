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
import org.msh.tb.entities.enums.YesNoType;
import org.msh.utils.ItemSelect;
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
	@In(create=true) CaseMoveHome caseMoveHome;
	@In(create=true) MedicalExaminationBRHome medicalExaminationBRHome;
	
	private boolean examMicroscopy;
	private boolean examCulture;
	private boolean molecularBiology;
	private boolean examDst;
	private boolean examHiv;
	private boolean examXRay;
	private boolean sideEffects;
	private boolean moveCase;
	
	private YesNoType schemaModification;
	
	public boolean isMoveCase() {
		return moveCase;
	}
	public YesNoType getSchemaModification() {
		return schemaModification;
	}
	public void setSchemaModification(YesNoType schemaModification) {
		this.schemaModification = schemaModification;
	}
	public void setMoveCase(boolean moveCase) {
		this.moveCase = moveCase;
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
		
		if(!moveCase){
			if(!validCaseMoveForm()){
				//Some code if needs, just going on in the pattern used before
			}else{
				validationError = true;
			}
		}
		
//		if(!caseDataBRHome.validateAndPrepare())
//			validationError = true;
			
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
			
			if(!moveCase){
				caseMoveHome.transferOut();
			}
			
			caseDataBRHome.saveMedicalExamination();
			
			facesMessages.clear();
			
			String s = caseHome.persist();
			
			if(schemaModification.equals(YesNoType.YES)){
				return "modifyschema";
			}else{
				return s;
			}
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
			facesMessages.addToControlFromResourceBundle("examculturelabselection2", "javax.faces.component.UIInput.REQUIRED");
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
			facesMessages.addToControlFromResourceBundle("molecularbiologylabselection2", "javax.faces.component.UIInput.REQUIRED");
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
			facesMessages.addToControlFromResourceBundle("examdst", "Adicione pelo menos um resultado para um medicamento ou marque a opção 'Não Realizado'.");
            validationError = true;
		}
			
		if (examDSTHome.getInstance().getDateCollected() == null) {
			facesMessages.addToControlFromResourceBundle("examdstdatecollected", "javax.faces.component.UIInput.REQUIRED");
            validationError = true;
		}

		if (examDSTHome.getLabselection().getLaboratory() == null) {
			facesMessages.addToControlFromResourceBundle("examdstlabselection2", "javax.faces.component.UIInput.REQUIRED");
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
		boolean emptyMonthForSelected = false;
		
		List<CaseSideEffect> lst = ItemSelectHelper.getSelectedItems(sideEffectBRHome.getItems(), true);
		
		if(lst.size() < 1){
			validationError = true;
			facesMessages.addToControlFromResourceBundle("sideeffects", "Selecione pelo menos um efeito adverso ou marque a opção 'Nenhuma'.");
		}
		
		for(ItemSelect<CaseSideEffect> c : sideEffectBRHome.getItems()){
			if(c.isSelected() && c.getItem().getMonth() == 0)
				emptyMonthForSelected = true;
		}
		
		if(emptyMonthForSelected){
			validationError = true;
			facesMessages.addToControlFromResourceBundle("sideeffects", "É obrigatório que você selecione um mês de tratamento para cada efeito adverso que foi marcado.");
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
		
		if(caseMoveHome.getTbunitselection().getAdminUnit() == null){
			facesMessages.addToControlFromResourceBundle("casemovecbselau", "javax.faces.component.UIInput.REQUIRED");
			validationError = true;			
		}else if (tbunit == null){
			facesMessages.addToControlFromResourceBundle("casemovecbunits", "javax.faces.component.UIInput.REQUIRED");
			validationError = true;
		}else if (prev.getTbunit().equals(tbunit)) {
			facesMessages.addFromResourceBundle("cbselau", "cases.move.errorunit");
			validationError = true;
		}
		
		return validationError;
	}
}
