package org.msh.tb.misc;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.Renderer;
import org.msh.tb.application.EtbmanagerApp;

import javax.persistence.EntityManager;

/**
 * Handle system configuration issues and centralized operations, like e-mail shippment
 * @author Ricardo Memoria
 *
 */
@Name("dmsystem")
public class DmSystemHome {
	
	@In EntityManager entityManager;
	@In(create=true) EtbmanagerApp etbmanagerApp;
	

	public String getSystemMail()  {
		return etbmanagerApp.getConfiguration().getSystemMail();
	}
	
	public String getSystemURL() {
		return etbmanagerApp.getConfiguration().getSystemURL();
	}
	

	/**
	 * Envia uma nova mensagem de e-mail. A mensagem � uma p�gina de e-mail no diret�rio /mail das p�ginas
	 * @param mailPage
	 * @return true=mensagem enviada com sucesso
	 */
	public boolean enviarEmail(String mailPage) {
		try {
			Renderer.instance().render("/mail/".concat(mailPage));
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}
}
