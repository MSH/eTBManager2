/**
 * 
 */
package org.msh.tb.cases;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.entities.enums.DiagnosisType;
import org.msh.tb.entities.enums.InfectionSite;
import org.msh.tb.entities.enums.RoleAction;

/**
 * Controller class to handle follow-up of suspect cases. Basically there are two
 * main methods that handle suspect follow-up: <p/>
 *  {@link SuspectFollowupHome#registerConfirmedFollowup()} for not confirmed cases and
 *  {@link SuspectFollowupHome#registerNotConfirmedFollowup()} for confirmed cases
 * <p/>
 * Depending on the follow-up of the suspect, other information must be provided.
 * 
 * @author Ricardo Memoria
 *
 */
@Name("suspectFollowupHome")
public class SuspectFollowupHome {

	@In CaseHome caseHome;
	
	private TbCase dataModel;
	
	/**
	 * Initialize the data model with default values
	 */
	public void createDataModel() {
		dataModel = new TbCase();

		TbCase tbcase = caseHome.getInstance();
		dataModel.setClassification( tbcase.getClassification() );
	}
	
	/**
	 * Register the follow-up of the suspect case when the outcome will be as not confirmed
	 */
	public String registerNotConfirmedFollowup() {
		CaseCloseHome caseCloseHome = (CaseCloseHome)Component.getInstance("caseCloseHome");

		if ("case-closed".equals(caseCloseHome.closeCase()))
			return "followup-registered";
		else return "error";
	}
	
	
	/**
	 * Register the follow-up of the suspect case when the case is confirmed
	 */
	public String registerConfirmedFollowup() {
		if (dataModel == null)
			throw new IllegalArgumentException("dataModel cannot be null");

		if (dataModel.getInfectionSite() == InfectionSite.PULMONARY) {
//			if (dataModel.getPulmonaryType() == null) {
//				FacesMessages.instance().addToControlFromResourceBundle("cbpulmonary", "javax.faces.component.UIInput.REQUIRED");
//				return "error";
//			}
			dataModel.setExtrapulmonaryType(null);
			dataModel.setExtrapulmonaryType2(null);
		}
		else {
			if ((dataModel.getExtrapulmonaryType() == null) && (dataModel.getPulmonaryType() == null)) {
				FacesMessages.instance().addToControlFromResourceBundle("cbextrapulmonary", "javax.faces.component.UIInput.REQUIRED");
				return "error";
			}
			dataModel.setPulmonaryType(null);
		}

		if ((dataModel.getClassification() == CaseClassification.DRTB) && (dataModel.getDrugResistanceType() == null)) {
			FacesMessages.instance().addToControlFromResourceBundle("cbresistance", "javax.faces.component.UIInput.REQUIRED");
			return "error";
		}

		if((dataModel.getClassification() == CaseClassification.TB) && (dataModel.getDrugResistanceType() != null))
			dataModel.setDrugResistanceType(null);
		
		caseHome.initTransactionLog(RoleAction.EDIT);
		caseHome.setTransactionLogActive(true);

		TbCase tbcase = caseHome.getInstance();
		tbcase.setDiagnosisType(DiagnosisType.CONFIRMED);
		tbcase.setClassification(dataModel.getClassification());
		tbcase.setDiagnosisDate(dataModel.getDiagnosisDate());
		tbcase.setPatientType(dataModel.getPatientType());
		tbcase.setDrugResistanceType(dataModel.getDrugResistanceType());
		tbcase.setInfectionSite(dataModel.getInfectionSite());
		tbcase.setPulmonaryType(dataModel.getPulmonaryType());
		tbcase.setExtrapulmonaryType(dataModel.getExtrapulmonaryType());
		tbcase.setExtrapulmonaryType2(dataModel.getExtrapulmonaryType2());

		caseHome.setRoleName("SUSPECT_FOLLOWUP");

		if ("persisted".equals(caseHome.persist()))
			return "followup-registered";
		else return "error";
	}


	/**
	 * Return the instance of {@link TbCase} that will contain the changes from the UI
	 * @return the dataModel as instance of {@link TbCase}
	 */
	public TbCase getDataModel() {
		if (dataModel == null) {
			createDataModel();
		}
		return dataModel;
	}
}
