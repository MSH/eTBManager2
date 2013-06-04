package org.msh.tb.client.commons;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ComplexPanel;

/**
 * GWT widget that displays a text message wrapped inside HTML divs ready to 
 * be decorated within three main types - error, info and warning
 * @author Ricardo Memoria
 *
 */
public class MessagePanel extends ComplexPanel {

	private final Element mainDiv = DOM.createDiv();
	private final Element innerMessage = DOM.createDiv();
	
	private MessageType type = MessageType.ERROR;
	
	public enum MessageType { ERROR, WARNING, INFO };
	
	public MessagePanel() {
		super();
		setElement(mainDiv);
		mainDiv.insertFirst(innerMessage);
		setStyleName("messagePanel");
	}

	public void setText(String txt) {
		innerMessage.setInnerText(txt);
	}
	
	public String getText() {
		return innerMessage.getInnerText();
	}
	
	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.UIObject#setStyleName(java.lang.String)
	 */
	@Override
	public void setStyleName(String name) {
		super.setStyleName(name);
		updateStyles();
	}

	protected void updateStyles() {
		String s = getStyleName();
		String suffix = "-" + type.toString().toLowerCase();
		removeStyleName(s + suffix);
		
		mainDiv.setAttribute("class", s + " " + s + suffix);
		innerMessage.setClassName(s + "-inner");
	}
	
	/**
	 * @return the type
	 */
	public MessageType getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(MessageType type) {
		this.type = type;
		updateStyles();
	}
}
