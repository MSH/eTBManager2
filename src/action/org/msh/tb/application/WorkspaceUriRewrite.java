package org.msh.tb.application;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.entities.Workspace;

@Name("workspaceUriRewrite")
@Scope(ScopeType.APPLICATION)
@BypassInterceptors
public class WorkspaceUriRewrite {

	private Map<String, Boolean> pages = new HashMap<String, Boolean>();
	private static final String customPath = "/custom/";

	/**
	 * Convert an URI page name to a view id to be used by facelets
	 * @param facesContext
	 * @param uri
	 * @return
	 */
	public String convertUriToView(FacesContext facesContext, String uri) {
		String ext = getWorkspaceExtension();
		if (ext == null)
			return uri;
		
		String extPage = "/custom/" + ext + uri;
		
		if (workspacePageExists(facesContext, extPage))
			 return extPage;
		else return uri;
	}

	
	/**
	 * Convert a view id to a URI to be used by facelets
	 * @param facesContext
	 * @param view
	 * @return
	 */
	public String convertViewToUri(FacesContext facesContext, String view) {
		String ext = getWorkspaceExtension(); 
		if (ext == null)
			return view;
		
		if (view.startsWith(customPath)) {
			// get the index of 3rd slash
			int pos = view.indexOf('/', customPath.length() + 1);
			if (pos == -1)
				return view;
			
			// remove the path /custom/<country> from the view 
			String uri = view.substring(pos, view.length() - 1);
			return uri;
		}
		else return view;
	}

	
	public String getWorkspaceExtension() {
		return (String)Component.getInstance("workspaceExtension");
	}
	
	
	/**
	 * Check if a extension page exists
	 * @param extensionPage
	 * @return
	 */
	protected boolean workspacePageExists(FacesContext facesContext, String extensionPage) {
		extensionPage = extensionPage.replaceFirst(".seam", ".xhtml");
		if (pages.containsKey(extensionPage)) {
			return pages.get(extensionPage);
		}

		String pg = ((ServletContext)facesContext.getExternalContext().getContext()).getRealPath(extensionPage);

		File pageFile = new File(pg);

		boolean exists = pageFile.exists();
		pages.put(extensionPage, exists);
		
		return exists;
	}
	
	
	/**
	 * Return the page according to its corresponding page in the custom folder 
	 * @param page
	 * @return
	 */
	public String getWorkspacePage(String page) {
		String ext = getWorkspaceExtension();

		if ((ext == null) || (ext.isEmpty()))
			return page;
		
		String pagePath = "/custom/" + ext + page;
		
		if (workspacePageExists(FacesContext.getCurrentInstance(), pagePath))
			 return pagePath;
		else return page;
	}
}
