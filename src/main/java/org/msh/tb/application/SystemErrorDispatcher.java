package org.msh.tb.application;

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

import javax.persistence.EntityManager;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

/**
 * Register information about exceptions and its associated user session and
 * dispatch an e-mail message to the administrators about it. 
 * 
 * @author Ricardo Memoria
 *
 */
@Name("systemErrorDispatcher")
public class SystemErrorDispatcher {

	@In(create=true) DmSystemHome dmsystem;
	@In(create=true) EtbmanagerApp etbmanagerApp;
	
	private ErrorLog errorLog; 
	
	/**
	 * Register the exception in the database and send an e-mail message to the system administrators
	 * (registered in the system settings)
	 * 
	 * @param exception is the exception thrown
	 * @param userLogin instance of the {@link UserLogin} containing information about the user session
	 * @param url is the URL that caused the error
	 * @param request additional information about the error
	 */
	@Asynchronous
	@Transactional
	public void dispatch(Exception exception, UserLogin userLogin, String url, String request) {

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
		errorLog.setRequest(request);
		
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

	/**
	 * @param errorLog the errorLog to set
	 */
	public void setErrorLog(ErrorLog errorLog) {
		this.errorLog = errorLog;
	}

}
