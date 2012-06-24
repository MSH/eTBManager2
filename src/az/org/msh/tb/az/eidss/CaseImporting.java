package org.msh.tb.az.eidss;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.transaction.UserTransaction;
import org.msh.tb.az.entities.TbCaseAZ;
import org.msh.tb.entities.Patient;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.Workspace;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.DiagnosisType;
import org.msh.tb.entities.enums.Gender;
import org.msh.tb.entities.enums.ValidationState;

/**
 * Import cases from case DTO list. Fully independent from specific EIDSS web services, so, from data structures
 */
public  class CaseImporting {
	private static final Integer workspaceID = 8;
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
	ArrayList<CaseInfo> caseInfo;
	private UserTransaction transaction;


	public CaseImporting(){
		entityManager = (EntityManager)Component.getInstance("entityManager");
		errorOnCurrentImport=true;
	}
	/*
	 * add records from list to etb
	 */	
	public boolean importRecords(CaseInfo oneCase){

		if (entityManager==null){
			entityManager = (EntityManager)Component.getInstance("entityManager");
		}
		List<TbCase> result=isCaseExist(oneCase);
		if (result.size()==0){
			errorOnCurrentImport=WriteCase(oneCase);
		} else {
			if (result.size()==1)
				errorOnCurrentImport=UpdateCase(oneCase,result);
		}



		return errorOnCurrentImport;

	}

	private boolean UpdateCase(CaseInfo oneCase, List<TbCase> result) {
		Iterator<TbCase> it=result.iterator();
		TbCase c=it.next();
		c.setLegacyId(oneCase.getCaseID());
		entityManager.persist(c);
		return true;

	}
	private List<TbCase> isCaseExist(CaseInfo oneCase) {
		List<TbCase> result=new ArrayList<TbCase>();
		if (entityManager==null){
			entityManager = (EntityManager)Component.getInstance("entityManager");
		}
		try {
			List<TbCase> lstcases = entityManager.createQuery(" from TbCase c " +
					"join fetch c.patient p " 
					//		+" where c.legacyId = :id "
					+" where c.registrationDate = :rd "
					+" and p.lastName = :ln " 
					+" and p.middleName = :mn " 
					//+ "and p.workspace.id = " + s)
					+ " and p.workspace.id = " + workspaceID.toString())
					//	.setParameter("id", oneCase.getCaseID())
					.setParameter("ln", oneCase.getLastName())
					.setParameter("mn", oneCase.getMiddleName())
					.setParameter("rd", oneCase.getFinalDiagnosisDate())
					.getResultList();
			if (lstcases.size() != 0) {
				Iterator <TbCase> it= lstcases.iterator();
				TbCase etbInfo =it.next();
				if (etbInfo.getPatient().getBirthDate().getYear()==oneCase.getDateOfBirth().getYear() && 
						etbInfo.getPatient().getName().equalsIgnoreCase(oneCase.getFirstName())){
					result.add(etbInfo);
				}

			}

		} catch (Exception e) {

		}
		return result;
	}
	public boolean WriteCase(CaseInfo oneCase){

		errorOnCurrentImport=true;
		Patient p = new Patient();
		p.setLastName(oneCase.getLastName());
		p.setName(oneCase.getFirstName());
		p.setMiddleName(oneCase.getMiddleName());
		p.setBirthDate(oneCase.getDateOfBirth());
		p.setWorkspace(getWorkspace());

		if (oneCase.getPatientGender()!="10043001"){
			p.setGender(Gender.MALE); 
		} else {
			p.setGender(Gender.FEMALE);
		}
		TbCaseAZ tbcase = new TbCaseAZ();
		tbcase.setClassification(CaseClassification.TB); //TODO change
		tbcase.setLegacyId(oneCase.getCaseID());
		tbcase.setDiagnosisDate(oneCase.getFinalDiagnosisDate());
		tbcase.setRegistrationDate(oneCase.getFinalDiagnosisDate());
		tbcase.setDiagnosisType(DiagnosisType.CONFIRMED); //TODO change
		tbcase.setValidationState(ValidationState.WAITING_VALIDATION); 
		tbcase.setPatient(p);
		tbcase.setAge(50);//mandatory, true value will be calculated automatically 
		tbcase.setState(CaseState.WAITING_TREATMENT);
		tbcase.setNotifAddressChanged(true);
		tbcase.setEIDSSComment(oneCase.getAdditionalComment());
		beginTransaction();
		entityManager.persist(p);
		entityManager.persist(tbcase);
		entityManager.flush();
		commitTransaction();
		return errorOnCurrentImport;

	}
	public List<String> CheckIfExistInEtb(List<String> allCases){
		entityManager = (EntityManager)Component.getInstance("entityManager");
		BigInteger count;
		List<String> resCases=new ArrayList<String>();
		Iterator<String> it= allCases.iterator();
		while (it.hasNext()){
			String legacy=it.next();
			try {
				/*
				List<TbCaseAZ> lstcases = entityManager.createQuery("from TbCase c " +
						"join fetch c.patient p " +
						"where c.legacyId = :id " +
						"and p.workspace.id = " + workspaceID.toString())
						.setParameter("id", legacy)
						.getResultList();

				if (lstcases.size() == 0) {
					resCases.add(legacy);
				}
				 */
				count = (BigInteger) entityManager.createNativeQuery(getEIDSSCodeSQL(legacy)).getSingleResult();
				if (count.intValue() == 0){
					resCases.add(legacy);
				}
			} catch (Exception e) {

			}
		}
		return resCases;

	}
	/**
	 * Get native SQL to search TBCase by EIDSS code
	 * @param eidssCode
	 * @return
	 */
	private String getEIDSSCodeSQL(String eidssCode){
		String s = "select count(*) from tbcaseAZ cAZ inner join tbcase c on cAZ.id=c.id";
		s = s+ " where c.legacyId ='" + eidssCode + "'";
		return s;
	}

	public boolean isNewCase() {
		return newCase;
	}

	//public TbCaseAZ getTbcase() {
	//	return tbcase;
	//}



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


	public void beginTransaction() {
		try {
			getTransaction().begin();
			getEntityManager().joinTransaction();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	/**
	 * Commit a transaction that is under progress 
	 */
	public void commitTransaction() {
		try {
			getTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	protected UserTransaction getTransaction() {
		if (transaction == null)
			transaction = (UserTransaction)Component.getInstance("org.jboss.seam.transaction.transaction");
		return transaction;
	}





}

