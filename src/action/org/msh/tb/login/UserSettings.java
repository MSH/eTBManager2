package org.msh.tb.login;

import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.LocaleSelector;
import org.jboss.seam.international.TimeZoneSelector;
import org.msh.mdrtb.entities.User;
import org.msh.mdrtb.entities.UserLogin;
import org.msh.mdrtb.entities.UserWorkspace;

@Name("userSettings")
public class UserSettings {

	@In(required=true) UserLogin userLogin;
	@In(create=true) EntityManager entityManager;
	@In(create=true) LocaleSelector localeSelector;
	@In(create=true) TimeZoneSelector timeZoneSelector;
	
	private List<UserWorkspace> userWorkspaces;
	
	/**
	 * Update user's preferences
	 * @return
	 */
	public String savePreferences() {
		User user = entityManager.find(User.class, userLogin.getUser().getId());

		user.setName(userLogin.getUser().getName());
		user.setEmail(userLogin.getUser().getEmail());

		// update locality
		user.setLanguage(localeSelector.getLocaleString());
		if (localeSelector.getLocaleString().equals("es_DO"))
			localeSelector.setLocaleString("es");
		localeSelector.select();

		// update time-zone
		user.setTimeZone(timeZoneSelector.getTimeZoneId());
		timeZoneSelector.select();
		
		// update database
		entityManager.persist(user);
		
		// reloads the user workspaces
		user.getWorkspaces().size();
		
		userLogin.setUser(user);
		
		return "preferenceschanged";
	}


	/**
	 * Return the list of workspaces from the logged user
	 * @return List of {@link UserWorkspace} objects
	 */
	public List<UserWorkspace> getUserWorkspaces() { 
		if (userWorkspaces == null) {
			userWorkspaces = entityManager.createQuery("from UserWorkspace uw where uw.user.id = :id order by uw.workspace.name.name1")
				.setParameter("id", userLogin.getUser().getId())
				.getResultList();
		}
		return userWorkspaces;
	}
	
	/**
	 * Just a faster way to check if the user has more than one workspace assigned to him
	 * @return
	 */
	public boolean isMultipleUserWorkspaces() {
		return getUserWorkspaces().size() > 1;
	}
}
