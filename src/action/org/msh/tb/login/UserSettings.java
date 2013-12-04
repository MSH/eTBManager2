package org.msh.tb.login;

import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.LocaleSelector;
import org.jboss.seam.international.TimeZoneSelector;
import org.msh.tb.entities.User;
import org.msh.tb.entities.UserLogin;
import org.msh.tb.entities.UserWorkspace;

@Name("userSettings")
public class UserSettings {

	private List<UserWorkspace> userWorkspaces;
	
	/**
	 * Update user's preferences
	 * @return
	 */
	public String savePreferences() {
		EntityManager em = (EntityManager)Component.getInstance("entityManager");
		UserLogin userLogin = (UserLogin)Component.getInstance("userLogin", true);
		
		User user = em.find(User.class, userLogin.getUser().getId());

		user.setName(userLogin.getUser().getName());
		user.setEmail(userLogin.getUser().getEmail());

		// update locality
/*		LocaleSelector localeSelector = LocaleSelector.instance();
		user.setLanguage(localeSelector.getLocaleString());
		localeSelector.select();
*/
		// update time-zone
		TimeZoneSelector timeZoneSelector = TimeZoneSelector.instance();
		user.setTimeZone(timeZoneSelector.getTimeZoneId());
		timeZoneSelector.select();

		// update database
		em.persist(user);
		
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
			EntityManager em = (EntityManager)Component.getInstance("entityManager");
			UserLogin userLogin = (UserLogin)Component.getInstance("userLogin");
			userWorkspaces = em.createQuery("from UserWorkspace uw where uw.user.id = :id order by uw.workspace.name.name1")
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


	/**
	 * @return the localeString
	 */
	public String getLocaleString() {
		// return null just to avoid the parameter to be included in evey page
		return null;
	}


	/**
	 * @param localeString the localeString to set
	 */
	public void setLocaleString(String localeString) {
		LocaleSelector localeSelector = LocaleSelector.instance();
		if ((localeString != null) && (!localeString.equals(localeSelector.getLocaleString()))) {
			String matchedLocale = null;
			String closestLocale = null;
			// check if locale is supported and find the best one in the list
			for (SelectItem item: localeSelector.getSupportedLocales()) {
				String loc = (String)item.getValue();
				if (loc.equals(localeString)) {
					matchedLocale = loc;
					break;
				}
				if ((loc.startsWith(localeString)) && (closestLocale == null))
					closestLocale = loc;
			}

			if ((closestLocale == null) && (matchedLocale == null))
				matchedLocale = FacesContext.getCurrentInstance().getApplication().getDefaultLocale().toString();

			if (matchedLocale != null)
				 localeSelector.setLocaleString(matchedLocale);
			else localeSelector.setLocaleString(closestLocale);
			localeSelector.select();
		}
		updateUserLanguage(localeSelector.getLocaleString());
	}
	
	
	/**
	 * Update the user selected language
	 */
	private void updateUserLanguage(String lang) {
		if (lang == null)
			return;
		EntityManager em = (EntityManager)Component.getInstance("entityManager");
		UserLogin userLogin = (UserLogin)Component.getInstance("userLogin", true);
		
		User user = em.find(User.class, userLogin.getUser().getId());
		if (lang.equals(user.getLanguage()))
			return;
		user.setLanguage(lang);
		em.persist(user);
		em.flush();
	}
}
