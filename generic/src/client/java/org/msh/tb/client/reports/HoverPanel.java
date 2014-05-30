package org.msh.tb.client.reports;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;

public abstract class HoverPanel extends FlowPanel  implements MouseOverHandler, MouseOutHandler {

	private static final String selectedStyle = "hover-panel-selected";
	private Anchor removeButton;
	private boolean removeEnabled = true;

	public HoverPanel() {
		setStyleName("hover-panel");
		removeButton = new Anchor("X");
		removeButton.setStyleName("del-button");
		removeButton.setVisible(false);
		removeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				removePanel();
			}
		});
		add(removeButton);

		addDomHandler(this, MouseOverEvent.getType());
		addDomHandler(this, MouseOutEvent.getType());		
	}
	

	/**
	 * Called to remove itself
	 */
	protected abstract void removePanel();

	@Override
	public void onMouseOut(MouseOutEvent event) {
		removeButton.setVisible(false);
		removeStyleName(selectedStyle);
	}

	@Override
	public void onMouseOver(MouseOverEvent event) {
		removeButton.setVisible(true && removeEnabled);
		addStyleName(selectedStyle);
	}


	/**
	 * @return the removeEnabled
	 */
	public boolean isRemoveEnabled() {
		return removeEnabled;
	}


	/**
	 * @param removeEnabled the removeEnabled to set
	 */
	public void setRemoveEnabled(boolean removeEnabled) {
		this.removeEnabled = removeEnabled;
		removeButton.setVisible(false);
	}

}
