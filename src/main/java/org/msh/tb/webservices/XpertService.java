package org.msh.tb.webservices;

import org.jboss.seam.Component;
import org.jboss.seam.framework.EntityNotFoundException;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.exams.ExamXpertHome;
import org.msh.tb.entities.ExamXpert;
import org.msh.tb.entities.Laboratory;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.XpertResult;
import org.msh.tb.entities.enums.XpertRifResult;
import org.msh.tb.laboratories.LaboratoryHome;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.xml.bind.ValidationException;

/**
 * Web-service to post new or update xpert exam results.
 * @author Ricardo Memoria
 *
 */
@WebService(name="xpertService", serviceName="xpertService", targetNamespace="http://etbmanager.org/xpertservice")
@SOAPBinding(style=Style.RPC)
public class XpertService {

	/**
	 * Web service method exposed to the client. Update or post a new exam result. If it's a new exam result,
	 * the laboratory id and the date when sample was collected are required. If it's not a 
	 * 
	 * @param sessionId is the user session id created in the authentication method
	 * @param examResult is the data containing the result
	 * @return instance of the {@link Response} object containing the result of the operation
	 */
	@WebMethod
	public Response postResult(@WebParam(name="sessionId") String sessionId, @WebParam(name="examResult") XpertData examResult) {
		return (new RemoteActionHandler(sessionId, examResult) {
			@Override
			protected Object execute(Object result) throws ValidationException {
				return persistResult(this, (XpertData)result);
			}
		}).run();
	}


	/**
	 * Save the result sent by the web service. The data is initially validated, and in case
	 * there is no validation error, the xpert exam is saved
	 * 
	 * @param result
	 * @return
	 * @throws ValidationException 
	 */
	protected Object persistResult(RemoteActionHandler handler, XpertData result) throws ValidationException {
		// validate the data
		if (result.getCaseId() == null)
			throw new ValidationException("Case ID is required");
		
		if (result.getSampleId() == null)
			throw new ValidationException("Sample ID is required");
		
		if (result.getResult() == null)
			throw new ValidationException("Result is required");

		// check results
		XpertResult res =  result.getResult(); 
		XpertRifResult rifRes = result.getRifResult();
		
		if ((res == XpertResult.TB_DETECTED) && (rifRes == null))
			throw new ValidationException("RIF Result is required");

		// check if it's a valid case/suspect
		CaseHome caseHome = (CaseHome)Component.getInstance("caseHome", true);
		try {
			caseHome.setId(result.getCaseId());
		} catch (EntityNotFoundException e) {
			throw new ValidationException("No case found with id = " + result.getCaseId());
		}
		TbCase tbcase = caseHome.getInstance();
		
		// check if laboratory was given
		Laboratory lab = null;
		if (result.getLaboratoryId() != null) {
			LaboratoryHome labHome = (LaboratoryHome)Component.getInstance("laboratoryHome", true);
			try {
				labHome.setId(result.getLaboratoryId());
			} catch (EntityNotFoundException e) {
				throw new ValidationException("Laboratory with id " + result.getLaboratoryId() + " was not found");
			}
			lab = labHome.getInstance();
		}

		// check for sample id
		ExamXpertHome examHome = (ExamXpertHome)Component.getInstance("examXpertHome", true);
		Integer examId = examHome.findExamBySampleId(tbcase, result.getSampleId());

		// is a new exam
		boolean bnew = examId == null;

		// if it's a new exam and the laboratory or date collected is not informed, so it's wrong
		if ((bnew) && ((lab == null) || (result.getSampleDateCollected() == null)))
			throw new ValidationException("No sample with id " + result.getSampleId() + " was found for case " + tbcase.getId());

		// loads the exam and save the changes in the transaction log system
		examHome.setIdWithLog(examId);

		// get the exam and fill the results
		ExamXpert exam = examHome.getInstance();
		
		exam.setComments(result.getComments());
		if (result.getSampleDateCollected() != null)
			exam.setDateCollected(result.getSampleDateCollected());
		if (lab != null)
			exam.setLaboratory(lab);
		
		exam.setDateRelease(result.getReleaseDate());
		exam.setResult(res);
		exam.setRifResult(rifRes);
		exam.setSampleNumber(result.getSampleId());

		// tries to save the exam. In case of error, return a value different from persisted
		if (!("persisted".equals(examHome.persist()))) {
			handler.checkValidationErrors();
			return null;
		}
		
		return exam.getId();
	}

}
