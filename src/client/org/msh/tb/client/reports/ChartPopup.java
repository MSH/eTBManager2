package org.msh.tb.client.reports;

import org.msh.tb.client.reports.chart.ChartType;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Display a pop-up window that displays the chart icons
 * for selection of the user
 * 
 * @author Ricardo Memoria
 *
 */
public class ChartPopup extends PopupPanel {

	public ChartPopup() {
		VerticalPanel pnl = new VerticalPanel();

		for (int i = 0; i < MainPage.chartImgs.length; i++) {
			Anchor lnk = new Anchor();
			lnk.addStyleName("chart-button");
			lnk.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					Anchor lnk = (Anchor)event.getSource();
					chartLinkClick(lnk);
				}
			});
			Image img = new Image(MainPage.chartImgs[i]);
			DOM.insertBefore(lnk.getElement(), img.getElement(), DOM.getFirstChild(lnk.getElement()));
			pnl.add(lnk);
		}
		pnl.setWidth("40px");
		pnl.setHeight(Integer.toString(MainPage.chartImgs.length * 36) + "px");
		add(pnl);
		setAutoHideEnabled(true);
		addStyleName("chart-popup");
	}
	
	/**
	 * Called when a chart link is clicked
	 * @param lnk
	 */
	protected void chartLinkClick(Anchor lnk) {
		VerticalPanel pnl = (VerticalPanel)lnk.getParent();
		int chartIndex = pnl.getWidgetIndex(lnk);
		MainPage.instance().selectChart(ChartType.values()[chartIndex]);
		hide();
	}
}
