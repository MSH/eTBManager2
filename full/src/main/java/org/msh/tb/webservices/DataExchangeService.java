package org.msh.tb.webservices;

import org.jboss.seam.Component;
import org.msh.tb.cases.exams.ExamDSTHome;
import org.msh.tb.entities.ExamDST;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

@WebService(name="dataExchangeService", serviceName="dataExchangeService")
@SOAPBinding(style=Style.RPC)
public class DataExchangeService {

	@WebMethod
	public Response importData(@WebParam String sessionId, @WebParam String xmldata) {
		return (new RemoteActionHandler(sessionId, xmldata) {
			@Override
			protected Object execute(Object xmlData) {
				return importSessionData(this, (String)xmlData);
			}
		}).run();
	}
	
	
	/**
	 * Import data represented in the XML format
	 * @param handler
	 * @param xmldata
	 */
	protected Object importSessionData(RemoteActionHandler handler, String xmldata) {
		ExamDST exam = (ExamDST)handler.deserializeFromXml(xmldata, ExamDST.class);
		if (exam == null) {
			handler.setResponseError(Response.RESP_VALIDATION_ERROR, "No data found in xml");
			return null;
		}

		ExamDSTHome home = (ExamDSTHome)Component.getInstance("examDSTHome", true);
		home.setInstance(exam);

		// try to save and check result
		if (!("persisted".equals(home.persist()))) {
			if (handler.checkValidationErrors())
				return null;
		}
		return home.getId();
	}

}
