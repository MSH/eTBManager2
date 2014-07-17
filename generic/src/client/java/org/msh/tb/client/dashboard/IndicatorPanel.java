/**
 * 
 */
package org.msh.tb.client.dashboard;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import org.msh.tb.client.shared.model.CIndicator;
import org.msh.tb.client.tableview.TableView;

/**
 * @author Ricardo Memoria
 *
 */
public class IndicatorPanel extends Composite {

	interface Binder extends UiBinder<Widget, IndicatorPanel> { }
	private static final Binder binder = GWT.create(Binder.class);

	@UiField TableView tblResult;
	@UiField Label txtTitle;
//	@UiField ChartView chart;


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
/*
		txtTitle.setText(indicator.getTitle());
		TableData data = new TableData();
		data.setColVariables(indicator.getColVariables());
		data.setRowVariables(indicator.getRowVariables());
		data.update(indicator.getReportResponse());
		
		if (indicator.getTblSelectedCell() != null) {
			data.setSelectedCell(indicator.getTblSelectedCell());
		}
		
		if (indicator.getTblSelection() != null) {
			TableSelection sel = TableSelection.values()[indicator.getTblSelection()];
			data.setSelection(sel);
		}
		
		tblResult.update(data);
		
		Integer chartIndex = indicator.getChartType();
		if (chartIndex == null) {
			chartIndex = 0;
		}
		
		CChartType type = CChartType.values()[chartIndex];
		chart.setShowTitle(false);
		chart.setSelectedChart(type);
		chart.setWidth("400px");
		
		ChartReport.update(chart, data);
*/
	}
}
