package org.msh.tb.md;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.msh.mdrtb.entities.Patient;
import org.msh.mdrtb.entities.TbCase;
import org.msh.mdrtb.entities.Tbunit;
import org.msh.mdrtb.entities.Workspace;
import org.msh.mdrtb.entities.enums.CaseClassification;
import org.msh.mdrtb.entities.enums.CaseState;
import org.msh.mdrtb.entities.enums.Gender;
import org.msh.utils.date.DateUtils;
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
	
	public boolean importCase(Element xmlCaseData, Workspace workspace) {
		EntityManager entityManager = getEntityManager();

		// default is new case, and not an existing one
		newCase = true;
		
		int numWarnings = getWarning().size();
		
		caseId = getValue(xmlCaseData, "ID", false);
		String firstName = getValue(xmlCaseData, "FIRSTNAME", false);
		String lastName = getValue(xmlCaseData, "LASTNAME", false);
		String fatherName = getValue(xmlCaseData, "FATHERNAME", false);
		
		if (caseId == null) 
			addWarning("ID of the case was not specified");

		Patient p = new Patient();
		p.setLastName(lastName);
		p.setName(firstName);
		p.setMiddleName(fatherName);
		p.setWorkspace(workspace);
		patientName = p.getFullName();
		
		if ((patientName == null) || (patientName.isEmpty()))
			addWarning("Patient name must be specified");

		Integer gender = getValueAsInteger(xmlCaseData, "GENDER", false);
		Date birthDate = getValueAsDate(xmlCaseData, "BIRTHDATE_string", false);
		String unitId = getValue(xmlCaseData, "NOTIFICATIONUNIT", true);
		Date beginTreatmentDate = getValueAsDate(xmlCaseData, "BEGINTREATMENTDATE_string", true);
		
		if (getWarning().size() > numWarnings)
			return false;
		
		TbCase tbcase;
		try {
			List<TbCase> lstcases = entityManager.createQuery("from TbCase c " +
					"join fetch c.patient p " +
					"where c.legacyId = :id " +
					"and p.workspace.id = " + getWorkspace().getId().toString())
					.setParameter("id", caseId)
					.getResultList();

			if (lstcases.size() == 0) {
				tbcase = new TbCase();
				tbcase.setClassification(CaseClassification.MDRTB_DOCUMENTED);
				tbcase.setState(CaseState.WAITING_TREATMENT);
			}
			else {
				tbcase = lstcases.get(0);
				newCase = false;
			}
		} catch (Exception e) {
			tbcase = new TbCase();
			tbcase.setClassification(CaseClassification.MDRTB_DOCUMENTED);
			tbcase.setState(CaseState.WAITING_TREATMENT);
		}
		
		Tbunit unit = loadTBUnit(unitId);
		if (unit == null) {
			addWarning("No TB Unit found with ID = " + unitId);
			unit = entityManager.merge(getConfig().getDefaultTbunit());
		}
		
		tbcase.setNotificationUnit(unit);
		tbcase.setLegacyId(caseId);
		tbcase.setDiagnosisDate(beginTreatmentDate);
		tbcase.setRegistrationDate(beginTreatmentDate);
		
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
	 * Add a new warning message during import a case
	 * @param message
	 */
	public void addWarning(String message) {
		addWarnMessage(caseId, patientName, message);
	}
	
	public boolean isNewCase() {
		return newCase;
	}

	public TbCase getTbcase() {
		return tbcase;
	}
}
