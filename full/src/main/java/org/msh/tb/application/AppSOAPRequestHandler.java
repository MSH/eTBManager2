package org.msh.tb.application;

import org.jboss.seam.contexts.ServletLifecycle;
import org.jboss.seam.web.ServletContexts;
import org.jboss.seam.web.Session;
import org.jboss.seam.webservice.SOAPRequestHandler;

import javax.xml.ws.handler.MessageContext;

/**
 * eTB Manager own implementation of SOAP Request Handler. It seems there is a bug in the
 * default SEAM SOAP request handler, because it doesn't handle end of session, so 
 * when a web service is called, it creates a session context that remains until the 
 * time out period. This class apparently fixes that and turn all web services as
 * stateless.
 * 
 * @author Ricardo Memoria
 *
 */
public class AppSOAPRequestHandler extends SOAPRequestHandler {


	/* (non-Javadoc)
	 * @see org.jboss.seam.webservice.SOAPRequestHandler#close(javax.xml.ws.handler.MessageContext)
	 */
	@Override
	public void close(MessageContext messageContext) {
		Session.instance().invalidate();
		ServletLifecycle.endRequest( ServletContexts.instance().getRequest() );
		super.close(messageContext);
	}

}
