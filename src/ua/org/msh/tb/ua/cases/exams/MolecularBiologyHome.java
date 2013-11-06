package org.msh.tb.ua.cases.exams;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.application.App;
import org.msh.tb.cases.exams.ExamHome;
import org.msh.tb.laboratories.LaboratorySelection;
import org.msh.tb.transactionlog.LogInfo;
import org.msh.tb.ua.entities.MolecularBiologyUA;
import org.msh.tb.ua.entities.enums.MolecularBiologyResult;

@Name("molecularBiologyUAHome")
@LogInfo(roleName="EXAM_BIOMOL",entityClass=MolecularBiologyUA.class)
public class MolecularBiologyHome extends ExamHome<MolecularBiologyUA> {
	private static final long serialVersionUID = -1149058962516904296L;

	@In(create=true) FacesMessages facesMessages;
	
	private LaboratorySelection labselection = new LaboratorySelection("labid");

	
	@Factory("molecularBiologyUA")
	public MolecularBiologyUA getExamHIV() {
		return getInstance();
	}
	

	@Override
	public String persist() {
		if (!validate())
			return "error";

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

		/*if (mb.getMethod() == null) {
			facesMessages.addToControlFromResourceBundle("cbMBMethod", "javax.faces.component.UIInput.REQUIRED");
			res = false;
		}*/
		
		if ((MolecularBiologyResult.GeneXpert.equals(mb.getResult()) && !mb.isPcr() && mb.getR())
		|| (MolecularBiologyResult.GenoTypeMTBDRplus.equals(mb.getResult()) && !mb.isPcr() && (mb.getR() || mb.getH()))
		|| (MolecularBiologyResult.GenoTypeMTBDRsl.equals(mb.getResult()) && !mb.isPcr() && (mb.getKm() || mb.getCm() || mb.getE() || mb.getLfx() || mb.getMfx())))
			{
				facesMessages.addFromResourceBundle(App.getMessage("MolecularBiology.error1"));
				res = false;
			}
		/*if (!mb.isPcr() && mb.isResist())
			{
				facesMessages.addFromResourceBundle(App.getMessage("MolecularBiology.error1"));
				res = false;
			}*/
		return res;
	}
	
	public MolecularBiologyResult[] getResultOptions() {
		return MolecularBiologyResult.values();
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
	
	/**
	 * Return result of current test in user eye view 
	 * */
	public String getResult(MolecularBiologyUA mb){
		String res = (MolecularBiologyResult.GeneXpert.equals(mb.getResult()) ? "MTB":"MBT")+sign(mb.isPcr())+"(PCR"+sign(mb.isPcr())+"), Resist "+
		(MolecularBiologyResult.GenoTypeMTBDRsl.equals(mb.getResult()) ? 2:1)+
		sign(mb.isResistance())+" (PCR\\";
		if (MolecularBiologyResult.GeneXpert.equals(mb.getResult()))
			res += "R"+sign(mb.getR());
		if (MolecularBiologyResult.GenoTypeMTBDRplus.equals(mb.getResult()))
			res += "R"+sign(mb.getR())+", H"+sign(mb.getH());
		if (MolecularBiologyResult.GenoTypeMTBDRsl.equals(mb.getResult()))
			res += "Km"+sign(mb.getKm())+", Cm"+sign(mb.getCm())+", E"+sign(mb.getE())+", Lfx"+sign(mb.getLfx())+", Mfx"+sign(mb.getMfx());
		res += ")";
		return res;
	}
	
	/**
	 * Return "+" or "-" depending on boolean value 
	 * */
	 private String sign(Boolean val){
		 if (val)
			 return "+";
		 if (!val)
			 return "-";
		 return null;
	 }
}
