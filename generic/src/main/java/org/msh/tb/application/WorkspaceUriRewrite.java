package org.msh.tb.application;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import java.io.File;
import java.util.*;

/**
 * Handle conversion from URI page address to custom workspace pages. It also contains the factory to "wspage" component, which
 * converts URI to custom workspace pages
 * @author Ricardo Memoria
 *
 */
@Name("workspaceUriRewrite")
@Scope(ScopeType.APPLICATION)
@BypassInterceptors
public class WorkspaceUriRewrite {

	private Map<String, Boolean> pages = new HashMap<String, Boolean>();
	private Boolean developmentMode;
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

	
	/**
	 * Return the workspace extension in use
	 * @return
	 */
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

		if (developmentMode == null)
			developmentMode = EtbmanagerApp.instance().isDevelopmentMode();

		// if under development mode, workspace pages address are not cached in memory
		if ((!developmentMode) && (pages.containsKey(extensionPage))) {
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
	
	
	/**
	 * Factory to return a single instance of a Map to convert page address in custom workspace pages
	 * @return
	 */
	@Factory(value="wspage", autoCreate=true, scope=ScopeType.EVENT)
	public Map<String, String> getWorkspacePageMap() {

		return new AbstractMap<String, String>() {
            @Override
            public String get(Object key) {
            	return key == null? null: getWorkspacePage(key.toString());
            }

			@Override
			public Set<java.util.Map.Entry<String, String>> entrySet() {
				return new HashSet();
			}
			
		};
	}
}
