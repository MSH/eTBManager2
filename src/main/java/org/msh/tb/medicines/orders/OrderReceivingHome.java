package org.msh.tb.medicines.orders;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.Controller;
import org.jboss.seam.international.Messages;
import org.msh.tb.entities.*;
import org.msh.tb.entities.enums.MovementType;
import org.msh.tb.entities.enums.OrderStatus;
import org.msh.tb.medicines.movs.MovementHome;
import org.msh.tb.medicines.orders.SourceOrderItem.OrderItemAux;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Name("orderReceivingHome")
public class OrderReceivingHome extends Controller {
	private static final long serialVersionUID = -3888460575221647968L;

	@In(create=true) EntityManager entityManager;
	@In(create=true) MovementHome movementHome;
	@In(create=true) OrderHome orderHome;
	@In(create=true) Order order;
	@In(create=true) FacesMessages facesMessages;
	
	private List<SourceOrderItem> sources;

	
	/**
	 * Create source list from {@link OrderHome} class, but removing those items that were not shipped
	 */
	public void createSources() {
		boolean bBatchControl = order.getUnitFrom().isBatchControl();

		sources = orderHome.getSources();
		for (SourceOrderItem s: sources) {
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
		while (i < sources.size()) {
			if (sources.get(i).getItems().size() == 0)
				sources.remove(i);
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

		if (dtReceiving.before(order.getShippingDate()) || (dtReceiving.after(new Date()))) {
			facesMessages.addFromResourceBundle("meds.orders.invalidreceivingdate");
			return "error";
		}

		boolean bBatchControl = order.getUnitFrom().isBatchControl();

		if (bBatchControl) {
			updateBatchInfo();
		}
		
		// altera o status para autorizado
		order.setStatus(OrderStatus.RECEIVED);
		
		MovementType type = MovementType.ORDERRECEIVING;

		// gera os movimentos de sa�da do pedido
		movementHome.initMovementRecording();
		Map<OrderItem, Movement> itens = new HashMap<OrderItem, Movement>();
		for (OrderItem it: order.getItems())
			if ((it.getReceivedQuantity() != null) && (it.getReceivedQuantity() > 0))
			{
				Map<Batch, Integer> batches = new HashMap<Batch, Integer>();
				
				for (OrderBatch ob: it.getBatches())
					batches.put(ob.getBatch(), ob.getReceivedQuantity());

				String s = order.getUnitFrom().getName().toString();
				if ((it.getComment() != null) && (!it.getComment().isEmpty()))
					s = "[" + s + "] " + it.getComment();
	
				// create the stock movement
				Movement mov = movementHome.prepareNewMovement(dtReceiving, 
						order.getUnitFrom(), 
						it.getSource(), 
						it.getMedicine(), 
						type, 
						batches,
						s);
				itens.put(it, mov);
			}
		movementHome.savePreparedMovements();
		
		for (OrderItem it: itens.keySet()) {
			Movement mov = itens.get(it);
			it.setMovementIn(mov);
		}

		facesMessages.addFromResourceBundle("meds.orders.received");
		entityManager.persist(order);
		
		Events.instance().raiseEvent("medicine-order-received");

		return "received";		
	}


	/**
	 * Atualiza o total do recebimento pelo total dos lotes 
	 */
	public void updateBatchInfo() {
		for (OrderItem item: order.getItems()) {
			int qtd = 0;
			for (OrderBatch b: item.getBatches()) {
				if (item.getReceivedQuantity() != null)
					qtd += b.getReceivedQuantity();
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
			UIComponent txt = children.get(index - 1).getChildren().get(0);

			// coment�rio foi definido ?
			if (((UIInput)txt).getValue().toString().isEmpty()) {
				((UIInput)txt).setValid(false);

				FacesMessage message = new FacesMessage(Messages.instance().get("meds.orders.noobs"));
				context.addMessage(txt.getClientId(context), message);
			}
		}
	}
	

	public List<SourceOrderItem> getSources() {
		if (sources == null)
			createSources();
		return sources;
	}
}
