package org.msh.tb.medicines.orders;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.Controller;
import org.jboss.seam.international.Messages;
import org.msh.mdrtb.entities.Batch;
import org.msh.mdrtb.entities.Movement;
import org.msh.mdrtb.entities.Order;
import org.msh.mdrtb.entities.OrderBatch;
import org.msh.mdrtb.entities.OrderItem;
import org.msh.mdrtb.entities.enums.MovementType;
import org.msh.mdrtb.entities.enums.OrderStatus;
import org.msh.tb.log.LogService;
import org.msh.tb.medicines.movs.MovementHome;
import org.msh.tb.medicines.orders.SourceOrderItem.OrderItemAux;


@Name("orderReceivingHome")
public class OrderReceivingHome extends Controller {
	private static final long serialVersionUID = -3888460575221647968L;

	@In(create=true) EntityManager entityManager;
	@In(create=true) MovementHome movementHome;
	@In(create=true) List<SourceOrderItem> orderSources;
	@In(create=true) Order order;
	@In(create=true) FacesMessages facesMessages;

	/**
	 * Inicia os dados para notifica��o do pedido de medicamentos
	 */
	public void beginReceiving() {
		boolean bBatchControl = order.getTbunitFrom().isBatchControl();
		
		for (SourceOrderItem s: orderSources) {
			// remove items que n�o foram enviados
			int i = 0;
			while (i < s.getItems().size()) {
				Integer qtd = s.getItems().get(i).getItem().getShippedQuantity(); 
				if ((qtd == null) || (qtd == 0))
					 s.getItems().remove(i);
				else i++;
			}

			// percorre a lista de itens para inicializar a quantidade recebida
			for (OrderItemAux aux: s.getItems()) {
				OrderItem item = aux.getItem();
				if (item.getReceivedQuantity() == null)
					item.setReceivedQuantity(item.getShippedQuantity());
				if (bBatchControl) {
					for (OrderBatch orderBatch: item.getBatches()) {
						orderBatch.setReceivedQuantity(orderBatch.getQuantity());
					}
				}
			}
		}
		
		// remove fontes sem itens
		int i = 0;
		while (i < orderSources.size()) {
			if (orderSources.get(i).getItems().size() == 0)
				orderSources.remove(i);
			else i++;
		}
	}


	/**
	 * Notifica o recebimento de medicamentos e realiza a entrada de materiais no estoque
	 * @return
	 */
	@Transactional
	public String notifyReceiving() {
		Date dtReceiving = order.getReceivingDate();
		if (dtReceiving.before(order.getShippingDate())) {
			facesMessages.addFromResourceBundle("medicines.orders.invalidreceivingdate");
			return "error";
		}

		boolean bBatchControl = order.getTbunitFrom().isBatchControl();

		if (bBatchControl) {
			updateBatchInfo();
		}
		
		// altera o status para autorizado
		order.setStatus(OrderStatus.RECEIVED);
		
		MovementType type = MovementType.ORDERRECEIVING;
		// gera os movimentos de sa�da do pedido
		for (OrderItem it: order.getItems())
			if ((it.getReceivedQuantity() != null) && (it.getReceivedQuantity() > 0))
			{
				Map<Batch, Integer> batches = new HashMap<Batch, Integer>();
				
				for (OrderBatch ob: it.getBatches())
					batches.put(ob.getBatch(), ob.getReceivedQuantity());

				// create the stock movement
				Movement mov = movementHome.newMovement(dtReceiving, order.getTbunitFrom(), it.getSource(), it.getMedicine(), type, batches, it.getComment());
				it.setMovementIn(mov);
			}

		facesMessages.addFromResourceBundle("medicines.orders.received");
		entityManager.persist(order);

		LogService log = new LogService();
		log.addValue(".receivingDate", order.getReceivingDate());
		log.saveExecuteTransaction(order, "RECEIV_ORDER");

		return "received";		
	}


	/**
	 * Atualiza o total do recebimento pelo total dos lotes 
	 */
	public void updateBatchInfo() {
//		Tbunit unit = order.getTbunitFrom();
		
		for (OrderItem item: order.getItems()) {
			int qtd = 0;
			for (OrderBatch b: item.getBatches()) {
				qtd += b.getReceivedQuantity();
				
/*				Batch bout = b.getBatch();
				Batch bin;

				// verifica se batch existe
				List<BatchQuantity> lst = entityManager.createQuery("from BatchQuantity b " +
						"join fetch b.batch bat " +
						"where b.tbunit = :unit and b.batchNumber = :number " +
						"and b.source = :source and b.medicine = :med")
						.setParameter("unit", unit)
						.setParameter("number", bout.getBatchNumber())
						.setParameter("source", bout.getSource())
						.setParameter("med", bout.getMedicine())
						.getResultList();
				if (lst.size() > 0) {
					// grava lote no destino 
					bin = lst.get(0);
					bin.setQuantity(b.getReceivedQuantity() + bin.getQuantity());
				}
				else {
					// cria lote de recebimento
					bin = new Batch();
					bin.copyFromBatch(bout);
					bin.setTbunit(unit);
					bin.setQuantity(b.getReceivedQuantity());
					// zero because it's going to be updated during movement creation
					bin.setRemainingQuantity(0);
				}
				bin.setTotalPrice(bout.getUnitPrice() * bin.getQuantity());
				bin.setReservedQuantity(b.getReceivedQuantity());
				
				b.setBatchReceiving(bin);

				entityManager.persist(bin);
*/
			}
			
			item.setReceivedQuantity(qtd);
		}
	}


	/**
	 * Valida a quantidade informada pelo usu�rio
	 * @param context
	 * @param compQtd
	 * @param value
	 */
	public void validateQuantity(FacesContext context, UIComponent compQtd, Object value) {
		OrderBatch dobatch = (OrderBatch) ((UIParameter)compQtd.findComponent("batch")).getValue();

		// os valores s�o diferentes?
		if (dobatch.getQuantity() != (Integer)value) {
			dobatch.setReceivedQuantity((Integer)value);

			// procura por inputText com coment�rio
			UIComponent table = compQtd.getParent().getParent();
			List<UIComponent> children = table.getParent().getChildren();
			int index = children.indexOf(table);
			UIComponent txt = children.get(index - 2);

			// coment�rio foi definido ?
			if (((UIInput)txt).getValue().toString().isEmpty()) {
				((UIInput)compQtd).setValid(false);

				FacesMessage message = new FacesMessage(Messages.instance().get("medicines.orders.noobs"));
				context.addMessage(txt.getClientId(context), message);
			}
		}
	}
}
