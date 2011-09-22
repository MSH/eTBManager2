package org.msh.tb.uitheme;

import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.msh.tb.entities.UITheme;
import org.msh.tb.entities.User;
import org.msh.tb.entities.UserLogin;

/**
 * Manages the theme in use by the user
 * @author Ricardo Memoria
 *
 */
@Name("themeManager")
@AutoCreate
public class ThemeManager {

	@In UserLogin userLogin;
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
	 * Select the user's new theme 
	 * @return "theme-selected" is successfully changed
	 */
	public String selectTheme() {
		if (theme == null)
			return "error";
		theme = entityManager.merge(theme);

		// update in memory
		userLogin.getUser().setTheme(theme);
		
		// update in the database
		User user = userLogin.getUser();
		user = entityManager.merge(user);
		user.setTheme(theme);
		entityManager.persist(user);
		
		String url = theme.getUrl();
		Contexts.getSessionContext().set("themeurl", url);
		
		return "theme-selected";
	}
	
	public UITheme getTheme() {
		if (theme == null)
			restoreUserTheme();
		return theme;
	}
	
	/**
	 * Restore the theme in use by the user 
	 */
	protected void restoreUserTheme() {
		theme = entityManager.merge( userLogin.getUser().getTheme() );
	}
	
	
	/**
	 * Return the default theme
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
