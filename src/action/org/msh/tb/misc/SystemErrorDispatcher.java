package org.msh.tb.misc;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.async.Asynchronous;
import org.msh.mdrtb.entities.ErrorLog;
import org.msh.mdrtb.entities.SystemConfig;
import org.msh.mdrtb.entities.User;
import org.msh.mdrtb.entities.UserLogin;

@Name("systemErrorDispatcher")
public class SystemErrorDispatcher {

	@In(create=true) DmSystemHome dmsystem;
	@In(create=true) SystemConfig systemConfig;
	
	private ErrorLog errorLog; 
	
	@Asynchronous
	@Transactional
	public void dispatch(Exception exception, UserLogin userLogin, String url) {

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
		
		if (userLogin != null) {
			User user = userLogin.getUser();
			errorLog.setUser(user.getLogin() + " - " + user.getName());
			errorLog.setUserId(user.getId());
			errorLog.setWorkspace(userLogin.getWorkspace().getName().toString());
		}
		
		entityManager.persist(errorLog);
		entityManager.flush();
		
		if (systemConfig.getAdminMail() != null)
			dmsystem.enviarEmail("systemerror.xhtml");
	}

	
	/**
	 * @return the errorLog
	 */
	public ErrorLog getErrorLog() {
		return errorLog;
	}
}
