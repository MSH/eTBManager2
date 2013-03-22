package org.msh.tb.webservices;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

import org.jboss.seam.Component;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.exams.ExamXpertHome;
import org.msh.tb.entities.ExamXpert;
import org.msh.tb.entities.Laboratory;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.XpertResult;
import org.msh.tb.entities.enums.XpertRifResult;
import org.msh.tb.laboratories.LaboratoryHome;

/**
 * 
 * @author Ricardo Memoria
 *
 */
@WebService(name="xpertService", serviceName="xpertService")
@SOAPBinding(style=Style.RPC)
public class XpertService {

	/**
	 * Web service method exposed to the client
	 * @param sessionId is the user session id created in the authentication method
	 * @param examResult is the data containing the result
	 * @return instance of the {@link Response} object containing the result of the operation
	 */
	@WebMethod
	public Response postResult(@WebParam String sessionId, @WebParam XpertData examResult) {
		return (new RemoteActionHandler(sessionId, examResult) {
			@Override
			protected Object execute(Object result) {
				return persistResult((XpertData)result);
			}
		}).run();
	}


	/**
	 * Save the result sent by the web service. The data is initially validated, and in case
	 * there is no validation error, the xpert exam is saved
	 * 
	 * @param result
	 * @return
	 */
	protected Object persistResult(XpertData result) {
		// validate the data
		if (result.getCaseId() == null)
			return new Response(Response.RESP_VALIDATION_ERROR, "Case ID is required");
		
		if (result.getSampleId() == null)
			return new Response(Response.RESP_VALIDATION_ERROR, "Sample ID is required");
		
		if (result.getResult() == null)
			return new Response(Response.RESP_VALIDATION_ERROR, "Result is required");
		
		XpertResult res = XpertResult.values()[result.getResult()];
		XpertRifResult rifRes = XpertRifResult.values()[result.getRifResult()];
		
		if ((res == XpertResult.TB_DETECTED) && (rifRes == null))
			return new Response(Response.RESP_VALIDATION_ERROR, "RIF Result is required");

		// check if case is ok
		CaseHome caseHome = (CaseHome)Component.getInstance("caseHome", true);
		caseHome.setId(result.getCaseId());
		TbCase tbcase = caseHome.getInstance();
		
		if (tbcase == null) {
			return new Response(Response.RESP_VALIDATION_ERROR, "No case found with id = " + result.getCaseId());
		}

		// check if laboratory was given
		Laboratory lab = null;
		if (result.getLaboratoryId() != null) {
			LaboratoryHome labHome = (LaboratoryHome)Component.getInstance("laboratoryHome", true);
			labHome.setId(result.getLaboratoryId());
			lab = labHome.getInstance();
			if (lab == null)
				return new Response(Response.RESP_VALIDATION_ERROR, "Laboratory with id " + result.getLaboratoryId() + " was not found");
		}

		// check for sample id
		ExamXpertHome examHome = (ExamXpertHome)Component.getInstance("examGenexpertHome", true);
		if (!examHome.findExamBySampleId(tbcase, result.getSampleId())) {
			return new Response(Response.RESP_VALIDATION_ERROR, "No sample with id " + result.getSampleId() + " was found to case " + tbcase.getId());
		}

		// get the exam and fill the results
		ExamXpert exam = examHome.getInstance();
		
		exam.setComments(result.getComments());
		if (result.getSampleDateCollected() != null)
			exam.setDateCollected(result.getSampleDateCollected());
		
		exam.setDateRelease(result.getReleaseDate());
		exam.setResult(res);
		exam.setRifResult(rifRes);
		exam.setSampleNumber(result.getSampleId());
		if (lab != null)
			exam.setLaboratory(lab);
		
		examHome.persist();
		
		return new Response(exam.getId().toString());
	}

}
