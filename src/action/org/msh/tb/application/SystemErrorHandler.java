package org.msh.tb.application;

import javax.faces.application.ViewExpiredException;
import javax.faces.context.FacesContext;
import javax.persistence.OptimisticLockException;
import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.framework.EntityNotFoundException;
import org.jboss.seam.security.AuthorizationException;
import org.jboss.seam.security.NotLoggedInException;
import org.msh.tb.entities.UserLogin;

@Name("systemErrorHandler")
public class SystemErrorHandler {

	@In(create=true) SystemErrorDispatcher systemErrorDispatcher;
	@In(required=false) UserLogin userLogin;
	
	@Observer("org.jboss.seam.exceptionNotHandled")
	public void handleExceptionNotHandled() {
		registerException();
	}
	
	@Observer("org.jboss.seam.exceptionHandled")
	public void handleExceptionHandled() {
		registerException();
	}
	
	protected void registerException() {
		Exception exception = (Exception)Component.getInstance("org.jboss.seam.handledException");
		if ((exception == null) || (exception instanceof ViewExpiredException) ||
			(exception instanceof IllegalStateException) || (exception instanceof OptimisticLockException) ||
			(exception instanceof EntityNotFoundException) || (exception instanceof javax.persistence.EntityNotFoundException) ||
			(exception instanceof NotLoggedInException) || (exception instanceof AuthorizationException))
			return;

		FacesContext facesContext = FacesContext.getCurrentInstance();

		// mount original URL
		String url = null;

		StringBuilder reqdata = new StringBuilder();
		
		if (facesContext != null) {
			HttpServletRequest req = (HttpServletRequest)facesContext.getExternalContext().getRequest();
//			HttpServletRequest req = (HttpServletRequest)contexts.getRequest();
			
			url = req.getRequestURL().toString();
			reqdata.append("ip address = " + req.getRemoteAddr());
			reqdata.append("\n* browser = " + req.getHeader("user-Agent"));
			reqdata.append("\n* method = " + req.getMethod());
			reqdata.append("\n* auth type = " + req.getAuthType());
			reqdata.append("\n* context path = " + req.getContextPath());
			reqdata.append("\n* path info = " + req.getPathInfo());
			reqdata.append("\n* path translated = " + req.getPathTranslated());
			reqdata.append("\n* query string = " + req.getQueryString());
			reqdata.append("\n* remote user = " + req.getRemoteUser());
			reqdata.append("\n* requested session id = " + req.getRequestedSessionId());
			reqdata.append("\n* request URI = " + req.getRequestURI());
			reqdata.append("\n* request URL = " + req.getRequestURL());
			reqdata.append("\n* servlet path = " + req.getServletPath());
			reqdata.append("\n* is Request session id valid = " + req.isRequestedSessionIdValid());


			// check if it's the seam error page that is generating the error. In this case does nothing
			if (url.endsWith("errorall.seam"))
				return;
			String query = req.getQueryString();
			if (query != null)
				url += "?" + query;
		}
		else url = null;

		systemErrorDispatcher.dispatch(exception, userLogin, url, reqdata.toString());			
	}
}
