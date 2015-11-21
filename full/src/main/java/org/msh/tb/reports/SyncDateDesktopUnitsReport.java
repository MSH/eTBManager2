package org.msh.tb.reports;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.application.App;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.Workspace;
import org.msh.tb.login.OnlineUser;
import org.msh.tb.login.OnlineUsersHome;
import org.msh.tb.login.UserSettings;
import org.msh.tb.tbunits.TbUnitHome;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Generate report of synchronized tbunits
 * @author Mauricio Santos
 *
 */
@Name("syncDateDesktopUnitsReport")
@BypassInterceptors
public class SyncDateDesktopUnitsReport {

	private List<Tbunit> items;
	private List<UserWorkspace> userWorkspaces;
	private ReportSelection reportSelection;
	
	/**
	 * Return list of on-line users
	 * @return
	 */
	public List<Tbunit> getItems() {
		if (items == null)
			createReport();
		return items;
	}
	
	
	/**
	 * Refresh the report data
	 */
	public void refresh() {
		items = null;
	}


	/**
	 * Create the report of on-line users by workspace
	 */
	protected void createReport() {
		EntityManager em = App.getEntityManager();
		if (em == null)
			return;

		items = new ArrayList<Tbunit>();
		
		List<Tbunit> lst = em.createQuery("from Tbunit where lastSyncDate is not null order by adminUnit.code")
							.getResultList();

		for (Tbunit item: lst) {
			if (isWorkspaceInView(item.getWorkspace()))
				items.add(item);
		}
	}

	
	/**
	 * Check if workspace is in view
	 * @param ws
	 * @return
	 */
	public boolean isWorkspaceInView(Workspace ws) {
		UserWorkspace userWorkspace = getReportSelection().getUserWorkspace();
		if (userWorkspace != null) {
			return (ws.equals(userWorkspace.getWorkspace()));
		}

		if (userWorkspaces == null)
			userWorkspaces = ((UserSettings)Component.getInstance("userSettings", true)).getUserWorkspaces();
		
		for (UserWorkspace aux: userWorkspaces) {
			if (aux.getWorkspace().equals(ws))
				return true;
		}
		return false;
	}


	/**
	 * Return onlineUsersHome component containing list of on-line users
	 * @return
	 */
	public TbUnitHome geTbUnitsHome() {
		return (TbUnitHome)Component.getInstance("tbunitHome", true);
	}

	
	public ReportSelection getReportSelection() {
		if (reportSelection == null)
			reportSelection = (ReportSelection)Component.getInstance("reportSelection");
		return reportSelection;
	}
}
