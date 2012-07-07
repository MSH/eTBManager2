package org.msh.tb.md;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.msh.tb.entities.Address;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.FieldValue;
import org.msh.tb.entities.Patient;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.TreatmentHealthUnit;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.DiagnosisType;
import org.msh.tb.entities.enums.DrugResistanceType;
import org.msh.tb.entities.enums.Gender;
import org.msh.tb.entities.enums.InfectionSite;
import org.msh.tb.entities.enums.Nationality;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.entities.enums.ValidationState;
import org.msh.tb.misc.FieldsOptions;
import org.msh.utils.date.DateUtils;
import org.msh.utils.date.Period;
import org.w3c.dom.Element;

/**
 * Import a single case from a XML data sent by SYMETA into etbmanager database 
 * @author Ricardo Memoria
 *
 */
public abstract class CaseImporting extends ImportingBase{

	private boolean newCase;
	
	private TbCase tbcase;
	private String caseId;
	private String patientName;
	
	
	/**
	 * Return the case classification to be used during importing 
	 * @return
	 */
	public abstract CaseClassification getCaseClassification();


	@Override
	public boolean importRecord(Element xmlCaseData) {
		EntityManager entityManager = getEntityManager();

		// default is new case, and not an existing one
		newCase = true;
		
		caseId = getValue(xmlCaseData, "ID", false);
		String firstName = getValue(xmlCaseData, "FIRSTNAME", false);
		String lastName = getValue(xmlCaseData, "LASTNAME", false);
		String fatherName = getValue(xmlCaseData, "FATHERNAME", false);

		Patient p = new Patient();
		p.setLastName(lastName);
		p.setName(firstName);
		p.setMiddleName(fatherName);
		p.setWorkspace(getWorkspace());
		patientName = p.getFullName();
		
		if (caseId == null) { 
			addError("ERROR: ID of the case was not specified");
			return false;
		}
		
		if ((patientName == null) || (patientName.isEmpty())) {
			addError("ERROR: Patient name must be specified");
			return false;
		}

		Integer gender = getValueAsInteger(xmlCaseData, "GENDER", false);
		Integer nationality = getValueAsInteger(xmlCaseData, "NATIONALITY", false);
		Date birthDate = getValueAsDate(xmlCaseData, "BIRTHDATE_string", false);
		Date beginTreatmentDate = getValueAsDate(xmlCaseData, "BEGINTREATMENTDATE_string", true);
		String unitId = getValue(xmlCaseData, "NOTIFICATIONUNIT", false);
		String personalNumber = getValue(xmlCaseData, "PERSONALIDNUMBER", false);
		String treatmentUnitId = getValue(xmlCaseData, "TREATMENTUNIT", true);
		Integer mainLocalization = getValueAsInteger(xmlCaseData, "MAINLOCALIZATION", false);
		String siteOfDisease = getValue(xmlCaseData, "SITEOFDISEASE", false);
		String addressLocality = getValue(xmlCaseData, "ADDRESS_LOCALITY", false);
		String addressSector = getValue(xmlCaseData, "ADDRESS_SECTOR", false);
		String addressRayon = getValue(xmlCaseData, "ADDRESS_RAYON", false);
		String addressHome = getValue(xmlCaseData, "ADDRESS_HOME", false);
		String typeOfResistance = getValue(xmlCaseData, "TYPEOFRESISTANCE", false);
		String typeOfPatient = getValue(xmlCaseData, "TYPEOFPATIENT", false);

		AdministrativeUnit admAddress = getAdressAdminUnit(addressRayon, addressSector, addressLocality);
				
		if (isErrorOnCurrentImport())
			return false;

		
		tbcase = null;
		try {
			List<TbCase> lstcases = entityManager.createQuery("from TbCase c " +
					"join fetch c.patient p " +
					"where c.legacyId = :id " +
					"and p.workspace.id = " + getWorkspace().getId().toString())
					.setParameter("id", caseId)
					.getResultList();

			if (lstcases.size() == 0) {
				tbcase = new TbCase();
			}
			else {
				tbcase = lstcases.get(0);
				newCase = false;
			}
		} catch (Exception e) {
			tbcase = new TbCase();
		}

		tbcase.setClassification(getCaseClassification());
		
		Tbunit treatmentUnit = loadTBUnit(treatmentUnitId);
		if (treatmentUnit == null) {
			addError("No TB Unit found with ID = " + unitId);
			treatmentUnit = entityManager.merge(getConfig().getDefaultTbunit());
		}

		Tbunit unit = loadTBUnit(unitId);
		
		if (unit == null)
			unit = treatmentUnit;
		
		if (unit == null) {
			addError("No unit specified");
			return false;
		}

		// handle nationality of the patient
		if (nationality != null) {
			if (nationality == 1)
			 	 tbcase.setNationality(Nationality.NATIVE);
			else tbcase.setNationality(Nationality.FOREIGN);
		}
		
		tbcase.setNotificationUnit(unit);
		tbcase.setLegacyId(caseId);
		tbcase.setDiagnosisDate(beginTreatmentDate);
		if (tbcase.getDiagnosisType() == null)
			tbcase.setDiagnosisType(DiagnosisType.CONFIRMED);
		tbcase.setRegistrationDate(beginTreatmentDate);

/*		Period treatPeriod = new Period();
		treatPeriod.setIniDate(beginTreatmentDate);
		tbcase.setTreatmentPeriod(treatPeriod);
		tbcase.setTreatmentUnit(treatmentUnit);
*/
		tbcase.setValidationState(ValidationState.VALIDATED);
		tbcase.setRegistrationCode(personalNumber);
		if (tbcase.getNotifAddress() == null)
			tbcase.setNotifAddress(new Address());
		tbcase.getNotifAddress().setAddress(addressHome);
		tbcase.getNotifAddress().setAdminUnit( admAddress );
		tbcase.setInfectionSite( getInfectionSite(mainLocalization) );
		tbcase.setPulmonaryType( getPulmonaryType(siteOfDisease) );
		tbcase.setDrugResistanceType( getDrugResistanceType(typeOfResistance) );
		tbcase.setPatientType( getPatientType(typeOfPatient) );

		// check if treatment must be included
		updateCaseState(treatmentUnit, beginTreatmentDate);
		
		p = tbcase.getPatient();
		if (p == null) {
			p = findPatient(firstName, fatherName, lastName, birthDate);
			tbcase.setPatient(p);					
		}
		else {
			p.setName(firstName);
			p.setLastName(lastName);
			p.setMiddleName(fatherName);
			p.setBirthDate(birthDate);
		}
		
		Gender g = null;
		if (gender != null) {
			if (gender == 1)
				 g = Gender.MALE;
			else g = Gender.FEMALE;
		}
		else {
			g = Gender.MALE;
			p.setRecordNumber(123456789);
		}
		
		p.setGender(g);
//		p.setSecurityNumber(securityId);
		p.setWorkspace(getWorkspace());

		if (p.getBirthDate() != null) {
			if (tbcase.getRegistrationDate() != null)
				tbcase.setAge(DateUtils.yearsBetween(p.getBirthDate(), tbcase.getRegistrationDate()));
			else tbcase.setAge(200);
		}
		else tbcase.setAge(200);
		
		entityManager.persist(p);
		entityManager.persist(tbcase);
		entityManager.flush();

		return newCase;
	}

	
	/**
	 * Search for a patient in a new case. If patient doesn't exist, a new one is returned
	 * @param firstName
	 * @param middleName
	 * @param lastName
	 * @param birthDate
	 * @return
	 */
	protected Patient findPatient(String firstName, String middleName, String lastName, Date birthDate) {
		List<Patient> lst = getEntityManager()
			.createQuery("from Patient where name=:name and middleName=:middleName and lastName=:lastName and birthDate=:dt")
			.setParameter("name", firstName)
			.setParameter("middleName", middleName)
			.setParameter("lastName", lastName)
			.setParameter("dt", birthDate)
			.getResultList();
		
		Patient p;
		if (lst.size() == 0) {
			p = new Patient();
			p.setName(firstName);
			p.setMiddleName(middleName);
			p.setLastName(lastName);
			p.setBirthDate(birthDate);
		}
		else p = lst.get(0);
		
		return p;
	}


	/**
	 * Update case state according to the data from the server
	 */
	protected void updateCaseState(Tbunit treatmentUnit, Date beginTreatmentDate) {
		// treatment information was set ?
		if ((treatmentUnit != null) && (beginTreatmentDate != null)) {

			// there is no information about treatment ?
			if (tbcase.getHealthUnits().size() == 0) {
				int monthsTreat;
				if (tbcase.getClassification() == CaseClassification.TB)
					monthsTreat = 6;
				else monthsTreat = 24;
				
				Date endDate = DateUtils.incMonths(beginTreatmentDate, monthsTreat);
				endDate = DateUtils.incDays(endDate, -1);
				
				TreatmentHealthUnit hu = new TreatmentHealthUnit();
				hu.setPeriod(new Period(beginTreatmentDate, endDate));
				hu.setTbCase(tbcase);
				hu.setTbunit(treatmentUnit);
				hu.setTransferring(false);
				tbcase.getHealthUnits().add(hu);
				tbcase.getTreatmentPeriod().set(hu.getPeriod());
				tbcase.setTreatmentUnit(treatmentUnit);
			}
			else {
				// Update data about current treatment
				TreatmentHealthUnit hu = tbcase.getHealthUnits().get( tbcase.getHealthUnits().size() - 1 );
				hu.setTbunit(treatmentUnit);
				if (hu.getPeriod() != null)
					hu.getPeriod().setIniDate(beginTreatmentDate);
			}
			
			tbcase.setState(CaseState.ONTREATMENT);
		}
		else tbcase.setState(CaseState.WAITING_TREATMENT);
	}
	
	/**
	 * Return the patient type from symetb
	 * @param typeOfPatient
	 * @return
	 */
	protected PatientType getPatientType(String typeOfPatient) {
		if ("1".equals(typeOfPatient))
			return PatientType.NEW;
		else
		if ("2".equals(typeOfPatient))
			return PatientType.RELAPSE;
		else
		if ("3".equals(typeOfPatient))
			return PatientType.AFTER_DEFAULT;
		else
		if ("4".equals(typeOfPatient))
			return PatientType.FAILURE;
		else
		if ("41".equals(typeOfPatient))
			return PatientType.FAILURE_FT;
		else
		if ("42".equals(typeOfPatient))
			return PatientType.FAILURE_RT;
		else
		if ("5".equals(typeOfPatient))
			return PatientType.OTHER;
		else
		if ("6".equals(typeOfPatient))
			return PatientType.OTHER;
		else
		if ("21".equals(typeOfPatient))
			return PatientType.RELAPSE;
		else
		if ("22".equals(typeOfPatient))
			return PatientType.RELAPSE;
		
		if (typeOfPatient != null)
			addError("Ukwown type of patient = " + typeOfPatient);

		return null;
	}


	/**
	 * get infection site based on main localization from symetb
	 * @param mainLocalization
	 * @return
	 */
	protected InfectionSite getInfectionSite(Integer mainLocalization) {
		if (mainLocalization != null) {
			switch (mainLocalization) {
			case 1: return InfectionSite.PULMONARY;
			case 2: return InfectionSite.EXTRAPULMONARY;
			default:
				addError("Unknown infection site = " + mainLocalization);
			}
		}
		return null;
	}

	
	/**
	 * Return pulmonary type based on legacy id
	 * @param legacyId
	 * @return
	 */
	protected FieldValue getPulmonaryType(String legacyId) {
		if ((legacyId == null) || (legacyId.isEmpty()))
			return null;
		
		FieldsOptions options = (FieldsOptions)Component.getInstance("fieldOptions", true);
		List<FieldValue> lst = options.getPulmonaryTypes();
		
		for (FieldValue fld: lst) {
			if (fld.getCustomId().equals(legacyId))
				return fld;
		}
		
		addError("Site of disease not found = " + legacyId);
		return null;
	}

	
	/**
	 * Return the drug resistance type according to symetb type of resistance
	 * @param typeOfResistance
	 * @return
	 */
	protected DrugResistanceType getDrugResistanceType(String typeOfResistance) {
		if ((typeOfResistance == null) || (typeOfResistance.isEmpty()))
			return null;
		
		if (typeOfResistance.equals("1"))
			return DrugResistanceType.MULTIDRUG_RESISTANCE;
		if (typeOfResistance.equals("2"))
			return DrugResistanceType.POLY_RESISTANCE;
		
		addError("Unkown type of resistance = " + typeOfResistance);
		return null;
	}

	
	/**
	 * Load the administrative unit of the patient address
	 * @param rayon
	 * @param sector
	 * @param locality
	 * @return
	 */
	protected AdministrativeUnit getAdressAdminUnit(String rayon, String sector, String locality) {
		String legacyId = locality;
		
		if (legacyId == null)
			legacyId = sector;
	
		if (legacyId == null)
			legacyId = rayon;

		if (legacyId == null) {
			addError("Rayon/Sector/Locality address not specified");
			return null;
		}

		AdministrativeUnit adm = loadAdminUnit(legacyId);
		if (adm == null) {
			addError("Rayon/Sector address number " + rayon + "/" + sector + "/" + locality + " not found");
		}
		return adm;
	}

	public boolean isNewCase() {
		return newCase;
	}

	public TbCase getTbcase() {
		return tbcase;
	}
	
	
	@Override
	public void addError(String msg) {
		String s = "";
		if (caseId != null)
			s = "(" + caseId + ") ";
		if (patientName != null)
			s += patientName + ": ";
		super.addError(s + msg);
	}
}
