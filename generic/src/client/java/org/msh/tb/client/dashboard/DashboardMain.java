/**
 * 
 */
package org.msh.tb.client.dashboard;

import java.util.ArrayList;

import org.msh.tb.client.AppModule;
import org.msh.tb.client.commons.StandardCallback;
import org.msh.tb.client.shared.DashboardService;
import org.msh.tb.client.shared.DashboardServiceAsync;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;

/**
 * @author Ricardo Memoria
 *
 */
public class DashboardMain extends Composite implements AppModule {
	
	public static final DashboardServiceAsync service = GWT.create(DashboardService.class);
	
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
	protected void updateIndicators(ArrayList<Integer> result) {
		Window.alert("There are " + result.size() + " indicators to be displayed");
	}

}
