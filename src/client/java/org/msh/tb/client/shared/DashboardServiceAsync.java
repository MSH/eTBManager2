/**
 * 
 */
package org.msh.tb.client.shared;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.msh.tb.client.shared.model.CReportUIData;

/**
 * @author Ricardo Memoria
 *
 */
public interface DashboardServiceAsync {

	void initialize(AsyncCallback<CReportUIData> callback);

}
