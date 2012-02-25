package org.msh.tb.medicines.orders;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.msh.tb.entities.Order;
import org.msh.tb.transactionlog.TransactionLogService;

/**
 * Responsible for registering in the log system the transactions performed in the system
 * @author Ricardo Memoria
 *
 */
@Name("orderTransactionLog")
public class OrderTransactionLog {
	
	@In OrderHome orderHome;

	private TransactionLogService logService = (TransactionLogService)Component.getInstance("transactionLogService", true);

	
	/**
	 * Register in the transaction log when a new order is posted
	 */
	@Observer("new-medicine-order")
	public void newOrder() {
		Order order = orderHome.getInstance();

		// register log about new order
		logService.addTableRow("Order.unitFrom", order.getUnitFrom().toString());
		logService.addTableRow("Order.unitTo", order.getUnitTo().toString());
		logService.saveExecuteTransaction("NEW_ORDER", order);
	}
	
	
	/**
	 * Register in the transaction log system an order that was canceled 
	 */
	@Observer("order-canceled")
	public void orderCanceled() {
		Order order = orderHome.getInstance();

		logService.addTableRow("Order.unitFrom", order.getUnitFrom().toString());
		logService.addTableRow("Order.unitTo", order.getUnitTo().toString());
		logService.addTableRow("Order.cancelReason", order.getCancelReason());
		logService.saveExecuteTransaction("ORDER_CANC", order);
	}

	/**
	 * Register in the transaction log system an order that was authorized 
	 */
	@Observer("medicine-order-authorized")
	public void orderAuthorized() {
		Order order = orderHome.getInstance();

		logService.addTableRow("Tbunit.authorizerUnit", order.getAuthorizer().toString());
		logService.addTableRow("Order.unitFrom", order.getUnitFrom().toString());
		logService.addTableRow("Order.unitTo", order.getUnitTo().toString());
		logService.saveExecuteTransaction("VAL_ORDER", order);
	}

	/**
	 * Register in the transaction log system an order that was shipped
	 */
	@Observer("medicine-order-shipped")
	public void registerLogOrderShipped() {
		Order order = orderHome.getInstance();

		logService.addTableRow("Order.unitFrom", order.getUnitFrom().toString());
		logService.addTableRow("Order.unitTo", order.getUnitTo().getName().toString());
		logService.addTableRow("Order.shippingDate", order.getShippingDate());
		logService.saveExecuteTransaction("SEND_ORDER", order);
	}

	/**
	 * Register in the transaction log system an order that was shipped
	 */
	@Observer("medicine-order-received")
	public void registerLogOrderReceived() {
		Order order = orderHome.getInstance();

		logService.addTableRow("Order.unitFrom", order.getUnitFrom().toString());
		logService.addTableRow("Order.unitTo", order.getUnitTo().getName().toString());
		logService.addTableRow("Order.receivingDate", order.getReceivingDate());
		logService.saveExecuteTransaction("RECEIV_ORDER", order);
	}
}
