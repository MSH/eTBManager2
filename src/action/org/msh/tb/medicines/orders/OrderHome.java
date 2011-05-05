package org.msh.tb.medicines.orders;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.security.Identity;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.entities.Movement;
import org.msh.tb.entities.Order;
import org.msh.tb.entities.OrderItem;
import org.msh.tb.entities.Source;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.User;
import org.msh.tb.entities.enums.OrderStatus;
import org.msh.tb.log.LogService;
import org.msh.tb.login.UserSession;
import org.msh.tb.medicines.movs.MovementHome;
import org.msh.tb.medicines.orders.SourceOrderItem.OrderItemAux;
import org.msh.utils.date.DateUtils;


@Name("orderHome")
public class OrderHome extends EntityHomeEx<Order>{
	private static final long serialVersionUID = 2666375478687085792L;

	@In(create=true) FacesMessages facesMessages;
	@In(create=true) MovementHome movementHome;
	@In(create=true) UserSession userSession;
	
	private List<SourceOrderItem> orderSources;
	
	@Factory("order")
	public Order getOrder() {
		return getInstance();
	}

	@Factory("orderStatus")
	public OrderStatus[] getOrderStatus() {
		return OrderStatus.values();
	}
	
	@Factory("orderSources")
	public List<SourceOrderItem> createOrderSources() {
		if (orderSources == null) {
			orderSources = new ArrayList<SourceOrderItem>();
			mountOrderSourcesList();
		}
		
		return orderSources;
	}
	
	/**
	 * Registra um novo pedido de medicamentos no sistema
	 * @return
	 */
	@Transactional
	public String saveNew() {
		Order order = getOrder();
		createOrderSources();

		// monta a lista de itens e casos do pedido
		for (SourceOrderItem s: orderSources) {
			for (OrderItemAux itaux: s.getItems()) {
				OrderItem it = itaux.getItem();
				if (it.getRequestedQuantity() > 0) {
					it.setOrder(order);
					order.getItems().add(it);
				}
			}
		}
		
		if (order.getItems().size() == 0) {
			facesMessages.addFromResourceBundle("medicines.orders.noproduct");
			return "error";
		}
		Date orderDate = DateUtils.getDatePart(new Date());
		
		OrderStatus st;
		if (order.getTbunitTo().getAuthorizerUnit() != null)
			st = OrderStatus.WAITINGAUT;
		else {
			st = OrderStatus.AUTHORIZED;
/*			order.setApprovingDate(orderDate);
			for (OrderItem it: order.getItems()) {
				it.setApprovedQuantity(it.getRequestedQuantity());
			}
*/		}
		order.setStatus(st);
		order.setOrderDate(orderDate);
		order.setNumDays(order.getTbunitFrom().getNumDaysOrder());
		order.setTbunitTo(order.getTbunitFrom().getSecondLineSupplier());
		
		User user = getEntityManager().merge(getUserLogin().getUser());
		order.setUserCreator(user);

		facesMessages.addFromResourceBundle("default.entity_created");
		
		String ret = persist();
		if (!ret.equals("persisted"))
			return ret;

		// register log about new order
		LogService logSrv = getLogService();
		logSrv.addValue(".tbunitFrom", order.getTbunitFrom().toString());
		logSrv.addValue(".tbunitTo", order.getTbunitTo().toString());
		logSrv.saveExecuteTransaction(order, "NEW_ORDER");
		
		return ret;
	}
	


	/**
	 * Cancela o pedido de medicamentos. Estorna o estoque após o cancelamento
	 * @return
	 */
	@Transactional
	public String cancelOrder() {
		if (getOrder().getStatus() == OrderStatus.RECEIVED) {
			facesMessages.addFromResourceBundle("medicines.orders.cannotcancel");
			return "error";
		}
		
		getOrder().setStatus(OrderStatus.CANCELLED);

		// exclui os movimento gerados com o pedido
		for (OrderItem item: getOrder().getItems()) {
			Movement mov = item.getMovementIn();
			if (mov != null) {
				item.setMovementIn(null);
				getEntityManager().persist(item);
				movementHome.removeMovement(mov);
			}
			
			mov = item.getMovementOut();
			if (mov != null) {
				item.setMovementOut(null);
				getEntityManager().persist(item);
				movementHome.removeMovement(mov);
			}
		}

		// register log
		getLogService().saveExecuteTransaction(getInstance(), "ORDER_CANC");
		
		if (update().equals("updated"))
			 return "ordercanceled";
		else return "error";
	}
	

	@Override
	protected void updatedMessage()
	{
	}
	
	@Override
	protected void createdMessage()
	{
    }
	
	protected void mountOrderSourcesList() {
		orderSources.clear();
		
		String hql = "from OrderItem it " + 
			"join fetch it.source join fetch it.medicine " +
			"where it.order.id = #{order.id}";
		
		List<OrderItem> lst = getEntityManager().createQuery(hql).getResultList();
		for (OrderItem it: lst) {
			SourceOrderItem s = sourceOrderBySource(it.getSource());
			s.addOrderItem(it);
		}
	}


	/**
	 * Search for an object of class SourceOrderItem by its source. 
	 * If the object doesn't exist, then creates a new one
	 * @param source
	 * @return
	 */
	public SourceOrderItem sourceOrderBySource(Source source) {
		for (SourceOrderItem s: orderSources) {
			if (s.getSource().equals(source))
				return s;
		}
		
		SourceOrderItem s = new SourceOrderItem(source);
		orderSources.add(s);
		return s;
	}

	
	/**
	 * Check if the order can be authorized
	 * @return
	 */
	public boolean isCanAuthorize() {
		Order order = getInstance();
		
		if ((order.getStatus() != OrderStatus.WAITINGAUT) || (!Identity.instance().hasRole("VAL_ORDER")))
			return false;
		
		Tbunit autUnit = order.getTbunitTo().getAuthorizerUnit();
		Tbunit userUnit = userSession.getWorkingTbunit();

		return (autUnit == null? false: (autUnit.getId().equals(userUnit.getId())));
	}

	
	/**
	 * Check if the order can be shipped
	 * @return
	 */
	public boolean isCanShip() {
		Order order = getInstance();
		
		if ((order.getStatus() != OrderStatus.AUTHORIZED) || (!Identity.instance().hasRole("SEND_ORDER")))
			return false;

		Tbunit userUnit = userSession.getWorkingTbunit();
		
		return userUnit.getId().equals(order.getTbunitTo().getId());
	}
	
	
	/**
	 * Check if the order can be received
	 * @return
	 */
	public boolean isCanReceive() {
		Order order = getInstance();

		if ((order.getStatus() != OrderStatus.SHIPPED) || (!Identity.instance().hasRole("RECEIV_ORDER")))
			return false;
		
		Tbunit userUnit = userSession.getWorkingTbunit();
		
		return userUnit.getId().equals(order.getTbunitFrom().getId());
	}
	
	
	@Override
	public String remove() {
		getLogService().saveRemoveTransaction(getInstance(), "ORDERS");
		return super.remove();
	}
	
	/**
	 * Check if the order can be removed. The only one that can remove is the owner of the order
	 * @return
	 */
	public boolean isCanRemove() {
		Order order = getInstance();

		if ((order.getStatus() != OrderStatus.WAITINGAUT) || (!Identity.instance().hasRole("NEW_ORDER")))
			return false;
		
		Tbunit userUnit = userSession.getWorkingTbunit();
		
		return userUnit.getId().equals(order.getTbunitFrom().getId());		
	}

	
	/**
	 * Check if the order can be canceled. The authorizer unit and the shipping unit can cancel the order
	 * @return
	 */
	public boolean isCanCancel() {
		Order order = getInstance();

		if (((order.getStatus() != OrderStatus.WAITINGAUT) &&
			(order.getStatus() != OrderStatus.AUTHORIZED)) ||
			(!Identity.instance().hasRole("ORDER_CANC"))) 
		{
			return false;			
		}
		
		Tbunit autUnit = order.getTbunitFrom().getAuthorizerUnit();
		Tbunit userUnit = userSession.getWorkingTbunit();
		
		return ((autUnit != null)&&(userUnit.getId().equals(autUnit.getId()))) || 
			   (userUnit.getId().equals(order.getTbunitFrom().getId()));				
	}
	
}