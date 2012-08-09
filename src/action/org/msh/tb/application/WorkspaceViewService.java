package org.msh.tb.application;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.msh.tb.entities.Workspace;
import org.msh.tb.entities.WorkspaceView;

/**
 * Handle specific pages of each workspace, control page printing return right template page
 * @author Ricardo Memoria
 *
 */
@Name("workspaceViewService")
@BypassInterceptors
public class WorkspaceViewService {

	private List<WorkspaceView> views;
	private String pictureFile;
	
	

	/**
	 * Get the page name being requested
	 * @return
	 */
	protected String getPageName() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest)facesContext.getExternalContext().getRequest();
		
		String path = request.getContextPath();

		String s = request.getRequestURI();
		s = s.replaceFirst(path, "");
		s = s.replaceFirst(".seam", ".xhtml");
		return s;
	}
	
	
	/**
	 * Check if the HTTP request is a form post
	 * @return true if it's a form post
	 */
	public boolean isFormPost() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest)facesContext.getExternalContext().getRequest();

		return request.getMethod().equals("POST"); 
	}
	

	/**
	 * Return the view assigned to the default workspace
	 * @return
	 */
	public WorkspaceView getView() {
		Workspace workspace = getWorkspace();
		if (workspace == null)
			return null;

		return getViewById(workspace.getId());
	}

	
	/**
	 * Load views from application scope or from database
	 */
	protected void loadViews() {
		views = (List<WorkspaceView>)Contexts.getApplicationContext().get("workspaceViews");
		if (views == null) {
			views = getEntityManager().createQuery("from WorkspaceView").getResultList();
			Contexts.getApplicationContext().set("workspaceViews", views);
		}
	}


	/**
	 * Search for a workspace by its ID
	 * @param id
	 * @return {@link WorkspaceView} instance of the corresponding id
	 */
	public WorkspaceView getViewById(Integer id) {
		if (views == null)
			loadViews();
		
		for (WorkspaceView view: views) {
			if (view.getId().equals(id)) {
				return view;
			}
		}
		return null;
	}

	public String getLogoImage() {
		WorkspaceView view = getView();
		String path = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getContextPath();
		
		if ((view == null) || (view.getLogoImage() == null))
			 return path + "/public/themes/default/images/logo.gif";
		else return path + "/public/themes/default/images/" + view.getLogoImage();
	}
	
	
	/**
	 * Return the default workspace in use in the current user session
	 * @return
	 */
	public Workspace getWorkspace() {
		return (Workspace)Component.getInstance("defaultWorkspace");
	}


	/**
	 * Return the entity manager for JPA connections
	 * @return
	 */
	public EntityManager getEntityManager() {
		return (EntityManager)Component.getInstance("entityManager", true);
	}


	/**
	 * Update view information stored
	 * @param view
	 */
	public void updateView(WorkspaceView view) {
		if (views == null)
			loadViews();
		int index = views.indexOf(view);
		if (index >= 0) {
			views.remove(index);
		}
		views.add(view);
		Contexts.getApplicationContext().set("workspaceViews", views);
	}


	/**
	 * Return the image as a response
	 */
	public void responseImage() {
		FacesContext fc = FacesContext.getCurrentInstance();
		
		if (pictureFile == null)
			return;
		
		int index = pictureFile.lastIndexOf('.');
		String sid = pictureFile.substring(3, index);
		int id = Integer.parseInt(sid);
		
		WorkspaceView view = getViewById(id);
		HttpServletResponse response = (HttpServletResponse)fc.getExternalContext().getResponse();

		if ((view == null) || (view.getPicture() == null))
			return;

		response.reset();

		response.setContentType(view.getPictureContentType());
		response.setContentLength(view.getPicture().length);

		response.setHeader("Cache-Control", "public");
		Calendar date = Calendar.getInstance();
		date.setTime(new Date());
		response.setDateHeader("Last-modified", date.getTimeInMillis());

		date.add(Calendar.DAY_OF_YEAR, 1);
		response.setDateHeader("Expires", date.getTimeInMillis());
		response.setHeader("Cache-Control", "Public");

        OutputStream os;
		try {
			os = response.getOutputStream();
	        os.write(view.getPicture());
	        os.flush();
	        os.close();
	        fc.responseComplete();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * @return the pictureFile
	 */
	public String getPictureFile() {
		return pictureFile;
	}


	/**
	 * @param pictureFile the pictureFile to set
	 */
	public void setPictureFile(String pictureFile) {
		this.pictureFile = pictureFile;
	}
	
	
	/**
	 * Return the instance of the component
	 * @return
	 */
	public static WorkspaceViewService instance() {
		return (WorkspaceViewService)Component.getInstance(WorkspaceViewService.class);
	}
}
