package org.msh.tb.medicines.orders;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.Order;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.enums.OrderStatus;
import org.msh.tb.login.UserSession;

/**
 * Generate a list of open orders of a specific unit, separated by received orders,
 * orders to authorize and open orders from the unit
 * @author Ricardo Memoria
 *
 */
@Name("openOrders")
public class OpenOrders {

	@In EntityManager entityManager;
	
	private List<Order> receivedOrders;
	private List<Order> ordersToAuthorize;
	private List<Order> unitOrders;


	/**
	 * Create the list of open orders of the selected unit
	 */
	protected void createListOrders() {
		String hql = "from Order a " +
			"join fetch a.unitFrom uf join fetch a.unitTo ut " +
			"join fetch uf.adminUnit join fetch ut.adminUnit " +
			"where (a.unitFrom.id = :unitid and a.status not in (:stREC, :stCANC))" +
			"or (a.unitTo.id = :unitid and a.status in (:stWAITSHIP, :stPREPSHIP)) " +
			"or (a.unitTo.authorizerUnit.id = :unitid and a.status = :stWAITVAL) " +
			"order by a.orderDate";
		
		UserSession userSession = (UserSession)Component.getInstance("userSession");
		
		Tbunit unit = userSession.getTbunit();

		List<Order> lst = entityManager
			.createQuery(hql)
			.setParameter("unitid", userSession.getTbunit().getId())
			.setParameter("stREC", OrderStatus.RECEIVED)
			.setParameter("stCANC", OrderStatus.CANCELLED)
			.setParameter("stWAITSHIP", OrderStatus.WAITSHIPMENT)
			.setParameter("stPREPSHIP", OrderStatus.PREPARINGSHIPMENT)
			.setParameter("stWAITVAL", OrderStatus.WAITAUTHORIZING)
			.getResultList();

		receivedOrders = new ArrayList<Order>();
		ordersToAuthorize = new ArrayList<Order>();
		unitOrders = new ArrayList<Order>();
		
		for (Order order: lst) {
			if (order.getUnitFrom().equals(unit))
				unitOrders.add(order);
			else
			if (order.getStatus() == OrderStatus.WAITAUTHORIZING)
				ordersToAuthorize.add(order);
			else				
			if (order.getUnitTo().equals(unit))
				receivedOrders.add(order);
		}
	}

	/**
	 * @return the receivedOrders
	 */
	public List<Order> getReceivedOrders() {
		if (receivedOrders == null)
			createListOrders();
		return receivedOrders;
	}

	/**
	 * @return the ordersToAutorize
	 */
	public List<Order> getOrdersToAuthorize() {
		if (ordersToAuthorize == null)
			createListOrders();
		return ordersToAuthorize;
	}

	/**
	 * @return the unitOrders
	 */
	public List<Order> getUnitOrders() {
		if (unitOrders == null)
			createListOrders();
		return unitOrders;
	}
}
