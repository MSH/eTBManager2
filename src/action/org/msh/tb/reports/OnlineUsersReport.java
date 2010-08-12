package org.msh.tb.reports;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.mdrtb.entities.UserWorkspace;
import org.msh.mdrtb.entities.Workspace;
import org.msh.tb.login.OnlineUser;
import org.msh.tb.login.OnlineUsersHome;
import org.msh.tb.login.UserSettings;

/**
 * Generate report of on-line users by workspace
 * @author Ricardo Memoria
 *
 */
@Name("onlineUsersReport")
@BypassInterceptors
public class OnlineUsersReport {

	private List<OnlineUser> items;
	private List<UserWorkspace> userWorkspaces;
	private ReportSelection reportSelection;
	
	/**
	 * Return list of on-line users
	 * @return
	 */
	public List<OnlineUser> getItems() {
		if (items == null)
			createReport();
		return items;
	}
	
	
	/**
	 * Create the report of on-line users by workspace
	 */
	protected void createReport() {
		OnlineUsersHome onlineUsersHome = getOnlineUsersHome();
		if (onlineUsersHome == null)
			return;

		items = new ArrayList<OnlineUser>();
		
		List<OnlineUser> lst = onlineUsersHome.getList();
		for (OnlineUser item: lst) {
			if (isWorkspaceInView(item.getUserLogin().getWorkspace()))
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
	public OnlineUsersHome getOnlineUsersHome() {
		return (OnlineUsersHome)Component.getInstance("onlineUsers", true);
	}

	
	public ReportSelection getReportSelection() {
		if (reportSelection == null)
			reportSelection = (ReportSelection)Component.getInstance("reportSelection");
		return reportSelection;
	}
}
