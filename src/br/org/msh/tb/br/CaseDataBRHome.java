package org.msh.tb.br;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.beanutils.PropertyUtils;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.Messages;
import org.msh.tb.adminunits.AdminUnitSelection;
import org.msh.tb.br.entities.MolecularBiology;
import org.msh.tb.br.entities.TbCaseBR;
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
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.ExamCulture;
import org.msh.tb.entities.ExamDSTResult;
import org.msh.tb.entities.ExamHIV;
import org.msh.tb.entities.ExamMicroscopy;
import org.msh.tb.entities.ExamXRay;
import org.msh.tb.entities.FieldValue;
import org.msh.tb.entities.MedicalExamination;
import org.msh.tb.entities.Patient;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.CultureResult;
import org.msh.tb.entities.enums.DiagnosisType;
import org.msh.tb.entities.enums.DstResult;
import org.msh.tb.entities.enums.Gender;
import org.msh.tb.entities.enums.HIVResult;
import org.msh.tb.entities.enums.MicroscopyResult;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.entities.enums.YesNoType;
import org.msh.tb.misc.FieldsOptions;
import org.msh.tb.tbunits.TBUnitSelection;

@Name("caseDataBRHome")
@Scope(ScopeType.CONVERSATION)
public class CaseDataBRHome {

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

	
	
/*	@Factory("caseDataBR")
	public TbCaseBR getCaseDataBR() {
		try {
			if (caseHome.getId() != null)
				setId(caseHome.getId());
		} catch (Exception e) {
			setId(null);
		}
		return getInstance();
	}
*/
	
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
		TbCaseBR tbcase = (TbCaseBR)caseHome.getInstance();
		// all cases are confirmed cases
		tbcase.setDiagnosisType(DiagnosisType.CONFIRMED);
		if(tbcase.getDiagnosisDate() == null)
			tbcase.setDiagnosisDate(startTreatmentHome.getIniTreatmentDate());
		
		if (!validateForm())
			return "error";

//		if (caseEditingHome!=null)
//			return "error";

		tbcase.setState(CaseState.WAITING_TREATMENT);

		adjustFields();
		
//		data.setTbcase(tbcase);
//		data.setId(tbcase.getId());
		tbcase.setCurrAddressNumber(tbcase.getNotifAddressNumber());
		tbcase.setAdminUnitUsOrigem(getAuselection().getSelectedUnit());

		// all cases are confirmed
		tbcase.setDiagnosisType(DiagnosisType.CONFIRMED);

		caseHome.setTransactionLogActive(false);
		String ret = caseEditingHome.saveNew();

		if ("error".equals(ret))
			return ret;
		
		
		// save exam results
		if (!saveExams()) 
			return "error";
		
		saveComorbidities();
		
		saveXRay();
		
		saveMedicalExamination();
		
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
		
		adjustFields();

		TbCaseBR tbcase = (TbCaseBR)caseHome.getInstance();
		if (!tbcase.isNotifAddressChanged())
			tbcase.setCurrAddressNumber(tbcase.getNotifAddressNumber());

		if(tbcase.getDiagnosisDate() == null)
			tbcase.setDiagnosisDate(startTreatmentHome.getIniTreatmentDate());
		
		tbcase.setAdminUnitUsOrigem(getAuselection().getSelectedUnit());

		return caseEditingHome.saveEditing();
	}

	
	/**
	 * change the text fields to upper case
	 */
	protected void adjustFields() {
		TbCaseBR tbcase = (TbCaseBR)caseHome.getInstance();
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
		
		setPropertyUpperCase(tbcase, "numSinan");
		setPropertyUpperCase(tbcase, "usOrigem");
		setPropertyUpperCase(tbcase, "country");
		setPropertyUpperCase(tbcase, "currAddressNumber");
		setPropertyUpperCase(tbcase, "notifAddressNumber");
		setPropertyUpperCase(tbcase, "prefixMobile");
		setPropertyUpperCase(tbcase, "prefixPhone");
		setPropertyUpperCase(tbcase, "notifDistrict");
		setPropertyUpperCase(tbcase, "currDistrict");
		
		PatientType tp = tbcase.getPatientType();
		if (tp != PatientType.SCHEMA_CHANGED)
			tbcase.setSchemaChangeType(null);
		if (!CaseClassification.DRTB.equals( tbcase.getClassification() ))
			tbcase.setDrugResistanceType(null);

		if (tp != PatientType.OTHER)
			tbcase.setPatientTypeOther(null);
		
		if (p.getGender() == Gender.MALE)
			tbcase.setPregnancePeriod(null);
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
		
		MedicalExamination medInst = medicalExaminationBRHome.getInstance();
		//Start validation
		for(MedicalExamination m : caseHome.getTbCase().getExaminations()){
			
			if(medInst.getId() != m.getId() && medInst.getDate().equals(m.getDate())){
				facesMessages.addToControlFromResourceBundle("date", "form.duplicatedname");
				return "error";
			}
			
			if( caseHome.getTbCase()!=null && (caseHome.getTbCase().getAge()>=18) && (medInst.getHeight()!= null && medInst.getHeight() != 0)){
				m.setHeight(medInst.getHeight());
			}
		
		}

		adjustMedicalExaminationFields();
		caseHome.getTbCase().getCurrentAddress().setAdminUnit(auselection.getSelectedUnit());
		medicalExaminationBRHome.setDisplayMessage(false);
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
			
			if(medExam.getDate().before(caseHome.getTbCase().getDiagnosisDate())){
				facesMessages.addToControlFromResourceBundle("date", "cases.medexamdate.val1");
				return false;
			}
			//Finish Validation
		}
		
		// check exams
		if ((examCultureHome != null) && (examMicroscopyHome != null)) {
			if (examMicroscopyHome.getInstance().getResult() != MicroscopyResult.NOTDONE) {
				if (examMicroscopyHome.getInstance().getDateCollected() == null) {
					facesMessages.addToControlFromResourceBundle("edtdatemicroscopy", "javax.faces.component.UIInput.REQUIRED");
					res = false;
				}
			}
			
			if (!examCultureHome.getInstance().getResult().equals(CultureResult.NOTDONE)) 
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
		
		// check diagnosis date and notification date
		if (tbcase.getRegistrationDate()!=null
				&& tbcase.getDiagnosisDate()!=null
				&& tbcase.getDiagnosisDate().after(tbcase.getRegistrationDate())) {
			facesMessages.addToControlFromResourceBundle("diagdateedt", "cases.details.valerror1");
			res = false;
		}
		
		// check diagnosis date and the treatment initiation date
		if (tbcase.getRegistrationDate()!=null
				&& tbcase.getDiagnosisDate()!=null &&
				tbcase.getTreatmentPeriod().getIniDate() != null && tbcase.getDiagnosisDate().after(tbcase.getTreatmentPeriod().getIniDate())) {
			facesMessages.addToControlFromResourceBundle("diagdateedt", "cases.treat.inidatemsg");
			res = false;
		}
		
//		if ((!caseHome.isManaged()) && (treatmentHealthUnitHome != null) && (treatmentHealthUnitHome.getRegimen() == null))
//			return false;
		return res;
	}


	/**
	 * Save exams results for DST, Culture and microscopy
	 */
	protected boolean saveExams() {

		// turn off message displaying
		if (examMicroscopyHome != null) 
			examMicroscopyHome.setDisplayMessage(false);
		if (examCultureHome != null) 
			examCultureHome.setDisplayMessage(false);
		if (molecularBiologyHome != null) 
			molecularBiologyHome.setDisplayMessage(false);
		if (examDSTHome != null) 
			examDSTHome.setDisplayMessage(false);
		if (examHIVHome != null) 
			examHIVHome.setDisplayMessage(false);

		// check HIV
		ExamHIV exam = examHIVHome.getInstance();
		if (exam.getResult() != HIVResult.NOTDONE) {
			if (exam.getResult() == HIVResult.ONGOING)
				exam.setDate(null);
			examHIVHome.setDisplayMessage(false);
			examHIVHome.persist();
		}

		if ((examCultureHome == null) || (examMicroscopyHome == null))
			return true;

		ExamCulture examCulture = examCultureHome.getInstance();
		ExamMicroscopy examMicroscopy = examMicroscopyHome.getInstance();
		MolecularBiology molecularBiology = molecularBiologyHome.getInstance();

		Date dtCollected = examCultureHome.getInstance().getDateCollected();
		
//		examMicroscopy.setMethod(examCulture.getMethod());

		// save microscopy exam
		if (!MicroscopyResult.NOTDONE.equals(examMicroscopy.getResult())) {
			if (!examMicroscopyHome.validateDates()) {
				facesMessages.addToControlFromResourceBundle("dtrelease", "cases.exams.datereleasebeforecol");
				return false;			
			}
//			examMicroscopyHome.getInstance().setDateCollected(dtCollected);
//			examMicroscopy.setDateRelease(examCulture.getDateRelease());
//			examMicroscopy.setLaboratory(examCulture.getLaboratory());
			examMicroscopyHome.setDisplayMessage(false);
			examMicroscopyHome.persist();
		}
		
		// save culture exam
		if (!CultureResult.NOTDONE.equals(examCulture.getResult())) {
			if (!examCultureHome.validateDates()) {
				facesMessages.addToControlFromResourceBundle("dtrelease", "cases.exams.datereleasebeforecol");
				return false;
			}			
			examCultureHome.setDisplayMessage(false);
			examCultureHome.persist();
		}

		// save molecular biology
		if (!MolecularBiologyResult.NOTDONE.equals(molecularBiology.getResult())) {
			molecularBiologyHome.setDisplayMessage(false);
			molecularBiologyHome.persist();
		}

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
			examDSTHome.setDisplayMessage(false);
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
		examXRayHome.setDisplayMessage(false);
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
				caseHome.setDisplayMessage(false);
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
		if (auselection == null) {
			auselection = new AdminUnitSelection();
			if (caseHome.isManaged())
				auselection.setSelectedUnit(getTbCase().getAdminUnitUsOrigem());
		}
		return auselection;
	}

	public String changeCaseClassification(CaseClassification it) {
		
		TbCaseBR tbcase = (TbCaseBR)caseHome.getInstance();
		
		if(tbcase.getClassification().equals(it))
			return "classificationModified";

		tbcase.setClassification(it);
		tbcase.setDrugResistanceType(null);
		tbcase.setTipoResistencia(null);

		caseHome.persist();
		
		return "classificationModified";
	}
	
	public List<PatientType> getPatientTypesList(CaseClassification cla){
		List<PatientType> list = new ArrayList<PatientType>();
		
		list.add(PatientType.AFTER_DEFAULT);
		list.add(PatientType.RELAPSE);
		list.add(PatientType.FAILURE_FT);
		list.add(PatientType.FAILURE_RT);
		list.add(PatientType.SCHEMA_CHANGED);
		list.add(PatientType.OTHER);
		
		if(cla.equals(CaseClassification.DRTB)){
			list.remove(PatientType.SCHEMA_CHANGED);
			list.add(0,PatientType.NEW);
			list.add(list.size()-1, PatientType.RESISTANCE_PATTERN_CHANGED);
		}
		
		if(cla.equals(CaseClassification.NMT)){
			list.add(0,PatientType.NEW);
		}
		
		return list;
	}
	
	public String getClassificationLabel(CaseClassification cla, PatientType type){
		if(type.equals(PatientType.OTHER)){
			return Messages.instance().get(type.getKey());
		}
		
		if(cla.equals(CaseClassification.TB)){
			if(type.equals(PatientType.AFTER_DEFAULT))
				return Messages.instance().get(type.getKey()) + " de esquema especial";
			if(type.equals(PatientType.RELAPSE))
				return Messages.instance().get(type.getKey()) + " após esquema especial";
			return Messages.instance().get(type.getKey());
		}
		
		return Messages.instance().get(type.getKey()) + " de " + Messages.instance().get(cla.getKey2());
	}
	
	public void setDstRequired(Integer dstRequired) {
		this.dstRequired = dstRequired;
	}


	public Integer getDstRequired() {
		return dstRequired;
	}
	
	public TbCaseBR getTbCase() {
		return (TbCaseBR)caseHome.getInstance();
	}

}
