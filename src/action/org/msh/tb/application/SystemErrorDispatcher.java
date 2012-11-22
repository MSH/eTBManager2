package org.msh.tb.application;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.Renderer;
import org.msh.tb.entities.ErrorLog;
import org.msh.tb.entities.SystemConfig;
import org.msh.tb.entities.User;
import org.msh.tb.entities.UserLogin;
import org.msh.tb.misc.DmSystemHome;

@Name("systemErrorDispatcher")
public class SystemErrorDispatcher {

	@In(create=true) DmSystemHome dmsystem;
	@In(create=true) EtbmanagerApp etbmanagerApp;
	
	private ErrorLog errorLog; 
	
	@Asynchronous
	@Transactional
	public void dispatch(Exception exception, UserLogin userLogin, String url, String ipAddress, String agent) {

		EntityManager entityManager = (EntityManager)Component.getInstance("entityManager");
		
		errorLog = new ErrorLog();
		errorLog.setErrorDate(new Date());
		errorLog.setExceptionClass(exception.getClass().getName());
		String s = exception.getMessage();
		if (s == null)
			 s = "-No message in exception-";
		else if (s.length() > 200) s = s.substring(0, 199);
		errorLog.setExceptionMessage(s);
		StringWriter writer = new StringWriter();
		PrintWriter pw = new PrintWriter(writer);
		exception.printStackTrace(pw);
		errorLog.setStackTrace(writer.toString());
		errorLog.setUrl(url);
		errorLog.setIpAddress(ipAddress);
		if (agent.length() > 200)
			agent = agent.substring(0, 199);
		errorLog.setUserAgent(agent);
		
		if (userLogin != null) {
			User user = userLogin.getUser();
			errorLog.setUser(user.getLogin() + " - " + user.getName());
			errorLog.setUserId(user.getId());
			errorLog.setWorkspace(userLogin.getWorkspace().getName().toString());
		}
		
		entityManager.persist(errorLog);
		entityManager.flush();

		// check if eTB Manager is in development mode. If so, doesn't send the message
		if (!etbmanagerApp.isDevelopmentMode()) {
			SystemConfig systemConfig = etbmanagerApp.getConfiguration();
			
			String destMails = systemConfig.getAdminMail();
			
			if ((destMails != null) && (!destMails.isEmpty())) {
				String[] dests = destMails.split("[;,]+");
				for (String dest: dests) {
					Contexts.getEventContext().set("adminMail", dest);
					Renderer.instance().render("/mail/systemerror.xhtml");
				}
			}
		}
	}

	
	/**
	 * @return the errorLog
	 */
	public ErrorLog getErrorLog() {
		return errorLog;
	}
}
