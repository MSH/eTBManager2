package org.msh.tb.application.mail;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.Renderer;
import org.jboss.seam.international.LocaleSelector;
import org.jboss.seam.international.TimeZoneSelector;

import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.TimeZone;


/**
 * Send e-mail messages assyncronously
 * @author Ricardo Memoria
 *
 */
@Name("asyncMailSender")
public class AsyncMailSender {
	private static final Log logger = LogFactory.getLog(MailService.class);

	@In(create=true) Renderer renderer;

	/**
	 * Send mail message using the components in the scope
	 * @param mailPage
	 */
	public void sendMessage(String mailPage, Map<String, Object> components) {
		try {
			if (components != null) {
				for (String name: components.keySet()) {
					Object comp = components.get(name);
					Contexts.getEventContext().set(name, comp);
				}
			}

			renderer.render(mailPage);
			
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}


	/**
	 * Send an asynchronous mail message using a specific time zone and locale
	 * @param mailPage
	 * @param components
	 * @param timeZoneName
	 * @param localeName
	 */
	public void sendLocalizedMessage(String mailPage, Map<String, Object> components, String timeZoneName, String localeName) {
		LocaleSelector localeSelector = LocaleSelector.instance();
		TimeZoneSelector timeZoneSelector = TimeZoneSelector.instance();
		
		Locale locale = LocaleSelector.instance().getLocale();
		TimeZone timeZone = timeZoneSelector.getTimeZone();
		
		try {
			if (localeName != null)
				localeSelector.setLocaleString(localeName);
			
			if (timeZoneName != null)
				timeZoneSelector.setTimeZoneId(timeZoneName);
				
			sendMessage(mailPage, components);			
		}
		finally {
			localeSelector.setLocale(locale);
			timeZoneSelector.setTimeZone(timeZone);
		}
	}


	@Asynchronous
	public void sendMessageAsynchronously(String mailPage, Map<String, Object> components) {
		sendMessage(mailPage, components);
	}


	@Asynchronous
	public void sendLocalizedMessageAsynchronously(String mailPage, Map<String, Object> components, String timeZoneName, String localeName) {
		sendLocalizedMessage(mailPage, components, timeZoneName, localeName);
	}

	
	/**
	 * Dispatch messages in the queue
	 */
	@Asynchronous
	public void dispatchMessagesAsynchronously(Queue<MessageData> messages) {
		while (!messages.isEmpty()) {
			MessageData msg = messages.poll();

			if ((msg.getLocale() != null) || (msg.getTimezone() != null))
				 sendLocalizedMessage(msg.getMailPage(), msg.getComponents(), msg.getTimezone(), msg.getLocale());
			else sendMessage(msg.getMailPage(), msg.getComponents());
		}
	}

	
	/**
	 * Return the application instance of the component
	 * @return
	 */
	public static AsyncMailSender instance() {
		return (AsyncMailSender)Component.getInstance("asyncMailSender");
	}

}
