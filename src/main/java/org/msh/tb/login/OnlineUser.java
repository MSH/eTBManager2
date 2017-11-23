package org.msh.tb.login;

import org.msh.tb.entities.UserLogin;

import java.util.Date;


public class OnlineUser {

	private UserLogin userLogin;
	private String ultimaPagina;
	private Date ultimoAcesso;


	public UserLogin getUserLogin() {
		return userLogin;
	}
	public void setUserLogin(UserLogin userLogin) {
		this.userLogin = userLogin;
	}
	public String getUltimaPagina() {
		return ultimaPagina;
	}
	public void setUltimaPagina(String ultimaPagina) {
		this.ultimaPagina = ultimaPagina;
	}
	public Date getUltimoAcesso() {
		return ultimoAcesso;
	}
	public void setUltimoAcesso(Date ultimoAcesso) {
		this.ultimoAcesso = ultimoAcesso;
	}
}
