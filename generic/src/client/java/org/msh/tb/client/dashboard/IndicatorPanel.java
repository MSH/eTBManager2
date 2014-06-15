/**
 * 
 */
package org.msh.tb.client.dashboard;

import org.msh.tb.client.shared.model.CIndicator;
import org.msh.tb.client.tableview.TableData;
import org.msh.tb.client.tableview.TableView;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Ricardo Memoria
 *
 */
public class IndicatorPanel extends Composite {

	interface Binder extends UiBinder<Widget, IndicatorPanel> { }
	private static final Binder binder = GWT.create(Binder.class);

	@UiField TableView tblResult;


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
		TableData data = new TableData();
		data.setColVariables(indicator.getColVariables());
		data.setRowVariables(indicator.getRowVariables());
		data.update(indicator.getReportResponse());
		
		tblResult.update(data);
	}
}
