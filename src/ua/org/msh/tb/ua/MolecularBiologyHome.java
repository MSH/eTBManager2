package org.msh.tb.ua;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.cases.exams.ExamHome;
import org.msh.tb.laboratories.LaboratorySelection;
import org.msh.tb.transactionlog.LogInfo;
import org.msh.tb.ua.entities.MolecularBiologyUA;
import org.msh.tb.ua.entities.enums.MolecularBiologyResult;

@Name("molecularBiologyUAHome")
@LogInfo(roleName="EXAM_BIOMOL")
public class MolecularBiologyHome extends ExamHome<MolecularBiologyUA> {
	private static final long serialVersionUID = -1149058962516904296L;

	private static final MolecularBiologyResult[] options = {
		MolecularBiologyResult.MTUBERCULOSIS,
		MolecularBiologyResult.MICROBAC_NONTB,
		MolecularBiologyResult.NEGATIVE
	};
	
	@In(create=true) FacesMessages facesMessages;
	
	private LaboratorySelection labselection = new LaboratorySelection();

	
	@Factory("molecularBiologyUA")
	public MolecularBiologyUA getExamHIV() {
		return getInstance();
	}
	

	@Override
	public String persist() {
		MolecularBiologyUA mb = getInstance();
		
		if (labselection != null)
			mb.setLaboratory(labselection.getLaboratory());

		return super.persist();
	}


	/**
	 * Validate the data that cannot be validated by JSF controls
	 * @return true - if correct validated
	 */
	public boolean validate() {
		MolecularBiologyUA mb = getInstance();
		boolean res = true;

		if ((mb.getDateRelease() == null) || (mb.getDateRelease().before(mb.getDate()))) {
			facesMessages.addToControlFromResourceBundle("edtMBreleasedate", "javax.faces.component.UIInput.REQUIRED");
			res = false;
		}

		if (mb.getMethod() == null) {
			facesMessages.addToControlFromResourceBundle("cbMBMethod", "javax.faces.component.UIInput.REQUIRED");
			res = false;
		}
		
		return res;
	}
	
	public MolecularBiologyResult[] getNotifResultOptions() {
		return MolecularBiologyResult.values();
	}
	

	public MolecularBiologyResult[] getResultOptions() {
		return options;
	}
	
	public LaboratorySelection getLabselection() {
		return labselection;
	}

	@Override
	public void setId(Object id) {
		super.setId(id);

		initializeLabselection();
	}
	
	
	/**
	 * Initialize
	 */
	protected void initializeLabselection() {
		if (labselection != null) {
			labselection.setLaboratory(getInstance().getLaboratory());
		}
	}
}