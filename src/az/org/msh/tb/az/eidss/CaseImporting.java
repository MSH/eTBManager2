package org.msh.tb.az.eidss;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.msh.tb.az.entities.TbCaseAZ;
import org.msh.tb.entities.Patient;
import org.msh.tb.entities.Workspace;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.DiagnosisType;
import org.msh.tb.entities.enums.Gender;
import org.msh.tb.entities.enums.ValidationState;

/**
 * Import a single case from EIDSS into etbmanager database 
 *
 */
public  class CaseImporting {
	private static final int workspaceID = 8;
	private boolean newCase;

	private TbCaseAZ tbcase;
	private String caseId;
	private String patientName;
	private boolean errorOnCurrentImport;
	private int importedRecords;
	private int newRecords;
	private int errorsRecords;
	private List<String> errors = new ArrayList<String>();
	private EntityManager entityManager;

	/*
	 * add records from list to etb
	 */

	public boolean importRecords(List<CaseInfo> cases){
		entityManager = (EntityManager)Component.getInstance("entityManager");

		Iterator<CaseInfo> it=cases.iterator();
		while (it.hasNext()){
			CaseInfo oneCase=it.next();
			Patient p = new Patient();
			p.setLastName(oneCase.getLastName());
			p.setName(oneCase.getFirstName());
			p.setMiddleName(oneCase.getMiddleName());
			p.setBirthDate(oneCase.getDateOfBirth());
			p.setWorkspace(getWorkspace());
			p.setGender(Gender.MALE); //TODO change
			TbCaseAZ tbcase = new TbCaseAZ();
			tbcase.setClassification(CaseClassification.TB); //TODO change
			tbcase.setLegacyId(oneCase.getCaseID());
			tbcase.setDiagnosisDate(oneCase.getFinalDiagnosisDate());
			tbcase.setRegistrationDate(oneCase.getFinalDiagnosisDate());
			tbcase.setDiagnosisType(DiagnosisType.CONFIRMED); //TODO change
			tbcase.setValidationState(ValidationState.WAITING_VALIDATION); //TODO check IT
			tbcase.setPatient(p);
			tbcase.setAge(50);//mandatory, true value will be calculated automaticaly 
			tbcase.setState(CaseState.DEFAULTED);
			tbcase.setNotifAddressChanged(false);
			tbcase.setEIDSSComment(oneCase.getAdditionalComment());
			entityManager.persist(p);
			entityManager.persist(tbcase);
			entityManager.flush();
		}
		errorOnCurrentImport=true;
		return errorOnCurrentImport;

	}
	public List<String> CheckIfExistInEtb(List<String> allCases){
		List<String> resCases=new ArrayList<String>();
		Iterator<String> it= allCases.iterator();
		while (it.hasNext()){
			String legacy=it.next();
			try {
				List<TbCaseAZ> lstcases = entityManager.createQuery("from TbCase c " +
						"where c.legacyId = :id " +
						"and p.workspace.id = " + getWorkspace().getId().toString())
						.setParameter("id", legacy)
						.getResultList();

				if (lstcases.size() == 0) {
					resCases.add(legacy);
				}
			} catch (Exception e) {

			}
		}
		return resCases;

	}
	
public boolean isNewCase() {
		return newCase;
	}

	public TbCaseAZ getTbcase() {
		return tbcase;
	}



	public void addError(String msg) {
		String s = "";
		if (caseId != null)
			s = "(" + caseId + ") ";
		if (patientName != null)
			s += patientName + ": ";

	}

	public Workspace getWorkspace() {
		return getEntityManager().find(Workspace.class, workspaceID);
	}

	protected EntityManager getEntityManager() {
		return (EntityManager)Component.getInstance("entityManager");
	}
	public boolean isErrorOnCurrentImport() {
		return errorOnCurrentImport;
	}
	
	
	public int getImportedRecords() {
		return importedRecords;
	}
	public int getNewRecords() {
		return newRecords;
	}

	/**
	 * @return the errorsRecords
	 */
	public int getErrorsRecords() {
		return errorsRecords;
	}
	public List<String> getErrors() {
		return errors;
	}




}

