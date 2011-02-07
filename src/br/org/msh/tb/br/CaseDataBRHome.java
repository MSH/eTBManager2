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
import org.msh.mdrtb.entities.AdministrativeUnit;
import org.msh.mdrtb.entities.ExamCulture;
import org.msh.mdrtb.entities.ExamDSTResult;
import org.msh.mdrtb.entities.ExamHIV;
import org.msh.mdrtb.entities.ExamMicroscopy;
import org.msh.mdrtb.entities.ExamXRay;
import org.msh.mdrtb.entities.FieldValue;
import org.msh.mdrtb.entities.MedicalExamination;
import org.msh.mdrtb.entities.Patient;
import org.msh.mdrtb.entities.TbCase;
import org.msh.mdrtb.entities.UserWorkspace;
import org.msh.mdrtb.entities.enums.CaseClassification;
import org.msh.mdrtb.entities.enums.CaseState;
import org.msh.mdrtb.entities.enums.CultureResult;
import org.msh.mdrtb.entities.enums.DstResult;
import org.msh.mdrtb.entities.enums.Gender;
import org.msh.mdrtb.entities.enums.HIVResult;
import org.msh.mdrtb.entities.enums.MicroscopyResult;
import org.msh.mdrtb.entities.enums.PatientType;
import org.msh.mdrtb.entities.enums.YesNoType;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.adminunits.AdminUnitSelection;
import org.msh.tb.br.entities.CaseDataBR;
import org.msh.tb.br.entities.MolecularBiology;
import org.msh.tb.br.entities.enums.MolecularBiologyResult;
import org.msh.tb.cases.CaseEditingHome;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.ComorbidityHome;
import org.msh.tb.cases.exams.ExamCultureHome;
import org.msh.tb.cases.exams.ExamDSTHome;
import org.msh.tb.cases.exams.ExamHIVHome;
import org.msh.tb.cases.exams.ExamMicroscopyHome;
import org.msh.tb.cases.exams.ExamXRayHome;
import org.msh.tb.cases.treatment.StartTreatmentHome;
import org.msh.tb.cases.treatment.StartTreatmentIndivHome;
import org.msh.tb.misc.FieldsOptions;
import org.msh.tb.tbunits.TBUnitSelection;

@Name("caseDataBRHome")
public class CaseDataBRHome extends EntityHomeEx<CaseDataBR> {
	private static final long serialVersionUID = 8895495231680746480L;

	@In EntityManager entityManager;
	@In(create=true) CaseHome caseHome;
	@In(create=true) CaseEditingHome caseEditingHome;
	@In(required=false) ExamMicroscopyHome examMicroscopyHome;
	@In(required=false) ExamCultureHome examCultureHome;
	@In(create=true) StartTreatmentHome startTreatmentHome;
	@In(create=true) StartTreatmentIndivHome startTreatmentIndivHome;
//	@In(required=false) TreatmentHealthUnitHome treatmentHealthUnitHome;
	@In(required=false) ExamDSTHome examDSTHome;
	@In(required=false) ExamHIVHome examHIVHome;
	@In(required=false) ComorbidityHome comorbidityHome;
	@In(required=false) MedicalExaminationBRHome medicalExaminationBRHome;
	@In(required=false) MolecularBiologyHome molecularBiologyHome;
	@In(create=true) FacesMessages facesMessages;
	@In(create=true) ExamXRayHome examXRayHome;
	@In(create=true) FieldsOptions fieldOptions;
	
	private AdminUnitSelection auselection;
	private Integer dstRequired;

	@Factory("caseDataBR")
	public CaseDataBR getCaseDataBR() {
		try {
			if (caseHome.getId() != null) {
				entityManager.createQuery("from CaseDataBR c where c.tbcase.id = #{caseHome.id}").getSingleResult();
				getAuselection().setSelectedUnit(getInstance().getAdminUnitUsOrigem());
				caseHome.getLogService().addEntityMonitoring(getInstance());
			}
			setId(caseHome.getId());
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
			if (examDSTHome == null)
				examDSTHome = (ExamDSTHome)Component.getInstance("examDSTHome", true);
			if (molecularBiologyHome == null)
				molecularBiologyHome = (MolecularBiologyHome)Component.getInstance("molecularBiologyHome", true);
			
			examCultureHome.getLabselection().setAdminUnit(au);
			examDSTHome.getLabselection().setAdminUnit(au);
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
		if (!CaseClassification.DRTB.equals( tbcase.getClassification() ))
			data.setResistanceType(null);

		if (tp != PatientType.OTHER)
			tbcase.setPatientTypeOther(null);
		
		if (p.getGender() == Gender.MALE)
			data.setPregnancePeriod(null);
	}

	
	/**
	 * Adjust values of the medical examination fields
	 */
	protected void adjustMedicalExaminationFields() {
		if (medicalExaminationBRHome == null)
			return;

		MedicalExamination medExam = medicalExaminationBRHome.getInstance();
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
		if (medicalExaminationBRHome == null)
			return "error";

		adjustMedicalExaminationFields();
		return medicalExaminationBRHome.persist();
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
		
		if (medicalExaminationBRHome != null) {
			MedicalExamination medExam = medicalExaminationBRHome.getInstance();
			if (YesNoType.YES.equals(medExam.getSupervisedTreatment())) {
				if ((medExam.getSupervisionUnitName() == null) || (medExam.getSupervisionUnitName().isEmpty())) {
					facesMessages.addToControlFromResourceBundle("supUnitName", "javax.faces.component.UIInput.REQUIRED");
					res = false;
				}
			}
		}
		
		// check exams
		if ((examCultureHome != null) && (examMicroscopyHome != null)) {
			if ((!examCultureHome.getInstance().getResult().equals(CultureResult.NOTDONE)) || 
				(!examMicroscopyHome.getInstance().getResult().equals(MicroscopyResult.NOTDONE))) 
			{
				if (examCultureHome.getInstance().getDateCollected() == null) {
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
		
		
		// check if HIV exam date was informed
		if (examHIVHome != null) {
			ExamHIV examHIV = examHIVHome.getInstance();
			if ((examHIV.getDate() == null) && (examHIV.getResult() != HIVResult.NOTDONE) && (examHIV.getResult() != HIVResult.ONGOING)) {
				facesMessages.addToControlFromResourceBundle("edthivdate", "javax.faces.component.UIInput.REQUIRED");
				res = false;
			}
		}
		
//		if ((!caseHome.isManaged()) && (treatmentHealthUnitHome != null) && (treatmentHealthUnitHome.getRegimen() == null))
//			return false;
		return res;
	}


	/**
	 * Save exams results for DST, Culture and microscopy
	 */
	protected boolean saveExams() {
		// check HIV
		ExamHIV exam = examHIVHome.getInstance();
		if (exam.getResult() != HIVResult.NOTDONE) {
			if (exam.getResult() == HIVResult.ONGOING)
				exam.setDate(null);
			examHIVHome.persist();
		}

		if ((examCultureHome == null) || (examMicroscopyHome == null))
			return true;

		ExamCulture examCulture = examCultureHome.getInstance();
		ExamMicroscopy examMicroscopy = examMicroscopyHome.getInstance();
		MolecularBiology molecularBiology = molecularBiologyHome.getInstance();

		// turn off message displaying
		examMicroscopyHome.setDisplayMessage(false);
		examCultureHome.setDisplayMessage(false);
		molecularBiologyHome.setDisplayMessage(false);
		examDSTHome.setDisplayMessage(false);
		examHIVHome.setDisplayMessage(false);

		Date dtCollected = examCultureHome.getInstance().getDateCollected();
		
//		examMicroscopy.setMethod(examCulture.getMethod());

		// save microscopy exam
		if (!MicroscopyResult.NOTDONE.equals(examMicroscopy.getResult())) {
			if (!examMicroscopyHome.validateDates()) {
				facesMessages.addToControlFromResourceBundle("dtrelease", "cases.exams.datereleasebeforecol");
				return false;			
			}
			examMicroscopyHome.getInstance().setDateCollected(dtCollected);
			examMicroscopy.setDateRelease(examCulture.getDateRelease());
			examMicroscopy.setLaboratory(examCulture.getLaboratory());
			examMicroscopyHome.persist();
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
		for (ExamDSTResult res: examDSTHome.getItems()) {
			if ((res.getResult() != null) && (res.getResult().equals(DstResult.NOTDONE))) {
				hasResult = true;
				break;
			}
		}
		
		if (hasResult) {
			examDSTHome.getInstance().setDateCollected(dtCollected);
			examDSTHome.persist();
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
		TBUnitSelection tbsel = startTreatmentIndivHome.getTbunitselection();
		if (tbsel.getTbunit() == null) {
			tbsel.setTbunit(caseHome.getInstance().getNotificationUnit());
		}
	}

	
	/**
	 * Save treatment editing for individualized regimen
	 * @return
	 */
	public String saveTreatmentEditing() {
		TbCase tbcase = caseHome.getInstance();
		startTreatmentIndivHome.setIniTreatmentDate(tbcase.getRegistrationDate());

		String ret = startTreatmentIndivHome.startIndividualizedRegimen(); 
		if (ret.equals("persisted")) {
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
}
