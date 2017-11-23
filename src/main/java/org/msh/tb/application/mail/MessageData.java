package org.msh.tb.application.mail;

import java.util.Map;

public class MessageData {
	private String mailPage;
	private Map<String, Object> components;
	private String timezone;
	private String locale;
	
	public MessageData(String mailPage, Map<String, Object> components,
			String timezone, String locale) {
		super();
		this.mailPage = mailPage;
		this.components = components;
		this.timezone = timezone;
		this.locale = locale;
	}

	/**
	 * @return the mailPage
	 */
	public String getMailPage() {
		return mailPage;
	}
	/**
	 * @return the components
	 */
	public Map<String, Object> getComponents() {
		return components;
	}
	/**
	 * @return the timezone
	 */
	public String getTimezone() {
		return timezone;
	}
	/**
	 * @return the locale
	 */
	public String getLocale() {
		return locale;
	}
}
