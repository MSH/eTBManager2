package org.msh.tb.misc;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.Renderer;
import org.msh.tb.application.EtbmanagerApp;

/**
 * Handle system configuration issues and centralized operations, like e-mail shippment
 * @author Ricardo Memoria
 *
 */
@Name("dmsystem")
public class DmSystemHome {
	
	@In EntityManager entityManager;
	@In(create=true) Renderer renderer;
	@In(create=true) EtbmanagerApp etbmanagerApp;
	

	public String getSystemMail()  {
		return etbmanagerApp.getConfiguration().getSystemMail();
	}
	
	public String getSystemURL() {
		return etbmanagerApp.getConfiguration().getSystemURL();
	}
	

	/**
	 * Envia uma nova mensagem de e-mail. A mensagem é uma página de e-mail no diretório /mail das páginas
	 * @param mailPage
	 * @return true=mensagem enviada com sucesso
	 */
	public boolean enviarEmail(String mailPage) {
		try {
			renderer.render("/mail/".concat(mailPage));
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}
}
