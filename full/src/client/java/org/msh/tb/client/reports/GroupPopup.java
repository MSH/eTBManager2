package org.msh.tb.client.reports;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.*;
import org.msh.tb.client.commons.StandardEventHandler;
import org.msh.tb.client.shared.model.CGroup;
import org.msh.tb.client.ui.AnchorData;

import java.util.List;

public abstract class GroupPopup extends PopupPanel {

	public static Integer ITEM_SELECTED = 100;
	
	private ScrollPanel pnlContent;
	private StandardEventHandler eventHandler;
	
	public GroupPopup() {
		super(true);

		setStyleName("group-popup");
		pnlContent = new ScrollPanel();
		pnlContent.setWidth("300px");
		pnlContent.setHeight("400px");
		add(pnlContent);

        final GroupPopup grp = this;
        addCloseHandler(new CloseHandler<PopupPanel>() {
            @Override
            public void onClose(CloseEvent<PopupPanel> popupPanelCloseEvent) {
                grp.eventHandler = null;
            }
        });
	}

	/**
	 * @return the groups
	 */
	public List<CGroup> getGroups() {
		return ReportUtils.getReportUIData().getGroups();
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

        List<CGroup> groups = getGroups();
		if (groups == null)
			return;

		VerticalPanel pnl = new VerticalPanel();
		pnl.setStyleName("group-panel");
		for (CGroup group: groups) {
			if (hasItems(group)) {
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
		}
		
		pnlContent.add(pnl);
	}

	
	/**
	 * Indicate if the given groups has child items to be included, i.e,
	 * variables or items, depending on the pop up display
	 * If the group has no item, it will not be displayed in the 
	 * list of groups. This method is called internally by the class.
	 * @param group the instance of {@link CGroup} to be tested
	 * @return true if the group has items
	 */
	protected abstract boolean hasItems(CGroup group);

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
					eventHandler.handleEvent(ITEM_SELECTED, lnk.getData());
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
