package org.msh.az;

import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.AdministrativeUnit;
import org.msh.mdrtb.entities.HealthSystem;
import org.msh.mdrtb.entities.Patient;
import org.msh.mdrtb.entities.Regimen;
import org.msh.mdrtb.entities.TbCase;
import org.msh.mdrtb.entities.Tbunit;
import org.msh.mdrtb.entities.Workspace;
import org.msh.mdrtb.entities.enums.CaseClassification;
import org.msh.mdrtb.entities.enums.CaseState;
import org.msh.mdrtb.entities.enums.DiagnosisType;
import org.msh.mdrtb.entities.enums.Gender;
import org.msh.mdrtb.entities.enums.PatientType;
import org.msh.mdrtb.entities.enums.ValidationState;
import org.msh.tb.cases.CaseEditingHome;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.PatientHome;
import org.msh.tb.cases.treatment.StartTreatmentHome;
import org.msh.tb.importexport.ImportBase;
import org.msh.utils.date.DateUtils;

@Name("az.caseImport")
public class CaseImport extends ImportBase {
	
	private static final int workspaceID = 940349;
	private static final int regimenID = 940780;
	private static final int unitID = 940725;
	private static final int adminUnitID = 958373;
	private static final int healthSystemID = 903;

	@In(create=true) CaseEditingHome caseEditingHome;
	@In(create=true) PatientHome patientHome;
	@In(create=true) StartTreatmentHome startTreatmentHome;
	@In(create=true) CaseHome caseHome;

	/* (non-Javadoc)
	 * @see org.msh.tb.importexport.ImportBase#importRecord(java.lang.String[])
	 */
	@Override
	public void importRecord(String[] values) {
		Integer recordNumber = parseInt(values[0]);
		String lastName = values[1];
		String middleName = values[2];
		String firstName = values[3];
		
		// check if patient was already imported
		String hql = "select count(*) from Patient where lastName = :last and middleName = :middle and name = :name";
		long num = (Long) getEntityManager().createQuery(hql)
			.setParameter("name", firstName)
			.setParameter("middle", middleName)
			.setParameter("last", lastName)
			.getSingleResult();
		if (num > 0)
			return;
		
		Integer yearBirth = parseInt(values[4]);
		String city = values[5];
		String address = values[6];
		PatientType patientType = parsePatientType( values[7] );
		Date startTreatDate = parseDate(values[8]);
		if (startTreatDate == null) {
			System.out.println("ERROR IMPORTING (" + recordNumber + ") " + lastName);
			return;
		}
		String healthUnit = values[9];
//		String healthUnitType = values[10];
		CaseState caseState = parseOutcome(values[11]);

		patientHome.clearInstance();
		caseHome.clearInstance();
		Patient p = patientHome.getInstance();
		p.setLastName(lastName);
		p.setMiddleName(middleName);
		p.setName(firstName);
		p.setRecordNumber(recordNumber);
		p.setGender(Gender.MALE);
		
		TbCase tbcase = caseEditingHome.getTbcase();
		
		tbcase.setPatient(p);
		int age = 999;
		if (yearBirth != null)
			age = DateUtils.yearOf(startTreatDate) - yearBirth;
		
		tbcase.setDiagnosisType(DiagnosisType.CONFIRMED);
		tbcase.setDiagnosisDate(startTreatDate);
		tbcase.setAge(age);
		tbcase.setRegistrationDate(startTreatDate);
		tbcase.setPatientType(patientType);
		tbcase.setValidationState(ValidationState.PENDING);
		tbcase.getNotifAddress().setAddress(address);
		tbcase.setClassification(CaseClassification.DRTB);

		System.out.println("## Importing " + p.getFullName());
		AdministrativeUnit adminUnit = getAdminUnit(city);
//		tbcase.getNotifAddress().setAdminUnit(adminUnit);
		caseEditingHome.getNotifAdminUnit().setSelectedUnit(adminUnit);
		
		Tbunit unit = getTbunit(healthUnit, adminUnit);
		caseEditingHome.getTbunitselection().setTbunit(unit);

		if (caseState == CaseState.ONTREATMENT) {
			startTreatmentHome.setIniTreatmentDate(startTreatDate);
			Regimen reg = getEntityManager().find(Regimen.class, regimenID);
			startTreatmentHome.setRegimen( reg );
			startTreatmentHome.setSaveChages(true);
			startTreatmentHome.setUseDefaultDoseUnit(true);
			// standard regimen
			caseEditingHome.setRegimenType(1);
		}
		else caseEditingHome.setRegimenType(0);
		
		caseHome.setTransactionLogActive(false);
		caseEditingHome.saveNew();

		tbcase.setState(caseState);
		getEntityManager().persist(tbcase);
		
	}


	/**
	 * @param s
	 * @return
	 */
	protected CaseState parseOutcome(String s) {
		if ((s == null) || (s.isEmpty()))
			return CaseState.ONTREATMENT;
		
		s = s.toLowerCase();
		if (s.equals("cure"))
			return CaseState.CURED;
		
		if (s.equals("died"))
			return CaseState.DIED;
		
		if (s.equals("fail"))
			return CaseState.FAILED;
		
		if (s.equals("def"))
			return CaseState.DEFAULTED;
		
		System.out.println("*** OUTCOME NOT FOUND: " + s);
		
		return CaseState.ONTREATMENT;
	}

	
	/**
	 * @param s
	 * @return
	 */
	protected PatientType parsePatientType(String s) {
		if ((s == null) || (s.isEmpty()))
			return null;
		
		s = s.toLowerCase();
		
		if (s.equals("after failure"))
			return PatientType.FAILURE;
		else return null;
	}


	protected Integer parseInt(String s) {
		try {
			return Integer.parseInt(s);
		} catch (Exception e) {
			return null;
		}
	}
	
	
	protected Date parseDate(String s) {
		try {
			Integer dia = Integer.parseInt(s.substring(0, 2));
			Integer mes = Integer.parseInt(s.substring(3, 5));
			Integer ano = Integer.parseInt(s.substring(6, 10));
			if ((dia == null) || (mes == null) || (ano == null))
				return null;
			
			return DateUtils.newDate(ano, mes, dia);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Load the administrative unit of the patient address
	 * @param rayon
	 * @param sector
	 * @param locality
	 * @return
	 */
	protected AdministrativeUnit getAdminUnit(String name) {
		List<AdministrativeUnit> lst = getEntityManager()
			.createQuery("from AdministrativeUnit aux where aux.workspace.id = :id and upper(aux.name.name1) = :name")
			.setParameter("id", workspaceID)
			.setParameter("name", name.toUpperCase())
			.getResultList();
	
		if (lst.size() == 0)
			 return getEntityManager().find(AdministrativeUnit.class, adminUnitID);
		else return lst.get(0);
	}


	/**
	 * @param name
	 * @return
	 */
	protected Tbunit getTbunit(String name, AdministrativeUnit adminUnit) {
		if (!name.isEmpty()) {
			List<Tbunit> lst = getEntityManager()
			.createQuery("from Tbunit aux where aux.workspace.id = :id and upper(aux.name.name1) = :name")
			.setParameter("id", workspaceID)
			.setParameter("name", name.toUpperCase())
			.getResultList();
			if (lst.size() == 0) 
				 return newTbunit(name, adminUnit);
			else return lst.get(0);
		}
		else return getEntityManager().find(Tbunit.class, unitID);
	}

	
	protected Tbunit newTbunit(String name, AdministrativeUnit adminUnit) {
		Tbunit unit = new Tbunit();
		unit.setActive(true);
		unit.getName().setName1(name);
		unit.setAdminUnit(adminUnit);
		unit.setChangeEstimatedQuantity(true);
		unit.setHealthSystem(getEntityManager().find(HealthSystem.class, healthSystemID));
		unit.setMdrHealthUnit(true);
		unit.setTreatmentHealthUnit(true);
		unit.setWorkspace( getWorkspace() );
		
		getEntityManager().persist(unit);
		return unit;
	}
	

	@Override
	public Workspace getWorkspace() {
		return getEntityManager().find(Workspace.class, workspaceID);
	}
}
