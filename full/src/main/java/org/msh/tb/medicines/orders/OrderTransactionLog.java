package org.msh.tb.medicines.orders;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.msh.etbm.commons.transactionlog.ActionTX;
import org.msh.tb.entities.Order;
import org.msh.tb.entities.enums.RoleAction;

/**
 * Responsible for registering in the log system the transactions performed in the system
 * @author Ricardo Memoria
 *
 */
@Name("orderTransactionLog")
public class OrderTransactionLog {
	
	@In OrderHome orderHome;

	//private TransactionLogService logService = (TransactionLogService)Component.getInstance("transactionLogService", true);

	
	/**
	 * Register in the transaction log when a new order is posted
	 */
	@Observer("new-medicine-order")
	public void newOrder() {
		Order order = orderHome.getInstance();

		ActionTX atx = ActionTX.begin("NEW_ORDER", order, RoleAction.EXEC);

		// register log about new order
		atx.getDetailWriter().addTableRow("Order.unitFrom", order.getUnitFrom().toString());
		atx.getDetailWriter().addTableRow("Order.unitTo", order.getUnitTo().toString());

		atx.end();

//		logService.saveExecuteTransaction("NEW_ORDER", order);
	}
	
	
	/**
	 * Register in the transaction log system an order that was canceled 
	 */
	@Observer("order-canceled")
	public void orderCanceled() {
		Order order = orderHome.getInstance();

		ActionTX atx = ActionTX.begin("ORDER_CANC", order, RoleAction.EXEC);

		atx.getDetailWriter().addTableRow("Order.unitFrom", order.getUnitFrom().toString());
		atx.getDetailWriter().addTableRow("Order.unitTo", order.getUnitTo().toString());
		atx.getDetailWriter().addTableRow("Order.cancelReason", order.getCancelReason());

		atx.end();
//		logService.saveExecuteTransaction("ORDER_CANC", order);
	}

	/**
	 * Register in the transaction log system an order that was authorized 
	 */
	@Observer("medicine-order-authorized")
	public void orderAuthorized() {
		Order order = orderHome.getInstance();

		ActionTX atx = ActionTX.begin("VAL_ORDER", order, RoleAction.EXEC);

		atx.getDetailWriter().addTableRow("Tbunit.authorizerUnit", order.getAuthorizer().toString());
		atx.getDetailWriter().addTableRow("Order.unitFrom", order.getUnitFrom().toString());
		atx.getDetailWriter().addTableRow("Order.unitTo", order.getUnitTo().toString());

		atx.end();
//		logService.saveExecuteTransaction("VAL_ORDER", order);
	}

	/**
	 * Register in the transaction log system an order that was shipped
	 */
	@Observer("medicine-order-shipped")
	public void registerLogOrderShipped() {
		Order order = orderHome.getInstance();

		ActionTX atx = ActionTX.begin("SEND_ORDER", order, RoleAction.EXEC);

		atx.getDetailWriter().addTableRow("Order.unitFrom", order.getUnitFrom().toString());
		atx.getDetailWriter().addTableRow("Order.unitTo", order.getUnitTo().getName().toString());
		atx.getDetailWriter().addTableRow("Order.shippingDate", order.getShippingDate());

		atx.end();
//		logService.saveExecuteTransaction("SEND_ORDER", order);
	}

	/**
	 * Register in the transaction log system an order that was shipped
	 */
	@Observer("medicine-order-received")
	public void registerLogOrderReceived() {
		Order order = orderHome.getInstance();

		ActionTX atx = ActionTX.begin("RECEIV_ORDER", order, RoleAction.EXEC);

		atx.getDetailWriter().addTableRow("Order.unitFrom", order.getUnitFrom().toString());
		atx.getDetailWriter().addTableRow("Order.unitTo", order.getUnitTo().getName().toString());
		atx.getDetailWriter().addTableRow("Order.receivingDate", order.getReceivingDate());

		atx.end();
//		logService.saveExecuteTransaction("RECEIV_ORDER", order);
	}
}
