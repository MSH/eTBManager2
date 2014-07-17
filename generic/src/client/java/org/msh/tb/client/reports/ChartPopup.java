package org.msh.tb.client.reports;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.msh.tb.client.AppResources;
import org.msh.tb.client.chart.ChartType;
import org.msh.tb.client.commons.StandardEventHandler;

/**
 * Display a pop-up window that displays the chart icons
 * for selection of the user
 * 
 * @author Ricardo Memoria
 *
 */
public class ChartPopup extends PopupPanel {

    private StandardEventHandler eventHandler;

    /**
     * List of chart images to be displayed for chart selection
     */
    public static ImageResource[] chartImgs = {
            AppResources.imageResources().imgChartLine(),
            AppResources.imageResources().imgChartSpline(),
            AppResources.imageResources().imgChartArea(),
            AppResources.imageResources().imgChartAreaSpline(),
            AppResources.imageResources().imgChartColumn(),
            AppResources.imageResources().imgChartBar(),
            AppResources.imageResources().imgChartPie() };

    /**
     * Default constructor
     */
	public ChartPopup() {
		VerticalPanel pnl = new VerticalPanel();

		for (int i = 0; i < chartImgs.length; i++) {
			Anchor lnk = new Anchor();
			lnk.addStyleName("chart-button");
			lnk.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					Anchor lnk = (Anchor)event.getSource();
					chartLinkClick(lnk);
				}
			});
			Image img = new Image(chartImgs[i]);
			DOM.insertBefore(lnk.getElement(), img.getElement(), DOM.getFirstChild(lnk.getElement()));
			pnl.add(lnk);
		}
		pnl.setWidth("40px");
		pnl.setHeight(Integer.toString(chartImgs.length * 36) + "px");
		add(pnl);
		setAutoHideEnabled(true);
		addStyleName("chart-popup");
	}

    /**
     * Return the image of the selected chart
     * @return instance of {@link com.google.gwt.resources.client.ImageResource}
     */
    public static ImageResource getChartImage(ChartType type) {
        return type != null? chartImgs[type.ordinal()]: null;
    }

	/**
	 * Called when a chart link is clicked
	 * @param lnk the link
	 */
	protected void chartLinkClick(Anchor lnk) {
		VerticalPanel pnl = (VerticalPanel)lnk.getParent();
		int chartIndex = pnl.getWidgetIndex(lnk);
        ChartType type = ChartType.values()[chartIndex];
        if (eventHandler != null) {
            eventHandler.handleEvent(this, type);
        }
//		ReportMain.instance().selectChart(ChartType.values()[chartIndex]);
		hide();
	}

    public StandardEventHandler getEventHandler() {
        return eventHandler;
    }

    public void setEventHandler(StandardEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }
}
