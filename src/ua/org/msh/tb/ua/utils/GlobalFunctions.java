package org.msh.tb.ua.utils;

import org.jboss.seam.annotations.Name;
import org.msh.tb.application.App;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.login.UserSession;
/**
 * The class for necessary functions without associating with section
 * @author A.M.
 */
@Name("globalFunctions")
public class GlobalFunctions {
	
	public static void initializeDefaultAdminUnit() {
		UserSession.setDefaultAdminUnit((UserWorkspace) App.getComponent("userWorkspace"));
	}
}
