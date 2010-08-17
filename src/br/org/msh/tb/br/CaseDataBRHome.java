package org.msh.tb.br;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.beanutils.PropertyUtils;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.faces.FacesMessages;
import org.msh.entities.CaseDataBR;
import org.msh.entities.MolecularBiology;
import org.msh.entities.enums.MolecularBiologyResult;
import org.msh.mdrtb.entities.AdministrativeUnit;
import org.msh.mdrtb.entities.ExamCulture;
import org.msh.mdrtb.entities.ExamHIV;
import org.msh.mdrtb.entities.ExamSputumSmear;
import org.msh.mdrtb.entities.ExamSusceptibilityResult;
import org.msh.mdrtb.entities.ExamXRay;
import org.msh.mdrtb.entities.FieldValue;
import org.msh.mdrtb.entities.MedicalExamination;
import org.msh.mdrtb.entities.Patient;
import org.msh.mdrtb.entities.TbCase;
import org.msh.mdrtb.entities.UserWorkspace;
import org.msh.mdrtb.entities.enums.CaseState;
import org.msh.mdrtb.entities.enums.CultureResult;
import org.msh.mdrtb.entities.enums.Gender;
import org.msh.mdrtb.entities.enums.PatientType;
import org.msh.mdrtb.entities.enums.SputumSmearResult;
import org.msh.mdrtb.entities.enums.SusceptibilityResultTest;
import org.msh.mdrtb.entities.enums.YesNoType;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.adminunits.AdminUnitSelection;
import org.msh.tb.cases.CaseEditingHome;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.ComorbidityHome;
import org.msh.tb.cases.ExamCultureHome;
import org.msh.tb.cases.ExamHIVHome;
import org.msh.tb.cases.ExamSputumHome;
import org.msh.tb.cases.ExamSusceptHome;
import org.msh.tb.cases.ExamXRayHome;
import org.msh.tb.cases.MedicalExaminationHome;
import org.msh.tb.cases.treatment.StartTreatmentHome;
import org.msh.tb.misc.FieldsOptions;
import org.msh.tb.tbunits.TBUnitSelection;

@Name("caseDataBRHome")
public class CaseDataBRHome extends EntityHomeEx<CaseDataBR> {
	private static final long serialVersionUID = 8895495231680746480L;

	@In EntityManager entityManager;
	@In(create=true) CaseHome caseHome;
	@In(create=true) CaseEditingHome caseEditingHome;
	@In(required=false) ExamSputumHome examSputumHome;
	@In(required=false) ExamCultureHome examCultureHome;
	@In(create=true) StartTreatmentHome startTreatmentHome;
//	@In(required=false) TreatmentHealthUnitHome treatmentHealthUnitHome;
	@In(required=false) ExamSusceptHome examSusceptHome;
	@In(required=false) ExamHIVHome examHIVHome;
	@In(required=false) ComorbidityHome comorbidityHome;
	@In(required=false) MedicalExaminationHome medicalExaminationHome;
	@In(required=false) MolecularBiologyHome molecularBiologyHome;
	@In(create=true) FacesMessages facesMessages;
	@In(create=true) ExamXRayHome examXRayHome;
	@In(create=true) FieldsOptions fieldOptions;
	
	private AdminUnitSelection auselection;
	private Integer dstRequired;

	@Factory("caseDataBR")
	public CaseDataBR getCaseDataBR() {
		try {
			entityManager.createQuery("from CaseDataBR c where c.tbcase.id = #{caseHome.id}").getSingleResult();
			setId(caseHome.getId());
			getAuselection().setSelectedUnit(getInstance().getAdminUnitUsOrigem());
		} catch (Exception e) {
			setId(null);
		}
		return getInstance();
	}

	
	/**
	 * Called when a new notification is about to begin
	 */
	@Observer("new-notification")
	public void initializeNewNotification() {
		UserWorkspace userWorkspace = (UserWorkspace)Component.getInstance("userWorkspace");

		// initialize administrative units
		if ((userWorkspace != null) && (getAuselection().getSelectedUnit() == null)) {
			AdministrativeUnit au = userWorkspace.getAdminUnit();
			if (au == null)
				au = userWorkspace.getTbunit().getAdminUnit();

			au = entityManager.find(AdministrativeUnit.class, au.getId());
			getAuselection().setSelectedUnit(au);

			while (au.getParent() != null)
				au = au.getParent();
				
			if (examCultureHome == null)
				examCultureHome = (ExamCultureHome)Component.getInstance("examCultureHome", true);
			if (examSusceptHome == null)
				examSusceptHome = (ExamSusceptHome)Component.getInstance("examSusceptHome", true);
			if (molecularBiologyHome == null)
				molecularBiologyHome = (MolecularBiologyHome)Component.getInstance("molecularBiologyHome", true);
			
			examCultureHome.getLabselection().setAdminUnit(au);
			examSusceptHome.getLabselection().setAdminUnit(au);
			molecularBiologyHome.getLabselection().setAdminUnit(au);
		}
		
	}
	
	
	/**
	 * Save a new TB or MDR-TB case for the Brazilian version 
	 * @return "persisted" if successfully saved
	 */
	@Transactional
	public String saveNew() {
		if (!validateForm())
			return "error";

//		if (caseEditingHome!=null)
//			return "error";

		TbCase tbcase = caseEditingHome.getTbcase();
		tbcase.setDiagnosisDate(startTreatmentHome.getIniTreatmentDate());
		tbcase.setState(CaseState.WAITING_TREATMENT);

		adjustFields();

		String ret = caseEditingHome.saveNew();

		if ("error".equals(ret))
			return ret;
		
		CaseDataBR data = getInstance();
		
		data.setTbcase(tbcase);
		data.setId(tbcase.getId());
		data.setCurrAddressNumber(data.getNotifAddressNumber());
		data.setAdminUnitUsOrigem(getAuselection().getSelectedUnit());

		caseHome.setTransactionLogActive(false);
		
		// save exam results
		if (!saveExams()) 
			return "error";
		
		saveComorbidities();
		
		saveXRay();
		
		saveMedicalExamination();
		
		persist();
		
		if (caseEditingHome.getRegimenType() == 2)
			 return "individualized";
		else return ret;
	}


	/**
	 * Change the string property to upper case
	 * @param obj
	 * @param propName
	 */
	protected void setPropertyUpperCase(Object obj, String propName) {
		try {
			String value = (String)PropertyUtils.getProperty(obj, propName);
			if (value != null) {
				value = value.toUpperCase();
				PropertyUtils.setProperty(obj, propName, value);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error reading property " + propName + " of class " + obj.getClass().getCanonicalName());
		}
	}
	
	/**
	 * Save an existing TB or MDR-TB case of the Brazilian version
	 * @return "persisted" if successfully saved
	 */
	@Transactional
	public String saveEditing() {
		if (!validateForm())
			return "error";
		
		if (!caseEditingHome.saveEditing().equals("persisted"))
			return "error";
		
		adjustFields();

		CaseDataBR data = getInstance();
		TbCase tbcase = caseEditingHome.getTbcase();
		
		if (data.getId() == null)
			data.setId(tbcase.getId());

		if (!tbcase.isNotifAddressChanged())
			data.setCurrAddressNumber(data.getNotifAddressNumber());
		data.setAdminUnitUsOrigem(getAuselection().getSelectedUnit());
		
		setDisplayMessage(false);

		return persist(); 
	}

	
		/**
	 * change the text fields to upper case
	 */
	protected void adjustFields() {
		TbCase tbcase = caseHome.getInstance();
		setPropertyUpperCase(tbcase.getNotifAddress(), "address");
		setPropertyUpperCase(tbcase.getNotifAddress(), "complement");
		setPropertyUpperCase(tbcase.getNotifAddress(), "zipCode");

		setPropertyUpperCase(tbcase.getCurrentAddress(), "address");
		setPropertyUpperCase(tbcase.getCurrentAddress(), "complement");
		setPropertyUpperCase(tbcase.getCurrentAddress(), "zipCode");
		
		// set patient fields to upper case
		Patient p = tbcase.getPatient();
		setPropertyUpperCase(p, "name");
		setPropertyUpperCase(p, "middleName");
		setPropertyUpperCase(p, "lastName");
		setPropertyUpperCase(p, "motherName");
		setPropertyUpperCase(p, "securityNumber");
		setPropertyUpperCase(p, "motherName");

		CaseDataBR data = getInstance();
		
		setPropertyUpperCase(data, "numSinan");
		setPropertyUpperCase(data, "usOrigem");
		setPropertyUpperCase(data, "country");
		setPropertyUpperCase(data, "currAddressNumber");
		setPropertyUpperCase(data, "notifAddressNumber");
		setPropertyUpperCase(data, "prefixMobile");
		setPropertyUpperCase(data, "prefixPhone");
		setPropertyUpperCase(data, "notifDistrict");
		setPropertyUpperCase(data, "currDistrict");
		
		PatientType tp = tbcase.getPatientType();
		if (tp != PatientType.SCHEMA_CHANGED)
			data.setSchemaChangeType(null);
		if (tp != PatientType.MICROBACTERIOSE)
			data.setMicrobacteriose(null);
		if (tp != PatientType.RESISTANT)
			data.setResistanceType(null);
		if (tp != PatientType.FAILURE)
			data.setFailureType(null);
		if ((tp != PatientType.NEW_SPECIAL) && (tp != PatientType.OTHER))
			tbcase.setPatientTypeOther(null);
		
		if (p.getGender() == Gender.MALE)
			data.setPregnancePeriod(null);
	}

	
	/**
	 * Adjust values of the medical examination fields
	 */
	protected void adjustMedicalExaminationFields() {
		if (medicalExaminationHome == null)
			return;

		MedicalExamination medExam = medicalExaminationHome.getInstance();
		setPropertyUpperCase(medExam, "positionResponsible");
		setPropertyUpperCase(medExam, "responsible");
		if (!YesNoType.YES.equals(medExam.getSupervisedTreatment()))
			medExam.setSupervisionUnitName(null);
		setPropertyUpperCase(medExam, "supervisionUnitName");		
	}


	/**
	 * Save medical examination
	 * @return
	 */
	public String saveMedicalExamination() {
		if (medicalExaminationHome == null)
			return "error";

		adjustMedicalExaminationFields();
		return medicalExaminationHome.persist();
	}


	/**
	 * Check if information is ready to be saved
	 * @return
	 */
	protected boolean validateForm() {
		boolean res = caseEditingHome.validateData();

		TbCase tbcase = caseHome.getInstance();
		
		// check pulmonary type
		if ((tbcase.isPulmonary()) && (tbcase.getPulmonaryType() == null)) {
			facesMessages.addToControlFromResourceBundle("cbpulmonary", "javax.faces.component.UIInput.REQUIRED");
			res = false;
		}
		
		if (medicalExaminationHome != null) {
			MedicalExamination medExam = medicalExaminationHome.getInstance();
			if (YesNoType.YES.equals(medExam.getSupervisedTreatment())) {
				if ((medExam.getSupervisionUnitName() == null) || (medExam.getSupervisionUnitName().isEmpty())) {
					facesMessages.addToControlFromResourceBundle("supUnitName", "javax.faces.component.UIInput.REQUIRED");
					res = false;
				}
			}
		}
		
		// check exams
		if ((examCultureHome != null) && (examSputumHome != null)) {
			if ((!examCultureHome.getInstance().getResult().equals(CultureResult.NOTDONE)) || 
				(!examSputumHome.getInstance().getResult().equals(SputumSmearResult.NOTDONE))) 
			{
				if (examCultureHome.getSample().getDateCollected() == null) {
					facesMessages.addToControlFromResourceBundle("edtdatecollected", "javax.faces.component.UIInput.REQUIRED");
					res = false;
				}

				if (examCultureHome.getLabselection().getLaboratory() == null) {
					facesMessages.addToControlFromResourceBundle("cblabculture", "javax.faces.component.UIInput.REQUIRED");
					res = false;
				}
			}
			
			if (!examCultureHome.validateDates()) {
				facesMessages.addToControlFromResourceBundle("edtdatecollected", "cases.exams.datereleasebeforecol");
				res = false;
			}
		}
		
		if (molecularBiologyHome != null) {
			MolecularBiologyResult resmb = molecularBiologyHome.getInstance().getResult(); 
			if (!MolecularBiologyResult.NOTDONE.equals(resmb))
				res = res || molecularBiologyHome.validate();
		}
		
//		if ((!caseHome.isManaged()) && (treatmentHealthUnitHome != null) && (treatmentHealthUnitHome.getRegimen() == null))
//			return false;
		return res;
	}


	/**
	 * Save exams results for DST, Culture and microscopy
	 */
	protected boolean saveExams() {
		if ((examCultureHome == null) || (examSputumHome == null))
			return true;

		ExamCulture examCulture = examCultureHome.getInstance();
		ExamSputumSmear examSputum = examSputumHome.getInstance();
		MolecularBiology molecularBiology = molecularBiologyHome.getInstance();

		// turn off message displaying
		examSputumHome.setDisplayMessage(false);
		examCultureHome.setDisplayMessage(false);
		molecularBiologyHome.setDisplayMessage(false);
		examSusceptHome.setDisplayMessage(false);
		examHIVHome.setDisplayMessage(false);

		Date dtCollected = examCultureHome.getSample().getDateCollected();
		
//		examSputum.setMethod(examCulture.getMethod());

		// save microscopy exam
		if (!SputumSmearResult.NOTDONE.equals(examSputum.getResult())) {
			if (!examSputumHome.validateDates()) {
				facesMessages.addToControlFromResourceBundle("dtrelease", "cases.exams.datereleasebeforecol");
				return false;			
			}
			examSputumHome.getSample().setDateCollected(dtCollected);
			examSputum.setDateRelease(examCulture.getDateRelease());
			examSputum.setLaboratory(examCulture.getLaboratory());
			examSputumHome.persist();
		}
		
		// save culture exam
		if (!CultureResult.NOTDONE.equals(examCulture.getResult())) {
			if (!examCultureHome.validateDates()) {
				facesMessages.addToControlFromResourceBundle("dtrelease", "cases.exams.datereleasebeforecol");
				return false;
			}			
			examCultureHome.persist();
		}

		// save molecular biology
		if (!MolecularBiologyResult.NOTDONE.equals(molecularBiology.getResult()))
			molecularBiologyHome.persist();

		if (dtCollected == null) {
			dtCollected = molecularBiology.getDate();
		}

		// if there is no collected date, then the DST was not done
		if (dtCollected == null)
			return true;
		
		// check DST
		boolean hasResult = false;
		for (ExamSusceptibilityResult res: examSusceptHome.getItems()) {
			if ((res.getResult() != null) && (res.getResult().equals(SusceptibilityResultTest.NOTDONE))) {
				hasResult = true;
				break;
			}
		}
		
		if (hasResult) {
			examSusceptHome.getSample().setDateCollected(dtCollected);
			examSusceptHome.persist();
		}
		
		// check HIV
		ExamHIV exam = examHIVHome.getInstance();
		if (exam.getResult() != null) {
			examHIVHome.persist();
		}
		
		return true;
	}

	
	/**
	 * Save X-Ray exam during case notification
	 * @return true - if saved, otherwise false
	 */
	protected boolean saveXRay() {
		TbCase tbcase = caseHome.getInstance();
		
		if ((!tbcase.isPulmonary()) || (tbcase.getPulmonaryType() == null)) {
			return false;
		}
		
		Date notifDate = tbcase.getRegistrationDate();
		String pulmCode = tbcase.getPulmonaryType().getCustomId();
		
		if ((pulmCode == null) || (pulmCode.isEmpty()))
			return false;
		
		List<FieldValue> presentations = fieldOptions.getXRayPresentations();
		
		FieldValue presentation = null;
		for (FieldValue val: presentations) {
			if (val.getCustomId().equals(pulmCode)) {
				presentation = val;
				break;
			}
		}
		
		if (presentation == null)
			return false;
		
		// start a new exam
		examXRayHome.setId(null);
		ExamXRay exam = examXRayHome.getInstance();
		exam.setDate(notifDate);
		exam.setPresentation(presentation);
		examXRayHome.persist();
		
		return true;
	}
	
	
	/**
	 * Initialize individualized regimen editing
	 */
	public void initializeIndividualizedRegimen() {
		TBUnitSelection tbsel = startTreatmentHome.getTbunitselection();
		if (tbsel.getTbunit() == null) {
			tbsel.setTbunit(caseHome.getInstance().getNotificationUnit());
		}
	}

	
	/**
	 * Save treatment editing for individualized regimen
	 * @return
	 */
	public String saveTreatmentEditing() {
		String ret = startTreatmentHome.startIndividualizedRegimen(); 
		if (ret.equals("persisted")) {
			TbCase tbcase = caseHome.getInstance();
			if (tbcase.getHealthUnits().size() > 0) {
				tbcase.setNotificationUnit(tbcase.getHealthUnits().get(0).getTbunit());
				caseHome.persist();
			}
		}
		return ret;
	}
	
	/**
	 * Save the comorbidities for a new case
	 * @return
	 */
	public boolean saveComorbidities() {
		comorbidityHome.save();
		return true;
	}
	
	/**
	 * Return object to help filling the administrative unit fields 
	 * @return
	 */
	public AdminUnitSelection getAuselection() {
		if (auselection == null)
			auselection = new AdminUnitSelection();
		return auselection;
	}


	public void setDstRequired(Integer dstRequired) {
		this.dstRequired = dstRequired;
	}


	public Integer getDstRequired() {
		return dstRequired;
	}

	
	public void doNothing() {
		System.out.println("do nothing...");
	}
}
