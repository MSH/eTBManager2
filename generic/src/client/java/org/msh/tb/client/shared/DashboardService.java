/**
 * 
 */
package org.msh.tb.client.shared;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import org.msh.tb.client.shared.model.CIndicator;
import org.msh.tb.client.shared.model.CReport;

import java.util.ArrayList;

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
	ArrayList<CReport> initialize();
}
