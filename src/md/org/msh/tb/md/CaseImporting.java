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
import org.msh.tb.entities.Workspace;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.DiagnosisType;
import org.msh.tb.entities.enums.DrugResistanceType;
import org.msh.tb.entities.enums.Gender;
import org.msh.tb.entities.enums.InfectionSite;
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
public class CaseImporting extends ImportingBase{

	private boolean newCase;
	
	private TbCase tbcase;
	private String caseId;
	private String patientName;
	
	public boolean importCase(Element xmlCaseData, Workspace workspace, CaseClassification classification) {
		EntityManager entityManager = getEntityManager();
		setWorkspace(workspace);

		// default is new case, and not an existing one
		newCase = true;
		
		int numWarnings = getWarning().size();
		
		caseId = getValue(xmlCaseData, "ID", false);
		String firstName = getValue(xmlCaseData, "FIRSTNAME", false);
		String lastName = getValue(xmlCaseData, "LASTNAME", false);
		String fatherName = getValue(xmlCaseData, "FATHERNAME", false);

		Patient p = new Patient();
		p.setLastName(lastName);
		p.setName(firstName);
		p.setMiddleName(fatherName);
		p.setWorkspace(workspace);
		patientName = p.getFullName();
		
		if (caseId == null) { 
			addMessage("ERROR: ID of the case was not specified");
			return false;
		}
		
		if ((patientName == null) || (patientName.isEmpty())) {
			addMessage("ERROR: Patient name must be specified");
			return false;
		}

		Integer gender = getValueAsInteger(xmlCaseData, "GENDER", false);
		Date birthDate = getValueAsDate(xmlCaseData, "BIRTHDATE_string", false);
		Date beginTreatmentDate = getValueAsDate(xmlCaseData, "BEGINTREATMENTDATE_string", true);
		String unitId = getValue(xmlCaseData, "NOTIFICATIONUNIT", false);
		String personalNumber = getValue(xmlCaseData, "PERSONALNUMBER", false);
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
				
		if (getWarning().size() > numWarnings)
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
				tbcase.setState(CaseState.WAITING_TREATMENT);
			}
			else {
				tbcase = lstcases.get(0);
				newCase = false;
			}
		} catch (Exception e) {
			tbcase = new TbCase();
			tbcase.setClassification(CaseClassification.DRTB);
			tbcase.setState(CaseState.WAITING_TREATMENT);
		}

		tbcase.setClassification(classification);
		
		Tbunit treatmentUnit = loadTBUnit(treatmentUnitId);
		if (treatmentUnit == null) {
			addMessage("No TB Unit found with ID = " + unitId);
			treatmentUnit = entityManager.merge(getConfig().getDefaultTbunit());
		}

		Tbunit unit = loadTBUnit(unitId);
		
		if (unit == null)
			unit = treatmentUnit;
		
		if (unit == null) {
			addMessage("No unit specified");
			return false;
		}
		
		tbcase.setNotificationUnit(unit);
		tbcase.setLegacyId(caseId);
		tbcase.setDiagnosisDate(beginTreatmentDate);
		if (tbcase.getDiagnosisType() == null)
			tbcase.setDiagnosisType(DiagnosisType.CONFIRMED);
		tbcase.setRegistrationDate(beginTreatmentDate);

		Period treatPeriod = new Period();
		treatPeriod.setIniDate(beginTreatmentDate);
		tbcase.setTreatmentPeriod(treatPeriod);
		tbcase.setTreatmentUnit(treatmentUnit);

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
		
		p = tbcase.getPatient();
		if (p == null) {
			p = new Patient();
			tbcase.setPatient(p);					
		}
		p.setName(firstName);
		p.setLastName(lastName);
		
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
		p.setBirthDate(birthDate);
//		p.setSecurityNumber(securityId);
		p.setWorkspace(getWorkspace());
		p.setMiddleName(fatherName);

		if (p.getBirthDate() != null) {
			if (tbcase.getRegistrationDate() != null)
				tbcase.setAge(DateUtils.yearsBetween(p.getBirthDate(), tbcase.getRegistrationDate()));
			else tbcase.setAge(200);
		}
		else tbcase.setAge(200);
		
		entityManager.persist(p);
		entityManager.persist(tbcase);
		entityManager.flush();

		return true;
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
			addMessage("Ukwown type of patient = " + typeOfPatient);

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
				addMessage("Unknown infection site = " + mainLocalization);
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
		
		addMessage("Site of disease not found = " + legacyId);
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
		
		addMessage("Unkown type of resistance = " + typeOfResistance);
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
			addMessage("Rayon/Sector/Locality address not specified");
			return null;
		}

		AdministrativeUnit adm = loadAdminUnit(legacyId);
		if (adm == null) {
			addMessage("Rayon/Sector address number " + rayon + "/" + sector + "/" + locality + " not found");
		}
		return adm;
	}

	/**
	 * Add a new warning message during import a case
	 * @param message
	 */
	@Override
	public WarnMessage addMessage(String message) {
		return addWarnMessage(caseId, patientName, message);
	}
	
	public boolean isNewCase() {
		return newCase;
	}

	public TbCase getTbcase() {
		return tbcase;
	}
}
