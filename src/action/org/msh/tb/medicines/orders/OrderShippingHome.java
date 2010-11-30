package org.msh.tb.medicines.orders;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.Controller;
import org.msh.mdrtb.entities.Batch;
import org.msh.mdrtb.entities.BatchQuantity;
import org.msh.mdrtb.entities.Movement;
import org.msh.mdrtb.entities.Order;
import org.msh.mdrtb.entities.OrderBatch;
import org.msh.mdrtb.entities.OrderItem;
import org.msh.mdrtb.entities.Source;
import org.msh.mdrtb.entities.Tbunit;
import org.msh.mdrtb.entities.enums.MovementType;
import org.msh.mdrtb.entities.enums.OrderStatus;
import org.msh.tb.medicines.movs.MovementHome;
import org.msh.tb.medicines.orders.SourceOrderItem.OrderItemAux;

@Name("orderShippingHome")
public class OrderShippingHome extends Controller {
	private static final long serialVersionUID = -3201092037704977645L;
	
	@In(create=true) Order order;
	@In(create=true) EntityManager entityManager;
	@In(create=true) MovementHome movementHome;
	@In(create=true) List<SourceOrderItem> orderSources;
	@In(create=true) FacesMessages facesMessages;

	public String registerShipping() {
		try {
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
//			Tbunit dto = order.getTbunitTo();
			
			// check if stock can be decreased
			Tbunit unitTo = order.getTbunitTo();
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
			
			MovementType type = MovementType.ORDERSHIPPING;
			// gera os movimentos de saída do pedido
			for (OrderItem it: order.getItems())
				if ((it.getShippedQuantity() != null) && (it.getShippedQuantity() > 0))
				{
					Map<Batch, Integer> batches = new HashMap<Batch, Integer>();
					for (OrderBatch ob: it.getBatches())
						batches.put(ob.getBatch(), ob.getQuantity());
					
					Movement mov = movementHome.newMovement(dtShipping, order.getTbunitTo(), it.getSource(), it.getMedicine(), type, batches, null);
					
					it.setMovementOut(mov);
				}
			
//			facesMessages.addFromResourceBundle("medicines.orders.shipped");
			
			entityManager.persist(order);
			return "persisted";
			
		} catch (Exception e) {
			facesMessages.add(e.getLocalizedMessage());
			return "error";
		}
		
	}

	
	public void initialize() {
		// retorna todos os lotes
		List<BatchQuantity> batches = entityManager.createQuery("from BatchQuantity b " +
				"join fetch b.batch bat " +
				"where b.tbunit = :unit " +
				"order by bat.expiryDate")
				.setParameter("unit", order.getTbunitTo())
				.getResultList();
		
		// percorre as fontes de medicamentos do pedido
		for (SourceOrderItem s: orderSources) {
			Source source = s.getSource();
			
			// remove itens com quantidade aprovada 0
			int i = 0;
			List<OrderItemAux> lst = s.getItems();
			while (i < lst.size()) {
				OrderItemAux it = lst.get(i);
				if ((it.getItem().getApprovedQuantity() == null) || (it.getItem().getApprovedQuantity() == 0))
					 lst.remove(i);
				else i++;
			}
			
			// percorre os itens do pedido por fonte
			for (OrderItemAux it: s.getItems()) {
				// quantidade foi definida ?
				if (it.getItem().getApprovedQuantity() != null) {
					int qtd = it.getItem().getApprovedQuantity();
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
		}

		// remove fontes que não possuam nenhum item aprovado
		int i = 0;
		while (i < orderSources.size()) {
			if (orderSources.get(i).getItems().size() == 0)
				orderSources.remove(i);
			else i++;
		}		
	}
}
