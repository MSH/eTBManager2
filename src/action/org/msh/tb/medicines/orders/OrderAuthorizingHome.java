package org.msh.tb.medicines.orders;

import java.util.Date;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.Controller;
import org.msh.tb.entities.Order;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.enums.OrderStatus;
import org.msh.tb.login.UserSession;
import org.msh.tb.medicines.orders.SourceOrderItem.OrderItemAux;

@Name("orderAuthorizingHome")
public class OrderAuthorizingHome extends Controller {
	private static final long serialVersionUID = 3935785222496509854L;

	@In(create=true) Order order;
	@In(create=true) EntityManager entityManager;
	@In(create=true) OrderHome orderHome;
	@In(required=true) UserSession userSession;
	@In(create=true) FacesMessages facesMessages;

	/**
	 * Inicializa os dados para a autorização
	 */
	public void initialize() {
		for (SourceOrderItem s: orderHome.getSources()) 
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
		order.setStatus(OrderStatus.WAITSHIPMENT);
		order.setApprovingDate(authDate);

		Tbunit unit = entityManager.merge(userSession.getTbunit());
		order.setAuthorizer(unit);

		// pega os valores informados pelo usuário e atualiza no pedido
		for (SourceOrderItem s: orderHome.getSources())
			for (OrderItemAux item: s.getItems()) {
				item.getItem().setApprovedQuantity(item.getApprovedQuantity());
			}
		
		entityManager.persist(order);
		orderHome.saveComment();

		facesMessages.clear();
		facesMessages.addFromResourceBundle("meds.orders.authorized");
		
		Events.instance().raiseEvent("medicine-order-authorized");
		
		return "authorized";
	}
}
