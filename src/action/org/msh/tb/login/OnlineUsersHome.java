package org.msh.tb.login;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.context.FacesContext;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.entities.UserLogin;


/**
 * @author Ricardo
 *
 *	Mantém uma lista de usuários on-line no sistema
 */
@Name("onlineUsers")
@Scope(ScopeType.APPLICATION)
@BypassInterceptors
public class OnlineUsersHome {

	private List<OnlineUser> list = new ArrayList<OnlineUser>();

	
	/**
	 * Return the user login currently on-line
	 * @return
	 */
	public UserLogin getUserLogin() {
		return (UserLogin)Component.getInstance("userLogin", false);
	}
	
	/**
	 * Adiciona um novo usuário on-line na lista de usuários on-line
	 * @param loginUsuario
	 * @return
	 */
	public OnlineUser add(UserLogin loginUsuario) {
		OnlineUser item = new OnlineUser();
		item.setUserLogin(loginUsuario);
		
		list.add(item);
		
		return item;
	}

	/**
	 * Remove um usuário da lista de usuários on-line
	 * @param loginUsuario
	 */
	public void remove(UserLogin loginUsuario) {
		OnlineUser item = getOnlineUser(loginUsuario);
		if (item != null)
			list.remove(item);
	}

	/**
	 * Retorna informações do usuário on-line através do seu objeto loginUsuario
	 * @param loginUsuario
	 * @return
	 */
	public OnlineUser getOnlineUser(UserLogin loginUsuario) {
		OnlineUser aux = null;
		
		for (OnlineUser item: list)
			if (item.getUserLogin().getId().equals(loginUsuario.getId())) {
				aux = item;
				break;
			}

		return aux;
	}
	
	/**
	 * Atualiza as informações do usuário sobre página e data-hora do último acesso
	 * @param loginUsuario
	 */
	public void update() {
		UserLogin userLogin = getUserLogin();
		if (userLogin == null)
			return;
		
		OnlineUser item = getOnlineUser(userLogin);
		if (item == null)
			return;
		
		// atualiza a data atual de acesso
		item.setUltimoAcesso(new Date());
		
		String page = FacesContext.getCurrentInstance().getExternalContext().getRequestServletPath();
		item.setUltimaPagina(page);
	}
	
	public List<OnlineUser> getList() {
		return list;
	}


	public int getCount() {
		return list.size();
	}
}
