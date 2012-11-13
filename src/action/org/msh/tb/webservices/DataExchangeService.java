package org.msh.tb.webservices;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

import org.jboss.seam.Component;
import org.msh.tb.cases.exams.ExamDSTHome;
import org.msh.tb.entities.ExamDST;

@WebService(name="dataExchangeService", serviceName="dataExchangeService")
@SOAPBinding(style=Style.RPC)
public class DataExchangeService {

	@WebMethod
	public String importData(@WebParam String sessionId, @WebParam String xmldata) {
		final String xml = xmldata;
		return (new RemoteActionHandler(sessionId) {
			@Override
			protected Object execute() {
				return importSessionData(this, xml);
			}
		}).run();
	}
	
	
	/**
	 * Import data represented in the XML format
	 * @param resp
	 * @param xmldata
	 */
	protected Object importSessionData(RemoteActionHandler handler, String xmldata) {
		ExamDST exam = handler.deserializeFromXml(xmldata, ExamDST.class);
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
		return null;
	}

}
