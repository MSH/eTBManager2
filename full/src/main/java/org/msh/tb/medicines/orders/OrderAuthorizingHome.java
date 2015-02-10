package org.msh.tb.medicines.orders;

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
import org.msh.tb.tbunits.TBUnitSelection;
import org.msh.tb.tbunits.TBUnitType;

import javax.persistence.EntityManager;
import java.util.Date;

@Name("orderAuthorizingHome")
public class OrderAuthorizingHome extends Controller {
	private static final long serialVersionUID = 3935785222496509854L;

	@In(create=true) Order order;
	@In(create=true) EntityManager entityManager;
	@In(create=true) OrderHome orderHome;
	@In(required=true) UserSession userSession;
	@In(create=true) FacesMessages facesMessages;

	private TBUnitSelection unitSelection;

	/**
	 * Inicializa os dados para a autoriza��o
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

		// check if the supplier must be changed
		if ((unitSelection != null) && (unitSelection.getSelected() != null) &&
				(!unitSelection.getSelected().getId().equals(order.getUnitTo().getId()))) {
			order.setUnitTo(unitSelection.getSelected());
		}

		Tbunit unit = entityManager.merge(userSession.getTbunit());
		order.setAuthorizer(unit);

		// pega os valores informados pelo usu�rio e atualiza no pedido
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

	/**
	 * Get the object for selection of the medicine supplier
	 * @return
	 */
	public TBUnitSelection getUnitSelection() {
		if (unitSelection == null) {
			unitSelection = new TBUnitSelection("autid", false, TBUnitType.MEDICINE_SUPPLIERS);
			unitSelection.setSelected(order.getUnitTo());
		}
		return unitSelection;
	}
}
