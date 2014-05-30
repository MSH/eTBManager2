package org.msh.tb.application.mail;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.User;
import org.msh.tb.entities.Workspace;

/**
 * Main component to send mail messages
 * @author Ricardo Memoria
 *
 */
@Name("mailService")
@AutoCreate
public class MailService {
	
	private Map<String, Object> components;
	private boolean asynchronously = true;
	
	private Queue<MessageData> messages;
	
	/**
	 * Add a component to be available in the context of the mail sender
	 * @param compName
	 * @param component
	 */
	public void addComponent(String compName, Object component) {
		if (components == null)
			components = new HashMap<String, Object>();
		components.put(compName, component);
	}
	
	
	/**
	 * Send a message using the components defined in the <code>addComponent</code> method
	 * @param mailPage
	 */
	public void sendMessage(String mailPage) {
		AsyncMailSender sender = (AsyncMailSender)Component.getInstance("asyncMailSender", true);
		if (asynchronously)
			 sender.sendMessageAsynchronously(mailPage, components);
		else sender.sendMessage(mailPage, components);
	}

	
	/**
	 * Send a message using the components defined in the <code>addComponent</code> method
	 * @param mailPage
	 */
	public void sendLocalizedMessage(String mailPage, String timeZone, String locale) {
		AsyncMailSender sender = (AsyncMailSender)Component.getInstance("asyncMailSender", true);
		if (asynchronously)
			 sender.sendLocalizedMessageAsynchronously(mailPage, components, timeZone, locale);
		else sender.sendLocalizedMessage(mailPage, components, timeZone, locale);
	}

	/**
	 * Add a message to a queue. The message is not immediately sent, but it's dispatched 
	 * when the method <code>dispatchQueue()</code> is called.
	 * @param mailPage
	 * @param timeZone
	 * @param locale
	 */
	public void addMessageToQueue(String mailPage, String timeZone, String locale, User user, boolean systemMessage) {
		// if this is a system message, check if it can be dispatched
		if (systemMessage) {
			Workspace workspace = (Workspace)Component.getInstance("defaultWorkspace");
			if ((workspace != null) && (!workspace.isSendSystemMessages()))
				return;
		}

		// check if user must receive messages from the system
		if ((user != null) && (!user.isSendSystemMessages())) {
			return;
		}
		
		if (messages == null)
			messages = new LinkedList<MessageData>();
		
		MessageData msg = new MessageData(mailPage, components, timeZone, locale);
		components = null;
		messages.add(msg);
	}


	/**
	 * Add a message to a queue using the default time zone and locale.
	 *  The message is not immediately sent, but it's dispatched 
	 * when the method <code>dispatchQueue()</code> is called.
	 * @param mailPage
	 */
	public void addMessageToQueue(String mailPage, boolean systemMessage) {
		addMessageToQueue(mailPage, null, null, null, systemMessage);
	}


	/**
	 * Dispatch all messages in the queue asynchronously 
	 */
	public void dispatchQueue() {
		if (messages == null)
			return;
		AsyncMailSender sender = (AsyncMailSender)Component.getInstance("asyncMailSender", true);
		sender.dispatchMessagesAsynchronously(messages);
		messages = null;
	}
	
	/**
	 * Return the SEAM component instance associated with this class
	 * @return
	 */
	static public MailService instance() {
		return (MailService)Component.getInstance("mailService", true);
	}


	/**
	 * @return the asynchronously
	 */
	public boolean isAsynchronously() {
		return asynchronously;
	}


	/**
	 * @param asynchronously the asynchronously to set
	 */
	public void setAsynchronously(boolean asynchronously) {
		this.asynchronously = asynchronously;
	}
}

