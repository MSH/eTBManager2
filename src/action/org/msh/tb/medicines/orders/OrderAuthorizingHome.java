package org.msh.tb.medicines.orders;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.Controller;
import org.msh.mdrtb.entities.Order;
import org.msh.mdrtb.entities.Tbunit;
import org.msh.mdrtb.entities.enums.OrderStatus;
import org.msh.tb.login.UserSession;
import org.msh.tb.medicines.orders.SourceOrderItem.OrderItemAux;

@Name("orderAuthorizingHome")
public class OrderAuthorizingHome extends Controller {
	private static final long serialVersionUID = 3935785222496509854L;

	@In(create=true) Order order;
	@In(create=true) EntityManager entityManager;
	@In(create=true) List<SourceOrderItem> orderSources;
	@In(required=true) UserSession userSession;
	@In(create=true) FacesMessages facesMessages;

	/**
	 * Inicializa os dados para a autorização
	 */
	public void initialize() {
		for (SourceOrderItem s: orderSources) 
			for (OrderItemAux item: s.getItems()) {
				if (item.getApprovedQuantity() == null)
					item.setApprovedQuantity(item.getItem().getRequestedQuantity());
			}
	}


	/**
	 * Autoriza o pedido de medicamentos para ser enviado
	 * @return
	 */
	@Transactional
	public String authorize() {
		Date authDate = new Date();
		
		// altera o status para autorizado
		order.setStatus(OrderStatus.AUTHORIZED);
		order.setApprovingDate(authDate);

		Tbunit unit = entityManager.merge(userSession.getTbunit());
		order.setAuthorizer(unit);

		// pega os valores informados pelo usuário e atualiza no pedido
		for (SourceOrderItem s: orderSources)
			for (OrderItemAux item: s.getItems()) {
				item.getItem().setApprovedQuantity(item.getApprovedQuantity());
			}
		
		facesMessages.addFromResourceBundle("medicines.orders.authorized");
		
		entityManager.persist(order);
		return "authorized";
	}
}
