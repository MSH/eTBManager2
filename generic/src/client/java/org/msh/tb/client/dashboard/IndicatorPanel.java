/**
 * 
 */
package org.msh.tb.client.dashboard;

import org.msh.tb.client.reports.chart.ChartReport;
import org.msh.tb.client.reports.chart.ChartType;
import org.msh.tb.client.reports.chart.ChartView;
import org.msh.tb.client.shared.model.CIndicator;
import org.msh.tb.client.tableview.TableData;
import org.msh.tb.client.tableview.TableView;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Ricardo Memoria
 *
 */
public class IndicatorPanel extends Composite {

	interface Binder extends UiBinder<Widget, IndicatorPanel> { }
	private static final Binder binder = GWT.create(Binder.class);

	@UiField TableView tblResult;
	@UiField Label txtTitle;
	@UiField ChartView chart;


	/**
	 * Default constructor
	 */
	public IndicatorPanel() {
		initWidget(binder.createAndBindUi(this));
	}
	
	/**
	 * Update the indicator content with the data inside the given indicator object
	 * @param indicator instance of {@link CIndicator}
	 */
	public void update(CIndicator indicator) {
		txtTitle.setText(indicator.getTitle());
		TableData data = new TableData();
		data.setColVariables(indicator.getColVariables());
		data.setRowVariables(indicator.getRowVariables());
		data.update(indicator.getReportResponse());
		
		tblResult.update(data);
		
		Integer chartIndex = indicator.getChartType();
		if (chartIndex == null) {
			chartIndex = 0;
		}
		
		ChartType type = ChartType.values()[chartIndex];
		chart.setSelectedChart(type);
		
		ChartReport.update(chart, data);
	}
}
