package org.msh.tb.webservices;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

import org.jboss.seam.Component;
import org.msh.tb.entities.Workspace;
import org.msh.tb.workspaces.WorkspacesQuery;


@WebService(name="workspaceService", serviceName="workspaceService")
@SOAPBinding(style=Style.RPC)
//@WebContext(contextRoot="/etbmanager", urlPattern="/services")
public class WorkspaceServiceImpl  {

	@WebMethod
	public String getWorkspaces() {
		WorkspacesQuery lst = (WorkspacesQuery)Component.getInstance("workspaces");
		
		String s = "";
		for (Workspace workspace: lst.getResultList()) {
			s += workspace.getName().toString()  + "; ";
		}

		return s;
	}

}
