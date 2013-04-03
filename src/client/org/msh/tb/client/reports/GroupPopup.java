package org.msh.tb.client.reports;

import java.util.List;

import org.msh.tb.client.commons.AnchorData;
import org.msh.tb.client.commons.StandardEventHandler;
import org.msh.tb.client.shared.model.CGroup;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public abstract class GroupPopup extends PopupPanel {

	public static Integer ITEM_SELECTED = 100;
	
	private List<CGroup> groups;
	private ScrollPanel pnlContent;
	private StandardEventHandler eventHandler;
	
	public GroupPopup(StandardEventHandler eventHandler) {
		super(true);
		this.eventHandler = eventHandler;

		setStyleName("group-popup");
		pnlContent = new ScrollPanel();
		pnlContent.setWidth("300px");
		pnlContent.setHeight("400px");
		add(pnlContent);
	}

	/**
	 * @return the groups
	 */
	public List<CGroup> getGroups() {
		return groups;
	}

	/**
	 * @param groups the groups to set
	 */
	public void setGroups(List<CGroup> groups) {
		this.groups = groups;
	}

	
	/**
	 * Mount the list of options for the given group
	 * @param grp
	 * @param pnlOptions
	 */
	protected abstract void initializeOptions(CGroup grp, VerticalPanel pnlOptions);
	
	/**
	 * Called when the group is clicked
	 * @param event
	 */
	protected void groupClick(ClickEvent event) {
		AnchorData lnk = (AnchorData)event.getSource();
		VerticalPanel pnl = (VerticalPanel)lnk.getParent();
		// options were initialized ?
		if (pnl.getWidgetCount() == 1) {
			VerticalPanel pnlOptions = new VerticalPanel();
			pnlOptions.setStyleName("group-options");
			initializeOptions((CGroup)lnk.getData(), pnlOptions);
			pnl.add(pnlOptions);
		}
		else {
			// if options are initialized, them toggle status
			VerticalPanel pnlOptions = (VerticalPanel)pnl.getWidget(1);
			pnlOptions.setVisible( !pnlOptions.isVisible() );
		}
	}

	/**
	 * Show the filter in the popup window
	 * @param source
	 */
	public void showPopup(Widget source) {
		initializeGroups();
		showRelativeTo(source);
	}

	
	/**
	 * Prepare the filters to be displayed
	 */
	private void initializeGroups() {
		pnlContent.clear();

		if (groups == null)
			return;

		VerticalPanel pnl = new VerticalPanel();
		pnl.setStyleName("group-panel");
		for (CGroup group: groups) {
			VerticalPanel pnlGroup = new VerticalPanel();
			AnchorData lnk = new AnchorData(group.getName(), group);
			lnk.setStyleName("group-link");
			lnk.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					groupClick(event);
				}
			});
			pnlGroup.add(lnk);
			pnl.add(pnlGroup);
		}
		
		pnlContent.add(pnl);
	}

	
	/**
	 * Add a new item to the list of options
	 * @param parent
	 * @param label
	 * @param item
	 */
	protected void addItem(Panel parent, String label, Object item) {
		AnchorData lnk = new AnchorData(label, item);
		lnk.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (eventHandler != null) {
					AnchorData lnk = (AnchorData)event.getSource();
					eventHandler.eventHandler(ITEM_SELECTED, lnk.getData());
				}
			}
		});
		parent.add(lnk);
	}

	/**
	 * @return the eventHandler
	 */
	public StandardEventHandler getEventHandler() {
		return eventHandler;
	}

	/**
	 * @param eventHandler the eventHandler to set
	 */
	public void setEventHandler(StandardEventHandler eventHandler) {
		this.eventHandler = eventHandler;
	}
	

}
