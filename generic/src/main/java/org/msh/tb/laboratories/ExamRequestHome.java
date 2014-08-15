package org.msh.tb.laboratories;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.beanutils.BeanUtils;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.adminunits.AdminUnitSelection;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.PatientHome;
import org.msh.tb.entities.*;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.ExamStatus;
import org.msh.tb.entities.enums.ValidationState;
import org.msh.tb.login.UserSession;
import org.msh.tb.tbunits.TBUnitSelection;
import org.msh.utils.date.DateUtils;

/**
 * Base class for requesting of new exams for a patient
 * @author Ricardo Memoria
 *
 */
@Name("examRequestHome")
@Scope(ScopeType.CONVERSATION)
public class ExamRequestHome {

	@In(create=true) PatientHome patientHome;
	@In EntityManager entityManager;

	private List<SampleRequest> samples = new ArrayList<SampleRequest>();
	
	private TbCase tbcase;
	
	private SampleRequest sampleRequest;
	private SampleRequest edtSampleRequest;
	private boolean initialized;
	private AdminUnitSelection auselection;
	private TBUnitSelection unitselection;
	private boolean validated;
	private Date requestDate;
	private Laboratory laboratory;


	/**
	 * Initialize a new request of exams for a patient
	 */
	public String init() {
		if (initialized)
			return "initialized";
		
		Patient p = patientHome.getInstance();
		
		if ((!patientHome.isManaged()) && 
			(p.getName() == null) && (p.getMiddleName() == null) && (p.getLastName() == null) &&
			(p.getBirthDate() == null))
		return "patient-searching";

		List<TbCase> lst = entityManager.createQuery("from TbCase c where patient.id = :id and c.registrationDate = " +
				"(select max(aux.registrationDate) from TbCase aux where aux.patient.id = :id)")
				.setParameter("id", p.getId())
				.getResultList();

		tbcase = null;
		if (lst.size() > 0) {
			TbCase aux = lst.get(0);
			if (aux.getState().ordinal() <= CaseState.TRANSFERRING.ordinal()) {
				tbcase = aux;
				getAuselection().setSelectedUnit(tbcase.getNotifAddress().getAdminUnit());
			}
		}
		
		if (tbcase == null) {
			tbcase = new TbCase();
			tbcase.setPatient(p);
		}
		Contexts.getConversationContext().set("tbcase", tbcase);
		
		requestDate = DateUtils.getDate();
		
		initialized = true;
		
		return "initialized";
	}


	/**
	 * Initialize patient data
	 * @return
	 */
	public String selectPatientData() {
		initialized = false;
		if (init().equals("initialized"))
			return "/labs/newrequest.xhtml";
		return "error";
	}

	
	/**
	 * Generate exams request
	 * @return
	 */
	public String persist() {
		if (tbcase == null)
			return "error";

		if (samples.size() == 0) {
			FacesMessages.instance().add("At least one sample must be entered");
			return "error";
		}
		
		laboratory = LaboratorySession.instance().getLaboratory();

		// is this a suspect not registered yet ?
		if (tbcase.getId() == null) {
			tbcase.setNotificationUnit(unitselection.getTbunit());
			tbcase.getNotifAddress().setAdminUnit(auselection.getSelectedUnit());
			tbcase.getCurrentAddress().copy(tbcase.getNotifAddress());
			tbcase.setRegistrationDate(requestDate);
			tbcase.setState(CaseState.WAITING_TREATMENT);
			tbcase.setValidationState(ValidationState.WAITING_VALIDATION);
//			tbcase.setDiagnosisType(DiagnosisType.SUSPECT);
		}
		
		Patient p = tbcase.getPatient();
		if (p.getId() == null) {
			Workspace workspace = (Workspace)Component.getInstance("defaultWorkspace");
			p.setWorkspace(workspace);
		}
		entityManager.persist(p);

		entityManager.persist(tbcase);
		
		for (SampleRequest request: samples) {
			PatientSample sample = request.getSample();
			
			sample.setTbcase(tbcase);
			entityManager.persist(sample);
			
			// add microscopy exam requested
			if (request.isAddExamMicroscopy()) {
				ExamMicroscopy mic = new ExamMicroscopy();
				prepareExam(mic, sample);
				tbcase.getExamsMicroscopy().add(mic);
				sample.getMicroscopyExams().add(mic);
				entityManager.persist(mic);
			}

			// add culture exam requested
			if (request.isAddExamCulture()) {
				ExamCulture cult = new ExamCulture();
				prepareExam(cult, sample);
				tbcase.getExamsCulture().add(cult);
				sample.getCultureExams().add(cult);
				entityManager.persist(cult);
			}

			// add dst exam requested
			if (request.isAddExamDST()) {
				ExamDST dst = new ExamDST();
				prepareExam(dst, sample);
				tbcase.getExamsDST().add(dst);
				sample.getDstExams().add(dst);
				entityManager.persist(dst);
			}
			
			// add molecular biology exam requested
			if (request.isAddExamIdentification()) {
				ExamXpert exam = new ExamXpert();
				prepareExam(exam, sample);
				tbcase.getExamsXpert().add(exam);
				sample.getXpertExams().add(exam);
				entityManager.persist(exam);
			}
		}
		
		CaseHome caseHome = (CaseHome)Component.getInstance("caseHome", true);
		caseHome.setId(tbcase.getId());
		
		return "persisted";
	}


	/**
	 * Initialize variables of an exam 
	 * @param exam
	 */
	protected void prepareExam(LaboratoryExam exam, PatientSample sample) {
		exam.setTbcase(tbcase);
		exam.setLaboratory(laboratory);
//		exam.setRequestDate(requestDate);
		exam.setPatientSample(sample);
		exam.setStatus(ExamStatus.ONGOING);
	}
	
	/**
	 * Initialize the inclusion of a new sample
	 * @return
	 */
	public String initAddSample() {
		PatientSample sample = new PatientSample();
		sampleRequest = new SampleRequest(sample);
		edtSampleRequest = null;
		validated = false;
		
		return "sample_posted";
	}
	
	
	/**
	 * Initialize the editing of a sample request
	 * @param sampleReq
	 * @return
	 */
	public String initEditSample(SampleRequest sampleReq) {
		try {
			sampleRequest = (SampleRequest)BeanUtils.cloneBean(sampleReq);
			edtSampleRequest = sampleReq;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		validated = false;
		return "init_sample_editing";
	}

	
	/**
	 * Post sample editing 
	 * @return
	 */
	public String postSampleRequest() {
		if ((!sampleRequest.isAddExamCulture()) && (!sampleRequest.isAddExamMicroscopy()) && (!sampleRequest.isAddExamDST())) {
			FacesMessages.instance().addFromResourceBundle("labs.newreq.noexam");
			return "error";
		}
		
		// is new ?
		if (edtSampleRequest == null) {
			samples.add(sampleRequest);
			for (int i = 1; i <= samples.size(); i++)
				samples.get(i - 1).setIndex(i);
		}
		else {
			// copy the edited object to the original one
			try {
				BeanUtils.copyProperties(edtSampleRequest, sampleRequest);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}

		validated = true;
		return "sample_posted";
	}
	
	/**
	 * Delete a sample in the list of samples to be requested
	 * @param request
	 * @return
	 */
	public String deleteSampleRequest(SampleRequest request) {
		samples.remove(request);
		return "sample_deleted";
	}
	
	/**
	 * Register the samples and exams to be performed by the laboratory
	 * @return
	 */
	public String generateRequest() {
		return "persisted";
	}


	/**
	 * @return the samples
	 */
	public List<SampleRequest> getSamples() {
		return samples;
	}

	/**
	 * @return the tbcase
	 */
	public TbCase getTbcase() {
		return tbcase;
	}


	public AdminUnitSelection getAuselection() {
		if (auselection == null)
			auselection = new AdminUnitSelection();
		return auselection;
	}
	
	
	public TBUnitSelection getUnitselection() {
		if (unitselection == null)
			unitselection = new TBUnitSelection("unitid");
		return unitselection;
	}


	/**
	 * @return the sampleRequest
	 */
	public SampleRequest getSampleRequest() {
		return sampleRequest;
	}


	/**
	 * @return the validated
	 */
	public boolean isValidated() {
		return validated;
	}


	/**
	 * @param validated the validated to set
	 */
	public void setValidated(boolean validated) {
		this.validated = validated;
	}


	/**
	 * @return the requestDate
	 */
	public Date getRequestDate() {
		return requestDate;
	}


	/**
	 * @param requestDate the requestDate to set
	 */
	public void setRequestDate(Date requestDate) {
		this.requestDate = requestDate;
	}
}
