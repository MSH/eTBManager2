package org.msh.tb.application;

import javax.faces.application.ViewExpiredException;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.security.AuthorizationException;
import org.jboss.seam.security.NotLoggedInException;
import org.msh.mdrtb.entities.UserLogin;

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
			(exception instanceof IllegalStateException) ||
			(exception instanceof NotLoggedInException) || (exception instanceof AuthorizationException))
			return;

		FacesContext facesContext = FacesContext.getCurrentInstance();

		// mount original URL
		String url;
		if (facesContext != null) {
			HttpServletRequest req = (HttpServletRequest)facesContext.getExternalContext().getRequest();
			url = req.getRequestURL().toString();

			// check if it's the seam error page that is generating the error. In this case does nothing
			if (url.endsWith("errorall.seam"))
				return;
			String query = req.getQueryString();
			if (query != null)
				url += "?" + query;
		}
		else url = null;

		systemErrorDispatcher.dispatch(exception, userLogin, url);			
	}
}
