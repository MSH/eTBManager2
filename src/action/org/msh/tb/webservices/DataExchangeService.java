package org.msh.tb.webservices;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.security.Identity;
import org.msh.datastream.XmlDeserializer.ObjectReferenceable;
import org.msh.tb.application.AppFacesMessages;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.exams.ExamDSTHome;
import org.msh.tb.entities.ExamDST;
import org.msh.tb.entities.TbCase;
import org.msh.tb.login.AuthenticatorBean;

@WebService(name="dataExchangeService", serviceName="dataExchangeService")
@SOAPBinding(style=Style.RPC)
public class DataExchangeService implements ObjectReferenceable {

	@WebMethod
	public String importData(@WebParam String sessionId, @WebParam String xmldata) {
		Response resp = new Response();
		try {

			AuthenticatorBean authenticator = (AuthenticatorBean)Component.getInstance("authenticator");
			authenticator.setSessionId(sessionId);
			Identity.instance().login();
			if (!Identity.instance().isLoggedIn()) {
				resp.setErrorno(Response.RESP_INVALID_SESSION);
			}
			else {
				FacesMessages.instance().clear();
				importSessionData(resp, xmldata);
			}

		} catch (Exception e) {
			resp.setErrorno(Response.RESP_UNEXPECTED_ERROR);
			resp.setErrormsg(e.toString());
			e.printStackTrace();
		}
		return ObjectSerializer.serializeToXml(resp);
	}
	
	
	/**
	 * Import data represented in the XML format
	 * @param resp
	 * @param xmldata
	 */
	protected void importSessionData(Response resp, String xmldata) {
		ExamDST exam = ObjectSerializer.deserializeFromXml(xmldata, ExamDST.class, this);
		if (exam == null)
			return;

		ExamDSTHome home = (ExamDSTHome)Component.getInstance("examDSTHome", true);
		home.setInstance(exam);


		// try to save and check result
		if (!("persisted".equals(home.persist()))) {
			AppFacesMessages fm = (AppFacesMessages)FacesMessages.instance();

			StringBuilder s = new StringBuilder();
			for (StatusMessage msg: fm.getStatusMessages()) {
				if (s.length() > 0)
					s.append(", ");
				msg.interpolate();
				s.append(msg.getSummary());
			}

			resp.setErrormsg(s.toString());
			resp.setErrorno(Response.RESP_VALIDATION_ERROR);
		}
	}


	/* (non-Javadoc)
	 * @see org.msh.datastream.XmlDeserializer.ObjectReferenceable#objectByKey(java.lang.Class, java.lang.Object)
	 */
	@Override
	public Object objectByKey(Class clazz, Object key) {
		if (clazz == TbCase.class) {
			CaseHome home = (CaseHome)Component.getInstance("caseHome", true);
			home.setId(key);
			return home.getInstance();
		}
		
		EntityManager em = (EntityManager)Component.getInstance("entityManager");

		List lst = em.createQuery("from " + clazz.getCanonicalName() + " where id = :id")
			.setParameter("id", key)
			.getResultList();

		return (lst.size() > 0? lst.get(0) : null);
	}
}
