package org.msh.tb.uitheme;

import java.util.List;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.msh.tb.application.App;
import org.msh.tb.entities.UITheme;
import org.msh.tb.entities.User;
import org.msh.tb.entities.UserLogin;

/**
 * Manages the selection of the UI theme in use by the user logged into the system
 * @author Ricardo Memoria
 *
 */
@Name("themeManager")
@AutoCreate
public class ThemeManager {

	@In EntityManager entityManager;
	
	private List<UITheme> themes;
	private UITheme theme;
	
	/**
	 * Return the list of available themes
	 * @return
	 */
	public List<UITheme> getThemes() {
		if (themes == null)
			themes = entityManager.createQuery("from UITheme").getResultList();
		return themes;
	}

	
	/**
	 * Factory to return the UI theme URL of the user logged in the system
	 * @return
	 */
	@Factory(value="themeurl", scope=ScopeType.SESSION)
	public String createThemeUrl() {
		return getThemeUrl( getTheme() );
	}

	
	/**
	 * Return the URL for the folder with the CSS files and images of the given UI theme 
	 * @param theme
	 * @return
	 */
	public String getThemeUrl(UITheme theme) {
		if  (theme.isSystemTheme()) {
			HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
			return request.getContextPath() + "/public/themes/" + theme.getPath();
		}
		else return theme.getPath();
	}
	
	
	/**
	 * Change the user's UI theme. The new UI theme in use will be the one selected in the theme field. 
	 * @return "theme-selected" is successfully changed
	 */
	public String selectTheme() {
		if (theme == null)
			return "error";
		
		UserLogin userLogin = (UserLogin)App.getComponent("userLogin");
		// user not logged in?
		if (userLogin == null) {
			throw new RuntimeException("User not logged in");
		}
		
		theme = entityManager.merge(theme);

		// update in memory
		userLogin.getUser().setTheme(theme);
		
		// update in the database
		User user = userLogin.getUser();
		user = entityManager.merge(user);
		user.setTheme(theme);
		entityManager.persist(user);
		
		Contexts.getSessionContext().remove("themeurl");
		
		return "theme-selected";
	}


	/**
	 * Get the theme in use by the current user
	 * @return
	 */
	public UITheme getTheme() {
		if (theme == null)
			restoreUserTheme();
		return theme;
	}
	
	/**
	 * Restore the theme in use by the user. If no theme is selected by the user, the default UI theme is selected
	 * using the getDefaultTheme() method  
	 */
	protected void restoreUserTheme() {
		UserLogin userLogin = (UserLogin)App.getComponent("userLogin");
		// is user logged in ?
		if (userLogin != null) {
			User user = entityManager.merge(userLogin.getUser());
			theme = user.getTheme();
		}
		
		// if no theme was selected, use the default theme
		if (theme == null)
			theme = getDefaultTheme();
	}
	
	
	/**
	 * Return the default theme. If there is no default theme, the first of the list will be returned
	 * @return
	 */
	public UITheme getDefaultTheme() {
		for (UITheme item: getThemes())
			if (item.isDefaultTheme())
				return item;
		
		return getThemes().get(0);
	}
	
	public static ThemeManager instance() {
		return (ThemeManager)Component.getInstance("themeManager", true);
	}


	/**
	 * @param theme the theme to set
	 */
	public void setTheme(UITheme theme) {
		this.theme = theme;
	}


	public void setThemeId(Integer id) {
		if (id == null)
			 theme = null;
		else theme = entityManager.find(UITheme.class, id);
	}
	
	
	public Integer getThemeId() {
		return (theme != null? theme.getId(): null);
	}
}
