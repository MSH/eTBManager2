package org.msh.tb.application;

import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.msh.tb.login.AsyncMailSender;

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

