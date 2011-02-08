package org.msh.tb.medicines.orders;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.Controller;
import org.msh.mdrtb.entities.Batch;
import org.msh.mdrtb.entities.BatchQuantity;
import org.msh.mdrtb.entities.Movement;
import org.msh.mdrtb.entities.Order;
import org.msh.mdrtb.entities.OrderBatch;
import org.msh.mdrtb.entities.OrderItem;
import org.msh.mdrtb.entities.Source;
import org.msh.mdrtb.entities.enums.MovementType;
import org.msh.mdrtb.entities.enums.OrderStatus;
import org.msh.tb.log.LogService;
import org.msh.tb.medicines.BatchSelection;
import org.msh.tb.medicines.movs.MovementException;
import org.msh.tb.medicines.movs.MovementHome;
import org.msh.tb.medicines.orders.SourceOrderItem.OrderItemAux;
import org.msh.utils.date.DateUtils;

@Name("orderShippingHome")
@Scope(ScopeType.CONVERSATION)
public class OrderShippingHome extends Controller {
	private static final long serialVersionUID = -3201092037704977645L;
	
	@In(create=true) Order order;
	@In(create=true) EntityManager entityManager;
	@In(create=true) MovementHome movementHome;
	@In(create=true) List<SourceOrderItem> orderSources;
	@In(create=true) FacesMessages facesMessages;
	@In(create=true) BatchSelection batchSelection;
	
	private OrderItem orderItem;
	
	/**
	 * Check if notify shipment functionality was initialized
	 */
	private boolean initialized;


	/**
	 * Register shipment of order
	 * @return
	 */
	public String registerShipping() {
		Date dt = order.getApprovingDate();
		if (dt == null)
			dt = order.getOrderDate();
		
		if (dt.after(order.getShippingDate())) {
			facesMessages.addFromResourceBundle("medicines.orders.invalidshipmentdate");
			return "error";
		}
		
		// verifica se algum lote foi informado
		boolean bBatchSel = false;
		for (OrderItem it: order.getItems()) {
			if (it.getShippedQuantity() != null) {
				bBatchSel = true;
				break;
			}
		}
		if (!bBatchSel) {
			facesMessages.addFromResourceBundle("medicines.orders.nobatchsel");
			return "error";
		}
		
		// altera o status para autorizado
		OrderStatus status = OrderStatus.SHIPPED;
		order.setStatus(status);
		Date dtShipping = order.getShippingDate();
//		Tbunit dto = order.getTbunitTo();
		
		// check if stock can be decreased
/*		Tbunit unitTo = order.getTbunitTo();
		boolean canShip = true;
		for (OrderItem it: order.getItems()) {
			if (!movementHome.canDecreaseStock(unitTo, it.getSource(), 
					it.getMedicine(), it.getShippedQuantity(), order.getShippingDate())) {
				canShip = false;
				it.setData(true);
			}
		}

		if (!canShip)
			return "error";
*/		
		MovementType type = MovementType.ORDERSHIPPING;
		// gera os movimentos de saída do pedido
		boolean canShip = true;
		movementHome.initMovementRecording();
		for (OrderItem it: order.getItems())
			if ((it.getShippedQuantity() != null) && (it.getShippedQuantity() > 0))
			{
				Map<Batch, Integer> batches = new HashMap<Batch, Integer>();
				for (OrderBatch ob: it.getBatches())
					batches.put(ob.getBatch(), ob.getQuantity());

				try {
					Movement mov = movementHome.prepareNewMovement(dtShipping, 
							order.getTbunitTo(), 
							it.getSource(), 
							it.getMedicine(), 
							type, 
							batches, 
							order.getTbunitTo().toString());
					it.setMovementOut(mov);
				} catch (MovementException e) {
					facesMessages.add(e.getMessage());
					canShip = false;
					it.setData(true);
				}
			}
		
		if (!canShip)
			return "error";
		movementHome.savePreparedMovements();
		
//		facesMessages.addFromResourceBundle("medicines.orders.shipped");
		
		entityManager.persist(order);
		
		LogService log = new LogService();
		log.addValue(".shippingDate", order.getShippingDate());
		log.saveExecuteTransaction(order, "SEND_ORDER");
		
		return "persisted";	
	}


	
	/**
	 * Initialize order shipment 
	 */
	public void initialize() {
		if (initialized)
			return;
		
		// retorna todos os lotes
		List<BatchQuantity> batches = entityManager.createQuery("from BatchQuantity b " +
				"join fetch b.batch bat " +
				"where b.tbunit = :unit " +
				"and bat.expiryDate >= :dt " +
				"order by bat.expiryDate")
				.setParameter("unit", order.getTbunitTo())
				.setParameter("dt", DateUtils.getDate())
				.getResultList();
		
		// percorre as fontes de medicamentos do pedido
		for (SourceOrderItem s: orderSources) {
			Source source = s.getSource();

			// pedido foi aprovado ?
			if (order.getApprovingDate() != null) {
				// remove itens com quantidade aprovada 0
				int i = 0;
				List<OrderItemAux> lst = s.getItems();
				while (i < lst.size()) {
					OrderItemAux it = lst.get(i);
					if ((it.getItem().getApprovedQuantity() == null) || (it.getItem().getApprovedQuantity() == 0))
						 lst.remove(i);
					else i++;
				}
			}
			
			// percorre os itens do pedido por fonte
			for (OrderItemAux it: s.getItems()) {
				// quantidade foi definida ?
				OrderItem item = it.getItem();
				
				int qtd = (order.getApprovingDate() == null? item.getRequestedQuantity(): item.getApprovedQuantity());
				int qtdDeliv = 0;
				it.setUnavailable(true);
				
				// procura pelos lotes
				for (BatchQuantity batch: batches) {
					if ((batch.getSource().equals(source)) &&
						(batch.getBatch().getMedicine().equals(it.getItem().getMedicine()))) 
					{
						OrderBatch dob = new OrderBatch();

						// calcula a quantidade do lote
						int remQtd = batch.getQuantity();
						if (remQtd > qtd) {
							dob.setQuantity(qtd);
							qtd = 0;
						}
						else {
							dob.setQuantity(remQtd);
							qtd -= remQtd;
						}
						it.setUnavailable(false);
						dob.setBatch(batch.getBatch());
						dob.setOrderItem(it.getItem());
						it.getItem().getBatches().add(dob);
						qtdDeliv += dob.getQuantity();
						
						if (qtd <= 0)
							break;
					}
				}
				
				it.getItem().setShippedQuantity(qtdDeliv);
			}
		}

		// remove fontes que não possuam nenhum item aprovado
		int i = 0;
		while (i < orderSources.size()) {
			if (orderSources.get(i).getItems().size() == 0)
				orderSources.remove(i);
			else i++;
		}
		
		initialized = true;
	}


	/**
	 * Prepare the batch selection dialog to be displayed to the user
	 * @param it
	 */
	public void initBatchesSelection(OrderItem it) {
		orderItem = it;
		batchSelection.clear();
		batchSelection.setTbunit(it.getOrder().getTbunitTo());
		batchSelection.setMedicine(it.getMedicine());
		batchSelection.setSource(it.getSource());

		Map<Batch, Integer> batches = new HashMap<Batch, Integer>();
		for (OrderBatch ob: it.getBatches())
			batches.put(ob.getBatch(), ob.getQuantity());
		
		batchSelection.setSelectedBatches(batches);
	}


	/**
	 * Called when the user confirm the selection made in the batch selection dialog
	 */
	public void finishBatchesSelection() {
		Map<Batch, Integer> selectedBatches = batchSelection.getSelectedBatches();

		// update or include batches
		for (Batch batch: selectedBatches.keySet()) {
			Integer qtd = selectedBatches.get(batch);
			if ((qtd != null) && (qtd > 0)) {
				OrderBatch ob = orderItem.findOrderBatchByBatch(batch);
				if (ob != null)
					ob.setQuantity(qtd);
				else {
					ob = new OrderBatch();
					ob.setBatch(batch);
					ob.setOrderItem(orderItem);
					ob.setQuantity(qtd);
					ob.setReceivedQuantity(null);
					orderItem.getBatches().add(ob);
				}
			}
		}
		
		// remove batches
		int i = 0;
		while (i < orderItem.getBatches().size()) {
			OrderBatch ob = orderItem.getBatches().get(i);
			Integer qtd = selectedBatches.get(ob.getBatch());
			if ((qtd == null) || (qtd == 0)) {
				entityManager.remove(ob);
				orderItem.getBatches().remove(ob);
			}
			else i++;
		}
		
		// update quantity of order
		int qtd = 0;
		for (OrderBatch b: orderItem.getBatches()) {
			qtd += b.getQuantity();
		}
		orderItem.setShippedQuantity(qtd);
	}
}
