package org.msh.tb.medicines.orders;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.msh.tb.entities.BatchQuantity;
import org.msh.tb.entities.OrderBatch;
import org.msh.tb.entities.OrderItem;



@Name("orderBatchHome")
@Scope(ScopeType.CONVERSATION)
public class OrderBatchHome {

	@In
	private EntityManager entityManager;
	
//	@In(create=true)
//	private FacesMessages facesMessages;

	@Out(required=false)
	private OrderItem orderItem; 
	
	public List<OrderBatch> availableBatches;

	@Factory("orderItem")
	public OrderItem getItem() {
		return orderItem;
	}

	public String selectOrderItem(OrderItem it) {
		orderItem = it;
		availableBatches = null;
		return "selbatches";
	}
	
	public List<OrderBatch> getAvailableBatches() {
		if ((availableBatches == null) && (orderItem != null)) {
			createAvailableBatches();
		}
		
		return availableBatches;
	}

	
	private void createAvailableBatches() {
		int qtd = orderItem.getApprovedQuantity();
		
		List<BatchQuantity> lst = entityManager
			.createQuery("from BatchQuantity b join fetch b.batch " +
					"where b.batch.medicine.id = :medid " +
					"and b.source.id = :sourceid " +
					"and b.tbunit.id = :unitid " + 
					"order by b.batch.expiryDate")
			.setParameter("medid", orderItem.getMedicine().getId())
			.setParameter("sourceid", orderItem.getSource().getId())
			.setParameter("unitid", orderItem.getOrder().getTbunitTo().getId())
			.getResultList();
		
		availableBatches = new ArrayList<OrderBatch>();
		
		for (BatchQuantity b: lst) {
			OrderBatch batch = new OrderBatch();
			batch.setBatch(b.getBatch());
			
			if (b.getQuantity() >= qtd) {
				batch.setQuantity(qtd);
				qtd = 0;
			}
			else {
				batch.setQuantity(b.getQuantity());
				qtd = qtd - batch.getQuantity();
			}
			
			availableBatches.add(batch);
		}
	}

	/**
	 * Adiciona lotes selecionados
	 * @return
	 */
	public String addBatches() {
		int qtd = 0;
		orderItem.getBatches().clear();
		for (OrderBatch b: availableBatches) {
			if (b.getQuantity() > 0) {
				orderItem.getBatches().add(b);
				b.setOrderItem(orderItem);
				qtd += b.getQuantity();
			}
		}
		
// REMOVED THE TEST: ROMANIA DEMAND 
		// check if any batch were entered
//		if (orderItem.getBatches().size() == 0) {
//			facesMessages.addFromResourceBundle("drugs.orders.nobatchsel");
//			return "error";
//		}

		// informa quantidade enviada
		orderItem.setShippedQuantity(qtd);
		
		return "success";
	}
}
