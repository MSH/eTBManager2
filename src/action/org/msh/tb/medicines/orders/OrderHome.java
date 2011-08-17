package org.msh.tb.medicines.orders;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.security.Identity;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.adminunits.AdminUnitSelection;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.Movement;
import org.msh.tb.entities.Order;
import org.msh.tb.entities.OrderItem;
import org.msh.tb.entities.Source;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.User;
import org.msh.tb.entities.enums.OrderStatus;
import org.msh.tb.log.LogService;
import org.msh.tb.login.UserSession;
import org.msh.tb.medicines.MedicineSelection;
import org.msh.tb.medicines.movs.MovementHome;
import org.msh.tb.medicines.orders.SourceOrderItem.OrderItemAux;


@Name("orderHome")
@AutoCreate
public class OrderHome extends EntityHomeEx<Order>{
	private static final long serialVersionUID = 2666375478687085792L;

	@In(create=true) FacesMessages facesMessages;
	@In(create=true) MovementHome movementHome;
	@In(create=true) UserSession userSession;
	
	private List<SourceOrderItem> sources;
	private SourceOrderItem item;
	private AdminUnitSelection auselection;
	
	@Factory("order")
	public Order getOrder() {
		return getInstance();
	}

	
	/**
	 * Return list of sources and its medicines selected
	 * @return
	 */
	public List<SourceOrderItem> getSources() {
		if (sources == null) {
			sources = new ArrayList<SourceOrderItem>();
			mountOrderSourcesList();
		}
		
		return sources;
	}
	
	/**
	 * Registra um novo pedido de medicamentos no sistema
	 * @return
	 */
	@Transactional
	public String saveNew() {
		Order order = getOrder();
		if (sources == null)
			return "error";

		// monta a lista de itens e casos do pedido
		for (SourceOrderItem s: sources) {
			for (OrderItemAux itaux: s.getItems()) {
				OrderItem it = itaux.getItem();
				if (it.getRequestedQuantity() > 0) {
					it.setOrder(order);
					order.getItems().add(it);
				}
			}
		}

		// administrative unit of the institution that will receive the medicines
		if ((auselection != null) && (auselection.getSelectedUnit() != null))
			order.setShipAdminUnit(auselection.getSelectedUnit());
		
		if (order.getItems().size() == 0) {
			facesMessages.addFromResourceBundle("meds.orders.noproduct");
			return "error";
		}
		
		OrderStatus st;
		if (order.getUnitTo().getAuthorizerUnit() != null)
			st = OrderStatus.WAITAUTHORIZING;
		else {
			st = OrderStatus.WAITSHIPMENT;
/*			order.setApprovingDate(orderDate);
			for (OrderItem it: order.getItems()) {
				it.setApprovedQuantity(it.getRequestedQuantity());
			}
*/		}
		order.setStatus(st);
		order.setOrderDate(new Date());
		order.setNumDays(order.getUnitFrom().getNumDaysOrder());
		order.setUnitTo(order.getUnitFrom().getSecondLineSupplier());
		
		User user = getEntityManager().merge(getUserLogin().getUser());
		order.setUserCreator(user);

		facesMessages.addFromResourceBundle("default.entity_created");
		
		String ret = persist();
		if (!ret.equals("persisted"))
			return ret;

		// register log about new order
		LogService logSrv = getLogService();
		logSrv.addValue(".unitFrom", order.getUnitFrom().toString());
		logSrv.addValue(".unitTo", order.getUnitTo().toString());
		logSrv.saveExecuteTransaction(order, "NEW_ORDER");
		
		return ret;
	}


	/**
	 * Update list of medicines selected
	 * @return
	 */
	public String updateMedicineSelection() {
		if (sources == null)
			return "error";

		// check if there is any medicine to order
		boolean canMove = false;
		for (SourceOrderItem it: sources) {
			it.refreshItemsWithRequest();
			if (it.getItemsWithRequest().size() > 0) {
				canMove = true;
				break;
			}
		}
		if (!canMove) {
			facesMessages.addFromResourceBundle("meds.orders.nomedicine");
			return "error";
		}
		
		return "medselection-updated";
	}


	/**
	 * Update shipping address
	 * @return
	 */
	public String updateShipAddress() {
		// recreate information about items with requested quantities
		for (SourceOrderItem item: getSources()) {
			item.refreshItemsWithRequest();
		}
		
		return "shipaddress-updated";
	}
	

	/**
	 * Cancela o pedido de medicamentos. Estorna o estoque ap�s o cancelamento
	 * @return
	 */
	@Transactional
	public String cancelOrder() {
		if (getOrder().getStatus() == OrderStatus.RECEIVED) {
			facesMessages.addFromResourceBundle("meds.orders.cannotcancel");
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

	
	/**
	 * Initialize medicine selection to be included in the order
	 * @param item specify the source where medicines will be included into
	 */
	public void initMedicineSelection(SourceOrderItem item) {
		MedicineSelection medSel = (MedicineSelection)Component.getInstance("medicineSelection", true);

		medSel.applyFilter(item.getItems(), "item.medicine");
		this.item = item;
	}


	/**
	 * Finish the selection of the medicines to be included in the order
	 */
	public void finishMedicineSelection() {
		if (item == null)
			throw new RuntimeException("No source selected to include medicines into the order");

		MedicineSelection medSel = (MedicineSelection)Component.getInstance("medicineSelection", true);
		
		List<Medicine> meds = medSel.getSelectedMedicines();
		for (Medicine med: meds) {
			// this method automatically includes a new medicine if the medicine is not already in the list
			item.itemByMedicine(med);
		}
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
		sources.clear();
		
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
		for (SourceOrderItem s: sources) {
			if (s.getSource().equals(source))
				return s;
		}
		
		SourceOrderItem s = new SourceOrderItem(source);
		sources.add(s);
		return s;
	}

	
	/**
	 * Check if the order can be authorized
	 * @return
	 */
	public boolean isCanAuthorize() {
		Order order = getInstance();
		
		if ((order.getStatus() != OrderStatus.WAITAUTHORIZING) || (!Identity.instance().hasRole("VAL_ORDER")))
			return false;
		
		Tbunit autUnit = order.getUnitTo().getAuthorizerUnit();
		Tbunit userUnit = userSession.getWorkingTbunit();

		return (autUnit == null? false: (autUnit.getId().equals(userUnit.getId())));
	}

	
	/**
	 * Check if the order can be shipped
	 * @return
	 */
	public boolean isCanShip() {
		Order order = getInstance();
		
		if ((order.getStatus() != OrderStatus.WAITSHIPMENT) || (!Identity.instance().hasRole("SEND_ORDER")))
			return false;

		Tbunit userUnit = userSession.getWorkingTbunit();
		
		return userUnit.getId().equals(order.getUnitTo().getId());
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
		
		return userUnit.getId().equals(order.getUnitFrom().getId());
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

		if ((order.getStatus() != OrderStatus.WAITAUTHORIZING) || (!Identity.instance().hasRole("NEW_ORDER")))
			return false;
		
		Tbunit userUnit = userSession.getWorkingTbunit();
		
		return userUnit.getId().equals(order.getUnitFrom().getId());		
	}

	
	/**
	 * Check if the order can be canceled. The authorizer unit and the shipping unit can cancel the order
	 * @return
	 */
	public boolean isCanCancel() {
		Order order = getInstance();

		if (((order.getStatus() != OrderStatus.WAITAUTHORIZING) &&
			(order.getStatus() != OrderStatus.WAITSHIPMENT)) ||
			(!Identity.instance().hasRole("ORDER_CANC"))) 
		{
			return false;			
		}
		
		Tbunit autUnit = order.getUnitFrom().getAuthorizerUnit();
		Tbunit userUnit = userSession.getWorkingTbunit();
		
		return ((autUnit != null)&&(userUnit.getId().equals(autUnit.getId()))) || 
			   (userUnit.getId().equals(order.getUnitFrom().getId()));				
	}


	public AdminUnitSelection getAuselection() {
		if (auselection == null)
			auselection = new AdminUnitSelection();
		return auselection;
	}
}