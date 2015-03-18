package org.msh.tb.webservices;

import org.jboss.seam.Component;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.security.Identity;
import org.msh.tb.application.AppFacesMessages;
import org.msh.tb.application.TransactionManager;
import org.msh.tb.login.AuthenticatorBean;

import javax.persistence.EntityManager;
import javax.xml.bind.ValidationException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;


/**
 * Support for remote procedure calls (web services), handling authentication, transaction, error and preparation of response to the client.
 * This class wraps common operations when dealing with remote calls from Web Services. This is an abstract class and its main usage is
 * being declared as an anonymous class implementing the <code>execute();</code> method. The <code>execute()</code> method must implement
 * the action to be performed, and its return value is serialized (to XML) and sent back to the client as a {@link Response} object.
 * <p/> 
 * The action wrapped by {@link RemoteActionHandler} is fired by calling the <code>run();</code> method. Inside the <code>run()</code> method, 
 * the class will try to authenticate the client (if the sessionID) was provided, start a transaction (if the getTransaction() is true) 
 * and call the <code>execute()</code> method. It also catches any exception thrown and wrap them inside the {@link Response} object
 * sent back to the client.
 * <p/>
 * If the <code>sessionId</code> is not provided (by the constructor or the setSessionId() method), the authentication will not be done,
 * and it's the responsibility of the action to authenticate or answer the client without authentication.
 *   
 * @author Ricardo Memoria
 *
 */
public abstract class RemoteActionHandler<E extends Response> {

	private String sessionId;
	private Object data;
	private Response response;
	private TransactionManager transaction;
	private boolean transactional= true;
	
	/**
	 * Constructor of the class passing the client session identification as parameter. If the 
	 * sessionId is provided, the class will try to authenticate the client before executing
	 * the action
	 * @param sessionId
	 */
	public RemoteActionHandler(String sessionId) {
		super();
		this.sessionId = sessionId;
	}
	
	
	public RemoteActionHandler(String sessionId, Object data) {
		super();
		this.sessionId = sessionId;
		this.data = data;
	}


	/**
	 * Default constructor of the class
	 */
	public RemoteActionHandler() {
		super();
	}


	/**
	 * Abstract method that should be implemented to execute the action of the remote call. This method
	 * is never called directly, but by the <code>run()</code> method.
	 * <p/>
	 * @return The result of the action. This result will be serialized to XML using the {@link XmlSerializer} class
	 * and make available as a response to the client by the return of the method <code>run()</code>
	 */
	protected abstract Object execute(Object data) throws ValidationException;


    /**
     * Get the result type used as generic type
     * @return Class of E
     */
    public Class<Response> getResultType() {
        Type[] types = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments();

        if (types.length  == 0) {
            return null;
        }

        return (Class<Response>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
    }

    /**
     * Create instance of the response class
     * @return
     */
    protected Response createResponseInstance() {
        try {
            Class<Response> clazz = getResultType();
            if (clazz != null) {
                return clazz.newInstance();
            }
        }
        catch (Exception e) {
            // do nothing, because there is no response class
//            throw new RuntimeException(e);
        }

        return new Response();
    }

    /**
     * Return the response object that will be serialized to the caller
     * @return instance of the Response class
     */
    public E getResponse() {
        if (response == null) {
            response = createResponseInstance();
        }
        return (E)response;
    }

	/**
	 * Run the remote call handler, calling the <code>execute()</code> method. If information about user
	 * authentication is supported, the system will call the authenticate method before. 
	 * @return instance of the {@link Response} class already serialized to the client
	 */
	public E run() {
		response = getResponse();
		try {
			// authenticate first ?
			if (sessionId != null) {
				if (!authenticate())
					return (E)response;
			}

			if (transactional)
				beginTransaction();

			// run the action
			Object result = execute(data);
			
			if (transactional)
				commitTransaction();

			if (result != null)
				response.setResult( ObjectSerializer.serializeToXml(result) );

		} catch (ValidationException ve) {
			if (transactional)
				rollbackTransaction();
			setResponseError(Response.RESP_VALIDATION_ERROR, ve.getMessage());
		}
		catch (Exception e) {
			e.printStackTrace();
			if (transactional)
				rollbackTransaction();
			setResponseError(Response.RESP_UNEXPECTED_ERROR, e.toString());
		}
		
		return (E)response;
//		return getSerializedResponse();
	}

	/**
	 * Check if there are messages declared in the {@link FacesMessages} SEAM component. If there
	 * is no message, the method returns false. If there are messages, the system returns
	 * true and fills the response with the proper validation error messages  
	 * @return
	 */
	protected boolean checkValidationErrors() {
		AppFacesMessages fm = (AppFacesMessages)FacesMessages.instance();

		List<StatusMessage> msgs = fm.getStatusMessages();
		if (msgs.size() == 0)
			return false;

		StringBuilder s = new StringBuilder();
		for (StatusMessage msg: msgs) {
			if (s.length() > 0)
				s.append(", ");
			msg.interpolate();
			s.append(msg.getSummary());
		}

		setResponseError(Response.RESP_VALIDATION_ERROR, s.toString());
		return true;
	}

	/**
	 * Set the error number and error msg of the response
	 * @param errorno
	 * @param errormsg
	 */
	public void setResponseError(int errorno, String errormsg) {
		response.setErrorno(errorno);
		response.setErrormsg(errormsg);
	}

	/**
	 * Create a serialized XML of the response property
	 * @return
	 */
	protected String getSerializedResponse() {
		return ObjectSerializer.serializeToXml(response);
	}

	/**
	 * Authenticate the remote call using the session identification
	 * @return
	 */
	protected boolean authenticate() {
		AuthenticatorBean authenticator = (AuthenticatorBean)Component.getInstance("authenticator");
		authenticator.setSessionId(sessionId);
		// instantiate credentials, otherwise the authenticator method will not be called
		Identity.instance().getCredentials();
		Identity.instance().login();

		if (!Identity.instance().isLoggedIn()) {
			response.setErrorno(Response.RESP_INVALID_SESSION);
			return false;
		}

		FacesMessages.instance().clear();
		return true;
	}

	/**
	 * @return the sessionId
	 */
	public String getSessionId() {
		return sessionId;
	}
	
	
	/**
	 * @param xmldata
     * @param clazz
	 * @return
	 */
	public <T> T deserializeFromXml(String xmldata, Class<T> clazz) {
		return ObjectSerializer.deserializeFromXml(xmldata, clazz);
	}


	/**
	 * Start a new transaction
	 */
	public void beginTransaction() {
		getTransaction().begin();
	}
	

	/**
	 * Commit a transaction that is under progress 
	 */
	public void commitTransaction() {
		getTransaction().commit();
	}


	/**
	 * Roll back a transaction that is under progress 
	 */
	public void rollbackTransaction() {
		getTransaction().rollback();
	}
	

	/**
	 * Return the transaction in use by the task
	 * @return
	 */
	protected TransactionManager getTransaction() {
		if (transaction == null)
			transaction = (TransactionManager)Component.getInstance("transactionManager");
		return transaction;
	}

	/**
	 * Return the {@link EntityManager} instance in use
	 * @return
	 */
	public EntityManager getEntityManager() {
		return transaction.getEntityManager();
	}

	/**
	 * @return the transactional
	 */
	public boolean isTransactional() {
		return transactional;
	}

	/**
	 * @param transactional the transactional to set
	 */
	public void setTransactional(boolean transactional) {
		this.transactional = transactional;
	}


}
