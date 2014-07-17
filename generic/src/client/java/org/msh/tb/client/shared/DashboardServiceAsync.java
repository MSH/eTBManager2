/**
 * 
 */
package org.msh.tb.client.shared;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.msh.tb.client.shared.model.CIndicator;

import java.util.ArrayList;

/**
 * @author Ricardo Memoria
 *
 */
public interface DashboardServiceAsync {

	void initialize(AsyncCallback<ArrayList<Integer>> callback);

	void generateIndicator(Integer id, AsyncCallback<CIndicator> callback);

}
