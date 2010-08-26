package org.msh.tb.application;

import java.io.IOException;

import javax.faces.FacesException;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.jboss.seam.Component;

import com.sun.facelets.FaceletViewHandler;

/**
 * Overwrite the FaceletsViewHandler supporting URI redirection to specific pages written to an workspace.
 * It calls the component {@link WorkspaceUriRewrite} class to convert a URI to its workspace view id and vice-versa.
 * @author Ricardo Memoria
 *
 */
public class WorkspaceFaceletViewHandler extends FaceletViewHandler {

	ViewHandler parent;
	
	public WorkspaceFaceletViewHandler(ViewHandler parent) {
		super(parent);
		this.parent = parent;
	}


	protected String uriToView(FacesContext facesContext, String uri) {
		String viewid = ((WorkspaceUriRewrite)Component.getInstance("workspaceUriRewrite")).convertUriToView(facesContext, uri);
		return viewid;
	}


	protected String viewToUri(FacesContext facesContext, String viewid) {
		String uri = ((WorkspaceUriRewrite)Component.getInstance("workspaceUriRewrite")).convertViewToUri(facesContext, viewid);
		return uri;
	}
	
	@Override
	public String getActionURL(FacesContext context, String viewId) {
		viewId = viewToUri(context, viewId);
		return super.getActionURL(context, viewId);
	}


	@Override
	public UIViewRoot createView(FacesContext context, String viewId) {
		return super.createView(context, viewId);
	}


	@Override
	public UIViewRoot restoreView(FacesContext facesContext, String viewId) {
		String newView = uriToView(facesContext, viewId);
		return super.restoreView(facesContext, newView);
	}


	@Override
	public void renderView(FacesContext facesContext, UIViewRoot viewToRender)
			throws IOException, FacesException {
		
		String viewId = viewToRender.getViewId();
		viewId = uriToView(facesContext, viewId);
		viewToRender.setViewId(viewId);
		
		super.renderView(facesContext, viewToRender);
	}

}
