/**
 * 
 */
package org.msh.tb.client.shared;

import java.util.ArrayList;

import org.msh.tb.client.shared.model.CIndicator;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Ricardo Memoria
 *
 */
public interface DashboardServiceAsync {

	void initialize(AsyncCallback<ArrayList<Integer>> callback);

	void generateIndicator(Integer id, AsyncCallback<CIndicator> callback);

}
