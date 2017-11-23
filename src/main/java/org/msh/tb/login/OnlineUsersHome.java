package org.msh.tb.login;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.entities.UserLogin;

import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * @author Ricardo
 *
 *	Mant�m uma lista de usu�rios on-line no sistema
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
	 * Adiciona um novo usu�rio on-line na lista de usu�rios on-line
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
	 * Remove um usu�rio da lista de usu�rios on-line
	 * @param loginUsuario
	 */
	public void remove(UserLogin loginUsuario) {
		OnlineUser item = getOnlineUser(loginUsuario);
		if (item != null)
			list.remove(item);
	}

	/**
	 * Retorna informa��es do usu�rio on-line atrav�s do seu objeto loginUsuario
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
	 * Atualiza as informa��es do usu�rio sobre p�gina e data-hora do �ltimo acesso
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
