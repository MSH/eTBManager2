/**
 * 
 */
package org.msh.tb.client.dashboard;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import org.msh.tb.client.indicators.IndicatorWrapperPanel;
import org.msh.tb.client.indicators.ResultView;

/**
 * Simple wrapper for the indicator
 *
 * @author Ricardo Memoria
 *
 */
public class IndicatorPanel extends IndicatorWrapperPanel {

    private FlowPanel pnlIndicator;
    private ResultView view;

	/**
	 * Default constructor
	 */
	public IndicatorPanel() {
        pnlIndicator = new FlowPanel();
        pnlIndicator.setStyleName("indicator-view");
        initWidget(pnlIndicator);
	}

    @Override
    public void updateIndicator(AsyncCallback<ResultView> callback) {
        if (view == null) {
            view = new ResultView();
            pnlIndicator.add(view);
        }
        view.update(getController(), callback);
    }
}
