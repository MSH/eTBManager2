package org.msh.tb.medicines.orders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.Order;
import org.msh.tb.entities.enums.OrderStatus;
import org.msh.tb.entities.enums.ShippedReceivedDiffTypes;
import org.msh.tb.login.UserSession;
import org.msh.utils.EntityQuery;

@Name("ordersHistory")
public class OrdersHistory extends EntityQuery<Order> {
	private static final long serialVersionUID = -4180773858607462034L;

	@In(create=true) UserSession userSession;
	
	private ShippedReceivedDiffTypes diffType;
	
	private static final String[] restrictions = {
		"month(o.orderDate) = #{ordersHistory.month} + 1",
		"year(o.orderDate) = #{ordersHistory.year}",
		"o.status = #{ordersHistory.os}"};
		
	private Integer month;
	private Integer year;
	private OrderStatus os;
	
	protected String getCondition() {
		String cond =  " where ((o.unitTo.id = #{userSession.tbunit.id} and o.status in (" +
						OrderStatus.SHIPPED.ordinal() + "," + OrderStatus.CANCELLED.ordinal() + "," + OrderStatus.RECEIVED.ordinal() + ")) " +
						" or (o.unitFrom.id = #{userSession.tbunit.id} " +
						" and o.status in (" +
						OrderStatus.RECEIVED.ordinal() + "," + OrderStatus.CANCELLED.ordinal() + ")) " +
						"or (o.unitTo.authorizerUnit.id = #{userSession.tbunit.id} " +
						" and o.status in (" + OrderStatus.CANCELLED.ordinal() + ", " + OrderStatus.PREPARINGSHIPMENT.ordinal() + ", " + 
						OrderStatus.RECEIVED.ordinal() + "," + OrderStatus.SHIPPED.ordinal() + ")))";
	
		if(diffType != null){
			if(diffType.equals(ShippedReceivedDiffTypes.RECEIVED_BT_SHIPPED)){
				cond += " and i.shippedQuantity < i.receivedQuantity";
			}else if(diffType.equals(ShippedReceivedDiffTypes.SHIPPED_BT_RECEIVED)){
				cond += " and i.shippedQuantity > i.receivedQuantity";
			}else if(diffType.equals(ShippedReceivedDiffTypes.BOTH)){
				cond += " and i.shippedQuantity <> i.receivedQuantity and i.receivedQuantity is not null";
			}else if(diffType.equals(ShippedReceivedDiffTypes.NONE)){
				cond += " and i.shippedQuantity = i.receivedQuantity";
			}
		}
		
		return cond;
	}

	@Override
	public String getEjbql() {
		return "from Order o" +
				" join fetch o.unitFrom" + 
				" join fetch o.items i" .concat(getCondition());
	}

	@Override
	protected String getCountEjbql() {
		return "select count(*) from Order o join o.unitFrom join o.items i".concat(getCondition());
	}

	@Override
	public List<String> getStringRestrictions() {
		return Arrays.asList(restrictions);
	}

	@Override
	public Integer getMaxResults() {
		return 30;
	}

	@Override
	public String getOrder() {
		String s = super.getOrder();

		if (s == null)
			return "o.orderDate desc";
		else return s;
	}

	public Integer getMonth() {
		return month;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public OrderStatus getReceivedStatus() {
		return OrderStatus.RECEIVED;
	}

	public OrderStatus getCanceledStatus() {
		return OrderStatus.CANCELLED;
	}

	public OrderStatus getOs() {
		return os;
	}

	public void setOs(OrderStatus os) {
		this.os = os;
	}
	
	public List<OrderStatus> getOrderStatusList(){
		ArrayList<OrderStatus> list = new ArrayList<OrderStatus>();
		
		list.add(OrderStatus.RECEIVED);
		list.add(OrderStatus.CANCELLED);
		
		if(userSession.getTbunit().isMedicineSupplier()){
			list.add(OrderStatus.SHIPPED);
		}
		
		return list;
	}

	/**
	 * @return the diffType
	 */
	public ShippedReceivedDiffTypes getDiffType() {
		return diffType;
	}

	/**
	 * @param diffType the diffType to set
	 */
	public void setDiffType(ShippedReceivedDiffTypes diffType) {
		this.diffType = diffType;
	}

}
