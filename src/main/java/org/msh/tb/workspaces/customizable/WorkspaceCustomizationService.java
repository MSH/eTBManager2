package org.msh.tb.workspaces.customizable;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.entities.Workspace;


/**
 * Central interface to create custom code for the specific implementations of each country.
 * The class return an instance of a control object by its specific implementation in the Workspace. 
 * The name of the component must follow a standard rule as class name + _ + workspace extension
 * @author Ricardo Memoria
 *
 */
@Name("workspaceCustomizationService")
@Scope(ScopeType.SESSION)
@BypassInterceptors
public class WorkspaceCustomizationService {

	private static WorkspaceCustomizationService instance; 

	private ExamControl examControl;


	/**
	 * Return an instance of the class
	 * @return
	 */
	public static WorkspaceCustomizationService instance() {
		if (instance == null) {
			instance = (WorkspaceCustomizationService) Component.getInstance("workspaceCustomizationService", true);
		}
		return instance;
	}


	/**
	 * Return instance of ExamControl class
	 * @return
	 */
	public ExamControl getExamControl() {
		if (examControl == null) {
			examControl = (ExamControl)getComponentFromDefaultWorkspace("examControl");
			
			if (examControl == null)
				examControl = new ExamControl();
		}
		return examControl;
	}


	/**
	 * Return an instance of a component. The name of the component must be composed of the name parameter 
	 * plus the suffix _ws, where ws is the extension of the default workspace.
	 * @param name
	 * @return
	 */
	protected Object getComponentFromDefaultWorkspace(String name) {
		Workspace ws = getDefaultWorkspace();
		String s = name + "_" + ws.getExtension();
		return Component.getInstance(s, true);
	}


	/**
	 * Return the default workspace
	 * @return instance of {@link Workspace} class
	 */
	protected Workspace getDefaultWorkspace() {
		return (Workspace)Component.getInstance("defaultWorkspace", true);
	}

	
	@Observer("change-workspace")
	public void workspaceChangeListener() {
		examControl = null;
		WorkspaceCustomizationService.instance = null;
	}


	@Observer("org.jboss.seam.preDestroyContext.SESSION")
	public void destroySessionListener() {
		workspaceChangeListener();
	}
}
