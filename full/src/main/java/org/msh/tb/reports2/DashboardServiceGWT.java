/**
 * 
 */
package org.msh.tb.reports2;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.remoting.WebRemote;
import org.msh.tb.client.shared.DashboardService;
import org.msh.tb.client.shared.model.CReportUIData;

/**
 * Implement the services exposed to the GWT client application
 * @author Ricardo Memoria
 *
 */
@Name("org.msh.tb.client.shared.DashboardService")
public class DashboardServiceGWT implements DashboardService {

	/** {@inheritDoc}
	 */
	@Override
	@WebRemote
	public CReportUIData initialize() {
        ReportResources.instance().prepareDashboard();
        CReportUIData data = ReportGenerator.createInitializationData(true);
        return data;
		// get list of indicators
/*
		ReportDAO dao = (ReportDAO)Component.getInstance("reportDAO");
		List<Report> reps = dao.getDashboardIndicators();
		
		ArrayList<CReport> lst = new ArrayList<CReport>();
		for (Report rep: reps) {
            CReport crep = ReportJson.convertToClient(rep);
			lst.add(crep);
		}
		return lst;
*/
	}

}
