/**
 * 
 */
package org.msh.tb.client.shared;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.msh.tb.client.shared.model.CIndicator;
import org.msh.tb.client.shared.model.CReport;

import java.util.ArrayList;

/**
 * @author Ricardo Memoria
 *
 */
public interface DashboardServiceAsync {

	void initialize(AsyncCallback<ArrayList<CReport>> callback);

}
