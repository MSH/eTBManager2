package org.msh.tb.client.ui;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
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
    private final Element closeAnchor = DOM.createAnchor();
	
	private MessageType type = MessageType.ERROR;
	
	public enum MessageType { ERROR, WARNING, INFO };

    /**
     * Default constructor
     */
	public MessagePanel() {
		super();
		setElement(mainDiv);
		mainDiv.insertFirst(innerMessage);
        closeAnchor.setAttribute("class", "close-btn");

        Element icon = DOM.createElement("i");
        icon.setAttribute("class", "icon-remove");
        closeAnchor.insertFirst(icon);

        DOM.sinkEvents(closeAnchor, Event.ONCLICK);
        DOM.setEventListener(closeAnchor, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                setVisible(false);
            }
        });

        mainDiv.insertFirst(closeAnchor);
		setStyleName("msg");
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
        String[] vals = s.split(" ");
        s = vals[0] + " " + vals[0] + "-" + type.toString().toLowerCase();

		mainDiv.setAttribute("class", s);
		innerMessage.setClassName("msg-inner");
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
