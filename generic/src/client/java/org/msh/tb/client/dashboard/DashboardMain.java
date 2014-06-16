/**
 * 
 */
package org.msh.tb.client.dashboard;

import java.util.ArrayList;

import org.msh.tb.client.AppModule;
import org.msh.tb.client.commons.StandardCallback;
import org.msh.tb.client.shared.DashboardService;
import org.msh.tb.client.shared.DashboardServiceAsync;
import org.msh.tb.client.shared.model.CIndicator;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Main page that display the dashboard
 * 
 * @author Ricardo Memoria
 *
 */
public class DashboardMain extends Composite implements AppModule {

	interface Binder extends UiBinder<Widget, DashboardMain> { }
	private static final Binder binder = GWT.create(Binder.class);

	// service to display the dashboard
	public static final DashboardServiceAsync service = GWT.create(DashboardService.class);
	
	@UiField FlowPanel pnlContent;
	
	/**
	 * Default constructor
	 */
	public DashboardMain() {
		initWidget(binder.createAndBindUi(this));
	}
	
	/** {@inheritDoc}
	 */
	@Override
	public void run() {
		service.initialize(new StandardCallback<ArrayList<Integer>>() {
			@Override
			public void onSuccess(ArrayList<Integer> result) {
				updateIndicators(result);
			}
		});
	}

	/**
	 * Update the list of indicators displayed
	 * @param result
	 */
	protected void updateIndicators(ArrayList<Integer> indicators) {
		pnlContent.clear();
		updateIndicator(indicators, 0);
	}

	
	/**
	 * Update an indicator and call recursively the next indicator 
	 * @param id
	 */
	private void updateIndicator(ArrayList<Integer> indicators, int index) {
		if (index < indicators.size()) {
			final ArrayList<Integer> lst = indicators;
			final int i = index + 1;
			service.generateIndicator(indicators.get(index), new StandardCallback<CIndicator>() {
				@Override
				public void onSuccess(CIndicator result) {
					addIndicator(result, i);
					updateIndicator(lst, i);
				}
			});
		}
	}

	
	/**
	 * Add a new indicator
	 * @param indicator
	 */
	protected void addIndicator(CIndicator indicator, int index) {
		IndicatorPanel pnl = new IndicatorPanel();
		pnlContent.add(pnl);
		if (index % 2 == 1) {
			pnl.getElement().setAttribute("style", "clear:left");
		}
		pnl.update(indicator);
	}
}
