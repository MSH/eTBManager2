/**
 * 
 */
package org.msh.tb.client.shared;

import java.util.ArrayList;

import org.msh.tb.client.shared.model.CIndicator;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * Server services available to display the dashboard
 * 
 * @author Ricardo Memoria
 *
 */
@RemoteServiceRelativePath("../seam/resource/gwtRpc")
public interface DashboardService extends RemoteService{

	/**
	 * Return the list of indicator identifications available to be displayed in the dashboard
	 * @return
	 */
	ArrayList<Integer> initialize();
	
	/**
	 * Request from the server the indicator data of the given ID to be displayed in the dashboard
	 * @param id
	 * @return
	 */
	CIndicator generateIndicator(Integer id);
}
