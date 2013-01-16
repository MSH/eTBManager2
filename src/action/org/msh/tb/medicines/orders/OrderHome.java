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
import org.jboss.seam.core.Events;
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
import org.msh.tb.login.UserSession;
import org.msh.tb.medicines.MedicineSelection;
import org.msh.tb.medicines.movs.MovementHome;
import org.msh.tb.medicines.orders.SourceOrderItem.OrderItemAux;
import org.msh.tb.test.dbgen.SaveCaseAction;



@Name("orderHome")
@AutoCreate
public class OrderHome extends EntityHomeEx<Order>{
	private static final long serialVersionUID = 2666375478687085792L;

	@In(create=true) FacesMessages facesMessages;
	@In(create=true) MovementHome movementHome;
	@In(create=true) UserSession userSession;
	@In(create=true) OrderCommentHome orderCommentsHome;
	
	private List<SourceOrderItem> sources;
	private SourceOrderItem item;
	private AdminUnitSelection auselection;
	private Date receivingDateModif;
	private boolean saving;
	private boolean requiredObservation;
	
	private String cancelReason;
	
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
			s.clearNonRequestedOrderItemAux();
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
		else st = OrderStatus.WAITSHIPMENT;

		order.setStatus(st);
		order.setOrderDate(new Date());
		order.setNumDays(order.getUnitFrom().getNumDaysOrder());
		order.setUnitTo(order.getUnitFrom().getSecondLineSupplier());
		
		User user = getEntityManager().merge(getUserLogin().getUser());
		order.setUserCreator(user);

		String ret = persist();
		if (!ret.equals("persisted"))
			return ret;

		saveComment();
		
		facesMessages.clear();
		facesMessages.addFromResourceBundle("default.entity_created");
		
		setRequiredObservation(false);
		Events.instance().raiseEvent("new-medicine-order");
		
		return ret;
	}


	/**
	 * Update list of medicines selected
	 * @return
	 */
	public String updateMedicineSelection() {
		if (sources == null)
			return "error";

		if(requiredObservation==true && orderCommentsHome.getInstance().getComment().isEmpty()){
			facesMessages.addToControlFromResourceBundle("ordercomments", "form.required");
			return "";
		}
		
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
	 * Cancela o pedido de medicamentos. Estorna o estoque após o cancelamento
	 * @return
	 */
	@Transactional
	public String cancelOrder() {
		Order order = getOrder();
		
		if (order.getStatus() == OrderStatus.RECEIVED) {
			facesMessages.addFromResourceBundle("meds.orders.cannotcancel");
			return "error";
		}
		
		// limit the number of chars
		if ((cancelReason != null) && (cancelReason.length() > 200)) {
			facesMessages.addToControlFromResourceBundle("edtcanceltext", "javax.faces.validator.LengthValidator.MAXIMUM", 200);
			return "error";
		}
		
		order.setStatus(OrderStatus.CANCELLED);
		order.setCancelReason(cancelReason);
		
		movementHome.initMovementRecording();
		// exclui os movimento gerados com o pedido
		for (OrderItem item: getOrder().getItems()) {
			Movement mov = item.getMovementIn();
			if (mov != null) {
				item.setMovementIn(null);
				getEntityManager().persist(item);
				movementHome.prepareMovementsToRemove(mov);
			}
			
			mov = item.getMovementOut();
			if (mov != null) {
				item.setMovementOut(null);
				getEntityManager().persist(item);
				movementHome.prepareMovementsToRemove(mov);
			}
		}
		movementHome.savePreparedMovements();
		
		Events.instance().raiseEvent("order-canceled");
		
		if (update().equals("updated"))
			 return "ordercanceled";
		else return "error";
	}

	public String selectOpenOrder(){
		OpenOrders o = (OpenOrders)Component.getInstance("openOrders");
		List<Order> lst = o.getUnitOrders();
		if(lst != null && lst.size() > 0){
			setId(lst.get(0).getId());
			return "open-order";
		}
		else{
			setId(null);
			return "no-open-order";
		}
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

	
	public void saveComment(){
		orderCommentsHome.addComment();
		orderCommentsHome.setId(null);
	}

	/**
	 * Finish the selection of the medicines to be included in the order
	 */
	public void finishMedicineSelection() {
		if (item == null)
			throw new RuntimeException("No source selected to include medicines into the order");

		MedicineSelection medSel = (MedicineSelection)Component.getInstance("medicineSelection", true);
		
		List<Medicine> meds = medSel.getSelectedMedicines();
		
		if(meds != null && meds.size() > 0)
			requiredObservation = true;
			
		for (Medicine med: meds) {
			// this method automatically includes a new medicine if the medicine is not already in the list
			item.itemByMedicine(med);
		}
		
	}
	
	/**
	 * Verifies if an order can be edited
	 */
	public boolean isEditableOrder(Object n) {
		Order o = null;
		if(n!= null && n instanceof Order)
			o = (Order) n;
		else
			return false;
		
		return ( (Identity.instance().hasRole("NEW_ORDER")) // user has to be able to create new orders
					&& (o.getUnitFrom().equals(UserSession.getUserWorkspace().getTbunit()) || UserSession.getUserWorkspace().isPlayOtherUnits()) // the user has to be from the unit that he is trying to create/edit the order or he has to be able to play other units.
					&& (o.getStatus().equals(OrderStatus.WAITAUTHORIZING) || (o.getAuthorizer() == null && o.getStatus().equals(OrderStatus.WAITSHIPMENT)))); // The order has to be waiting for authorization or has to be waiting for shipment if it doens't needs to be authorized.
	}
	
	/**
	 * Verifies if user can comment a order
	 */
	public boolean isCanComment() {
		boolean a = (getInstance().getUnitFrom().equals(UserSession.getUserWorkspace().getTbunit()));
		boolean b = (getInstance().getUnitTo().equals(UserSession.getUserWorkspace().getTbunit()));
		
		if( (!UserSession.getUserWorkspace().isPlayOtherUnits()) 
				&& !( (getInstance().getUnitFrom().equals(UserSession.getUserWorkspace().getTbunit()))
						|| (getInstance().getUnitTo().equals(UserSession.getUserWorkspace().getTbunit())) ))
			return false;

		return (Identity.instance().hasRole("NEW_ORDER") || 
				Identity.instance().hasRole("VAL_ORDER") || 
				Identity.instance().hasRole("SEND_ORDER") || 
				Identity.instance().hasRole("RECEIV_ORDER"));
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
	
	public void modifyReceivingDate(){
		receivingDateModif = getInstance().getReceivingDate();
		saving = true;
	}
	
	public void saveReceivingDate(){
		if(receivingDateModif != null){
			if(receivingDateModif.before(getInstance().getShippingDate())){
				facesMessages.addToControlFromResourceBundle("receivingDateModif", "meds.orders.invalidreceivingdate");
				saving = true;
				return;
			}else if(receivingDateModif.after(new Date())){
				facesMessages.addToControlFromResourceBundle("receivingDateModif", "validator.notfuture");
				saving = true;
				return;
			}else{
				getInstance().setReceivingDate(receivingDateModif);
				this.persist();
				saving = false;
			}
		}
		
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
//		getLogService().save("ORDERS", getInstance().toString(), getInstance().getId());
		String s = super.remove();
		this.setId(null);
		return s;
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


	/**
	 * @return the cancelReason
	 */
	public String getCancelReason() {
		return cancelReason;
	}


	/**
	 * @param cancelReason the cancelReason to set
	 */
	public void setCancelReason(String cancelReason) {
		this.cancelReason = cancelReason;
	}

	/**
	 * @return the receivingDateModif
	 */
	public Date getReceivingDateModif() {
		return receivingDateModif;
	}

	/**
	 * @param receivingDateModif the receivingDateModif to set
	 */
	public void setReceivingDateModif(Date receivingDateModif) {
		this.receivingDateModif = receivingDateModif;
	}


	/**
	 * @return the saving
	 */
	public boolean isSaving() {
		return saving;
	}


	/**
	 * @param saving the saving to set
	 */
	public void setSaving(boolean saving) {
		this.saving = saving;
	}

	/**
	 * @return the requiredObservation
	 */
	public boolean isRequiredObservation() {
		return requiredObservation;
	}

	/**
	 * @param requiredObservation the requiredObservation to set
	 */
	public void setRequiredObservation(boolean requiredObservation) {
		this.requiredObservation = requiredObservation;
	}	
		
	public boolean isEditing(){
		Order ord = getInstance();
		return !(ord.getId() == null || ord.getId().equals(0));
	}
}